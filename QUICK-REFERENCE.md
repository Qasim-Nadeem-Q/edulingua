# Quick Reference Guide - EduLingua

## 🚀 Common Maven Commands

### Build Entire Project
```bash
mvn clean install
```

### Build Specific Module
```bash
# Core module
cd core
mvn clean install

# API Services module
cd api-services
mvn clean package
```

### Run Application
```bash
cd api-services
mvn spring-boot:run
```

### Run Tests
```bash
mvn test
```

### Skip Tests During Build
```bash
mvn clean install -DskipTests
```

---

## 🔧 Adding New Dependencies

### To Parent POM (edulingua/pom.xml)
Add to `<dependencyManagement>` section:
```xml
<dependency>
    <groupId>group.id</groupId>
    <artifactId>artifact-id</artifactId>
    <version>x.x.x</version>
</dependency>
```

### To Core Module (core/pom.xml)
Add to `<dependencies>` section (no version needed if managed by parent):
```xml
<dependency>
    <groupId>group.id</groupId>
    <artifactId>artifact-id</artifactId>
</dependency>
```

### To API Services Module (api-services/pom.xml)
Same as core module

---

## 📝 Adding New Features

### 1. Add New Entity (Core Module)
```kotlin
// core/src/main/kotlin/com/edulingua/core/domain/Product.kt
package com.edulingua.core.domain

import jakarta.persistence.*

@Entity
@Table(name = "products")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false)
    val name: String,
    
    val description: String? = null
)
```

### 2. Add DTOs (Core Module)
```kotlin
// core/src/main/kotlin/com/edulingua/core/dto/ProductDto.kt
package com.edulingua.core.dto

data class ProductCreateDto(
    val name: String,
    val description: String?
)

data class ProductResponseDto(
    val id: Long,
    val name: String,
    val description: String?
)
```

### 3. Add Repository (Core Module)
```kotlin
// core/src/main/kotlin/com/edulingua/core/repository/ProductRepository.kt
package com.edulingua.core.repository

import com.edulingua.core.domain.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<Product, Long>
```

### 4. Add Service (Core Module)
```kotlin
// core/src/main/kotlin/com/edulingua/core/service/ProductService.kt
package com.edulingua.core.service

import com.edulingua.core.dto.ProductCreateDto
import com.edulingua.core.dto.ProductResponseDto
import com.edulingua.core.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ProductService(
    private val productRepository: ProductRepository
) {
    // Add methods here
}
```

### 5. Add Controller (API Services Module)
```kotlin
// api-services/src/main/kotlin/com/edulingua/api/controller/ProductController.kt
package com.edulingua.api.controller

import com.edulingua.core.service.ProductService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/products")
class ProductController(
    private val productService: ProductService
) {
    // Add endpoints here
}
```

---

## 🔍 Testing Endpoints

### Using curl (Windows PowerShell)

#### Create User
```powershell
curl -X POST http://localhost:8080/api/users `
  -H "Content-Type: application/json" `
  -d '{\"email\":\"user@example.com\",\"name\":\"John Doe\",\"password\":\"password123\"}'
```

#### Get All Users
```powershell
curl http://localhost:8080/api/users
```

#### Get User by ID
```powershell
curl http://localhost:8080/api/users/1
```

#### Update User
```powershell
curl -X PUT http://localhost:8080/api/users/1 `
  -H "Content-Type: application/json" `
  -d '{\"name\":\"Jane Doe\"}'
```

#### Delete User
```powershell
curl -X DELETE http://localhost:8080/api/users/1
```

---

## 🗄️ Database Access

### H2 Console
- URL: http://localhost:8080/api/h2-console
- JDBC URL: `jdbc:h2:mem:edulingua`
- Username: `sa`
- Password: (leave empty)

### Common SQL Queries
```sql
-- View all users
SELECT * FROM users;

-- Insert user manually
INSERT INTO users (email, name, password, created_at, updated_at)
VALUES ('test@test.com', 'Test User', 'pass123', NOW(), NOW());

-- Clear all users
DELETE FROM users;
```

---

## 🔄 Switching to PostgreSQL

### 1. Add PostgreSQL Dependency
In `api-services/pom.xml`:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 2. Update application.yml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/edulingua
    username: your_username
    password: your_password
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

---

## 📊 Monitoring & Health Checks

### Actuator Endpoints
- Health: http://localhost:8080/api/actuator/health
- Info: http://localhost:8080/api/actuator/info
- Metrics: http://localhost:8080/api/actuator/metrics

---

## 🐛 Troubleshooting

### Application Won't Start
1. Check if port 8080 is already in use
2. Verify Java 17 is installed: `java -version`
3. Clean and rebuild: `mvn clean install`
4. Check logs in console for specific errors

### Build Failures
1. Ensure Maven is installed: `mvn -version`
2. Delete `.m2/repository` cache if corrupted
3. Check internet connection (Maven needs to download dependencies)

### Kotlin Compilation Issues
1. Verify Kotlin version in parent POM
2. Check kotlin-maven-plugin configuration
3. Ensure source directories are correct

---

## 📦 Project Structure Quick View

```
api-services (depends on) → core
     ↓                       ↓
Controllers            Services
     ↓                       ↓
  DTOs    ←    ←    ←    DTOs
                           ↓
                    Repositories
                           ↓
                       Entities
```

---

## 🎯 Best Practices Checklist

When adding new features:
- [ ] Create entity in `core/domain`
- [ ] Create DTOs in `core/dto` with validation
- [ ] Create repository in `core/repository`
- [ ] Create service in `core/service` with `@Transactional`
- [ ] Create controller in `api-services/controller`
- [ ] Add exception handling if needed
- [ ] Test endpoints manually
- [ ] Write unit tests
- [ ] Update README if significant changes

---

## 🔐 Security Notes (TODO)

Current setup has NO authentication. Before production:
1. Add Spring Security dependency
2. Configure authentication
3. Hash passwords (BCrypt)
4. Add JWT tokens for API
5. Secure endpoints with @PreAuthorize
6. Enable CORS properly
7. Add rate limiting

---

## 📚 Useful Links

- Spring Boot Docs: https://spring.io/projects/spring-boot
- Kotlin Docs: https://kotlinlang.org/docs/home.html
- Spring Data JPA: https://spring.io/projects/spring-data-jpa
- Hibernate Docs: https://hibernate.org/orm/documentation/

---

**Keep this file handy for quick reference!** 📖
