package com.example.datacenter.store;

import com.example.datacenter.model.Device;
import com.example.datacenter.model.Rack;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryStore {
    public final Map<String, Device> devices = new ConcurrentHashMap<>();
    public final Map<String, Rack> racks = new ConcurrentHashMap<>();
    public final Map<String, String> placement = new ConcurrentHashMap<>();
}
