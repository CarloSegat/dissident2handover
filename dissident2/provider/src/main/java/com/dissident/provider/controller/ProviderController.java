package com.dissident.provider.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;

import com.dissident.common.model.AssociationRequest;
import com.dissident.common.model.AssociationResponse;
import com.dissident.common.model.ControllerConnectionRecord;
import com.dissident.common.model.EnumEntity;
import com.dissident.common.model.events.Attachment;
import com.dissident.common.model.events.ServiceDiscovery;
import com.dissident.common.model.events.ServiceRegistration;
import com.dissident.common.service.LogService;
import com.dissident.common.service.MessagingService;
import com.dissident.common.service.VerificationService;
import com.dissident.provider.model.ServiceRegistrationMessage;
import com.google.gson.Gson;
import com.dissident.common.service.AssociationService;
import com.dissident.common.service.ConnectionManagerService;
import com.dissident.common.service.DidExchangeService;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.hyperledger.acy_py.generated.model.SendMessage;
import org.hyperledger.aries.AriesClient;
import org.hyperledger.aries.api.connection.ConnectionRecord;
import org.hyperledger.aries.api.resolver.DIDDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

@Controller
@CrossOrigin(
    origins = "*", 
    allowedHeaders = "*",
    methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.OPTIONS}
)
public class ProviderController {

    private final AssociationService associationResponseProcessingService;
    private final ConnectionManagerService connectionManagerService;
    private final VerificationService verificationService;
    private final AriesClient ariesClient;
    private final LogService logService;
    private final DidExchangeService didExchangeService;
    private final MessagingService messagingService;

    HttpClient client = HttpClient.newHttpClient();

    public ProviderController(
            AssociationService associationResponseProcessingService,
            ConnectionManagerService connectionManagerService,
            AriesClient ariesClient,
            VerificationService verificationService,
            LogService logService,
            DidExchangeService didExchangeService,
            MessagingService messagingService) {
        this.associationResponseProcessingService = associationResponseProcessingService;
        this.connectionManagerService = connectionManagerService;
        this.ariesClient = ariesClient;
        this.verificationService = verificationService;
        this.logService = logService;
        this.didExchangeService = didExchangeService;
        this.messagingService = messagingService;
    }

    @Value("${entity.did}")
    private String providerDid;

