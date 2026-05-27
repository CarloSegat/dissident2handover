package com.dissident.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.retry.annotation.EnableRetry;

import com.dissident.common.service.ConnectionManagerService;
import com.dissident.common.service.LogService;
import com.dissident.common.service.VerificationService;

import org.hyperledger.aries.AriesClient;

@Configuration
@EnableRetry
public class VerificationServiceConfig {

    @Autowired
    private AriesClient ariesClient;

    @Autowired
    private LogService logService;

    @Autowired
    private ConnectionManagerService connectionManagerService;

    @Bean   
    @DependsOn({
        "ariesClient", 
    })
    public VerificationService verificationService() {
       return new VerificationService(
        ariesClient, logService, connectionManagerService);
    }
}
