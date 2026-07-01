package com.loga.sms.sample.controller;

import com.loga.sms.sdk.LogaSmsClient;
import com.loga.sms.sdk.exception.LogaSmsException;
import com.loga.sms.sdk.model.SMSSendResponse;
import com.loga.sms.sdk.model.SmsPriority;
import com.loga.sms.sdk.model.SmsStatusResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Sékou Sallah Sow <sowsekou@hotmail.com>
 */
@RestController
@RequestMapping("/sms")
public class SmsController {

    private final LogaSmsClient smsClient;

    public SmsController(LogaSmsClient smsClient) {
        this.smsClient = smsClient;
    }

    @PostMapping("/send/default")
    public ResponseEntity<?> sendWithDefaults(
            @RequestParam String to,
            @RequestParam String message) {
        try {
            SMSSendResponse response = smsClient.send(to, message);
            return ResponseEntity.ok(Map.of(
                    "externalRefNo", response.getExternalRefNo(),
                    "status", response.getStatus(),
                    "message", response.getMessage(),
                    "mode", "default sender + default callback"
            ));
        } catch (LogaSmsException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                    "error", e.getMessage(),
                    "statusCode", e.getStatusCode()
            ));
        }
    }

    @PostMapping("/send/custom-sender")
    public ResponseEntity<?> sendWithCustomSender(
            @RequestParam String to,
            @RequestParam String message,
            @RequestParam String senderName) {
        try {
            SMSSendResponse response = smsClient.sendWithSenderName(to, message, senderName);
            return ResponseEntity.ok(Map.of(
                    "externalRefNo", response.getExternalRefNo(),
                    "status", response.getStatus(),
                    "message", response.getMessage(),
                    "mode", "custom sender: " + senderName
            ));
        } catch (LogaSmsException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                    "error", e.getMessage(),
                    "statusCode", e.getStatusCode()
            ));
        }
    }

    @PostMapping("/send/custom-callback")
    public ResponseEntity<?> sendWithCustomCallback(
            @RequestParam String to,
            @RequestParam String message,
            @RequestParam String callbackUrl) {
        try {
            SMSSendResponse response = smsClient.sendWithCallback(to, message, callbackUrl);
            return ResponseEntity.ok(Map.of(
                    "externalRefNo", response.getExternalRefNo(),
                    "status", response.getStatus(),
                    "message", response.getMessage(),
                    "mode", "custom callback: " + callbackUrl
            ));
        } catch (LogaSmsException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                    "error", e.getMessage(),
                    "statusCode", e.getStatusCode()
            ));
        }
    }

    @PostMapping("/send/full")
    public ResponseEntity<?> sendFull(
            @RequestParam String to,
            @RequestParam String message,
            @RequestParam(required = false) String senderName,
            @RequestParam(required = false) String callbackUrl,
            @RequestParam(defaultValue = "QUEUED") String priority,
            @RequestParam(required = false) String idempotencyKey) {
        try {
            SmsPriority smsPriority = SmsPriority.valueOf(priority.toUpperCase());
            SMSSendResponse response = smsClient.send(to, message, senderName, callbackUrl, smsPriority, idempotencyKey);
            return ResponseEntity.ok(Map.of(
                    "externalRefNo", response.getExternalRefNo(),
                    "status", response.getStatus(),
                    "message", response.getMessage(),
                    "mode", "full control",
                    "priority", priority,
                    "idempotencyKey", idempotencyKey != null ? idempotencyKey : "auto-generated"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid priority. Use: INSTANT, TRANSACTION, CAMPAIGN, QUEUED"
            ));
        } catch (LogaSmsException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                    "error", e.getMessage(),
                    "statusCode", e.getStatusCode()
            ));
        }
    }

    @PostMapping("/send/with-idempotency")
    public ResponseEntity<?> sendWithIdempotency(
            @RequestParam String to,
            @RequestParam String message,
            @RequestParam String idempotencyKey) {
        try {
            SMSSendResponse response = smsClient.send(to, message, idempotencyKey);
            return ResponseEntity.ok(Map.of(
                    "externalRefNo", response.getExternalRefNo(),
                    "status", response.getStatus(),
                    "message", response.getMessage(),
                    "mode", "custom idempotency key"
            ));
        } catch (LogaSmsException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                    "error", e.getMessage(),
                    "statusCode", e.getStatusCode()
            ));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> checkStatus(
            @RequestParam(required = false) String externalRefNo,
            @RequestParam(required = false) String idempotencyKey) {
        try {
            SmsStatusResponse status = externalRefNo != null
                    ? smsClient.status(externalRefNo)
                    : smsClient.statusByKey(idempotencyKey);
            return ResponseEntity.ok(Map.of(
                    "externalRefNo", status.getExternalRefNo(),
                    "status", status.getStatus(),
                    "receiverAddress", status.getReceiverAddress(),
                    "createdAt", status.getCreatedAt(),
                    "updatedAt", status.getUpdatedAt(),
                    "message", status.getMessage()
            ));
        } catch (LogaSmsException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                    "error", e.getMessage(),
                    "statusCode", e.getStatusCode()
            ));
        }
    }
}
