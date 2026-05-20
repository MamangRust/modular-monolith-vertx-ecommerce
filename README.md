# Distributed Modular Monolith — Java 21 & Eclipse Vert.x E-Commerce Platform

A production-grade, highly resilient **reactive modular monolith e-commerce platform** built with **Java 21** and the **Eclipse Vert.x** toolkit. Engineered around Domain-Driven Design (DDD) service boundaries, this platform achieves high throughput, low latency, and non-blocking asynchronous event execution, all while retaining the deployment and operational simplicity of a single monorepo unit.

Each business domain exists in its own isolated module with a clean architecture design, communicating synchronously via type-safe **gRPC** and asynchronously via an event-driven **Kafka** backbone.

---

## Key Architectural Innovations (Java 21 + Vert.x)

- **Reactive Non-Blocking Core**: Every service utilizes the Vert.x reactive execution model. All I/O operations (database queries, Redis caching, gRPC, and Kafka events) run on the non-blocking **Event Loop** with `Future` composition, completely avoiding traditional thread-per-request blocking overhead.
- **Java 21 Native Features**: Uses Java 21 features including type-safe **Record Classes** for immutable DTOs, **Pattern Matching** for cleaner domain state validation, and modern collections.
- **Distributed Cache (Redis Cluster)**: Native integration with **Redis Cluster** in Java Vert.x. Transparently switches between standalone and cluster client modes via environment variables (`REDIS_CLUSTER_ENABLED` and `REDIS_CLUSTER_ENDPOINTS`).
- **Unified gRPC Inter-Service Fabric**: Strongly-typed synchronous service-to-service communication is powered by Vert.x gRPC clients, leveraging unified client-side socket connection pooling.
- **Modern Event-Driven Architecture (KRaft Kafka)**: Asynchronous actions (such as automated email dispatch on merchant onboarding, document upload, and transaction creation) are decoupled through the Vert.x Kafka Client interacting with an Apache Kafka cluster running in modern **KRaft mode** (no Zookeeper dependency).
- **Observability-First Philosophy**: Instrumented from the ground up with OpenTelemetry traces, Prometheus application metrics, pgBouncer connection pooling metrics, Logback structured logging, and Grafana dashboards.

---

## Domain & Module Matrix

| Domain / Module | Technology Stack & Features |
|:---|:---|
| **API Gateway** | Unified REST entry point utilizing Vert.x Web router. Validates JWT, routes requests, and marshals JSON payloads into type-safe gRPC commands. Supports modular sub-handlers. |
| **Auth & Users** | Secure registration, role-based access control (RBAC), bcrypt hashing, JWT access/refresh token generation. |
| **Merchants** | Onboarding pipelines, verified registration, custom document uploads, auto-generated secure API keys (`mc_...`). |
| **Catalog & Products** | High-performance catalog searching, category mapping, product inventory tracking, slider/banner systems. |
| **Commerce & Checkout** | Shopping carts, reactive order lifecycle handling, shipping address directory. |
| **Transactions** | Payment recording, transaction status tracking, and event-driven confirmation pipelines. |
| **Email Service** | Asynchronous Kafka event consumer. Ingests notification payloads and dispatches responsive HTML email templates. |

---

## Comprehensive Architecture Blueprint

