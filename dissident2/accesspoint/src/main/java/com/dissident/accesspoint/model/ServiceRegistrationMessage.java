package com.dissident.accesspoint.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ServiceRegistrationMessage {
    private String messageType; // New field to determine the type of message
    private String serviceType;
    private Integer serviceId;
    private String serviceName;
    private String provider;
    private String endpoint;

    // Default constructor
    public ServiceRegistrationMessage() {
    }

    // Parameterized constructor

    @JsonCreator
    public ServiceRegistrationMessage(
            @JsonProperty("messageType") String messageType,
            @JsonProperty("serviceType") String serviceType,
            @JsonProperty("serviceId") Integer serviceId,
            @JsonProperty("serviceName") String serviceName,
            @JsonProperty("provider") String provider,
            @JsonProperty("endpoint") String endpoint) {
        this.messageType = messageType;
        this.serviceType = serviceType;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.provider = provider;
        this.endpoint = endpoint;
    }

    // Getter and setter methods
    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    // toString method
    @Override
    public String toString() {
        return "ServiceRegistrationMessage{" +
                "messageType='" + messageType + '\'' +
                ", serviceType='" + serviceType + '\'' +
                ", serviceId=" + serviceId +
                ", serviceName='" + serviceName + '\'' +
                ", provider='" + provider + '\'' +
                ", endpoint='" + endpoint + '\'' +
                '}';
    }
}
