# REWRITE Mode Analysis: Struts2 to Spring MVC Migration

**Generated:** 2025-02-09  
**Project:** s2jh  
**Migration Mode:** REWRITE  
**Phase:** Design (Phase 2)

---

## 1. Executive Summary

### 1.1 Analysis Scope
This document provides a comprehensive REWRITE mode analysis for migrating the S2JH enterprise application framework from Struts2 to Spring Boot/Spring MVC. The analysis covers:

- **Business Logic Extraction**: Identification and extraction strategy for 40+ service classes and domain entities
- **Target Framework Selection**: Spring Boot 3.x + Spring MVC as the modern replacement
- **Migration Strategy**: Module-by-module migration with clean architecture principles

### 1.2 Current State Assessment

**Codebase Metrics:**
- Total Java Files: 354
- Struts2 Controllers: 11 (in prototype module)
- Service Classes: 40
- JPA Entities: 44 identified
- JSP Pages: 101
- Multi-module Maven project (8 modules)

**Technology Stack (Legacy):**
- Struts2 2.3.16.1 with Convention Plugin
- Spring Framework 3.2.10.RELEASE
- Hibernate 4.1.8.Final + JPA 2.0
- Spring Data JPA 1.6.2.RELEASE
- JDK 1.6
- JSP + FreeMarker views

**Critical Dependencies:**
- Struts2-Spring-Plugin (tight coupling)
- Struts2-REST-Plugin (API layer)
- Custom Struts2 interceptors (3 identified)
- Legacy infrastructure (Activiti 5.14, Quartz 2.2.1, CXF 2.7.0)

---

## 2. Business Logic Extraction Analysis

### 2.1 Domain Model Analysis

#### 2.1.1 Core Domain Entities (core-service)
**Base Entity Hierarchy:**
```
PersistableEntity<ID> (interface)
├── BaseEntity (Long ID)
├── BaseUuidEntity (UUID ID)
└── BaseNativeEntity (native strategy)
```

**Key Characteristics:**
- Generic ID types support (Long, UUID, Native)
- Audit fields: createdBy, createdDate, lastModifiedBy, lastModifiedDate
- JPA annotations mixed with domain logic
- Anemic domain model pattern (mostly getters/setters)

**Extraction Strategy:**
- **EXT-001**: Separate persistence concerns from domain model
- **EXT-002**: Create rich domain entities with business behavior
- **EXT-003**: Implement Value Objects for composite concepts
- **EXT-004**: Define Aggregate boundaries

#### 2.1.2 Business Domain Modules (common-service)

**Module 1: System Management**
- Menu (10 @Entity references) - Hierarchical menu structure
- DataDict (8 @Entity references) - Data dictionary/lookup tables
- ConfigProperty (6 @Entity references) - System configuration

**Module 2: Authentication & Authorization**
- User (9 @Entity references) - User accounts
- Role (5 @Entity references) - Role management
- Department (2 @Entity references) - Organizational structure
- Privilege (9 @Entity references) - Permission management
- UserR2Role, RoleR2Privilege - Many-to-many relationships
- UserLogonLog (17 @Entity references) - Audit logging
- SignupUser, UserOauth, PersistentLogin - Extended auth features

**Module 3: Reporting**
- ReportDef (9 @Entity references) - Report definitions
- ReportParam - Report parameters
- ReportDefR2Role - Report access control

**Module 4: File Management**
- AttachmentFile - File attachments with metadata

**Module 5: Notifications**
- PubPost (5 @Entity references) - Public posts/announcements
- PubPostRead (6 @Entity references) - Read status tracking

**Module 6: Scheduling**
- JobBeanCfg (7 @Entity references) - Job configurations
- JobRunHist (14 @Entity references) - Job execution history

**Module 7: Profile/Configuration**
- ProfileParamDef, ProfileParamVal, SimpleParamVal - Dynamic parameters

**Module 8: System Logging**
- LoggingEvent (2 @Entity references) - Application logs
- LoggingEventProperty, LoggingEventException - Log details

