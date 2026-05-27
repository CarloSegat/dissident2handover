package com.dissident.client.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import com.dissident.common.model.ServiceInquiryResponseEvent;

@Component
public class ServiceInquiryResponseEventListener {

    @Autowired
    private final EventFutureManager eventFutureManager;

    // Constructor injection of the EventFutureManager
    public ServiceInquiryResponseEventListener(EventFutureManager eventFutureManager) {
        this.eventFutureManager = eventFutureManager;
    }

    @EventListener
    public void handleServiceInquiryResponseEvent(ServiceInquiryResponseEvent event) {
        // Extract the connectionId and content from the event
        String connectionId = event.getConnectionId();
        String content = event.getContent();

        System.out.println("Handling ServiceInquiryResponseEvent for connectionId: " + connectionId);

        // Complete the future associated with the connectionId
        // This will allow the /service-inquiry endpoint to proceed
        eventFutureManager.completeFutureWithEventContent(connectionId, content);
    }
}