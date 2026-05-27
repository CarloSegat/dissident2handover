package com.dissident.common.service;

import org.hyperledger.aries.AriesClient;
import org.hyperledger.aries.api.present_proof_v2.V20PresExRecord;
import org.hyperledger.aries.api.present_proof_v2.V20PresSendRequestRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.dissident.common.model.EnumEntity;
import com.dissident.common.model.events.Presentation;

import org.hyperledger.aries.api.present_proof.PresentProofRequest;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class VerificationService {

        private final AriesClient ariesClient;
        private final LogService logService;
        private final ConnectionManagerService connectionManagerService;

        // hack to stop infinite loop on service usage
        private Map<String, Boolean> idToVerified = new HashMap<String, Boolean>();

        public void addAsVerified(String connectionId){
                this.idToVerified.put(connectionId, true);
        }
        public boolean isAlreadyVerified(String connectionId){
                return this.idToVerified.containsKey(connectionId) && this.idToVerified.get(connectionId);
        }

        @Value("${entity.role}")
        private String entityRole;

        public VerificationService(
                AriesClient ariesClient,
                LogService logService,
                ConnectionManagerService connectionManagerService
        ) {
                this.ariesClient = ariesClient;
                this.logService = logService;
                this.connectionManagerService = connectionManagerService;
        }

        public Optional<V20PresExRecord> verifyParty(String connectionId) throws IOException {
                // Build requested attributes
                System.out.println("Connection ID: " + connectionId);
                if(this.isAlreadyVerified(connectionId)){
                        System.out.println("SKIPPING verifyParty");
                        return Optional.empty();
                }
                if (!isValidUUID(connectionId)) {
                        System.out.println("Invalid connectionId: Not a valid UUID.");
                }
                Map<String, PresentProofRequest.ProofRequest.ProofRequestedAttributes> requestedAttributes = new HashMap<>();
                requestedAttributes.put("attr1_referent",
                                PresentProofRequest.ProofRequest.ProofRequestedAttributes.builder()
                                                .name("name")
                                                .restrictions(Collections.emptyList())
                                                .build());

                // Build proof request
                PresentProofRequest.ProofRequest proofRequest = PresentProofRequest.ProofRequest.builder()
                                .name("Proof request")
                                .version("1.0")
                                .requestedAttributes(requestedAttributes)
                                .requestedPredicates(new HashMap<>()) // Empty for this example
                                .build();

                // Build V20PresSendRequestRequest
                V20PresSendRequestRequest sendRequest = V20PresSendRequestRequest.builder()
                                .autoRemove(false)
                                .autoVerify(false)
                                .comment("string")
                                .connectionId(connectionId)
                                .presentationRequest(V20PresSendRequestRequest.V20PresRequestByFormat.builder()
                                                .indy(proofRequest)
                                                .build())
                                .trace(false)
                                .build();

                // Send the presentation request

                logService.logEvent(
                        EnumEntity.fromString(entityRole),
                        EnumEntity.fromString(connectionManagerService.getConnectionRoleById(connectionId)),
                        new Presentation.PresentationRequestSend()
                );


                return ariesClient.presentProofV2SendRequest(sendRequest);
        }

        private boolean isValidUUID(String uuid) {
                try {
                        // This will throw an exception if uuid is not a valid UUID
                        UUID.fromString(uuid);
                        return true;
                } catch (IllegalArgumentException exception) {
                        return false;
                }
        }
}