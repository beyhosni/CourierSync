# Modèle de Données - CourierSync

## 1. Vue d'ensemble

CourierSync utilise une approche **polyglotte** avec plusieurs bases de données adaptées aux besoins spécifiques.

## 2. User Auth Service - PostgreSQL (users_db)

### 2.1 Table: users
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'DISPATCHER', 'DRIVER', 'FINANCE')),
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    CONSTRAINT email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);
```

### 2.2 Table: refresh_tokens
```sql
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    revoked BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
```

### 2.3 Table: audit_logs
```sql
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50),
    resource_id VARCHAR(100),
    ip_address INET,
    user_agent TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    details JSONB
);

CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp DESC);
CREATE INDEX idx_audit_action ON audit_logs(action);
```

## 3. Dispatch Service - PostgreSQL (dispatch_db)

### 3.1 Table: delivery_orders
```sql
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
```

### 3.2 Table: drivers
```sql
CREATE TABLE drivers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL,
    
    -- Driver information
    license_number VARCHAR(50) UNIQUE NOT NULL,
    license_expiry_date DATE NOT NULL,
    
    -- Vehicle information
    vehicle_type VARCHAR(50) CHECK (vehicle_type IN ('CAR', 'VAN', 'MOTORCYCLE', 'BIKE')),
    vehicle_plate VARCHAR(20),
    vehicle_model VARCHAR(100),
    
    -- Status
    status VARCHAR(20) DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'ON_DUTY', 'OFF_DUTY', 'BREAK')),
    current_latitude DECIMAL(10, 8),
    current_longitude DECIMAL(11, 8),
    last_location_update TIMESTAMP,
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_drivers_status ON drivers(status);
CREATE INDEX idx_drivers_user ON drivers(user_id);
```

### 3.3 Table: delivery_status_history
```sql
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
```

## 4. Tracking Service - MongoDB (tracking_db)

### 4.1 Collection: location_updates
```javascript
{
  _id: ObjectId(),
  driverId: "uuid",
  deliveryId: "uuid",
  location: {
    type: "Point",
    coordinates: [longitude, latitude] // GeoJSON format
  },
  accuracy: 10.5,
  altitude: 45.2,
  speed: 35.0,
  heading: 180.0,
  timestamp: ISODate("2024-01-15T10:30:00Z"),
  batteryLevel: 85,
  metadata: {
    deviceId: "string",
    appVersion: "1.0.0",
    networkType: "4G"
  }
}

// Indexes
db.location_updates.createIndex({ "driverId": 1, "timestamp": -1 });
db.location_updates.createIndex({ "deliveryId": 1, "timestamp": -1 });
db.location_updates.createIndex({ "location": "2dsphere" });
db.location_updates.createIndex({ "timestamp": 1 }, { expireAfterSeconds: 2592000 }); // 30 days TTL
```

### 4.2 Collection: delivery_routes
```javascript
{
  _id: ObjectId(),
  deliveryId: "uuid",
  driverId: "uuid",
  startTime: ISODate("2024-01-15T10:00:00Z"),
  endTime: ISODate("2024-01-15T11:30:00Z"),
  totalDistance: 25.5, // km
  totalDuration: 5400, // seconds
  route: {
    type: "LineString",
    coordinates: [
      [2.3522, 48.8566],
      [2.3530, 48.8570],
      // ... more points
    ]
  },
  stops: [
    {
      type: "PICKUP",
      location: [2.3522, 48.8566],
      timestamp: ISODate("2024-01-15T10:15:00Z"),
      duration: 300 // seconds
    },
    {
      type: "DROPOFF",
      location: [2.3650, 48.8700],
      timestamp: ISODate("2024-01-15T11:25:00Z"),
      duration: 180
    }
  ]
}

db.delivery_routes.createIndex({ "deliveryId": 1 });
db.delivery_routes.createIndex({ "driverId": 1, "startTime": -1 });
```

## 5. Billing Service - PostgreSQL (billing_db)

### 5.1 Table: invoices
```sql
CREATE TABLE invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_number VARCHAR(50) UNIQUE NOT NULL,
    delivery_id UUID UNIQUE NOT NULL,
    customer_id UUID NOT NULL,
    
    -- Amounts
    base_amount DECIMAL(10, 2) NOT NULL,
    distance_amount DECIMAL(10, 2) NOT NULL,
    priority_surcharge DECIMAL(10, 2) DEFAULT 0,
    sla_premium DECIMAL(10, 2) DEFAULT 0,
    tax_amount DECIMAL(10, 2) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    
    -- Calculation details
    distance_km DECIMAL(10, 2),
    duration_minutes INTEGER,
    
    -- Status
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SENT', 'PAID', 'OVERDUE', 'CANCELLED')),
    
    -- Dates
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    paid_date DATE,
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT
);

