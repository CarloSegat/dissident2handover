package com.dissident.common.config;

import org.hyperledger.aries.AriesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import com.dissident.common.service.MessagingService;

@Configuration
public class MessagingServiceConfig {

    @Autowired
    private AriesClient ariesClient;

    @Bean
    @DependsOn({"ariesClient"})
    public MessagingService messagingService() {
        MessagingService messagingService = new MessagingService(ariesClient);
        return messagingService;
    }
}


