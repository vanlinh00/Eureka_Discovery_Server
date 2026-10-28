package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderResponseDto;
import com.example.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Order operations.
 * Demonstrates dynamic ephemeral port detection (server.port: 0) and service discovery.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Value("${spring.application.name}")
    private String applicationName;

    private final OrderService orderService;
    private final Environment environment;

    public OrderController(OrderService orderService, Environment environment) {
        this.orderService = orderService;
        this.environment = environment;
    }

    /**
     * Health and dynamic port inspection endpoint.
     * Displays the actual runtime port allocated by the operating system kernel.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        String activePort = environment.getProperty("local.server.port");

        return ResponseEntity.ok(Map.of(
            "service", applicationName,
            "status", "UP",
            "allocatedPort", activePort != null ? activePort : "unknown",
            "timestamp", Instant.now().toString()
        ));
    }

    /**
     * Sample REST endpoint returning order details annotated with the serving port.
     */
    @GetMapping
    public ResponseEntity<List<OrderSummary>> getOrders() {
        String activePort = environment.getProperty("local.server.port");

        return ResponseEntity.ok(List.of(
            new OrderSummary("ORD-501", "MacBook Pro M3", 1, new BigDecimal("3499.00"), activePort),
            new OrderSummary("ORD-502", "Dell UltraSharp 4K", 2, new BigDecimal("1598.00"), activePort)
        ));
    }

    /**
     * GET endpoint: Retrieves order details and dynamically enriches with User details
     * fetched from user-service via OpenFeign Eureka client.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrder(
            @PathVariable String orderId,
            @RequestParam(defaultValue = "1") Long userId) {
        OrderResponseDto response = orderService.getOrderWithUserDetails(orderId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST endpoint: Creates an order after validating the user via UserClient.
     */
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody CreateOrderRequest request) {
        OrderResponseDto response = orderService.createOrder(
            request.userId(),
            request.productName(),
            request.quantity(),
            request.pricePerUnit()
        );
        return ResponseEntity.ok(response);
    }

    public record OrderSummary(
        String orderId,
        String product,
        int quantity,
        BigDecimal total,
        String servedByPort
    ) {}

    public record CreateOrderRequest(
        Long userId,
        String productName,
        int quantity,
        BigDecimal pricePerUnit
    ) {}
}