#### 2.1.3 Business Domain Entities (prototype module)

**Demo Module:**
- Demo (3 @Entity references) - Demo/sample entities

**Purchase Module:**
- PurchaseOrder - Purchase orders
- PurchaseOrderDetail - Order line items

**Finance Module:**
- AccountSubject - Chart of accounts
- AccountInOut - Financial transactions
- BizTradeUnit - Trading partners

**Inventory/Stock Module:**
- Commodity - Product/commodity master
- StorageLocation - Warehouse locations
- CommodityStock - Stock levels
- StockInOut - Inventory movements

**Sales Module:**
- SaleDelivery - Sales delivery orders
- SaleDeliveryDetail - Delivery line items

**Extraction Priority:**
1. **High Priority**: Auth, System Management (foundation modules)
2. **Medium Priority**: Reporting, File Management, Logging
3. **Low Priority**: Business domains (can be migrated incrementally)

### 2.2 Service Layer Analysis

**Current Pattern:**
```java
@Service
@Transactional
public class SomeService extends BaseService<Entity, Long> {
    @Autowired
    private SomeDao dao;
    
    // Business logic mixed with persistence logic
    // Direct JPA repository usage
    // Generic CRUD operations
}
```

**Issues Identified:**
- Tight coupling to infrastructure (Spring annotations, JPA)
- Anemic domain model (logic in services, not entities)
- No clear separation of concerns
- Transaction boundaries not well-defined
- No use case/command pattern

**Extraction Strategy:**

**EXT-005: Extract Pure Business Logic**
- Identify business rules in services
- Move to domain entities (rich domain model)
- Create domain services for cross-entity operations

**EXT-006: Implement Use Case Pattern**
- One use case = One business operation
- Clear input/output contracts
- Example transformations:
  - `UserService.createUser()` → `CreateUserUseCase`
  - `RoleService.assignPrivileges()` → `AssignPrivilegesToRoleUseCase`

**EXT-007: Define Port Interfaces**
- Input ports: Use case interfaces (application layer)
- Output ports: Repository interfaces (domain layer)
- Implementations in infrastructure layer

### 2.3 Web Layer Analysis

**Current Struts2 Controllers (11 identified in prototype):**

**Base Controller Patterns:**
```java
@Namespace("/admin/...")
@Action(...)
public class SomeController extends PersistableController<Entity, Long> {
    @Autowired
    private SomeService service;
    
    // Struts2 action methods
    // Mixed concerns (validation, business logic, view logic)
    // Convention-based routing
}
```

**Custom Struts2 Components:**
1. **ExtTokenInterceptor** - CSRF token validation
2. **ExtParametersInterceptor** - Parameter binding
3. **ExtPrepareInterceptor** - Prepare logic

**Issues:**
- Tight coupling to Struts2 framework
- Convention-based routing (implicit mapping)
- Action suffix pattern
- JSP view rendering
- No API-first design

**Extraction Strategy:**

**EXT-008: Extract Request/Response DTOs**
- Create DTOs for API contracts
- Separate from domain entities
- Bean Validation annotations

**EXT-009: Extract Business Logic from Controllers**
- Move to use cases
- Controllers become thin adapters
- Only handle HTTP concerns

**EXT-010: Map Struts2 Actions to REST Endpoints**
- Document current URL patterns
- Design RESTful API structure
- Define HTTP methods and status codes

### 2.4 Data Access Layer Analysis

**Current DAO Pattern:**
```java
public interface SomeDao extends BaseDao<Entity, Long> {
    // Spring Data JPA method naming
    // @Query annotations for complex queries
    // QueryDSL support
}
```

**Characteristics:**
- Spring Data JPA repositories (37 identified)
- Generic CRUD operations via BaseDao
- Custom query methods
- Direct entity manipulation

**Extraction Strategy:**

**EXT-011: Define Domain Repository Interfaces**
- Create interfaces in domain layer
- Define domain-focused operations (not CRUD-focused)
- Hide persistence details

