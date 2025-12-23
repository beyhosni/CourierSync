CREATE TABLE delivery_orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id UUID NOT NULL,

    -- Pickup information
    pickup_name VARCHAR(255) NOT NULL,
    pickup_address VARCHAR(500) NOT NULL,
    pickup_city VARCHAR(100) NOT NULL,
    pickup_postal_code VARCHAR(20) NOT NULL,
    pickup_latitude DECIMAL(10, 8),
    pickup_longitude DECIMAL(11, 8),
    pickup_contact_name VARCHAR(100),
    pickup_contact_phone VARCHAR(20),
    pickup_notes TEXT,

    -- Dropoff information
    dropoff_name VARCHAR(255) NOT NULL,
    dropoff_address VARCHAR(500) NOT NULL,
    dropoff_city VARCHAR(100) NOT NULL,
    dropoff_postal_code VARCHAR(20) NOT NULL,
    dropoff_latitude DECIMAL(10, 8),
    dropoff_longitude DECIMAL(11, 8),
    dropoff_contact_name VARCHAR(100),
    dropoff_contact_phone VARCHAR(20),
    dropoff_notes TEXT,

    -- Delivery details
    priority VARCHAR(20) DEFAULT 'NORMAL' CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
    status VARCHAR(20) DEFAULT 'CREATED' CHECK (status IN ('CREATED', 'ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED')),
    package_description TEXT,
    package_weight DECIMAL(10, 2),
    is_medical_specimen BOOLEAN DEFAULT TRUE,
    temperature_controlled BOOLEAN DEFAULT FALSE,

    -- Timing
    requested_pickup_time TIMESTAMP,
    actual_pickup_time TIMESTAMP,
    estimated_delivery_time TIMESTAMP,
    actual_delivery_time TIMESTAMP,

    -- Assignment
    assigned_driver_id UUID,
    assigned_at TIMESTAMP,

    -- Metadata
    created_by UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancellation_reason TEXT
);

CREATE INDEX idx_orders_status ON delivery_orders(status);
CREATE INDEX idx_orders_driver ON delivery_orders(assigned_driver_id);
CREATE INDEX idx_orders_customer ON delivery_orders(customer_id);
CREATE INDEX idx_orders_created ON delivery_orders(created_at DESC);
CREATE INDEX idx_orders_number ON delivery_orders(order_number);
