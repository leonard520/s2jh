# Migration Constitution: Struts2 to Spring MVC (REWRITE Mode)

**Project:** s2jh  
**Repository:** leonard520/s2jh  
**Migration Mode:** REWRITE  
**Migration Date:** 2025  
**Version:** 1.0.0

---

## 1. Executive Summary

### 1.1 Migration Objective
Re-architect the S2JH enterprise application framework from legacy Struts2 architecture to a modern Spring Boot/Spring MVC based architecture using the REWRITE approach. This migration will establish a clean, maintainable foundation while preserving business logic and domain knowledge.

### 1.2 Current State Analysis

**Technology Stack (Legacy):**
- **Web Framework:** Struts2 2.3.16.1 (Convention Plugin)
- **Core Framework:** Spring 3.2.10.RELEASE
- **Persistence:** Hibernate 4.1.8.Final + JPA 2.0 + Spring Data JPA 1.6.2
- **Build Tool:** Maven 3.x
- **Java Version:** JDK 1.6
- **View Technology:** JSP + FreeMarker 2.3.19
- **Security:** Spring Security 3.x
- **Additional:** Activiti 5.14, Quartz 2.2.1, CXF 2.7.0, JasperReports

**Project Structure:**
```
s2jh-parent (Multi-module Maven Project)
├── core-service (100 Java files)      - Core business abstractions
├── common-service (135 Java files)    - Common business modules
├── prototype (Main web app)           - Struts2 web layer
├── jasper-service                     - Reporting module
├── crawl-service                      - Web crawling module
├── core-test                          - Testing utilities
├── assets-resource                    - Static resources
└── project-tools                      - Development tools
```

**Codebase Metrics:**
- Total Java Files: 354
- Controllers (Struts2): 46
- Services: 46
- DAOs (Spring Data JPA): 37
- JSP Pages: 101
- Entity Classes: ~40+

### 1.3 Target State Vision

**Technology Stack (Target):**
- **Web Framework:** Spring Boot 3.x + Spring MVC
- **Core Framework:** Spring Framework 6.x
- **Persistence:** Hibernate 6.x + JPA 3.0 + Spring Data JPA 3.x
- **Build Tool:** Maven 4.x (or Gradle 8.x)
- **Java Version:** JDK 17+ (LTS)
- **View Technology:** Thymeleaf (or RESTful API + Modern Frontend)
- **Security:** Spring Security 6.x
- **Additional:** Replace/upgrade legacy integrations

**Architecture Pattern:**
- Clean Architecture / Hexagonal Architecture
- Domain-Driven Design (DDD) principles
- RESTful API design
- Separation of concerns (Presentation, Application, Domain, Infrastructure)

---

## 2. Migration Principles

### 2.1 Core Principles (REWRITE Mode)

**P1: Clean Slate with Preserved Business Logic**
- Start with a new Spring Boot project structure
- Extract and refactor business logic from legacy code
- Do NOT copy-paste legacy Struts2 patterns
- Preserve domain knowledge, business rules, and data models

**P2: Modern Architecture Patterns**
- Implement Clean Architecture layers:
  - **Presentation Layer:** REST Controllers (Spring MVC)
  - **Application Layer:** Use Cases / Application Services
  - **Domain Layer:** Domain Entities, Value Objects, Domain Services
  - **Infrastructure Layer:** JPA Repositories, External Integrations
- Use dependency inversion (interfaces in domain, implementations in infrastructure)

**P3: Framework-Agnostic Business Logic**
- Business logic must NOT depend on Spring, Struts2, or any framework
- Use Plain Old Java Objects (POJOs) for domain models
- Apply Domain-Driven Design patterns

**P4: API-First Design**
- Design RESTful APIs following REST maturity model
- Use standard HTTP methods (GET, POST, PUT, DELETE, PATCH)
- Implement proper status codes and error handling
- Consider API versioning from the start

