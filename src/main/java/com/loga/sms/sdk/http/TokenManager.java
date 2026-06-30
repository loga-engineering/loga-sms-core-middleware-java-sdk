package com.loga.sms.sdk.http;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loga.sms.sdk.exception.LogaSmsException;
import com.loga.sms.sdk.model.OAuth2TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.Collections;

/**
 * Thread-safe OAuth2 token manager with automatic refresh.
 *
 * <p>Uses a {@code volatile} cached token and refreshes it proactively
 * before expiry (with a safety margin). Consumers of the SDK never
 * need to worry about token lifecycle — this class handles it entirely.</p>
 *
 * <p>Double-checked locking ensures only one thread refreshes at a time,
 * while all others read the cached value without blocking.</p>
 */
public final class TokenManager {

    private static final Logger log = LoggerFactory.getLogger(TokenManager.class);

    /**
     * Refresh the token 60 seconds before actual expiry to avoid
     * racing against clock skew or network latency.
     */
    private static final long SAFETY_MARGIN_MS = 60_000L;

    private final String tokenUrl;
    private final String clientId;
    private final String clientSecret;
    private final String grantType;
    private final LogaHttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Volatile guarantees visibility across threads without full synchronization
     * on the hot-path (reads).
     */
    private volatile CachedToken cachedToken;

    private final Object refreshLock = new Object();

    public TokenManager(String tokenUrl, String clientId, String clientSecret, String grantType, LogaHttpClient httpClient) {
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.grantType = grantType;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Returns a valid access token. Refreshes automatically if expired or about to expire.
     *
     * @return a non-null, valid Bearer access token
     * @throws LogaSmsException if the token endpoint returns an error
     */
    public String getAccessToken() {
        CachedToken current = this.cachedToken;
        if (current != null && current.isExpiredOrExpiring()) {
            return current.accessToken;
        }

        synchronized (refreshLock) {
            // Double-check: another thread may have refreshed while we waited
            current = this.cachedToken;
            if (current != null && current.isExpiredOrExpiring()) {
                return current.accessToken;
            }

            log.debug("Refreshing OAuth2 token from {}", tokenUrl);
            this.cachedToken = fetchNewToken();
            log.info("OAuth2 token refreshed successfully, expires in {}s", this.cachedToken.expiresInSeconds);
            return this.cachedToken.accessToken;
        }
    }

    /**
     * Forces a token refresh, e.g. after receiving a 401.
     */
    public void invalidate() {
        this.cachedToken = null;
    }

    private CachedToken fetchNewToken() {
        try {
            String formBody = "clientId=" + urlEncode(clientId)
                    + "&clientSecret=" + urlEncode(clientSecret)
                    + "&grant_type=" + urlEncode(grantType);

            LogaHttpClient.HttpResponse response = httpClient.postForm(tokenUrl, formBody, Collections.<String, String>emptyMap());

            if (!response.isSuccessful()) {
                throw new LogaSmsException(
                        "OAuth2 token request failed with status " + response.getStatusCode() + ": " + response.getBody(),
                        response.getStatusCode(),
                        response.getBody()
                );
            }

            OAuth2TokenResponse tokenResponse = objectMapper.readValue(response.getBody(), OAuth2TokenResponse.class);

            if (tokenResponse.getAccessToken() == null || tokenResponse.getAccessToken().trim().isEmpty()) {
                throw new LogaSmsException("OAuth2 token response did not contain an access_token");
            }

            return new CachedToken(
                    tokenResponse.getAccessToken(),
                    tokenResponse.getExpiresIn(),
                    System.currentTimeMillis()
            );

        } catch (LogaSmsException e) {
            throw e;
        } catch (Exception e) {
            throw new LogaSmsException("Failed to obtain OAuth2 token: " + e.getMessage(), e);
        }
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

    /**
     * Immutable holder for a cached token and its metadata.
     */
    private static final class CachedToken {
        final String accessToken;
        final long expiresInSeconds;
        final long obtainedAtMs;

        CachedToken(String accessToken, long expiresInSeconds, long obtainedAtMs) {
            this.accessToken = accessToken;
            this.expiresInSeconds = expiresInSeconds;
            this.obtainedAtMs = obtainedAtMs;
        }

        boolean isExpiredOrExpiring() {
            long expiresAtMs = obtainedAtMs + (expiresInSeconds * 1000L);
            return System.currentTimeMillis() < (expiresAtMs - SAFETY_MARGIN_MS);
        }
    }
}
