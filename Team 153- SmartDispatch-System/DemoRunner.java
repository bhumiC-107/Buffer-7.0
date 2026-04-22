package com.project.service;

import com.project.algorithm.DijkstraAlgorithm;
import com.project.algorithm.HungarianAlgorithm;
import com.project.graph.BipartiteGraph;
import com.project.graph.Graph;
import com.project.model.Assignment;
import com.project.model.Customer;
import com.project.model.Warehouse;

import java.util.ArrayList;
import java.util.List;

/**
 * DemoRunner — Runs the FULL optimization pipeline using in-memory sample data.
 * No MySQL connection required. Perfect for demonstrating the algorithm pipeline.
 *
 * Pipeline: Sample Data → Graph → Dijkstra → Cost Matrix → Hungarian → Assignment
 */
public class DemoRunner {

    public static void main(String[] args) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║   SMARTDISPATCH — MULTI-WAREHOUSE DELIVERY OPTIMIZER    ║");
        System.out.println("║   Demo Mode (In-Memory Data — No MySQL Required)        ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        
        System.out.println("\n STEP 1: Loading sample warehouse & customer data...");

        List<Warehouse> warehouses = new ArrayList<>();
        warehouses.add(new Warehouse(1, "Central Hub",       28.6139, 77.2090, 150, 1.2, "ACTIVE"));
        warehouses.add(new Warehouse(2, "North Depot",       28.7041, 77.1025, 120, 1.5, "ACTIVE"));
        warehouses.add(new Warehouse(3, "South Terminal",    28.5245, 77.1855, 130, 1.3, "ACTIVE"));
        warehouses.add(new Warehouse(4, "East Warehouse",    28.6280, 77.2950, 100, 1.4, "ACTIVE"));
        warehouses.add(new Warehouse(5, "West Distribution", 28.6508, 77.0969, 110, 1.6, "ACTIVE"));

        List<Customer> customers = new ArrayList<>();
        customers.add(new Customer(1,  "Rajesh Kumar",   28.6350, 77.2250, 3, "HIGH",   "PENDING"));
        customers.add(new Customer(2,  "Priya Sharma",   28.6800, 77.1500, 2, "MEDIUM", "PENDING"));
        customers.add(new Customer(3,  "Amit Patel",     28.5500, 77.2100, 4, "URGENT", "PENDING"));
        customers.add(new Customer(4,  "Sneha Gupta",    28.6100, 77.3100, 1, "LOW",    "PENDING"));
        customers.add(new Customer(5,  "Vikram Singh",   28.7100, 77.0800, 2, "HIGH",   "PENDING"));
        customers.add(new Customer(6,  "Anita Reddy",    28.5400, 77.1600, 3, "MEDIUM", "PENDING"));
        customers.add(new Customer(7,  "Rohit Mehta",    28.6700, 77.2500, 2, "HIGH",   "PENDING"));
        customers.add(new Customer(8,  "Kavita Joshi",   28.5900, 77.1200, 1, "LOW",    "PENDING"));
        customers.add(new Customer(9,  "Suresh Nair",    28.6450, 77.3300, 5, "URGENT", "PENDING"));
        customers.add(new Customer(10, "Deepika Verma",  28.6900, 77.1900, 2, "MEDIUM", "PENDING"));

        System.out.println("  " + warehouses.size() + " warehouses loaded:");
        for (Warehouse w : warehouses) {
            System.out.println("    • " + w);
        }
        System.out.println("   " + customers.size() + " customers loaded:");
        for (Customer c : customers) {
            System.out.println("    • " + c);
        }

        
        System.out.println("\n STEP 2: Building Bipartite Graph (Adjacency List)...");

        BipartiteGraph bipartiteGraph = new BipartiteGraph(warehouses, customers);
        Graph graph = bipartiteGraph.getGraph();
        graph.printGraph();
        System.out.println("   Graph built with " + graph.getNodeCount() + " nodes");
        System.out.println("    Warehouses: nodes 0-" + (warehouses.size()-1));
        System.out.println("    Customers:  nodes " + warehouses.size() + "-" + (graph.getNodeCount()-1));


        System.out.println("\n STEP 3: Running Dijkstra's Algorithm (Min-Heap Priority Queue)...");

        DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);
        int W = warehouses.size();
        int C = customers.size();

