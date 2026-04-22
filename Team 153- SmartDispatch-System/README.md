# Problem Statement

Our project aims to design a *Real-Time Multi-Warehouse Delivery Optimizer* that determines the most optimal warehouse to fulfil any customer's order(s). The system will model warehouses and customers as a bipartite graph and computes the minimum-cost assignment between them based on factors such as distance, delivery time, and cost.

It will basically simulate how large platforms like Amazon allocate orders across warehouses to achieve efficient and cost-effective delivery operations.

Video link  : https://drive.google.com/file/d/1STIEBy8xRgIzzRy1INcxLRuzZJxzMFm5/view?usp=sharing

# SmartDispatch — Real-Time Multi-Warehouse Delivery Optimizer

A Java-based delivery optimization system that uses **graph algorithms** and **optimal assignment** to determine the best warehouse-to-customer pairings in a multi-warehouse delivery network.

---

## Backend Pipeline

```
DATABASE → GRAPH → DIJKSTRA → COST MATRIX → HUNGARIAN → OPTIMAL ASSIGNMENT
   ↓         ↓        ↓           ↓             ↓              ↓
 MySQL    Bipartite  Shortest   W×C matrix   Min-cost      W→C mapping
  JDBC    Adj. List   paths    (weighted)    matching      saved to DB
```

### Data Structures Used

- **Graph**: Adjacency List (`HashMap<Integer, List<Edge>>`)
- **Priority Queue**: Min-Heap (`java.util.PriorityQueue`) for Dijkstra
- **Bipartite Graph**: Warehouses (Set A) ↔ Customers (Set B)

### Algorithms

1. **Dijkstra's Algorithm** — Computes shortest paths from each warehouse to all customers
2. **Hungarian Algorithm** — Finds minimum-cost one-to-one matching between warehouses and customers

---

## Prerequisites

1. **Java JDK 17+** (with JavaFX bundled, or separate JavaFX SDK)
2. **MySQL 8.0+**
3. **MySQL Connector/J** (`mysql-connector-j-9.6.0.jar` included in project)

---


### Dashboard Features

- **Network Map**: Visual map showing warehouses (blue squares) and customers (colored circles by priority), with assignment lines
- **Statistics Cards**: Total cost, total distance, and average delivery time
- **Assignment Table**: Detailed table of all warehouse-to-customer matchings
- **Cost Matrix**: The computed W×C cost matrix
- **Pipeline Log**: Real-time log of all operations

---

## Sample Output (CLI Mode)

```

  REAL-TIME MULTI-WAREHOUSE DELIVERY OPTIMIZER
   Starting Full Optimization Pipeline...


 STEP 1: Loading data from MySQL...
  ✓ 5 warehouses loaded
  ✓ 10 pending customers loaded

 STEP 2: Building bipartite graph...
  ✓ Bipartite graph constructed (15 nodes)

 STEP 3-4: Running Dijkstra + Building cost matrix...
  ✓ Cost matrix generated (5×10)

 STEP 5: Running Hungarian Algorithm...
  ✓ Optimal matching computed
  ✓ Total minimum cost: ₹XX.XX

 STEP 6: Creating assignment objects...
  Central Hub → Rajesh Kumar: 2.15 km | ₹2.58 | ~4 min
  North Depot → Priya Sharma: 5.12 km | ₹7.68 | ~10 min
  ...

 STEP 7: Saving assignments to database...
  ✓ Assignments persisted to MySQL

║           PIPELINE COMPLETED SUCCESSFULLY           ║
```

---

## Key Design Decisions

1. **Haversine Distance**: Used instead of Euclidean distance for accurate real-world calculations
2. **Priority Weighting**: URGENT orders get a 0.5x cost multiplier (served first by the optimizer)
3. **Rectangular Hungarian**: Handles cases where #warehouses ≠ #customers by padding
4. **Transaction Safety**: Assignment persistence uses database transactions with rollback
5. **Modular OOP**: Each algorithm, service, and model is a separate class following SOLID principles

---

