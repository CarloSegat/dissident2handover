package com.dissident.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;

import java.util.Optional;
import org.hyperledger.aries.AriesClient;
import org.hyperledger.aries.api.connection.ConnectionRecord;

@SpringBootApplication
@ComponentScan({
    "com.dissident.common.service",
    "com.dissident.common.model", 
    "com.dissident.common.config", 
})
public class ClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    @Autowired
    private AriesClient ac;

    @PostConstruct
    public void fetchConnections() {
        try {
            Optional<List<ConnectionRecord>> optionalConnections = ac.connections();

            if (optionalConnections.isPresent()) {
                List<ConnectionRecord> connections = optionalConnections.get();
                for (ConnectionRecord connection : connections) {
                    // Process each connection
                    System.out.println(connection);
                }
            } else {
                System.out.println("No connections found");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
