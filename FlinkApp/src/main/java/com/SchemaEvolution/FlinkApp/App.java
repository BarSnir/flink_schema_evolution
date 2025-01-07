package com.SchemaEvolution.FlinkApp;

import org.json.*;
import org.apache.flink.table.api.*;
import org.apache.flink.table.expressions.TimeIntervalUnit;
import static org.apache.flink.table.api.Expressions.*;

public class App 
{
    public static void main( String[] args )
    {
        String schema = SchemaRegistryHandler.get_schema("stam-value");
        JSONObject obj = new JSONObject(schema);
        String converted_sql_schema  = JsonSchemaToFlinkSQL.convert_schema(obj);
        JSONObject connectorProperties = new JSONObject();
        connectorProperties.put("connector", "kafka");
        connectorProperties.put("topic", "transactions");
        connectorProperties.put("properties.bootstrap.servers", "kafka:9092");
        connectorProperties.put("format", "csv");
        String sqlQuery = JsonSchemaToFlinkSQL.wrapCreateTableStatement(
            "source_topic",
            converted_sql_schema,
            connectorProperties
        );
        System.out.println(sqlQuery);
        // EnvironmentSettings settings = EnvironmentSettings.inStreamingMode();
        // TableEnvironment tEnv = TableEnvironment.create(settings);
        
        // fetch from schema registry AVRO schema
        // detect schema change with curl
        // convert to schema string for kafka connector
        // execute sql source
        // tEnv.executeSql("CREATE TABLE transactions (\n" +
        // "    account_id  BIGINT,\n" +
        // "    amount      BIGINT,\n" +
        // "    transaction_time TIMESTAMP(3),\n" +
        // "    WATERMARK FOR transaction_time AS transaction_time - INTERVAL '5' SECOND\n" +
        // ") WITH (\n" +
        // "    'connector' = 'kafka',\n" +
        // "    'topic'     = 'transactions',\n" +
        // "    'properties.bootstrap.servers' = 'kafka:9092',\n" +
        // "    'format'    = 'csv'\n" +
        // ")");
        // Update upcoming schema with lower-case
        // move desired compatibility mode 
        // execute sql ingest
        // tEnv.executeSql("CREATE TABLE transactions (\n" +
        // "    account_id  BIGINT,\n" +
        // "    amount      BIGINT,\n" +
        // "    transaction_time TIMESTAMP(3),\n" +
        // "    WATERMARK FOR transaction_time AS transaction_time - INTERVAL '5' SECOND\n" +
        // ") WITH (\n" +
        // "    'connector' = 'kafka',\n" +
        // "    'topic'     = 'transactions',\n" +
        // "    'properties.bootstrap.servers' = 'kafka:9092',\n" +
        // "    'format'    = 'csv'\n" +
        // ")");
        // transform to lower-case & ingest
        // Table transactions = tEnv.from("transactions");
        // report(transactions).executeInsert("spend_report");
    }

    public static Table report(Table transactions) {
        return transactions.select(
                $("account_id"),
                $("transaction_time").floor(TimeIntervalUnit.HOUR).as("log_ts"),
                $("amount"))
            .groupBy($("account_id"), $("log_ts"))
            .select(
                $("account_id"),
                $("log_ts"),
                $("amount").sum().as("amount"));
    }
}
