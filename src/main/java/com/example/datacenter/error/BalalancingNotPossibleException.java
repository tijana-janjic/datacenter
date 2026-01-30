package com.example.datacenter.error;

import jakarta.validation.constraints.NotBlank;

public class BalalancingNotPossibleException extends RuntimeException {
    public BalalancingNotPossibleException(@NotBlank String message) {
        super(message);
    }
}
