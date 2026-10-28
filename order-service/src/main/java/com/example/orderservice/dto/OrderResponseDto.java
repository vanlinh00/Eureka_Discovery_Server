package com.example.orderservice.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Aggregated Order DTO containing order metadata and fetched User details.
 */
public record OrderResponseDto(
    String orderId,
    String productName,
    int quantity,
    BigDecimal totalPrice,
    String status,
    Instant createdAt,
    UserResponseDto user
) {}
