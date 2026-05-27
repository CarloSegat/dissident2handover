    package com.dissident.provider.runner;

import org.hyperledger.aries.AriesClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dissident.common.service.ConnectionManagerService;
import com.dissident.common.service.DidExchangeService;
import com.dissident.common.service.LogService;
import com.dissident.common.BaseRunner;

@Component
public class CredentialRequestRunner extends BaseRunner {

    public CredentialRequestRunner(
        AriesClient ariesClient, 
        ConnectionManagerService connectionManagerService,
        DidExchangeService didExchangeService,
        LogService logService
    ) {
        super(ariesClient, connectionManagerService, didExchangeService, logService);
    }
}
