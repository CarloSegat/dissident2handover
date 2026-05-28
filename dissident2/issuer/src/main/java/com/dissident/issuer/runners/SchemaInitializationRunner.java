package com.dissident.issuer.runners;

import org.hyperledger.aries.AriesClient;
import org.hyperledger.aries.api.schema.SchemaSendRequest;
import org.hyperledger.aries.api.schema.SchemaSendResponse;
import org.hyperledger.aries.api.schema.SchemasCreatedFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.dissident.issuer.config.AppProperties;

import org.hyperledger.aries.api.credential_definition.CredentialDefinition;
import org.hyperledger.aries.api.credential_definition.CredentialDefinitionFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class SchemaInitializationRunner implements ApplicationRunner {

    private final AriesClient ariesClient;
    private final String schemaId;
    private final AppProperties appProperties;

    @Autowired
    public SchemaInitializationRunner(AriesClient ariesClient, @Value("${entity.schemaId}") String schemaId,
            AppProperties appProperties) {
        this.ariesClient = ariesClient;
        this.schemaId = schemaId;
        this.appProperties = appProperties;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            // Delete all existing connections first
            deleteAllConnections();

            SchemasCreatedFilter filter = SchemasCreatedFilter.builder().schemaId(schemaId).build();
            Optional<List<String>> schemas = ariesClient.schemasCreated(filter);

            if (schemas.isPresent() && !schemas.get().isEmpty()) {
                System.out.println("Schema with ID " + schemaId + " exists.");

                Optional<String> credentialDefId = getCredentialDefinitionId(schemaId);

                if (credentialDefId.isPresent()) {
                    System.out.println("Credential Definition for Schema with ID " + schemaId + " exists.");
                    System.out.println("Credential Definition ID: " + credentialDefId.get());
                } else {
                    System.out.println("Credential Definition for Schema with ID " + schemaId + " does not exist.");
                    throw new Exception(
                        "You need to increase the version number of the schema ID, delete " +
                        "the persisted wallets and run everything again. If the credential " +
                        "definition cannot be found in the wallet but exists on the ledger, " +
                        "it must be recreated. " +
                        "See https://github.com/hyperledger/aries-cloudagent-python/issues/506"
                    );
                }

                // Store the credential definition ID in the app properties
                appProperties.setSchemaId(schemaId);
                appProperties.setCredentialDefId(credentialDefId.get());

            } else {
                System.out.println("Schema with ID " + schemaId + " does not exist. Creating schema...");

                String[] schemaParts = schemaId.split(":");
                String schemaName = schemaParts[2];
                String schemaVersion = schemaParts[3];

                List<String> attributes = Arrays.asList("name");
                SchemaSendRequest schemaRequest = SchemaSendRequest.builder()
                        .attributes(attributes)
                        .schemaName(schemaName)
                        .schemaVersion(schemaVersion)
                        .build();

                Optional<SchemaSendResponse> schemaResponse = ariesClient.schemas(schemaRequest);
                if (schemaResponse.isPresent()) {
                    SchemaSendResponse response = schemaResponse.get();
                    System.out.println("Schema created successfully with ID: " + response.getSchemaId());

                    // Store the schema ID in the app properties
                    appProperties.setSchemaId(response.getSchemaId());

                    // Create Credential Definition
                    createCredentialDefinition(response.getSchemaId());
                } else {
                    System.out.println("Failed to create the schema.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createCredentialDefinition(String schemaId) throws IOException {
        CredentialDefinition.CredentialDefinitionRequest credDefRequest = CredentialDefinition.CredentialDefinitionRequest
                .builder()
                .schemaId(schemaId)
                .supportRevocation(false)
                .tag("default")
                .build();

        Optional<CredentialDefinition.CredentialDefinitionResponse> credDefResponse = ariesClient
                .credentialDefinitionsCreate(credDefRequest);

        if (credDefResponse.isPresent()) {
            System.out.println("Credential Definition created successfully with ID: "
                    + credDefResponse.get().getCredentialDefinitionId());
            // Store the credential definition ID in the app properties
            appProperties.setCredentialDefId(credDefResponse.get().getCredentialDefinitionId());
        } else {
            System.out.println("Failed to create Credential Definition.");
        }
    }

    private Optional<String> getCredentialDefinitionId(String schemaId) throws IOException {
        CredentialDefinitionFilter credDefFilter = CredentialDefinitionFilter.builder()
                .schemaId(schemaId)
                .build();

        Optional<CredentialDefinition.CredentialDefinitionsCreated> credDefsResponse = ariesClient
                .credentialDefinitionsCreated(credDefFilter);

        if (credDefsResponse.isPresent() && !credDefsResponse.get().getCredentialDefinitionIds().isEmpty()) {
            // Return the first credential definition ID in the list
            return Optional.of(credDefsResponse.get().getCredentialDefinitionIds().get(0));
        } else {
            return Optional.empty();
        }
    }

    private void deleteAllConnections() throws IOException {
        List<String> connectionIds = ariesClient.connectionIds();
        for (String connectionId : connectionIds) {
            try {
                ariesClient.connectionsRemove(connectionId);
                System.out.println("Deleted connection with ID: " + connectionId);
            } catch (IOException e) {
                System.out.println("Failed to delete connection with ID: " + connectionId);
                e.printStackTrace();
            }
        }
    }

}
