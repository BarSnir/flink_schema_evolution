package com.SchemaEvolution.FlinkApp;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonSchemaToFlinkSQL {

    public static String convert_schema(JSONObject schema) {
        // Convert to Flink SQL schema
        return processRecord(schema);
    }

    private static String processRecord(JSONObject record) {
        // Ensure the type is 'record'
        if (!"record".equals(record.getString("type"))) {
            throw new IllegalArgumentException("Unsupported type: " + record.getString("type"));
        }

        // Start building the schema string
        StringBuilder schemaBuilder = new StringBuilder();
        schemaBuilder.append("(\n");

        // Process each field in the fields array
        JSONArray fields = record.getJSONArray("fields");
        for (int i = 0; i < fields.length(); i++) {
            JSONObject field = fields.getJSONObject(i);

            // Extract field name and type
            String fieldName = field.getString("name");
            Object fieldType = field.get("type");

            // Handle recursive types (e.g., record)
            if (fieldType instanceof JSONObject && "record".equals(((JSONObject) fieldType).getString("type"))) {
                schemaBuilder.append(backtickFieldName(fieldName)).append(" ").append(processRecord((JSONObject) fieldType));
            } else {
                schemaBuilder.append(backtickFieldName(fieldName)).append(" ").append(mapTypeToFlinkSQL(fieldType.toString()));
            }

            // Add a comma if not the last field
            if (i < fields.length() - 1) {
                schemaBuilder.append(", \n");
            }
        }

        schemaBuilder.append("\n )");
        return schemaBuilder.toString();
    }

    private static String mapTypeToFlinkSQL(String avroType) {
        // Map Avro types to Flink SQL types
        switch (avroType) {
            case "int":
                return "INT";
            case "long":
                return "BIGINT";
            case "double":
                return "DOUBLE";
            case "string":
                return "STRING";
            case "boolean":
                return "BOOLEAN";
            case "float":
                return "FLOAT";
            default:
                throw new IllegalArgumentException("Unsupported Avro type: " + avroType);
        }
    }

    private static String backtickFieldName(String fieldName) {
        // Wrap field name in backticks
        return "`" + fieldName + "`";
    }

    public static String wrapCreateTableStatement(
            String tableName, 
            String fields, 
            JSONObject connectorProperties
        ) {
        // Build the CREATE TABLE statement
        StringBuilder statementBuilder = new StringBuilder();
        statementBuilder.append("CREATE TABLE ")
                        .append(tableName)
                        .append(" ")
                        .append(fields)
                        .append(" \n")
                        .append("WITH (");

        // Append connector properties
        for (String key : connectorProperties.keySet()) {
            statementBuilder.append("\n    '").append(key).append("' = '").append(connectorProperties.getString(key)).append("',");
        }

        // Remove trailing comma and close the WITH clause
        statementBuilder.setLength(statementBuilder.length() - 1);
        statementBuilder.append("\n);");

        return statementBuilder.toString();
    }
}