**EXT-012: Implement Adapters**
- Spring Data JPA as implementation detail
- Map between domain entities and persistence entities (if needed)
- Optimize query performance

### 2.5 Cross-Cutting Concerns

#### 2.5.1 Security
**Current:** Spring Security 3.x + Struts2 integration  
**Target:** Spring Security 6.x + JWT/OAuth2

**Extraction Items:**
- Authentication logic
- Authorization rules (RBAC)
- Password encoding
- Session management

#### 2.5.2 Validation
**Current:** Struts2 validation + Hibernate Validator 4.x  
**Target:** Bean Validation 3.0 (Jakarta)

**Extraction Items:**
- Input validation rules
- Business validation rules
- Error message handling

#### 2.5.3 Auditing
**Current:** Custom audit support (createdBy, lastModifiedBy fields)  
**Target:** Spring Data JPA Auditing

**Extraction Items:**
- Audit field population
- Change tracking logic

#### 2.5.4 Transactions
**Current:** Spring @Transactional  
**Target:** Same, but clarify boundaries

**Extraction Items:**
- Transaction boundary definitions
- Isolation levels
- Propagation strategies

---

## 3. Target Framework Selection

### 3.1 Framework Recommendation: Spring Boot 3.x + Spring MVC

#### 3.1.1 Rationale

**Spring Boot 3.x:**
- Industry standard for Java enterprise applications
- Auto-configuration reduces boilerplate
- Production-ready features (Actuator, metrics, health checks)
- Strong ecosystem and community support
- Migration path from existing Spring 3.x code

**Spring MVC:**
- RESTful API development
- Annotation-based configuration
- Strong validation support
- Exception handling mechanisms
- HATEOAS support (optional)

**Alternative Considered:** Spring WebFlux (Reactive)
- **Rejected:** Requires complete paradigm shift (blocking → reactive)
- **Complexity:** Higher learning curve, existing team expertise
- **Use Case:** Current application is not high-concurrency/streaming focused

#### 3.1.2 Target Technology Stack

**Core Framework:**
- Spring Boot: 3.2.x (latest stable)
- Spring Framework: 6.1.x
- Java: 17 LTS (minimum) or 21 LTS (recommended)

**Persistence:**
- Spring Data JPA: 3.2.x
- Hibernate: 6.4.x
- JPA: 3.1 (Jakarta Persistence)
- Database: PostgreSQL 15+ (primary), MySQL 8+ (secondary)

**Web:**
- Spring MVC (REST APIs)
- Validation: Bean Validation 3.0
- Documentation: Springdoc OpenAPI 2.x
- Serialization: Jackson 2.16+

**Security:**
- Spring Security: 6.2.x
- JWT: Auth0 java-jwt or jjwt
- OAuth2/OIDC: Spring Security OAuth2 Resource Server

**Testing:**
- JUnit: 5.10.x (Jupiter)
- Mockito: 5.x
- AssertJ: 3.25.x
- Testcontainers: 1.19.x
- REST Assured: 5.4.x

**Build & DevOps:**
- Build Tool: Maven 4.x (or Gradle 8.5+)
- Database Migrations: Flyway 10.x (or Liquibase 4.x)
- Logging: SLF4J + Logback
- Monitoring: Micrometer + Prometheus
- Containerization: Docker + Docker Compose

**Additional Libraries:**
- Lombok: 1.18.30 (reduce boilerplate)
- MapStruct: 1.5.5 (DTO mapping)
- Apache Commons Lang3: 3.14
- Guava: 33.0 (if needed)

### 3.2 Architecture Pattern Selection

**Selected: Clean Architecture (Hexagonal Architecture)**

