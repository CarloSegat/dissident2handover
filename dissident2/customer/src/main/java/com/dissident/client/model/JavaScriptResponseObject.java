package com.dissident.client.model;

import org.hyperledger.acy_py.generated.model.ConnectionInvitation;

/**
 * Class representing the response object structure for handling JSON response.
 */
public class JavaScriptResponseObject {

    private String connectionId;
    private ConnectionInvitation invitation;
    private String invitationUrl;

    // Default constructor
    public JavaScriptResponseObject() {
    }

    // Constructor with all fields
    public JavaScriptResponseObject(String connectionId, ConnectionInvitation invitation, String invitationUrl) {
        this.connectionId = connectionId;
        this.invitation = invitation;
        this.invitationUrl = invitationUrl;
    }

    // Getters and Setters
    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public ConnectionInvitation getInvitation() {
        return invitation;
    }

    public void setInvitation(ConnectionInvitation invitation) {
        this.invitation = invitation;
    }

    public String getInvitationUrl() {
        return invitationUrl;
    }

    public void setInvitationUrl(String invitationUrl) {
        this.invitationUrl = invitationUrl;
    }

    // Optional: toString method for debugging
    @Override
    public String toString() {
        return "ResponseObject{" +
                "connectionId='" + connectionId + '\'' +
                ", invitation=" + invitation +
                ", invitationUrl='" + invitationUrl + '\'' +
                '}';
    }
}
