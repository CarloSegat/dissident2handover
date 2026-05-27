package com.dissident.common.model;

public class AssociationRequest {
    private String role;
    private String did;

    // Standard getters and setters for 'did'
    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    // Getter and setter for the new 'role' attribute
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}