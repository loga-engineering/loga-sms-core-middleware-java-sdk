package com.loga.sms.sdk.config;

import java.io.InputStream;
import java.util.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Loads and manages configuration properties for the Loga SMS SDK.
 * It automatically attempts to read from 'application.properties' in the classpath.
 * <p>
 * When used with Spring Boot, properties are bound automatically from the
 * environment using the prefix {@value #PREFIX}. In plain Java, use
 * {@link #loadFromClasspath()} or the {@link com.loga.sms.sdk.LogaSmsClient.Builder}.
 */
@ConfigurationProperties(prefix = "loga.api.sms-messaging.meteor")
public class LogaSmsProperties {

    public static final String PREFIX = "loga.api.sms-messaging.meteor";

    private String tokenUrl = this.apiBaseUrl + "/oauth/v1/token"; // Default TokenURL
    private String grantType = "client_credentials";
    private String clientId;
    private String clientSecret;
    private String apiKey;
    private String apiBaseUrl = "https://api.sms.loga-apps.com"; // Default URL as placeholder
    private String callbackUrl;
    private String defaultSenderName;

    public LogaSmsProperties() {
    }

    /**
     * Attempts to load properties from application.properties in the classpath.
     */
    public static LogaSmsProperties loadFromClasspath() {
        LogaSmsProperties props = new LogaSmsProperties();
        Properties systemProps = new Properties();

        try (InputStream is = LogaSmsProperties.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                systemProps.load(is);
            }
        } catch (Exception e) {
            // Ignore if file doesn't exist or cannot be read. We rely on defaults or explicit builder config.
        }

        // Try application.yml ? Standard Java properties parser doesn't support YML. 
        // In Spring Boot, environment variables / Spring Environment usually injects these.
        // We will read system properties and application.properties.

        props.tokenUrl = getProperty(systemProps, PREFIX + ".oauth2.token-url", props.tokenUrl);
        props.grantType = getProperty(systemProps, PREFIX + ".oauth2.grant-type", props.grantType);
        props.clientId = getProperty(systemProps, PREFIX + ".oauth2.client-id", props.clientId);
        props.clientSecret = getProperty(systemProps, PREFIX + ".oauth2.client-secret", props.clientSecret);
        props.apiKey = getProperty(systemProps, PREFIX + ".oauth2.api-key", props.apiKey);
        props.apiBaseUrl = getProperty(systemProps, PREFIX + ".restclient.api-base-url", props.apiBaseUrl);
        props.callbackUrl = getProperty(systemProps, PREFIX + ".restclient.callback-url", props.callbackUrl);
        props.defaultSenderName = getProperty(systemProps, PREFIX + ".default-sender-name", props.defaultSenderName);

        return props;
    }

    private static String getProperty(Properties props, String key, String defaultValue) {
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.trim().isEmpty()) {
            return sysProp;
        }

        String envProp = System.getenv(key.replace('.', '_').replace('-', '_').toUpperCase());
        if (envProp != null && !envProp.trim().isEmpty()) {
            return envProp;
        }

        return props.getProperty(key, defaultValue);
    }

    // Getters and Setters

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getDefaultSenderName() {
        return defaultSenderName;
    }

    public void setDefaultSenderName(String defaultSenderName) {
        this.defaultSenderName = defaultSenderName;
    }

    public void validate() {
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new IllegalArgumentException("clientId cannot be null or empty");
        }
        if (clientSecret == null || clientSecret.trim().isEmpty()) {
            throw new IllegalArgumentException("clientSecret cannot be null or empty");
        }
        if (tokenUrl == null || tokenUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("tokenUrl cannot be null or empty");
        }
        if (apiBaseUrl == null || apiBaseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("apiBaseUrl cannot be null or empty");
        }
    }
}
