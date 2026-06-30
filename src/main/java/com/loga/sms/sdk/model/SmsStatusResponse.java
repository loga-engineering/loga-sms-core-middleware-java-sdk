package com.loga.sms.sdk.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Sékou Sallah Sow <sowsekou@hotmail.com>
 */
public class SmsStatusResponse {

    @JsonProperty("externalRefNo")
    private String externalRefNo;

    @JsonProperty("status")
    private SMSRequestStatus status;

    @JsonProperty("receiverAddress")
    private String receiverAddress;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("updatedAt")
    private String updatedAt;

    @JsonProperty("message")
    private String message;

    public SmsStatusResponse() {
    }

    public String getExternalRefNo() {
        return externalRefNo;
    }

    public void setExternalRefNo(String externalRefNo) {
        this.externalRefNo = externalRefNo;
    }

    public SMSRequestStatus getStatus() {
        return status;
    }

    public void setStatus(SMSRequestStatus status) {
        this.status = status;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
