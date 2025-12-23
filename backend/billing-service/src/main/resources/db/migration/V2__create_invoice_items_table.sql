CREATE TABLE invoice_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,

    -- Item details
    item_type VARCHAR(50) NOT NULL CHECK (item_type IN ('DELIVERY', 'SURCHARGE', 'DISCOUNT', 'OTHER')),
    description VARCHAR(500) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL,
    discount_percent DECIMAL(5, 2) DEFAULT 0,
    line_total DECIMAL(10, 2) NOT NULL,

    -- Reference to related delivery (if applicable)
    delivery_id UUID,

    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_invoice_items_invoice ON invoice_items(invoice_id);
CREATE INDEX idx_invoice_items_delivery ON invoice_items(delivery_id);
CREATE INDEX idx_invoice_items_type ON invoice_items(item_type);
