package com.example.datacenter.dto.distribution;

import java.util.List;

public record RackDistribution(
        String rackSerialNumber,
        int usedSpace,
        int spaceSize,
        int usedPower,
        int maxPowerUsage,
        double powerUtilization, // percentage 0-100
        List<String> deviceSerialNumbers
) {}
