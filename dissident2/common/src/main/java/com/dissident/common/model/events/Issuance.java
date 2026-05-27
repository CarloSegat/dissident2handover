package com.dissident.common.model.events;

abstract class IssuanceBase implements EventGroup {
    public String getTopLevelClassName() {
        return "Issuance";
    }
}

public class Issuance extends IssuanceBase {
    public static class IssuanceRequestSend extends IssuanceBase implements EventSubGroup {}
    public static class IssuanceRequestReceived extends IssuanceBase implements EventSubGroup {}
    public static class IssuanceResponseSend extends IssuanceBase implements EventSubGroup {}
    public static class IssuanceResponseReceived extends IssuanceBase implements EventSubGroup {}
}

