package com.dissident.common.model;

public class ControllerConnectionRecord {
    private String connectionId; // Changed to String
    private String did;
    private String didDocument; // Assuming JSON is stored as String
    private boolean isTrusted;
    private String status; // Existing attribute
    private String role; // New attribute

    public ControllerConnectionRecord(String connectionId, String did, String didDocument, boolean isTrusted,
            String status, String role) {
        this.connectionId = connectionId;
        this.did = did;
        this.didDocument = didDocument;
        this.isTrusted = isTrusted;
        this.status = status.toLowerCase(); // Ensure status is in lowercase
        this.role = role.toLowerCase(); // Ensure role is in lowercase
    }

    // Getters
    public String getConnectionId() {
        return connectionId;
    }

    public String getDid() {
        return did;
    }

    public String getDidDocument() {
        return didDocument;
    }

    public boolean isTrusted() {
        return isTrusted;
    }

    public String getStatus() {
        return status;
    }

    public String getRole() {
        return role;
    }

    // Setters
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public void setDidDocument(String didDocument) {
        this.didDocument = didDocument;
    }

    public void setTrusted(boolean isTrusted) {
        this.isTrusted = isTrusted;
    }

    public void setStatus(String status) {
        this.status = status.toLowerCase(); // Convert to lowercase before storing
    }

    public void setRole(String role) {
        this.role = role.toLowerCase(); // Convert to lowercase before storing
    }
}
