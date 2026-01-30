package com.example.datacenter.repository;

import com.example.datacenter.model.Rack;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RackRepository {

    private final Map<String, Rack> store = new HashMap<>();

    public boolean exists(String serialNumber) {
        return store.containsKey(serialNumber);
    }

    public Optional<Rack> findBySerialNumber(String serialNumber) {
        return Optional.ofNullable(store.get(serialNumber));
    }

    public List<Rack> findAll() {
        return new ArrayList<>(store.values());
    }

    public Rack save(Rack rack) {
        store.put(rack.getSerialNumber(), rack);
        return rack;
    }

    public void delete(String serialNumber) {
        store.remove(serialNumber);
    }
}
