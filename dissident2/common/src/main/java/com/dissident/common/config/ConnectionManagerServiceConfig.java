package com.dissident.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.dissident.common.service.ConnectionManagerService;

@Configuration
public class ConnectionManagerServiceConfig {

    @Bean
    public ConnectionManagerService connectionManagerService() {
        return new ConnectionManagerService();
    }
}