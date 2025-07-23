// --- NEW FILE: AnalysisReport.java ---
package com.example.alert_analyzer.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class AnalysisReport {
    private String llmAnalysisText;
    private ParsedAlert originalAlert;
    private List<Map<String, String>> dbSchema;
    private CodeContext codeContext;
}