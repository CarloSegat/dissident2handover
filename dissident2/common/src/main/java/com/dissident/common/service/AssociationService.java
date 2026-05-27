package com.dissident.common.service;

import org.hyperledger.aries.AriesClient;
import org.hyperledger.aries.api.connection.ConnectionRecord;
import org.hyperledger.aries.api.resolver.DIDDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.dissident.common.model.ControllerConnectionRecord;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class AssociationService {

    @Value("${dlg.did}")
    private String dlgDid;

    @Value("${entity.role}")
    private String myRole;

    private final ConnectionManagerService connectionManagerService;
    private final DidResolutionService didResolutionService;
    private final DidExchangeService didExchangeService;

    public AssociationService(
        ConnectionManagerService connectionManagerService,
        AriesClient ariesClient,
        DidResolutionService didResolutionService, 
        LogService logService,
        DidExchangeService didExchangeService
    ) {
        this.connectionManagerService = connectionManagerService;
        this.didResolutionService = didResolutionService;
        this.didExchangeService = didExchangeService;
    }

    public DIDDocument resolveDidOfRequester(String did)
            throws Exception {
                
        if (!connectionManagerService.isAnyActiveDlgPresent()) {
            initiateConnectionToDlg();
            connectionManagerService.printAllConnectionRecords();
        }
        
        System.out.println("resolving DID document . . . of " + did);
        // GETS STUCK HERE
        DIDDocument didDocument = didResolutionService.resolveDidToDidDocument(did);
        return didDocument;
    }

    private void initiateConnectionToDlg()
            throws Exception {

        System.out.println("initiateConnectionToDlg . . .");

        Optional<ConnectionRecord> connectionRecord = didExchangeService.createDidExchangeRequest(dlgDid);

        // Handle the Optional<ConnectionRecord>
        if (connectionRecord.isPresent()) {
            String connectionId = connectionRecord.get().getConnectionId();
            System.out.println("\tThe connection to DLG has been requested with connectionID " + connectionId);
        } else {
            throw new Exception("\tFailed to request a connection to DLG.");
        }

        // Wait for an active DLG connection
        // This is not a direct response from the DLG agent
        CompletableFuture<ControllerConnectionRecord> future = connectionManagerService.waitForActiveDlgConnection();
        future.get(); // This will block until the future is complete
        System.out.println("\tConnection to DLG established");
        return;
    }
}
