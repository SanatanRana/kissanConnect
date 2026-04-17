# Kissan Connect - Architecture & System Diagrams

This document illustrates the architectural design, microservice communication, deployment topology, and database relationships of the **Kissan Connect** platform. 

All diagrams are written in **Mermaid.js**. You can view these directly in VS Code (by opening the preview) or on GitHub.

---

## 1. High-Level System Architecture

This diagram shows the topology of the application, representing how the frontend communicates through the Spring Cloud Gateway to the downstream Spring Boot microservices, which each operate on their own isolated databases.

```mermaid
graph TD;
    %% External layer
    Client["React Frontend / Web Browser"]

    %% Gateway layer
    subgraph "API Gateway Layer"
        Gateway["API Gateway :8080<br/>(JWT Filter + Routing)"]
    end

    %% Service Registry
    Eureka["Eureka Discovery Service :8761"]

    %% Services layer
    subgraph "Microservices Layer"
        UserService["User Service :8081<br/>(Auth, Profile)"]
        ProductService["Product Service :8082<br/>(Catalog, Uploads)"]
        OrderService["Order Service :8083<br/>(Cart, Orders)"]
    end

    %% Database layer
    subgraph "Database Layer (MySQL)"
        DB_User[(kisan_user_db)]
        DB_Product[(kisan_product_db)]
        DB_Order[(kisan_order_db)]
    end

    %% Connections
    Client -->|"REST & Images"| Gateway
    Gateway -->|"/api/auth & /api/users"| UserService
    Gateway -->|"/api/products & /uploads"| ProductService
    Gateway -->|"/api/orders"| OrderService

    Gateway -.->|"Fetch Routes"| Eureka
    UserService -.->|"Register"| Eureka
    ProductService -.->|"Register"| Eureka
    OrderService -.->|"Register"| Eureka

    UserService --> DB_User
    ProductService --> DB_Product
    OrderService --> DB_Order
```

---

## 2. Authentication & Authorization Flow

This sequence demonstrates how JSON Web Tokens (JWT) are issued during login and seamlessly validated at the API Gateway level before traffic touches backend services.

```mermaid
sequenceDiagram
    autonumber
    participant Client as React Frontend
    participant Gateway as API Gateway
    participant UserSvc as User Service
    participant DB as User Database

    %% Login Flow
    Client->>Gateway: POST /api/auth/login (email, pwd)
    Gateway->>UserSvc: Forward (Open Endpoint)
    UserSvc->>DB: Verify Credentials
    DB-->>UserSvc: Valid User
    UserSvc->>UserSvc: Generate JWT Token
    UserSvc-->>Gateway: Return Token
    Gateway-->>Client: HTTP 200 OK + JWT Token

    %% Protected Flow
    Client->>Gateway: GET /api/orders/cart/9 (Header: Bearer Token)
    Gateway->>Gateway: JwtAuthenticationFilter:<br/>Parse & Validate Token Signature
    Gateway->>Gateway: Extract `userId` and `role`
    Gateway->>OrderService: Route Request<br/>(Inject Headers: X-User-Id, X-User-Role)
    OrderService-->>Gateway: HTTP 200 OK (Cart Data)
    Gateway-->>Client: Return Secure Data
```

---

## 3. Shopping Cart to Checkout Flow

A major feature of Kissan Connect is placing an order from the cart. Since order information relies on the customer's details, the Order Service interacts directly with the User Service.

```mermaid
sequenceDiagram
    autonumber
    participant UI as React Frontend
    participant Gateway as API Gateway
    participant OrderSvc as Order Service
    participant UserSvc as User Service
    participant MasterDB as Order DB

    UI->>Gateway: POST /api/orders/checkout/{userId}
    Gateway->>OrderSvc: Forward with User Headers
    OrderSvc->>MasterDB: Fetch 'Cart' & 'CartItems'
    
    OrderSvc->>UserSvc: RestTemplate:<br/>GET http://user-service/api/users/{userId}
    UserSvc-->>OrderSvc: Return Customer Info (Name, Address, Phone)

    OrderSvc->>OrderSvc: Map CartItems -> OrderItems
    OrderSvc->>OrderSvc: Calculate Total Amounts & Commissions
    OrderSvc->>MasterDB: Save `Order` entity
    OrderSvc->>MasterDB: Clear user's `Cart`
    
    OrderSvc-->>Gateway: Return Created Order Config
    Gateway-->>UI: Alert: "Order Confirmed!" & Redirect
```

---

## 4. Simplified Entity Relationship Diagram (ERD)

This diagram shows the primary data objects across all microservice databases and how they semantically relate to one another (using soft-links like `customerId` rather than hard foreign constraints across database boundaries).

```mermaid
erDiagram
    %% User Service
    USER {
        Long id PK
        String name
        String email
        String role "FARMER / SHOPKEEPER / CUSTOMER"
        String phone
    }

    %% Product Service
    PRODUCT {
        Long id PK
        String name
        Double price
        Integer stockQuantity
        Long sellerId FK "References User.id"
        String imageUrl
    }
    
    REVIEW {
        Long id PK
        Integer rating
        String comment
        Long productId FK
        Long customerId FK
    }

    %% Order Service
    CART {
        Long id PK
        Long customerId FK
        Double totalAmount
    }
    CART_ITEM {
        Long id PK
        Long productId FK
        Integer quantity
        Double price
    }
    ORDER {
        Long id PK
        Long customerId FK
        Long sellerId FK
        Double totalAmount
        String orderStatus "PLACED, SHIPPED, DELIVERED"
        String paymentStatus
    }
    ORDER_ITEM {
        Long id PK
        Long productId FK
        Integer quantity
        Double price
    }

    %% Relationships
    USER ||--o{ PRODUCT : "Creates (If Seller)"
    USER ||--o{ ORDER : "Places (If Customer)"
    USER ||--|| CART : "Owns"
    
    PRODUCT ||--o{ REVIEW : "Receives"
    USER ||--o{ REVIEW : "Writes"

    CART ||--o{ CART_ITEM : "Contains"
    ORDER ||--o{ ORDER_ITEM : "Consists of"
    PRODUCT ||--o{ ORDER_ITEM : "Included in"
```

---

## 5. Docker Architecture

This illustrates how Docker Orchestration maps external traffic to the Docker Network containers.

```mermaid
graph LR;
    User(("User")) -->|"Port 80"| Nginx["Frontend Container<br/>Nginx + React JS"]
    Nginx -->|"API Proxy :8080"| Gateway["api-gateway"]
    Gateway -->|"Backplane network"| Discovery["discovery-service"]
    Gateway -->|"Backplane network"| MS["user/product/order services"]
    
    MS --> MySQL[("MySQL Container<br/>Port 3306")]
    
    classDef container fill:#0db7ed,stroke:#fff,stroke-width:2px,color:#fff;
    class Nginx,Gateway,Discovery,MS,MySQL container;
```