---
name: architecture
type: knowledge
version: 1.0.0
agent: CodeActAgent
triggers:
  - architecture
  - spring boot
  - jpa
  - controller
  - service
  - repository
---

# Architecture — Java (Spring Boot 3.4 + JPA)

## Spring Boot Layered Architecture

```
src/main/java/com/example/app/
├── Application.java          ← @SpringBootApplication entry
├── config/                   ← Security, CORS, OpenAPI config
│   ├── SecurityConfig.java
│   └── WebConfig.java
├── controller/               ← REST controllers (@RestController)
│   └── UserController.java
├── service/                  ← Business logic (@Service)
│   ├── UserService.java
│   └── impl/
│       └── UserServiceImpl.java
├── repository/               ← Spring Data JPA (@Repository)
│   └── UserRepository.java
├── entity/                   ← JPA entities (@Entity)
│   └── User.java
├── dto/                      ← Request/Response DTOs (records)
│   ├── CreateUserRequest.java
│   └── UserResponse.java
├── exception/                ← Custom exceptions + handler
│   ├── AppException.java
│   └── GlobalExceptionHandler.java
└── mapper/                   ← Entity ↔ DTO mappers
    └── UserMapper.java
```

## Controller → Service → Repository

```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserResponse> getAll() {
        return userService.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable UUID id) {
        return userService.getById(id);
    }
}
```

## JPA Entity

```java
@Entity
@Table(name = "users")
@Getter @Setter
public class User {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
```

## Spring Data JPA Repository

```java
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByNameContainingIgnoreCase(String name);
}
```

## Database Migrations — Flyway

```
src/main/resources/db/migration/
├── V1__create_users_table.sql
├── V2__add_email_index.sql
```

## Rules

- Controller → Service → Repository layering.
- DTOs as Java records (immutable).
- `@Valid` for request validation (Jakarta Bean Validation).
- Flyway for all schema changes — never JPA auto-DDL in production.
