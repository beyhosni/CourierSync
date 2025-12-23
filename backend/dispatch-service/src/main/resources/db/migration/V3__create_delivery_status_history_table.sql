CREATE TABLE delivery_status_history (
    id BIGSERIAL PRIMARY KEY,
    delivery_id UUID NOT NULL REFERENCES delivery_orders(id) ON DELETE CASCADE,
    from_status VARCHAR(20),
    to_status VARCHAR(20) NOT NULL,
    changed_by UUID,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    location_latitude DECIMAL(10, 8),
    location_longitude DECIMAL(11, 8)
);

CREATE INDEX idx_status_history_delivery ON delivery_status_history(delivery_id);
CREATE INDEX idx_status_history_timestamp ON delivery_status_history(changed_at DESC);
