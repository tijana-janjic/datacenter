package com.example.datacenter.util.distribution;

public record RackSpec(String serialNumber, int spaceSize, int maxPower) {
        public RackSpec {
            if (spaceSize <= 0) throw new IllegalArgumentException("spaceSize must be > 0");
            if (maxPower <= 0) throw new IllegalArgumentException("maxPower must be > 0");
        }
    }