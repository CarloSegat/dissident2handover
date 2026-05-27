package com.dissident.dlg.controller;

import org.hyperledger.acy_py.generated.model.SendMessage;
import org.hyperledger.aries.AriesClient;
import org.hyperledger.aries.api.message.BasicMessage;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dissident.common.WebhookControllerBase;
import com.dissident.common.model.EnumEntity;
import com.dissident.common.model.events.DidResolution;
import com.dissident.common.service.ConnectionManagerService;
import com.dissident.common.service.DidResolutionService;
import com.dissident.common.service.LogService;
import com.dissident.common.service.MessagingService;
import com.dissident.common.service.VerificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/webhook")
@CrossOrigin
public class WebhookController extends WebhookControllerBase {

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
    protected ResponseEntity<String> basicMessageCallback(Map<String, Object> eventData) {
        super.basicMessageCallback(eventData);

        BasicMessage basicMessage = objectMapper.convertValue(eventData, BasicMessage.class);
        String content = basicMessage.getContent();
        
        System.out.println("\tbasicMessage.getContent() " + content);
        
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>(){}.getType();

        Map<String, String> myMap = gson.fromJson(content, type);

        this.logService.logEvent(
            EnumEntity.fromString((String) myMap.get("their_did")),
            EnumEntity.fromString(this.connectionManagerService.getConnectionRoleById(basicMessage.getConnectionId())),
            new DidResolution.DidResolutionRequestReceived(), 
            myMap
        );

        try {
            // wea ssume that a basicMessage always and only implies a didResolution

            String didToResolve = myMap.get("did_to_resolve");
            String resolverResponse = resolveDidDocument(didToResolve);

            String resolvedDidDocument = extractDidDocument(resolverResponse);
            System.out.println("Resolved DID Document: " + resolvedDidDocument);
            
            SendMessage messageToSend = SendMessage.builder()
                    .content(resolvedDidDocument)
                    .build();
            // Send this DIDDocument string back to the sender
            
            this.logService.logEvent(
                null,
                EnumEntity.fromString((String) myMap.get("their_did")),
                new DidResolution.DidResolutionResponseSend());
                
            messagingService.sendMessage(basicMessage.getConnectionId(), messageToSend);

            return ResponseEntity.ok("basicMessageCallback of " + this.entityRole
                    + " has finished executing. Note that didComms mechanism has been used to return ");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing BASIC_MESSAGES: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing BASIC_MESSAGES: " + eventData);
    }

    @Override
    protected ResponseEntity<String> connectionsCallback(Map<String, Object> eventData) {
        super.connectionsCallback(eventData);

        String state = (String) eventData.get("state");
        if ("request".equals(state)) {
            return this.respondToDidExchangeRequest("8000", eventData);
        }

        if ("completed".equals(state) || "active".equals(state)) {
            System.out.println("DLG received completed or active didExchane, doing nothing");
        }
        return ResponseEntity
                    .ok("didexchange state was " + state);
    }

    private String resolveDidDocument(String content) {
        try {
            // URL encode the content
            String encodedContent = URLEncoder.encode(content, StandardCharsets.UTF_8.toString());

            String url = "http://" + ariesClientIp + ":8000/resolver/resolve/" + encodedContent;

            HttpClient client = HttpClient.newHttpClient();

            // Create HttpRequest for the URL
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("accept", "application/json")
                    .build();

            // Send the request and get the response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Print the response for debugging
            System.out.println("Response: " + response.body());

            // Return the response body
            return response.body();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    private static String extractDidDocument(String inputJson) {
        // Find the start of the did_document section
        int start = inputJson.indexOf("\"did_document\":") + "\"did_document\":".length();
        // Find the end of the did_document section by looking for the last closing
        // brace before "metadata"
        int end = inputJson.indexOf(", \"metadata\":");

        if (start < 0 || end < 0 || end <= start) {
            return "Invalid JSON format for extraction";
        }

        // Extract the substring from start to end
        int braceCount = 0;
        int i = start;
        for (; i < inputJson.length(); i++) {
            if (inputJson.charAt(i) == '{') {
                braceCount++;
            } else if (inputJson.charAt(i) == '}') {
                braceCount--;
                if (braceCount == 0) {
                    // Found the matching closing brace
                    break;
                }
            }
        }

        if (i == inputJson.length()) {
            return "Invalid JSON format: No matching closing brace found";
        }

        // Extract and remove all whitespace
        String extractedJson = inputJson.substring(start, i + 1).trim();
        return extractedJson.replaceAll("\\s+", "");
    }
}