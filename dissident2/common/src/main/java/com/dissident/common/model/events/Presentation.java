package com.dissident.common.model.events;

abstract class PresentationBase implements EventGroup {
    public String getTopLevelClassName() {
        return "Presentation";
    }
}

public class Presentation extends PresentationBase {
    public static class PresentationRequestSend extends PresentationBase implements EventSubGroup {}
    public static class PresentationRequestReceived extends PresentationBase implements EventSubGroup {}
    public static class PresentationResponseSend extends PresentationBase implements EventSubGroup {}
    public static class PresentationResponseReceived extends PresentationBase implements EventSubGroup {}
}

