# Loga SMS Java SDK

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/loga-engineering/loga-sms-core-middleware-java-sdk)
[![Java 8+](https://img.shields.io/badge/java-8%2B-orange.svg)](https://www.oracle.com/java/technologies/javase-jdk8-downloads.html)

**Official Java SDK for the Loga SMS Core Middleware API — send and track SMS messages programmatically.**

## Features

- SMS sending with Instant, Transaction, Campaign, Queued priorities
- Delivery status checking
- OAuth2 client credentials authentication with automatic token refresh
- Idempotency-Key support (header-based, Stripe convention)
- Environment variable or programmatic configuration
- Thread-safe client, designed for singleton usage
- Java 8+ compatible

## Table of Contents

- [Installation](#installation)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Usage](#usage)
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
```

## Configuration

The SDK can be configured via environment variables or programmatically through the builder.

| Environment Variable | Property Key | Required | Default |
|---|---|---|---|
| `LOGA_SMS_CLIENT_ID` | `loga.sms.client-id` | Yes (for OAuth) | — |
| `LOGA_SMS_CLIENT_SECRET` | `loga.sms.client-secret` | Yes (for OAuth) | — |
| `LOGA_SMS_API_KEY` | `loga.sms.api-key` | Yes | — |
| `LOGA_SMS_BASE_URL` | `loga.sms.api-base-url` | No | `https://api.sms.loga-apps.com` |
| `LOGA_SMS_DEFAULT_SENDER_NAME` | `loga.sms.default-sender-name` | No | — |
| `LOGA_SMS_DEFAULT_CALLBACK_URL` | `loga.sms.callback-url` | No | — |
| `LOGA_SMS_TOKEN_URL` | `loga.sms.token-url` | No | `{baseUrl}/oauth/v1/token` |

## Usage

### Creating a Client

The simplest way is to use the factory method — it reads all configuration from environment variables:

```java
LogaSmsClient client = LogaSmsClient.create();
```

For full programmatic control, use the builder:

```java
LogaSmsClient client = LogaSmsClient.builder()
    .clientId("your-client-id")
    .clientSecret("your-client-secret")
    .apiKey("your-api-key")
    .baseUrl("https://api.sms.loga-apps.com")
    .build();
```

### Sending SMS

**With defaults** (sender name and callback URL configured via environment variables):

```java
SMSSendResponse response = client.send("+22370000000", "Hello!");
```

**Custom sender name:**

```java
SMSSendResponse response = client.sendWithSenderName("+22370000000", "Hello!", "MyApp");
```

**Custom callback URL:**

```java
SMSSendResponse response = client.sendWithCallback("+22370000000", "Hello!", "https://myapp.com/webhook");
```

**Full control** (sender, callback, and priority):

```java
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
SmsStatusResponse status = client.status("external-ref-no");
System.out.println("Status: " + status.getStatus());
```

### Error Handling

All SDK exceptions are wrapped in a `LogaSmsException`:

```java
try {
    SMSSendResponse response = client.send("+22370000000", "Hello!");
} catch (LogaSmsException e) {
    System.err.println("Error: " + e.getMessage());
    System.err.println("HTTP Status: " + e.getStatusCode());
}
```

### Advanced Builder Reference

| Method | Description |
|---|---|
| `.clientId(String)` | OAuth2 client ID |
| `.clientSecret(String)` | OAuth2 client secret |
| `.apiKey(String)` | API key for authentication |
| `.baseUrl(String)` | Base URL for the SMS API |
| `.tokenUrl(String)` | OAuth2 token endpoint URL |
| `.defaultSenderName(String)` | Default sender name for messages |
| `.callbackUrl(String)` | Default callback URL for delivery receipts |
| `.connectTimeout(Duration)` | HTTP connect timeout |
| `.readTimeout(Duration)` | HTTP read timeout |

## Examples

A complete Spring Boot integration example is available under [`examples/spring-boot-app/`](examples/spring-boot-app/).

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## License

MIT © Loga Engineering — see [LICENSE](LICENSE) for details.
