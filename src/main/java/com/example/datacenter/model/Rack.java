package com.example.datacenter.model;

import java.util.HashSet;
import java.util.Set;

public class Rack {

    private final String serialNumber;

    private String name;
    private String description;

    // Space capacity (in units)
    private int spaceSize;
    private int usedSpaceSize;

    // Power capacity (in Watts)
    private int maxPowerUsage;
    private int currentPowerUsage;

    private Set<Device> devices;

    public Rack(String serialNumber, String name, String description, int spaceSize, int maxPowerUsage) {
        this.serialNumber = serialNumber;
        this.name = name;
        this.description = description;
        this.spaceSize = spaceSize;
        this.maxPowerUsage = maxPowerUsage;
        usedSpaceSize = 0;
        currentPowerUsage = 0;
        devices = new HashSet<>();
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSpaceSize() {
        return spaceSize;
    }

    public void setSpaceSize(int spaceSize) {
        this.spaceSize = spaceSize;
    }

    public int getUsedSpaceSize() {
        return usedSpaceSize;
    }

    public void setUsedSpaceSize(int usedSpaceSize) {
        this.usedSpaceSize = usedSpaceSize;
    }

    public int getMaxPowerUsage() {
        return maxPowerUsage;
    }

    public void setMaxPowerUsage(int maxPowerUsage) {
        this.maxPowerUsage = maxPowerUsage;
    }

    public int getCurrentPowerUsage() {
        return currentPowerUsage;
    }

    public void setCurrentPowerUsage(int currentPowerUsage) {
        this.currentPowerUsage = currentPowerUsage;
    }

    public Set<Device> getDevices() {
        return devices;
    }

    public void setDevices(Set<Device> devices) {
        this.devices = devices;
    }

    private int getAvailableSpace() {
        return spaceSize - usedSpaceSize;
    }

    private int getAvailablePower() {
        return maxPowerUsage - currentPowerUsage;
    }

    public double getProjectedPowerUtilization(Device d) {
        if (maxPowerUsage <= 0) {
            return Double.MAX_VALUE;
        }
        return (currentPowerUsage + d.getPowerUsage()) / (double) maxPowerUsage;
    }

    public int getAvailableUnits() {
        return spaceSize - usedSpaceSize;
    }

    public void addDevice(Device device) {
        usedSpaceSize += device.getSize();
        currentPowerUsage += device.getPowerUsage();
        devices.add(device);
        device.setRack(this);
    }

    public void removeDevice(Device device) {
        usedSpaceSize -= device.getSize();
        currentPowerUsage -= device.getPowerUsage();
        devices.remove(device);
        device.setRack(null);
    }

    public void clear() {
        currentPowerUsage = 0;
        usedSpaceSize = 0;
        devices = new HashSet<>();
    }

    public boolean canFit(Device device) {
        return getAvailablePower() >= device.getPowerUsage() && getAvailableSpace() >= device.getSize();
    }


}
