package com.project.ui;

import com.project.graph.BipartiteGraph;
import com.project.model.Assignment;
import com.project.model.Customer;
import com.project.model.Warehouse;
import com.project.service.DeliveryOptimizer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;


public class DashboardApp extends Application {

    private DeliveryOptimizer optimizer;
    private Canvas mapCanvas;
    private TextArea logArea;
    private TableView<Assignment> assignmentTable;
    private Label statusLabel;
    private Label totalCostLabel;
    private Label totalDistanceLabel;
    private Label avgTimeLabel;
    private VBox costMatrixBox;

    // Map bounds (Delhi region)
    private static final double MIN_LAT = 28.50;
    private static final double MAX_LAT = 28.75;
    private static final double MIN_LON = 77.05;
    private static final double MAX_LON = 77.40;

    // Color palette for warehouse-customer lines
    private static final Color[] ASSIGNMENT_COLORS = {
            Color.rgb(0, 200, 83),      // Green
            Color.rgb(33, 150, 243),     // Blue
            Color.rgb(255, 152, 0),      // Orange
            Color.rgb(156, 39, 176),     // Purple
            Color.rgb(244, 67, 54),      // Red
            Color.rgb(0, 188, 212),      // Cyan
            Color.rgb(255, 235, 59),     // Yellow
            Color.rgb(121, 85, 72),      // Brown
    };

    @Override
    public void start(Stage primaryStage) {
        optimizer = new DeliveryOptimizer();

        // ── ROOT LAYOUT 
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");

        // ── HEADER 
        HBox header = createHeader();
        root.setTop(header);

        // ── CENTER: Map Visualization 
        VBox mapSection = createMapSection();
        root.setCenter(mapSection);

        // ── RIGHT: Results Panel 
        VBox resultsPanel = createResultsPanel();
        root.setRight(resultsPanel);

        // ── BOTTOM: Log & Statistics 
        HBox bottomPanel = createBottomPanel();
        root.setBottom(bottomPanel);

        // ── SCENE 
        Scene scene = new Scene(root, 1500, 900);
        primaryStage.setTitle("SmartDispatch - Real-Time Multi-Warehouse Delivery Optimizer");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1300);
        primaryStage.setMinHeight(750);
        primaryStage.setMaximized(true);  // Start maximized to use full screen
        primaryStage.show();