**P5: Incremental Migration**
- Migrate module-by-module (vertical slices)
- Prioritize based on business value and technical risk
- Maintain legacy system until full migration
- Run both systems in parallel during transition

**P6: Data Integrity**
- Preserve existing database schema initially
- Plan schema improvements separately
- Ensure zero data loss during migration
- Implement robust migration scripts

**P7: Testing from Day One**
- Write unit tests for domain logic (80%+ coverage)
- Implement integration tests for infrastructure
- Create API contract tests
- Establish CI/CD pipeline early

**P8: Documentation as Code**
- Document architectural decisions (ADRs)
- Generate API documentation (OpenAPI/Swagger)
- Maintain living documentation
- Knowledge transfer through code and tests

---

## 3. Architecture Mapping

### 3.1 Layer Migration Strategy

#### 3.1.1 Presentation Layer (Web → REST)

**Legacy Pattern:**
```
Struts2 Convention-based Controllers
├── BaseController extends PersistableController
├── URL Mapping: Convention plugin auto-discovery
├── Result: JSP pages, JSON (Struts2-REST plugin)
├── Interceptors: Token, Parameters, Prepare
└── Action suffix: *Controller.java
```

**Target Pattern:**
```
Spring MVC REST Controllers
├── @RestController with @RequestMapping
├── URL Mapping: Explicit @GetMapping, @PostMapping, etc.
├── Response: ResponseEntity<T>, @ResponseBody
├── Validation: @Valid, BindingResult
├── Exception Handling: @ControllerAdvice, @ExceptionHandler
└── HATEOAS support (optional)
```

**Migration Rules:**
- **MR-WEB-01:** Convert Struts2 Controllers to Spring REST Controllers
  - Remove Struts2 dependencies (ActionSupport, HttpHeaders, etc.)
  - Replace Struts2 annotations with Spring annotations
  - Convert method signatures to Spring MVC patterns
  
- **MR-WEB-02:** Replace Struts2 interceptors with Spring mechanisms
  - Token validation → Spring Security CSRF
  - Parameter binding → Spring @RequestParam, @RequestBody
  - Exception handling → @ControllerAdvice
  
- **MR-WEB-03:** Standardize REST API design
  - Use consistent URL patterns: `/api/v1/{resource}`
  - Apply HTTP verb semantics properly
  - Return proper status codes (200, 201, 204, 400, 404, 500)
  
- **MR-WEB-04:** Implement DTOs for API contracts
  - Separate domain entities from API DTOs
  - Use MapStruct or similar for entity-DTO mapping
  - Validate DTOs with Bean Validation 3.0

#### 3.1.2 Application Layer (Service → Use Cases)

**Legacy Pattern:**
```
Service Layer
├── BaseService<T, ID> (Generic CRUD operations)
├── @Service annotation
├── Direct JPA Repository usage
├── Transaction management: @Transactional
└── Business logic mixed with persistence logic
```

**Target Pattern:**
```
Application Layer
├── Use Case / Application Service classes
├── Command/Query Separation (CQRS optional)
├── Input/Output ports (interfaces)
├── Transaction boundaries defined here
└── Orchestration of domain logic
```

**Migration Rules:**
- **MR-APP-01:** Extract business logic from legacy services
  - Identify pure business logic vs. infrastructure code
  - Move business logic to domain layer
  - Keep orchestration in application layer
  
- **MR-APP-02:** Implement Use Case pattern
  - One use case = One business operation
  - Clear input (command/query) and output (result)
  - Example: `CreateUserUseCase`, `UpdateProductUseCase`
  
- **MR-APP-03:** Define port interfaces
  - Input ports: Interfaces for use cases
  - Output ports: Interfaces for repositories, external services
  - Dependency inversion: Interfaces in application/domain, implementations in infrastructure

#### 3.1.3 Domain Layer (Entity → Domain Model)

