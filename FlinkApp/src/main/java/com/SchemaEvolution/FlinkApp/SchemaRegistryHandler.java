package com.SchemaEvolution.FlinkApp;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;

import java.io.IOException;

public class SchemaRegistryHandler {

    public static String get_schema(String subject) {
        // Define the Schema Registry URL
        String schemaRegistryUrl = "http://schema-registry:8082";
        int maxSchemas = 1000;
        
        // Create a Schema Registry client
        SchemaRegistryClient schemaRegistryClient = new CachedSchemaRegistryClient(
            schemaRegistryUrl, maxSchemas
        );

        try {
            // Fetch the latest schema version for the subject
            String schema = schemaRegistryClient.getLatestSchemaMetadata(subject).getSchema();
            schemaRegistryClient.close();
            return schema;
        } catch (IOException | RestClientException e) {
            System.err.println("Error fetching schema: " + e.getMessage());
            e.printStackTrace();
            return "Error";
        }
    }
}

