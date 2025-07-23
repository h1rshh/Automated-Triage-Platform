// File: src/main/java/com/example/alert_analyzer/service/DatabaseService.java
// --- THIS IS A NEW FILE ---
package com.example.alert_analyzer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DatabaseService {

    private final DataSource dataSource;

    @Autowired
    public DatabaseService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Fetches the schema definition for a given table name.
     * @param tableName The name of the table to inspect.
     * @return A list of maps, where each map represents a column and its properties.
     */
    public List<Map<String, String>> getTableSchema(String tableName) {
        List<Map<String, String>> schema = new ArrayList<>();

        // Using try-with-resources to ensure the connection is closed automatically
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            // Fetching columns for the specified table
            // The arguments are (catalog, schemaPattern, tableNamePattern, columnNamePattern)
            // null means we don't filter by that criteria.
            ResultSet columns = metaData.getColumns(null, null, tableName.toUpperCase(), null);

            while (columns.next()) {
                Map<String, String> columnDetails = new HashMap<>();
                columnDetails.put("columnName", columns.getString("COLUMN_NAME"));
                columnDetails.put("dataType", columns.getString("TYPE_NAME"));
                columnDetails.put("size", columns.getString("COLUMN_SIZE"));
                columnDetails.put("isNullable", columns.getString("IS_NULLABLE"));
                schema.add(columnDetails);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error while fetching schema for table " + tableName);
            e.printStackTrace();
            // Return an empty list or throw a custom exception
            return new ArrayList<>();
        }
        return schema;
    }
}

