package com.example.datacenter.dto.rack;

import com.example.datacenter.dto.device.DeviceResponse;
import com.example.datacenter.model.Rack;

import java.util.Set;
import java.util.stream.Collectors;

public record RackResponse(
        String serialNumber,
        String name,
        String description,

        int spaceSize,
        int usedSpaceSize,

        int maxPowerUsage,
        int currentPowerUsage,

        Set<DeviceResponse> devices,

        double powerUtilizationPercent

) {

    public static RackResponse from(Rack r) {

        double utilization = ((double) r.getCurrentPowerUsage() / r.getMaxPowerUsage()) * 100;

        return new RackResponse(
                r.getSerialNumber(),
                r.getName(),
                r.getDescription(),

                r.getSpaceSize(),
                r.getUsedSpaceSize(),

                r.getMaxPowerUsage(),
                r.getCurrentPowerUsage(),
                r.getDevices()
                 .stream()
                 .map(DeviceResponse::from)
                 .collect(Collectors.toSet()),
                utilization

        );
    }
}
