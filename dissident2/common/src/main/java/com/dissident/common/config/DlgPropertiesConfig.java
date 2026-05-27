package com.dissident.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "dlg")
public class DlgPropertiesConfig {

    private String did;
    private String didDocument;

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getDidDocument() {
        return didDocument;
    }

    public void setDidDocument(String didDocument) {
        this.didDocument = didDocument;
    }
}