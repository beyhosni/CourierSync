# 🚀 CourierSync - Medical Courier Platform

![Build](https://img.shields.io/badge/Build-Passing-brightgreen?style=for-the-badge&logo=github-actions&logoColor=white) ![Coverage](https://img.shields.io/badge/Coverage-85%25-brightgreen?style=for-the-badge&logo=codecov&logoColor=white) ![License](https://img.shields.io/badge/License-Apache%202.0-blue?style=for-the-badge&logo=apache&logoColor=white) ![Version](https://img.shields.io/badge/Version-1.0.0--SNAPSHOT-orange?style=for-the-badge&logo=semantic-release&logoColor=white)

## 📌 Vue d'ensemble

**CourierSync** est une plateforme SaaS dédiée aux entreprises de transport médical, centralisant :
- 📦 Dispatch des courses
- 🗺️ Suivi en temps réel
- 💰 Facturation automatisée
- 🔒 Conformité réglementaire (HIPAA)

## 🏗️ Architecture

### Architecture Microservices

```
┌─────────────────────────────────────────────────────────────────┐
│                        FRONTEND (Angular 18)                     │
│                     WebSocket + REST Client                      │
└──────────────────────────────┬──────────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────────┐
│                    API GATEWAY (Spring Cloud)                    │
│              Routing │ Auth │ Rate Limiting │ CORS              │
└───┬──────────┬──────────┬──────────┬──────────┬─────────────────┘
    │          │          │          │          │
┌───▼────┐ ┌──▼─────┐ ┌──▼──────┐ ┌─▼────────┐ ┌▼─────────────┐
│ User & │ │Dispatch│ │Tracking │ │ Billing  │ │  Notification│
│  Auth  │ │Service │ │ Service │ │ Service  │ │   Service    │
│Service │ │        │ │         │ │          │ │  (Phase 2)   │
└───┬────┘ └──┬─────┘ └──┬──────┘ └─┬────────┘ └──────────────┘
    │         │           │          │
┌───▼─────────▼───────────▼──────────▼─────────────────────────┐
│                     EVENT BUS (Kafka)                          │
│   Topics: user.events, dispatch.events, tracking.updates,     │
│           billing.events                                       │
└────────────────────────────────────────────────────────────────┘
    │         │           │          │
┌───▼────┐ ┌──▼─────┐ ┌──▼──────┐ ┌─▼────────┐
│PostgreSQL│PostgreSQL│ MongoDB ││PostgreSQL│
│  Users  │ Dispatch │ Tracking││ Billing  │
└─────────┘ └────────┘ └─────────┘└──────────┘
                │
            ┌───▼────┐
            │ Redis  │
            │ Cache  │
            └────────┘
```

### Principes architecturaux

- ✅ **Separation of Concerns** : Chaque service a une responsabilité unique
- ✅ **Database per Service** : Autonomie des données
- ✅ **Event-Driven Architecture** : Communication asynchrone via Kafka
- ✅ **API Gateway Pattern** : Point d'entrée unique
- ✅ **CQRS Ready** : Séparation lecture/écriture préparée
- ✅ **Cloud-Native** : Stateless, containerisé, scalable

## 🗄️ Stratégie Data

| Service | Database | Justification |
|---------|----------|---------------|
| **User & Auth** | PostgreSQL | Relations complexes, transactions ACID |
| **Dispatch** | PostgreSQL | Intégrité référentielle, requêtes complexes |
| **Tracking** | MongoDB | Haute fréquence d'écriture, données géospatiales |
| **Billing** | PostgreSQL | Transactions financières, reporting |
| **Cache** | Redis | Sessions, tokens, données temps réel |

## 🛠️ Stack Technique

### Backend

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white) ![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white)

- **Java 21** (LTS)
- **Spring Boot 3.3+**
  - Spring Web
  - Spring Data JPA
  - Spring Security 6
  - Spring Cloud Gateway
  - Spring Kafka
  - Spring Validation
- **MapStruct** (mapping DTO)
- **Lombok** (boilerplate reduction)
- **OpenAPI 3** (documentation)

### Frontend