CREATE INDEX idx_invoices_delivery ON invoices(delivery_id);
CREATE INDEX idx_invoices_customer ON invoices(customer_id);
CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_invoices_issue_date ON invoices(issue_date DESC);
```

### 5.2 Table: pricing_rules
```sql
CREATE TABLE pricing_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    
    -- Base pricing
    base_price DECIMAL(10, 2) NOT NULL,
    price_per_km DECIMAL(10, 2) NOT NULL,
    
    -- Multipliers
    priority_multipliers JSONB NOT NULL, -- {"NORMAL": 1.0, "HIGH": 1.5, "URGENT": 2.0}
    
    -- Time-based
    after_hours_multiplier DECIMAL(5, 2) DEFAULT 1.0,
    weekend_multiplier DECIMAL(5, 2) DEFAULT 1.0,
    
    -- Tax
    tax_rate DECIMAL(5, 4) NOT NULL,
    
    -- Validity
    effective_from DATE NOT NULL,
    effective_to DATE,
    
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pricing_rules_active ON pricing_rules(is_active, effective_from);
```

### 5.3 Table: payments
```sql
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id UUID NOT NULL REFERENCES invoices(id),
    
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(50) CHECK (payment_method IN ('CREDIT_CARD', 'BANK_TRANSFER', 'CASH', 'CHECK')),
    
    transaction_id VARCHAR(255),
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')),
    
    payment_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    notes TEXT
);

CREATE INDEX idx_payments_invoice ON payments(invoice_id);
CREATE INDEX idx_payments_status ON payments(status);
```

## 6. Cache Layer - Redis

### 6.1 Structure des clés

**Sessions utilisateur** :
```
session:{userId}:{sessionId} -> {userData}
TTL: 15 minutes
```

**JWT Blacklist** :
```
blacklist:token:{jti} -> true
TTL: token expiration
```

**Driver locations (cache)** :
```
driver:location:{driverId} -> {lat, lng, timestamp}
TTL: 2 minutes
```

**Active deliveries (cache)** :
```
delivery:active:{deliveryId} -> {status, driverId, eta}
TTL: Until completion
```

## 7. Relations entre services

### 7.1 Références cross-database

```
User (users_db)
  ├─→ Driver (dispatch_db) [user_id]
  ├─→ DeliveryOrder (dispatch_db) [created_by]
  └─→ AuditLog (users_db) [user_id]

Driver (dispatch_db)
  ├─→ LocationUpdate (tracking_db) [driverId]
  └─→ DeliveryOrder (dispatch_db) [assigned_driver_id]

DeliveryOrder (dispatch_db)
  ├─→ LocationUpdate (tracking_db) [deliveryId]
  ├─→ DeliveryRoute (tracking_db) [deliveryId]
  ├─→ Invoice (billing_db) [delivery_id]
  └─→ StatusHistory (dispatch_db) [delivery_id]

Invoice (billing_db)
  └─→ Payment (billing_db) [invoice_id]
```

### 7.2 Cohérence des données

**Problème** : Pas de clés étrangères cross-database

**Solutions** :
1. **Validation applicative** : Vérifier l'existence avant insert
2. **Events Kafka** : Synchronisation éventuelle
3. **Saga Pattern** : Transactions distribuées avec compensation

## 8. Stratégie de migration

### 8.1 Flyway (PostgreSQL)
```
backend/*/src/main/resources/db/migration/
  V1__create_users_table.sql
  V2__create_refresh_tokens_table.sql
  V3__create_audit_logs_table.sql
```

### 8.2 MongoDB Migrations
Utilisation de scripts Node.js ou mongosh pour évolution du schéma.

## 9. Backup & Retention

| Database | Backup Frequency | Retention |
|----------|------------------|----------|
| PostgreSQL | Daily (full) + Hourly (incremental) | 30 days |
| MongoDB | Daily | 30 days |
| Redis | Snapshot toutes les 15 min | 1 day |

## 10. Volumétrie estimée (1 an)

| Table/Collection | Croissance estimée |
|------------------|-------------------|
| users | 1,000 utilisateurs |
| delivery_orders | 500,000 commandes |
| location_updates | 50M positions |
| invoices | 500,000 factures |
| audit_logs | 10M entrées |

---

**Version** : 1.0  
**Dernière mise à jour** : 2024