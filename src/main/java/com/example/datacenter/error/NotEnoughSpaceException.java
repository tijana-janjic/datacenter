package com.example.datacenter.error;

public class NotEnoughSpaceException extends RuntimeException {
    public NotEnoughSpaceException(String message) {
        super(message);
    }
}