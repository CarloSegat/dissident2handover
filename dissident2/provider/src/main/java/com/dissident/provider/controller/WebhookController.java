package com.dissident.provider.controller;

import java.util.Map;

import org.hyperledger.aries.AriesClient;
import org.hyperledger.aries.api.resolver.DIDDocument;
import org.springframework.web.bind.annotation.*;

import com.dissident.common.WebhookControllerClientBase;
import com.dissident.common.service.ConnectionManagerService;
import com.dissident.common.service.DidResolutionService;
import com.dissident.common.service.LogService;
import com.dissident.common.service.MessagingService;
import com.dissident.common.service.VerificationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/webhook")
@CrossOrigin
public class WebhookController extends WebhookControllerClientBase {

    public WebhookController(
            ObjectMapper objectMapper,
            ConnectionManagerService connectionManagerService,
            DidResolutionService didResolutionService,
            AriesClient ariesClient,
            VerificationService verificationService,
            MessagingService messagingService,
            ApplicationEventPublisher eventPublisher,
            LogService logService) {
        super(objectMapper, connectionManagerService, didResolutionService, ariesClient, verificationService,
                messagingService, eventPublisher, logService);
    }

    @Override
    protected ResponseEntity<String> connectionsCallback(Map<String, Object> eventData) {
        super.connectionsCallback(eventData);
        try {
            // Check the state
            String state = (String) eventData.get("state");
            if ("request".equals(state)) {
                System.out.println("Request received, accepted and responded to the requester.");
                // The problem is, this entity (provider) never knows the role of the requester
                // (client, or client1, client2, etc.)
                // I fix it by add the label field. The label field is the role of the
                // requester.

                System.out.println("Request content " + eventData);
                String label = (String) eventData.get("their_label");
                System.out.println("Label: " + label);

                String requesterDid = (String) eventData.get("their_did");
                System.out.println("Requester's: " + requesterDid);

                if (label != null) {
                    // resolve DID
                    DIDDocument didDocument = didResolutionService.resolveDidToDidDocument(requesterDid);
                    System.out.println("Resolved DID Document of requester: " + didDocument);

                    connectionManagerService.addConnectionRecord(null, requesterDid, didDocument.toString(), false,
                            "inactive", label);
                }

                return this.respondToDidExchangeRequest("6000", eventData);

            } 
            System.out.println("Connection recoreds after connections events:");
            connectionManagerService.printAllConnectionRecords();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing CONNECTIONS event: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}