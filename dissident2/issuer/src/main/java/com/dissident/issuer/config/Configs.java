package com.dissident.issuer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.dissident.common.config.AriesClientConfig;
import com.dissident.common.config.ConnectionManagerServiceConfig;
import com.dissident.common.config.DidExchangeServiceConfig;
import com.dissident.common.config.DidResolutionConfig;
import com.dissident.common.config.JsonConfig;
import com.dissident.common.config.LogServiceConfig;
import com.dissident.common.config.MessagingServiceConfig;
import com.dissident.common.config.VerificationServiceConfig;

@Configuration
@Import({
    LogServiceConfig.class,
    JsonConfig.class, 
    AriesClientConfig.class, 
    MessagingServiceConfig.class, 
    ConnectionManagerServiceConfig.class,
    DidResolutionConfig.class,
    VerificationServiceConfig.class,
    DidExchangeServiceConfig.class,
})
public class Configs {}
