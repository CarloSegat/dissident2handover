package com.dissident.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;

import java.net.ConnectException;
import org.hyperledger.aries.AriesClient;

@Configuration
@EnableRetry
public class AriesClientConfig {

    @Value("${aries.client.ip}")
    private String ip;

    @Value("${aries.client.port}")
    private String port;

    @Bean
    @Retryable(value = ConnectException.class, maxAttempts = 5, backoff = @Backoff(delay = 5000))
    public AriesClient ariesClient() {
        String url = String.format("http://%s:%s", ip, port);
        return AriesClient.builder()
            .url(url)
            .build();
    }
}
