# 🎮 Game & Electronics Price Tracker

A full-stack web application that automatically tracks prices of gaming hardware and electronics across online retailers. Built with Spring Boot, PostgreSQL, and vanilla JavaScript.

---

## 📸 Features

- **Add products** with a Newegg, B&H Photo, or Adorama URL
- **Automatic price scraping** runs in the background every 30 minutes
- **Price history chart** — interactive line graph showing price over time
- **Lowest price tracking** — always see the best recorded price per product
- **Delete products** and all associated price history
- **REST API** — fully documented endpoints for all operations

---

## 🧱 Architecture

```
Browser (HTML5 + CSS3 + Vanilla JS + Chart.js)
                    ↕ REST API (JSON)
         Spring Boot Backend (Java 17+)
          Controllers → Services → Repositories
                    ↕ JPA / Hibernate
              PostgreSQL Database
                    ↑
     @Scheduled Scraper (Jsoup → Newegg / B&H / Adorama)
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Frontend | HTML5, CSS3, Vanilla JavaScript, Chart.js |
| Backend | Java 25, Spring Boot 4.x |
| ORM | Spring Data JPA, Hibernate |
| Database | PostgreSQL 18 |
| Migrations | Flyway |
| Scraping | Jsoup |
| Build Tool | Maven |

---

## ⚙️ Prerequisites

- Java JDK 17+
- Maven 3.6+
- PostgreSQL 15+
- Git

---

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/your-username/price-tracker.git
cd price-tracker
```

### 2. Create the database

```bash
psql -U postgres
```

```sql
CREATE DATABASE app_db;
CREATE USER app_user WITH PASSWORD 'app_pass';
GRANT ALL PRIVILEGES ON DATABASE app_db TO app_user;
ALTER DATABASE app_db OWNER TO app_user;
\q
```

### 3. Configure `application.yml`

The default config in `src/main/resources/application.yml` connects to:

```
Host:     localhost:5432
Database: app_db
Username: app_user
Password: app_pass
```

Update these values if your setup differs.

### 4. Run the application

```bash
mvn spring-boot:run
```

Flyway will automatically create the schema on first run.

### 5. Open the app

```
http://localhost:8080
```

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/example/app/
│   │   ├── controller/        ← REST endpoints
│   │   ├── service/           ← Business logic + scraper + scheduler
│   │   ├── repository/        ← Spring Data JPA repositories
│   │   ├── model/             ← JPA entities (Product, PriceHistory)
│   │   ├── dto/               ← Request/response DTOs + ApiResponse envelope
│   │   └── exception/         ← Custom exceptions + GlobalExceptionHandler
│   └── resources/
│       ├── application.yml
│       ├── db/migration/      ← Flyway SQL migrations (V1, V2, V3)
│       └── static/            ← Frontend (index.html, style.css, app.js)
```

---

## 🔌 API Reference

### Products

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/products` | Get all products |
| `GET` | `/api/products/{id}` | Get product by ID |
| `POST` | `/api/products` | Create a new product |
| `DELETE` | `/api/products/{id}` | Delete product + price history |
| `GET` | `/api/products/category/{category}` | Filter by category |

### Prices

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/products/prices` | Record a price manually |
| `GET` | `/api/products/{id}/prices` | Get full price history |
| `GET` | `/api/products/{id}/prices/lowest` | Get lowest recorded price |

### Response Envelope

All responses follow a consistent envelope:

```json
{
  "status": "success",
  "data": { ... },
  "message": null
}
```

Error responses:

```json
{
  "status": "error",
  "data": null,
  "message": "Product with ID 99 not found"
}
```

---

## 🤖 Automatic Price Tracking

The scraper runs every 30 minutes (configurable in `application.yml`):

```yaml
app:
  tracking:
    interval-ms: 1800000  # 30 minutes
```

### Supported Stores

| Store | URL Domain |
|---|---|
| Newegg | `newegg.com` |
| B&H Photo | `bhphotovideo.com` |
| Adorama | `adorama.com` |
| Micro Center | `microcenter.com` |

To add a product for automatic tracking, paste a supported store URL and enable the **Auto Track** checkbox in the UI.

---

## 🗄️ Database Schema

```sql
CREATE TABLE product (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    brand       VARCHAR(100) NOT NULL,
    category    VARCHAR(50)  NOT NULL,
    url         VARCHAR(500),
    auto_track  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE price_history (
    id          BIGSERIAL PRIMARY KEY,
    product_id  BIGINT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    store_name  VARCHAR(100) NOT NULL,
    price       NUMERIC(10,2) NOT NULL,
    recorded_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

---

## 🧪 Example: Add a Product via API

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "ASUS RTX 4070",
    "brand": "ASUS",
    "category": "GPU",
    "url": "https://www.newegg.com/asus-geforce-rtx-4070/p/N82E16814126673",
    "autoTrack": true
  }'
```

---

## 📝 Key Design Decisions

**Why Flyway?** Database schema is version-controlled just like code. Every change is a numbered migration file — no accidental schema drift between environments.

**Why DTOs?** Entities are never returned directly from controllers. DTOs decouple the API contract from the database schema so either can change independently.

**Why constructor injection?** Dependencies are explicit and testable. Field injection with `@Autowired` hides dependencies and makes unit testing without a Spring context impossible.

**Why `@Transactional(readOnly = true)` on reads?** Hibernate skips dirty checking on read-only transactions — free performance improvement that also signals intent clearly.

**Why `BigDecimal` for prices?** `float` and `double` cannot represent values like `9.99` exactly due to floating-point precision. `BigDecimal` maps directly to PostgreSQL's `NUMERIC` type for exact decimal arithmetic.

---

## 🔮 Potential Next Steps

- Unit tests with Mockito for the service layer
- `PATCH /api/products/{id}` endpoint to toggle auto-tracking from the UI
- Price drop email/SMS alerts when a price falls below a threshold
- Support for more retailers
- Docker containerization

---

## 👤 Author

Built as a full-stack learning project covering Spring Boot, REST API design, JPA, Flyway, web scraping, and vanilla JavaScript frontend development.
