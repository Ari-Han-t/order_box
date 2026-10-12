# Real-Time Stock Exchange Order Book & Matching Engine

A production-quality, multi-user **Electronic Stock Exchange Simulation** implemented in **Java 21**, featuring strict **Object-Oriented Design & Inheritance**, a concurrent **Price-Time Priority Order Book & Matching Engine**, **Jakarta Servlets REST APIs**, **JDBC Persistence**, and a modern **JavaFX Financial Trading Dashboard**.

---

## 🏛️ System Architecture

The exchange implements a complete 4-tier layered architecture:

```
+--------------------------------------------------------------------------+
|                     PRESENTATION LAYER (JavaFX)                          |
|  - Real-Time Order Book Ladder with Depth Visualizer                     |
|  - Live Trades Tape & Interactive Order Placement Ticket (Buy/Sell)      |
|  - Account Cash, Net Worth, Real-time P&L, Active Holdings Breakdown     |
|  - Open Order Management with 1-Click Cancellation                       |
|  - Presentation Scenario Visualizer (Slides 07 & 11)                     |
+--------------------------------------------------------------------------+
                                     │  HTTP / REST (JSON)
                                     ▼
+--------------------------------------------------------------------------+
|                  API LAYER (Jakarta Servlets on Jetty)                   |
|  - POST /api/auth/login, POST /api/auth/register, GET /api/auth/users    |
|  - POST /api/orders, GET /api/orders, DELETE /api/orders/{id}            |
|  - GET /api/orderbook?symbol=AAPL&depth=15                               |
|  - GET /api/trades?symbol=AAPL, GET /api/portfolio, GET /api/stocks      |
|  - POST /api/demo/run-scenario (Scripted Demo Scenario)                  |
+--------------------------------------------------------------------------+
                                     │  Method Invocations
                                     ▼
+--------------------------------------------------------------------------+
|                       BUSINESS LOGIC LAYER                               |
|  - OrderBook: Strict Price-Time Priority Queues (Bids desc, Asks asc)   |
|  - MatchingEngine: Atomic Order Matching with Maker-Price Execution      |
|  - TradingService: Invariant Checks, Risk Controls, Concurrency Locks     |
|  - PortfolioService: Cost Basis, Real-Time Valuation, Position Tracking  |
+--------------------------------------------------------------------------+
                                     │  JDBC (HikariCP Connection Pool)
                                     ▼
+--------------------------------------------------------------------------+
|                    PERSISTENCE LAYER (MySQL / H2)                        |
|  - Users: User credentials, BCrypt hashes, persistent cash balances      |
|  - Orders: Full lifecycle history (PENDING, PARTIAL, FILLED, CANCELLED)  |
|  - Trades: Immutable transaction log of executions and counterparties    |
|  - Portfolios: Isolated trader stock holdings and weighted cost basis    |
+--------------------------------------------------------------------------+
```

---

## 🧬 OOP Design & Inheritance Hierarchy

The core domain model adheres to strict Object-Oriented principles:

```
                          ┌────────────────┐
                          │   Comparable   │
                          └───────┬────────┘
                                  │
                          ┌───────▼────────┐
                          │  Order (Base)  │ ◄─── Encapsulation & Validation
                          └───────┬────────┘
                                  │
                 ┌────────────────┴────────────────┐
                 │ (Inheritance)                   │ (Inheritance)
         ┌───────▼────────┐                ┌───────▼────────┐
         │    BuyOrder    │                │   SellOrder    │
         └────────────────┘                └────────────────┘
           - isMatchableWith(Ask)            - isMatchableWith(Bid)
           - Priority: Highest Price         - Priority: Lowest Price
           - FIFO timestamp tie-break        - FIFO timestamp tie-break
```

