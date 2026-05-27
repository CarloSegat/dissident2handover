package com.dissident.common.config;

import org.hyperledger.aries.AriesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import com.dissident.common.service.AssociationService;
import com.dissident.common.service.ConnectionManagerService;
import com.dissident.common.service.DidExchangeService;
import com.dissident.common.service.DidResolutionService;
import com.dissident.common.service.LogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@Configuration
@ComponentScan("com.dissident.common.config")
public class AssociationServiceConfig {
    @Autowired
    private ConnectionManagerService connectionManagerService;

    @Autowired
    private AriesClient ariesClient;
    
    @Autowired
    private DidResolutionService didResolutionService;

    @Autowired
    private LogService logService;

    @Autowired
    private DidExchangeService didExchangeService;

    @Bean   
    @DependsOn({
        "didResolutionService", 
        "ariesClient", 
        "connectionManagerService", 
        "logService",
        "didExchangeService"
    })
    public AssociationService associationService() throws JsonMappingException, JsonProcessingException {
        return new AssociationService(
            connectionManagerService, 
            ariesClient, 
            didResolutionService, 
            logService, 
            didExchangeService
        );
    }
}