**Layers:**
```
┌─────────────────────────────────────────────────────┐
│         Presentation Layer (Adapters)               │
│  - REST Controllers (Spring MVC)                    │
│  - DTOs, Request/Response models                    │
│  - Exception handlers (@ControllerAdvice)           │
└────────────────┬────────────────────────────────────┘
                 │
┌────────────────┴────────────────────────────────────┐
│         Application Layer (Use Cases)               │
│  - Use Case implementations                         │
│  - Application services                             │
│  - Input/Output ports (interfaces)                  │
│  - Transaction boundaries                           │
└────────────────┬────────────────────────────────────┘
                 │
┌────────────────┴────────────────────────────────────┐
│              Domain Layer (Core)                    │
│  - Domain entities (rich models)                    │
│  - Value objects                                    │
│  - Domain services                                  │
│  - Domain events                                    │
│  - Specifications                                   │
│  - Repository interfaces (output ports)             │
└────────────────┬────────────────────────────────────┘
                 │
┌────────────────┴────────────────────────────────────┐
│         Infrastructure Layer (Adapters)             │
│  - JPA repositories (implementations)               │
│  - Persistence entities (if separate from domain)   │
│  - External service clients                         │
│  - Configuration                                    │
└─────────────────────────────────────────────────────┘
```

**Benefits:**
- Framework independence in domain layer
- Testability (mock ports)
- Flexibility (swap implementations)
- Clear separation of concerns
- Aligns with DDD principles

### 3.3 Module Structure Design

**Proposed Multi-Module Maven/Gradle Project:**

```
s2jh-spring-boot/
├── s2jh-domain/              # Domain layer (framework-agnostic)
│   ├── auth/                 # Auth domain
│   ├── system/               # System management domain
│   ├── reporting/            # Reporting domain
│   └── shared/               # Shared domain concepts
│
├── s2jh-application/         # Application layer
│   ├── auth/                 # Auth use cases
│   ├── system/               # System use cases
│   └── reporting/            # Reporting use cases
│
├── s2jh-infrastructure/      # Infrastructure layer
│   ├── persistence/          # JPA repositories
│   ├── security/             # Security implementations
│   └── external/             # External integrations
│
├── s2jh-api/                 # Presentation layer
│   ├── rest/                 # REST controllers
│   ├── dto/                  # API DTOs
│   └── config/               # Spring Boot configuration
│
├── s2jh-starter/             # Spring Boot application
│   └── main/                 # Application entry point
│
└── s2jh-test/                # Test utilities
    └── fixtures/             # Test data builders
```

**Module Dependencies:**
- `s2jh-domain`: No dependencies (pure Java)
- `s2jh-application`: depends on `s2jh-domain`
- `s2jh-infrastructure`: depends on `s2jh-domain`, `s2jh-application`
- `s2jh-api`: depends on `s2jh-application`
- `s2jh-starter`: depends on `s2jh-api`, `s2jh-infrastructure`

---

## 4. Migration Strategy Design

### 4.1 Migration Approach: Strangler Fig Pattern

**Strategy:** Build new system alongside legacy, gradually replace functionality

**Phases:**
1. **Foundation Phase**: New Spring Boot skeleton
2. **Core Migration Phase**: Domain + Application layer
3. **Infrastructure Phase**: Persistence + Integrations
4. **API Phase**: REST controllers
5. **Cutover Phase**: Switch traffic, decommission legacy

### 4.2 Module Migration Sequence

**Priority Order (Based on Dependencies):**

**Wave 1: Foundation (Weeks 1-2)**
- Setup Spring Boot 3.x project structure
- Configure multi-module build
- Setup CI/CD pipeline
- Database connectivity (Flyway migrations)
- Logging and monitoring setup

**Wave 2: Core Domain (Weeks 3-6)**
- Extract core entities (BaseEntity hierarchy → Domain entities)
- Implement core repositories
- Core value objects and specifications
- Unit tests for domain logic (80%+ coverage)

**Wave 3: System Management Module (Weeks 7-9)**
- Menu, DataDict, ConfigProperty domain models
- Use cases: CRUD operations
- REST API endpoints
- Integration tests

**Wave 4: Auth Module (Weeks 10-13)**
- User, Role, Privilege domain models
- Authentication use cases
- Authorization use cases
- Spring Security 6 integration
- JWT token support
- API endpoints for user/role management

**Wave 5: File & Notification Modules (Weeks 14-15)**
- AttachmentFile domain + use cases
- PubPost domain + use cases
- File upload/download API
- Notification API

