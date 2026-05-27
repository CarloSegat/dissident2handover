package com.dissident.issuer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "entity")
public class AppProperties {

    private String schemaId;
    private String credentialDefId;
    private String did; // Add this field

    // Getters and Setters for schemaId, credentialDefId, and did

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    // Getters and setters
    public String getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }

    public String getCredentialDefId() {
        return credentialDefId;
    }

    public void setCredentialDefId(String credentialDefId) {
        this.credentialDefId = credentialDefId;
    }
}
