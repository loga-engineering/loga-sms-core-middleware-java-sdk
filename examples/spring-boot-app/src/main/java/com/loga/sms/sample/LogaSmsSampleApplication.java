package com.loga.sms.sample;

import com.loga.sms.sdk.LogaSmsClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LogaSmsSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogaSmsSampleApplication.class, args);
    }

    @Bean
    public LogaSmsClient logaSmsClient() {
        return LogaSmsClient.create();
    }
}
