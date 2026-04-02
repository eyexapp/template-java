---
name: testing
type: knowledge
version: 1.0.0
agent: CodeActAgent
triggers:
  - test
  - junit
  - mockito
  - testcontainers
  - spring test
---

# Testing — Java (JUnit 5 + Mockito + Testcontainers)

## Service Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock UserRepository userRepository;
    @InjectMocks UserServiceImpl userService;

    @Test
    void shouldCreateUser() {
        var request = new CreateUserRequest("Alice", "alice@test.com", "password");
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> {
            var user = inv.getArgument(0, User.class);
            user.setId(UUID.randomUUID());
            return user;
        });

        var result = userService.create(request);

        assertThat(result.name()).isEqualTo("Alice");
        verify(userRepository).save(any());
    }

    @Test
    void shouldThrowOnDuplicateEmail() {
        when(userRepository.existsByEmail("a@b.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(new CreateUserRequest("Bob", "a@b.com", "pass")))
            .isInstanceOf(ConflictException.class);
    }
}
```

## Controller Integration Tests

```java
@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean UserService userService;

    @Test
    void shouldReturn201OnCreate() throws Exception {
        when(userService.create(any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "Alice", "email": "a@b.com", "password": "password123"}
                """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Alice"));
    }
}
```

## Testcontainers (DB Integration)

```java
@SpringBootTest
@Testcontainers
class UserRepositoryIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired UserRepository userRepository;

    @Test
    void shouldFindByEmail() {
        // test with real DB
    }
}
```

## Rules

- `@ExtendWith(MockitoExtension.class)` for unit tests.
- `@WebMvcTest` for controller tests (mock service layer).
- `@SpringBootTest` + Testcontainers for DB integration.
- AssertJ for fluent assertions: `assertThat(x).isEqualTo(y)`.