```mermaid
graph TB
    classDef client fill:#0f172a,stroke:#38bdf8,color:#e0f2fe,stroke-width:2px,font-weight:bold
    classDef gateway fill:#1e293b,stroke:#22d3ee,color:#cffafe,stroke-width:2px,font-weight:bold
    classDef domain fill:#1e1b4b,stroke:#818cf8,color:#e0e7ff,stroke-width:1.5px
    classDef infra fill:#172554,stroke:#60a5fa,color:#dbeafe,stroke-width:1.5px
    classDef obs fill:#052e16,stroke:#4ade80,color:#dcfce7,stroke-width:1.5px
    classDef event fill:#431407,stroke:#fb923c,color:#fed7aa,stroke-width:1.5px

    Client["Client Applications<br/>(Web / Mobile / HTTP API)"]:::client

    subgraph APIGateway["API Gateway — Vert.x Web Router & REST Bridge"]
        direction LR
        REST["REST Endpoints<br/>/api/v1/*"]
        JWT["JWT Auth &<br/>Role Validation"]
        ClientPool["Unified gRPC Client Pool"]
    end
    class APIGateway gateway

    Client --> REST
    REST --> JWT
    JWT --> ClientPool

    subgraph BusinessServices["Vert.x Reactive Domain Verticles"]
        direction TB

        subgraph IdentityDomain["Identity & Access Module"]
            AUTH["Auth Verticle<br/>JWT & RBAC"]
            USER["User Verticle<br/>User Management"]
            ROLE["Role Verticle<br/>Role Registry"]
        end

        subgraph MerchantDomain["Merchant Module Suite"]
            MERCH["Merchant Verticle"]
            MDETAIL["Merchant Detail"]
            MBIZ["Merchant Business"]
            MPOL["Merchant Policy"]
            MAWARD["Merchant Award"]
        end

        subgraph CatalogDomain["Catalog Module Suite"]
            PROD["Product Verticle"]
            CAT["Category Verticle"]
            BANNER["Banner Verticle"]
            SLIDER["Slider Verticle"]
        end

        subgraph CommerceDomain["Commerce & Checkout Module"]
            CART["Cart Verticle"]
            ORDER["Order Verticle"]
            TXN["Transaction Verticle"]
            SHIP["Shipping Address"]
        end

        subgraph FeedbackDomain["Customer Feedback Module"]
            REVIEW["Review Verticle"]
            RDETAIL["Review Detail"]
        end
    end
    class BusinessServices domain

    ClientPool -->|"Non-Blocking gRPC"| BusinessServices

    subgraph Infrastructure["Infrastructure & Persistence"]
        direction LR
        BOUNCER["pgBouncer<br/>Connection Pooler"]
        PG[("PostgreSQL Database<br/>(PAYMENT_GATEWAY)")]
        REDIS[("Redis Cluster<br/>(6-Node StatefulSet)")]
        KAFKA[("Apache Kafka (KRaft)<br/>(Vert.x Kafka Client)")]
    end
    class Infrastructure infra

    BusinessServices -->|"Reactive Client"| BOUNCER
    BOUNCER -->|"Pooled Connection"| PG
    BusinessServices -->|"RedisAPI Cluster Commands"| REDIS
    BusinessServices -->|"Publish Events"| KAFKA

    subgraph EventConsumers["Event-Driven Notification Layer"]
        EMAIL["Email Consumer Verticle<br/>Kafka Consumer + SMTP"]
    end
    class EventConsumers event

    KAFKA -->|"Consume Event"| EMAIL

    subgraph Observability["Observability Stack"]
        direction LR
        PROM["Prometheus<br/>Scrapers"]
        OTEL["OTel SDK<br/>Distributed Traces"]
        JAEGER["Jaeger UI<br/>Trace Visualizer"]
        GRAFANA["Grafana<br/>Dashboards"]
    end
    class Observability obs

    BusinessServices -.->|"/metrics"| PROM
    BusinessServices -.->|"OTLP Spans"| OTEL
    OTEL -.-> JAEGER
    PROM -.-> GRAFANA
```

---

## Internal Service Architecture (Vert.x Verticle Pattern)

Every module uses a custom **CQRS & Clean Architecture** implementation with strict dependency control:

