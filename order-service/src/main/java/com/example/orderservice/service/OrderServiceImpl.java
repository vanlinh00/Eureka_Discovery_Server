package com.example.orderservice.service;

import com.example.orderservice.client.UserClient;
import com.example.orderservice.dto.OrderResponseDto;
import com.example.orderservice.dto.UserResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Service implementation demonstrating discovery-based inter-service calls.
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final UserClient userClient;

    public OrderServiceImpl(UserClient userClient) {
        this.userClient = userClient;
    }

    @Override
    public OrderResponseDto getOrderWithUserDetails(String orderId, Long userId) {
        log.info("Fetching user details for userId: {} via OpenFeign Eureka Client", userId);

        // Discovers user-service from Eureka and performs load-balanced HTTP GET request
        UserResponseDto user = userClient.getUserById(userId);

        return new OrderResponseDto(
            orderId,
            "MacBook Pro 16-inch M3 Max",
            1,
            new BigDecimal("3499.00"),
            "CONFIRMED",
            Instant.now(),
            user
        );
    }

    @Override
    public OrderResponseDto createOrder(Long userId, String productName, int quantity, BigDecimal pricePerUnit) {
        log.info("Validating user existence for userId: {} prior to placing order", userId);

        // Inter-service call through OpenFeign
        UserResponseDto user = userClient.getUserById(userId);

        BigDecimal total = pricePerUnit.multiply(BigDecimal.valueOf(quantity));
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return new OrderResponseDto(
            orderId,
            productName,
            quantity,
            total,
            "PROCESSING",
            Instant.now(),
            user
        );
    }
}