**Wave 6: Reporting Module (Weeks 16-17)**
- ReportDef, ReportParam domain
- Report execution use cases
- Replace JasperReports (evaluate alternatives)
- Report API endpoints

**Wave 7: Logging & Scheduling (Weeks 18-19)**
- LoggingEvent migration (consider ELK stack)
- Job scheduling (evaluate Quartz vs Spring Scheduler)
- Monitoring and observability

**Wave 8: Business Domains (Weeks 20-24)**
- Purchase, Finance, Stock, Sales modules
- Domain models + use cases
- API endpoints
- Business logic validation

**Wave 9: Testing & Performance (Weeks 25-26)**
- End-to-end testing
- Performance testing and optimization
- Security testing
- Load testing

**Wave 10: Deployment & Cutover (Weeks 27-28)**
- Production deployment
- Traffic switch (blue-green or canary)
- Monitoring and incident response
- Legacy system decommission

### 4.3 Data Migration Strategy

**Database Schema:**
- **Preserve existing schema initially** (minimize risk)
- **Incremental improvements** in separate change sets
- **Version control** with Flyway/Liquibase

**Migration Scripts:**
```
db/migration/
├── V1.0__baseline_legacy_schema.sql      # Current schema
├── V1.1__add_audit_columns.sql           # Enhancements
├── V1.2__normalize_user_roles.sql        # Schema improvements
└── V2.0__cleanup_legacy_tables.sql       # Remove deprecated
```

**Data Integrity:**
- **Zero data loss** requirement
- **Backup strategy** before each migration
- **Rollback plan** for each change
- **Validation queries** to verify data

### 4.4 Parallel Run Strategy

**Dual System Operation:**
- **Legacy Struts2 app** (read-only mode)
- **New Spring Boot app** (read-write mode)
- **Shared database** (same schema)

**Traffic Routing:**
```
┌──────────┐
│ Load     │
│ Balancer │
└────┬─────┘
     │
     ├───► Legacy Struts2 (10% traffic) ──┐
     │                                     │
     └───► New Spring Boot (90% traffic) ──┤
                                          │
                                          ▼
                                    [Database]
```

**Monitoring:**
- Response time comparison
- Error rate monitoring
- Feature parity validation
- User acceptance metrics

### 4.5 Risk Mitigation

**Technical Risks:**

| Risk | Mitigation |
|------|------------|
| Data inconsistency | Shared database, transaction management, validation |
| Performance degradation | Load testing, profiling, caching strategy |
| Integration failures | Contract testing, mocking, feature flags |
| Scope creep | Strict requirement control, MVP approach |

**Business Risks:**

| Risk | Mitigation |
|------|------------|
| Extended downtime | Blue-green deployment, rollback plan |
| Feature regression | Comprehensive test suite, UAT |
| User adoption issues | Training, documentation, support team |

### 4.6 Testing Strategy

**Unit Tests (Domain Layer):**
- Pure domain logic
- 100% coverage for business rules
- No framework dependencies
- Fast execution (milliseconds)

**Integration Tests (Infrastructure):**
- Repository tests with Testcontainers
- Database migrations
- External service integrations
- Spring Boot Test slices

**API Tests (Presentation):**
- REST Assured or MockMvc
- Contract testing (OpenAPI schema validation)
- Authentication/Authorization tests
- Error handling tests

**End-to-End Tests:**
- Critical user journeys
- Business process flows
- UI automation (if web UI exists)

**Performance Tests:**
- JMeter or Gatling
- Load testing (1000 req/s target)
- Stress testing
- Endurance testing

---

## 5. Legacy Code Analysis

### 5.1 Struts2 Component Inventory

**Controllers (11 in prototype):**
- Located in: `prototype/src/main/java/.../web/action/`
- Base classes: `BaseController`, `PersistableController`
- Convention plugin based routing
- Action suffix pattern

