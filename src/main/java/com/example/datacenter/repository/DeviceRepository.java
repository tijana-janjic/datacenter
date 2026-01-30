package com.example.datacenter.repository;

import com.example.datacenter.model.Device;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class DeviceRepository {

    private final Map<String, Device> bySerial = new HashMap<>();

    public boolean exists(String serialNumber) {
        return bySerial.containsKey(serialNumber);
    }

    public Optional<Device> findBySerialNumber(String serialNumber) {
        return Optional.ofNullable(bySerial.get(serialNumber));
    }

    public List<Device> findAll() {
        return new ArrayList<>(bySerial.values());
    }

    public Device save(Device d) {
        bySerial.put(d.getSerialNumber(), d);
        return d;
    }

    public void delete(String serialNumber) {
        bySerial.remove(serialNumber);
    }
}
