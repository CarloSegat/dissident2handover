package com.dissident.common.model;

public class AssociationResponse {
    private String role;
    private String did;
    // association requests can fail, in this case the isSuccesful field is false
    private boolean isSuccesful = true;

    public void setSuccesful(boolean isSuccesful) {
        this.isSuccesful = isSuccesful;
    }

    public boolean isSuccesful() {
        return isSuccesful;
    }

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