**Legacy Pattern:**
```
JPA Entities
├── BaseEntity<ID>, BaseUuidEntity, BaseNativeEntity
├── JPA annotations (@Entity, @Table, @Column)
├── Getters/Setters (JavaBean pattern)
├── Audit fields: createdBy, createdDate, lastModifiedBy, lastModifiedDate
└── Custom annotations: @MetaData, @EntityAutoCode
```

**Target Pattern:**
```
Domain Model
├── Rich domain entities (behavior, not just data)
├── Value Objects (immutable, no identity)
├── Domain Services (when logic doesn't fit in entity)
├── Domain Events (for cross-aggregate communication)
├── Aggregates (consistency boundaries)
└── Specification pattern (complex queries)
```

**Migration Rules:**
- **MR-DOM-01:** Create rich domain models
  - Add business methods to entities
  - Encapsulate business rules
  - Avoid anemic domain model
  
- **MR-DOM-02:** Separate persistence from domain
  - Keep JPA annotations in infrastructure layer
  - Use separate persistence entities if needed
  - Map between domain and persistence models
  
- **MR-DOM-03:** Implement Value Objects
  - Extract multi-field concepts (Address, Money, DateRange)
  - Make them immutable
  - Override equals/hashCode based on value
  
- **MR-DOM-04:** Define Aggregates
  - Identify aggregate roots
  - Enforce invariants within aggregates
  - Access child entities only through aggregate root
  
- **MR-DOM-05:** Use Domain Events
  - Publish events for significant domain occurrences
  - Enable loose coupling between aggregates
  - Example: UserCreatedEvent, OrderPlacedEvent

#### 3.1.4 Infrastructure Layer (DAO → Repository)

**Legacy Pattern:**
```
Spring Data JPA Repositories
├── BaseDao<T, ID> extends JpaRepository
├── Custom query methods (method naming)
├── @Query annotations for complex queries
├── Direct entity manipulation
└── QueryDSL for dynamic queries
```

**Target Pattern:**
```
Infrastructure Layer
├── JPA Repository implementations (adapters)
├── Implements domain repository interfaces
├── Separate read and write models (CQRS optional)
├── Database-specific optimizations
└── External service integrations
```

**Migration Rules:**
- **MR-INF-01:** Implement repository interfaces
  - Define interfaces in domain layer
  - Implement in infrastructure layer
  - Use Spring Data JPA as implementation detail
  
- **MR-INF-02:** Optimize database access
  - Use projections for read-only queries
  - Implement batch operations
  - Apply N+1 query prevention
  
- **MR-INF-03:** Externalize configuration
  - Use Spring Boot configuration properties
  - Externalize database credentials
  - Environment-specific profiles (dev, test, prod)

### 3.2 Cross-Cutting Concerns

#### 3.2.1 Security Migration

**Legacy:** Spring Security 3.x + Struts2 integration  
**Target:** Spring Security 6.x + JWT/OAuth2

**Migration Rules:**
- **MR-SEC-01:** Upgrade authentication
  - Replace form-based auth with JWT tokens (if RESTful API)
  - Implement OAuth2/OIDC for SSO scenarios
  - Use BCrypt for password encoding (if not already)
  
- **MR-SEC-02:** Authorization model
  - Keep role-based access control (RBAC)
  - Use Spring Security method security (@PreAuthorize)
  - Implement permission-based authorization if needed
  
- **MR-SEC-03:** CSRF protection
  - Enable for state-changing operations
  - Use double-submit cookie pattern for SPA
  - Configure CORS properly

#### 3.2.2 Transaction Management

**Legacy:** Spring @Transactional  
**Target:** Spring @Transactional (no change, but clarify boundaries)

**Migration Rules:**
- **MR-TXN-01:** Transaction boundaries at application layer
  - Do NOT start transactions in controllers
  - Use case / application service = transaction boundary
  - Keep transactions as short as possible

