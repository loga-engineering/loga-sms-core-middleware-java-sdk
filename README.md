# Loga SMS Java SDK

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/loga-engineering/loga-sms-core-middleware-java-sdk)
[![Java 8+](https://img.shields.io/badge/java-8%2B-orange.svg)](https://www.oracle.com/java/technologies/javase-jdk8-downloads.html)

**Official Java SDK for the Loga SMS Core Middleware API — send and track SMS messages programmatically.**

## Features

- SMS sending with `QUEUED`, `INSTANT`, `TRANSACTION`, `CAMPAIGN` priorities
- Multiple convenience overloads: `send()`, `send(..., priority)`, `sendWithSenderName()`, `sendWithCallback()`, `send(..., senderName, callbackUrl, priority)`
- **Per-request idempotency key** — auto-generated UUID or explicit via parameter
- Delivery status checking by `externalRefNo` or `idempotencyKey`
- Dedicated `statusByKey()` method for idempotency-key-based lookup
- OAuth2 client credentials authentication with automatic token refresh + 401 retry
- Idempotency-Key header (Stripe convention)
- Auto-load from `application.properties`, system properties, or environment variables
- Fluent builder with timeout and all configuration options
- **Spring Boot auto-configuration** (`@ConfigurationProperties` binding, auto-wired `LogaSmsClient` bean)
- **Quarkus / CDI support** (CDI producer, auto-discovery)
- Thread-safe client, designed for singleton usage
- Java 8+ compatible (bytecode level 8)

## Table of Contents

- [Installation](#installation)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Usage](#usage)
- [API Reference](#api-reference)
- [Framework Integration](#framework-integration)
- [Examples](#examples)
- [Contributing](#contributing)
- [License](#license)

## Installation

### Maven

```xml
<dependency>
  <groupId>com.loga</groupId>
  <artifactId>loga-sms-core-middleware-java-sdk</artifactId>
  <version>1.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.loga:loga-sms-core-middleware-java-sdk:1.0.0'
```

## Quick Start

```java
LogaSmsClient client = LogaSmsClient.create();
SMSSendResponse response = client.send("+22370000000", "Hello from Loga!");
System.out.println("Sent: " + response.getExternalRefNo());

// Check delivery status
SmsStatusResponse status = client.status(response.getExternalRefNo());
System.out.println("Status: " + status.getStatus());
```

## Configuration

The SDK reads from environment variables, system properties, or `application.properties` using the prefix `loga.api.sms-messaging.meteor.*`.

| Environment Variable | Property Key | Required | Default |
|---|---|---|---|
| `LOGA_API_SMS_MESSAGING_METEOR_OAUTH2_CLIENT_ID` | `loga.api.sms-messaging.meteor.oauth2.client-id` | For OAuth | — |
| `LOGA_API_SMS_MESSAGING_METEOR_OAUTH2_CLIENT_SECRET` | `loga.api.sms-messaging.meteor.oauth2.client-secret` | For OAuth | — |
| `LOGA_API_SMS_MESSAGING_METEOR_OAUTH2_API_KEY` | `loga.api.sms-messaging.meteor.oauth2.api-key` | Yes | — |
| `LOGA_API_SMS_MESSAGING_METEOR_OAUTH2_TOKEN_URL` | `loga.api.sms-messaging.meteor.oauth2.token-url` | No | `{baseUrl}/oauth/v1/token` |
| `LOGA_API_SMS_MESSAGING_METEOR_RESTCLIENT_API_BASE_URL` | `loga.api.sms-messaging.meteor.restclient.api-base-url` | No | `https://api.sms.loga-apps.com` |
| `LOGA_API_SMS_MESSAGING_METEOR_RESTCLIENT_CALLBACK_URL` | `loga.api.sms-messaging.meteor.restclient.callback-url` | No | — |
| `LOGA_API_SMS_MESSAGING_METEOR_DEFAULT_SENDER_NAME` | `loga.api.sms-messaging.meteor.default-sender-name` | No | — |

> **Note:** The idempotency key is not a configuration property. It is either auto-generated (UUID) for each `send()` call or passed explicitly as a parameter — see [Sending SMS with idempotency key](#sending-sms).

## Usage

### Creating a Client

```java
// Auto-load from environment variables / application.properties
LogaSmsClient client = LogaSmsClient.create();

// Full programmatic control via builder
LogaSmsClient client = LogaSmsClient.builder()
    .clientId("your-client-id")
    .clientSecret("your-client-secret")
    .apiKey("your-api-key")
    .apiBaseUrl("https://api.sms.loga-apps.com")
    .defaultSenderName("MyApp")
    .callbackUrl("https://myapp.com/webhook/sms")
    .tokenUrl("https://api.sms.loga-apps.com/oauth/v1/token")
    .grantType("client_credentials")
    .connectTimeoutMs(10_000)
    .readTimeoutMs(30_000)
    .build();
```

### Sending SMS

```java
// Basic send — idempotency key auto-generated
SMSSendResponse response = client.send("+22370000000", "Hello!");

// With specific priority
SMSSendResponse response = client.send("+22370000000", "Hello!", SmsPriority.INSTANT);

// Custom sender name
SMSSendResponse response = client.sendWithSenderName("+22370000000", "Hello!", "MyApp");

// Custom callback URL
SMSSendResponse response = client.sendWithCallback("+22370000000", "Hello!", "https://myapp.com/webhook");

// Full control — auto-generated idempotency key
SMSSendResponse response = client.send(
    "+22370000000",
    "Hello!",
    "MyApp",
    "https://myapp.com/webhook",
    SmsPriority.INSTANT
);
```

### Sending SMS with idempotency key

Pass an explicit idempotency key to ensure idempotent request delivery:

```java
// With explicit idempotency key (auto-generated if null/empty)
SMSSendResponse response = client.send("+22370000000", "Hello!", "my-unique-key-123");

// With explicit idempotency key + priority
SMSSendResponse response = client.send("+22370000000", "Hello!", SmsPriority.INSTANT, "my-unique-key-456");

// Full control + explicit idempotency key
SMSSendResponse response = client.send(
    "+22370000000",
    "Hello!",
    "MyApp",
    "https://myapp.com/webhook",
    SmsPriority.INSTANT,
    "my-unique-key-789"
);
```

### Checking Delivery Status

```java
// By external reference number (returned from send())
SmsStatusResponse status = client.status("ext-ref-12345");
System.out.println("Status: " + status.getStatus());
System.out.println("Receiver: " + status.getReceiverAddress());
System.out.println("Created: " + status.getCreatedAt());

// By idempotency key (dedicated method)
SmsStatusResponse status = client.statusByKey("my-idempotency-key-123");

// By either (returns whichever is non-null)
SmsStatusResponse status = client.status("ext-ref-12345", null);
```

### Error Handling

```java
try {
    SMSSendResponse response = client.send("+22370000000", "Hello!");
} catch (LogaSmsException e) {
    System.err.println("Error: " + e.getMessage());
    System.err.println("HTTP Status: " + e.getStatusCode());
    System.err.println("Body: " + e.getResponseBody());
}
```

## API Reference

### LogaSmsClient — Send Methods

| Method | Idempotency Key |
|---|---|
| `send(receiverAddress, message)` | Auto-generated |
| `send(receiverAddress, message, idempotencyKey)` | Explicit |
| `send(receiverAddress, message, priority)` | Auto-generated |
| `send(receiverAddress, message, priority, idempotencyKey)` | Explicit |
| `sendWithSenderName(receiverAddress, message, senderName)` | Auto-generated |
| `sendWithSenderName(receiverAddress, message, senderName, idempotencyKey)` | Explicit |
| `sendWithCallback(receiverAddress, message, callbackUrl)` | Auto-generated |
| `sendWithCallback(receiverAddress, message, callbackUrl, idempotencyKey)` | Explicit |
| `send(receiverAddress, message, senderName, callbackUrl, priority)` | Auto-generated |
| `send(receiverAddress, message, senderName, callbackUrl, priority, idempotencyKey)` | Explicit |

### LogaSmsClient — Status Methods

| Method | Description |
|---|---|
| `status(externalRefNo)` | Check status by external reference |
| `statusByKey(idempotencyKey)` | Check status by idempotency key |
| `status(externalRefNo, idempotencyKey)` | Check status by either (first non-null wins) |

### Builder

| Method | Description |
|---|---|
| `.clientId(String)` | OAuth2 client ID |
| `.clientSecret(String)` | OAuth2 client secret |
| `.apiKey(String)` | API key for header-based auth |
| `.apiBaseUrl(String)` | Base URL for the SMS API |
| `.tokenUrl(String)` | OAuth2 token endpoint URL |
| `.grantType(String)` | OAuth2 grant type (default: `client_credentials`) |
| `.defaultSenderName(String)` | Default sender name |
| `.callbackUrl(String)` | Default callback URL |
| `.connectTimeoutMs(int)` | HTTP connect timeout in ms |
| `.readTimeoutMs(int)` | HTTP read timeout in ms |

## Framework Integration

### Spring Boot

The SDK provides auto-configuration for Spring Boot. When the SDK is on the classpath, a `LogaSmsClient` bean is automatically created and properties are bound from the Spring Environment using the `loga.api.sms-messaging.meteor.*` prefix.

```properties
# application.properties
loga.api.sms-messaging.meteor.oauth2.client-id=${LOGA_SMS_CLIENT_ID}
loga.api.sms-messaging.meteor.oauth2.client-secret=${LOGA_SMS_CLIENT_SECRET}
loga.api.sms-messaging.meteor.oauth2.api-key=${LOGA_SMS_API_KEY}
loga.api.sms-messaging.meteor.restclient.api-base-url=https://api.sms.loga-apps.com
loga.api.sms-messaging.meteor.default-sender-name=MyApp
```

```java
@SpringBootApplication
public class MyApplication {
    // LogaSmsClient is auto-wired — no manual bean definition needed
}
```

### Quarkus / CDI

A CDI producer is included for Quarkus and other CDI containers. The `LogaSmsClient` bean is automatically produced from classpath configuration.

```java
@Path("/sms")
public class SmsResource {
    @Inject
    LogaSmsClient smsClient;

    @POST
    @Path("/send")
    public Response send(String to, String message) {
        SMSSendResponse response = smsClient.send(to, message);
        return Response.ok(response).build();
    }
}
```

## Examples

A complete Spring Boot integration example is available under [`examples/spring-boot-app/`](examples/spring-boot-app/).
