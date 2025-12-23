# Architecture Système - CourierSync

## 1. Vue d'ensemble

CourierSync utilise une **architecture microservices** avec séparation claire des responsabilités, permettant :
- Scalabilité indépendante de chaque composant
- Déploiement continu sans interruption
- Résilience et isolation des pannes
- Évolution technologique par service

## 2. Principes architecturaux

### 2.1 Domain-Driven Design (DDD)
- Bounded Contexts clairs
- Ubiquitous Language
- Agrégats et Entités bien définis

### 2.2 Microservices Patterns
- ✅ API Gateway Pattern
- ✅ Database per Service
- ✅ Event-Driven Architecture
- ✅ Circuit Breaker (Resilience4j)
- ✅ Service Discovery (futur : Eureka/Consul)
- ✅ Distributed Tracing (OpenTelemetry)

### 2.3 Clean Architecture
Chaque microservice suit :
```
┌────────────────────────────────────┐
│   Presentation Layer (Controllers) │
├────────────────────────────────────┤
│   Application Layer (Use Cases)    │
├────────────────────────────────────┤
│   Domain Layer (Business Logic)    │
├────────────────────────────────────┤
│   Infrastructure Layer (Persistence)│
└────────────────────────────────────┘
```

## 3. Microservices détaillés

### 3.1 API Gateway (Port 8080)

**Responsabilités** :
- Point d'entrée unique pour tous les clients
- Routage vers les microservices
- Authentification JWT
- Rate limiting
- CORS configuration
- Request/Response logging

**Technologies** :
- Spring Cloud Gateway
- Spring Security OAuth2 Resource Server
- Redis (rate limiting)

**Routes** :
```
/api/auth/**      → User Auth Service (8081)
/api/dispatch/**  → Dispatch Service (8082)
/api/tracking/**  → Tracking Service (8083)
/api/billing/**   → Billing Service (8084)
```

### 3.2 User & Auth Service (Port 8081)

**Domaine** : Identity & Access Management

**Responsabilités** :
- Gestion des utilisateurs (CRUD)
- Authentification (login/logout)
- Autorisation (RBAC)
- Gestion des tokens JWT
- Audit des connexions

**Entités principales** :
- `User` (id, email, password, role, status)
- `Role` (ADMIN, DISPATCHER, DRIVER, FINANCE)
- `AuditLog` (user_id, action, timestamp)

**API Endpoints** :
```
POST   /api/auth/register
POST   /api/auth/login
POST   /api/auth/refresh
POST   /api/auth/logout
GET    /api/users/{id}
PUT    /api/users/{id}
GET    /api/users/me
```

**Base de données** : PostgreSQL (users_db)

### 3.3 Dispatch Service (Port 8082)

**Domaine** : Order & Delivery Management

**Responsabilités** :
- Création et gestion des courses
- Attribution aux chauffeurs
- Gestion des statuts
- Calcul d'itinéraires (phase simple)
- Historique des courses

**Entités principales** :
- `DeliveryOrder` (id, pickup, dropoff, status, priority)
- `Driver` (id, name, vehicle, status, location)
- `Assignment` (delivery_id, driver_id, assigned_at)

**Machine à états** :
```
CREATED → ASSIGNED → PICKED_UP → IN_TRANSIT → DELIVERED
                ↓
            CANCELLED
```

**API Endpoints** :
```
POST   /api/dispatch/orders
GET    /api/dispatch/orders/{id}
PUT    /api/dispatch/orders/{id}
GET    /api/dispatch/orders
POST   /api/dispatch/orders/{id}/assign
PUT    /api/dispatch/orders/{id}/status
GET    /api/dispatch/drivers
GET    /api/dispatch/drivers/available
```

**Base de données** : PostgreSQL (dispatch_db)

**Events produits** :
- `OrderCreatedEvent`
- `OrderAssignedEvent`
- `OrderStatusChangedEvent`
- `OrderCompletedEvent`

### 3.4 Tracking Service (Port 8083)

**Domaine** : Real-Time Location Tracking

**Responsabilités** :
- Réception positions GPS des drivers
- Stockage historique des trajectoires
- Streaming temps réel via WebSocket
- Calcul de la distance parcourue
- Géofencing (phase 2)

**Entités principales** :
```json
{
  "driverId": "uuid",
  "deliveryId": "uuid",
  "location": {
    "latitude": 48.8566,
    "longitude": 2.3522,
    "accuracy": 10
  },
  "timestamp": "2024-01-15T10:30:00Z",
  "speed": 45,
  "heading": 180
}
```

**API Endpoints** :
```
POST   /api/tracking/position        (from driver app)
GET    /api/tracking/driver/{id}
GET    /api/tracking/delivery/{id}
WS     /ws/tracking/{deliveryId}     (WebSocket)
GET    /api/tracking/history/{driverId}
```

**Base de données** : MongoDB (tracking_db)
- Index géospatial 2dsphere
- TTL index (30 jours)

**Events consommés** :
- `OrderAssignedEvent` (création du tracking)

### 3.5 Billing Service (Port 8084)

**Domaine** : Invoicing & Financial

**Responsabilités** :
- Calcul des coûts de livraison
- Génération des factures
- Historique facturation
- Export comptable (phase 2)

**Entités principales** :
- `Invoice` (id, delivery_id, amount, status)
- `PricingRule` (base_price, per_km, urgency_multiplier)
- `Payment` (invoice_id, status, date)

