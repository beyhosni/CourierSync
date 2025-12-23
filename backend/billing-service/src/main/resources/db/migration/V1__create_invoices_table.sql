CREATE TABLE invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id UUID NOT NULL,

    -- Invoice details
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'SENT', 'PAID', 'OVERDUE', 'CANCELLED')),
    subtotal DECIMAL(10, 2) NOT NULL,
    tax_rate DECIMAL(5, 4) NOT NULL,
    tax_amount DECIMAL(10, 2) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',

    -- Customer details (snapshot at time of invoice)
    customer_name VARCHAR(255) NOT NULL,
    customer_address VARCHAR(500),
    customer_city VARCHAR(100),
    customer_postal_code VARCHAR(20),
    customer_country VARCHAR(100),

    -- Payment details
    payment_method VARCHAR(50),
    payment_date DATE,
    payment_reference VARCHAR(100),

    -- Metadata
    created_by UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,
    notes TEXT
);

CREATE INDEX idx_invoices_customer ON invoices(customer_id);
CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_invoices_issue_date ON invoices(issue_date);
CREATE INDEX idx_invoices_number ON invoices(invoice_number);
