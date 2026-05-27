package com.dissident.client.controller;

import com.dissident.common.service.MessagingService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.dissident.client.model.EventFutureManager;
import com.dissident.client.service.AvailableServiceManagerService;
import com.dissident.common.model.AssociationRequest;
import com.dissident.common.model.AssociationResponse;
import com.dissident.common.model.ControllerConnectionRecord;
import com.dissident.common.model.EnumEntity;
import com.dissident.common.model.RegisteredService;
import com.dissident.common.model.events.Attachment;
import com.dissident.common.model.events.ServiceDiscovery;
import com.dissident.common.model.events.ServiceRegistration;
import com.dissident.common.model.events.ServiceUsage;
import com.dissident.common.service.LogService;
import com.dissident.common.service.VerificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.dissident.common.service.AssociationService;
import com.dissident.common.service.ConnectionManagerService;
import com.dissident.common.service.DidExchangeService;
import com.dissident.common.service.DidResolutionService;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.core.type.TypeReference;
import org.hyperledger.acy_py.generated.model.SendMessage;
import org.hyperledger.aries.AriesClient;
import org.hyperledger.aries.api.connection.ConnectionRecord;
import org.hyperledger.aries.api.present_proof_v2.V20PresExRecord;
import org.hyperledger.aries.api.resolver.DIDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

@Controller
@CrossOrigin(origins = "*")
public class ClientController {

    @Autowired
    private EventFutureManager eventFutureManager;

    @Autowired
    private AvailableServiceManagerService availableServiceManagerService;

    private final AssociationService associationResponseProcessingService;
    private final ConnectionManagerService connectionManagerService;
    private final VerificationService verificationService;
    private final LogService logService;
    private final DidExchangeService didExchangeService;
    private final MessagingService messagingService;
    private final DidResolutionService didResolutionService;

    HttpClient client = HttpClient.newHttpClient();

    public ClientController(
            AssociationService associationResponseProcessingService,
            ConnectionManagerService connectionManagerService,
            AriesClient ariesClient,
            VerificationService verificationService,
            LogService logService,
            DidExchangeService didExchangeService,
            MessagingService messagingService,
            DidResolutionService didResolutionService) {
        this.associationResponseProcessingService = associationResponseProcessingService;
        this.connectionManagerService = connectionManagerService;
        this.verificationService = verificationService;
        this.logService = logService;
        this.didExchangeService = didExchangeService;
        this.messagingService = messagingService;
        this.didResolutionService = didResolutionService;
    }

    @Value("${entity.did}")
    private String clientDid;

