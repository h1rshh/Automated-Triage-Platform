package com.example.alert_analyzer.service;

import com.example.alert_analyzer.model.AnalysisReport;
import com.example.alert_analyzer.model.CodeContext;
import com.example.alert_analyzer.model.ParsedAlert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AnalysisService {

    private final DatabaseService databaseService;
    private final CodebaseService codebaseService;
    private final LLMAnalysisService llmAnalysisService;

    @Autowired
    public AnalysisService(DatabaseService databaseService, CodebaseService codebaseService, LLMAnalysisService llmAnalysisService) {
        this.databaseService = databaseService;
        this.codebaseService = codebaseService;
        this.llmAnalysisService = llmAnalysisService;
    }

    public AnalysisReport performAnalysis(ParsedAlert alert) {
        Set<String> tableNames = extractTableNamesFromQuery(alert.getInfo());
        List<Map<String, String>> fullSchema = tableNames.stream()
                .flatMap(tableName -> databaseService.getTableSchema(tableName).stream())
                .collect(Collectors.toList());
        CodeContext codeContext = getCodeContext(alert);
        String prompt = buildPrompt(alert, fullSchema, codeContext);
        String analysisText = llmAnalysisService.analyze(prompt);

        return AnalysisReport.builder()
                .llmAnalysisText(analysisText)
                .originalAlert(alert)
                .dbSchema(fullSchema)
                .codeContext(codeContext)
                .build();
    }

    private CodeContext getCodeContext(ParsedAlert alert) {
        if (alert.getFunction() == null || alert.getFunction().isEmpty()) {
            return new CodeContext("N/A", "No specific function was implicated in this database alert.");
        }
        // Ensure service name is not null before passing it
        String serviceName = alert.getService() != null ? alert.getService() : "unknown-service";
        return codebaseService.findFunctionInCodebase(serviceName, alert.getFunction());
    }

    private Set<String> extractTableNamesFromQuery(String sql) {
        Set<String> tableNames = new HashSet<>();
        Pattern pattern = Pattern.compile("(?:FROM|JOIN)\\s+([a-zA-Z_][a-zA-Z0-9_]*)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        while (matcher.find()) {
            tableNames.add(matcher.group(1));
        }
        return tableNames;
    }

    private String buildPrompt(ParsedAlert alert, List<Map<String, String>> dbSchema, CodeContext codeContext) {
        String codeContextForPrompt;
        // --- FIX: Corrected typo from 'code' to 'codeContext' ---
        if (codeContext != null && codeContext.getFilename() != null && !codeContext.getFilename().equals("N/A")) {
            codeContextForPrompt = String.format(
                    "File: %s\n```java\n%s\n```",
                    codeContext.getFilename(),
                    codeContext.getFunctionCode()
            );
        } else {
            codeContextForPrompt = "No specific code function was identified in this alert.";
        }

        String dbSchemaForPrompt = dbSchema.stream()
                .map(col -> String.format("- **%s:** %s(%s)", col.get("columnName"), col.get("dataType"), col.get("size")))
                .collect(Collectors.joining("\n"));

        String serviceName = alert.getService() != null ? alert.getService() : "N/A";

        return String.format(
                "You are an expert Senior Database Administrator and Performance Engineer. Your task is to analyze a long-running query alert and provide a concise, actionable analysis.\n\n" +
                        "## Alert Details\n" +
                        "- **Service:** %s\n" +
                        "- **Database:** %s\n" +
                        "- **User:** %s\n" +
                        "- **Running For:** %s seconds\n" +
                        "- **Status:** %s\n\n" +
                        "## Full SQL Query\n" +
                        "```sql\n%s\n```\n\n" +
                        "## Relevant Database Schema\n" +
                        "The query involves tables with the following columns:\n%s\n\n" +
                        "## Relevant Code Context\n" +
                        "%s\n\n" +
                        "## Analysis Task\n" +
                        "1.  **Root Cause:** Briefly explain the likely reason this query is running for so long. Focus on potential performance bottlenecks like joins, subqueries, or missing indexes.\n" +
                        "2.  **Recommendation:** Suggest one or two specific, actionable recommendations to optimize this query.\n" +
                        "3.  **Impact Analysis:** Briefly describe the potential impact of this long-running query on the application.\n" +
                        "4.  Format your response in simple Markdown using bold headers like **Root Cause:**.",
                serviceName,
                alert.getDatabase(), alert.getUser(), alert.getTimeInSec(), alert.getStatus(), alert.getInfo(), dbSchemaForPrompt, codeContextForPrompt
        );
    }
}
