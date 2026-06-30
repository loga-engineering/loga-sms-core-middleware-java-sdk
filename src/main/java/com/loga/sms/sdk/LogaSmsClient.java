package com.loga.sms.sdk;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loga.sms.sdk.config.LogaSmsProperties;
import com.loga.sms.sdk.exception.LogaSmsException;
import com.loga.sms.sdk.http.LogaHttpClient;
import com.loga.sms.sdk.http.TokenManager;
import com.loga.sms.sdk.model.SMSSendRequest;
import com.loga.sms.sdk.model.SMSSendResponse;
import com.loga.sms.sdk.model.SmsPriority;
import com.loga.sms.sdk.model.SmsStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Official Java SDK client for the Loga SMS Core Middleware API.
 *
 * <h3>Usage with application.properties (Spring Boot / Quarkus / Micronaut):</h3>
 * <pre>{@code
 * LogaSmsClient client = LogaSmsClient.create();
 * SMSSendResponse response = client.send("+22370000000", "Hello from Loga!");
 * }</pre>
 *
 * <h3>Usage with explicit builder (plain Java):</h3>
 * <pre>{@code
 * LogaSmsClient client = LogaSmsClient.builder()
 *     .tokenUrl("https://api.sms.loga-apps.com/oauth/v1/token")
 *     .clientId("my-client-id")
 *     .clientSecret("my-secret")
 *     .apiKey("my-api-key")
 *     .apiBaseUrl("https://api.sms.loga-apps.com")
 *     .defaultSenderName("MyApp")
 *     .build();
 *
 * SMSSendResponse response = client.send("+22370000000", "Hello!");
 * }</pre>
 *
 * <p>This client is <strong>thread-safe</strong>. Create one instance and share it
 * across your application.</p>
 *
 * @author Loga Engineering
 */
public final class LogaSmsClient {

    private static final Logger log = LoggerFactory.getLogger(LogaSmsClient.class);
    private static final String SMS_SEND_PATH = "/api/smsmessaging/v1/outbound/send";
    private static final String SMS_STATUS_PATH = "/api/smsmessaging/v1/status";

    private final LogaSmsProperties properties;
    private final LogaHttpClient httpClient;
    private final TokenManager tokenManager;
    private final ObjectMapper objectMapper;

    private LogaSmsClient(LogaSmsProperties properties, LogaHttpClient httpClient) {
        this.properties = properties;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        String tokenUrl = properties.getTokenUrl();
        if (tokenUrl == null || tokenUrl.trim().isEmpty()) {
            // Derive token URL from apiBaseUrl if not explicitly set
            tokenUrl = properties.getApiBaseUrl() + "/oauth/v1/token";
        }

        this.tokenManager = new TokenManager(
                tokenUrl,
                properties.getClientId(),
                properties.getClientSecret(),
                properties.getGrantType(),
                httpClient
        );
    }

    // ──────────────────────────────────────────────────────────────
    // Factory Methods
    // ──────────────────────────────────────────────────────────────

    /**
     * Creates a client by auto-loading configuration from {@code application.properties},
     * system properties, or environment variables.
     *
     * @return a ready-to-use {@link LogaSmsClient}
     * @throws IllegalArgumentException if required properties are missing
     */
    public static LogaSmsClient create() {
        LogaSmsProperties props = LogaSmsProperties.loadFromClasspath();
        props.validate();
        return new LogaSmsClient(props, new LogaHttpClient());
    }

    /**
     * Returns a new {@link Builder} for programmatic configuration.
     */
    public static Builder builder() {
        return new Builder();
    }

    // ──────────────────────────────────────────────────────────────
    // SMS Send Methods
    // ──────────────────────────────────────────────────────────────

    /**
     * Sends an SMS using the configured {@code default-sender-name} and {@code callback-url}.
     *
     * @param receiverAddress phone number in international format (e.g. "+22370000000")
     * @param message         the SMS body
     * @return the send response with external reference number and status
     * @throws LogaSmsException on any communication or authentication error
     */
    public SMSSendResponse send(String receiverAddress, String message) {
        return send(receiverAddress, message, SmsPriority.QUEUED);
    }

    /**
     * Sends an SMS using the configured {@code default-sender-name} and {@code callback-url}
     * with a specific priority.
     *
     * @param receiverAddress phone number in international format
     * @param message         the SMS body
     * @param priority        the delivery priority
     * @return the send response
     */
    public SMSSendResponse send(String receiverAddress, String message, SmsPriority priority) {
        return send(receiverAddress, message, properties.getDefaultSenderName(), properties.getCallbackUrl(), priority);
    }

