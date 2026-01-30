package com.example.datacenter.api;

import com.example.datacenter.dto.device.DeviceCreateRequest;
import com.example.datacenter.dto.device.DeviceResponse;
import com.example.datacenter.dto.device.DeviceUpdateRequest;
import com.example.datacenter.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/devices", produces = MediaType.APPLICATION_JSON_VALUE)
public class DeviceController {

    private final DeviceService service;

    public DeviceController(DeviceService service) {
        this.service = service;
    }

    // Create
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public DeviceResponse add(@Valid @RequestBody DeviceCreateRequest request) {
        return service.add(request);
    }


    @PostMapping(value = "/rack/{rackSerial}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public DeviceResponse addToRack(@PathVariable String rackSerial, @Valid @RequestBody DeviceCreateRequest request) {
        return service.addToRack(rackSerial, request);
    }

    // Read
    @GetMapping("/{serialNumber}")
    public DeviceResponse get(@PathVariable String serialNumber) {
        return service.get(serialNumber);
    }

    @GetMapping
    public List<DeviceResponse> list() {
        return service.list();
    }

    // Update
    @PutMapping(value = "/{serialNumber}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public DeviceResponse update(@PathVariable String serialNumber, @Valid @RequestBody DeviceUpdateRequest request) {
        return service.update(serialNumber, request);
    }

    // Delete
    @DeleteMapping("/{serialNumber}")
    public void delete(@PathVariable String serialNumber) {
        service.delete(serialNumber);
    }

}