**Interceptors (3 custom):**
- `ExtTokenInterceptor` - CSRF protection
- `ExtParametersInterceptor` - Parameter binding
- `ExtPrepareInterceptor` - Preparation logic

**Result Types:**
- JSON (Struts2-REST plugin)
- JSP views
- Redirects

**URL Patterns:**
```
/admin/{module}/{action}!{method}
/api/{module}/{resource}
```

### 5.2 Spring Configuration Files

**Key Configuration:**
- `spring-context.xml` - Main application context
- `spring-security.xml` - Security configuration
- `context-profiles.xml` - Environment profiles
- `spring-schedule.xml` - Quartz scheduling
- `spring-mail.xml` - Email configuration

**Migration Target:**
- Java-based configuration (`@Configuration`)
- Spring Boot auto-configuration
- application.yml/properties
- Profile-specific configuration

### 5.3 View Layer Analysis

**Current:**
- 101 JSP files
- FreeMarker templates
- jQuery + Bootstrap UI

**Migration Options:**

**Option 1: RESTful API Only**
- Decouple frontend completely
- Modern SPA framework (React, Vue, Angular)
- API-first approach
- **Recommended for new development**

**Option 2: Server-Side Rendering**
- Thymeleaf templates
- Progressive enhancement
- SEO-friendly
- **For content-heavy applications**

**Option 3: Hybrid**
- REST API for dynamic features
- Thymeleaf for static content
- Gradual migration to SPA

**Recommendation:** Option 1 (API-first) - aligns with modern architecture

### 5.4 Dependencies to Remove

**Struts2 Dependencies:**
```xml
<dependency>
    <groupId>org.apache.struts</groupId>
    <artifactId>struts2-core</artifactId>
    <!-- REMOVE -->
</dependency>
<dependency>
    <groupId>org.apache.struts</groupId>
    <artifactId>struts2-spring-plugin</artifactId>
    <!-- REMOVE -->
</dependency>
<dependency>
    <groupId>org.apache.struts</groupId>
    <artifactId>struts2-rest-plugin</artifactId>
    <!-- REMOVE -->
</dependency>
<dependency>
    <groupId>org.apache.struts</groupId>
    <artifactId>struts2-convention-plugin</artifactId>
    <!-- REMOVE -->
</dependency>
```

**Legacy Dependencies to Upgrade:**
- Spring 3.2.10 → Spring Boot 3.x
- Hibernate 4.1.8 → Hibernate 6.4
- Jackson 2.2.3 → Jackson 2.16
- Activiti 5.14 → Camunda 7.x or Flowable 7.x
- Quartz 2.2.1 → 2.3.2 or Spring Scheduler

---

## 6. Implementation Roadmap

### 6.1 Detailed Timeline

**Phase 1: Setup & Foundation (2 weeks)**
- Week 1: Project setup, CI/CD, baseline
- Week 2: Database setup, Flyway, logging

**Phase 2: Core Domain (4 weeks)**
- Week 3-4: Domain model extraction
- Week 5-6: Core repositories, unit tests

**Phase 3: System & Auth Modules (7 weeks)**
- Week 7-9: System management
- Week 10-13: Authentication & authorization

**Phase 4: Supporting Modules (6 weeks)**
- Week 14-15: File & notifications
- Week 16-17: Reporting
- Week 18-19: Logging & scheduling

**Phase 5: Business Domains (5 weeks)**
- Week 20-24: Purchase, Finance, Stock, Sales

**Phase 6: Testing & Deployment (4 weeks)**
- Week 25-26: Testing & performance
- Week 27-28: Deployment & cutover

**Total Estimated Duration: 28 weeks (7 months)**

### 6.2 Team Requirements

**Recommended Team Composition:**
- 1 Tech Lead (Spring Boot expert)
- 2-3 Backend Developers (Java/Spring)
- 1 Frontend Developer (if SPA)
- 1 QA Engineer
- 1 DevOps Engineer
- 0.5 DBA

**Skills Required:**
- Spring Boot 3.x
- Spring MVC & Spring Data JPA
- Clean Architecture / DDD
- RESTful API design
- JUnit 5, Mockito, Testcontainers
- Docker, CI/CD
- PostgreSQL/MySQL