![Angular](https://img.shields.io/badge/Angular-DD0031?style=for-the-badge&logo=angular&logoColor=white) ![TypeScript](https://img.shields.io/badge/TypeScript-007ACC?style=for-the-badge&logo=typescript&logoColor=white) ![RxJS](https://img.shields.io/badge/RxJS-B7178C?style=for-the-badge&logo=reactivex&logoColor=white)

- **Angular 18**
- **TypeScript 5**
- **RxJS 7**
- **Angular Material**
- **WebSocket** (real-time tracking)

### Infrastructure

![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white) ![Kafka](https://img.shields.io/badge/Apache%20Kafka-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white) ![MongoDB](https://img.shields.io/badge/MongoDB-4EA94B?style=for-the-badge&logo=mongodb&logoColor=white)

- **Docker** + **Docker Compose**
- **Kafka** (event streaming)
- **PostgreSQL 16**
- **MongoDB 7**
- **Redis 7**
- **Kubernetes** (ready)

### Observabilité

![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white) ![Grafana](https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white)

- **Spring Boot Actuator**
- **Prometheus** (metrics)
- **Grafana** (visualization)
- **OpenTelemetry** (distributed tracing - phase 2)

## 🔐 Sécurité

![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=JSON%20web%20tokens&logoColor=white) ![OAuth2](https://img.shields.io/badge/OAuth2-EB5424?style=for-the-badge&logo=oauth&logoColor=white) ![HIPAA](https://img.shields.io/badge/HIPAA-2A6BDD?style=for-the-badge&logo=hipaa&logoColor=white)

- **OAuth2 Resource Server** : JWT avec signature RSA
- **RBAC** : Role-Based Access Control
  - `ADMIN` : Accès total
  - `DISPATCHER` : Gestion des courses
  - `DRIVER` : Consultation missions
  - `FINANCE` : Facturation
- **Audit Logging** : Traçabilité complète
- **Data Encryption** : Données sensibles chiffrées (HIPAA compliance)
- **API Rate Limiting** : Protection DoS

## 📐 Normes de développement

![Clean Architecture](https://img.shields.io/badge/Clean%20Architecture-339933?style=for-the-badge&logo=architecture&logoColor=white) ![Hexagonal](https://img.shields.io/badge/Hexagonal%20Architecture-6DB33F?style=for-the-badge&logo=hexagonal&logoColor=white) ![CQRS](https://img.shields.io/badge/CQRS-FF6B6B?style=for-the-badge&logo=cqrs&logoColor=white)

### Backend
- **Clean Architecture** : Séparation claire des responsabilités
- **Hexagonal / Ports & Adapters** : Isolation de la logique métier
- **DTO / Mapper** : Conversion avec MapStruct
- **Exception handling global** : Centralisation de la gestion des erreurs
- **Validation robuste** : Spring Validation
- **Tests unitaires (JUnit 5, Mockito)** : Couverture de code minimale 80%

### Frontend
- **Architecture modulaire** : Lazy loading des fonctionnalités
- **Services pour API** : Centralisation des appels HTTP
- **Guards (auth)** : Protection des routes
- **Interceptors (JWT)** : Injection automatique des tokens

## 📦 Structure du projet

```
couriersync/
├── docs/                          # Documentation architecture
│   ├── architecture/
│   ├── data-model/
│   └── api-specs/
├── infrastructure/                # Infrastructure as Code
│   ├── docker/
│   │   ├── docker-compose.yml
│   │   └── docker-compose.dev.yml
│   ├── kubernetes/               # Phase 2
│   └── monitoring/
├── backend/
│   ├── api-gateway/              # Spring Cloud Gateway
│   ├── user-auth-service/        # User & Authentication
│   ├── dispatch-service/         # Dispatch management
│   ├── tracking-service/         # Real-time tracking
│   ├── billing-service/          # Billing & invoicing
│   └── shared-libs/              # Common libraries
│       ├── common-dtos/
│       ├── common-security/
│       └── common-events/
└── frontend/
    └── couriersync-web/          # Angular application
```

## 🚀 Quick Start

### Prérequis
- **Java 21**
- **Node.js 20+**
- **Docker Desktop**
- **Maven 3.9+**

### Démarrage local

```bash
# 1. Démarrer l'infrastructure
cd infrastructure/docker
docker-compose up -d

# 2. Démarrer les microservices
cd backend/api-gateway
mvn spring-boot:run

# 3. Démarrer le frontend
cd frontend/couriersync-web
npm install
npm start
```

### Accès

- **Frontend** : http://localhost:4200
- **API Gateway** : http://localhost:8080
- **Swagger UI** : http://localhost:8080/swagger-ui.html
- **Kafka UI** : http://localhost:9000
- **Grafana** : http://localhost:3000

## 📊 Ports utilisés

| Service | Port | Description |
|---------|------|-------------|
| API Gateway | 8080 | Point d'entrée API |
| User Auth Service | 8081 | Authentication |
| Dispatch Service | 8082 | Dispatch management |
| Tracking Service | 8083 | Real-time tracking |
| Billing Service | 8084 | Billing & invoicing |
| Frontend | 4200 | Angular dev server |
| PostgreSQL (Users) | 5432 | Users database |
| PostgreSQL (Dispatch) | 5433 | Dispatch database |
| PostgreSQL (Billing) | 5434 | Billing database |
| MongoDB | 27017 | Tracking database |
| Redis | 6379 | Cache |
| Kafka | 9092 | Event streaming |
| Zookeeper | 2181 | Kafka coordination |

## 🧪 Tests

![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white) ![Mockito](https://img.shields.io/badge/Mockito-E6522C?style=for-the-badge&logo=mockito&logoColor=white) ![Jacoco](https://img.shields.io/badge/Jacoco-00B4AB?style=for-the-badge&logo=codecov&logoColor=white)

```bash
# Tests unitaires
mvn test

# Tests d'intégration
mvn verify -P integration-tests

# Coverage
mvn jacoco:report
```

## 📖 Documentation

Voir le répertoire `/docs` pour :
- Architecture détaillée
- Modèle de données (ERD)
- API Specifications
- Guide de déploiement

## 🗺️ Roadmap

### ✅ Phase 1 - Foundation (Current)
- [x] Architecture design
- [ ] Core microservices implementation
- [ ] API Gateway setup
- [ ] JWT Authentication
- [ ] Basic frontend (Login + Dashboard)
- [ ] Docker Compose environment

### 📅 Phase 2 - Advanced Features
- [ ] Advanced routing optimization
- [ ] Notification service (Email/SMS)
- [ ] Document management (POD - Proof of Delivery)
- [ ] Advanced reporting & analytics
- [ ] Mobile app (React Native)

### 📅 Phase 3 - Enterprise
- [ ] Multi-tenant support
- [ ] Advanced HIPAA compliance features
- [ ] AI-powered route optimization
- [ ] Predictive analytics
- [ ] Integration marketplace

---

**Version** : 1.0.0-SNAPSHOT  
**Last Updated** : 2024
