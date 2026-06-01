package com.energiefixers.backend.shared;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException ex) {
        log.warn("Not found: {}", ex.getMessage());
        return ResponseEntity.status(404).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage());
        return ResponseEntity.status(400).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        String message = "Invalid data: ";
        
        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            String cause = ex.getCause().getMessage();
            if (cause.contains("uk_energy_reading_property_period")) {
                message += "A reading already exists for this period on this property.";
            } else if (cause.contains("unique")) {
                message += "A record with this data already exists.";
            } else {
                message += "The provided data violates database constraints.";
            }
        } else {
            message += "The provided data violates database constraints.";
        }
        
        return ResponseEntity.status(409).body(ApiResponse.error(message));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatus(ResponseStatusException ex) {
        log.warn("Response status exception: {} - {}", ex.getStatusCode(), ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).body(ApiResponse.error(ex.getReason()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        String message = "Validation failed: " + 
            ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + " - " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Invalid input");
        return ResponseEntity.status(400).body(ApiResponse.error(message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(500).body(ApiResponse.error("Internal server error"));
    }
}