### 6.3 Success Criteria

**Technical:**
- ✅ Zero Struts2 dependencies
- ✅ Test coverage > 80%
- ✅ API documentation (OpenAPI)
- ✅ Performance: p95 < 200ms
- ✅ Security audit passed
- ✅ All integrations functional

**Business:**
- ✅ Feature parity with legacy
- ✅ Zero data loss
- ✅ UAT passed
- ✅ Production deployment successful
- ✅ Legacy decommissioned

---

## 7. Recommendations

### 7.1 Key Recommendations

**REC-001: Start with Domain Layer**
- Build solid foundation
- Framework-agnostic
- High test coverage
- Reusable across modules

**REC-002: Adopt API-First Approach**
- Design REST APIs before implementation
- OpenAPI specification
- Enable frontend flexibility
- Supports microservices evolution

**REC-003: Incremental Migration**
- Module-by-module approach
- Parallel run during transition
- Feature flags for gradual rollout
- Minimize big-bang risk

**REC-004: Invest in Testing**
- Write tests before migration
- Maintain test parity
- Automated regression suite
- Performance benchmarks

**REC-005: Documentation as Code**
- ADRs for key decisions
- Living API documentation
- Code comments where needed
- Runbooks for operations

### 7.2 Anti-Patterns to Avoid

**AVOID-001: Copy-Paste Migration**
- Don't copy Struts2 patterns to Spring MVC
- Don't preserve anemic domain model
- Don't skip refactoring

**AVOID-002: Big Bang Approach**
- Don't attempt full migration at once
- Don't skip parallel run phase
- Don't skip testing

**AVOID-003: Framework Coupling**
- Don't put Spring annotations in domain layer
- Don't mix business logic with infrastructure
- Don't violate dependency inversion

### 7.3 Open Questions for Resolution

**Q1: Frontend Strategy**
- Decision needed: API-only vs Server-side rendering
- Impact: Development effort, timeline, team skills

**Q2: Reporting Solution**
- Decision needed: Keep JasperReports vs Modern alternative
- Options: BIRT, Jaspersoft, custom solution
- Impact: Reporting module complexity

**Q3: BPM Engine**
- Decision needed: Activiti → Camunda vs Flowable vs Remove
- Impact: Workflow functionality, licensing

**Q4: Build Tool**
- Decision needed: Maven 4 vs Gradle 8
- Impact: Build performance, developer experience

---

## 8. Next Steps

### 8.1 Immediate Actions

1. **Review and approve this analysis** with stakeholders
2. **Make architectural decisions** on open questions
3. **Setup development environment** (Spring Boot skeleton)
4. **Identify pilot module** for proof-of-concept
5. **Create detailed specification** (requirements document)

### 8.2 Proof of Concept

**Recommended POC Scope:**
- Migrate one small module end-to-end
- Example: Menu or DataDict module
- Validate architecture patterns
- Measure effort and complexity
- Adjust estimates and approach

**POC Duration:** 2-3 weeks

**POC Deliverables:**
- Working Spring Boot application
- REST API for chosen module
- Unit + integration tests
- Performance benchmark
- Lessons learned document

---

## 9. Conclusion

The REWRITE mode migration from Struts2 to Spring Boot is a significant but achievable undertaking. Key success factors:

1. **Clean Architecture**: Framework-independent domain layer
2. **Incremental Approach**: Module-by-module migration
3. **Testing Strategy**: Comprehensive automated testing
4. **Parallel Run**: Risk mitigation during transition
5. **Team Capability**: Skilled Spring Boot developers

**Estimated Effort:** 28 weeks with 4-6 person team

**Risk Level:** Medium (manageable with proper planning)

**Business Value:** High (modern, maintainable, scalable platform)

---

**Document Status:** DRAFT - Awaiting Review  
**Next Phase:** Detailed Specification (Requirements Definition)  
**Approval Required:** Technical Lead, Architecture Team, Product Owner