#### 3.2.3 Logging and Monitoring

**Legacy:** SLF4J + Logback  
**Target:** SLF4J + Logback + Spring Boot Actuator

**Migration Rules:**
- **MR-LOG-01:** Structured logging
  - Use JSON format for production
  - Include correlation IDs
  - Log levels: ERROR (user-facing), WARN (degradation), INFO (significant events), DEBUG (details)
  
- **MR-LOG-02:** Monitoring
  - Enable Spring Boot Actuator
  - Expose health, metrics, info endpoints
  - Integrate with Prometheus/Grafana (optional)

#### 3.2.4 Validation

**Legacy:** Hibernate Validator 4.x + Struts2 validation  
**Target:** Bean Validation 3.0 (Jakarta)

**Migration Rules:**
- **MR-VAL-01:** Validation layers
  - Input validation: DTOs (@NotNull, @Size, etc.)
  - Business validation: Domain entities and services
  - Separate concerns clearly

---

## 4. Module Migration Strategy

### 4.1 Migration Priority

**Phase 1: Foundation (Weeks 1-2)**
1. Setup new Spring Boot project
2. Configure build (Maven/Gradle)
3. Setup CI/CD pipeline
4. Database connectivity and migrations

**Phase 2: Core Domain (Weeks 3-6)**
1. **core-service migration:**
   - Extract domain models from BaseEntity hierarchy
   - Implement core repositories
   - Core business logic

**Phase 3: Common Modules (Weeks 7-10)**
1. **common-service migration:**
   - System management (Menu, DataDict, ConfigProperty)
   - User/Role/Permission (auth module)
   - Reporting (rpt module)
   - File attachments

**Phase 4: Web Layer (Weeks 11-14)**
1. **prototype migration:**
   - REST API implementation
   - Authentication/Authorization
   - API documentation

**Phase 5: Specialized Services (Weeks 15-18)**
1. **jasper-service:** Reporting migration
2. **crawl-service:** Web crawling migration
3. Integration testing

**Phase 6: Testing & Deployment (Weeks 19-20)**
1. End-to-end testing
2. Performance testing
3. Production deployment
4. Legacy system decommission

### 4.2 Module-Specific Rules

#### 4.2.1 core-service Module

**Responsibilities:**
- Base entity abstractions
- Generic CRUD operations
- Pagination and search
- Audit support
- Security utilities
- Core web utilities

**Migration Approach:**
- Extract domain-agnostic patterns
- Implement as shared library (separate module)
- Remove Struts2 dependencies completely
- Modernize to Spring Boot patterns

#### 4.2.2 common-service Module

**Responsibilities:**
- System management (Menu, DataDict, ConfigProperty)
- Authentication/Authorization (User, Role, Privilege)
- Logging (LoggingEvent)
- File management (AttachmentFile)
- Notifications (PubPost)
- Reporting (ReportDef, ReportParam)

**Migration Approach:**
- Treat as bounded contexts (DDD)
- Each functional area = separate package/module
- Define clear module boundaries
- Implement module-level APIs

**Modules to Extract:**
1. **System Module:** Menu, DataDict, ConfigProperty
2. **Auth Module:** User, Role, Privilege, Department
3. **Logging Module:** LoggingEvent
4. **File Module:** AttachmentFile
5. **Notification Module:** PubPost, PubPostRead
6. **Reporting Module:** ReportDef, ReportParam

#### 4.2.3 prototype Module (Web Layer)

**Responsibilities:**
- Struts2 web application
- JSP views
- Web configuration

**Migration Approach:**
- Replace entirely with Spring Boot application
- Implement REST API endpoints
- Consider modern frontend (React, Vue, Angular) or Thymeleaf
- Extract all business logic before deletion

---

## 5. Technology Upgrade Guidelines

### 5.1 Java Version Upgrade

**Current:** JDK 1.6  
**Target:** JDK 17 (LTS) or JDK 21 (LTS)

