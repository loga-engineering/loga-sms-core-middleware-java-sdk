# Loga SMS Java SDK

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/loga-engineering/loga-sms-core-middleware-java-sdk)
[![Java 8+](https://img.shields.io/badge/java-8%2B-orange.svg)](https://www.oracle.com/java/technologies/javase-jdk8-downloads.html)

**Official Java SDK for the Loga SMS Core Middleware API — send and track SMS messages programmatically.**

## Features

- SMS sending with `QUEUED`, `INSTANT`, `TRANSACTION`, `CAMPAIGN` priorities
- 5 convenience overloads: `send()`, `send(receiverAddress, message, priority)`, `sendWithSenderName()`, `sendWithCallback()`, `send(receiverAddress, message, senderName, callbackUrl, priority)`
- Delivery status checking by `externalRefNo` or `idempotencyKey`
- Dedicated `statusByKey()` method for idempotency-key-based lookup
- OAuth2 client credentials authentication with automatic token refresh + 401 retry
- Idempotency-Key support (header-based, Stripe convention)
- Auto-load from `application.properties`, system properties, or environment variables
- Fluent builder with timeout, grant type, and all configuration options
- Thread-safe client, designed for singleton usage
- Java 8+ compatible (bytecode level 8)

## Table of Contents

- [Installation](#installation)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Usage](#usage)
- [API Reference](#api-reference)
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
// Basic send (uses defaults)
SMSSendResponse response = client.send("+22370000000", "Hello!");

// With specific priority
SMSSendResponse response = client.send("+22370000000", "Hello!", SmsPriority.INSTANT);

// Custom sender name
SMSSendResponse response = client.sendWithSenderName("+22370000000", "Hello!", "MyApp");

// Custom callback URL
SMSSendResponse response = client.sendWithCallback("+22370000000", "Hello!", "https://myapp.com/webhook");

// Full control
SMSSendResponse response = client.send(
    "+22370000000",
    "Hello!",
    "MyApp",
    "https://myapp.com/webhook",
    SmsPriority.INSTANT
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

### LogaSmsClient

| Method | Description |
|---|---|
| `send(receiverAddress, message)` | Send with default sender/priority |
| `send(receiverAddress, message, priority)` | Send with specific priority |
| `sendWithSenderName(receiverAddress, message, senderName)` | Send with custom sender name |
| `sendWithCallback(receiverAddress, message, callbackUrl)` | Send with custom callback URL |
| `send(receiverAddress, message, senderName, callbackUrl, priority)` | Full control send |
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
| `.idempotencyKey(String)` | Default idempotency key |
| `.connectTimeoutMs(int)` | HTTP connect timeout in ms |
| `.readTimeoutMs(int)` | HTTP read timeout in ms |

## Examples

A complete Spring Boot integration example is available under [`examples/spring-boot-app/`](examples/spring-boot-app/).

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## License

MIT © Loga Engineering — see [LICENSE](LICENSE) for details.
