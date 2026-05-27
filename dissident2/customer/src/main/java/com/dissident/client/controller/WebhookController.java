package com.dissident.client.controller;

import org.hyperledger.aries.AriesClient;
import org.springframework.web.bind.annotation.*;

import com.dissident.common.WebhookControllerClientBase;
import com.dissident.common.service.ConnectionManagerService;
import com.dissident.common.service.DidResolutionService;
import com.dissident.common.service.LogService;
import com.dissident.common.service.MessagingService;
import com.dissident.common.service.VerificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationEventPublisher;

@RestController
@RequestMapping("/webhook")
@CrossOrigin
public class WebhookController extends WebhookControllerClientBase {

    public WebhookController(
            ObjectMapper objectMapper,
            ConnectionManagerService connectionManagerService,
            DidResolutionService didResolutionService,
            AriesClient ariesClient,
            VerificationService verificationService,
            MessagingService messagingService,
            ApplicationEventPublisher eventPublisher,
            LogService logService) {
        super(objectMapper, connectionManagerService, didResolutionService, ariesClient, verificationService,
                messagingService, eventPublisher, logService);
    }
}