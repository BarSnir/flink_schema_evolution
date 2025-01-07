package com.SchemaEvolution.FlinkApp;

import org.apache.flink.table.api.*;
import org.apache.flink.table.expressions.TimeIntervalUnit;

import static org.apache.flink.table.api.Expressions.*;
/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        EnvironmentSettings settings = EnvironmentSettings.inStreamingMode();
        TableEnvironment tEnv = TableEnvironment.create(settings);
        // fetch from schema registry AVRO schema
        // detect schema change with curl
        // convert to schema string for kafka connector
        // execute sql source
        tEnv.executeSql("CREATE TABLE transactions (\n" +
        "    account_id  BIGINT,\n" +
        "    amount      BIGINT,\n" +
        "    transaction_time TIMESTAMP(3),\n" +
        "    WATERMARK FOR transaction_time AS transaction_time - INTERVAL '5' SECOND\n" +
        ") WITH (\n" +
        "    'connector' = 'kafka',\n" +
        "    'topic'     = 'transactions',\n" +
        "    'properties.bootstrap.servers' = 'kafka:9092',\n" +
        "    'format'    = 'csv'\n" +
        ")");
        // Update upcoming schema with lower-case
        // move desired compatibility mode 
        // execute sql ingest
        tEnv.executeSql("CREATE TABLE transactions (\n" +
        "    account_id  BIGINT,\n" +
        "    amount      BIGINT,\n" +
        "    transaction_time TIMESTAMP(3),\n" +
        "    WATERMARK FOR transaction_time AS transaction_time - INTERVAL '5' SECOND\n" +
        ") WITH (\n" +
        "    'connector' = 'kafka',\n" +
        "    'topic'     = 'transactions',\n" +
        "    'properties.bootstrap.servers' = 'kafka:9092',\n" +
        "    'format'    = 'csv'\n" +
        ")");
        // transform to lower-case & ingest
        Table transactions = tEnv.from("transactions");
        report(transactions).executeInsert("spend_report");
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
