package com.example.datacenter.api;

import com.example.datacenter.dto.rack.RackCreateRequest;
import com.example.datacenter.dto.rack.RackResponse;
import com.example.datacenter.dto.rack.RackUpdateRequest;
import com.example.datacenter.service.RackService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/racks")
public class RackController {

    private final RackService service;

    public RackController(RackService service) {
        this.service = service;
    }

    @PostMapping
    public RackResponse create(@Valid @RequestBody RackCreateRequest request) {
        return service.create(request);
    }

    @GetMapping("/{serialNumber}")
    public RackResponse get(@PathVariable String serialNumber) {
        return service.get(serialNumber);
    }

    @GetMapping
    public List<RackResponse> list() {
        return service.list();
    }

    @PutMapping("/{serialNumber}")
    public RackResponse update(@PathVariable String serialNumber,
                               @Valid @RequestBody RackUpdateRequest request) {
        return service.update(serialNumber, request);
    }

    @DeleteMapping("/{serialNumber}")
    public void delete(@PathVariable String serialNumber) {
        service.delete(serialNumber);
    }
}
