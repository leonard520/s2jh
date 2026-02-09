````skill
---
name: Target Project Scaffolding
description: Create a new project with target technology stack for rewrite migration.
mode: rewrite
---

## Overview

This skill creates a fresh project with the target technology stack, configured with modern best practices. Unlike upgrade mode, you directly use the target JDK and framework versions without version stepping.

## User Input

```text
$ARGUMENTS
```

You **MUST** consider the user input before proceeding (if not empty).

## When to Use

- **Mode**: REWRITE only
- **Phase**: Step 3 (Mode-Specific Analysis) - Target Project Scaffolding
- **Prerequisites**: Business Logic Inventory completed

## Technology Stack Selection

### Common Target Stacks

| Source Stack | Recommended Target | JDK | Key Changes |
|--------------|-------------------|-----|-------------|
| Struts 2 | Spring Boot 3.x | 17+ | MVC framework |
| JSF 2.x | Spring Boot 3.x | 17+ | REST or Thymeleaf |
| EJB 3.x | Spring Boot 3.x | 17+ | DI framework |
| Java EE 7/8 | Jakarta EE 10 or Spring Boot 3.x | 17+ | Namespace changes |
| Legacy Spring | Spring Boot 3.x | 17+ | Boot conventions |

### JDK Version Guidelines

| Target JDK | LTS Until | Features |
|------------|-----------|----------|
| JDK 17 | 2029+ | Records, Sealed classes, Pattern matching |
| JDK 21 | 2031+ | Virtual threads, Sequenced collections |

**Recommendation**: Use JDK 21 for new projects unless specific compatibility requirements exist.

## Scaffolding Process

### Step 1: Define Target Configuration

Create `FEATURE_DIR/target-config.yaml`:

```yaml
target_configuration:
  project_name: "[NEW_PROJECT_NAME]"
  
  jdk:
    version: 21
    vendor: "Eclipse Temurin"  # or Oracle, Amazon Corretto, etc.
  
  framework:
    name: "Spring Boot"
    version: "3.2.x"
  
  build_tool:
    name: "Maven"  # or Gradle
    version: "3.9.x"
  
  project_structure:
    type: "multi-module"  # or single-module
    modules:
      - name: "api"
        description: "REST API controllers"
      - name: "service"
        description: "Business logic services"
      - name: "persistence"
        description: "Data access layer"
      - name: "common"
        description: "Shared utilities and DTOs"
  
  dependencies:
    - "spring-boot-starter-web"
    - "spring-boot-starter-data-jpa"
    - "spring-boot-starter-validation"
    - "spring-boot-starter-test"
    - "lombok"  # optional
    - "mapstruct"  # optional
  
  database:
    type: "PostgreSQL"  # or MySQL, Oracle, etc.
    migration_tool: "Flyway"  # or Liquibase
```

### Step 2: Generate Project Structure

#### Option A: Use Spring Initializr (Recommended)

```bash
# Generate project using Spring Initializr
curl https://start.spring.io/starter.zip \
  -d type=maven-project \
  -d language=java \
  -d bootVersion=3.2.0 \
  -d baseDir=[PROJECT_NAME] \
  -d groupId=com.example \
  -d artifactId=[PROJECT_NAME] \
  -d name=[PROJECT_NAME] \
  -d packageName=com.example.[PACKAGE] \
  -d javaVersion=21 \
  -d dependencies=web,data-jpa,validation,lombok \
  -o [PROJECT_NAME].zip

unzip [PROJECT_NAME].zip
```

#### Option B: Manual Creation

```
[PROJECT_NAME]/
├── pom.xml                          # Parent POM
├── api/
│   ├── pom.xml
│   └── src/main/java/
│       └── com/example/api/
│           └── controller/
├── service/
│   ├── pom.xml
│   └── src/main/java/
│       └── com/example/service/
├── persistence/
│   ├── pom.xml
│   └── src/main/java/
│       └── com/example/persistence/
│           ├── entity/
│           └── repository/
├── common/
│   ├── pom.xml
│   └── src/main/java/
│       └── com/example/common/
│           ├── dto/
│           └── util/
└── application/
    ├── pom.xml
    └── src/main/java/
        └── com/example/
            └── Application.java
```

### Step 3: Configure Base Files

#### pom.xml (Parent)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>com.example</groupId>
    <artifactId>[PROJECT_NAME]-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    
    <properties>
        <java.version>21</java.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
    </properties>
    
    <modules>
        <module>common</module>
        <module>persistence</module>
        <module>service</module>
        <module>api</module>
        <module>application</module>
    </modules>
    
    <dependencyManagement>
        <dependencies>
            <!-- Internal modules -->
            <dependency>
                <groupId>com.example</groupId>
                <artifactId>[PROJECT_NAME]-common</artifactId>
                <version>${project.version}</version>
            </dependency>
            <!-- Add other managed dependencies -->
        </dependencies>
    </dependencyManagement>
</project>
```

#### Application.java

```java
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

#### application.yml

```yaml
spring:
  application:
    name: [PROJECT_NAME]
  
  datasource:
    url: jdbc:postgresql://localhost:5432/[DB_NAME]
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
    properties:
      hibernate:
        format_sql: true
  
  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: 8080

logging:
  level:
    com.example: DEBUG
    org.springframework: INFO
```

### Step 4: Add Infrastructure

#### Logging Configuration

```xml
<!-- logback-spring.xml -->
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
    
    <logger name="com.example" level="DEBUG"/>
</configuration>
```

#### Exception Handling

```java
package com.example.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal server error",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    // Add more specific exception handlers
}
```

### Step 5: Verify Scaffold

Run verification commands:

```bash
# Verify build
cd [PROJECT_NAME]
./mvnw clean compile

# Verify tests run (should pass with no tests)
./mvnw test

# Verify application starts
./mvnw spring-boot:run

# Check endpoints
curl http://localhost:8080/actuator/health
```

## Scaffold Checklist

- [ ] Project structure created
- [ ] Parent POM configured with correct versions
- [ ] All module POMs created with dependencies
- [ ] Application main class created
- [ ] application.yml/properties configured
- [ ] Logging configured
- [ ] Global exception handler created
- [ ] Database migration folder created
- [ ] Build succeeds (`mvn clean compile`)
- [ ] Application starts (`mvn spring-boot:run`)
- [ ] Health check responds (`/actuator/health`)

## Output Artifacts

| Artifact | Path | Purpose |
|----------|------|---------|
| Target Config | `FEATURE_DIR/target-config.yaml` | Target stack configuration |
| Project Root | `[PROJECT_NAME]/` | Scaffolded project |
| Build Verification | `FEATURE_DIR/scaffold-verification.md` | Verification results |

## Integration with Workflow

After scaffolding:
1. Create `FEATURE_DIR/scaffold-verification.md` documenting successful setup
2. Proceed to implementation phase
3. Reference `business-logic-inventory.md` for what to implement
4. Use `target-config.yaml` for any configuration questions
````