    /**
     * Sends an SMS using the configured {@code default-sender-name} with a custom callback URL.
     *
     * @param receiverAddress phone number in international format
     * @param message         the SMS body
     * @param callbackUrl     the per-request callback URL
     * @return the send response
     */
    public SMSSendResponse sendWithCallback(String receiverAddress, String message, String callbackUrl) {
        return send(receiverAddress, message, properties.getDefaultSenderName(), callbackUrl, SmsPriority.QUEUED);
    }

    /**
     * Sends an SMS with a custom sender name, using the configured {@code callback-url}.
     *
     * @param receiverAddress phone number in international format
     * @param message         the SMS body
     * @param senderName      custom sender name for this message
     * @return the send response
     */
    public SMSSendResponse sendWithSenderName(String receiverAddress, String message, String senderName) {
        return send(receiverAddress, message, senderName, properties.getCallbackUrl(), SmsPriority.QUEUED);
    }

    /**
     * Full-control send: specify all parameters explicitly.
     *
     * @param receiverAddress phone number in international format
     * @param message         the SMS body
     * @param senderName      the sender name (may be null to use default)
     * @param callbackUrl     the callback URL (may be null to use default)
     * @param priority        the delivery priority
     * @return the send response
     * @throws LogaSmsException on any communication or authentication error
     */
    public SMSSendResponse send(String receiverAddress, String message, String senderName, String callbackUrl, SmsPriority priority) {
        if (receiverAddress == null || receiverAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("receiverAddress cannot be null or empty");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("message cannot be null or empty");
        }
        if (priority == null) {
            priority = SmsPriority.QUEUED;
        }
        if (senderName == null || senderName.trim().isEmpty()) {
            senderName = properties.getDefaultSenderName();
        }
        if (callbackUrl == null || callbackUrl.trim().isEmpty()) {
            callbackUrl = properties.getCallbackUrl();
        }

        SMSSendRequest request = new SMSSendRequest(receiverAddress, message, senderName, callbackUrl, priority);

        try {
            String jsonBody = objectMapper.writeValueAsString(request);
            String url = properties.getApiBaseUrl() + SMS_SEND_PATH;

            // DX: Use provided idempotencyKey if available, otherwise generate one
            String finalIdempotencyKey = properties.getIdempotencyKey();
            if (finalIdempotencyKey == null || finalIdempotencyKey.trim().isEmpty()) {
                finalIdempotencyKey = UUID.randomUUID().toString();
            }

            Map<String, String> headers = buildAuthHeaders();
            headers.put("Idempotency-Key", finalIdempotencyKey);

            log.debug("Sending SMS to {} via {} with Idempotency-Key: {}", receiverAddress, url, finalIdempotencyKey);

            LogaHttpClient.HttpResponse httpResponse = httpClient.postJson(url, jsonBody, headers);

            if (httpResponse.getStatusCode() == 401) {
                // Token may have been invalidated server-side; retry once
                log.info("Received 401, refreshing token and retrying...");
                tokenManager.invalidate();
                headers = buildAuthHeaders();
                headers.put("Idempotency-Key", finalIdempotencyKey);
                httpResponse = httpClient.postJson(url, jsonBody, headers);
            }

            if (!httpResponse.isSuccessful() && httpResponse.getStatusCode() != 202) {
                throw new LogaSmsException(
                        "SMS send failed with HTTP " + httpResponse.getStatusCode() + ": " + httpResponse.getBody(),
                        httpResponse.getStatusCode(),
                        httpResponse.getBody()
                );
            }

            return objectMapper.readValue(httpResponse.getBody(), SMSSendResponse.class);

        } catch (LogaSmsException e) {
            throw e;
        } catch (Exception e) {
            throw new LogaSmsException("Failed to send SMS: " + e.getMessage(), e);
        }
    }

    // ──────────────────────────────────────────────────────────────
    // SMS Status
    // ──────────────────────────────────────────────────────────────

    /**
     * Checks the delivery status of a previously submitted SMS.
     *
     * @param externalRefNo the external reference number returned by {@link #send}
     * @return the status response
     * @throws LogaSmsException on any communication or authentication error
     */
    public SmsStatusResponse status(String externalRefNo) {
        return status(externalRefNo, null);
    }

