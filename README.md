# EduLingua - Multi-Module Spring Boot Application

## 🏗️ Architecture

This is a multi-module Maven project built with Kotlin, Spring Boot, and Hibernate following best practices.

### Modules

#### 1. **core** - Business Logic Layer
- **Purpose**: Contains domain models, DTOs, repositories, and business logic
- **Location**: `core/`
- **Responsibilities**:
  - Domain entities (JPA entities)
  - Data Transfer Objects (DTOs)
  - Repository interfaces
  - Service layer (business logic)
- **Package Structure**:
  ```
  com.edulingua.core
  ├── domain/      # JPA Entities
  ├── dto/         # Data Transfer Objects
  ├── repository/  # Spring Data JPA Repositories
  └── service/     # Business Logic Services
  ```

#### 2. **api-services** - REST API Layer
- **Purpose**: Exposes REST endpoints and handles HTTP requests
- **Location**: `api-services/`
- **Dependencies**: Depends on `core` module
- **Responsibilities**:
  - REST Controllers
  - Exception handling
  - Configuration
  - Application entry point
- **Package Structure**:
  ```
  com.edulingua.api
  ├── controller/  # REST Controllers
  ├── config/      # Configuration classes
  └── EduLinguaApplication.kt  # Main Spring Boot Application
  ```

## 🛠️ Technology Stack

- **Language**: Kotlin 1.9.22
- **Framework**: Spring Boot 3.2.2
- **ORM**: Hibernate (via Spring Data JPA)
- **Database**: H2 (development), configurable for PostgreSQL/MySQL
- **Build Tool**: Maven
- **Java Version**: 17

## 📦 Dependencies

### Core Module Dependencies
- Spring Data JPA (Hibernate)
- Spring Validation
- Jackson Kotlin Module
- Kotlin stdlib & reflect

### API Services Module Dependencies
- Core module (internal dependency)
- Spring Boot Web (REST APIs)
- Spring Boot Actuator (health checks)
- Spring Boot DevTools (hot reload)
- H2 Database (development)

## 🚀 Getting Started

### Prerequisites
- JDK 17 or higher
- Maven 3.6+

### Build the Project
```bash
mvn clean install
```

### Run the Application
```bash
cd api-services
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api`

## 📡 API Endpoints

### User Management
- **POST** `/api/users` - Create a new user
- **GET** `/api/users` - Get all users
- **GET** `/api/users/{id}` - Get user by ID
- **PUT** `/api/users/{id}` - Update user
- **DELETE** `/api/users/{id}` - Delete user

### Health Check
- **GET** `/api/actuator/health` - Application health status

### H2 Console
- **URL**: `http://localhost:8080/api/h2-console`
- **JDBC URL**: `jdbc:h2:mem:edulingua`
- **Username**: `sa`
- **Password**: (empty)

## 📝 Sample API Requests

### Create User
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "name": "John Doe",
    "password": "password123"
  }'
```

### Get All Users
```bash
curl http://localhost:8080/api/users
```

### Get User by ID
```bash
curl http://localhost:8080/api/users/1
```

## 🏛️ Design Principles

1. **Separation of Concerns**: Business logic (core) is separated from API layer (api-services)
2. **Dependency Direction**: api-services depends on core, not vice versa
3. **Clean Architecture**: Each layer has a specific responsibility
4. **DTOs**: Data Transfer Objects separate internal domain models from API contracts
5. **Repository Pattern**: Data access abstraction through Spring Data JPA
6. **Service Layer**: Business logic encapsulation
7. **Exception Handling**: Centralized error handling with @RestControllerAdvice

## 📂 Project Structure
```
edulingua/
├── pom.xml (parent)
├── core/
│   ├── pom.xml
│   └── src/main/kotlin/com/edulingua/core/
│       ├── domain/         # Entities
│       ├── dto/            # DTOs
│       ├── repository/     # Repositories
│       └── service/        # Business Logic
└── api-services/
    ├── pom.xml
    └── src/
        ├── main/kotlin/com/edulingua/api/
        │   ├── controller/    # REST Controllers
        │   ├── config/        # Configuration
        │   └── EduLinguaApplication.kt
        └── resources/
            └── application.yml
```

## 🔧 Configuration

### Database Configuration
Edit `api-services/src/main/resources/application.yml` to configure database settings.

### Switching to PostgreSQL
1. Add PostgreSQL dependency to `api-services/pom.xml`
2. Update datasource URL in `application.yml`
3. Change Hibernate dialect

## 🧪 Testing
```bash
mvn test
```

## 📌 Next Steps

- [ ] Add authentication & authorization (Spring Security)
- [ ] Add password encryption
- [ ] Add API documentation (Swagger/OpenAPI)
- [ ] Add integration tests
- [ ] Configure production database
- [ ] Add caching layer
- [ ] Add logging configuration
- [ ] Set up CI/CD pipeline

## 👨‍💻 Development

### Adding New Features

1. **Add Domain Model**: Create entity in `core/domain/`
2. **Add DTOs**: Create DTOs in `core/dto/`
3. **Add Repository**: Create repository interface in `core/repository/`
4. **Add Service**: Implement business logic in `core/service/`
5. **Add Controller**: Create REST endpoints in `api-services/controller/`

## 📄 License

This project is private and proprietary.
