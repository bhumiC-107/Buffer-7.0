package com.project.graph;

import com.project.model.Customer;
import com.project.model.Warehouse;

import java.util.List;


public class BipartiteGraph {

    private final Graph graph;
    private final int warehouseCount;
    private final int customerCount;

    

    /**
     * Builds the bipartite graph from warehouse and customer lists.
     * Every warehouse is connected to every customer via weighted edges.
     *
     * @param warehouses List of warehouses (Set A)
     * @param customers  List of customers (Set B)
     */
    public BipartiteGraph(List<Warehouse> warehouses, List<Customer> customers) {
        this.warehouseCount = warehouses.size();
        this.customerCount = customers.size();
        int totalNodes = warehouseCount + customerCount;

        // Create graph with totalNodes nodes
        this.graph = new Graph(totalNodes);

        // Build edges: each warehouse → each customer
        for (int i = 0; i < warehouseCount; i++) {
            Warehouse w = warehouses.get(i);
            for (int j = 0; j < customerCount; j++) {
                Customer c = customers.get(j);

                // Compute Haversine distance as edge weight
                double distance = haversineDistance(
                    w.getLatitude(), w.getLongitude(),
                    c.getLatitude(), c.getLongitude()
                );

                int warehouseNode = i;
                int customerNode = warehouseCount + j;

                // Connect warehouse to customer (directed: W → C)
                graph.addEdge(warehouseNode, customerNode, distance);
            }
        }

        System.out.printf("[Graph] Built bipartite graph: %d warehouses, %d customers, %d total nodes%n",
                warehouseCount, customerCount, totalNodes);
    }


    /**
     * Computes the Haversine distance between two lat/lon points.
     * Returns distance in kilometers.
     *
     * Formula accounts for Earth's curvature, which is important
     * even for city-scale distances (several km error if using Euclidean).
     *
     * @param lat1 Latitude of point 1 (degrees)
     * @param lon1 Longitude of point 1 (degrees)
     * @param lat2 Latitude of point 2 (degrees)
     * @param lon2 Longitude of point 2 (degrees)
     * @return Distance in kilometers
     */
    public static double haversineDistance(double lat1, double lon1,
                                           double lat2, double lon2) {
        final double R = 6371.0;  // Earth's radius in km

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }


    public Graph getGraph() { return graph; }
    public int getWarehouseCount() { return warehouseCount; }
    public int getCustomerCount() { return customerCount; }
}
