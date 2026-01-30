package com.example.datacenter.util.distribution;

public record DeviceSpec(String serial, int size, int power) {
        public DeviceSpec {
            if (size <= 0) throw new IllegalArgumentException("size must be > 0");
            if (power <= 0) throw new IllegalArgumentException("power must be > 0");
        }
    }