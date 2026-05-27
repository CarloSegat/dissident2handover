package com.dissident.common.service;

import java.io.IOException;
import java.util.Optional;

import org.hyperledger.aries.AriesClient;
import org.hyperledger.aries.api.connection.ConnectionRecord;
import org.hyperledger.aries.api.did_exchange.DidExchangeCreateRequestFilter;
import org.hyperledger.aries.api.did_exchange.DidExchangeCreateRequestFilter.DidExchangeCreateRequestFilterBuilder;
import org.springframework.beans.factory.annotation.Value;

import com.dissident.common.model.EnumEntity;
import com.dissident.common.model.events.ConnectionSetup;

public class DidExchangeService {

    private final LogService logService;
    private final AriesClient ariesClient;

    @Value("${entity.role}")
    private String myRole;

    public DidExchangeService(LogService logService, AriesClient ariesClient) {
        this.logService = logService;
        this.ariesClient = ariesClient;
    }

    public Optional<ConnectionRecord> createDidExchangeRequest(String didToResolve, String myEndpoint) throws IOException {
        DidExchangeCreateRequestFilterBuilder filter = DidExchangeCreateRequestFilter.builder();

        if(myEndpoint != null){
            filter = filter.myEndpoint(myEndpoint);
        }

        filter = filter.myLabel(myRole)
                .usePublicDid(true)
                .theirPublicDid(didToResolve);

        logService.logEvent(
            EnumEntity.fromString(myRole), 
            EnumEntity.fromString(didToResolve), 
            new ConnectionSetup.ConnectionRequestSend()
        );

        return ariesClient.didExchangeCreateRequest(filter.build());
    }

    public Optional<ConnectionRecord> createDidExchangeRequest(String didToResolve) throws IOException {
        return createDidExchangeRequest(didToResolve, null);
    }
    
}


