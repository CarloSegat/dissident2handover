package com.dissident.accesspoint.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hyperledger.acy_py.generated.model.SendMessage;
import org.hyperledger.aries.AriesClient;
import org.hyperledger.aries.api.credentials.Credential;
import org.hyperledger.aries.api.message.BasicMessage;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dissident.accesspoint.model.RegisteredService;
import com.dissident.accesspoint.model.ServiceRegistrationMessage;
import com.dissident.accesspoint.service.ServiceRepositoryManagerService;
import com.dissident.common.WebhookControllerBase;
import com.dissident.common.model.BasicMessageEvent;
import com.dissident.common.model.ControllerConnectionRecord;
import com.dissident.common.model.EnumEntity;
import com.dissident.common.model.events.ConnectionSetup.ConnectionResponseReceived;
import com.dissident.common.model.events.Issuance;
import com.dissident.common.model.events.Presentation;
import com.dissident.common.model.events.ServiceDiscovery;
import com.dissident.common.model.events.ServiceRegistration;
import com.dissident.common.service.ConnectionManagerService;
import com.dissident.common.service.DidResolutionService;
import com.dissident.common.service.LogService;
import com.dissident.common.service.MessagingService;
import com.dissident.common.service.VerificationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

@RestController
@RequestMapping("/webhook")
@CrossOrigin
public class WebhookController extends WebhookControllerBase {

    private final ServiceRepositoryManagerService serviceRepositoryManagerService;

    public WebhookController(
            ObjectMapper objectMapper,
            ConnectionManagerService connectionManagerService,
            DidResolutionService didResolutionService,
            AriesClient ariesClient,
            VerificationService verificationService,
            MessagingService messagingService,
            ApplicationEventPublisher eventPublisher,
            LogService logService,
            ServiceRepositoryManagerService serviceRepositoryManagerService) {
        super(objectMapper, connectionManagerService, didResolutionService, ariesClient, verificationService,
                messagingService, eventPublisher, logService);
        this.serviceRepositoryManagerService = serviceRepositoryManagerService;
    }

    @Override
    protected ResponseEntity<String> basicMessageCallback(Map<String, Object> eventData) {
        super.basicMessageCallback(eventData);
        try {
            BasicMessage basicMessage = objectMapper.convertValue(eventData, BasicMessage.class);
            System.out.println("\tbasicMessage.getContent() " + basicMessage.getContent());

            // Use TypeReference to specify the type of Map expected
            Map<String, Object> contentMap = objectMapper.readValue(basicMessage.getContent(),
                    new TypeReference<Map<String, Object>>() {
                    });

            // Check if messageType exists and matches "ServiceRegistrationMessage"
            if ("ServiceRegistrationMessage".equals(contentMap.get("messageType"))) {

                logService.logEvent(
                        EnumEntity.fromString(
                                connectionManagerService.getConnectionRoleById(basicMessage.getConnectionId())),
                        EnumEntity.AP,
                        new ServiceRegistration.ServiceRegistrationRequestReceived());
                // Log and process specific message type
                System.out.println("ServiceRegistrationMessage received: " + basicMessage.getContent());
                // Optionally, convert to ServiceRegistrationMessage object if needed for
                // further processing
                ServiceRegistrationMessage serviceRegistrationMessage = objectMapper.convertValue(contentMap,
                        ServiceRegistrationMessage.class);
                RegisteredService registeredService = new RegisteredService(serviceRegistrationMessage.getServiceType(),
                        serviceRegistrationMessage.getServiceId(), serviceRegistrationMessage.getServiceName(),
                        serviceRegistrationMessage.getProvider(), serviceRegistrationMessage.getEndpoint());
                serviceRepositoryManagerService.addService(registeredService);
                // Here, you could do additional processing with serviceRegistrationMessage
                // Log all services in the repository for demonstration
                System.out.println("All services in the repository:");
                List<RegisteredService> allServices = serviceRepositoryManagerService.getAllServices();
                allServices.forEach(System.out::println);

                // notify sender of message that their service has been registered

                logService.logEvent(
                        EnumEntity.AP,
                        EnumEntity.fromString(
                                connectionManagerService.getConnectionRoleById(basicMessage.getConnectionId())),
                        new ServiceRegistration.ServiceRegistrationResponseSend());
                SendMessage messageToSend = SendMessage.builder()
                        .content("{\"messageType\": \"ServiceRegistrationSuccesful\"}")
                        .build();
                messagingService.sendMessage(basicMessage.getConnectionId(), messageToSend);
            } else if ("ServiceInquiryMessage".equals(contentMap.get("messageType"))) {

                logService.logEvent(
                        EnumEntity.fromString(
                                connectionManagerService.getConnectionRoleById(basicMessage.getConnectionId())),
                        EnumEntity.AP,
                        new ServiceDiscovery.ServiceDiscoveryRequestReceived());

                // Send all services in the repository back to the sender
                SendMessage messageToSend = SendMessage.builder()
                        .content(serviceRepositoryManagerService.getAllServices().toString())
                        .build();
                messagingService.sendMessage(basicMessage.getConnectionId(), messageToSend);

                logService.logEvent(
                        EnumEntity.AP,
                        EnumEntity.fromString(
                                connectionManagerService.getConnectionRoleById(basicMessage.getConnectionId())),
                        new ServiceDiscovery.ServiceDiscoveryResponseSend());
            }

            // Publish the BasicMessageEvent as in the original code
            BasicMessageEvent event = new BasicMessageEvent(
                    this,
                    basicMessage.getConnectionId(),
                    basicMessage.getContent());

            eventPublisher.publishEvent(event);

            return ResponseEntity.ok("basicMessageCallback of " + this.entityRole
                    + " has finished executing. Note that SpringBoot event publishing mechanism is responsible for further processing of the request.");
        } catch (Exception e) { // Catching broad exception to cover both JSON parsing and other errors
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing BASIC_MESSAGES: " + eventData);
        }
    }

