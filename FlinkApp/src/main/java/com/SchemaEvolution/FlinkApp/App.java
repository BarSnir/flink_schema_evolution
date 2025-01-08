package com.SchemaEvolution.FlinkApp;

import org.json.*;
import org.apache.flink.table.api.*;

public class App 
{
    public static void main( String[] args )
    {
        EnvironmentSettings settings = EnvironmentSettings.inStreamingMode();
        TableEnvironment tEnv = TableEnvironment.create(settings);
        String sqlQuerySource = getSqlQuerySource(args);
        tEnv.executeSql(sqlQuerySource);
        Table source_topic = tEnv.from(args[0]);
        source_topic.getResolvedSchema();
        
        // fetch from schema registry AVRO schema
        // detect schema change with curl
        // convert to schema string for kafka connector
        // execute sql source

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

    public static String getSqlQuerySource(String[] args){
        String schema = SchemaRegistryHandler.get_schema(args[0]+"-value");
        JSONObject obj = new JSONObject(schema);
        String converted_sql_schema  = JsonSchemaToFlinkSQL.convert_schema(obj);
        JSONObject connectorProperties = new JSONObject();
        connectorProperties.put("connector", "kafka");
        connectorProperties.put("topic", args[0]);
        connectorProperties.put("properties.bootstrap.servers", "broker:9092");
        connectorProperties.put("format", "avro-confluent");
        connectorProperties.put("value.avro-confluent.url", "http://schema-registry:8082");
        connectorProperties.put("key.format", "raw");
        
        return JsonSchemaToFlinkSQL.wrapCreateTableStatement(
            args[0],
            converted_sql_schema,
            connectorProperties
        );
    }
}
