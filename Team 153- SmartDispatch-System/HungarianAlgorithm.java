package com.project.algorithm;

import java.util.Arrays;

/**
 * HungarianAlgorithm — Solves the Assignment Problem using the
 * Hungarian (Kuhn-Munkres) method in O(n³) time.
 *
 * Handles rectangular matrices by padding to square internally.
 * Uses potential-based approach for correctness and performance.
 */
public class HungarianAlgorithm {

    private final double[][] costMatrix;
    private final int size;         // Dimension of the (padded) square matrix
    private final int originalRows; // Original number of warehouses
    private final int originalCols; // Original number of customers

    /**
     * @param costMatrix 2D array where costMatrix[i][j] = cost of assigning
     *                   warehouse i to customer j
     */
    public HungarianAlgorithm(double[][] costMatrix) {
        this.originalRows = costMatrix.length;
        this.originalCols = costMatrix[0].length;
        this.size = Math.max(originalRows, originalCols);

        // Pad to square matrix if needed
        this.costMatrix = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i < originalRows && j < originalCols) {
                    this.costMatrix[i][j] = costMatrix[i][j];
                } else {
                    // Dummy entries get zero cost (won't affect real assignments)
                    this.costMatrix[i][j] = 0;
                }
            }
        }
    }

    /**
     * Executes the Hungarian Algorithm using the potential (label) based method.
     * This is the standard O(n³) implementation that is guaranteed to terminate.
     *
     * @return Array where result[i] = column (customer index) assigned to row i (warehouse).
     *         result[i] = -1 if warehouse i has no valid assignment.
     */
    public int[] solve() {
        int n = size;

        // u[i] and v[j] are the potentials (dual variables) for rows and columns
        double[] u = new double[n + 1];
        double[] v = new double[n + 1];

        // p[j] = row assigned to column j (1-indexed, 0 means unassigned)
        int[] p = new int[n + 1];

        // way[j] = the column that led to column j in the augmenting path
        int[] way = new int[n + 1];

        // Process each row one at a time
        for (int i = 1; i <= n; i++) {
            // Introduce a virtual column 0 assigned to row i
            p[0] = i;
            int j0 = 0; // Start from virtual column 0

            double[] minv = new double[n + 1]; // minv[j] = minimum reduced cost to reach column j
            boolean[] used = new boolean[n + 1]; // used[j] = whether column j is in the augmenting tree
            Arrays.fill(minv, Double.MAX_VALUE);
            Arrays.fill(used, false);

            // Build augmenting tree until we find a free column
            do {
                used[j0] = true;
                int i0 = p[j0]; // Row assigned to column j0
                double delta = Double.MAX_VALUE;
                int j1 = -1;

                // Scan all free columns to find the one with minimum reduced cost
                for (int j = 1; j <= n; j++) {
                    if (used[j]) continue;

                    double cur = costMatrix[i0 - 1][j - 1] - u[i0] - v[j];
                    if (cur < minv[j]) {
                        minv[j] = cur;
                        way[j] = j0;
                    }
                    if (minv[j] < delta) {
                        delta = minv[j];
                        j1 = j;
                    }
                }

                // Update potentials
                for (int j = 0; j <= n; j++) {
                    if (used[j]) {
                        u[p[j]] += delta;
                        v[j] -= delta;
                    } else {
                        minv[j] -= delta;
                    }
                }

                j0 = j1;
            } while (p[j0] != 0); // Continue until we reach a free column

            // Trace back the augmenting path and update assignments
            do {
                int j1 = way[j0];
                p[j0] = p[j1];
                j0 = j1;
            } while (j0 != 0);
        }

        // Extract results: convert from 1-indexed column assignments to 0-indexed
        int[] result = new int[originalRows];
        for (int j = 1; j <= n; j++) {
            if (p[j] != 0 && p[j] <= originalRows && j <= originalCols) {
                result[p[j] - 1] = j - 1;
            }
        }

        // Mark unassigned warehouses (those mapped to dummy columns)
        boolean[] assigned = new boolean[originalRows];
        for (int j = 1; j <= n; j++) {
            if (p[j] != 0 && p[j] <= originalRows && j <= originalCols) {
                assigned[p[j] - 1] = true;
            }
        }
        for (int i = 0; i < originalRows; i++) {
            if (!assigned[i]) {
                result[i] = -1;
            }
        }

        return result;
    }

    /**
     * Computes the total cost of a given assignment.
     *
     * @param assignment Result from solve()
     * @return Total cost of all assignments
     */
    public double computeTotalCost(int[] assignment) {
        double totalCost = 0;
        for (int i = 0; i < assignment.length; i++) {
            if (assignment[i] != -1) {
                totalCost += costMatrix[i][assignment[i]];
            }
        }
        return totalCost;
    }

    /**
     * Prints the cost matrix in a formatted table.
     */
    public void printCostMatrix() {
        System.out.println("\n═════════════════════════════════════");
        System.out.println("  COST MATRIX (Warehouse × Customer)");
        System.out.println("═════════════════════════════════════");
        System.out.printf("%8s", "");
        for (int j = 0; j < originalCols; j++) {
            System.out.printf(" C%-6d", j);
        }
        System.out.println();

        for (int i = 0; i < originalRows; i++) {
            System.out.printf("W%-6d", i);
            for (int j = 0; j < originalCols; j++) {
                System.out.printf(" %7.2f", costMatrix[i][j]);
            }
            System.out.println();
        }
        System.out.println("═════════════════════════════════════\n");
    }

    public void printAssignment(int[] assignment) {
        System.out.println("\n═════════════════════════════════════");
        System.out.println("  OPTIMAL ASSIGNMENT (Hungarian)");
        System.out.println("═════════════════════════════════════");
        for (int i = 0; i < assignment.length; i++) {
            if (assignment[i] != -1) {
                System.out.printf("  Warehouse %d → Customer %d (cost: %.2f)%n",
                        i, assignment[i], costMatrix[i][assignment[i]]);
            } else {
                System.out.printf("  Warehouse %d → [No assignment]%n", i);
            }
        }
        System.out.printf("  Total Cost: %.2f%n", computeTotalCost(assignment));
        System.out.println("═════════════════════════════════════\n");
    }
}
