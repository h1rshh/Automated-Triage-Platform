package com.example.alert_analyzer.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeContext {
    private String filename;
    private String functionCode;
}