**Upgrade Steps:**
1. Update to JDK 11 first (intermediate step)
2. Fix deprecated API usage
3. Update to JDK 17
4. Leverage new language features (records, sealed classes, pattern matching)

### 5.2 Spring Framework Upgrade

**Current:** Spring 3.2.10  
**Target:** Spring Boot 3.x (Spring Framework 6.x)

**Breaking Changes:**
- Namespace change: javax.* → jakarta.*
- Deprecated classes removed
- Configuration changes

**Migration Path:**
1. Spring Boot 2.7 (Spring 5.x) - intermediate
2. Spring Boot 3.0+ (Spring 6.x) - target

### 5.3 Hibernate Upgrade

**Current:** Hibernate 4.1.8  
**Target:** Hibernate 6.x

**Key Changes:**
- JPA 3.0 compliance
- Criteria API changes
- Configuration updates

### 5.4 Build Tool

**Current:** Maven 3.x  
**Options:**
1. Maven 4.x (minimal change)
2. Gradle 8.x (modern alternative, better performance)

**Recommendation:** Gradle for new project (better dependency management, faster builds)

### 5.5 Database

**Current:** H2, MySQL, Oracle support  
**Target:** Maintain compatibility, add PostgreSQL support

**Migration Scripts:**
- Use Flyway or Liquibase for version control
- Separate data migration from schema migration

---

## 6. Quality Standards

### 6.1 Code Quality

**Standards:**
- Follow Google Java Style Guide
- Use Checkstyle, PMD, SpotBugs
- SonarQube quality gates (A rating)
- Code coverage: Minimum 80% for domain logic

### 6.2 Testing Strategy

**Unit Tests:**
- Domain logic: 100% coverage
- Application services: 90% coverage
- Test frameworks: JUnit 5, Mockito, AssertJ

**Integration Tests:**
- Repository tests with Testcontainers
- API tests with REST Assured or MockMvc
- Spring Boot Test slices

**End-to-End Tests:**
- Critical user journeys
- Selenium/Playwright for UI (if applicable)

### 6.3 Documentation

**Required Documentation:**
- Architecture Decision Records (ADRs)
- API documentation (OpenAPI 3.0)
- Domain model documentation
- Deployment guides
- Runbooks

### 6.4 Performance

**Targets:**
- API response time: p95 < 200ms
- Database query time: p95 < 50ms
- Throughput: 1000 req/s minimum
- Memory: Heap usage < 70% under load

---

## 7. Risk Management

### 7.1 Technical Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Data loss during migration | High | Low | Comprehensive backup, dry runs, rollback plan |
| Performance degradation | High | Medium | Load testing, profiling, optimization sprints |
| Integration failures | Medium | Medium | Thorough integration testing, mocking |
| Security vulnerabilities | High | Low | Security scanning, penetration testing |
| Scope creep | Medium | High | Clear requirements, change control process |

### 7.2 Business Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Extended downtime | High | Low | Phased rollout, blue-green deployment |
| Feature regression | High | Medium | Comprehensive test suite, QA validation |
| User adoption issues | Medium | Medium | Training, documentation, support |
| Timeline overrun | Medium | High | Regular checkpoints, agile approach |

---

## 8. Success Criteria

### 8.1 Technical Metrics

- ✅ Zero Struts2 dependencies
- ✅ All business logic migrated
- ✅ Test coverage > 80%
- ✅ API documentation complete
- ✅ Performance benchmarks met
- ✅ Security audit passed
- ✅ All integrations functional

### 8.2 Business Metrics

- ✅ Feature parity with legacy system
- ✅ Zero data loss
- ✅ User acceptance testing passed
- ✅ Production deployment successful
- ✅ Legacy system decommissioned

---

## 9. Appendices

### 9.1 Key Legacy Components Inventory

