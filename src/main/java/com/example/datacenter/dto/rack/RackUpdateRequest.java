package com.example.datacenter.dto.rack;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record RackUpdateRequest(
        @NotBlank String name,
        String description,
        @Min(1) int spaceSize,
        @Min(1) int maxPowerUsage
) {}
