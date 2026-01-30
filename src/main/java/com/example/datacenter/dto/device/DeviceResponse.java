package com.example.datacenter.dto.device;

import com.example.datacenter.model.Device;

public record DeviceResponse(
        String serialNumber,
        String name,
        String description,
        int size,
        int powerUsage,
        String rackSerial
) {

    public static DeviceResponse from(Device d) {
        return new DeviceResponse(
                d.getSerialNumber(),
                d.getName(),
                d.getDescription(),
                d.getSize(),
                d.getPowerUsage(),
                d.getRack() != null ? d.getRack().getSerialNumber() : null
        );
    }
}
