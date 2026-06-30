package com.loga.sms.sdk.exception;

/**
 * Base exception for the Loga SMS SDK.
 */
public class LogaSmsException extends RuntimeException {

    private int statusCode;
    private String responseBody;

    public LogaSmsException(String message) {
        super(message);
    }

    public LogaSmsException(String message, Throwable cause) {
        super(message, cause);
    }

    public LogaSmsException(String message, int statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
