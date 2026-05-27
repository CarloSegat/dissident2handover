package com.dissident.accesspoint.controller;

import java.util.HashMap;
import java.util.Map;
import org.hyperledger.aries.AriesClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.dissident.common.model.AssociationRequest;
import com.dissident.common.model.AssociationResponse;
import com.dissident.common.model.EnumEntity;
import com.dissident.common.model.events.Attachment;
import com.dissident.common.service.LogService;
import com.dissident.common.service.AssociationService;
import com.dissident.common.service.ConnectionManagerService;

import org.springframework.web.bind.annotation.CrossOrigin;

import org.hyperledger.aries.api.resolver.DIDDocument;

@CrossOrigin(origins = "*")
@RestController
public class AccessPointController {

    @Value("${entity.did}")
    private String apDid;

    private final ConnectionManagerService connectionManagerService;
    private final AssociationService associationService;
    private final LogService logService;

    public AccessPointController(ConnectionManagerService connectionManagerService, AriesClient ariesClient,
            AssociationService associationService, LogService logService) {
        this.connectionManagerService = connectionManagerService;
        this.associationService = associationService;
        this.logService = logService;
    }

    @PostMapping("/start-association")
    public ResponseEntity<?> startAssociation(@RequestBody AssociationRequest request) throws Exception {
        System.out.println("start-association, received DID: " + request.getDid());

        logService.logEvent(
            EnumEntity.fromString(request.getRole()),
            EnumEntity.AP,
            new Attachment.AttachmentRequestReceived());

        AssociationResponse associationResponse = new AssociationResponse();

        try {
            DIDDocument didDocument = associationService.resolveDidOfRequester(request.getDid());

            // save this DID to cache
            connectionManagerService.addConnectionRecord(null, request.getDid(), didDocument.toString(), false,
                    "inactive", request.getRole());

            System.out.println("Save DID Document of " + request.getRole());
            connectionManagerService.printAllConnectionRecords();

            // Create an AssociationResponse object
            associationResponse.setRole("accesspoint");
            associationResponse.setDid(apDid);

            logService.logEvent(
                    EnumEntity.AP,
                    EnumEntity.fromString(request.getRole()),
                    new Attachment.AttachmentResponseSend());

            return ResponseEntity.ok(associationResponse);
        } catch (Exception e) {
            e.printStackTrace();
            associationResponse.setSuccesful(false);
            // Construct an error response with a JSON body
            Map<String, String> errorBody = new HashMap<>();
            errorBody.put("error", "Error processing ASSOCIATION_REQUEST event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
        }
    }

}
