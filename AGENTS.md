# AGENTS.md — Spring Boot 3.4 REST API

## Project Identity

| Key | Value |
|-----|-------|
| Framework | Spring Boot 3.4 |
| Language | Java 21 |
| Category | Backend REST API |
| Build | Maven (Maven Wrapper `./mvnw`) |
| Database | PostgreSQL (H2 for tests) |
| ORM | Spring Data JPA + Flyway migrations |
| Auth | Stateless JWT (Bearer token) |
| Docs | SpringDoc OpenAPI (Swagger UI) |
| Testing | JUnit 5 + Mockito + @WebMvcTest + Testcontainers |
| Linting | Checkstyle |

---

## Architecture — Layered Spring Boot API

```
src/main/java/com/example/myapp/
├── config/              ← INFRASTRUCTURE: Spring configuration (Security, CORS, OpenAPI)
├── domain/
│   ├── entity/          ← DOMAIN: JPA entities (Lombok @Getter/@Setter/@Builder)
│   ├── repository/      ← DOMAIN: Spring Data JPA repositories
│   └── exception/       ← DOMAIN: Exceptions (extend AppException)
├── security/            ← INFRASTRUCTURE: JWT provider + auth filter
├── service/             ← BUSINESS LOGIC: @Service beans (constructor injection)
├── web/
│   ├── controller/      ← PRESENTATION: @RestController endpoints
│   ├── dto/             ← PRESENTATION: Request/Response Java records
│   └── advice/          ← CROSS-CUTTING: @RestControllerAdvice exception handler
└── MyAppApplication.java
```

### Strict Layer Rules

| Layer | Can Import From | NEVER Imports |
|-------|----------------|---------------|
| `web/controller/` | service/, web/dto/ | domain/repository/ |
| `service/` | domain/entity/, domain/repository/, domain/exception/ | web/ |
| `domain/entity/` | (none — pure JPA) | service/, web/ |
| `domain/repository/` | domain/entity/ | service/, web/ |
| `config/` | domain/, security/ | service/ logic |

---

## Adding New Code — Where Things Go

### New Feature Checklist
1. **Migration**: `src/main/resources/db/migration/V{next}__description.sql`
2. **Entity**: `domain/entity/Product.java` — Lombok, UUID PK, audit fields
3. **Repository**: `domain/repository/ProductRepository.java` — extends `JpaRepository`
4. **DTOs**: `web/dto/CreateProductRequest.java` + `ProductResponse.java` — Java records
5. **Service**: `service/ProductService.java` — constructor injection, business logic
6. **Controller**: `web/controller/ProductController.java` — `/api/v1/products`
7. **Exception** (if needed): `domain/exception/ProductNotFoundException.java`
8. **Tests**: Unit (service) + Slice (@WebMvcTest) + Integration (@SpringBootTest)

### Entity Pattern
```java
@Entity
@Table(name = "products")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist  void onCreate()  { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate   void onUpdate()  { updatedAt = LocalDateTime.now(); }
}
```

### DTO Pattern — Java Records
```java
// Request DTO — Bean Validation annotations
public record CreateProductRequest(
    @NotBlank String name,
    @Positive BigDecimal price
) {}

// Response DTO — immutable
public record ProductResponse(UUID id, String name, BigDecimal price) {
    public static ProductResponse from(Product entity) {
        return new ProductResponse(entity.getId(), entity.getName(), entity.getPrice());
    }
}
```

---

## Design & Architecture Principles

### Constructor Injection — ALWAYS
```java
@Service
public class ProductService {
    private final ProductRepository productRepository;

    // ✅ Constructor injection (implicit @Autowired for single constructor)
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
}

// ❌ NEVER use @Autowired on fields
```

### REST Controller Pattern
```java
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create product")
    public ProductResponse create(@Valid @RequestBody CreateProductRequest request) {
        return productService.create(request);
    }
}
```

