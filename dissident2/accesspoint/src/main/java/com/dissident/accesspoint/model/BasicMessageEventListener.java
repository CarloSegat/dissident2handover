package com.dissident.accesspoint.model;

import org.springframework.context.event.EventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dissident.common.model.BasicMessageEvent;
import com.dissident.common.service.DidResolutionService;

@Component
public class BasicMessageEventListener {

    @Autowired
    private DidResolutionService didResolutionService;

    @EventListener
    public void onBasicMessageEvent(BasicMessageEvent event) {
        didResolutionService.completeFuture(event.getConnectionId(), event);
    }
}