        double[][] distanceMatrix = new double[W][C];

        for (int i = 0; i < W; i++) {
            double[] shortestPaths = dijkstra.computeShortestPaths(i);
            System.out.printf("  Dijkstra from %s:%n", warehouses.get(i).getName());
            for (int j = 0; j < C; j++) {
                distanceMatrix[i][j] = shortestPaths[W + j];
                System.out.printf("    → %-15s : %8.4f km%n", customers.get(j).getName(), distanceMatrix[i][j]);
            }
        }
        System.out.println("  ✓ Shortest paths computed for all warehouse-customer pairs");

        
        //  STEP 4: BUILD COST MATRIX
        
        System.out.println("\n STEP 4: Building Cost Matrix (distance × costPerKm × priority × demand)...");

        CostMatrixService costService = new CostMatrixService();
        double[][] costMatrix = costService.buildCostMatrix(bipartiteGraph, warehouses, customers);

        // Print cost matrix as a table
        System.out.println("\n  ┌─────────────────── COST MATRIX (₹) ───────────────────┐");
        System.out.printf("  │ %-18s", "");
        for (int j = 0; j < C; j++) {
            String name = customers.get(j).getName();
            if (name.length() > 8) name = name.substring(0, 8);
            System.out.printf("│%8s ", name);
        }
        System.out.println("│");
        System.out.println("  ├──────────────────" + "─────────".repeat(C) + "┤");

        for (int i = 0; i < W; i++) {
            System.out.printf("  │ %-18s", warehouses.get(i).getName());
            for (int j = 0; j < C; j++) {
                System.out.printf("│%8.2f ", costMatrix[i][j]);
            }
            System.out.println("│");
        }
        System.out.println("  └──────────────────" + "─────────".repeat(C) + "┘");

        //  STEP 5: HUNGARIAN ALGORITHM — OPTIMAL MATCHING
        // 
        System.out.println("\n STEP 5: Running Hungarian Algorithm (Minimum Cost Matching)...");

        HungarianAlgorithm hungarian = new HungarianAlgorithm(costMatrix);
        int[] optimalAssignment = hungarian.solve();
        hungarian.printAssignment(optimalAssignment);

        double totalOptimalCost = hungarian.computeTotalCost(optimalAssignment);
        System.out.printf("   Optimal matching found! Minimum total cost: ₹%.2f%n", totalOptimalCost);

        // 
        //  STEP 6: CREATE ASSIGNMENT OBJECTS
        // 
        System.out.println("\n STEP 6: Creating final delivery assignments...");

        AssignmentService assignmentService = new AssignmentService();
        List<Assignment> assignments = assignmentService.createAssignments(
                optimalAssignment, warehouses, customers, costMatrix
        );

        // 
        //  FINAL SUMMARY
        // 
        double totalCost = 0, totalDist = 0, totalTime = 0;
        for (Assignment a : assignments) {
            totalCost += a.getDeliveryCost();
            totalDist += a.getDistanceKm();
            totalTime += a.getEstimatedTimeMinutes();
        }

        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║              OPTIMAL DELIVERY PLAN — SUMMARY            ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.printf("║  %-54s  ║%n", "Assignments: " + assignments.size());
        System.out.printf("║  %-54s  ║%n", String.format("Total Distance: %.2f km", totalDist));
        System.out.printf("║  %-54s  ║%n", String.format("Total Cost:     ₹%.2f", totalCost));
        System.out.printf("║  %-54s  ║%n", String.format("Avg Delivery:   %.0f minutes",
                assignments.isEmpty() ? 0 : totalTime / assignments.size()));
        System.out.println("╠══════════════════════════════════════════════════════════╣");

        for (Assignment a : assignments) {
            System.out.printf("║  %-16s → %-15s %6.2f km  ₹%6.2f  ~%2.0f min  ║%n",
                    a.getWarehouseName(), a.getCustomerName(),
                    a.getDistanceKm(), a.getDeliveryCost(), a.getEstimatedTimeMinutes());
        }

        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println("\n   Pipeline complete. All customers optimally assigned!");
        System.out.println();
    }
}
