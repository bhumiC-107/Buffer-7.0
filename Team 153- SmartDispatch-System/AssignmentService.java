package com.project.service;

import com.project.model.Assignment;
import com.project.model.Customer;
import com.project.model.Warehouse;
import com.project.graph.BipartiteGraph;

import java.util.ArrayList;
import java.util.List;

/**

 * Pipeline role: Database → Graph → Dijkstra → Cost Matrix → Hungarian → ASSIGNMENT
 */
public class AssignmentService {

    
    //  *
    //  * The Hungarian result is an int[] where result[i] = j means
    //  * warehouse i is assigned to customer j. This method creates
    //  * Assignment objects with full details (names, distance, cost, time).
    //  *
    //  * @param hungarianResult Array from HungarianAlgorithm.solve()
    //  * @param warehouses      List of warehouses (indexed by i)
    //  * @param customers       List of customers (indexed by j)
    //  * @param costMatrix      The computed cost matrix
    //  * @return List of Assignment objects
    //  */
    public List<Assignment> createAssignments(int[] hungarianResult,
                                               List<Warehouse> warehouses,
                                               List<Customer> customers,
                                               double[][] costMatrix) {

        List<Assignment> assignments = new ArrayList<>();

        System.out.println("\n═══════════════════════════════════════");
        System.out.println("  OPTIMAL DELIVERY ASSIGNMENTS");
        System.out.println("═══════════════════════════════════════");

        for (int i = 0; i < hungarianResult.length; i++) {
            int j = hungarianResult[i];

            // Skip dummy assignments (when padded matrix was used)
            if (j == -1 || j >= customers.size()) {
                System.out.printf("  Warehouse %d → [No customer assigned]%n", i);
                continue;
            }

            Warehouse w = warehouses.get(i);
            Customer c = customers.get(j);

            // Compute raw distance (Haversine)
            double distance = BipartiteGraph.haversineDistance(
                    w.getLatitude(), w.getLongitude(),
                    c.getLatitude(), c.getLongitude()
            );

            // Compute delivery cost and time
            double cost = CostMatrixService.computeSimpleCost(distance, w.getCostPerKm());
            double time = CostMatrixService.computeDeliveryTime(distance);

            Assignment assignment = new Assignment(
                    w.getWarehouseId(), c.getCustomerId(),
                    w.getName(), c.getName(),
                    distance, cost, time
            );

            assignments.add(assignment);

            System.out.printf("  %s → %s%n", w.getName(), c.getName());
            System.out.printf("    Distance: %.2f km | Cost: ₹%.2f | Time: ~%.0f min%n",
                    distance, cost, time);
        }

        System.out.println("═══════════════════════════════════════\n");
        return assignments;
    }
}
