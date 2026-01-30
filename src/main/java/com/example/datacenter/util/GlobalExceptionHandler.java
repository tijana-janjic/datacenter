package com.example.datacenter.util;

import com.example.datacenter.error.ConflictException;
import com.example.datacenter.error.InvalidInputError;
import com.example.datacenter.error.NotEnoughSpaceException;
import com.example.datacenter.error.NotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ApiError(String message, Instant timestamp) {}

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> notFound(NotFoundException e) {
        return ResponseEntity.status(404).body(new ApiError(e.getMessage(), Instant.now()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> conflict(ConflictException e) {
        return ResponseEntity.status(409).body(new ApiError(e.getMessage(), Instant.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> badRequest(MethodArgumentNotValidException e) {
        return ResponseEntity.status(400).body(new ApiError("Validation failed", Instant.now()));
    }

    @ExceptionHandler(InvalidInputError.class)
    public ResponseEntity<ApiError> invalidInput(InvalidInputError e) {
        return ResponseEntity.status(400).body(new ApiError(e.getMessage(), Instant.now()));
    }

    @ExceptionHandler(NotEnoughSpaceException.class)
    public ResponseEntity<ApiError> handleNotEnoughSpace(NotEnoughSpaceException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(new ApiError(ex.getMessage(), Instant.now()));
    }
}
