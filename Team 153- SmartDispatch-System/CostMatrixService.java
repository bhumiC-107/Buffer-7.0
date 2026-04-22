package com.project.service;

import com.project.algorithm.DijkstraAlgorithm;
import com.project.graph.BipartiteGraph;
import com.project.graph.Graph;
import com.project.model.Customer;
import com.project.model.Warehouse;

import java.util.List;


public class CostMatrixService {

    private static final double AVG_SPEED_KM_PER_MIN = 0.5; // 30 km/hr average delivery speed

    // /**
    //  * Builds the cost matrix using Dijkstra's algorithm on the bipartite graph.
    //  *
    //  * For each warehouse (source node), Dijkstra computes the shortest distance
    //  * to all customer nodes. These distances are then multiplied by cost factors
    //  * (per-km rate, priority, demand) to produce the final cost matrix.
    //  *
    //  * @param bipartiteGraph The bipartite graph (warehouses → customers)
    //  * @param warehouses     List of warehouses
    //  * @param customers      List of customers
    //  * @return 2D cost matrix [warehouses.size()][customers.size()]
    //  */
    public double[][] buildCostMatrix(BipartiteGraph bipartiteGraph,
                                       List<Warehouse> warehouses,
                                       List<Customer> customers) {

        int W = warehouses.size();
        int C = customers.size();
        Graph graph = bipartiteGraph.getGraph();

        double[][] costMatrix = new double[W][C];

        // Dijkstra engine on the bipartite graph
        DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);

        System.out.println("\n═══════════════════════════════════════");
        System.out.println("  BUILDING COST MATRIX via DIJKSTRA");
        System.out.println("═══════════════════════════════════════");

        for (int i = 0; i < W; i++) {
            Warehouse warehouse = warehouses.get(i);

            // Run Dijkstra from warehouse node i
            double[] shortestDistances = dijkstra.computeShortestPaths(i);

            for (int j = 0; j < C; j++) {
                Customer customer = customers.get(j);

                // Customer node ID in the graph = W + j
                int customerNodeId = W + j;
                double distance = shortestDistances[customerNodeId];

                // Compute effective cost with all factors
                double deliveryCost = computeEffectiveCost(
                        distance, warehouse.getCostPerKm(),
                        customer.getPriorityMultiplier(), customer.getDemand()
                );

                costMatrix[i][j] = deliveryCost;

                System.out.printf("  %s → %s: dist=%.2f km, cost=₹%.2f%n",
                        warehouse.getName(), customer.getName(), distance, deliveryCost);
            }
        }

        System.out.println("═══════════════════════════════════════\n");
        return costMatrix;
    }

    /**
     * Computes the effective delivery cost considering all factors.
     *
     * Formula: distance × costPerKm × priorityMultiplier × (1 + demandFactor)
     *
     * @param distanceKm          Shortest path distance in km
     * @param costPerKm           Warehouse-specific cost per kilometer
     * @param priorityMultiplier  Customer priority weight (URGENT=0.5, HIGH=0.75, etc.)
     * @param demand              Number of items in the order
     * @return Effective delivery cost
     */
    private double computeEffectiveCost(double distanceKm, double costPerKm,
                                         double priorityMultiplier, int demand) {
        // Base cost from distance
        double baseCost = distanceKm * costPerKm;

        // Apply priority: urgent/high orders get cost reduction (incentive to serve first)
        double priorityAdjusted = baseCost * priorityMultiplier;

        // Apply demand factor: larger orders cost slightly more
        double demandFactor = 1.0 + (demand * 0.1);

        return priorityAdjusted * demandFactor;
    }

    /**
     * Computes estimated delivery time in minutes.
     *
     * @param distanceKm Distance to the customer
     * @return Estimated time in minutes
     */
    public static double computeDeliveryTime(double distanceKm) {
        return distanceKm / AVG_SPEED_KM_PER_MIN;
    }

    /**
     * Computes simple delivery cost (distance × rate).
     *
     * @param distanceKm Distance to the customer
     * @param costPerKm  Rate per kilometer
     * @return Delivery cost
     */
    public static double computeSimpleCost(double distanceKm, double costPerKm) {
        return distanceKm * costPerKm;
    }
}
