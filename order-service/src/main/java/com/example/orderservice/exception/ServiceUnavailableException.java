package com.example.orderservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a downstream microservice cannot be reached or times out.
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ServiceUnavailableException extends RuntimeException {

    private final String targetService;

    public ServiceUnavailableException(String targetService, String message) {
        super(String.format("[%s] %s", targetService, message));
        this.targetService = targetService;
    }

    public ServiceUnavailableException(String targetService, String message, Throwable cause) {
        super(String.format("[%s] %s", targetService, message), cause);
        this.targetService = targetService;
    }

    public String getTargetService() {
        return targetService;
    }
}
