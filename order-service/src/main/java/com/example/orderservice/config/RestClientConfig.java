package com.example.orderservice.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Client-Side Load Balancing Configuration.
 *
 * Annotating RestTemplate with @LoadBalanced attaches Spring Cloud LoadBalancer's
 * LoadBalancerInterceptor. This interceptor intercepts requests made to logical service
 * names (e.g. "http://user-service/..."), queries the Eureka registry cache, selects
 * a healthy target instance, and replaces the logical hostname with the actual IP:Port.
 */
@Configuration
public class RestClientConfig {

    /**
     * Synchronous @LoadBalanced RestTemplate with strict timeout safeguards.
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(3))  // Max 3s to establish TCP handshake with user-service
            .setReadTimeout(Duration.ofSeconds(5))     // Max 5s to wait for HTTP response packets
            .build();
    }
}
