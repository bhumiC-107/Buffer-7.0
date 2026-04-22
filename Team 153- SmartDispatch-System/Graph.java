package com.project.graph;

import com.project.model.Edge;

import java.util.*;


public class Graph {

    // Adjacency list: nodeId → list of edges from that node
    private final Map<Integer, List<Edge>> adjacencyList;
    private final int nodeCount;


    public Graph(int nodeCount) {
        this.nodeCount = nodeCount;
        this.adjacencyList = new HashMap<>();
        // Initialize empty adjacency lists for all nodes
        for (int i = 0; i < nodeCount; i++) {
            adjacencyList.put(i, new ArrayList<>());
        }
    }


    /**
     * Adds a directed edge from source to destination with given weight.
     *
     * @param source      Source node ID
     * @param destination Destination node ID
     * @param weight      Edge weight (distance/cost)
     */
    public void addEdge(int source, int destination, double weight) {
        adjacencyList.get(source).add(new Edge(source, destination, weight));
    }

    /**
     * Adds an undirected edge (both directions) between two nodes.
     *
     * @param node1  First node ID
     * @param node2  Second node ID
     * @param weight Edge weight (distance/cost)
     */
    public void addUndirectedEdge(int node1, int node2, double weight) {
        addEdge(node1, node2, weight);
        addEdge(node2, node1, weight);
    }

    /**
     * Returns all edges from a given node.
     *
     * @param nodeId The node to get neighbors for
     * @return List of edges from the node
     */
    public List<Edge> getNeighbors(int nodeId) {
        return adjacencyList.getOrDefault(nodeId, Collections.emptyList());
    }

    /**
     * Returns the number of nodes in the graph.
     */
    public int getNodeCount() {
        return nodeCount;
    }

    /**
     * Returns the full adjacency list.
     */
    public Map<Integer, List<Edge>> getAdjacencyList() {
        return adjacencyList;
    }

    /**
     * Prints the graph structure for debugging.
     */
    public void printGraph() {
        System.out.println("\n═══════════════════════════════════════");
        System.out.println("  GRAPH ADJACENCY LIST");
        System.out.println("═══════════════════════════════════════");
        for (Map.Entry<Integer, List<Edge>> entry : adjacencyList.entrySet()) {
            System.out.print("Node " + entry.getKey() + " → ");
            for (Edge edge : entry.getValue()) {
                System.out.printf("[%d: %.2f] ", edge.getDestinationId(), edge.getWeight());
            }
            System.out.println();
        }
        System.out.println("═══════════════════════════════════════\n");
    }
}
