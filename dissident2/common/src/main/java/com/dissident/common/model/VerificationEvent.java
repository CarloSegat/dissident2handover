package com.dissident.common.model;

import org.springframework.context.ApplicationEvent;

public class VerificationEvent extends ApplicationEvent {
    private String connectionId;
    private String src;

    public VerificationEvent(Object source, String connectionId, String src) {
        super(source);
        this.connectionId = connectionId;
        this.src = src;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public String getSrc() {
        return src;
    }
}