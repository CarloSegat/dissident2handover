package com.dissident.common.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.dissident.common.model.EnumEntity;
import com.dissident.common.model.EventModel;
import com.dissident.common.model.events.EventSubGroup;
import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Service
public class LogService {

    HttpClient client = HttpClient.newHttpClient();

    @Value("${entity.role}")
    private String myRole;

    public void logEvent(EnumEntity src, EnumEntity trg, EventSubGroup eventType, Map<String, String> content) {
        // String url = "http-logger-service:8887/http-logger/ciao";

        if(src == null && trg == null){
            throw new RuntimeException("Must specify at least src or trg");
            // logEvent(null, null, null, new HashMap<String, String>());
        }

        if(src == trg){
            throw new RuntimeException("Src and trg must be different");
            // logEvent(null, null, null, new HashMap<String, String>());
        }

        if(src == null){
            logEvent(EnumEntity.fromString(myRole), trg, eventType, new HashMap<String, String>());
            return;
        }

        if(trg == null){
            logEvent(src, EnumEntity.fromString(myRole), eventType, new HashMap<String, String>());
            return;
        }

        System.out.println("logging src: " + src + " trg: " + trg + " type " + eventType);

        EventModel e = new EventModel();
        e.srcEntity = src;
        e.trgEntity = trg;
        e.timestamp = System.currentTimeMillis();
        e.content = content;
        e.subGroup = eventType;

        Map<String, Object> data = e.toHashMap();
        String json = new Gson().toJson(data);


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://http-logger-service:8887/http-logger"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(System.out::println)
                .join();
    }

    public void logEvent(EnumEntity src, EnumEntity trg, EventSubGroup eventType) {
        logEvent(src, trg, eventType, new HashMap<String, String>());
    }
}
