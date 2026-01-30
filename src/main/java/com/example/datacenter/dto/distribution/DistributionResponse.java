package com.example.datacenter.dto.distribution;

import java.util.List;

public record DistributionResponse(
        double utilRange,
        List<DevicePlacement> placements,
        List<RackDistribution> racks
) {}