    @Value("${entity.role}")
    private String entityRole;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("clientDid", clientDid);
        return "index";
    }

    @PostMapping("/send-attachment-customer")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> sendAttachmentRequest(@RequestBody AssociationRequest associationReq) {
        System.out.println("/send-attachment-customer called!");

        HashMap<String, String> body = new HashMap<String, String>();
        body.put("did", associationReq.getDid());
        body.put("role", associationReq.getRole());

        String json = new Gson().toJson(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://accesspoint-service:7777/start-association"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        CompletableFuture<HttpResponse<String>> futureResponse = client.sendAsync(request,
                HttpResponse.BodyHandlers.ofString());

        logService.logEvent(
                EnumEntity.CUSTOMER,
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
                    EnumEntity.CUSTOMER,
                    new Attachment.AttachmentResponseReceived(),
                    responseMap);
        }
    }

    @PostMapping("/process-response-customer")
    public ResponseEntity<?> processServerResponse(@RequestBody AssociationResponse associationResponse) {
        System.out.println("/process-response-customer called!");
        
        final String didAp = "did:sov:SUqWD8ZL3r6KeYTKKQ6zRw";
        try {
            // Assuming processAssociationRequest now returns a DIDDocument object
            System.out.println("111111111");
            DIDDocument didDocument = associationResponseProcessingService
                    .resolveDidOfRequester(didAp);
            System.out.println("2222222222222222");

            // save this DID to cache
            connectionManagerService.addConnectionRecord(null, didAp,
                    didDocument.toString(), false, "inactive", "accesspoint");

            System.out.println("33333333333");

            System.out.println("Save DID Document of accesspoint");
            connectionManagerService.printAllConnectionRecords();

            // create connection to the AP
            Optional<ConnectionRecord> connectionRecordOptional = didExchangeService.createDidExchangeRequest(
                didAp,
                "http://customer-agent-service:5100"
            );

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

    @GetMapping("/service-inquiry")
    public ResponseEntity<String> inquireRegisteredService()
            throws InterruptedException, ExecutionException, IOException {

        String connectionId = connectionManagerService.getConnectionIdByRole("accesspoint");
        System.out.println("Send service inquiry to AP");
        System.out.println("Connection id: " + connectionId);

        logService.logEvent(
                EnumEntity.fromString(entityRole),
                EnumEntity.AP,
                new ServiceDiscovery.ServiceDiscoveryRequestSend());

        SendMessage messageToSend = SendMessage.builder()
                .content("{\"messageType\": \"ServiceInquiryMessage\"}")
                .build();
        messagingService.sendMessage(connectionId, messageToSend);

        CompletableFuture<String> futureResponse = new CompletableFuture<>();

        // Register the CompletableFuture with EventFutureManager to be completed when
        // the event arrives
        eventFutureManager.registerFutureForConnectionId(connectionId, futureResponse);

        // Wait for the event to occur and the future to be completed
        String result = futureResponse.get(); // This will block until futureResponse is completed
        System.out.println("wait completed!");

        // Now process the result string to JSON
        ObjectMapper mapper = new ObjectMapper();
        Pattern pattern = Pattern.compile("RegisteredService\\{([^}]*)\\}");
        Matcher matcher = pattern.matcher(result);

        ArrayNode arrayNode = mapper.createArrayNode();

        while (matcher.find()) {
            // Splitting each key-value pair
            String[] keyValuePairs = matcher.group(1).split(", ");
            ObjectNode objectNode = mapper.createObjectNode();

            for (String pair : keyValuePairs) {
                String[] entry = pair.split("=");
                String key = entry[0].trim();
                String value = entry.length > 1 ? entry[1].trim().replaceAll("'", "") : ""; // Removing single quotes
                                                                                            // around the value
                objectNode.put(key, value);
            }

            arrayNode.add(objectNode);
        }

        // If the array has only one element, return it directly; otherwise, return the
        // array
        // String jsonResponse = arrayNode.size() == 1 ? arrayNode.get(0).toString() :
        // arrayNode.toString();
        String jsonResponse = arrayNode.toString();

        processAndAddServices(jsonResponse);

        System.out.println("jsonResponse " + jsonResponse);

        return ResponseEntity.ok(jsonResponse);
    }

    // Assume this is inside your controller method where you get the JSON response
    private void processAndAddServices(String jsonResponse) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<RegisteredService> services;

        // Determine if jsonResponse is an array or single object
        if (jsonResponse.startsWith("[")) {
            // It's an array of services
            services = mapper.readValue(jsonResponse, new TypeReference<List<RegisteredService>>() {
            });
        } else {
            // It's a single service
            RegisteredService service = mapper.readValue(jsonResponse, RegisteredService.class);
            services = List.of(service); // Convert single service to list for uniform processing
        }

        System.out.println("services " + services);

        // Iterate through the services and add them if not already present
        services.forEach(service -> {
            Optional<RegisteredService> existingService = availableServiceManagerService
                    .getServiceById(service.getServiceId());
            if (existingService.isEmpty()) {
                // Service not present, add it
                availableServiceManagerService.addService(service);
                System.out.println("Added service: " + service);
            } else {
                System.out.println("Service already exists: " + service);
            }
        });

        // After processing, list all services
        List<RegisteredService> allServices = availableServiceManagerService.getAllServices();
        System.out.println("Listing all services:");
        allServices.forEach(service -> System.out.println(service.toString()));

    }

    @GetMapping("/consume-service/{serviceId}")
    public ResponseEntity<String> consumeService(@PathVariable("serviceId") Integer serviceId)
            throws IOException, InterruptedException, ExecutionException {

        Optional<RegisteredService> serviceOptional = availableServiceManagerService.getServiceById(serviceId);

        logService.logEvent(
            EnumEntity.CUSTOMER,
            EnumEntity.PROVIDER,
            new ServiceUsage.ServiceUsageRequestSend()
        );

        if (serviceOptional.isPresent()) {

            RegisteredService service = serviceOptional.get();

            String providerDid = service.getProvider();
            System.out.println("Provider's DID: " + providerDid);

            // Check if a connection exists with the provider, active, and trusted?
            if (connectionManagerService.isConnectionActiveAndTrusted(providerDid) == true) {
                System.out.println("Connection with provider is active and trusted");
            } else {
                System.out.println("Connection with provider is NOT active and trusted for " + providerDid);
                DIDDocument didDocument = didResolutionService.resolveDidToDidDocument(providerDid);

                System.out.println("Resolved DID Document of provider of serivce: " + didDocument);

                connectionManagerService.addConnectionRecord(null, providerDid,
                        didDocument.toString(), false, "inactive", "provider");

                System.out.println("Save DID Document of provider, current state of connection manager: ");
                // connectionManagerService.printAllConnectionRecords();

                Optional<ConnectionRecord> connectionRecordOptional = didExchangeService.createDidExchangeRequest(
                        providerDid,
                        "http://customer-agent-service:5100");

                if (connectionRecordOptional.isPresent()) {
                    connectionManagerService.printAllConnectionRecords();
                } else {
                    throw new IOException("DID Exchange Request from CUSTOMER to PROVIDER failed");
                }
            }
            
            ControllerConnectionRecord activeConnection = connectionManagerService
                    .waitForConnectionWithRole("provider")
                    .get();

            System.out.println("Connection to Provider is active");

            System.out.println("Verifying provider");

            if(verificationService.isAlreadyVerified(activeConnection.getConnectionId())){
                SendMessage messageToSend = SendMessage.builder()
                .content("{\"messageType\": \"ServiceUsageRequest\"}")
                .build();
            try {
                messagingService.sendMessage(activeConnection.getConnectionId(), messageToSend);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            } else {
                verificationService.verifyParty(activeConnection.getConnectionId());
            }
            

            return ResponseEntity.ok("Service consumption request sent");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Service with ID " + serviceId + " not found.");
        }
    }

    @GetMapping("/test-service-customer")
    public ResponseEntity<String> testService() throws IOException {
        // find connectionID.
        // Check if active and trusted
        String connectionId = connectionManagerService.getConnectionIdByRole("provider");

        SendMessage messageToSend = SendMessage.builder()
                .content("{\"messageType\": \"TestServiceMessage\"}")
                .build();
        messagingService.sendMessage(connectionId, messageToSend);
        // send message

        return ResponseEntity.ok("Service is working!");
    }
}
