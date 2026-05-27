package com.dissident.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RegisteredService {
    private String serviceType;
    private Integer serviceId;
    private String serviceName;
    private String provider;
    private String endpoint;

    // Default constructor
    public RegisteredService() {
    }

    @JsonCreator
    public RegisteredService(
            @JsonProperty("serviceType") String serviceType,
            @JsonProperty("serviceId") Integer serviceId,
            @JsonProperty("serviceName") String serviceName,
            @JsonProperty("provider") String provider,
            @JsonProperty("endpoint") String endpoint) {
        this.serviceType = serviceType;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.provider = provider;
        this.endpoint = endpoint;
    }

    // Getter and setter methods
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

    // toString method for debugging
    @Override
    public String toString() {
        return "RegisteredService{" +
                "serviceType='" + serviceType + '\'' +
                ", serviceId=" + serviceId +
                ", serviceName='" + serviceName + '\'' +
                ", provider='" + provider + '\'' +
                ", endpoint='" + endpoint + '\'' +
                '}';
    }
}
