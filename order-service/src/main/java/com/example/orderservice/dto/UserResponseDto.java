package com.example.orderservice.dto;

/**
 * DTO matching the user-service REST response payload.
 */
public record UserResponseDto(
    Long id,
    String username,
    String email,
    String department
) {}
