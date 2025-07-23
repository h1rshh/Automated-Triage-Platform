package com.example.alert_analyzer.service;

import com.example.alert_analyzer.model.ParsedAlert;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.conversations.ConversationsHistoryRequest;
import com.slack.api.methods.response.conversations.ConversationsHistoryResponse;
import com.slack.api.model.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SlackService {

    @Value("${slack.bot.token}")
    private String slackToken;

    private static final List<Pattern> ALERT_PATTERNS = List.of(
            // Pattern 1: For complex, multi-line SQL alerts
            Pattern.compile(
                    "DB\\s*:\\s*(?<database>.*?)\\n" +
                            "USER\\s*:\\s*(?<user>.*?)\\n" +
                            "HOST\\s*:\\s*(?<host>.*?)\\n" +
                            "TIME_IN_SEC\\s*:\\s*(?<timeInSec>.*?)\\n" +
                            "INFO\\s*:\\s*(?<info>[\\s\\S]*?)" +
                            "LOGGED_AT\\s*:\\s*(?<loggedAt>.*?)\\n" +
                            "STATUS\\s*:\\s*(?<status>.*)",
                    Pattern.MULTILINE
            ),
            // Pattern 2: For simple, single-line code alerts
            Pattern.compile(
                    "\\[(?<level>CRITICAL|ERROR|WARNING)\\]\\s+" +
                            "\\[(?<service>[\\w-]+)\\]\\s+-\\s+" +
                            "Message:\\s*(?<message>.*?)," +
                            "(?:\\s*Function:\\s*(?<function>[\\w()]+),)?" + // Optional function
                            "(?:\\s*Table:\\s*(?<table>[\\w]+),)?" +      // Optional table
                            "\\s*TraceID:\\s*(?<traceId>[\\w-]+)"
            )
    );

    public List<ParsedAlert> fetchRecentAlerts(String channelId) {
        List<ParsedAlert> alerts = new ArrayList<>();
        MethodsClient client = Slack.getInstance().methods(slackToken);
        ConversationsHistoryRequest request = ConversationsHistoryRequest.builder()
                .channel(channelId)
                .limit(20)
                .build();

        try {
            ConversationsHistoryResponse response = client.conversationsHistory(request);
            if (response.isOk()) {
                for (Message message : response.getMessages()) {
                    String[] potentialAlerts = message.getText().split("\\n\\n");
                    for (String textBlock : potentialAlerts) {
                        for (Pattern pattern : ALERT_PATTERNS) {
                            Matcher matcher = pattern.matcher(textBlock.trim());
                            if (matcher.find()) {
                                alerts.add(parseMatchToAlert(matcher, message));
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching from Slack: " + e.getMessage());
        }
        return alerts;
    }

    // --- FIX: This method is now much safer and checks for nulls ---
    private ParsedAlert parseMatchToAlert(Matcher matcher, Message message) {
        ParsedAlert.ParsedAlertBuilder builder = ParsedAlert.builder().id(message.getTs());

        // Safely get a value from a capture group, returning null if it doesn't exist
        String db = getGroupValue(matcher, "database");
        String user = getGroupValue(matcher, "user");
        String host = getGroupValue(matcher, "host");
        String timeInSec = getGroupValue(matcher, "timeInSec");
        String info = getGroupValue(matcher, "info");
        String loggedAt = getGroupValue(matcher, "loggedAt");
        String status = getGroupValue(matcher, "status");
        String level = getGroupValue(matcher, "level");
        String service = getGroupValue(matcher, "service");
        String msg = getGroupValue(matcher, "message");
        String func = getGroupValue(matcher, "function");
        String table = getGroupValue(matcher, "table");

        if (db != null) { // This is a SQL Alert
            builder.database(db)
                    .user(user)
                    .host(host)
                    .timeInSec(timeInSec)
                    .info(info)
                    .loggedAt(loggedAt)
                    .status(status);
        } else if (level != null) { // This is a Code Alert
            builder.status(level)
                    .service(service)
                    .info(msg)
                    .function(func)
                    .database(table) // Use 'database' field for table name for consistency
                    .loggedAt(message.getTs()); // Use message timestamp for loggedAt
        }
        return builder.build();
    }

    // --- NEW: A safe helper method to get a group's value ---
    private String getGroupValue(Matcher matcher, String groupName) {
        try {
            String value = matcher.group(groupName);
            return value != null ? value.trim() : null;
        } catch (IllegalArgumentException e) {
            // This exception is thrown if the group name doesn't exist at all
            return null;
        }
    }
}
