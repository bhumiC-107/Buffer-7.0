package com.project.model;


public class Warehouse {

    private int warehouseId;
    private String name;
    private double latitude;
    private double longitude;
    private int capacity;
    private double costPerKm;
    private String status;


    public Warehouse() {}

    public Warehouse(int warehouseId, String name, double latitude,
                     double longitude, int capacity, double costPerKm, String status) {
        this.warehouseId = warehouseId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.capacity = capacity;
        this.costPerKm = costPerKm;
        this.status = status;
    }


    public int getWarehouseId() { return warehouseId; }
    public void setWarehouseId(int warehouseId) { this.warehouseId = warehouseId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public double getCostPerKm() { return costPerKm; }
    public void setCostPerKm(double costPerKm) { this.costPerKm = costPerKm; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("Warehouse[%d] %s (%.4f, %.4f) cap=%d cost/km=%.2f [%s]",
                warehouseId, name, latitude, longitude, capacity, costPerKm, status);
    }
}