        log("Application started. Click 'Run Optimization' to begin.");
    }

    //  UI COMPONENT BUILDERS
    // 

    /**
     * Creates the gradient header bar with title and action buttons.
     */
    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setStyle("-fx-background-color: linear-gradient(to right, #0f3460, #16213e, #1a1a2e);");

        // Title
        Label title = new Label("⚡ SmartDispatch — Delivery Optimizer");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#e94560"));

        Label subtitle = new Label("Real-Time Multi-Warehouse Assignment Engine");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        subtitle.setTextFill(Color.web("#a0a0b0"));

        VBox titleBox = new VBox(2, title, subtitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        // Action buttons
        Button runBtn = createStyledButton("Run Optimization", "#00c853", "#00a844");
        runBtn.setOnAction(e -> runOptimization());

        Button resetBtn = createStyledButton("Reset", "#ff9800", "#e68900");
        resetBtn.setOnAction(e -> resetSystem());

        Button simulateBtn = createStyledButton("Simulate", "#2196f3", "#1976d2");
        simulateBtn.setOnAction(e -> simulateRealTime());

        Button addWhBtn = createStyledButton("+ Warehouse", "#9c27b0", "#7b1fa2");
        addWhBtn.setOnAction(e -> showAddWarehouseDialog());

        Button addCustBtn = createStyledButton("+ Customer", "#e91e63", "#c2185b");
        addCustBtn.setOnAction(e -> showAddCustomerDialog());

        header.getChildren().addAll(titleBox, addWhBtn, addCustBtn, runBtn, resetBtn, simulateBtn);
        return header;
    }

    /**
     * Creates a styled button with hover effects.
     */
    private Button createStyledButton(String text, String bgColor, String hoverColor) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btn.setTextFill(Color.WHITE);
        btn.setStyle(String.format(
                "-fx-background-color: %s; -fx-background-radius: 8; "
              + "-fx-padding: 10 20; -fx-cursor: hand;", bgColor));
        btn.setOnMouseEntered(e -> btn.setStyle(String.format(
                "-fx-background-color: %s; -fx-background-radius: 8; "
              + "-fx-padding: 10 20; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, %s, 10, 0.5, 0, 0);",
                hoverColor, bgColor)));
        btn.setOnMouseExited(e -> btn.setStyle(String.format(
                "-fx-background-color: %s; -fx-background-radius: 8; "
              + "-fx-padding: 10 20; -fx-cursor: hand;", bgColor)));
        return btn;
    }

    /**
     * Creates the central map visualization section.
     */
    private VBox createMapSection() {
        VBox mapSection = new VBox(10);
        mapSection.setPadding(new Insets(10, 10, 10, 15));

        Label mapTitle = new Label("📍 Delivery Network Map");
        mapTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        mapTitle.setTextFill(Color.web("#e0e0e0"));

        mapCanvas = new Canvas(800, 550);
        drawEmptyMap();

        // Wrap canvas in a styled container
        StackPane canvasContainer = new StackPane(mapCanvas);
        canvasContainer.setStyle(
                "-fx-background-color: #16213e; -fx-background-radius: 12; "
              + "-fx-border-color: #0f3460; -fx-border-radius: 12; -fx-border-width: 2;");
        canvasContainer.setPadding(new Insets(10));

        mapSection.getChildren().addAll(mapTitle, canvasContainer);
        VBox.setVgrow(canvasContainer, Priority.ALWAYS);
        return mapSection;
    }

    /**
     * Creates the right-side results panel with Assignment table and cost matrix.
     */
    private VBox createResultsPanel() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(10, 15, 10, 10));
        panel.setPrefWidth(520);
        panel.setMinWidth(480);
        panel.setStyle("-fx-background-color: #16213e; -fx-background-radius: 12;");

        // ── Statistics Cards ─────────────────────────────────
        Label statsTitle = new Label("📊 Statistics");
        statsTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        statsTitle.setTextFill(Color.web("#e0e0e0"));

        HBox statsCards = new HBox(10);
        statsCards.setAlignment(Pos.CENTER);

        totalCostLabel = createStatCard("Total Cost", "₹0.00", "#e94560");
        totalDistanceLabel = createStatCard("Total Dist.", "0.00 km", "#2196f3");
        avgTimeLabel = createStatCard("Avg Time", "0 min", "#00c853");

        statsCards.getChildren().addAll(totalCostLabel, totalDistanceLabel, avgTimeLabel);

        // ── Assignment Table ─────────────────────────────────
        Label tableTitle = new Label("📋 Assignments");
        tableTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        tableTitle.setTextFill(Color.web("#e0e0e0"));

        assignmentTable = createAssignmentTable();

        // ── Cost Matrix ──────────────────────────────────────
        Label matrixTitle = new Label("🔢 Cost Matrix");
        matrixTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        matrixTitle.setTextFill(Color.web("#e0e0e0"));

        costMatrixBox = new VBox(5);
        costMatrixBox.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 8; -fx-padding: 8;");

        Label placeholder = new Label("Run optimization to see cost matrix");
        placeholder.setTextFill(Color.web("#666"));
        placeholder.setFont(Font.font("Segoe UI", 12));
        costMatrixBox.getChildren().add(placeholder);

        ScrollPane matrixScroll = new ScrollPane(costMatrixBox);
        matrixScroll.setFitToWidth(true);
        matrixScroll.setPrefHeight(180);
        matrixScroll.setStyle("-fx-background: #1a1a2e; -fx-background-color: #1a1a2e;");

        // ── Status ───────────────────────────────────────────
        statusLabel = new Label("⏳ Ready");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        statusLabel.setTextFill(Color.web("#ffc107"));

        panel.getChildren().addAll(
                statsTitle, statsCards,
                new Separator(),
                tableTitle, assignmentTable,
                new Separator(),
                matrixTitle, matrixScroll,
                statusLabel
        );
        VBox.setVgrow(assignmentTable, Priority.ALWAYS);
        return panel;
    }

    /**
     * Creates the bottom log panel.
     */
    private HBox createBottomPanel() {
        HBox bottom = new HBox(10);
        bottom.setPadding(new Insets(10, 15, 15, 15));

        Label logTitle = new Label("📝 Pipeline Log:");
        logTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        logTitle.setTextFill(Color.web("#a0a0b0"));

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(160);
        logArea.setMinHeight(140);
        logArea.setStyle(
                "-fx-control-inner-background: #0d1117; -fx-text-fill: #c9d1d9; "
              + "-fx-font-family: 'Consolas'; -fx-font-size: 12; "
              + "-fx-border-color: #30363d; -fx-border-radius: 8; -fx-background-radius: 8;");

        VBox logBox = new VBox(5, logTitle, logArea);
        HBox.setHgrow(logBox, Priority.ALWAYS);
        bottom.getChildren().add(logBox);
        return bottom;
    }

    //  TABLE & CARD HELPERS
    // 

    @SuppressWarnings("unchecked")
    private TableView<Assignment> createAssignmentTable() {
        TableView<Assignment> table = new TableView<>();
        table.setStyle(
                "-fx-background-color: #1a1a2e; -fx-table-cell-border-color: #2a2a4e; "
              + "-fx-control-inner-background: #1a1a2e; -fx-control-inner-background-alt: #16213e;");
        table.setPrefHeight(220);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<Assignment, String> whCol = new TableColumn<>("Warehouse");
        whCol.setCellValueFactory(new PropertyValueFactory<>("warehouseName"));
        whCol.setMinWidth(120);

        TableColumn<Assignment, String> custCol = new TableColumn<>("Customer");
        custCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        custCol.setMinWidth(110);

        TableColumn<Assignment, Double> distCol = new TableColumn<>("Dist (km)");
        distCol.setCellValueFactory(new PropertyValueFactory<>("distanceKm"));
        distCol.setMinWidth(85);

        TableColumn<Assignment, Double> costCol = new TableColumn<>("Cost (Rs)");
        costCol.setCellValueFactory(new PropertyValueFactory<>("deliveryCost"));
        costCol.setMinWidth(85);

        TableColumn<Assignment, Double> timeCol = new TableColumn<>("Time (min)");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("estimatedTimeMinutes"));
        timeCol.setMinWidth(90);

        table.getColumns().addAll(whCol, custCol, distCol, costCol, timeCol);
        return table;
    }

    private Label createStatCard(String title, String value, String color) {
        Label card = new Label(title + "\n" + value);
        card.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        card.setTextFill(Color.web(color));
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(150);
        card.setMinWidth(130);
        card.setPrefHeight(60);
        card.setWrapText(true);
        card.setStyle(
                "-fx-background-color: #1a1a2e; -fx-background-radius: 10; "
              + "-fx-border-color: " + color + "; -fx-border-radius: 10; -fx-border-width: 1;"
              + "-fx-padding: 8;");
        return card;
    }

    // 
    //  MAP DRAWING
    // 

    /**
     * Draws the empty map background with grid lines.
     */
    private void drawEmptyMap() {
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        double w = mapCanvas.getWidth();
        double h = mapCanvas.getHeight();

        // Background
        gc.setFill(Color.web("#0d1117"));
        gc.fillRect(0, 0, w, h);

        // Grid lines
        gc.setStroke(Color.web("#21262d"));
        gc.setLineWidth(0.5);
        for (int i = 0; i <= 10; i++) {
            gc.strokeLine(i * w / 10, 0, i * w / 10, h);
            gc.strokeLine(0, i * h / 10, w, i * h / 10);
        }

        // Legend
        gc.setFill(Color.web("#8b949e"));
        gc.setFont(Font.font("Segoe UI", 11));
        gc.fillText("🟦 Warehouse   🟠 Customer   ━━ Assignment", 15, h - 15);
    }

    /**
     * Draws the full map with warehouses, customers, and assignment lines.
     */
    private void drawMap(List<Warehouse> warehouses, List<Customer> customers,
                         List<Assignment> assignments) {
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        double w = mapCanvas.getWidth();
        double h = mapCanvas.getHeight();

        // Clear and redraw background
        drawEmptyMap();

        // ── Draw assignment lines ────────────────────────────
        if (assignments != null) {
            for (int idx = 0; idx < assignments.size(); idx++) {
                Assignment a = assignments.get(idx);

                // Find warehouse and customer coordinates
                Warehouse wh = findWarehouse(warehouses, a.getWarehouseId());
                Customer cu = findCustomer(customers, a.getCustomerId());
                if (wh == null || cu == null) continue;

                double x1 = lonToX(wh.getLongitude(), w);
                double y1 = latToY(wh.getLatitude(), h);
                double x2 = lonToX(cu.getLongitude(), w);
                double y2 = latToY(cu.getLatitude(), h);

                Color lineColor = ASSIGNMENT_COLORS[idx % ASSIGNMENT_COLORS.length];
                gc.setStroke(lineColor);
                gc.setLineWidth(2.5);
                gc.setGlobalAlpha(0.7);
                gc.strokeLine(x1, y1, x2, y2);
                gc.setGlobalAlpha(1.0);

                // Distance label on the line midpoint
                double mx = (x1 + x2) / 2;
                double my = (y1 + y2) / 2;
                gc.setFill(Color.web("#e0e0e0"));
                gc.setFont(Font.font("Segoe UI", 9));
                gc.fillText(String.format("%.1fkm", a.getDistanceKm()), mx + 3, my - 3);
            }
        }

        // ── Draw warehouse markers ───────────────────────────
        if (warehouses != null) {
            for (Warehouse wh : warehouses) {
                double x = lonToX(wh.getLongitude(), w);
                double y = latToY(wh.getLatitude(), h);

                // Outer glow
                gc.setFill(Color.rgb(33, 150, 243, 0.3));
                gc.fillOval(x - 16, y - 16, 32, 32);

                // Main marker
                gc.setFill(Color.rgb(33, 150, 243));
                gc.fillRect(x - 10, y - 10, 20, 20);

                // Border
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(1.5);
                gc.strokeRect(x - 10, y - 10, 20, 20);

                // Label
                gc.setFill(Color.web("#90caf9"));
                gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
                gc.fillText(wh.getName(), x + 14, y + 4);
            }
        }

        // ── Draw customer markers ────────────────────────────
        if (customers != null) {
            for (Customer cu : customers) {
                double x = lonToX(cu.getLongitude(), w);
                double y = latToY(cu.getLatitude(), h);

                // Outer glow based on priority
                Color priorityColor = getPriorityColor(cu.getPriority());
                gc.setFill(priorityColor.deriveColor(0, 1, 1, 0.3));
                gc.fillOval(x - 14, y - 14, 28, 28);

                // Main marker (circle)
                gc.setFill(priorityColor);
                gc.fillOval(x - 8, y - 8, 16, 16);

                // Border
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(1.5);
                gc.strokeOval(x - 8, y - 8, 16, 16);

                // Label
                gc.setFill(Color.web("#ffcc80"));
                gc.setFont(Font.font("Segoe UI", 9));
                gc.fillText(cu.getName(), x + 12, y + 4);
            }
        }
    }

    /**
     * Gets color based on customer priority level.
     */
    private Color getPriorityColor(String priority) {
        if (priority == null) return Color.GRAY;
        switch (priority) {
            case "URGENT": return Color.rgb(244, 67, 54);    // Red
            case "HIGH":   return Color.rgb(255, 152, 0);    // Orange
            case "MEDIUM": return Color.rgb(255, 235, 59);   // Yellow
            case "LOW":    return Color.rgb(76, 175, 80);    // Green
            default:       return Color.GRAY;
        }
    }

    // ── Coordinate conversion (lat/lon → canvas x/y) ─────────

    private double lonToX(double lon, double canvasWidth) {
        return ((lon - MIN_LON) / (MAX_LON - MIN_LON)) * (canvasWidth - 60) + 30;
    }

    private double latToY(double lat, double canvasHeight) {
        // Invert Y axis (latitude increases northward, canvas Y increases downward)
        return canvasHeight - ((lat - MIN_LAT) / (MAX_LAT - MIN_LAT)) * (canvasHeight - 60) - 30;
    }

    // 
    //  PIPELINE ACTIONS
    // 

    /**
     * Runs the full optimization pipeline in a background thread.
     */
    private void runOptimization() {
        statusLabel.setText("⏳ Running optimization pipeline...");
        statusLabel.setTextFill(Color.web("#ffc107"));
        log("Starting optimization pipeline...");

        Thread optimizationThread = new Thread(() -> {
            try {
                // Auto-reset before running so customers are back to PENDING
                optimizer.reset();
                log("Auto-reset: all customers set to PENDING.");

                List<Assignment> results = optimizer.runOptimization();

                Platform.runLater(() -> {
                    if (results.isEmpty()) {
                        statusLabel.setText("✗ No data found. Check database.");
                        statusLabel.setTextFill(Color.web("#f44336"));
                        log("ERROR: No warehouses or customers found in database.");
                        return;
                    }

                    // Update the map
                    drawMap(optimizer.getWarehouses(), optimizer.getCustomers(), results);

                    // Update the table
                    ObservableList<Assignment> tableData = FXCollections.observableArrayList(results);
                    assignmentTable.setItems(tableData);

                    // Update statistics
                    updateStatistics(results);

                    // Update cost matrix display
                    updateCostMatrixDisplay();

                    // Update status
                    statusLabel.setText("✓ Optimization complete! " + results.size() + " assignments made.");
                    statusLabel.setTextFill(Color.web("#00c853"));
                    log("Pipeline completed: " + results.size() + " optimal assignments.");
                });

            } catch (Exception ex) {
                Platform.runLater(() -> {
                    statusLabel.setText("✗ Error: " + ex.getMessage());
                    statusLabel.setTextFill(Color.web("#f44336"));
                    log("ERROR: " + ex.getMessage());
                });
                ex.printStackTrace();
            }
        });
        optimizationThread.setDaemon(true);
        optimizationThread.start();
    }

    /**
     * Resets the system state and UI.
     */
    private void resetSystem() {
        optimizer.reset();
        drawEmptyMap();
        assignmentTable.getItems().clear();
        costMatrixBox.getChildren().clear();
        Label placeholder = new Label("Run optimization to see cost matrix");
        placeholder.setTextFill(Color.web("#666"));
        costMatrixBox.getChildren().add(placeholder);

        totalCostLabel.setText("Total Cost\n₹0.00");
        totalDistanceLabel.setText("Total Dist.\n0.00 km");
        avgTimeLabel.setText("Avg Time\n0 min");

        statusLabel.setText("↺ System reset. Ready for new run.");
        statusLabel.setTextFill(Color.web("#ffc107"));
        log("System reset. All assignments cleared.");
    }

    /**
     * Simulates real-time order allocation with visual animation.
     * Runs the pipeline and reveals assignments one-by-one with delays.
     */
    private void simulateRealTime() {
        statusLabel.setText("⟳ Starting real-time simulation...");
        statusLabel.setTextFill(Color.web("#2196f3"));
        log("Starting real-time simulation...");

        // First reset
        optimizer.reset();
        drawEmptyMap();
        assignmentTable.getItems().clear();

        Thread simulationThread = new Thread(() -> {
            try {
                List<Assignment> results = optimizer.runOptimization();

                if (results.isEmpty()) {
                    Platform.runLater(() -> {
                        statusLabel.setText("✗ No data for simulation.");
                        statusLabel.setTextFill(Color.web("#f44336"));
                    });
                    return;
                }

                // Draw warehouses and customers first (no assignment lines)
                Platform.runLater(() -> {
                    drawMap(optimizer.getWarehouses(), optimizer.getCustomers(), List.of());
                    log("Loaded " + optimizer.getWarehouses().size() + " warehouses and "
                      + optimizer.getCustomers().size() + " customers on map.");
                });
                Thread.sleep(1500);

                // Reveal assignments one by one
                for (int i = 0; i < results.size(); i++) {
                    final int idx = i;
                    final List<Assignment> partialAssignments = results.subList(0, i + 1);

                    Platform.runLater(() -> {
                        Assignment a = results.get(idx);
                        drawMap(optimizer.getWarehouses(), optimizer.getCustomers(), partialAssignments);

                        assignmentTable.getItems().add(a);
                        updateStatistics(partialAssignments);

                        log(String.format("Assigned: %s → %s (%.2f km, ₹%.2f)",
                                a.getWarehouseName(), a.getCustomerName(),
                                a.getDistanceKm(), a.getDeliveryCost()));

                        statusLabel.setText(String.format("⟳ Simulating... %d/%d assignments",
                                idx + 1, results.size()));
                    });

                    Thread.sleep(1200);  // Delay between each assignment
                }

                // Update cost matrix after simulation
                Platform.runLater(() -> {
                    updateCostMatrixDisplay();
                    statusLabel.setText("✓ Simulation complete!");
                    statusLabel.setTextFill(Color.web("#00c853"));
                    log("Real-time simulation finished.");
                });

            } catch (Exception ex) {
                Platform.runLater(() -> {
                    statusLabel.setText("✗ Simulation error: " + ex.getMessage());
                    statusLabel.setTextFill(Color.web("#f44336"));
                    log("SIMULATION ERROR: " + ex.getMessage());
                });
                ex.printStackTrace();
            }
        });
        simulationThread.setDaemon(true);
        simulationThread.start();
    }

    // 
    //  UPDATE HELPERS
    // 

    /**
     * Updates the statistics cards with computed totals.
     */
    private void updateStatistics(List<Assignment> assignments) {
        double totalCost = 0, totalDist = 0, totalTime = 0;
        for (Assignment a : assignments) {
            totalCost += a.getDeliveryCost();
            totalDist += a.getDistanceKm();
            totalTime += a.getEstimatedTimeMinutes();
        }
        double avgTime = assignments.isEmpty() ? 0 : totalTime / assignments.size();

        totalCostLabel.setText(String.format("Total Cost\n₹%.2f", totalCost));
        totalDistanceLabel.setText(String.format("Total Dist.\n%.2f km", totalDist));
        avgTimeLabel.setText(String.format("Avg Time\n%.0f min", avgTime));
    }

    /**
     * Updates the cost matrix visual display.
     */
    private void updateCostMatrixDisplay() {
        double[][] matrix = optimizer.getCostMatrix();
        List<Warehouse> warehouses = optimizer.getWarehouses();
        List<Customer> customers = optimizer.getCustomers();

        if (matrix == null || warehouses == null || customers == null) return;

        costMatrixBox.getChildren().clear();

        // Header row
        StringBuilder header = new StringBuilder(String.format("%-14s", ""));
        for (Customer c : customers) {
            String shortName = c.getName().length() > 8
                    ? c.getName().substring(0, 8) : c.getName();
            header.append(String.format("%-10s", shortName));
        }
        Label headerLabel = new Label(header.toString());
        headerLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 10));
        headerLabel.setTextFill(Color.web("#90caf9"));
        costMatrixBox.getChildren().add(headerLabel);

        // Data rows
        for (int i = 0; i < warehouses.size(); i++) {
            StringBuilder row = new StringBuilder(
                    String.format("%-14s", warehouses.get(i).getName()));
            for (int j = 0; j < customers.size(); j++) {
                row.append(String.format("%-10.2f", matrix[i][j]));
            }
            Label rowLabel = new Label(row.toString());
            rowLabel.setFont(Font.font("Consolas", 10));
            rowLabel.setTextFill(Color.web("#c9d1d9"));
            costMatrixBox.getChildren().add(rowLabel);
        }
    }

    // 
    //  UTILITY METHODS
    // 

    private void log(String message) {
        Platform.runLater(() -> {
            String time = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            logArea.appendText("[" + time + "] " + message + "\n");
            logArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    private Warehouse findWarehouse(List<Warehouse> list, int id) {
        return list.stream().filter(w -> w.getWarehouseId() == id).findFirst().orElse(null);
    }

    private Customer findCustomer(List<Customer> list, int id) {
        return list.stream().filter(c -> c.getCustomerId() == id).findFirst().orElse(null);
    }

    // 
    //  USER INPUT DIALOGS
    // 

    /**
     * Shows a dialog to add a new warehouse to the database.
     */
    private void showAddWarehouseDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Warehouse");
        dialog.setHeaderText("Enter warehouse details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Airport Hub");
        TextField latField = new TextField();
        latField.setPromptText("28.50 — 28.75");
        TextField lonField = new TextField();
        lonField.setPromptText("77.05 — 77.40");
        TextField capField = new TextField();
        capField.setPromptText("e.g. 100");
        capField.setText("100");
        TextField costField = new TextField();
        costField.setPromptText("e.g. 1.5");
        costField.setText("1.5");

        grid.add(new Label("Name:"), 0, 0);       grid.add(nameField, 1, 0);
        grid.add(new Label("Latitude:"), 0, 1);    grid.add(latField, 1, 1);
        grid.add(new Label("Longitude:"), 0, 2);   grid.add(lonField, 1, 2);
        grid.add(new Label("Capacity:"), 0, 3);    grid.add(capField, 1, 3);
        grid.add(new Label("Cost/km:"), 0, 4);     grid.add(costField, 1, 4);

        Label rangeHint = new Label("⚠ Delhi region — Lat: 28.50 to 28.75, Lon: 77.05 to 77.40");
        rangeHint.setStyle("-fx-text-fill: #ff9800; -fx-font-size: 11;");
        grid.add(rangeHint, 0, 5, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String name = nameField.getText().trim();
                    double lat = Double.parseDouble(latField.getText().trim());
                    double lon = Double.parseDouble(lonField.getText().trim());
                    int cap = Integer.parseInt(capField.getText().trim());
                    double cost = Double.parseDouble(costField.getText().trim());

                    if (name.isEmpty()) { showError("Name cannot be empty"); return; }

                    if (optimizer.getDbManager().getConnection() == null) {
                        showError("Database not connected. Cannot add warehouse.");
                        return;
                    }
                    boolean ok = optimizer.getDbManager().insertWarehouse(name, lat, lon, cap, cost);
                    if (ok) {
                        log("Added warehouse: " + name + " (" + lat + ", " + lon + ")");
                        showInfo("Warehouse '" + name + "' added! Click Reset then Run Optimization to include it.");
                    } else {
                        showError("Failed to insert warehouse. Check database connection.");
                    }
                } catch (NumberFormatException ex) {
                    showError("Invalid number format. Please check Latitude, Longitude, Capacity, and Cost.");
                }
            }
        });
    }

    /**
     * Shows a dialog to add a new customer to the database.
     */
    private void showAddCustomerDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Customer");
        dialog.setHeaderText("Enter customer details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. John Doe");
        TextField latField = new TextField();
        latField.setPromptText("28.50 — 28.75");
        TextField lonField = new TextField();
        lonField.setPromptText("77.05 — 77.40");
        TextField demandField = new TextField();
        demandField.setPromptText("e.g. 3");
        demandField.setText("1");

        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("LOW", "MEDIUM", "HIGH", "URGENT");
        priorityBox.setValue("MEDIUM");

        grid.add(new Label("Name:"), 0, 0);       grid.add(nameField, 1, 0);
        grid.add(new Label("Latitude:"), 0, 1);    grid.add(latField, 1, 1);
        grid.add(new Label("Longitude:"), 0, 2);   grid.add(lonField, 1, 2);
        grid.add(new Label("Demand:"), 0, 3);      grid.add(demandField, 1, 3);
        grid.add(new Label("Priority:"), 0, 4);    grid.add(priorityBox, 1, 4);

        Label rangeHint = new Label("⚠ Delhi region — Lat: 28.50 to 28.75, Lon: 77.05 to 77.40");
        rangeHint.setStyle("-fx-text-fill: #ff9800; -fx-font-size: 11;");
        grid.add(rangeHint, 0, 5, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String name = nameField.getText().trim();
                    double lat = Double.parseDouble(latField.getText().trim());
                    double lon = Double.parseDouble(lonField.getText().trim());
                    int demand = Integer.parseInt(demandField.getText().trim());
                    String priority = priorityBox.getValue();

                    if (name.isEmpty()) { showError("Name cannot be empty"); return; }

                    if (optimizer.getDbManager().getConnection() == null) {
                        showError("Database not connected. Cannot add customer.");
                        return;
                    }
                    boolean ok = optimizer.getDbManager().insertCustomer(name, lat, lon, demand, priority);
                    if (ok) {
                        log("Added customer: " + name + " [" + priority + "] (" + lat + ", " + lon + ")");
                        showInfo("Customer '" + name + "' added! Click Reset then Run Optimization to include them.");
                    } else {
                        showError("Failed to insert customer. Check database connection.");
                    }
                } catch (NumberFormatException ex) {
                    showError("Invalid number format. Please check Latitude, Longitude, and Demand.");
                }
            }
        });
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showError(String msg) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // 
    //  MAIN ENTRY POINT
    // 

    public static void main(String[] args) {
        launch(args);
    }
}
