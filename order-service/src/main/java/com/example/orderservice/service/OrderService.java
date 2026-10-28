package com.example.orderservice.service;

import com.example.orderservice.dto.OrderResponseDto;

import java.math.BigDecimal;

public interface OrderService {
    OrderResponseDto getOrderWithUserDetails(String orderId, Long userId);
    OrderResponseDto createOrder(Long userId, String productName, int quantity, BigDecimal pricePerUnit);
}
