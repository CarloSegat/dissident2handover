package com.dissident.common;

import org.hyperledger.aries.AriesClient;
import org.hyperledger.aries.api.connection.ConnectionRecord;
import org.hyperledger.acy_py.generated.model.SendMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.dissident.common.service.ConnectionManagerService;
import com.dissident.common.service.DidExchangeService;
import com.dissident.common.service.LogService;
import com.dissident.common.model.ControllerConnectionRecord;
import com.dissident.common.model.EnumEntity;
import com.dissident.common.model.events.Issuance;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Component
public class BaseRunner implements ApplicationRunner {
    private final AriesClient ariesClient;
    private final ConnectionManagerService connectionManagerService;
    private final DidExchangeService didExchangeService;
    private final LogService logService;

    @Value("${entity.role}")
    private String entityRole;

    @Value("${issuer.did}")
    protected String issuerDid;

    public BaseRunner(
            AriesClient ariesClient,
            ConnectionManagerService connectionManagerService,
            DidExchangeService didExchangeService,
            LogService logService) {
        this.ariesClient = ariesClient;
        this.connectionManagerService = connectionManagerService;
        this.didExchangeService = didExchangeService;
        this.logService = logService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        deleteAllConnections();
        // Start the DID exchange process and send a message to the issuer
        try {
            sendMessageToIssuer();
        } catch (IOException | InterruptedException | ExecutionException e) {
            // Handle exceptions appropriately
            e.printStackTrace();
        }
    }

    public void sendMessageToIssuer() throws IOException, InterruptedException, ExecutionException {

        System.out.println(this.entityRole + " sending a request to issuer");

        // First, create the DID exchange request
        System.out.println("this.issuerDid >> " + this.issuerDid);
        Optional<ConnectionRecord> connectionRecordOptional = didExchangeService.createDidExchangeRequest(this.issuerDid,
                "http://customer-agent-service:5100");
        if (connectionRecordOptional.isPresent()) {

            // Print current connection records for debugging
            connectionManagerService.printAllConnectionRecords();

            // Wait for the connection to become active
            ControllerConnectionRecord activeConnection = connectionManagerService.waitForConnectionWithRole("issuer")
                    .get();

            // After the connection is active, send a message using the connection ID
            SendMessage message = SendMessage.builder()
                    .content(entityRole)
                    .build();
            ariesClient.connectionsSendMessage(activeConnection.getConnectionId(), message);

            logService.logEvent(
                EnumEntity.fromString(entityRole),
                EnumEntity.fromString(issuerDid),
                new Issuance.IssuanceRequestSend()
            );
            
        } else {
            // Handle the case where the DID exchange request failed
            throw new IOException("DID Exchange Request failed");
        }
    }

    private void deleteAllConnections() throws IOException {
        List<String> connectionIds = ariesClient.connectionIds();
        for (String connectionId : connectionIds) {
            try {
                ariesClient.connectionsRemove(connectionId);
                System.out.println("Deleted connection with ID: " + connectionId);
            } catch (IOException e) {
                System.out.println("Failed to delete connection with ID: " + connectionId);
                e.printStackTrace();
            }
        }
    }
}
