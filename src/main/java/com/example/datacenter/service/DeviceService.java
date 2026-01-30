package com.example.datacenter.service;

import com.example.datacenter.dto.device.DeviceCreateRequest;
import com.example.datacenter.dto.device.DeviceResponse;
import com.example.datacenter.dto.device.DeviceUpdateRequest;
import com.example.datacenter.error.NotFoundException;
import com.example.datacenter.model.Device;
import com.example.datacenter.model.Rack;
import com.example.datacenter.repository.DeviceRepository;
import com.example.datacenter.repository.RackRepository;
import org.springframework.stereotype.Service;

import com.example.datacenter.error.NotEnoughSpaceException;

import java.util.List;
import java.util.Objects;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepo;
    private final RackRepository rackRepo;

    public DeviceService(DeviceRepository deviceRepo, RackRepository rackRepo) {
        this.deviceRepo = deviceRepo;
        this.rackRepo = rackRepo;
    }

    // ===== READ =====

    public DeviceResponse get(String serialNumber) {
        Device d = deviceRepo.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new NotFoundException("Device not found: " + serialNumber));
        return DeviceResponse.from(d);
    }

    public List<DeviceResponse> list() {
        return deviceRepo.findAll().stream()
                .map(DeviceResponse::from)
                .toList();
    }

    // ===== INSERT =====

    public DeviceResponse add(DeviceCreateRequest request) {
        Device device = new Device(
                request.serialNumber(),
                request.name(),
                request.description(),
                request.size(),
                request.powerUsage()
        );

        Rack rack;
        String rackSN = request.rackSerialNumber();
        
        if (rackSN != null && !rackSN.isBlank()) {
            rack = rackRepo.findBySerialNumber(rackSN)
                    .orElseThrow(() -> new NotFoundException("Rack not found: " + rackSN));
            if (!rack.canFit(device)) {
                throw new NotEnoughSpaceException("Device cannot fit in rack: " + rackSN);
            }
        } else {
            rack = findBestRack(device);
        }

        rack.addDevice(device);
        deviceRepo.save(device);
        return DeviceResponse.from(device);
    }

    public DeviceResponse addToRack(String rackSerialNumber, DeviceCreateRequest request) {
        Device device = new Device(
                request.serialNumber(),
                request.name(),
                request.description(),
                request.size(),
                request.powerUsage()
        );
        
        Rack rack = rackRepo.findBySerialNumber(rackSerialNumber)
                .orElseThrow(() -> new NotFoundException("Rack not found: " + rackSerialNumber));
        
        if (!rack.canFit(device)) {
            throw new NotEnoughSpaceException("Device cannot fit in rack: " + rackSerialNumber);
        }
        
        rack.addDevice(device);
        deviceRepo.save(device);
        return DeviceResponse.from(device);
    }

    private Rack findBestRack(Device device) {
        List<Rack> allRacks = rackRepo.findAll();
        
        Rack best = null;
        double bestProjected = Double.POSITIVE_INFINITY;
        int bestFreeUnits = -1;

        for (Rack r : allRacks) {
            if (!r.canFit(device)) continue;

            double projected = r.getProjectedPowerUtilization(device);
            int freeUnits = r.getAvailableUnits();

            if (projected < bestProjected || (projected == bestProjected && freeUnits > bestFreeUnits)) {
                best = r;
                bestProjected = projected;
                bestFreeUnits = freeUnits;
            }
        }

        if (best == null) {
            throw new NotEnoughSpaceException("No rack available for device: " + device.getSerialNumber());
        }

        return best;
    }


    // ===== UPDATE =====

    public DeviceResponse update(String serialNumber, DeviceUpdateRequest request) {
        Objects.requireNonNull(request, "request");

        Device d = deviceRepo.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new NotFoundException("Device not found: " + serialNumber));

        d.setName(request.name());
        d.setDescription(request.description());

        d.setSize(request.size());
        d.setPowerUsage(request.powerUsage());

        deviceRepo.save(d);
        return DeviceResponse.from(d);
    }

    // ===== DELETE =====

    public void delete(String serialNumber) {
        Device d = deviceRepo.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new NotFoundException("Device not found: " + serialNumber));

        Rack rack = d.getRack();
        if (rack != null) {
            removeDeviceFromRack(d, rack);
            rackRepo.save(rack);
        }

        deviceRepo.delete(serialNumber);
    }

    private void removeDeviceFromRack(Device device, Rack rack) {
        device.setRack(null);
        rack.removeDevice(device);
    }

}
