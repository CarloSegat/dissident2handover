package com.dissident.issuer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    // Use healthcheck endpoint, so that the client can check if the issuer is
    // running before starting.
    // This allow us to have some order in the deployment on Kubernetes.
    @GetMapping("/health")
    public String healthCheck() {
        return "OK";
    }
}