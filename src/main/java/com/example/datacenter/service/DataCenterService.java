package com.example.datacenter.service;

import com.example.datacenter.dto.device.DeviceCreateRequest;
import com.example.datacenter.dto.distribution.DevicePlacement;
import com.example.datacenter.dto.distribution.DistributionRequest;
import com.example.datacenter.dto.distribution.DistributionResponse;
import com.example.datacenter.dto.distribution.RackDistribution;
import com.example.datacenter.util.distribution.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataCenterService {

    private final PlacementPipeline pipeline = new PlacementPipeline();

    public DistributionResponse generateBalancedDistribution(@Valid DistributionRequest request) {
        List<RackSpec> racks = request.racks().stream()
                .map(r -> new RackSpec(r.serialNumber(), r.spaceSize(), r.maxPowerUsage()))
                .toList();

        List<DeviceSpec> devices = request.devices().stream()
                .map(d -> new DeviceSpec(d.serialNumber(), d.size(), d.powerUsage()))
                .toList();

        Assignment best = pipeline.plan(devices, racks);

        return buildResponse(request, best);
    }

    public DistributionResponse rebalance(@Valid DistributionRequest request) {
        List<RackSpec> racks = request.racks().stream()
                .map(r -> new RackSpec(r.serialNumber(), r.spaceSize(), r.maxPowerUsage()))
                .toList();

        List<DeviceSpec> devices = request.devices().stream()
                .map(d -> new DeviceSpec(d.serialNumber(), d.size(), d.powerUsage()))
                .toList();

        Assignment feasible = pipeline.rebalanceOnly(devices, racks);
        return buildResponse(request, feasible);
    }

    private DistributionResponse buildResponse(DistributionRequest request, Assignment assignment) {
        List<DevicePlacement> placements = assignment.deviceToRack().entrySet().stream()
                .map(e -> new DevicePlacement(e.getKey(), e.getValue()))
                .toList();

        Map<String, List<DeviceCreateRequest>> devicesByRack = new HashMap<>();
        for (DeviceCreateRequest d : request.devices()) {
            String rackSerial = assignment.deviceToRack().get(d.serialNumber());
            devicesByRack.computeIfAbsent(rackSerial, k -> new ArrayList<>()).add(d);
        }

        List<RackDistribution> rackDistributions = request.racks().stream()
                .map(r -> {
                    List<DeviceCreateRequest> ds = devicesByRack.getOrDefault(r.serialNumber(), List.of());

                    int usedSpace = ds.stream().mapToInt(DeviceCreateRequest::size).sum();
                    int usedPower = ds.stream().mapToInt(DeviceCreateRequest::powerUsage).sum();
                    double util = (usedPower / (double) r.maxPowerUsage()) * 100.0;

                    List<String> devSerials = ds.stream().map(DeviceCreateRequest::serialNumber).toList();

                    return new RackDistribution(
                            r.serialNumber(),
                            usedSpace,
                            r.spaceSize(),
                            usedPower,
                            r.maxPowerUsage(),
                            util,
                            devSerials
                    );
                })
                .toList();

        return new DistributionResponse(assignment.score(), placements, rackDistributions);
    }
}
