package com.dissident.common.model.events;

abstract class DidResolutionBase implements EventGroup {
    public String getTopLevelClassName() {
        return "DidResolution";
    }
}

public class DidResolution extends DidResolutionBase {
    public static class DidResolutionRequestSend extends DidResolutionBase implements EventSubGroup {}
    public static class DidResolutionRequestReceived extends DidResolutionBase implements EventSubGroup {}
    public static class DidResolutionResponseSend extends DidResolutionBase implements EventSubGroup {}
    public static class DidResolutionResponseReceived extends DidResolutionBase implements EventSubGroup {}
}
