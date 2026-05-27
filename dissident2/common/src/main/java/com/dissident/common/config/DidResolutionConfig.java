package com.dissident.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.dissident.common.service.ConnectionManagerService;
import com.dissident.common.service.DidResolutionService;
import com.dissident.common.service.LogService;
import com.dissident.common.service.MessagingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class DidResolutionConfig {
    @Autowired
    private ConnectionManagerService connectionManagerService;

    @Autowired
    private MessagingService messagingService;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LogService logService;

    @Bean   
    public DidResolutionService didResolutionService() throws JsonMappingException, JsonProcessingException {
        return new DidResolutionService(connectionManagerService, messagingService, objectMapper, logService);
    }
}