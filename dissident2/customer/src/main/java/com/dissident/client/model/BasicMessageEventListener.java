package com.dissident.client.model;

import org.springframework.context.event.EventListener;

import java.io.IOException;

import org.hyperledger.acy_py.generated.model.SendMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dissident.common.model.BasicMessageEvent;
import com.dissident.common.model.EnumEntity;


import com.dissident.common.model.VerificationEvent;
import com.dissident.common.model.events.ServiceUsage;
import com.dissident.common.service.DidResolutionService;
import com.dissident.common.service.LogService;
import com.dissident.common.service.MessagingService;

@Component
public class BasicMessageEventListener {

    private boolean clientToggle = true;
    private boolean providerToggle = false;

    @Autowired
    private DidResolutionService didResolutionService;

    @Autowired
    private LogService logService;

    @Autowired
    private MessagingService messagingService;

    @EventListener
    public void onBasicMessageEvent(BasicMessageEvent event) {
        System.out.println("CUSTOMER onBasicMessageEvent");
        didResolutionService.completeFuture(event.getConnectionId(), event);
    }

    @EventListener
    public void onVerificationEvent(VerificationEvent event) {
        System.out.println("event.getSrc() " + event.getSrc());
        
        if(event.getSrc().equals("provider")){
            // provider has verified the customer

            System.out.println("this.providerToggle " + this.providerToggle);

            if(! this.providerToggle){
                this.providerToggle = ! this.providerToggle;
                return;
            }

            logService.logEvent(
                EnumEntity.PROVIDER,
                EnumEntity.CUSTOMER,
                new ServiceUsage.ServiceUsageResponseSend()
            );

            SendMessage messageToSend = SendMessage.builder()
                .content("{\"messageType\": \"ServiceUsageResponse\",\"qrCode\": \"https://api.qrserver.com/v1/create-qr-code/?size=500x500&data=https://drive.google.com/file/d/1DWyjlWgRhRlUQgHM9mFvylCD89qLuKIw/view?usp=sharing\"}")
                .build();

            try {
                messagingService.sendMessage(event.getConnectionId(), messageToSend);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if(event.getSrc().equals("customer")){
            // customer has verified the provider
            // actual serviceUsage request sent here even though in sequence diagram we
            // place it before

            System.out.println("this.clientToggle " + this.clientToggle);

            if(! this.clientToggle){
                this.clientToggle = ! this.clientToggle;
                return;
            }

            SendMessage messageToSend = SendMessage.builder()
                .content("{\"messageType\": \"ServiceUsageRequest\"}")
                .build();

            try {
                messagingService.sendMessage(event.getConnectionId(), messageToSend);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
