package com.drinkhere.infrapass.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
@Getter @Setter
@ConfigurationProperties(prefix = "nice-api")
public class NiceApiProperties {
    private String organizationToken;
    private String clientId;
    private String clientSecret;
    private String productId;
}
