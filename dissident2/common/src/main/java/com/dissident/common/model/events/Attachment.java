package com.dissident.common.model.events;

abstract class AttachmentBase implements EventGroup {
    public String getTopLevelClassName() {
        return "Attachment";
    }
}

public class Attachment extends AttachmentBase {
    public static class AttachmentRequestSend extends AttachmentBase implements EventSubGroup {}
    public static class AttachmentRequestReceived extends AttachmentBase implements EventSubGroup {}
    public static class AttachmentResponseSend extends AttachmentBase implements EventSubGroup {}
    public static class AttachmentResponseReceived extends AttachmentBase implements EventSubGroup {}
}

