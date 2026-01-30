package com.example.datacenter.model;

public class Device implements Comparable<Device> {

    // Id
    private final String serialNumber;

    private String name;
    private String description;

    // Size (in units)
    private int size;

    // Power (in Watts)
    private int powerUsage;

    private Rack rack;

    public Device(String serialNumber, String name, String description, int size, int powerUsage, Rack rack) {
        this.serialNumber = serialNumber;
        this.name = name;
        this.description = description;
        this.size = size;
        this.powerUsage = powerUsage;
        this.rack = rack;
    }

    public Device(String serialNumber, String name, String description, int size, int powerUsage) {
        this(serialNumber, name, description, size, powerUsage, null);
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

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getPowerUsage() {
        return powerUsage;
    }

    public void setPowerUsage(int powerUsage) {
        this.powerUsage = powerUsage;
    }

    public Rack getRack() {
        return rack;
    }

    public void setRack(Rack rack) {
        this.rack = rack;
    }

    @Override
    public int compareTo(Device other) {
        return other.powerUsage - this.powerUsage;
    }
}
