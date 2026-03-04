package com.filadelfia.store.filadelfiastore.config;

import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration class for Stripe payment gateway integration
 */
@Configuration
@Slf4j
@ConditionalOnProperty(name = "stripe.enabled", havingValue = "true", matchIfMissing = true)
public class StripeConfig {

    @Value("${stripe.secret.key:}")
    private String secretKey;

    @PostConstruct
    public void init() {
        if (secretKey != null && !secretKey.trim().isEmpty() && !secretKey.equals("sk_test_your_secret_key_here")) {
            Stripe.apiKey = secretKey;
            log.info("Stripe API key configured successfully");
        } else {
            log.warn("Stripe API key not configured - Stripe functionality will be disabled");
        }
    }
}
