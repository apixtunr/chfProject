package com.lacasadelchef.erp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Security security, Cors cors) {

    public record Security(Jwt jwt, int maxIntentosAcceso) {

        public record Jwt(String secret, long expirationMinutes) {
        }
    }

    public record Cors(List<String> allowedOrigins) {
    }
}