    @Override
    protected ResponseEntity<String> connectionsCallback(Map<String, Object> eventData) {
        super.connectionsCallback(eventData);
        try {
            // Check the state
            String state = (String) eventData.get("state");
            if ("request".equals(state)) {
                return this.respondToDidExchangeRequest("7000", eventData);
            } else {
                if ("completed".equals(state) || "active".equals(state)) {
                    // ACA-Py 0.11.x (the resolver agents) reports the terminal didexchange
                    // state as "active", while 0.10.x (dlg/issuer) reports "completed".
                    // Accept either; the existingRecord guard below makes it idempotent if
                    // both fire. (Previously only "completed" was handled, so the DLG/issuer
                    // connection records were never created and waitForActiveDlgConnection()
                    // blocked forever -> the first "connect" click hung.)
                    // It is important to ignore the "done" state or this will be called twice
                    String theirDid = (String) eventData.get("their_public_did");
                    if (theirDid == null) {
                        theirDid = (String) eventData.get("their_did");
                    }

                    logService.logEvent(
                            EnumEntity.fromString(theirDid),
                            EnumEntity.AP,
                            new ConnectionResponseReceived());

                    ControllerConnectionRecord existingRecord = connectionManagerService
                            .getConnectionRecordByDid(theirDid);

                    if (existingRecord != null) {
                        connectionManagerService.updateConnectionRecordStatusByDid(
                                theirDid, "active");
                        connectionManagerService.updateConnectionIdByDid(theirDid,
                                (String) eventData.get("connection_id"));
                        verificationService.verifyParty((String) eventData.get("connection_id"));
                    } else {

                        // existingRecord is null because in our demo the AP always initiate the
                        // connection with the DLG and the ISSUER
                        // on the other hand the client always initiates the connection with the AP,
                        // this means that by the time we reach this function a record is already
                        // present if it's a connection with a client

                        String didDocument = "";
                        String role = "";

                        System.out.println("state is " + state + " is DLG: " + theirDid.equals(dlgDid));

                        if (theirDid.equals(dlgDid)) {
                            didDocument = dlgDidDocument;
                            role = "dlg";
                        } else if (theirDid.equals(issuerDid)) {
                            didDocument = issuerDidDocument;
                            role = "issuer";
                        } else {
                            // Should not happen here
                            System.out.println(
                                    "DID not found in the connection record, neither be DLG or Issuer");
                        }

                        String connectionId = (String) eventData.get("connection_id");
                        String status = "active";
                        // It is automatically trusted, because only DLG/Issuer can reach this state
                        boolean isTrusted = true;

                        connectionManagerService.addConnectionRecord(connectionId, theirDid, didDocument, isTrusted,
                                status, role);
                    }
                }
            }
            System.out.println("Connection recoreds after connections events:");
            connectionManagerService.printAllConnectionRecords();

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing CONNECTIONS event: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @Override
    protected ResponseEntity<String> didResolutionCallback(Map<String, Object> eventData) {
        System.out.println("Receive event type: DID_RESOLUTION");
        final String requestDid = (String) eventData.get("requestDid"); // Updated variable name
        if (requestDid == null) {
            throw new RuntimeException("requestDid is empty");
        }
        if (requestDid.equals(dlgDid)) {
            System.out.println("fetched DID Document of dlg");
            return ResponseEntity.ok(dlgDidDocument);

        } else if (requestDid.equals(issuerDid)) {
            System.out.println("fetched DID Document of issuer");
            return ResponseEntity.ok(issuerDidDocument);
        } else {
            System.out.println("Resolving requestDid: " + requestDid);
            // Maybe it is smarter to check at the local connection manager first
            try {
                String resolvedDidDocString = didResolutionService.resolveDidToDidDocumentString(requestDid);
                // String jsonResponse = objectMapper.writeValueAsString(resolvedDidDoc);

                // Print out the JSON response for debugging
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
        System.out.println("Event Data: " + eventData);

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
                                body);

                    }

                    // Show full list of connection here
                    connectionManagerService.printAllConnectionRecords();
                    return ResponseEntity.ok("Credentials fetched from wallet");
                } else {
                    System.out.println("No credentials found in the wallet.");
                    return ResponseEntity.internalServerError().body("No credentials found in the wallet.");
                }
            } catch (IOException e) {
                System.out.println("Error fetching credentials: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.internalServerError().body("Error fetching credentials: " + e.getMessage());
            }
        }
        return ResponseEntity.ok("Createredentials callback not implmeneted for state:" + state);
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
                        EnumEntity.AP,
                        new Presentation.PresentationRequestReceived());
                // Handle request-received state
                System.out.println("Presentation request received for connection ID: " + connectionId);
                // Add your logic here
                break;

