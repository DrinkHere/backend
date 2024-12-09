package com.drinkhere.infrapass.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("https://svc.niceapi.co.kr")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();
    }
}