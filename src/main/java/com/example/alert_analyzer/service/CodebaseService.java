package com.example.alert_analyzer.service;

import com.example.alert_analyzer.model.CodeContext; // <-- Import our new class
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CodebaseService {

    // --- CHANGE: The map now stores CodeContext objects, not just Strings ---
    private final Map<String, CodeContext> codeIndex = new HashMap<>();

    @PostConstruct
    public void buildIndex() {
        System.out.println("Building codebase index...");
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources("classpath:mock-codebase/**/*.java");
            for (Resource resource : resources) {
                String serviceName = resource.getURL().getPath().split("/mock-codebase/")[1].split("/")[0];
                String fileContent = new BufferedReader(new InputStreamReader(resource.getInputStream()))
                        .lines().collect(Collectors.joining("\n"));

                Pattern functionPattern = Pattern.compile(
                        "(/\\*\\*[\\s\\S]*?\\*/\\s*)?" +
                                "public\\s+\\w+\\s+(\\w+)\\s*\\([\\s\\S]*?\\)\\s*\\{[\\s\\S]*?\\}",
                        Pattern.MULTILINE
                );

                Matcher matcher = functionPattern.matcher(fileContent);
                while (matcher.find()) {
                    String functionName = matcher.group(2);
                    String functionBlock = matcher.group(0);
                    String indexKey = serviceName + ":" + functionName + "()";

                    // --- CHANGE: Create a CodeContext object and put it in the index ---
                    CodeContext context = new CodeContext(resource.getFilename(), functionBlock);
                    codeIndex.put(indexKey.toLowerCase(), context);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to build codebase index.");
            e.printStackTrace();
        }
        System.out.println("Codebase index built successfully with " + codeIndex.size() + " functions.");
    }

    // --- CHANGE: The method now returns a CodeContext object ---
    public CodeContext findFunctionInCodebase(String serviceName, String functionName) {
        String searchKey = (serviceName + ":" + functionName).toLowerCase();
        return codeIndex.get(searchKey); // Returns null if not found
    }
}
