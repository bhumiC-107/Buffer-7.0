package com.project.service;

import com.project.algorithm.HungarianAlgorithm;
import com.project.database.DatabaseManager;
import com.project.graph.BipartiteGraph;
import com.project.model.Assignment;
import com.project.model.Customer;
import com.project.model.Warehouse;

import java.util.List;


public class DeliveryOptimizer {

    private final DatabaseManager dbManager;
    private final CostMatrixService costMatrixService;
    private final AssignmentService assignmentService;

    // Cached data from the last run
    private List<Warehouse> warehouses;
    private List<Customer> customers;
    private double[][] costMatrix;
    private int[] hungarianResult;
    private List<Assignment> assignments;
    private BipartiteGraph bipartiteGraph;

    // ── Constructor ───────────────────────────────────────────

    public DeliveryOptimizer() {
        this.dbManager = new DatabaseManager();
        this.costMatrixService = new CostMatrixService();
        this.assignmentService = new AssignmentService();
    }

    public DeliveryOptimizer(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.costMatrixService = new CostMatrixService();
        this.assignmentService = new AssignmentService();
    }

    //  *
    //  * @return List of optimal assignments
    //  */
    public List<Assignment> runOptimization() {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║   REAL-TIME MULTI-WAREHOUSE DELIVERY OPTIMIZER      ║");
        System.out.println("║   Starting Full Optimization Pipeline...            ║");
        System.out.println("╚══════════════════════════════════════════════════════╝\n");

        // ── Step 1: DATABASE 
        System.out.println(" STEP 1: Loading data from MySQL...");
        warehouses = dbManager.loadWarehouses();
        customers = dbManager.loadPendingCustomers();

        if (warehouses.isEmpty() || customers.isEmpty()) {
            System.err.println(" No warehouses or customers found. Pipeline aborted.");
            return List.of();
        }

        System.out.println("   " + warehouses.size() + " warehouses loaded");
        System.out.println("   " + customers.size() + " pending customers loaded");

        // ── Step 2: GRAPH ────────────────────────────────────
        System.out.println("\n STEP 2: Building bipartite graph...");
        bipartiteGraph = new BipartiteGraph(warehouses, customers);
        bipartiteGraph.getGraph().printGraph();
        System.out.println("   Bipartite graph constructed");

        // ── Step 3 & 4: DIJKSTRA + COST MATRIX ─────────────
        System.out.println("\n STEP 3-4: Running Dijkstra + Building cost matrix...");
        costMatrix = costMatrixService.buildCostMatrix(bipartiteGraph, warehouses, customers);
        System.out.println("   Cost matrix generated (" + warehouses.size() + "×" + customers.size() + ")");

        // ── Step 5: HUNGARIAN ALGORITHM ─────────────────────
        System.out.println("\n STEP 5: Running Hungarian Algorithm...");
        HungarianAlgorithm hungarian = new HungarianAlgorithm(costMatrix);
        hungarian.printCostMatrix();
        hungarianResult = hungarian.solve();
        hungarian.printAssignment(hungarianResult);
        System.out.println("   Optimal matching computed");
        System.out.printf("   Total minimum cost: ₹%.2f%n", hungarian.computeTotalCost(hungarianResult));

        // ── Step 6: CREATE ASSIGNMENTS ──────────────────────
        System.out.println("\n STEP 6: Creating assignment objects...");
        assignments = assignmentService.createAssignments(
                hungarianResult, warehouses, customers, costMatrix
        );
        System.out.println("   " + assignments.size() + " assignments created");

        // ── Step 7: PERSIST TO DATABASE ─────────────────────
        System.out.println("\n STEP 7: Saving assignments to database...");
        dbManager.saveAssignments(assignments);
        System.out.println("   Assignments persisted to MySQL");

        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║           PIPELINE COMPLETED SUCCESSFULLY           ║");
        System.out.println("╚══════════════════════════════════════════════════════╝\n");

        return assignments;
    }

    /**
     * Resets the system for a new optimization run.
     * Clears all assignments and resets customer statuses.
     */
    public void reset() {
        dbManager.resetAssignments();
        warehouses = null;
        customers = null;
        costMatrix = null;
        hungarianResult = null;
        assignments = null;
        bipartiteGraph = null;
    }

    // ── Getters (for UI access) ───────────────────────────────

    public List<Warehouse> getWarehouses() { return warehouses; }
    public List<Customer> getCustomers() { return customers; }
    public double[][] getCostMatrix() { return costMatrix; }
    public int[] getHungarianResult() { return hungarianResult; }
    public List<Assignment> getAssignments() { return assignments; }
    public BipartiteGraph getBipartiteGraph() { return bipartiteGraph; }
    public DatabaseManager getDbManager() { return dbManager; }
}