1. **Encapsulation**: Private attributes with defensive mutators. Invariants enforced on construction (positive quantity, valid limit prices).
2. **Inheritance**: `BuyOrder` and `SellOrder` inherit common state and behavior from abstract base class `Order`.
3. **Polymorphism**: `isMatchableWith(Order counterparty)` and `comparePriorityTo(Order other)` are overridden polymorphically to govern opposing sides of the order book.
4. **Abstraction**: DAO interfaces (`UserDAO`, `OrderDAO`, `TradeDAO`, `PortfolioDAO`) and `MatchingEngine` decouple business execution from persistence.

---

## ⚡ Matching Algorithm & Financial Invariants

### 1. Price-Time Priority
- **Buy Side (Bids)**: Prioritizes orders with the **highest price**. Ties broken by **earlier submission timestamp** (FIFO).
- **Sell Side (Asks)**: Prioritizes orders with the **lowest price**. Ties broken by **earlier submission timestamp** (FIFO).
- **Market Orders**: Automatically prioritized ahead of limit orders for instantaneous matching against best available liquidity.

### 2. Maker-Price Execution Rule
Trades execute at the **price of the resting counterparty order** already on the book (the "Maker"). For example:
- Resting Ask in the book: `SELL 50 AAPL @ $199.80`
- Incoming Bid submitted: `BUY 100 AAPL @ $200.00`
- Trade executes for **50 shares @ $199.80** (buyer pays $199.80, not their limit of $200.00!).
- Unmatched balance of **50 shares @ $200.00** rests in the order book as the new top bid.

### 3. Concurrency & ACID Safety
- **No Negative Balances**: Cash balance is atomically verified and deducted within a single JDBC transaction.
- **No Overselling**: When submitting a sell order, trader holdings are verified. Orders exceeding available shares are immediately rejected.
- **Symbol-Level Reentrant Locks**: Order placement, matching, and cancellation within a stock symbol are strictly serialized to eliminate race conditions, while distinct symbols execute in parallel.

---

## 👥 Seeded Accounts & Sample Securities

All seeded accounts have the password: **`password123`**

| Username | Full Name | Initial Cash | Seeded Holdings | Role |
| :--- | :--- | :--- | :--- | :--- |
| **alice** | Alice Smith | $50,000.00 | 150 AAPL, 50 MSFT | Active Trader / Buyer |
| **bob** | Bob Jones | $50,000.00 | 200 AAPL, 80 TSLA | Active Trader / Seller |
| **charlie** | Charlie Brown | $75,000.00 | 100 GOOGL, 250 NVDA | Retail Trader |
| **market_maker** | Institutional MM | $1,000,000.00 | 10,000 of each stock | Liquidity Provider |
| **demo_trader** | Demo Trader | $100,000.00 | 100 AAPL | Demonstration Account |

### Supported Stocks:
- **AAPL** (Apple Inc. - Baseline ~$200.00)
- **MSFT** (Microsoft Corp. - Baseline ~$420.00)
- **GOOGL** (Alphabet Inc. - Baseline ~$180.00)
- **TSLA** (Tesla Inc. - Baseline ~$250.00)
- **NVDA** (NVIDIA Corp. - Baseline ~$125.00)

---

## 🚀 One-Command Local Startup

### Prerequisites:
- **Java 21 LTS**
- **Maven 3.8+**
- (Optional) **Docker Desktop** / **MySQL 8.0** (The system includes intelligent auto-detection: if MySQL is not running, it automatically uses an embedded in-memory database in MySQL compatibility mode with zero configuration!).

### Option 1: Launch JavaFX Desktop Client (All-in-One)
The desktop client checks if the backend server is running; if not, it automatically boots the embedded backend in a background daemon thread!

**Windows Batch:**
```cmd
run-client.bat
```

**PowerShell:**
```powershell
.\run-client.ps1
```

**Linux / macOS:**
```bash
chmod +x *.sh
./run-client.sh
```

---

### Option 2: Run Standalone Backend Server (Port 8085)

**Windows Batch:**
```cmd
run-server.bat
```

**PowerShell:**
```powershell
.\run-server.ps1
```

**Linux / macOS:**
```bash
./run-server.sh
```

