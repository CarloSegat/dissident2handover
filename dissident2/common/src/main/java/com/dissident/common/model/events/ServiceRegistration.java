package com.dissident.common.model.events;

abstract class ServiceRegistrationBase implements EventGroup {
    public String getTopLevelClassName() {
        return "ServiceRegistration";
    }
}

public class ServiceRegistration extends ServiceRegistrationBase {
    public static class ServiceRegistrationRequestSend extends ServiceRegistrationBase implements EventSubGroup {}
    public static class ServiceRegistrationRequestReceived extends ServiceRegistrationBase implements EventSubGroup {}
    public static class ServiceRegistrationResponseSend extends ServiceRegistrationBase implements EventSubGroup {}
    public static class ServiceRegistrationResponseReceived extends ServiceRegistrationBase implements EventSubGroup {}
}

