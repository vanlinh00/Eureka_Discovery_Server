package com.example.userservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * REST Controller providing service health/info endpoints and sample user operations
 * to verify Eureka Client registration and connectivity.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private String serverPort;

    private final DiscoveryClient discoveryClient;

    public UserController(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    /**
     * Connectivity and Health/Info endpoint returning JSON.
     * Used by developers and monitoring systems to verify that the microservice
     * is up and connected to the Eureka registry.
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getServiceInfo() {
        Map<String, Object> info = Map.of(
            "service", applicationName,
            "status", "UP",
            "port", serverPort,
            "timestamp", Instant.now().toString(),
            "eurekaRegistry", Map.of(
                "registeredServices", discoveryClient.getServices(),
                "instancesForSelf", discoveryClient.getInstances(applicationName).size(),
                "description", "Netflix Eureka Client successfully connected"
            )
        );

        return ResponseEntity.ok(info);
    }

    /**
     * Sample endpoint returning mock users to verify JSON REST payload delivery.
     */
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = List.of(
            new UserDto(1L, "linh.nguyen", "linh.nguyen@example.com", "ENGINEERING"),
            new UserDto(2L, "sarah.connor", "sarah.c@example.com", "OPERATIONS"),
            new UserDto(3L, "alex.chen", "alex.chen@example.com", "ARCHITECTURE")
        );
        return ResponseEntity.ok(users);
    }

    /**
     * Sample endpoint returning a single user by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(
            new UserDto(id, "user_" + id, "user" + id + "@example.com", "ACTIVE")
        );
    }

    /**
     * Immutable User Data Transfer Object (Java 17+ Record).
     */
    public record UserDto(Long id, String username, String email, String department) {}
}
