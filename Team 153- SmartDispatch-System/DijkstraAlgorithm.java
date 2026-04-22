package com.project.algorithm;

import com.project.graph.Graph;
import com.project.model.Edge;

import java.util.*;


public class DijkstraAlgorithm {

    private final Graph graph;

    // ── Constructor 
    public DijkstraAlgorithm(Graph graph) {
        this.graph = graph;
    }

    
    //  * @param sourceNode The starting node ID
    //  * @return Array where dist[i] = shortest distance from source to node i
    //  */
    public double[] computeShortestPaths(int sourceNode) {
        int V = graph.getNodeCount();

        // Distance array: dist[i] = shortest known distance from source to node i
        double[] dist = new double[V];
        Arrays.fill(dist, Double.MAX_VALUE);
        dist[sourceNode] = 0.0;

        // Visited set to avoid re-processing nodes
        boolean[] visited = new boolean[V];

        // Min-Heap Priority Queue: orders Edge objects by weight (distance)
        // Each entry represents (distance, nodeId) — we reuse Edge for this purpose
        PriorityQueue<Edge> minHeap = new PriorityQueue<>();
        minHeap.offer(new Edge(sourceNode, sourceNode, 0.0));

        while (!minHeap.isEmpty()) {
            // Extract the node with minimum distance
            Edge current = minHeap.poll();
            int u = current.getDestinationId();

            // Skip if already processed (handles stale entries in the heap)
            if (visited[u]) continue;
            visited[u] = true;

            // Relax all neighbors of u
            for (Edge edge : graph.getNeighbors(u)) {
                int v = edge.getDestinationId();
                double newDist = dist[u] + edge.getWeight();

                // If we found a shorter path to v, update and enqueue
                if (!visited[v] && newDist < dist[v]) {
                    dist[v] = newDist;
                    minHeap.offer(new Edge(u, v, newDist));
                }
            }
        }

        return dist;
    }

    /**
     * Computes shortest distances from a specific source to a set of target nodes.
     * More convenient when you only need distances to specific destinations.
     *
     * @param sourceNode  The starting node ID
     * @param targetNodes Array of destination node IDs
     * @return Map of targetNodeId → shortest distance
     */
    public Map<Integer, Double> computeDistancesToTargets(int sourceNode, int[] targetNodes) {
        double[] allDistances = computeShortestPaths(sourceNode);
        Map<Integer, Double> result = new LinkedHashMap<>();

        for (int target : targetNodes) {
            result.put(target, allDistances[target]);
        }
        return result;
    }

    public void printResults(int sourceNode, double[] distances) {
        System.out.printf("%n── Dijkstra from Node %d ──%n", sourceNode);
        for (int i = 0; i < distances.length; i++) {
            if (distances[i] == Double.MAX_VALUE) {
                System.out.printf("  → Node %d: UNREACHABLE%n", i);
            } else {
                System.out.printf("  → Node %d: %.4f km%n", i, distances[i]);
            }
        }
    }
}
