package com.dissident.accesspoint.model;

import org.springframework.context.ApplicationEvent;

public class ServiceInquiryResponseEvent extends ApplicationEvent {
    private String connectionId;
    private String content;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source       the object on which the event initially occurred or with
     *                     which the event is associated (never {@code null})
     * @param connectionId the ID of the connection through which the inquiry was
     *                     made
     * @param content      the content to be sent back as part of the event,
     *                     typically the inquiry response
     */
    public ServiceInquiryResponseEvent(Object source, String connectionId, String content) {
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

    // Setters if you need them
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "ServiceInquiryResponseEvent{" +
                "connectionId='" + connectionId + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
