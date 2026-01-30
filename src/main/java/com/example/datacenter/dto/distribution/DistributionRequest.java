package com.example.datacenter.dto.distribution;

import com.example.datacenter.dto.device.DeviceCreateRequest;
import com.example.datacenter.dto.rack.RackCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record DistributionRequest(
        @NotEmpty List<@Valid RackCreateRequest> racks,
        @NotEmpty List<@Valid DeviceCreateRequest> devices
) {}
