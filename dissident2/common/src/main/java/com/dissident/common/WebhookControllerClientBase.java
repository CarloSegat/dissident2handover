package com.dissident.common;

import org.hyperledger.acy_py.generated.model.SendMessage;
import org.hyperledger.aries.AriesClient;
import org.hyperledger.aries.api.credentials.Credential;
import org.hyperledger.aries.api.message.BasicMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dissident.common.model.BasicMessageEvent;
import com.dissident.common.model.ControllerConnectionRecord;
import com.dissident.common.model.EnumEntity;
import com.dissident.common.model.ServiceInquiryResponseEvent;
import com.dissident.common.model.VerificationEvent;
import com.dissident.common.model.events.ConnectionSetup.ConnectionResponseReceived;
import com.dissident.common.model.events.Issuance;
import com.dissident.common.model.events.Presentation;
import com.dissident.common.model.events.ServiceRegistration;
import com.dissident.common.model.events.ServiceUsage;
import com.dissident.common.service.ConnectionManagerService;
import com.dissident.common.service.DidResolutionService;
import com.dissident.common.service.LogService;
import com.dissident.common.service.MessagingService;
import com.dissident.common.service.VerificationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/webhook")
@CrossOrigin
public class WebhookControllerClientBase extends WebhookControllerBase {

    /**
     * WebHookController common to both the client and the provider
     * 
     * @param objectMapper
     * @param connectionManagerService
     * @param didResolutionService
     * @param ariesClient
     * @param verificationService
     * @param messagingService
     * @param eventPublisher
     * @param logService
     */

