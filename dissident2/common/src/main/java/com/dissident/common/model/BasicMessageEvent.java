package com.dissident.common.model;

import org.springframework.context.ApplicationEvent;

public class BasicMessageEvent extends ApplicationEvent {
    private String connectionId;
    private String content;

    public BasicMessageEvent(Object source, String connectionId, String content) {
        super(source);
        this.connectionId = connectionId;
        this.content = content;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public String getContent() {
        return content;
    }
}