#### Controllers (46 total)
- BaseController: Base abstraction for all controllers
- PersistableController: Generic CRUD controller
- SimpleController: Simplified controller
- MenuController, DataDictController, ConfigPropertyController
- User/Role/Privilege controllers (auth)
- Reporting controllers
- And 38+ more...

#### Services (46 total)
- BaseService<T, ID>: Generic service with CRUD
- MenuService, DataDictService, ConfigPropertyService
- User/Role management services
- And 40+ more...

#### Entities (~40+ total)
**Core:**
- BaseEntity, BaseUuidEntity, BaseNativeEntity
- PersistableEntity, AttachmentableEntity

**System:**
- Menu, DataDict, ConfigProperty
- AttachmentFile, PubPost, PubPostRead
- LoggingEvent

**Auth:**
- User, Role, Department, UserExt
- Privilege

**Reporting:**
- ReportDef, ReportParam

### 9.2 Configuration Files

**Struts2:**
- struts.xml: Main Struts2 configuration
- Convention plugin configuration

**Spring:**
- spring-context.xml: Main Spring context
- spring-security.xml: Security configuration
- context-profiles.xml: Profile-based configuration
- spring-schedule.xml: Quartz scheduling
- spring-mail.xml: Email configuration
- spring-bpm.xml: Activiti BPM

**Web:**
- web.xml: Servlet configuration

### 9.3 Custom Struts2 Components

**Interceptors:**
- ExtTokenInterceptor: Custom token handling
- ExtParametersInterceptor: Enhanced parameter binding
- ExtPrepareInterceptor: Modified prepare interceptor

**Plugins/Extensions:**
- NegotiationRestActionProxyFactory: REST negotiation
- Jackson2LibHandler: Jackson 2 support for JSON
- ExtRestActionMapper: Extended REST action mapping
- ExtDefaultResultMapBuilder: Custom result mapping
- ExtPackageBasedActionConfigBuilder: Custom action config

**These need complete replacement in Spring MVC.**

### 9.4 Dependencies to Replace/Upgrade

**Remove:**
- Struts2 (all artifacts)
- Struts2-Spring-Plugin
- Struts2-REST-Plugin
- Struts2-Convention-Plugin
- XWork

**Upgrade:**
- Spring 3.2.10 → Spring Boot 3.x
- Hibernate 4.1.8 → Hibernate 6.x
- Spring Data JPA 1.6.2 → 3.x
- Jackson 2.2.3 → 2.15+
- Activiti 5.14 → Camunda/Flowable (latest)
- Quartz 2.2.1 → 2.3.2+

**Add:**
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Security
- Spring Boot Starter Validation
- Spring Boot Starter Actuator
- Springdoc OpenAPI (for API docs)
- MapStruct (for DTO mapping)
- Testcontainers (for integration tests)

---

## 10. Glossary

**ADR:** Architectural Decision Record  
**CQRS:** Command Query Responsibility Segregation  
**DDD:** Domain-Driven Design  
**DTO:** Data Transfer Object  
**HATEOAS:** Hypermedia as the Engine of Application State  
**JPA:** Java Persistence API  
**POJO:** Plain Old Java Object  
**RBAC:** Role-Based Access Control  
**REST:** Representational State Transfer  
**SPA:** Single Page Application  
**SSO:** Single Sign-On

---

## 11. References

- Spring Boot Documentation: https://spring.io/projects/spring-boot
- Spring Framework Documentation: https://spring.io/projects/spring-framework
- Domain-Driven Design by Eric Evans
- Clean Architecture by Robert C. Martin
- Building Microservices by Sam Newman
- Spring Data JPA Documentation: https://spring.io/projects/spring-data-jpa
- Hibernate 6 Migration Guide: https://hibernate.org/orm/documentation/6.0/

---

**Document Version:** 1.0.0  
**Last Updated:** 2025-02-09  
**Status:** ACTIVE  
**Next Review:** Post Phase 1 Completion
