# Project: Spring Boot REST API Master Template

## Architecture

Layered Spring Boot 3.4 REST API with JWT authentication, PostgreSQL, and Flyway migrations.

```
src/main/java/com/example/myapp/
├── config/          → Spring configuration (Security, CORS, OpenAPI)
├── domain/
│   ├── entity/      → JPA entities (Lombok @Getter/@Setter/@Builder)
│   ├── repository/  → Spring Data JPA repositories
│   └── exception/   → Domain exceptions (extend AppException)
├── security/        → JWT provider + authentication filter
├── service/         → Business logic (@Service, constructor injection)
├── web/
│   ├── controller/  → REST controllers (@RestController)
│   ├── dto/         → Request/Response records (Java records + Bean Validation)
│   └── advice/      → Global exception handler (@RestControllerAdvice)
└── MyAppApplication.java
```

## Key Conventions

### General
- Java 21, Spring Boot 3.4.x, Maven with Maven Wrapper (./mvnw)
- Constructor injection everywhere — NEVER use @Autowired on fields
- Use Java records for DTOs — immutable by design
- Lombok only on entities/config — @Getter, @Setter, @Builder, @RequiredArgsConstructor
- Package-by-feature structure under `com.example.myapp`

### REST API
- All endpoints under `/api/v1/` prefix
- Use @Valid on request bodies for Bean Validation
- Return proper HTTP status codes: 201 Created, 204 No Content, 400, 401, 404, 409
- Pagination via Spring Data's Pageable — use @PageableDefault
- Document endpoints with @Operation and @Tag (SpringDoc OpenAPI)

### Database & Persistence
- PostgreSQL in production, H2 for tests
- Flyway migrations in `src/main/resources/db/migration/` — naming: `V{n}__{description}.sql`
- JPA ddl-auto: validate (Flyway manages schema)
- UUID primary keys — generated with `GenerationType.UUID`
- Audit fields: createdAt + updatedAt with @PrePersist/@PreUpdate
- NEVER write native queries unless JPA/JPQL is insufficient

### Security
- Stateless JWT authentication (no sessions)
- JwtTokenProvider handles token generation/validation
- JwtAuthFilter extracts Bearer token from Authorization header
- Public paths: /api/v1/auth/**, /swagger-ui/**, /v3/api-docs/**, /actuator/health
- All other endpoints require valid JWT
- Unauthenticated requests return 401 (not 403)

### Error Handling
- All domain exceptions extend AppException (has errorCode + HttpStatus)
- GlobalExceptionHandler maps exceptions to ApiError response records
- Validation errors return field-level error details
- NEVER let raw stack traces reach the client

### Testing
- Unit tests: Mockito (@ExtendWith(MockitoExtension.class)) for services
- Slice tests: @WebMvcTest for controllers (with MockMvc)
- Integration tests: @SpringBootTest with @ActiveProfiles("test") and H2
- Test profile: src/test/resources/application-test.yml
- Use @MockitoBean (Spring Boot 3.4+) instead of @MockBean

### Adding a New Feature
1. Create Flyway migration: `V{next}__description.sql`
2. Create JPA entity in `domain/entity/`
3. Create repository in `domain/repository/`
4. Create request/response DTOs as Java records in `web/dto/`
5. Create service in `service/` with constructor injection
6. Create controller in `web/controller/` under `/api/v1/`
7. Add exceptions in `domain/exception/` if needed
8. Write tests: unit (service) + slice (controller) + integration

### Configuration
- `application.yml` → common config (all profiles)
- `application-dev.yml` → local development (localhost DB, debug logging)
- `application-prod.yml` → production (env vars, no swagger)
- `application-test.yml` → test profile (H2, Flyway disabled)
- Sensitive values via environment variables: `${ENV_VAR:default}`
