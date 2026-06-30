package com.loga.sms.sdk.model;

/**
 * @author Sékou Sallah Sow <sowsekou@hotmail.com>
 */
public class SMSSendRequest {
    private String receiverAddress;
    private String message;
    private String senderName;
    private String callbackUrl;
    private SmsPriority priority;

    public SMSSendRequest() {}

    public SMSSendRequest(String receiverAddress, String message, String senderName, String callbackUrl, SmsPriority priority) {
        this.receiverAddress = receiverAddress;
        this.message = message;
        this.senderName = senderName;
        this.callbackUrl = callbackUrl;
        this.priority = priority;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public SmsPriority getPriority() {
        return priority;
    }

    public void setPriority(SmsPriority priority) {
        this.priority = priority;
    }
}
