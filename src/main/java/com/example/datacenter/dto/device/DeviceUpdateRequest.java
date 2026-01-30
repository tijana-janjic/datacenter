package com.example.datacenter.dto.device;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record DeviceUpdateRequest(
        @NotBlank String name,
        String description,
        @Min(1) int size,
        @Min(1) int powerUsage
) {}
