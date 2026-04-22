package com.project.model;


public class Assignment {

    private int assignmentId;
    private int warehouseId;
    private int customerId;
    private String warehouseName;
    private String customerName;
    private double distanceKm;
    private double deliveryCost;
    private double estimatedTimeMinutes;

    // ── Constructors ──────────────────────────────────────────

    public Assignment() {}

    public Assignment(int warehouseId, int customerId, String warehouseName,
                      String customerName, double distanceKm, double deliveryCost,
                      double estimatedTimeMinutes) {
        this.warehouseId = warehouseId;
        this.customerId = customerId;
        this.warehouseName = warehouseName;
        this.customerName = customerName;
        this.distanceKm = distanceKm;
        this.deliveryCost = deliveryCost;
        this.estimatedTimeMinutes = estimatedTimeMinutes;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public int getAssignmentId() { return assignmentId; }
    public void setAssignmentId(int assignmentId) { this.assignmentId = assignmentId; }

    public int getWarehouseId() { return warehouseId; }
    public void setWarehouseId(int warehouseId) { this.warehouseId = warehouseId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public double getDeliveryCost() { return deliveryCost; }
    public void setDeliveryCost(double deliveryCost) { this.deliveryCost = deliveryCost; }

    public double getEstimatedTimeMinutes() { return estimatedTimeMinutes; }
    public void setEstimatedTimeMinutes(double estimatedTimeMinutes) { this.estimatedTimeMinutes = estimatedTimeMinutes; }

    @Override
    public String toString() {
        return String.format("Assignment: %s → %s | %.2f km | ₹%.2f | ~%.0f min",
                warehouseName, customerName, distanceKm, deliveryCost, estimatedTimeMinutes);
    }
}
