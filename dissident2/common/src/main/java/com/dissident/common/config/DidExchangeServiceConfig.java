package com.dissident.common.config;

import org.hyperledger.aries.AriesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import com.dissident.common.service.DidExchangeService;
import com.dissident.common.service.LogService;

@Configuration
@ComponentScan("com.dissident.common.config")
public class DidExchangeServiceConfig {

    @Autowired
    private AriesClient ariesClient;

    @Autowired
    private LogService logService;

    @Bean   
    @DependsOn({
        "ariesClient", 
        "logService"
    })
    public DidExchangeService didExchangeService() {
       return new DidExchangeService(logService, ariesClient);
    }
}