package com.dissident.issuer.controller;

import org.hyperledger.aries.AriesClient;
import org.hyperledger.aries.api.issue_credential_v2.V2CredentialExchangeFree;
import org.hyperledger.aries.api.message.BasicMessage;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.hyperledger.aries.api.issue_credential_v2.V20CredExRecord;
import org.hyperledger.acy_py.generated.model.V20CredFilterIndy;
import org.hyperledger.aries.api.credentials.CredentialAttributes;
import java.util.UUID;

import com.dissident.issuer.config.AppProperties;
import com.dissident.common.WebhookControllerBase;
import com.dissident.common.model.EnumEntity;
import com.dissident.common.model.events.Issuance;
import com.dissident.common.service.ConnectionManagerService;
import com.dissident.common.service.DidResolutionService;
import com.dissident.common.service.LogService;
import com.dissident.common.service.MessagingService;
import com.dissident.common.service.VerificationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/webhook")
@CrossOrigin
public class WebhookController extends WebhookControllerBase {

    private final AppProperties appProperties;

    public WebhookController(
            ObjectMapper objectMapper,
            ConnectionManagerService connectionManagerService,
            DidResolutionService didResolutionService,
            AriesClient ariesClient,
            VerificationService verificationService,
            MessagingService messagingService,
            ApplicationEventPublisher eventPublisher,
            AppProperties appProperties,
            LogService logService) {
        super(objectMapper, connectionManagerService, didResolutionService, ariesClient, verificationService,
                messagingService, eventPublisher, logService);
        this.appProperties = appProperties;
    }

    @Override
    protected ResponseEntity<String> basicMessageCallback(Map<String, Object> eventData) {
        System.out.println("Receive event type: BASIC_MESSAGES");

        BasicMessage basicMessage = objectMapper.convertValue(eventData, BasicMessage.class);
        String theirRole = basicMessage.getContent();

        // an entity initiates the issuance by sending a didcomm basic messageContent
        // to the issuer

        logService.logEvent(
            EnumEntity.fromString(theirRole),
            EnumEntity.ISSUER,
            new Issuance.IssuanceRequestReceived()
        );

        try {
            String connectionId = basicMessage.getConnectionId();

            V2CredentialExchangeFree credentialOffer = constructCredentialOffer(connectionId, theirRole);
            Optional<V20CredExRecord> credExRecord = ariesClient.issueCredentialV2Send(credentialOffer);

            if (credExRecord.isPresent()) {
                System.out.println("Credential issued successfully.");

                logService.logEvent(
                    EnumEntity.ISSUER,
                    EnumEntity.fromString(theirRole),
                    new Issuance.IssuanceResponseSend()
                );

                return ResponseEntity.ok("Credential issued successfully.");
            } else {
                System.out.println("Failed to issue credential.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to issue credential.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing BASIC_MESSAGES: " + e.getMessage());
        }
    }

    @Override
    protected ResponseEntity<String> connectionsCallback(Map<String, Object> eventData) {
        super.connectionsCallback(eventData);

        String state = (String) eventData.get("state");
        if ("request".equals(state)) {
            return this.respondToDidExchangeRequest("9000", eventData);
        } else {
            System.out.println("State is not 'request', state: " + state);
            return ResponseEntity
                    .ok("didexchange state was " + state + " this is fine, the ISSUER is not supposed to do anything");
        }
    }

    @Override
    protected ResponseEntity<String> issueCrededentialsCallback(Map<String, Object> eventData) {
        System.out.println("issuer issueCrededentialsCallback shouldn't do anything");
        return ResponseEntity.ok("issueCrededentialsCallback is empty, that's fine");
    }

    private V2CredentialExchangeFree constructCredentialOffer(String connectionId, String messageContent) {
        // Create CredentialAttributes
        CredentialAttributes nameAttribute = CredentialAttributes.builder()
            .name("name")
            .value(messageContent)
            .build();

        List<CredentialAttributes> attributes = new ArrayList<>();
        attributes.add(nameAttribute);

        String extractedDid = extractDid(appProperties.getDid());

        V20CredFilterIndy credFilterIndy = V20CredFilterIndy.builder()
                .credDefId(appProperties.getCredentialDefId())
                .issuerDid(extractedDid)
                .schemaId(appProperties.getSchemaId())
                .schemaIssuerDid(extractedDid)
                .schemaName(extractSchemaName(appProperties.getSchemaId()))
                .schemaVersion(extractSchemaVersion(appProperties.getSchemaId()))
                .build();

        V2CredentialExchangeFree.V2CredentialPreview credentialPreview = V2CredentialExchangeFree.V2CredentialPreview
                .builder()
                .attributes(attributes)
                .build();

        return V2CredentialExchangeFree.builder()
                .autoIssue(true)
                .connectionId(UUID.fromString(connectionId))
                .credentialPreview(credentialPreview)
                .filter(new V2CredentialExchangeFree.V20CredFilter(credFilterIndy, null))
                .build();
    }

    private String extractSchemaName(String schemaId) {
        // Extract the schema name from schema ID
        return schemaId.split(":")[2];
    }

    private String extractSchemaVersion(String schemaId) {
        // Extract the schema version from schema ID
        return schemaId.split(":")[3];
    }

    private String extractDid(String fullDid) {
        String[] parts = fullDid.split(":");
        return parts.length > 2 ? parts[2] : fullDid; // Return the extracted part if possible, else the full DID
    }
}