    public SmsStatusResponse statusByKey(String idempotencyKey) {
        return status(null, idempotencyKey);
    }

    public SmsStatusResponse status(String externalRefNo, String idempotencyKey) {
        if ((externalRefNo == null || externalRefNo.trim().isEmpty()) && (idempotencyKey == null || idempotencyKey.trim().isEmpty())) {
            throw new IllegalArgumentException("Either externalRefNo or idempotencyKey is required");
        }

        try {
            StringBuilder query = new StringBuilder("?");
            if (externalRefNo != null && !externalRefNo.trim().isEmpty()) {
                query.append("externalRefNo=").append(externalRefNo);
            } else {
                query.append("idempotencyKey=").append(idempotencyKey);
            }
            String url = properties.getApiBaseUrl() + SMS_STATUS_PATH + query.toString();
            Map<String, String> headers = buildAuthHeaders();

            LogaHttpClient.HttpResponse httpResponse = httpClient.get(url, headers);

            if (httpResponse.getStatusCode() == 401) {
                tokenManager.invalidate();
                headers = buildAuthHeaders();
                httpResponse = httpClient.get(url, headers);
            }

            if (!httpResponse.isSuccessful()) {
                throw new LogaSmsException(
                        "SMS status check failed with HTTP " + httpResponse.getStatusCode() + ": " + httpResponse.getBody(),
                        httpResponse.getStatusCode(),
                        httpResponse.getBody()
                );
            }

            return objectMapper.readValue(httpResponse.getBody(), SmsStatusResponse.class);

        } catch (LogaSmsException e) {
            throw e;
        } catch (Exception e) {
            throw new LogaSmsException("Failed to check SMS status: " + e.getMessage(), e);
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Internal helpers
    // ──────────────────────────────────────────────────────────────

    private Map<String, String> buildAuthHeaders() {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + tokenManager.getAccessToken());
        headers.put("X-API-KEY", properties.getApiKey());
        return headers;
    }

    // ──────────────────────────────────────────────────────────────
    // Builder
    // ──────────────────────────────────────────────────────────────

    /**
     * Fluent builder for constructing a {@link LogaSmsClient} programmatically.
     */
    public static final class Builder {
        private String tokenUrl;
        private String grantType = "client_credentials";
        private String clientId;
        private String clientSecret;
        private String apiKey;
        private String apiBaseUrl;
        private String callbackUrl;
        private String defaultSenderName;
        private String idempotencyKey;
        private int connectTimeoutMs = 10_000;
        private int readTimeoutMs = 30_000;

        private Builder() {
        }

        public Builder tokenUrl(String tokenUrl) {
            this.tokenUrl = tokenUrl;
            return this;
        }

        public Builder grantType(String grantType) {
            this.grantType = grantType;
            return this;
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder apiBaseUrl(String apiBaseUrl) {
            this.apiBaseUrl = apiBaseUrl;
            return this;
        }

        public Builder callbackUrl(String callbackUrl) {
            this.callbackUrl = callbackUrl;
            return this;
        }

        public Builder defaultSenderName(String defaultSenderName) {
            this.defaultSenderName = defaultSenderName;
            return this;
        }

        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public Builder connectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
            return this;
        }

        public Builder readTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
            return this;
        }

        /**
         * Builds the client. Starts from classpath properties, then overlays
         * any values set on this builder.
         *
         * @return a ready-to-use {@link LogaSmsClient}
         * @throws IllegalArgumentException if required properties are missing
         */
        public LogaSmsClient build() {
            LogaSmsProperties props = LogaSmsProperties.loadFromClasspath();

            // Overlay builder values (explicit values win over classpath)
            if (tokenUrl != null) props.setTokenUrl(tokenUrl);
            if (grantType != null) props.setGrantType(grantType);
            if (clientId != null) props.setClientId(clientId);
            if (clientSecret != null) props.setClientSecret(clientSecret);
            if (apiKey != null) props.setApiKey(apiKey);
            if (apiBaseUrl != null) props.setApiBaseUrl(apiBaseUrl);
            if (callbackUrl != null) props.setCallbackUrl(callbackUrl);
            if (defaultSenderName != null) props.setDefaultSenderName(defaultSenderName);
            if (idempotencyKey != null) props.setIdempotencyKey(idempotencyKey);

            props.validate();
            return new LogaSmsClient(props, new LogaHttpClient(connectTimeoutMs, readTimeoutMs));
        }
    }
}
