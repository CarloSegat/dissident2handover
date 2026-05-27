package com.dissident.common;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.hyperledger.aries.AriesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.dissident.common.model.EnumEntity;
import com.dissident.common.model.events.ConnectionSetup.ConnectionRequestReceived;
import com.dissident.common.model.events.ConnectionSetup.ConnectionResponseSend;
import com.dissident.common.service.ConnectionManagerService;
import com.dissident.common.service.DidResolutionService;
import com.dissident.common.service.LogService;
import com.dissident.common.service.MessagingService;
import com.dissident.common.service.VerificationService;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WebhookControllerBase {

    protected final ObjectMapper objectMapper;
    protected final MessagingService messagingService;
    protected final ConnectionManagerService connectionManagerService;
    protected final AriesClient ariesClient;
    protected final DidResolutionService didResolutionService;
    protected final VerificationService verificationService;
    protected final ApplicationEventPublisher eventPublisher;
    protected final LogService logService;

    // TODO find a common denominator for the constructor parameters
    // right now I am just ORing all the parameters needed by the different
    // instances

    @Autowired
    public WebhookControllerBase(
            ObjectMapper objectMapper,
            ConnectionManagerService connectionManagerService,
            DidResolutionService didResolutionService,
            AriesClient ariesClient,
            VerificationService verificationService,
            MessagingService messagingService,
            ApplicationEventPublisher eventPublisher,
            LogService logService) {
        this.objectMapper = objectMapper;
        this.connectionManagerService = connectionManagerService;
        this.didResolutionService = didResolutionService;
        this.ariesClient = ariesClient;
        this.verificationService = verificationService;
        this.messagingService = messagingService;
        this.eventPublisher = eventPublisher;
        this.logService = logService;
    }

    @Value("${dlg.didDocument}")
    protected String dlgDidDocument;

    @Value("${issuer.didDocument}")
    protected String issuerDidDocument;

    @Value("${dlg.did}")
    protected String dlgDid;

    @Value("${issuer.did}")
    protected String issuerDid;

    @Value("${entity.role}")
    protected String entityRole;

    // host of this entity's own ACA-Py admin API (env ARIES_CLIENT_IP); was hardcoded "localhost"
    // which only worked when controller+agent shared a k8s pod. In docker-compose they are
    // separate containers, so we must address the agent by its service name.
    @Value("${aries.client.ip}")
    protected String ariesClientIp;

    @PostMapping("/topic/{topic}/")
    public ResponseEntity<String> handleWebhookEvent(
            @PathVariable String topic,
            @RequestBody Map<String, Object> eventData) throws Exception {

        switch (topic.toLowerCase()) {
            case "basicmessages":
                this.basicMessageCallback(eventData);
                break;
            case "connections":
                return this.connectionsCallback(eventData);
            case "discover_feature":
                System.out.println("Receive event type: DISCOVER_FEATURE");
                break;
            case "endorse_transaction":
                System.out.println("Receive event type: ENDORSE_TRANSACTION");
                break;
            case "issuer_cred_rev":
                System.out.println("Receive event type: ISSUER_CRED_REV");
                break;
            case "issue_credential":
                System.out.println("Receive event type: ISSUE_CREDENTIAL");
                break;
            case "issue_credential_v2":
                System.out.println("Receive event type: ISSUE_CREDENTIAL_V2");
                break;
            case "issue_credential_v2_indy":
                System.out.println("Receive event type: ISSUE_CREDENTIAL_V2_INDY");
                break;
            case "issue_credential_v2_ld_proof":
                System.out.println("Receive event type: ISSUE_CREDENTIAL_V2_LD_PROOF");
                break;
            case "out_of_band":
                System.out.println("Receive event type: OUT_OF_BAND");
                break;
            case "ping":
                System.out.println("Receive event type: PING");
                break;
            case "present_proof":
                System.out.println("Receive event type: PRESENT_PROOF");
                break;
            case "present_proof_v2":
                System.out.println("Receive event type: PRESENT_PROOF_V2");
                break;
            case "problem_report":
                System.out.println("Receive event type: PROBLEM_REPORT");
                // Handle problem report
                break;
            case "revocation_notification":
                System.out.println("Receive event type: REVOCATION_NOTIFICATION");
                // Handle revocation notification
                break;
            case "revocation_notification_v2":
                System.out.println("Receive event type: REVOCATION_NOTIFICATION_V2");
                break;
            case "revocation_registry":
                System.out.println("Receive event type: REVOCATION_REGISTRY");
                break;
            case "settings":
                System.out.println("Receive event type: SETTINGS");
                break;
            case "did_resolution":
                return this.didResolutionCallback(eventData);
            case "issue_credential_v2_0":
                return this.issueCrededentialsCallback(eventData);
            case "issue_credential_v2_0_indy":
                System.out.println("Receive event type: ISSUE_CREDENTIAL_V2_0_INDY");
                break;
            case "present_proof_v2_0":
                return this.presentProofCallback(eventData);
            default:
                System.out.println("Unknown topic: " + topic);
                return ResponseEntity.badRequest().body("Unknown topic: " + topic);
        }

        // Return a response
        return ResponseEntity.ok("Received event on topic: " + topic);
    }

    protected ResponseEntity<String> presentProofCallback(Map<String, Object> eventData) {
        throw new UnsupportedOperationException("Unimplemented method 'presentProofCallback'");
    }

    protected ResponseEntity<String> issueCrededentialsCallback(Map<String, Object> eventData) {
        throw new UnsupportedOperationException("Unimplemented method 'createCrededentialsCallback'");
    }

    protected ResponseEntity<String> didResolutionCallback(Map<String, Object> eventData) {
        throw new UnsupportedOperationException("Unimplemented method 'didResolutionCallback'");
    }

    protected ResponseEntity<String> connectionsCallback(Map<String, Object> eventData) {
        System.out.println("Receive event type: CONNECTIONS");
        System.out.println("\tState: " + eventData.get("state"));
        System.out.println("\tTheir role: " + eventData.get("their_role"));
        System.out.println("\tTheir did: " + eventData.get("their_did"));
        return ResponseEntity.ok(
                "Temporary response from parent method, don't return to client directly; this is just to keep the compiler happy");
    }

    protected ResponseEntity<String> basicMessageCallback(Map<String, Object> eventData) {
        System.out.println("Receive event type: BASIC_MESSAGES");
            return ResponseEntity.ok(
                "Temporary response from parent method, don't return to client directly; this is just to keep the compiler happy");
    }

    protected ResponseEntity<String> respondToDidExchangeRequest(String port, Map<String, Object> eventData) {

        System.out.println(
                ">>>>> >> >from respond to did exchange request eventData.get is " + eventData.get("their_did"));

        logService.logEvent(
                EnumEntity.fromString(
                        (String) eventData.get("their_did")),
                null,
                new ConnectionRequestReceived());

        String connectionId = (String) eventData.get("connection_id");
        String url = "http://" + ariesClientIp + ":" + port + "/didexchange/" + connectionId
                + "/accept-request?use_public_did=true";

        // for (Map.Entry<String, Object> mapElement : eventData.entrySet()) {
        // String key = mapElement.getKey();
        // String value = (String) mapElement.getValue();
        // System.out.println(key + " : " + value);
        // }

        try {
            // Create the HttpClient
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody()) // No body is required for this request
                    .build();

            // Send the request
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Process the response as needed
            System.out.println("Response status code: " + response.statusCode());
            System.out.println("Response body: " + response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error sending POST request: " + e.getMessage());
        } finally {
            // their_did corresponds to the src entity from which the request came from
            logService.logEvent(
                    null,
                    EnumEntity.fromString(
                            (String) eventData.get("their_did")),
                    new ConnectionResponseSend());
        }
        return ResponseEntity.ok("DidExchange request was processesed");
    }

}