    public WebhookControllerClientBase(
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
    protected ResponseEntity<String> basicMessageCallback(Map<String, Object> eventData) {
        try {

            BasicMessage basicMessage = objectMapper.convertValue(eventData, BasicMessage.class);

            BasicMessageEvent event = new BasicMessageEvent(
                this, 
                basicMessage.getConnectionId(),
                basicMessage.getContent()
            );
            
            eventPublisher.publishEvent(event);

            System.out.println("Receive message from connectionId: " + basicMessage.getConnectionId());
            System.out.println("Received message: " + basicMessage.getContent());

            if (basicMessage.getContent().startsWith("[RegisteredService")) {
                System.out.println("The message seems to represent a RegisteredService or a list of them.");
                System.out.println("Received message is a Service Inquiry Response: " + basicMessage.getContent());

                ServiceInquiryResponseEvent ServiceInquiryResponseEvent = new ServiceInquiryResponseEvent(
                        this,
                        basicMessage.getConnectionId(),
                        basicMessage.getContent()
                );

                eventPublisher.publishEvent(ServiceInquiryResponseEvent);
            } else {

                Map<String, Object> contentMap = objectMapper.readValue(
                        basicMessage.getContent(),
                        new TypeReference<Map<String, Object>>() {
                        });

                if ("ServiceRegistrationSuccesful".equals(contentMap.get("messageType"))) {
                    logService.logEvent(
                        EnumEntity.AP,
                        EnumEntity.fromString(entityRole),
                        new ServiceRegistration.ServiceRegistrationResponseReceived());
                }

                if ("ServiceUsageRequest".equals(contentMap.get("messageType"))) {
                    Map<String, String> body = new HashMap<String, String>();
                    body.put("content", "message consumption received");

                    logService.logEvent(
                        EnumEntity.CUSTOMER,
                        EnumEntity.PROVIDER,
                        new ServiceUsage.ServiceUsageRequestReceived(),
                        body
                    );
                    
                    // Verify requester of service consumption
                    if(verificationService.isAlreadyVerified(basicMessage.getConnectionId())) {
                        
                        logService.logEvent(
                            EnumEntity.PROVIDER,
                            EnumEntity.CUSTOMER,
                            new ServiceUsage.ServiceUsageResponseSend()
                        );

                        SendMessage messageToSend = SendMessage.builder()
                            .content("{\"messageType\": \"ServiceUsageResponse\",\"qrCode\": \"https://api.qrserver.com/v1/create-qr-code/?size=500x500&data=https://drive.google.com/file/d/1DWyjlWgRhRlUQgHM9mFvylCD89qLuKIw/view?usp=sharing\"}")
                            .build();

                        try {
                            messagingService.sendMessage(event.getConnectionId(), messageToSend);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else {
                        verificationService.verifyParty(basicMessage.getConnectionId());
                    }
                    // then it continues in the BasicMessageEventListener
                    // because we want to wait for the verificaiton to complete
                }
                if ("ServiceUsageResponse".equals(contentMap.get("messageType"))) {
                    Map<String, String> body = new HashMap<String, String>();
                    body.put("qrCode", (String) contentMap.get("qrCode"));
                    
                    logService.logEvent(
                        EnumEntity.PROVIDER,
                        EnumEntity.CUSTOMER,
                        new ServiceUsage.ServiceUsageResponseReceived(),
                        body
                    );
                }
            }
            return ResponseEntity.ok("Client dealt with basic message succesfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing BASIC_MESSAGES: " + eventData);
        }
    }

    @Override
    protected ResponseEntity<String> connectionsCallback(Map<String, Object> eventData) {
        try {
            String json = objectMapper.writeValueAsString(eventData);
            System.out.println(json);

            TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {
            };
            Map<String, Object> parsedData = objectMapper.readValue(json, typeRef);

            String state = (String) parsedData.get("state");
            if ("completed".equals(state) || "active".equals(state)) {
                // first check if the connection is already in the connection record
                // Check for existing connection record

                String theirDid = (String) parsedData.get("their_public_did");
                if (theirDid == null) {
                    theirDid = (String) parsedData.get("their_did");
                }
                ControllerConnectionRecord existingRecord = connectionManagerService
                        .getConnectionRecordByDid(theirDid);

                logService.logEvent(
                        EnumEntity.fromString(theirDid),
                        EnumEntity.fromString(entityRole),
                        new ConnectionResponseReceived());

                if (existingRecord != null) {
                    // Update existing record
                    connectionManagerService.updateConnectionRecordStatusByDid(
                            theirDid, "active");
                    connectionManagerService.updateConnectionIdByDid(theirDid,
                            (String) parsedData.get("connection_id"));
                } else {
                    String connectionId = (String) parsedData.get("connection_id");
                    String did = theirDid;

                    String didDocument = "";
                    boolean isTrusted = true;
                    String status = "active";
                    String role = "";

                    if (did.equals(dlgDid)) {
                        didDocument = dlgDidDocument;
                        role = "dlg";
                    } else if (did.equals(issuerDid)) {
                        didDocument = issuerDidDocument;
                        role = "issuer";
                    } else {
                        // Should not happen here
                        System.out.println("DID not found in the connection record, neither be DLG or Issuer");
                    }

                    connectionManagerService.addConnectionRecord(connectionId, did, didDocument, isTrusted,
                            status,
                            role);
                }

            } else if ("request".equals(state)) {
                // Handle request state
                System.out.println("Connection request received");
                // Create a connection record
            } else {
                System.out.println("Unhandled state: " + state);
            }

            connectionManagerService.printAllConnectionRecords();
            return ResponseEntity.ok("Didexchange request dealt");

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing CONNECTIONS event: " + e.getMessage());
        }
    }

    @Override
    protected ResponseEntity<String> didResolutionCallback(Map<String, Object> eventData) {
        System.out.println("Receive event type: DID_RESOLUTION");
        final String requestDid = (String) eventData.get("requestDid");
        if (requestDid == null) {
            System.out.println("requestDid is empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("requestDid is empty");
        }
        if (requestDid.equals(dlgDid)) {
            System.out.println("The fetched DID Document was " + dlgDidDocument);
            return ResponseEntity.ok(dlgDidDocument);

        } else if (requestDid.equals(issuerDid)) {
            System.out.println("The fetched DID Document was " + issuerDidDocument);
            return ResponseEntity.ok(issuerDidDocument);
        } else {
            System.out.println("Resolving requestDid: " + requestDid);
            // Maybe it is smarter to check at the local connection manager first
            try {
                String resolvedDidDocString = didResolutionService.resolveDidToDidDocumentString(requestDid);

                System.out.println("JSON Response to the Resolver: " + resolvedDidDocString);

                return ResponseEntity.ok(resolvedDidDocString);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error resolving DID: " + e.getMessage());
            }
        }
    }

    @Override
    protected ResponseEntity<String> issueCrededentialsCallback(Map<String, Object> eventData) {
        System.out.println("Receive event type: ISSUE_CREDENTIAL_V2_0");

        // Check if the credential state is 'done'
        String state = (String) eventData.get("state");
        if ("done".equals(state)) {
            System.out.println("Received a Credential from the Issuer");

            try {
                // Fetch credentials from the wallet
                Optional<List<Credential>> credentials = ariesClient.credentials();

                if (credentials.isPresent() && !credentials.get().isEmpty()) {
                    System.out.println("Credentials Details:");
                    for (Credential credential : credentials.get()) {
                        System.out
                                .println("Credential Definition ID: " + credential.getCredentialDefinitionId());
                        System.out.println("Attributes: " + credential.getAttrs());
                        System.out.println("Credential Revocation ID: " + credential.getCredRevId());
                        System.out.println("Referent: " + credential.getReferent());
                        System.out.println("Revocation Registry ID: " + credential.getRevRegId());
                        System.out.println("Schema ID: " + credential.getSchemaId());

                        HashMap<String, String> body = new HashMap<String, String>();
                        body.put("attributes", new Gson().toJson(credential.getAttrs()));
                        body.put("revocation", credential.getCredRevId());
                        body.put("referent", credential.getReferent());
                        body.put("revocation_registry_id", credential.getRevRegId());
                        body.put("schema_id", credential.getSchemaId());
                        body.put("issuer_name", "DT ISSUER");

                        logService.logEvent(
                            EnumEntity.fromString(issuerDid),
                            EnumEntity.fromString(entityRole),
                            new Issuance.IssuanceResponseReceived(),
                            body
                        );

                    }
                    // Show full list of connection here
                    connectionManagerService.printAllConnectionRecords();
                } else {
                    System.out.println("No credentials found in the wallet.");
                }
            } catch (IOException e) {
                System.out.println("Error fetching credentials: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return ResponseEntity.ok("Client received a issueCredentials didComm message with state " + state
                + ", not supposed to do anything about it");
    }

    @Override
    protected ResponseEntity<String> presentProofCallback(Map<String, Object> eventData) {
        System.out.println("Receive event type: PRESENT_PROOF_V2_0");

        String presentProofstate = (String) eventData.get("state");
        String connectionId = (String) eventData.get("connection_id");

        switch (presentProofstate) {
            case "request-received":
                logService.logEvent(
                        EnumEntity.fromString(connectionManagerService.getConnectionRoleById(connectionId)),
                        EnumEntity.fromString(entityRole),
                        new Presentation.PresentationRequestReceived());

                System.out.println("Presentation request received for connection ID: " + connectionId);
                break;

            case "presentation-sent":
                logService.logEvent(
                        EnumEntity.fromString(entityRole),
                        EnumEntity.fromString(connectionManagerService.getConnectionRoleById(connectionId)),
                        new Presentation.PresentationResponseSend());
                System.out.println("Sent presentation for connection ID: " + connectionId);
                break;

            case "done":
                System.out.println("Vefification completed for connection ID: " + connectionId);

                String verified = (String) eventData.get("verified");

                if ("true".equals(verified)) {
                    System.out.println("The party on connection ID: " + connectionId + " is verified");
                    verificationService.addAsVerified(connectionId);
                    connectionManagerService.updateConnectionRecordTrustByConnectionId(connectionId, true);
                    connectionManagerService.printAllConnectionRecords();
                }

                System.out.println(
                    "CLIENT/PRODUCER publishing the VerificationEvent event or not? " 
                    + connectionManagerService.getConnectionRoleById(connectionId)
                );
                
                if(
                    connectionManagerService.getConnectionRoleById(connectionId).equals("provider") ||
                    connectionManagerService.getConnectionRoleById(connectionId).equals("customer")
                ){
                    VerificationEvent event = new VerificationEvent(
                        this,
                        connectionId,
                        entityRole
                    );

                    eventPublisher.publishEvent(event);
                }

                break;

            case "request-sent":
                // Handle request-sent state
                // this is sent automatically as part of receiving the presentation request from
                // the AP
                System.out.println("Presentation request sent for connection ID: " + connectionId);
                break;

            case "presentation-received":
                logService.logEvent(
                        EnumEntity.fromString(connectionManagerService.getConnectionRoleById(connectionId)),
                        EnumEntity.fromString(entityRole),
                        new Presentation.PresentationResponseReceived());
                System.out.println("Verifiable Presentation received for connection ID: " + connectionId);
                break;

            default:
                System.out.println(
                        "Unhandled state: " + presentProofstate + " for connection ID: " + connectionId);
                break;
        }
        return ResponseEntity.ok("presentProof didComm message handled");
    }
}