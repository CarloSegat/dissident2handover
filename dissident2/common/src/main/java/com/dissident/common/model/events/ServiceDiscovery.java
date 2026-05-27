package com.dissident.common.model.events;

abstract class ServiceDiscoveryBase implements EventGroup {
    public String getTopLevelClassName() {
        return "ServiceDiscovery";
    }
}

public class ServiceDiscovery extends ServiceDiscoveryBase {
    public static class ServiceDiscoveryRequestSend extends ServiceDiscoveryBase implements EventSubGroup {}
    public static class ServiceDiscoveryRequestReceived extends ServiceDiscoveryBase implements EventSubGroup {}
    public static class ServiceDiscoveryResponseSend extends ServiceDiscoveryBase implements EventSubGroup {}
    public static class ServiceDiscoveryResponseReceived extends ServiceDiscoveryBase implements EventSubGroup {}
}

