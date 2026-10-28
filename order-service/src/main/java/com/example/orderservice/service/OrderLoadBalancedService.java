package com.example.orderservice.service;

import com.example.orderservice.dto.OrderResponseDto;
import com.example.orderservice.dto.UserResponseDto;
import com.example.orderservice.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.UUID;

/**
 * Service demonstrating client-side load balancing via @LoadBalanced RestTemplate.
 * Directly addresses the microservice by its Eureka application name: "user-service".
 */
@Service("orderLoadBalancedService")
public class OrderLoadBalancedService {

    private static final Logger log = LoggerFactory.getLogger(OrderLoadBalancedService.class);
    private static final String USER_SERVICE_BASE_URL = "http://user-service";

    private final RestTemplate loadBalancedRestTemplate;

    @Autowired
    public OrderLoadBalancedService(RestTemplate loadBalancedRestTemplate) {
        this.loadBalancedRestTemplate = loadBalancedRestTemplate;
    }

    /**
     * Fetches user details by invoking http://user-service/api/users/{id}
     * with comprehensive exception handling for timeouts, 5xx server errors, and 404s.
     */
    public UserResponseDto fetchUserById(Long userId) {
        String url = USER_SERVICE_BASE_URL + "/api/users/{id}";
        log.info("Dispatching load-balanced request to logical URL: {} with userId={}", url, userId);

        try {
            ResponseEntity<UserResponseDto> response = loadBalancedRestTemplate.getForEntity(
                url,
                UserResponseDto.class,
                userId
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Successfully received user data for userId={}", userId);
                return response.getBody();
            }

            throw new ServiceUnavailableException("user-service", "Empty response payload received from user-service");

        } catch (ResourceAccessException ex) {
            // Handles Connection Refused, Connect Timeout, or Socket Read Timeout
            if (ex.getCause() instanceof SocketTimeoutException) {
                log.error("Read timeout expired while waiting for user-service response on userId: {}", userId, ex);
                throw new ServiceUnavailableException("user-service", "Request to user-service timed out", ex);
            }
            log.error("Connection failure: unable to reach any healthy instance of user-service in Eureka", ex);
            throw new ServiceUnavailableException("user-service", "user-service is currently unavailable or unreachable", ex);

        } catch (HttpServerErrorException ex) {
            // Handles 500 Internal Server Error, 502 Bad Gateway, 503 Service Unavailable, 504 Gateway Timeout
            log.error("user-service returned HTTP status {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            throw new ServiceUnavailableException("user-service", "user-service encountered an internal error: " + ex.getStatusCode(), ex);

        } catch (HttpClientErrorException.NotFound ex) {
            // 404 Not Found from user-service
            log.warn("User not found in user-service for userId={}", userId);
            return getFallbackUser(userId, "User not found in registry");

        } catch (Exception ex) {
            log.error("Unexpected failure during inter-service call to user-service", ex);
            return getFallbackUser(userId, "Fallback: Downstream unavailable");
        }
    }

    /**
     * Creates an order enriched with user data retrieved via client-side load balancing.
     */
    public OrderResponseDto createOrder(Long userId, String productName, int quantity, BigDecimal pricePerUnit) {
        UserResponseDto user = fetchUserById(userId);

        BigDecimal totalPrice = pricePerUnit.multiply(BigDecimal.valueOf(quantity));
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return new OrderResponseDto(
            orderId,
            productName,
            quantity,
            totalPrice,
            "CONFIRMED",
            Instant.now(),
            user
        );
    }

    /**
     * Graceful fallback pattern when downstream is unavailable or degraded.
     */
    private UserResponseDto getFallbackUser(Long userId, String statusNote) {
        log.info("Applying degraded fallback user response for userId={}", userId);
        return new UserResponseDto(
            userId,
            "guest_user_" + userId,
            "unavailable@domain.local",
            statusNote
        );
    }
}