```mermaid
graph TB
    classDef entry fill:#1e3a5f,stroke:#7dd3fc,color:#e0f2fe,stroke-width:1.5px
    classDef handler fill:#1e293b,stroke:#22d3ee,color:#cffafe,stroke-width:1.5px
    classDef service fill:#1e1b4b,stroke:#a78bfa,color:#ede9fe,stroke-width:1.5px
    classDef repo fill:#172554,stroke:#60a5fa,color:#dbeafe,stroke-width:1.5px
    classDef shared fill:#431407,stroke:#fb923c,color:#fed7aa,stroke-width:1.5px
    classDef thirdparty fill:#052e16,stroke:#4ade80,color:#dcfce7,stroke-width:1.5px

    subgraph Module["Reactive Module (e.g., transaction/)"]
        direction TB
        VERTICLE["TransactionVerticle.java<br/>Entry & Lifecycle Hook"]:::entry
        HANDLER["TransactionCommandHandler.java<br/>gRPC Dispatcher"]:::handler
        SERVICE["TransactionCommandServiceImpl.java<br/>Command Execution"]:::service
        REPO["TransactionCommandRepositoryImpl.java<br/>PgPool Execution"]:::repo
    end

    subgraph CommonModule["common/ Module — Shared Platform"]
        direction LR
        REDIS_SVC["RedisService.java<br/>Cache API"]:::shared
        KAFKA_SVC["KafkaService.java<br/>Event Producer"]:::shared
        TRACING["TracingMetrics.java<br/>Telemetry Hook"]:::shared
        EMAIL_TMPL["EmailTemplate.java<br/>HTML Template Engine"]:::shared
        CONFIG["AppConfig.java<br/>System Props Loader"]:::shared
    end

    subgraph ExternalLibraries["External Frameworks"]
        VERT_X["Eclipse Vert.x Core"]:::thirdparty
        GRPC_SERV["Vert.x gRPC Server"]:::thirdparty
        OTEL_SDK["OpenTelemetry SDK"]:::thirdparty
    end

    VERTICLE --> APPS_INIT["Initialize Pool, Redis, Kafka"]
    APPS_INIT --> HANDLER
    APPS_INIT --> SERVICE
    APPS_INIT --> REPO
    HANDLER --> SERVICE
    SERVICE --> REPO
    REPO -->|"Non-Blocking Query"| VERT_X
    SERVICE -->|"Cache Reads/Writes"| REDIS_SVC
    SERVICE -->|"Publish Topic Event"| KAFKA_SVC
    SERVICE -->|"Start Trace Span"| TRACING
    SERVICE -->|"Build Responsive HTML"| EMAIL_TMPL
    VERTICLE -->|"Bind Services"| GRPC_SERV
    TRACING -->|"Report Spans"| OTEL_SDK
```

---

## Getting Started & Local Development

### Prerequisites

Ensure the following tools are installed:
- **Java 21 JDK** (GraalVM or Eclipse Temurin recommended)
- **Apache Maven 3.9+**
- **Docker & Docker Compose**

### 1. Clone the Repository

```bash
git clone https://github.com/MamangRust/modular-monolith-vertx-ecommerce.git
cd modular-monolith-vertx-ecommerce
```

### 2. Build the Entire System

From the root directory:
```bash
mvn clean compile
```

To compile specific modules individually:
```bash
mvn clean compile -pl transaction
mvn clean compile -pl merchant
```

### 3. Run the Stack Locally

Launch the infrastructure services (PostgreSQL, 6-node Redis Cluster, Kafka in KRaft mode, pgBouncer) and all modular monolith containers:
```bash
docker-compose --env-file docker.env up --build
```

### 4. Local Ports Map

| Port | Service Component | Interface Protocol |
|:---|:---|:---|
| **80 / 5000** | REST API Gateway | HTTP / REST |
| **50051** | Auth Service | gRPC |
| **50053** | User & Identity | gRPC |
| **50055** | Merchant Suite | gRPC |
| **50057** | Orders Module | gRPC |
| **50059** | Transactions Module | gRPC |
| **3000** | Grafana Dashboards | HTTP |
| **9090** | Prometheus Metrics | HTTP |
| **16686** | Jaeger Traces | HTTP |

---

## Kubernetes Orchestration (deployments/kubernetes/)

Production deployments are managed using cloud-native Kubernetes manifests with robust orchestration defaults:

### 1. Architectural Highlights
- **High Availability Redis Cluster**: Configured as a **6-replica StatefulSet** utilizing headless services (`clusterIP: None`) for precise internal pod addressing. An automatic bootstrapping job handles initial cluster ring allocation.
- **Kafka KRaft Broker**: Integrated natively as a KRaft controller/broker container, fully omitting obsolete Zookeeper instances for rapid startup and lean resource usage.
- **Autoscaling (HPA)**: Integrated Horizontal Pod Autoscalers dynamically scale active deployment replicas (such as `auth`, `apigateway`, `transaction`) between `2` and `10` pods based on average CPU utilisation.
- **Central ConfigMap Management**: Environment definitions are centralized in `configsmaps.yaml` with reactive database URLs matching standard configurations.

