package com.example.datacenter.service;

import com.example.datacenter.dto.rack.RackCreateRequest;
import com.example.datacenter.dto.rack.RackResponse;
import com.example.datacenter.dto.rack.RackUpdateRequest;
import com.example.datacenter.error.ConflictException;
import com.example.datacenter.error.InvalidInputError;
import com.example.datacenter.error.NotFoundException;
import com.example.datacenter.model.Device;
import com.example.datacenter.model.Rack;
import com.example.datacenter.repository.RackRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RackService {

    private final RackRepository repository;

    public RackService(RackRepository repository) {
        this.repository = repository;
    }

    // CREATE
    public RackResponse create(RackCreateRequest request) {

        if (repository.exists(request.serialNumber())) {
            throw new ConflictException("Rack already exists: " + request.serialNumber());
        }

        Rack rack = new Rack(
                request.serialNumber(),
                request.name(),
                request.description(),
                request.spaceSize(),
                request.maxPowerUsage()
        );

        repository.save(rack);
        return RackResponse.from(rack);
    }

    // READ one
    public RackResponse get(String serialNumber) {
        Rack rack = getRack(serialNumber);
        return RackResponse.from(rack);
    }

    private Rack getRack(String serialNumber) {
        return repository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new NotFoundException("Rack not found: " + serialNumber));
    }

    // READ all
    public List<RackResponse> list() {
        return repository.findAll().stream()
                .map(RackResponse::from)
                .toList();
    }

    // UPDATE
    public RackResponse update(String serialNumber, @Valid RackUpdateRequest request) {

        Rack rack = getRack(serialNumber);

        int newMaxPower = request.maxPowerUsage();
        int newSpaceSize = request.spaceSize();

        if (rack.getCurrentPowerUsage() > newMaxPower) {
            throw new InvalidInputError("Power capacity is not sufficient. \n" +
                    "Current power usage: " + rack.getCurrentPowerUsage() +
                    ", new power capacity: " + newMaxPower);
        }
        if (rack.getUsedSpaceSize() > newSpaceSize) {
            throw new InvalidInputError("Space capacity is not sufficient. \n" +
                    "Currently used space size: " + rack.getUsedSpaceSize() +
                    ", new space capacity: " + newSpaceSize);
        }

        rack.setName(request.name());
        rack.setDescription(request.description());
        rack.setSpaceSize(newSpaceSize);
        rack.setMaxPowerUsage(newMaxPower);

        repository.save(rack);

        return RackResponse.from(rack);
    }


    // DELETE
    public void delete(String serialNumber) throws NotFoundException {
        if (!repository.exists(serialNumber)) {
            throw new NotFoundException("Rack not found: " + serialNumber);
        }
        Rack rack = getRack(serialNumber);
        new java.util.HashSet<>(rack.getDevices()).forEach(device -> removeDevice(rack, device));
        repository.delete(serialNumber);
    }

    protected void addDevice(Rack rack, Device device) {
        rack.addDevice(device);
    }

    protected void removeDevice(Rack rack, Device device) {
        rack.removeDevice(device);
    }

}