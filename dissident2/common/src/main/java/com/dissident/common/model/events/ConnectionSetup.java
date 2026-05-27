package com.dissident.common.model.events;

abstract class ConnectionSetupBase implements EventGroup {
    public String getTopLevelClassName() {
        return "ConnectionSetup";
    }
}

public class ConnectionSetup extends ConnectionSetupBase {
    public static class ConnectionRequestSend extends ConnectionSetupBase implements EventSubGroup {}
    public static class ConnectionRequestReceived extends ConnectionSetupBase implements EventSubGroup {}
    public static class ConnectionResponseSend extends ConnectionSetupBase implements EventSubGroup {}
    public static class ConnectionResponseReceived extends ConnectionSetupBase implements EventSubGroup {}
}
