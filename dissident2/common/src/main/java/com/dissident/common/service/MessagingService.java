package com.dissident.common.service;

import java.io.IOException;

import org.hyperledger.acy_py.generated.model.SendMessage;
import org.hyperledger.aries.AriesClient;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class MessagingService {
    private final AriesClient ariesClient;

    public MessagingService(AriesClient ariesClient) {
        this.ariesClient = ariesClient;
    }

    /**
     * Send a basic message to a connection
     * 
     * @param connectionId the connection id
     * @param message      the message to send
     * @throws IOException if the request could not be executed
     */
    public void sendMessage(@NonNull String connectionId, @NonNull SendMessage message) throws IOException {
        ariesClient.connectionsSendMessage(connectionId, message);
    }
}
