package com.project.database;

import com.project.model.Assignment;
import com.project.model.Customer;
import com.project.model.Warehouse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class DatabaseManager {

   
    private static final String URL = "jdbc:mysql://localhost:3306/delivery_optimizer";
    private static final String USER = "root";
    private static final String PASSWORD = "root";  

    private Connection connection;


    @SuppressWarnings("this-escape")
    public DatabaseManager() {
        connect();
    }

    
    public void connect() {
        try {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e1) {
                Class.forName("com.mysql.jdbc.Driver");
            }
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DB] Connected to database: delivery_optimizer");
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] MySQL JDBC driver not found. Add mysql-connector-j to classpath.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("[DB] Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }


    /**
     * Fetches all ACTIVE warehouses from the database.
     * Only active warehouses participate in the delivery graph.
     *
     * @return List of active Warehouse objects
     */
    public List<Warehouse> loadWarehouses() {
        List<Warehouse> warehouses = new ArrayList<>();
        String sql = "SELECT warehouse_id, name, latitude, longitude, capacity, cost_per_km, status "
                   + "FROM warehouses WHERE status = 'ACTIVE'";

        try {
            Connection conn = getConnection();
            if (conn == null) {
                System.err.println("[DB] Cannot load warehouses: no database connection");
                return warehouses;
            }
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Warehouse w = new Warehouse(
                    rs.getInt("warehouse_id"),
                    rs.getString("name"),
                    rs.getDouble("latitude"),
                    rs.getDouble("longitude"),
                    rs.getInt("capacity"),
                    rs.getDouble("cost_per_km"),
                    rs.getString("status")
                );
                warehouses.add(w);
            }
            rs.close();
            stmt.close();
            System.out.println("[DB] Loaded " + warehouses.size() + " warehouses");
        } catch (SQLException e) {
            System.err.println("[DB] Error loading warehouses: " + e.getMessage());
            e.printStackTrace();
        }
        return warehouses;
    }

    /**
     * Inserts a new warehouse into the database.
     */
    public boolean insertWarehouse(String name, double lat, double lon, int capacity, double costPerKm) {
        String sql = "INSERT INTO warehouses (name, latitude, longitude, capacity, cost_per_km) VALUES (?, ?, ?, ?, ?)";
        Connection conn = getConnection();
        if (conn == null) {
            System.err.println("[DB] Cannot insert warehouse: no database connection");
            return false;
        }
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setDouble(2, lat);
            stmt.setDouble(3, lon);
            stmt.setInt(4, capacity);
            stmt.setDouble(5, costPerKm);
            stmt.executeUpdate();
            System.out.println("[DB] Inserted warehouse: " + name);
            return true;
        } catch (SQLException e) {
            System.err.println("[DB] Error inserting warehouse: " + e.getMessage());
            return false;
        }
    }

    /**
     * Inserts a new customer into the database.
     */
    public boolean insertCustomer(String name, double lat, double lon, int demand, String priority) {
        String sql = "INSERT INTO customers (name, latitude, longitude, demand, priority) VALUES (?, ?, ?, ?, ?)";
        Connection conn = getConnection();
        if (conn == null) {
            System.err.println("[DB] Cannot insert customer: no database connection");
            return false;
        }
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setDouble(2, lat);
            stmt.setDouble(3, lon);
            stmt.setInt(4, demand);
            stmt.setString(5, priority);
            stmt.executeUpdate();
            System.out.println("[DB] Inserted customer: " + name);
            return true;
        } catch (SQLException e) {
            System.err.println("[DB] Error inserting customer: " + e.getMessage());
            return false;
        }
    }

   
    /**
     * Fetches all PENDING customers (not yet assigned to a warehouse).
     * These form the customer side of the bipartite graph.
     *
     * @return List of pending Customer objects
     */
    public List<Customer> loadPendingCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT customer_id, name, latitude, longitude, demand, priority, status "
                   + "FROM customers WHERE status = 'PENDING'";

        try {
            Connection conn = getConnection();
            if (conn == null) {
                System.err.println("[DB] Cannot load customers: no database connection");
                return customers;
            }
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Customer c = new Customer(
                    rs.getInt("customer_id"),
                    rs.getString("name"),
                    rs.getDouble("latitude"),
                    rs.getDouble("longitude"),
                    rs.getInt("demand"),
                    rs.getString("priority"),
                    rs.getString("status")
                );
                customers.add(c);
            }
            rs.close();
            stmt.close();
            System.out.println("[DB] Loaded " + customers.size() + " pending customers");
        } catch (SQLException e) {
            System.err.println("[DB] Error loading customers: " + e.getMessage());
            e.printStackTrace();
        }
        return customers;
    }

    // ── Assignment Operations ─────────────────────────────────

    /**
     * Persists optimal assignments to the database.
     * Also updates customer status from PENDING to ASSIGNED.
     *
     * @param assignments List of computed optimal assignments
     */
    public void saveAssignments(List<Assignment> assignments) {
        String insertSql = "INSERT INTO assignments (warehouse_id, customer_id, distance_km, "
                         + "delivery_cost, estimated_time) VALUES (?, ?, ?, ?, ?)";
        String updateStatusSql = "UPDATE customers SET status = 'ASSIGNED' WHERE customer_id = ?";

        try {
            Connection conn = getConnection();
            if (conn == null) {
                System.err.println("[DB] Cannot save assignments: no database connection");
                return;
            }
            conn.setAutoCommit(false);  // Transaction for atomicity

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                 PreparedStatement updateStmt = conn.prepareStatement(updateStatusSql)) {

                for (Assignment a : assignments) {
                    // Insert assignment record
                    insertStmt.setInt(1, a.getWarehouseId());
                    insertStmt.setInt(2, a.getCustomerId());
                    insertStmt.setDouble(3, a.getDistanceKm());
                    insertStmt.setDouble(4, a.getDeliveryCost());
                    insertStmt.setDouble(5, a.getEstimatedTimeMinutes());
                    insertStmt.addBatch();

                    // Update customer status
                    updateStmt.setInt(1, a.getCustomerId());
                    updateStmt.addBatch();
                }

                insertStmt.executeBatch();
                updateStmt.executeBatch();
                conn.commit();

                System.out.println("[DB] Saved " + assignments.size() + " assignments");

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.err.println("[DB] Error saving assignments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads all assignments from the database (for display purposes).
     *
     * @return List of all Assignment objects
     */
    public List<Assignment> loadAssignments() {
        List<Assignment> assignments = new ArrayList<>();
        String sql = "SELECT a.assignment_id, a.warehouse_id, a.customer_id, "
                   + "w.name AS warehouse_name, c.name AS customer_name, "
                   + "a.distance_km, a.delivery_cost, a.estimated_time "
                   + "FROM assignments a "
                   + "JOIN warehouses w ON a.warehouse_id = w.warehouse_id "
                   + "JOIN customers c ON a.customer_id = c.customer_id "
                   + "ORDER BY a.assigned_at DESC";

        try {
            Connection conn = getConnection();
            if (conn == null) {
                System.err.println("[DB] Cannot load assignments: no database connection");
                return assignments;
            }
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Assignment a = new Assignment(
                    rs.getInt("warehouse_id"),
                    rs.getInt("customer_id"),
                    rs.getString("warehouse_name"),
                    rs.getString("customer_name"),
                    rs.getDouble("distance_km"),
                    rs.getDouble("delivery_cost"),
                    rs.getDouble("estimated_time")
                );
                a.setAssignmentId(rs.getInt("assignment_id"));
                assignments.add(a);
            }
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("[DB] Error loading assignments: " + e.getMessage());
            e.printStackTrace();
        }
        return assignments;
    }

    /**
     * Resets all customers to PENDING and clears assignments.
     * Useful for re-running the optimization simulation.
     */
    public void resetAssignments() {
        try {
            Connection conn = getConnection();
            if (conn == null) {
                System.err.println("[DB] Cannot reset: no database connection");
                return;
            }
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("DELETE FROM assignments");
            stmt.executeUpdate("UPDATE customers SET status = 'PENDING'");
            stmt.close();
            System.out.println("[DB] Assignments reset, all customers set to PENDING");
        } catch (SQLException e) {
            System.err.println("[DB] Error resetting: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Closes the database connection.
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