### Pagination
```java
@GetMapping
public Page<ProductResponse> list(@PageableDefault(size = 20) Pageable pageable) {
    return productService.findAll(pageable).map(ProductResponse::from);
}
```

---

## Error Handling

### Domain Exceptions
```java
// All domain exceptions extend AppException
public class AppException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus status;
}

// Specific exceptions
public class ProductNotFoundException extends AppException {
    public ProductNotFoundException(UUID id) {
        super("Product not found: " + id, "PRODUCT_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}
```

### Global Exception Handler
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiError> handleAppException(AppException ex) {
        return ResponseEntity.status(ex.getStatus())
            .body(new ApiError(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        // Return field-level validation errors
    }
}
```

### Rules
- All domain exceptions extend `AppException` (has errorCode + HttpStatus)
- Validation errors return field-level details
- NEVER let raw stack traces reach the client

---

## Code Quality

### Naming Conventions
| Artifact | Convention | Example |
|----------|-----------|---------|
| Entity | PascalCase | `Product.java` |
| Repository | `*Repository` | `ProductRepository.java` |
| Service | `*Service` | `ProductService.java` |
| Controller | `*Controller` | `ProductController.java` |
| Request DTO | `*Request` record | `CreateProductRequest.java` |
| Response DTO | `*Response` record | `ProductResponse.java` |
| Exception | `*Exception` | `ProductNotFoundException.java` |
| Migration | `V{n}__description.sql` | `V2__create_products.sql` |

### Java 21 Features — Use Them
- Records for DTOs (immutable, concise)
- Pattern matching for `instanceof`
- Switch expressions
- Text blocks for multi-line strings
- Sealed classes where appropriate

---

## Testing Strategy

| Level | What | Annotation | Tool |
|-------|------|-----------|------|
| Unit | Services, domain logic | `@ExtendWith(MockitoExtension)` | Mockito |
| Slice | Controllers (HTTP layer) | `@WebMvcTest` | MockMvc |
| Integration | Full stack | `@SpringBootTest` + `@ActiveProfiles("test")` | H2 |

### Key Testing Rules
- Use `@MockitoBean` (Spring Boot 3.4+), NOT `@MockBean`
- Test profile: `src/test/resources/application-test.yml`
- H2 for integration tests (mirrors PostgreSQL SQL dialect)
- Test ALL validation constraints on request DTOs
- Test ALL exception handler mappings

---

## Security & Performance

### Security
- Stateless JWT (no sessions)
- Public paths: `/api/v1/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`, `/actuator/health`
- All other endpoints require valid Bearer token → 401 if missing/invalid
- Sensitive config via `${ENV_VAR:default}` pattern
- NEVER log secrets or tokens

### Performance
- `JpaRepository` pagination prevents full table scans
- Use `@EntityGraph` to prevent N+1 queries
- Use DTOs (records) — never return entities directly to API
- Flyway `ddl-auto: validate` — Flyway manages schema, JPA validates only
- UUID primary keys — no sequential ID exposure

---

## Commands

| Action | Command |
|--------|---------|
| Dev server | `./mvnw spring-boot:run` |
| Build | `./mvnw clean package` |
| Test | `./mvnw test` |
| Lint | `./mvnw checkstyle:check` |
| Skip test build | `./mvnw clean package -DskipTests` |

---

## Prohibitions — NEVER Do These

1. **NEVER** use `@Autowired` on fields — constructor injection only
2. **NEVER** use `@MockBean` — use `@MockitoBean` (Spring Boot 3.4+)
3. **NEVER** return JPA entities from controllers — use DTOs (records)
4. **NEVER** write native SQL unless JPQL is truly insufficient
5. **NEVER** use `ddl-auto: create/update` — Flyway manages schema
6. **NEVER** let stack traces reach clients — `GlobalExceptionHandler` catches all
7. **NEVER** use `Optional.get()` without check — use `orElseThrow()`
8. **NEVER** put business logic in controllers — delegate to services
9. **NEVER** use mutable DTOs — Java records are immutable by design
10. **NEVER** hardcode secrets — use environment variables
