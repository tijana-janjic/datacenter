package com.example.datacenter.error;

public class InvalidInputError extends RuntimeException {
    public InvalidInputError(String message) {
        super(message);
    }
}
