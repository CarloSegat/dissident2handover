package com.dissident.client.model;

import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class EventFutureManager {
    private final Map<String, CompletableFuture<String>> futures = new ConcurrentHashMap<>();

    public void registerFutureForConnectionId(String connectionId, CompletableFuture<String> future) {
        futures.put(connectionId, future);
    }

    public void completeFutureWithEventContent(String connectionId, String content) {
        CompletableFuture<String> future = futures.get(connectionId);
        if (future != null) {
            future.complete(content);
            futures.remove(connectionId);
        }
    }
}
