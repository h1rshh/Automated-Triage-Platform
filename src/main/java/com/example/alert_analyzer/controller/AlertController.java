// File: src/main/java/com/example/alert_analyzer/controller/AlertController.java
// --- THIS FILE IS MODIFIED ---
package com.example.alert_analyzer.controller;

import com.example.alert_analyzer.model.AnalysisReport; // 1. Import the new report object
import com.example.alert_analyzer.model.ParsedAlert;
import com.example.alert_analyzer.service.AnalysisService;
import com.example.alert_analyzer.service.SlackService; // 2. Import the SlackService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // 3. Add CrossOrigin to allow the frontend to connect
public class AlertController {

    private final AnalysisService analysisService;
    private final SlackService slackService; // 4. Inject the SlackService

    @Autowired
    public AlertController(AnalysisService analysisService, SlackService slackService) {
        this.analysisService = analysisService;
        this.slackService = slackService;
    }

    /**
     * NEW ENDPOINT: Fetches the list of recent alerts from Slack.
     * This is called when the user clicks "Fetch Alerts" in the UI.
     */
    @GetMapping("/alerts")
    public List<ParsedAlert> getAlerts(@RequestParam String channelId) {
        return slackService.fetchRecentAlerts(channelId);
    }

    /**
     * UPDATED ENDPOINT: Receives an alert, performs a full analysis,
     * and returns the complete report object to the UI.
     */
    @PostMapping("/analyze")
    public AnalysisReport analyzeAlert(@RequestBody ParsedAlert alert) { // 5. Changed return type
        // 6. Changed method call to return the full report
        return analysisService.performAnalysis(alert);
    }
}