### 2. Deploying to a Cluster
Create the namespace and load system variables, database configurations, and secrets first, then apply deployments:
```bash
# 1. Initialize namespace
kubectl apply -f deployments/kubernetes/namespace.yaml

# 2. Apply ConfigMaps and Secrets
kubectl apply -f deployments/kubernetes/configsmaps.yaml
kubectl apply -f deployments/kubernetes/secrets.yaml

# 3. Spin up Postgres & persistent volumes
kubectl apply -f deployments/kubernetes/postgres-pvc.yaml
kubectl apply -f deployments/kubernetes/postgres-service.yaml
kubectl apply -f deployments/kubernetes/postgres-deployment.yaml

# 4. Deploy the 6-Node Redis Cluster & Trigger Initialization
kubectl apply -f deployments/kubernetes/redis-service.yaml
kubectl apply -f deployments/kubernetes/redis-deployment.yaml
kubectl apply -f deployments/kubernetes/redis-cluster-creator.yaml

# 5. Apply the rest of the Microservices & Observability tools
kubectl apply -f deployments/kubernetes/
```

---

## Observability Matrix

| Pillar | Service Integration | Visualization Tooling |
|:---|:---|:---|
| **Distributed Tracing** | OpenTelemetry Java SDK automatically generates trace spans across gateway and RPC service jumps. | **Jaeger UI** - view end-to-end service hop details and query slow paths. |
| **Application Metrics** | Vert.x Micrometer Metrics exporter. | **Prometheus** - gathers database pool sizes, HTTP/gRPC latency, and cache hit ratios. |
| **Centralized Logging** | Logback SLF4J formatted with JSON output formats. | **Loki** - aggregated logs from all modular pods, searchable in Grafana. |

---

## Database Schema (ERD)

The relational database is orchestrated under the unified `PAYMENT_GATEWAY` database (leveraged cleanly by each module). Below is the logical data layout model represented as a native **Mermaid.js Entity-Relationship Diagram**:

```mermaid
erDiagram
    USERS ||--o| ROLES : "belongs to"
    USERS ||--o| MERCHANTS : "owns"
    USERS ||--o| CARTS : "has"
    USERS ||--o| ORDERS : "places"
    USERS ||--o| REVIEWS : "writes"
    USERS ||--o| SHIPPING_ADDRESSES : "manages"

    MERCHANTS ||--o{ PRODUCTS : "registers"
    CATEGORIES ||--o{ PRODUCTS : "classifies"

    ORDERS ||--|{ ORDER_ITEMS : "contains"
    ORDERS ||--|| TRANSACTIONS : "settles"
    PRODUCTS ||--o{ ORDER_ITEMS : "purchased in"
    PRODUCTS ||--o{ REVIEWS : "receives"
    REVIEWS ||--|| REVIEW_DETAILS : "elaborates"

    USERS {
        uuid id PK
        string email
        string password
        uuid role_id FK
        timestamp created_at
        timestamp updated_at
    }

    ROLES {
        uuid id PK
        string name
        string permissions
        timestamp created_at
        timestamp updated_at
    }

    MERCHANTS {
        uuid id PK
        uuid user_id FK
        string name
        string status
        string api_key
        timestamp created_at
        timestamp updated_at
    }

    PRODUCTS {
        uuid id PK
        uuid merchant_id FK
        uuid category_id FK
        string name
        decimal price
        int stock
        timestamp created_at
        timestamp updated_at
    }

    CATEGORIES {
        uuid id PK
        string name
        uuid parent_id FK
        timestamp created_at
        timestamp updated_at
    }

    CARTS {
        uuid id PK
        uuid user_id FK
        uuid product_id FK
        int quantity
        timestamp created_at
        timestamp updated_at
    }

    ORDERS {
        uuid id PK
        uuid user_id FK
        uuid shipping_address_id FK
        decimal total_price
        string status
        timestamp created_at
        timestamp updated_at
    }

    ORDER_ITEMS {
        uuid id PK
        uuid order_id FK
        uuid product_id FK
        int quantity
        decimal price
        timestamp created_at
        timestamp updated_at
    }

    TRANSACTIONS {
        uuid id PK
        uuid order_id FK
        decimal amount
        string status
        string payment_method
        timestamp created_at
        timestamp updated_at
    }

    REVIEWS {
        uuid id PK
        uuid user_id FK
        uuid product_id FK
        int rating
        timestamp created_at
        timestamp updated_at
    }

    REVIEW_DETAILS {
        uuid id PK
        uuid review_id FK
        string review_text
        string reply_text
        timestamp created_at
        timestamp updated_at
    }

    SHIPPING_ADDRESSES {
        uuid id PK
        uuid user_id FK
        string street
        string city
        string state
        string postal_code
        timestamp created_at
        timestamp updated_at
    }
```

---

<p align="center">
  Built with Eclipse Vert.x, Java 21, and a passion for modern reactive system architecture.
</p>