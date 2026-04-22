package com.project.model;


public class Customer {

    private int customerId;
    private String name;
    private double latitude;
    private double longitude;
    private int demand;
    private String priority;   // LOW, MEDIUM, HIGH, URGENT
    private String status;     // PENDING, ASSIGNED, DELIVERED


    public Customer() {}

    public Customer(int customerId, String name, double latitude,
                    double longitude, int demand, String priority, String status) {
        this.customerId = customerId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.demand = demand;
        this.priority = priority;
        this.status = status;
    }


    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public int getDemand() { return demand; }
    public void setDemand(int demand) { this.demand = demand; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /**
     * Returns a priority multiplier used in cost calculations.
     * Higher priority = lower multiplier = lower effective cost (served first).
     */
    public double getPriorityMultiplier() {
        switch (priority) {
            case "URGENT": return 0.5;
            case "HIGH":   return 0.75;
            case "MEDIUM": return 1.0;
            case "LOW":    return 1.25;
            default:       return 1.0;
        }
    }

    @Override
    public String toString() {
        return String.format("Customer[%d] %s (%.4f, %.4f) demand=%d [%s] status=%s",
                customerId, name, latitude, longitude, demand, priority, status);
    }
}
