package com.dissident.common.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.hyperledger.acy_py.generated.model.SendMessage;
import org.hyperledger.aries.api.resolver.DIDDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.dissident.common.model.BasicMessageEvent;
import com.dissident.common.model.ControllerConnectionRecord;
import com.dissident.common.model.EnumEntity;
import com.dissident.common.model.events.DidResolution;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

@Service
public class DidResolutionService {

    private final ConnectionManagerService connectionManagerService;
    private final MessagingService messagingService;
    private final ObjectMapper objectMapper;
    private final LogService logService;

    private Map<String, String> cache;

    @Value("${entity.did}")
    private String myDid;

    @Value("${entity.role}")
    private String myRole;

    public DidResolutionService(ConnectionManagerService connectionManagerService, MessagingService messagingService,
            ObjectMapper objectMapper, LogService logService) throws JsonMappingException, JsonProcessingException {
        this.connectionManagerService = connectionManagerService;
        this.messagingService = messagingService;
        this.objectMapper = objectMapper;
        this.logService = logService;
        cache = new HashMap<>();
    }

    private ConcurrentHashMap<String, CompletableFuture<BasicMessageEvent>> futures = new ConcurrentHashMap<>();

    /**
     * Resolve a DID to a DIDDocument using the custom didComm messaging
     * 
     * @param did the DID to be resolved
     * @return an Optional containing the DIDDocument, or an empty Optional if
     *         resolution fails
     */
    public DIDDocument resolveDidToDidDocument(String did) {
        
        final String resolvedDidDocument = this.resolveDidToDidDocumentString(did);

        try {
            DIDDocument didDocument = objectMapper.readValue(resolvedDidDocument, DIDDocument.class);
            return didDocument;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String resolveDidToDidDocumentString(String did) {
        System.out.println("DID RES SERVICE resolving " + did);
        if (cache.containsKey(did)) {
            System.out.println("cache has did " + cache.get(did));
            return cache.get(did);
        }

        List<ControllerConnectionRecord> records = connectionManagerService.getConnectionRecords();
        ControllerConnectionRecord matchedRecord = records.stream()
                .filter(record -> "dlg".equals(record.getRole().toLowerCase()))
                .findFirst()
                .orElse(null);

        System.out.println("matchedRecord " + matchedRecord);

        String connectionId = (matchedRecord != null) ? matchedRecord.getConnectionId() : null;

        System.out.println("connectionId " + connectionId);

        SendMessage message = new SendMessage();
        Map<String, String> didResolutionMessage = new HashMap<String, String>();

        didResolutionMessage.put("did_to_resolve", did);
        didResolutionMessage.put("their_did", myDid);

        final String body = new Gson().toJson(didResolutionMessage);
        
        message.setContent(body);

        CompletableFuture<BasicMessageEvent> future = new CompletableFuture<>();
        futures.put(connectionId, future);

        try {
            
            this.logService.logEvent(
                null, 
                EnumEntity.DLG, 
                new DidResolution.DidResolutionRequestSend()
                );

            messagingService.sendMessage(connectionId, message);

            // Wait for the event at the connection id
            BasicMessageEvent event = future.get(); // This will block until the future is completed

            System.out.println("AFTER THE future.get <<< ");

            String didDocumentString = event.getContent();

            System.out.println("cleint/provider resolving did docu " + didDocumentString);

            Map<String, String> content = new HashMap<String, String>();
            content.put("did", did);
            content.put("didDocumentString", didDocumentString);

            this.logService.logEvent(
                EnumEntity.DLG, 
                EnumEntity.fromString(myRole), 
                new DidResolution.DidResolutionResponseReceived(),
                content
            );

            cache.put(did, didDocumentString);

            return didDocumentString;
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        } finally {
            futures.remove(connectionId); // Clean up
        }
    }

    public void completeFuture(String connectionId, BasicMessageEvent event) {
        CompletableFuture<BasicMessageEvent> future = futures.get(connectionId);
        if (future != null) {
            future.complete(event);
            System.out.println("completeFuture called for DID Resolution! " + ((BasicMessageEvent) event).getContent());
        }
    }
}
