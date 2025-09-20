# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
This is a Spring Boot 3.3.5 application named "shop" built with Java 17. It's a music/instrument-focused e-commerce platform with post/comment functionality, member management, and AI integration for music score conversion.

## Build and Development Commands

### Building the project
```bash
./gradlew build
```

### Running the application
```bash
./gradlew bootRun
```

### Running tests
```bash
./gradlew test
```

### Running a specific test class
```bash
./gradlew test --tests "com.study.shop.domain.post.controller.PostControllerTest"
```

### Generating QueryDSL classes
QueryDSL Q-classes are generated automatically during compilation. Generated files are placed in `$buildDir/generated/querydsl`.

### API Documentation
- Swagger UI is available at `/swagger-ui.html` when the application is running
- OpenAPI 3 docs are available at `/v3/api-docs`

## Architecture

### Package Structure
The application follows Domain-Driven Design (DDD) principles:

- `domain/` - Core business domains (post, comment, member, instrument, auth)
  - Each domain contains: `dto/`, `entity/`, `repository/`, `controller/`, `service/`, `exception/`
- `global/` - Cross-cutting concerns
  - `config/` - Configuration classes (QueryDSL, JPA Auditing, Swagger, Web)
  - `security/` - Security configuration and authentication
  - `exception/` - Global exception handling
  - `response/` - Common response DTOs
- `infrastructure/` - External integrations
  - `ai/` - Music score conversion services
  - `external/` - Storage and other external services
- `api/` - API layer (member, market)
- `support/` - Utility classes, constants, and enums

### Key Technologies
- **Spring Boot 3.3.5** with Spring Web, JPA, Security, Validation
- **QueryDSL 5.0.0** for type-safe queries (Q-classes auto-generated)
- **Lombok** for reducing boilerplate code
- **H2 Database** for development/testing
- **JUnit 5** with Spring Boot Test for testing
- **SpringDoc OpenAPI** for API documentation
- **Spring Security** for authentication/authorization

### Database Configuration
- Uses Spring Data JPA with Hibernate
- H2 in-memory database for development
- JPA Auditing is configured for automatic timestamp management
- SQL formatting and logging configured for development

### Testing Strategy
- Uses `@SpringBootTest` with `@AutoConfigureMockMvc` for integration tests
- Test helpers available in `src/test/java/com/study/shop/testutil/`
- Controller tests use MockMvc for HTTP request simulation

### Configuration Profiles
- `application.yml` - Base configuration
- `application-dev.yml` - Development profile
- `application-prod.yml` - Production profile

## Development Notes

### QueryDSL Usage
- Q-classes are automatically generated in `$buildDir/generated/querydsl`
- Repository layer can use QueryDSL for complex queries
- Generated sources are included in the main source set

### AI Integration
The application includes music score conversion functionality:
- `MusicScoreConverter` - Core conversion logic  
- `MusicScoreClient` - External AI service integration

### Security
- Spring Security is configured with custom authentication
- Security configuration is in `global/security/config/`
- Authentication logic is in `global/security/auth/`

### Logging
- Root level: INFO
- Application level (`com.study.cruisin`): DEBUG  
- Hibernate SQL: WARN
- Spring framework: WARN