**Calcul tarifaire** :
```
Total = BASE_PRICE 
      + (DISTANCE_KM × PRICE_PER_KM) 
      + URGENCY_MULTIPLIER
      + SLA_PREMIUM
```

**API Endpoints** :
```
POST   /api/billing/invoices
GET    /api/billing/invoices/{id}
GET    /api/billing/invoices?customerId=...
PUT    /api/billing/invoices/{id}/status
GET    /api/billing/reports/monthly
```

**Base de données** : PostgreSQL (billing_db)

**Events consommés** :
- `OrderCompletedEvent` (génération facture)

## 4. Communication inter-services

### 4.1 Communication synchrone (REST)
Utilisée pour :
- Requêtes nécessitant réponse immédiate
- Validation métier
- Lecture de données

**Exemple** : Gateway → User Auth Service (vérification JWT)

### 4.2 Communication asynchrone (Kafka)
Utilisée pour :
- Événements métier
- Notifications
- Agrégation de données
- Découplage temporel

**Topics Kafka** :
```
user.events          → Création/Modification utilisateur
dispatch.events      → Événements dispatch
tracking.updates     → Mises à jour position
billing.events       → Facturation
```

**Configuration** :
- Partitions : 3 par topic (scalabilité)
- Replication factor : 2 (résilience)
- Retention : 7 jours

## 5. Gestion des données

### 5.1 Database per Service
Chaque microservice possède sa propre base :
- Autonomie
- Scalabilité indépendante
- Évolution de schéma isolée

### 5.2 Stratégie de cohérence
- **Cohérence forte** : Au sein d'un service (transactions ACID)
- **Cohérence éventuelle** : Entre services (via événements)

### 5.3 Gestion des transactions distribuées
Pattern **Saga** (phase 2) :
- Choreography-based Saga via Kafka
- Compensation en cas d'échec

## 6. Sécurité

### 6.1 Authentification
- JWT avec signature RSA-256
- Access Token : 15 minutes
- Refresh Token : 7 jours
- Stockage Redis pour révocation

### 6.2 Autorisation (RBAC)
```
ADMIN      → Tous droits
DISPATCHER → Gestion dispatch, lecture tracking/billing
DRIVER     → Lecture ses missions, update tracking
FINANCE    → Lecture/écriture billing, lecture dispatch
```

### 6.3 Chiffrement
- TLS 1.3 en production
- Données sensibles chiffrées en DB (AES-256)
- Secrets gérés via Vault (phase 2)

### 6.4 HIPAA Compliance
- Audit logging complet
- Chiffrement end-to-end
- Contrôle d'accès granulaire
- Rétention limitée des données

## 7. Résilience

### 7.1 Circuit Breaker (Resilience4j)
Configuration par défaut :
```yaml
failure-rate-threshold: 50%
wait-duration-in-open-state: 60s
sliding-window-size: 10
```

### 7.2 Retry Policy
```yaml
max-attempts: 3
wait-duration: 500ms
exponential-backoff: true
```

### 7.3 Bulkhead
Isolation des thread pools par service

### 7.4 Timeout
- API Gateway : 30s
- Inter-service : 5s
- Database queries : 10s

## 8. Observabilité

### 8.1 Métriques (Prometheus)
- JVM metrics (heap, threads, GC)
- HTTP metrics (requests, latency)
- Business metrics (orders/min, tracking updates/s)

### 8.2 Logs (Structure JSON)
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "level": "INFO",
  "service": "dispatch-service",
  "traceId": "abc123",
  "spanId": "def456",
  "message": "Order created",
  "orderId": "uuid"
}
```

### 8.3 Distributed Tracing (OpenTelemetry - Phase 2)
- Corrélation des requêtes cross-services
- Identification des bottlenecks

## 9. Scalabilité

### 9.1 Horizontal Scaling
Tous les services sont **stateless** :
- Session dans Redis
- Aucun état local
- Réplication facile

### 9.2 Stratégie de scaling
```
Tracking Service : CPU > 70% → scale +2 instances
Dispatch Service : Requests/s > 100 → scale +1
Billing Service  : Memory > 80% → scale +1
```

### 9.3 Database Scaling
- **PostgreSQL** : Réplication read replicas
- **MongoDB** : Sharding par driverId
- **Redis** : Redis Cluster

## 10. Déploiement

### 10.1 Conteneurisation
- Images Docker optimisées (multi-stage build)
- Base image : eclipse-temurin:21-jre-alpine
- Taille cible : < 200MB par service

### 10.2 Orchestration (Kubernetes - Phase 2)
```yaml
Deployment Strategy: RollingUpdate
Max Surge: 1
Max Unavailable: 0
Health Checks: Liveness + Readiness
Resource Limits: 
  CPU: 500m-2000m
  Memory: 512Mi-2Gi
```

## 11. CI/CD Pipeline

```
Code Push → GitHub
    ↓
Build & Test (Maven)
    ↓
SonarQube Analysis
    ↓
Docker Build & Push
    ↓
Deploy to Dev
    ↓
Integration Tests
    ↓
Manual Approval
    ↓
Deploy to Production
```

## 12. Evolution future

### Phase 2
- Service Discovery (Eureka)
- Config Server (Spring Cloud Config)
- Notification Service (Email/SMS)
- Advanced Routing Optimization

### Phase 3
- Multi-tenant support
- AI/ML route prediction
- Mobile SDK
- Integration marketplace

---

**Auteur** : Architecture Team  
**Version** : 1.0  
**Date** : 2024
