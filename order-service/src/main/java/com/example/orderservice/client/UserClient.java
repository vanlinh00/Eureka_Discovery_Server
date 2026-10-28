package com.example.orderservice.client;

import com.example.orderservice.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Declarative Feign Client for consuming user-service.
 *
 * - name = "user-service" resolves dynamically via Netflix Eureka Discovery.
 * - Spring Cloud LoadBalancer distributes traffic evenly across available user-service instances.
 * - No IP addresses or ports are hardcoded.
 */
@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/users/{id}")
    UserResponseDto getUserById(@PathVariable("id") Long id);

    @GetMapping("/api/users")
    List<UserResponseDto> getAllUsers();
}
