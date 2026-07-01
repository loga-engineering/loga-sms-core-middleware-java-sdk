package com.loga.sms.sdk.autoconfigure;

import com.loga.sms.sdk.LogaSmsClient;
import com.loga.sms.sdk.config.LogaSmsProperties;
import com.loga.sms.sdk.http.LogaHttpClient;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(LogaSmsClient.class)
@EnableConfigurationProperties(LogaSmsProperties.class)
public class LogaSmsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LogaSmsClient logaSmsClient(LogaSmsProperties properties) {
        return LogaSmsClient.create(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public LogaHttpClient logaHttpClient() {
        return new LogaHttpClient();
    }
}
