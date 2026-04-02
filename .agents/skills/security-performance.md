---
name: security-performance
type: knowledge
version: 1.0.0
agent: CodeActAgent
triggers:
  - security
  - performance
  - spring security
  - jwt
  - jpa optimization
  - cache
---

# Security & Performance — Java + Spring Boot

## Performance

### JPA Query Optimization

```java
// N+1 prevention — fetch join
@Query("SELECT u FROM User u JOIN FETCH u.orders WHERE u.id = :id")
Optional<User> findByIdWithOrders(@Param("id") UUID id);

// Projection — fetch only needed fields
@Query("SELECT new com.example.dto.UserSummary(u.id, u.name) FROM User u")
List<UserSummary> findAllSummaries();

// Pagination
Page<User> findByNameContaining(String name, Pageable pageable);
```

### Caching

```java
@Service
@RequiredArgsConstructor
public class ProductService {
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getById(UUID id) { ... }

    @CacheEvict(value = "products", key = "#id")
    public void update(UUID id, UpdateProductRequest req) { ... }
}
```

### Connection Pooling (HikariCP)

```yaml
spring.datasource.hikari:
  maximum-pool-size: 20
  minimum-idle: 5
  connection-timeout: 30000
```

## Security

### Spring Security + JWT

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())  // API-only, JWT-based
            .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

### Input Validation

```java
// Jakarta Bean Validation on DTOs
public record CreateUserRequest(
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Email String email
) {}

// @Valid in controller triggers validation
public UserResponse create(@Valid @RequestBody CreateUserRequest request)
```

### SQL Injection Prevention

- Spring Data JPA parameterizes all queries automatically.
- `@Query` with `:param` — safe parameterized JPQL.
- **Never** concatenate strings in `@Query` or `EntityManager.createNativeQuery`.

### Secrets

- `application-prod.yml` loaded from env: `${DATABASE_URL}`.
- Spring Cloud Vault for secret rotation.
- Never commit `application-prod.yml` with real values.

### CORS

```java
@Bean
public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/api/**")
                .allowedOrigins("https://myapp.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE");
        }
    };
}
```
