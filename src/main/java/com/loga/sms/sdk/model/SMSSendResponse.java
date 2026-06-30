package com.loga.sms.sdk.model;

/**
 * @author Sékou Sallah Sow <sowsekou@hotmail.com>
 */
public class SMSSendResponse {
    private String externalRefNo;
    private SMSRequestStatus status;
    private String message;

    public SMSSendResponse() {}

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
