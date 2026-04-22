
CREATE DATABASE IF NOT EXISTS delivery_optimizer;
USE delivery_optimizer;


DROP TABLE IF EXISTS assignments;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS warehouses;

CREATE TABLE warehouses (
    warehouse_id    INT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(100) NOT NULL,
    latitude        DOUBLE NOT NULL,
    longitude       DOUBLE NOT NULL,
    capacity        INT NOT NULL DEFAULT 100,       
    cost_per_km     DOUBLE NOT NULL DEFAULT 1.5,    
    status          ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;


CREATE TABLE customers (
    customer_id     INT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(100) NOT NULL,
    latitude        DOUBLE NOT NULL,
    longitude       DOUBLE NOT NULL,
    demand          INT NOT NULL DEFAULT 1,         
    priority        ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
    status          ENUM('PENDING', 'ASSIGNED', 'DELIVERED') DEFAULT 'PENDING',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;


CREATE TABLE assignments (
    assignment_id   INT PRIMARY KEY AUTO_INCREMENT,
    warehouse_id    INT NOT NULL,
    customer_id     INT NOT NULL,
    distance_km     DOUBLE NOT NULL,
    delivery_cost   DOUBLE NOT NULL,
    estimated_time  DOUBLE NOT NULL,    -- in minutes
    assigned_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(warehouse_id)
        ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

INSERT INTO warehouses (name, latitude, longitude, capacity, cost_per_km) VALUES
    ('Central Hub',       28.6139, 77.2090, 150, 1.2),   -- Delhi center
    ('North Depot',       28.7041, 77.1025, 120, 1.5),   -- North Delhi
    ('South Terminal',    28.5245, 77.1855, 130, 1.3),   -- South Delhi
    ('East Warehouse',    28.6280, 77.2950, 100, 1.4),   -- East Delhi
    ('West Distribution', 28.6508, 77.0969, 110, 1.6);   -- West Delhi


INSERT INTO customers (name, latitude, longitude, demand, priority) VALUES
    ('Rajesh Kumar',     28.6350, 77.2250, 3, 'HIGH'),
    ('Priya Sharma',     28.6800, 77.1500, 2, 'MEDIUM'),
    ('Amit Patel',       28.5500, 77.2100, 4, 'URGENT'),
    ('Sneha Gupta',      28.6100, 77.3100, 1, 'LOW'),
    ('Vikram Singh',     28.7100, 77.0800, 2, 'HIGH'),
    ('Anita Reddy',      28.5400, 77.1600, 3, 'MEDIUM'),
    ('Rohit Mehta',      28.6700, 77.2500, 2, 'HIGH'),
    ('Kavita Joshi',     28.5900, 77.1200, 1, 'LOW'),
    ('Suresh Nair',      28.6450, 77.3300, 5, 'URGENT'),
    ('Deepika Verma',    28.6900, 77.1900, 2, 'MEDIUM');


CREATE INDEX idx_warehouse_coords ON warehouses(latitude, longitude);
CREATE INDEX idx_customer_coords ON customers(latitude, longitude);
CREATE INDEX idx_customer_status ON customers(status);
CREATE INDEX idx_assignment_lookup ON assignments(warehouse_id, customer_id);
