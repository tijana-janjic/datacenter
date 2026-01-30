package com.example.datacenter.error;

import jakarta.validation.constraints.NotBlank;

public class ConflictException extends RuntimeException {
    public ConflictException(@NotBlank String message) {
        super(message);
    }
}
