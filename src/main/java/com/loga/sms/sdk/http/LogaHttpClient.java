package com.loga.sms.sdk.http;

import com.loga.sms.sdk.exception.LogaSmsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author Sékou Sallah Sow <sowsekou@hotmail.com>
 */
public final class LogaHttpClient {

    private static final Logger log = LoggerFactory.getLogger(LogaHttpClient.class);
    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 10_000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 30_000;
    private static final String USER_AGENT = "LogaSMS-Java-SDK/1.0";

    private final int connectTimeoutMs;
    private final int readTimeoutMs;

    public LogaHttpClient() {
        this(DEFAULT_CONNECT_TIMEOUT_MS, DEFAULT_READ_TIMEOUT_MS);
    }

    public LogaHttpClient(int connectTimeoutMs, int readTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
    }

    /**
     * Sends a POST request with a JSON body.
     */
    public HttpResponse postJson(String url, String jsonBody, Map<String, String> headers) {
        return execute("POST", url, jsonBody, "application/json", headers);
    }

    /**
     * Sends a POST request with a form-urlencoded body.
     */
    public HttpResponse postForm(String url, String formBody, Map<String, String> headers) {
        return execute("POST", url, formBody, "application/x-www-form-urlencoded", headers);
    }

    /**
     * Sends a GET request.
     */
    public HttpResponse get(String url, Map<String, String> headers) {
        return execute("GET", url, null, null, headers);
    }

    private HttpResponse execute(String method, String url, String body, String contentType, Map<String, String> headers) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(connectTimeoutMs);
            conn.setReadTimeout(readTimeoutMs);
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Accept", "application/json");

            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            if (body != null && !body.isEmpty()) {
                conn.setDoOutput(true);
                if (contentType != null) {
                    conn.setRequestProperty("Content-Type", contentType + "; charset=utf-8");
                }
                byte[] payload = body.getBytes(StandardCharsets.UTF_8);
                conn.setFixedLengthStreamingMode(payload.length);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload);
                    os.flush();
                }
            }

            int statusCode = conn.getResponseCode();
            InputStream stream = (statusCode >= 200 && statusCode < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String responseBody = readStream(stream);

            log.debug("HTTP {} {} → {} ({}B)", method, url, statusCode, responseBody != null ? responseBody.length() : 0);

            return new HttpResponse(statusCode, responseBody);

        } catch (IOException e) {
            log.error("HTTP {} {} failed: {}", method, url, e.getMessage());
            throw new LogaSmsException("HTTP request failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String readStream(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        try (InputStream is = stream) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream(1024);
            byte[] chunk = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(chunk)) != -1) {
                buffer.write(chunk, 0, bytesRead);
            }
            return buffer.toString("UTF-8");
        }
    }

    /**
     * Simple value object for HTTP responses.
     */
    public static final class HttpResponse {
        private final int statusCode;
        private final String body;

        public HttpResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getBody() {
            return body;
        }

        public boolean isSuccessful() {
            return statusCode >= 200 && statusCode < 300;
        }
    }
}
