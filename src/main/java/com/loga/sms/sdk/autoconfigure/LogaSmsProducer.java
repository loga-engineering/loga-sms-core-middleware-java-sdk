package com.loga.sms.sdk.autoconfigure;

import com.loga.sms.sdk.LogaSmsClient;
import com.loga.sms.sdk.config.LogaSmsProperties;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 * CDI producer for Quarkus and other CDI containers.
 * <p>
 * Automatically creates a {@link LogaSmsClient} bean from the configured
 * {@link LogaSmsProperties} loaded from the classpath.
 * <p>
 * In Quarkus, this producer is discovered automatically. In other CDI
 * containers, ensure this package is scanned.
 */
@ApplicationScoped
public class LogaSmsProducer {

    @Produces
    @ApplicationScoped
    public LogaSmsClient logaSmsClient() {
        return LogaSmsClient.create();
    }
}