---

### Option 3: Run Scripted Presentation Demo (CLI Headless)
Visibly executes the exact presentation scenario (Slides 07 & 11) from start to finish and prints formatted verification results:

**Windows Batch:**
```cmd
run-demo.bat
```

**PowerShell:**
```powershell
.\run-demo.ps1
```

**Linux / macOS:**
```bash
./run-demo.sh
```

---

## 🐳 Docker Compose Deployment (MySQL + Backend)

To launch the full production stack with MySQL 8.0 container and health-checked backend container:

```bash
docker compose up --build -d
```

To view logs:
```bash
docker compose logs -f
```

To stop containers:
```bash
docker compose down -v
```

---

## 📡 REST API Documentation

Base URL: `http://localhost:8085`

### Authentication
- `POST /api/auth/register` — Register a new trader account
  ```json
  { "username": "david", "password": "securePass123", "fullName": "David Warner", "initialBalance": 25000.0 }
  ```
- `POST /api/auth/login` (or `/login`) — Authenticate and receive session token
  ```json
  { "username": "alice", "password": "password123" }
  ```
- `GET /api/auth/users` — List all registered traders.

### Trading & Order Book
- `POST /api/orders` (or `/orders`) — Place Limit or Market order
  ```json
  { "symbol": "AAPL", "side": "BUY", "type": "LIMIT", "price": 200.0, "quantity": 100 }
  ```
- `GET /api/orders?activeOnly=true` (or `/orders`) — Fetch user's active orders.
- `DELETE /api/orders/{orderId}` (or `POST /api/orders/{orderId}/cancel`) — Cancel an open resting order.
- `GET /api/orderbook?symbol=AAPL&depth=15` (or `/orderbook`) — Real-time aggregated order book depth ladder.
- `GET /api/trades?symbol=AAPL&limit=25` (or `/trades`) — Recent executed trades feed.
- `GET /api/portfolio` (or `/portfolio`) — User cash balance, total net worth, positions, and unrealized P&L.
- `GET /api/stocks` — Available stock listings, current prices, and daily percentage change.
- `POST /api/demo/run-scenario` — Trigger scripted presentation demo scenario.
- `GET /api/health` — Backend and database operational status check.

---

## 🧪 Automated Test Suite

Run the full automated test suite covering order book priority, matching logic, multithreaded concurrency, portfolio isolation, and REST APIs:

```bash
mvn clean test
```

### Verified Test Suites:
1. `OrderBookTest`: Price-time priority sorting, FIFO tie-breaking, order cancellation, and depth aggregation snapshots.
2. `MatchingEngineTest`: Maker-price execution verification, partial fill remainder preservation, depth sweep, and market orders.
3. `ConcurrencyTest`: 16 threads submitting simultaneous buy and sell orders, verifying thread-safety and exact share conservation without race conditions.
4. `PortfolioIsolationTest`: Prevention of overselling, rejection of buy orders exceeding cash, and user account order isolation.
5. `EndToEndScenarioTest`: Full REST API integration lifecycle against the live database, including authentication, order matching, and scripted demo execution.

---

## ☁️ Cloud Deployment Guidelines

### 1. Google Cloud Run / AWS ECS
Build and push the Docker container to Container Registry (GCR / ECR / Docker Hub):
```bash
docker build -t your-org/stock-exchange:latest .
docker push your-org/stock-exchange:latest
```
Deploy the container with environment variables:
- `PORT=8080`
- `DB_URL=jdbc:mysql://<cloud-sql-or-rds-host>:3306/stock_exchange?useSSL=true`
- `DB_USER=<db-user>`
- `DB_PASSWORD=<db-password>`

### 2. Virtual Machine (AWS EC2 / DigitalOcean Droplet)
1. Clone the repository on the server.
2. Ensure Docker and Docker Compose are installed.
3. Run `docker compose up -d`.
4. Configure an Nginx reverse proxy or SSL termination with Certbot on port 443 pointing to `localhost:8085`.
