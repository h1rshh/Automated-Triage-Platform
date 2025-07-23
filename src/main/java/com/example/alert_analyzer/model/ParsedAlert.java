package com.example.alert_analyzer.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class ParsedAlert {
    private String id;
    private String database;
    private String user;
    private String host;
    private String timeInSec;
    private String info;
    private String loggedAt;
    private String status;
    private String function;
    private String service;
}