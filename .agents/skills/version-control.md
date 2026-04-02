---
name: version-control
type: knowledge
version: 1.0.0
agent: CodeActAgent
triggers:
  - git
  - commit
  - ci
  - maven
  - deploy
---

# Version Control — Java + Spring Boot

## Commits (Conventional)

- `feat(users): add pagination support`
- `fix(auth): handle expired refresh tokens`
- `db: add V3 migration for orders table`

## CI Pipeline (Maven)

1. `mvn dependency:resolve`
2. `mvn compile` — compile
3. `mvn test` — unit tests
4. `mvn verify` — integration tests (Testcontainers)
5. `mvn package -DskipTests` — build JAR
6. `mvn spring-boot:build-image` — Docker image

## Maven Wrapper

```bash
./mvnw clean package        # Build
./mvnw spring-boot:run      # Run locally
./mvnw test                 # Tests
./mvnw verify               # All tests including integration
```

## .gitignore

```
target/
.idea/
*.iml
.env
*.class
```

## Docker

```dockerfile
FROM eclipse-temurin:21-jre-alpine
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Profiles

```yaml
# application.yml
spring.profiles.active: ${SPRING_PROFILES_ACTIVE:dev}

# application-dev.yml
spring.datasource.url: jdbc:postgresql://localhost:5432/myapp

# application-prod.yml
spring.datasource.url: ${DATABASE_URL}
```
