package com.dissident.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.dissident.common.service.LogService;

@Configuration
public class LogServiceConfig {

    @Bean
    public LogService logService() {
        return new LogService();
    }
}