            case "presentation-sent":
                // Handle presentation-sent state
                logService.logEvent(
                        EnumEntity.AP,
                        EnumEntity.fromString(connectionManagerService.getConnectionRoleById(connectionId)),
                        new Presentation.PresentationResponseSend());
                System.out.println("Sent presentation for connection ID: " + connectionId);
                // Add your logic here
                break;

            case "done":
                // Handle done state
                System.out.println("Vefification completed for connection ID: " + connectionId);
                // Add your logic here
                // Check for additional fields if necessary

                String verified = (String) eventData.get("verified");

                if ("true".equals(verified)) {
                    System.out.println("The party on connection ID: " + connectionId + " is verified");
                    // update the connection record
                    connectionManagerService.updateConnectionRecordTrustByConnectionId(connectionId, true);
                    connectionManagerService.printAllConnectionRecords();
                }
                break;

            case "request-sent":
                // Handle request-sent state
                System.out.println("Presentation request sent for connection ID: " + connectionId);
                // Add your logic here
                break;

            case "presentation-received":
                logService.logEvent(
                        EnumEntity.fromString(connectionManagerService.getConnectionRoleById(connectionId)),
                        EnumEntity.fromString(entityRole),
                        new Presentation.PresentationResponseReceived());
                // Handle presentation-received state
                System.out.println("Verifiable Presentation received for connection ID: " + connectionId);
                // Add your logic here
                break;

            default:
                System.out.println(
                        "Unhandled state: " + presentProofstate + " for connection ID: " + connectionId);
                break;
        }
        return ResponseEntity.ok("presentProofCallback executed for presentProofstate: " + presentProofstate);
    }
}