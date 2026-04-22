package com.project.model;


public class Edge implements Comparable<Edge> {

    private int sourceId;       // source node identifier
    private int destinationId;  // destination node identifier
    private double weight;      // weight (distance / cost)


    public Edge() {}

    public Edge(int sourceId, int destinationId, double weight) {
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.weight = weight;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public int getSourceId() { return sourceId; }
    public void setSourceId(int sourceId) { this.sourceId = sourceId; }

    public int getDestinationId() { return destinationId; }
    public void setDestinationId(int destinationId) { this.destinationId = destinationId; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    /**
     * Comparison based on edge weight (for priority queue ordering).
     */
    @Override
    public int compareTo(Edge other) {
        return Double.compare(this.weight, other.weight);
    }

    @Override
    public String toString() {
        return String.format("Edge(%d -> %d, weight=%.4f)", sourceId, destinationId, weight);
    }
}
