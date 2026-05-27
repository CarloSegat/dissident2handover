package com.dissident.common.model.events;

abstract class ServiceUsageBase implements EventGroup {
    public String getTopLevelClassName() {
        return "ServiceUsage";
    }
}

public class ServiceUsage extends ServiceUsageBase {
    public static class ServiceUsageRequestSend extends ServiceUsageBase implements EventSubGroup {}
    public static class ServiceUsageRequestReceived extends ServiceUsageBase implements EventSubGroup {}
    public static class ServiceUsageResponseSend extends ServiceUsageBase implements EventSubGroup {}
    public static class ServiceUsageResponseReceived extends ServiceUsageBase implements EventSubGroup {}
}

