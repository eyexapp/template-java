---
name: code-quality
type: knowledge
version: 1.0.0
agent: CodeActAgent
triggers:
  - clean code
  - naming
  - lombok
  - dto
  - validation
  - exception handler
---

# Code Quality — Java 21 + Spring Boot 3.4

## Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| Class | PascalCase | `UserService` |
| Interface | PascalCase (no I prefix) | `UserRepository` |
| Method | camelCase | `findByEmail()` |
| Package | lowercase | `com.example.app.service` |
| Constant | UPPER_SNAKE | `MAX_PAGE_SIZE` |
| DTO | PascalCase + purpose | `CreateUserRequest` |
| Entity | singular PascalCase | `User` |
| Controller | noun + Controller | `UserController` |

## DTO as Records

```java
public record CreateUserRequest(
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String password
) {}

public record UserResponse(UUID id, String name, String email, Instant createdAt) {
    public static UserResponse from(User entity) {
        return new UserResponse(entity.getId(), entity.getName(), entity.getEmail(), entity.getCreatedAt());
    }
}
```

## Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ResourceNotFoundException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
    }
}
```

## Service Pattern

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already exists");
        }
        var user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        return UserResponse.from(userRepository.save(user));
    }
}
```

## Lombok Usage

- `@RequiredArgsConstructor` — constructor injection.
- `@Getter @Setter` — on entities.
- `@Slf4j` — logger.
- **Avoid** `@Data` on entities (equals/hashCode issues with JPA).
