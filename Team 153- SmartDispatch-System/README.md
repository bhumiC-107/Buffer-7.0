# SmartDispatch — Real-Time Multi-Warehouse Delivery Optimizer

A Java-based delivery optimization system that uses **graph algorithms** and **optimal assignment** to determine the best warehouse-to-customer pairings in a multi-warehouse delivery network.

---

## Project Structure

```
SmartDispatch-System/
├── sql/
│   └── schema.sql                          # MySQL schema + sample data
├── src/
│   └── com/project/
│       ├── Main.java                       # Application entry point (GUI/CLI)
│       ├── database/
│       │   └── DatabaseManager.java        # JDBC operations (MySQL)
│       ├── model/
│       │   ├── Warehouse.java              # Warehouse data model
│       │   ├── Customer.java               # Customer data model
│       │   ├── Edge.java                   # Graph edge model
│       │   └── Assignment.java             # Warehouse→Customer assignment
│       ├── graph/
│       │   ├── Graph.java                  # Adjacency list graph
│       │   └── BipartiteGraph.java         # Bipartite graph (W↔C)
│       ├── algorithm/
│       │   ├── DijkstraAlgorithm.java      # Shortest path (min-heap)
│       │   └── HungarianAlgorithm.java     # Min-cost matching
│       ├── service/
│       │   ├── CostMatrixService.java      # Dijkstra → cost matrix
│       │   ├── AssignmentService.java      # Assignment builder
│       │   └── DeliveryOptimizer.java      # Pipeline orchestrator
│       └── ui/
│           └── DashboardApp.java           # JavaFX visualization
└── README.md
```

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

## How to Run

### Step 1: Set Up Database

```bash
# Login to MySQL
mysql -u root -p

# Run the schema file
source /path/to/SmartDispatch-System/sql/schema.sql;
```

This creates the `delivery_optimizer` database with:

- `warehouses` table (5 warehouses across Delhi)
- `customers` table (10 customers with varying priorities)
- `assignments` table (populated by the optimizer)

### Step 2: Configure Database Connection

Edit `src/com/project/database/DatabaseManager.java`:

```java
private static final String URL = "jdbc:mysql://localhost:3306/delivery_optimizer";
private static final String USER = "root";
private static final String PASSWORD = "your_password_here";
```

### Step 3: Compile

```bash
cd SmartDispatch-System

# Set paths (adjust for your system)
set JAVAFX_LIB=..\javafx-sdk-21.0.6\lib
set MYSQL_JAR=..\mysql-connector-j-9.6.0\mysql-connector-j-9.6.0.jar

# Compile all Java files
javac -cp "%JAVAFX_LIB%\javafx.base.jar;%JAVAFX_LIB%\javafx.controls.jar;%JAVAFX_LIB%\javafx.graphics.jar;%MYSQL_JAR%" -d out src\com\project\model\*.java src\com\project\database\*.java src\com\project\graph\*.java src\com\project\algorithm\*.java src\com\project\service\*.java src\com\project\ui\*.java src\com\project\Main.java
```

### Step 4: Run

**GUI Mode (JavaFX Dashboard):**

```bash
java --module-path "%JAVAFX_LIB%" --add-modules javafx.controls,javafx.graphics -cp "out;%MYSQL_JAR%" com.project.Main
```

**Console Mode (CLI — no JavaFX needed):**

```bash
java -cp "out;%MYSQL_JAR%" com.project.Main --cli
```

> **Note**: The JavaFX SDK 21.0.6 is included in the project directory as `javafx-sdk-21.0.6`. If you need a different version, download from [Gluon](https://gluonhq.com/products/javafx/).

---

## Using the Dashboard

| Button                | Action                                                    |
| --------------------- | --------------------------------------------------------- |
| ** Run Optimization** | Runs the full pipeline and displays results instantly     |
| ** Reset**            | Clears all assignments and resets customers to PENDING    |
| ** Simulate**         | Runs real-time simulation (assignments appear one-by-one) |

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

## Packages

| Package                 | Purpose                                              |
| ----------------------- | ---------------------------------------------------- |
| `com.project.model`     | Data classes (Warehouse, Customer, Edge, Assignment) |
| `com.project.database`  | JDBC database operations                             |
| `com.project.graph`     | Graph data structures (adjacency list, bipartite)    |
| `com.project.algorithm` | Dijkstra & Hungarian algorithm implementations       |
| `com.project.service`   | Business logic (cost matrix, assignment, optimizer)  |
| `com.project.ui`        | JavaFX visualization dashboard                       |
