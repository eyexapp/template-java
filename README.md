# Spring Boot REST API Template

Production-ready Spring Boot 3.4 REST API with JWT authentication, PostgreSQL, Flyway migrations, and OpenAPI documentation.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Runtime | Java 21, Spring Boot 3.4.3 |
| Database | PostgreSQL 17, Spring Data JPA, Flyway |
| Security | Spring Security, JWT (JJWT 0.12.6) |
| Validation | Bean Validation (Jakarta) |
| API Docs | SpringDoc OpenAPI 2.8.5 (Swagger UI) |
| Build | Maven 3.9+ (wrapper included) |
| Testing | JUnit 5, Mockito, MockMvc, H2 |
| Container | Docker multi-stage, docker-compose |

## Quick Start

### Prerequisites

- Java 21+
- Docker & Docker Compose (for PostgreSQL)

### Run with Docker Compose

```bash
# Start app + PostgreSQL
docker compose up -d

# App available at http://localhost:8080
# Swagger UI at http://localhost:8080/swagger-ui.html
```

### Run Locally (dev profile)

```bash
# Start PostgreSQL only
docker compose up -d postgres

# Run the app
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Run Tests

```bash
./mvnw test
```

## Project Structure

```
src/main/java/com/example/myapp/
├── config/                     # Security, CORS, OpenAPI config
├── domain/
│   ├── entity/                 # JPA entities
│   ├── repository/             # Spring Data repositories
│   └── exception/              # Domain exceptions
├── security/                   # JWT provider + auth filter
├── service/                    # Business logic
├── web/
│   ├── controller/             # REST controllers
│   ├── dto/                    # Request/Response DTOs (Java records)
│   └── advice/                 # Global exception handler
└── MyAppApplication.java

src/main/resources/
├── application.yml             # Common config
├── application-dev.yml         # Dev profile (localhost)
├── application-prod.yml        # Production profile
└── db/migration/               # Flyway SQL migrations

src/test/
├── java/.../service/           # Unit tests (Mockito)
├── java/.../web/               # Controller tests (WebMvcTest)
├── java/.../integration/       # Integration tests (SpringBootTest + H2)
└── resources/application-test.yml
```

## API Endpoints

### Authentication

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| POST | `/api/v1/auth/login` | Get JWT token | No |

### Items (CRUD)

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/v1/items` | List items (paginated) | Yes |
| GET | `/api/v1/items/{id}` | Get item by ID | Yes |
| POST | `/api/v1/items` | Create item | Yes |
| PUT | `/api/v1/items/{id}` | Update item | Yes |
| DELETE | `/api/v1/items/{id}` | Delete item | Yes |

### Authentication Usage

```bash
# Get token
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' | jq -r '.token')

# Use token
curl http://localhost:8080/api/v1/items \
  -H "Authorization: Bearer $TOKEN"
```

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `8080` | Server port |
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `myapp` | Database name |
| `DB_USER` | `myapp` | Database user |
| `DB_PASS` | `myapp` | Database password |
| `JWT_SECRET` | (dev default) | JWT signing secret (min 32 chars) |
| `JWT_EXPIRATION_MS` | `86400000` | Token expiration (24h) |

### Profiles

- **dev** — localhost PostgreSQL, SQL logging, debug output
- **prod** — env vars only, Swagger disabled, INFO logging
- **test** — H2 in-memory, Flyway disabled, auto DDL

## Docker

### Build Image

```bash
docker build -t myapp .
```

### Run with Compose

```bash
# Start everything
docker compose up -d

# View logs
docker compose logs -f app

# Stop
docker compose down
```

## Adding a New Feature

1. Create migration: `src/main/resources/db/migration/V2__create_orders.sql`
2. Create entity: `domain/entity/Order.java`
3. Create repository: `domain/repository/OrderRepository.java`
4. Create DTOs: `web/dto/CreateOrderRequest.java`, `web/dto/OrderResponse.java`
5. Create service: `service/OrderService.java`
6. Create controller: `web/controller/OrderController.java`
7. Write tests for each layer