    @Value("${entity.role}")
    private String entityRole;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("providerDid", providerDid);
        return "index";
    }

    @PostMapping("/send-attachment-provider")
    public ResponseEntity<?> sendAttachmentRequest(@RequestBody AssociationRequest associationReq) {
        System.out.println("/send-attachment-provider is called");

        Map<String, String> body = createRequestBody(associationReq);
        String json = new Gson().toJson(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://accesspoint-service:7777/start-association"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        CompletableFuture<HttpResponse<String>> futureResponse = client.sendAsync(request,
                HttpResponse.BodyHandlers.ofString());

        logService.logEvent(
                EnumEntity.PROVIDER,
                EnumEntity.AP,
                new Attachment.AttachmentRequestSend(),
                body);

        HttpResponse<String> response = null;
        HashMap<String, String> responseMap = new HashMap<>();

        try {
            response = futureResponse.join(); // Wait for the response

            responseMap.put("code", response.statusCode() + "");
            responseMap.put("body", response.body());

            if (response.statusCode() == 200) {
                return ResponseEntity.ok(response.body());
            } else {
                return ResponseEntity.status(response.statusCode()).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            logService.logEvent(
                    EnumEntity.AP,
                    EnumEntity.PROVIDER,
                    new Attachment.AttachmentResponseReceived(),
                    responseMap);
        }
    }

    private HashMap<String, String> createRequestBody(AssociationRequest associationReq) {
        HashMap<String, String> body = new HashMap<String, String>();
        body.put("did", associationReq.getDid());
        body.put("role", associationReq.getRole());
        return body;
    }

    // called by client to resolve the DID Document of the AP after having attached
    // to AP
    @PostMapping("/process-response-provider")
    public ResponseEntity<?> processServerResponse(@RequestBody AssociationResponse associationResponse) {
        System.out.println("/process-response-provider called!");

        final String didAp = "did:sov:SUqWD8ZL3r6KeYTKKQ6zRw";
        try {
            // Assuming processAssociationRequest now returns a DIDDocument object
            DIDDocument didDocument = associationResponseProcessingService
                    .resolveDidOfRequester(didAp);

            // save this DID to cache
            connectionManagerService.addConnectionRecord(null, didAp,
                    didDocument.toString(), false, "inactive", "accesspoint");

            connectionManagerService.printAllConnectionRecords();

            // create connection to the AP
            Optional<ConnectionRecord> connectionRecordOptional = didExchangeService.createDidExchangeRequest(didAp,
                    "http://provider-agent-service:6100");

            if (connectionRecordOptional.isPresent()) {

                // Print current connection records for debugging
                connectionManagerService.printAllConnectionRecords();

                // Wait for the connection to become active
                ControllerConnectionRecord activeConnection = connectionManagerService
                        .waitForConnectionWithRole("accesspoint")
                        .get();

                // After the connection is active, verify the party
                System.out.println("Connection to AP is active");
                verificationService.verifyParty(activeConnection.getConnectionId());

            } else {
                // Handle the case where the DID exchange request failed
                throw new IOException("DID Exchange Request failed");
            }

            // Do the credential verification here.

            return ResponseEntity.ok().build(); // Return HTTP 200 OK
        } catch (Exception e) {
            e.printStackTrace();
            // Construct an error response with a JSON body
            Map<String, String> errorBody = new HashMap<>();
            errorBody.put("error", "Error processing ASSOCIATION_REQUEST event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody); // Return error response
        }
    }

    @CrossOrigin
    @PostMapping("/service-registration")
    public ResponseEntity<Map<String, String>> registerService(@RequestBody Map<String, Object> registrationRequest) {

        Object typeId = registrationRequest.get("id");
        Object type = registrationRequest.get("type");
        Object name = registrationRequest.get("name");
        Object url = registrationRequest.get("url");
        Object producer = registrationRequest.get("producer");

        // Ensure the correct types are being read from the map
        if (!(typeId instanceof Integer)) {
            throw new IllegalArgumentException("ID must be an Integer.");
        }
        if (!(type instanceof String)) {
            throw new IllegalArgumentException("Type must be a String.");
        }
        if (!(name instanceof String)) {
            throw new IllegalArgumentException("Name must be a String.");
        }
        if (!(url instanceof String)) {
            throw new IllegalArgumentException("URL must be a String.");
        }
        if (!(producer instanceof String)) {
            throw new IllegalArgumentException("producer must be a String.");
        }

        ServiceRegistrationMessage serviceRegistrationMessage = new ServiceRegistrationMessage(
                "ServiceRegistrationMessage",
                (String) type,
                (Integer) typeId,
                (String) name,
                (String) producer,
                (String) url);
        // Convert ServiceRegistrationMessage object to JSON string
        Gson gson = new Gson();
        String jsonString = gson.toJson(serviceRegistrationMessage);

        // Create SendMessage object with the JSON string
        SendMessage sendMessage = new SendMessage(jsonString);

        // Fetch the connection ID for the "accesspoint" role
        String connectionId = connectionManagerService.getConnectionIdByRole("accesspoint");
        System.out.println("Fetched connectionId for 'accesspoint' role: " + connectionId);

        if (connectionId != null) {
            try {

                HashMap<String, String> body = new HashMap<String, String>();
                body.put("type", (String) type);
                body.put("name", (String) name);
                body.put("producer", (String) producer);
                body.put("url", (String) url);

                logService.logEvent(
                    EnumEntity.fromString(entityRole),
                    EnumEntity.AP,
                    new ServiceRegistration.ServiceRegistrationRequestSend(),
                    body
                );
                // Send the message using the connectionsSendMessage method
                ariesClient.connectionsSendMessage(connectionId, sendMessage);
            } catch (IOException e) {
                e.printStackTrace();
                // Return an error response if message sending fails
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Failed to send service registration message");
                return ResponseEntity.status(500).body(errorResponse);
            }
        } else {
            // Handle case where no connection ID is found
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "No access point connection found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        // Return success response
        Map<String, String> response = new HashMap<>();
        response.put("message", "Service successfully registered");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test-service-provider")
    public ResponseEntity<String> testService() throws IOException {
        // find connectionID.
        // Check if active and trusted
        String connectionId = connectionManagerService.getConnectionIdByRole("client");

        SendMessage messageToSend = SendMessage.builder()
                .content("{\"messageType\": \"TestServiceMessage\"}")
                .build();
        messagingService.sendMessage(connectionId, messageToSend);
        // send message

        return ResponseEntity.ok("Service is working!");
    }

}
