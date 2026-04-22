package com.project;

import com.project.model.Assignment;
import com.project.service.DeliveryOptimizer;
import com.project.ui.DashboardApp;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--cli")) {
            runConsoleMode();
        } else {
            // Launch JavaFX Dashboard
            DashboardApp.main(args);
        }
    }

    /**
     * Runs the full optimization pipeline in console-only mode.
     * Useful for testing without JavaFX dependencies.
     */
    private static void runConsoleMode() {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║  SmartDispatch — Console Mode                   ║");
        System.out.println("║  Real-Time Multi-Warehouse Delivery Optimizer   ║");
        System.out.println("╚══════════════════════════════════════════════════╝");

        DeliveryOptimizer optimizer = new DeliveryOptimizer();

        try {
            // Run the full pipeline
            List<Assignment> assignments = optimizer.runOptimization();

            // Print summary
            System.out.println("\n╔══════════════════════════════════════╗");
            System.out.println("║          FINAL SUMMARY               ║");
            System.out.println("╠══════════════════════════════════════╣");

            double totalCost = 0;
            double totalDistance = 0;
            double totalTime = 0;

            for (Assignment a : assignments) {
                System.out.printf("║ %-36s ║%n", a.toString());
                totalCost += a.getDeliveryCost();
                totalDistance += a.getDistanceKm();
                totalTime += a.getEstimatedTimeMinutes();
            }

            System.out.println("╠══════════════════════════════════════╣");
            System.out.printf("║ Total Cost:     ₹%-19.2f ║%n", totalCost);
            System.out.printf("║ Total Distance: %-19.2f km ║%n", totalDistance);
            System.out.printf("║ Avg Time:       %-19.0f min║%n",
                    assignments.isEmpty() ? 0 : totalTime / assignments.size());
            System.out.println("╚══════════════════════════════════════╝");

        } catch (Exception e) {
            System.err.println("Error during optimization: " + e.getMessage());
            e.printStackTrace();
        } finally {
            optimizer.getDbManager().close();
        }
    }
}
