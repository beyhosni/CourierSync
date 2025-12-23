CREATE TABLE pricing_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,

    -- Rule details
    rule_type VARCHAR(50) NOT NULL CHECK (rule_type IN ('BASE_RATE', 'PER_KM_RATE', 'URGENT_SURCHARGE', 'AFTER_HOURS_SURCHARGE', 'WEEKEND_SURCHARGE', 'WEIGHT_SURCHARGE', 'DISTANCE_SURCHARGE', 'CUSTOM')),
    value DECIMAL(10, 2) NOT NULL,
    unit VARCHAR(20) DEFAULT 'FLAT',

    -- Conditions
    customer_id UUID, -- NULL for global rules
    customer_type VARCHAR(50), -- INDIVIDUAL, BUSINESS, MEDICAL_FACILITY
    priority_level VARCHAR(20), -- LOW, NORMAL, HIGH, URGENT
    min_distance_km DECIMAL(10, 2),
    max_distance_km DECIMAL(10, 2),
    min_weight_kg DECIMAL(10, 2),
    max_weight_kg DECIMAL(10, 2),
    time_of_day_start TIME,
    time_of_day_end TIME,
    day_of_week VARCHAR(20), -- MONDAY, TUESDAY, etc. or WEEKDAY, WEEKEND

    -- Status
    active BOOLEAN DEFAULT true,

    -- Metadata
    created_by UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    valid_from DATE,
    valid_until DATE
);

CREATE INDEX idx_pricing_rules_type ON pricing_rules(rule_type);
CREATE INDEX idx_pricing_rules_customer ON pricing_rules(customer_id);
CREATE INDEX idx_pricing_rules_active ON pricing_rules(active);
CREATE INDEX idx_pricing_rules_validity ON pricing_rules(valid_from, valid_until);
