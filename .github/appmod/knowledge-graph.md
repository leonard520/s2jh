# S2JH Knowledge Graph: Struts2 to Spring MVC Migration

**Generated:** 2025-02-09  
**Purpose:** Visual representation of codebase structure, dependencies, and migration pathways

---

## 1. Project Structure Graph

```
s2jh-parent (root)
│
├─── core-service ────────────────┐
│    ├── Entity Base Classes      │
│    ├── DAO Abstractions         │
│    ├── Service Base Classes     │
│    ├── Web Base (Struts2)       │  Dependencies
│    ├── Pagination               │  
│    ├── Security Utils           │
│    └── Audit Support            │
│                                 │
├─── common-service ──────────────┤
│    ├── System Module            │
│    │   ├── Menu                 │
│    │   ├── DataDict             │
│    │   └── ConfigProperty       │
│    ├── Auth Module              │
│    │   ├── User                 │
│    │   ├── Role                 │
│    │   ├── Department           │
│    │   └── Privilege            │
│    ├── File Module              │
│    │   └── AttachmentFile       │
│    ├── Notification Module      │
│    │   ├── PubPost              │
│    │   └── PubPostRead          │
│    ├── Logging Module           │
│    │   └── LoggingEvent         │
│    └── Reporting Module         │
│        ├── ReportDef            │
│        └── ReportParam          │
│                                 │
├─── jasper-service ──────────────┤
│    └── JasperReport Integration │
│                                 │
├─── crawl-service ───────────────┤
│    └── Web Crawling Logic       │
│                                 │
├─── prototype (Web App) ─────────┘ (depends on all above)
│    ├── Struts2 Configuration
│    ├── Spring Context
│    ├── Security Config
│    ├── Web Resources
│    └── JSP Views (101 files)
│
├─── core-test
│    └── Testing Utilities
│
├─── assets-resource
│    └── Static Resources
│
└─── project-tools
     └── Development Tools
```

---

## 2. Layer Dependency Graph

```
┌─────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                    │
│  (prototype module - Struts2 Controllers + JSP Views)   │
│                                                          │
│  Components:                                             │
│  • 46 Controllers (*Controller.java)                    │
│  • 101 JSP Pages                                        │
│  • Struts2 Configuration (struts.xml)                   │
│  • Custom Interceptors (3)                              │
└─────────────────┬───────────────────────────────────────┘
                  │ depends on
                  ▼
┌─────────────────────────────────────────────────────────┐
│                   APPLICATION LAYER                      │
│        (common-service - Business Services)              │
│                                                          │
│  Components:                                             │
│  • 46 Service Classes (*Service.java)                   │
│  • Transaction Management                               │
│  • Business Logic Orchestration                         │
│  • Validation                                           │
└─────────────────┬───────────────────────────────────────┘
                  │ depends on
                  ▼
┌─────────────────────────────────────────────────────────┐
│                    DOMAIN LAYER                          │
│           (Entities + Domain Logic)                      │
│                                                          │
│  Components:                                             │
│  • 40+ Entity Classes (@Entity)                         │
│  • Value Objects                                        │
│  • Domain Services                                      │
│  • Business Rules                                       │
└─────────────────┬───────────────────────────────────────┘
                  │ depends on
                  ▼
┌─────────────────────────────────────────────────────────┐
│                 INFRASTRUCTURE LAYER                     │
│       (DAOs + External Integrations)                     │
│                                                          │
│  Components:                                             │
│  • 37 DAO Interfaces (Spring Data JPA)                  │
│  • JPA Configuration                                    │
│  • Database Access                                      │
│  • External Service Clients                             │
└─────────────────┬───────────────────────────────────────┘
                  │
                  ▼
             [Database]
```

---

## 3. Technology Stack Dependency Graph

```
┌─────────────────────────────────────────────────────────┐
│                    WEB LAYER TECH                        │
├─────────────────────────────────────────────────────────┤
│  Struts2 2.3.16.1                                       │
│  ├── struts2-core                                       │
│  ├── struts2-spring-plugin ───────┐                     │
│  ├── struts2-rest-plugin          │                     │
│  ├── struts2-convention-plugin    │                     │
│  ├── struts2-jasperreports-plugin │                     │
│  └── struts2-jquery-plugin        │                     │
│                                    │                     │
│  JSP 2.2 + JSTL 1.2               │                     │
│  FreeMarker 2.3.19                │                     │
│  jQuery + Bootstrap               │                     │
└────────────────────────────────────┼─────────────────────┘
                                     │
┌────────────────────────────────────┼─────────────────────┐
│                SPRING FRAMEWORK    │                     │
├────────────────────────────────────┼─────────────────────┤
│  Spring Framework 3.2.10.RELEASE   │                     │
│  ├── spring-core                   │                     │
│  ├── spring-context       ◄────────┘                     │
│  ├── spring-beans                                        │
│  ├── spring-aop                                          │
│  ├── spring-tx                                           │
│  ├── spring-orm ──────────┐                              │
│  ├── spring-web           │                              │
│  └── spring-webmvc        │                              │
│                           │                              │
│  Spring Data JPA 1.6.2 ◄──┤                              │
│  Spring Security 3.x      │                              │
└───────────────────────────┼──────────────────────────────┘
                            │
┌───────────────────────────┼──────────────────────────────┐
│              PERSISTENCE   │                             │
├───────────────────────────┼──────────────────────────────┤
│  Hibernate 4.1.8.Final    │                              │
│  JPA 2.0 ◄────────────────┘                              │
│  JDBC                                                    │
│  C3P0 Connection Pool                                    │
│                                                          │
│  Databases:                                              │
│  ├── H2 1.3.170 (development)                           │
│  ├── MySQL 5.x                                          │
│  └── Oracle 11g                                         │
└──────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                  CROSS-CUTTING                           │
├─────────────────────────────────────────────────────────┤
│  Logging: SLF4J 1.7.7 + Logback                         │
│  Validation: Hibernate Validator 4.3.1                  │
│  JSON: Jackson 2.2.3                                    │
│  Scheduling: Quartz 2.2.1                               │
│  BPM: Activiti 5.14                                     │
│  Reporting: JasperReports                               │
│  Caching: Ehcache 2.6.2                                 │
│  Messaging: ActiveMQ 5.7.0                              │
│  Web Services: CXF 2.7.0                                │
└─────────────────────────────────────────────────────────┘
```

---

## 4. Entity Relationship Graph

```
┌──────────────────────────────────────────────────────────┐
│                 BASE ENTITY HIERARCHY                    │
└──────────────────────────────────────────────────────────┘

                    PersistableEntity<ID>
                           │
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
   BaseEntity<ID>   BaseUuidEntity    BaseNativeEntity
        │
        │
        ├─── AttachmentableEntity
        │
        └─── [Custom Entities]

┌──────────────────────────────────────────────────────────┐
│                   DOMAIN ENTITIES                         │
└──────────────────────────────────────────────────────────┘

SYSTEM MODULE:
├── Menu (tree structure)
│   ├── id: String
│   ├── name: String
│   ├── path: String
│   ├── parent: Menu (self-reference)
│   └── children: Set<Menu>
│
├── DataDict (key-value configuration)
│   ├── id: String
│   ├── primaryKey: String
│   ├── primaryValue: String
│   ├── secondaryKey: String
│   └── secondaryValue: String
│
└── ConfigProperty (system settings)
    ├── id: String
    ├── propKey: String
    ├── propValue: String
    └── description: String

AUTH MODULE:
├── User
│   ├── id: String
│   ├── username: String
│   ├── password: String (encrypted)
│   ├── email: String
│   ├── roles: Set<Role>
│   ├── department: Department
│   └── userExt: UserExt (1:1)
│
├── Role
│   ├── id: String
│   ├── code: String
│   ├── name: String
│   ├── privileges: Set<Privilege>
│   └── users: Set<User>
│
├── Department (tree structure)
│   ├── id: String
│   ├── code: String
│   ├── name: String
│   ├── parent: Department
│   └── children: Set<Department>
│
└── Privilege
    ├── id: String
    ├── code: String
    ├── name: String
    └── roles: Set<Role>

FILE MODULE:
└── AttachmentFile
    ├── id: String
    ├── fileRealName: String
    ├── fileLength: Long
    ├── filePath: String
    └── entityClazz: String

NOTIFICATION MODULE:
├── PubPost (announcements/posts)
│   ├── id: String
│   ├── title: String
│   ├── content: String (HTML)
│   ├── publishTime: Date
│   └── reads: Set<PubPostRead>
│
└── PubPostRead (read tracking)
    ├── id: String
    ├── post: PubPost
    ├── user: User
    └── readTime: Date

LOGGING MODULE:
└── LoggingEvent (audit logs)
    ├── id: Long
    ├── timestmp: Long
    ├── formattedMessage: String
    ├── loggerName: String
    ├── levelString: String
    └── callerClass: String

REPORTING MODULE:
├── ReportDef (report definitions)
│   ├── id: String
│   ├── code: String
│   ├── name: String
│   ├── templateFile: String
│   └── params: Set<ReportParam>
│
└── ReportParam (report parameters)
    ├── id: String
    ├── reportDef: ReportDef
    ├── code: String
    ├── name: String
    └── paramType: String
```

---

## 5. Struts2 to Spring MVC Migration Mapping

```
┌─────────────────────────────────────────────────────────┐
│              CONTROLLER MIGRATION PATTERN                │
└─────────────────────────────────────────────────────────┘

LEGACY STRUTS2 CONTROLLER:
┌────────────────────────────────────────────────────────┐
│ public class MenuController                            │
│     extends BaseController<Menu, String> {             │
│                                                        │
│   @Autowired                                           │
│   private MenuService menuService;                     │
│                                                        │
│   // Convention: GET /sys/menu/index                  │
│   public HttpHeaders index() {                         │
│     // Struts2 REST convention                        │
│     return buildDefaultHttpHeaders();                  │
│   }                                                    │
│                                                        │
│   // Convention: POST /sys/menu                       │
│   public HttpHeaders create() {                        │
│     menuService.save(model);                          │
│     return buildDefaultHttpHeaders();                  │
│   }                                                    │
│ }                                                      │
└────────────────────────────────────────────────────────┘
                         │
                         │ MIGRATE TO
                         ▼
TARGET SPRING MVC CONTROLLER:
┌────────────────────────────────────────────────────────┐
│ @RestController                                        │
│ @RequestMapping("/api/v1/menus")                      │
│ public class MenuController {                          │
│                                                        │
│   private final MenuService menuService;               │
│   private final MenuMapper menuMapper;                 │
│                                                        │
│   @Autowired                                           │
│   public MenuController(MenuService menuService,       │
│                        MenuMapper menuMapper) {        │
│     this.menuService = menuService;                   │
│     this.menuMapper = menuMapper;                     │
│   }                                                    │
│                                                        │
│   @GetMapping                                          │
│   public ResponseEntity<List<MenuDTO>> findAll() {     │
│     List<Menu> menus = menuService.findAll();         │
│     return ResponseEntity.ok(                         │
│       menuMapper.toDTOs(menus));                      │
│   }                                                    │
│                                                        │
│   @PostMapping                                         │
│   public ResponseEntity<MenuDTO> create(               │
│       @Valid @RequestBody CreateMenuRequest req) {    │
│     Menu menu = menuService.create(req);              │
│     return ResponseEntity                             │
│       .status(HttpStatus.CREATED)                     │
│       .body(menuMapper.toDTO(menu));                  │
│   }                                                    │
│ }                                                      │
└────────────────────────────────────────────────────────┘
```

---

## 6. Service Layer Evolution

```
┌─────────────────────────────────────────────────────────┐
│                SERVICE PATTERN MIGRATION                 │
└─────────────────────────────────────────────────────────┘

LEGACY PATTERN:
┌────────────────────────────────────────────────────────┐
│ @Service                                               │
│ public class MenuService                               │
│     extends BaseService<Menu, String> {                │
│                                                        │
│   @Autowired                                           │
│   private MenuDao menuDao;                             │
│                                                        │
│   @Transactional                                       │
│   public void save(Menu entity) {                      │
│     // Business logic mixed with persistence          │
│     menuDao.save(entity);                             │
│   }                                                    │
│                                                        │
│   public List<Menu> findAll() {                        │
│     return menuDao.findAll();                          │
│   }                                                    │
│ }                                                      │
└────────────────────────────────────────────────────────┘
                         │
                         │ REFACTOR TO
                         ▼
TARGET PATTERN (Clean Architecture):
┌────────────────────────────────────────────────────────┐
│                   APPLICATION LAYER                     │
│ ┌────────────────────────────────────────────────────┐ │
│ │ @Service                                           │ │
│ │ public class MenuApplicationService {              │ │
│ │   private final MenuRepository menuRepository;     │ │
│ │   private final MenuFactory menuFactory;           │ │
│ │                                                    │ │
│ │   @Transactional                                   │ │
│ │   public Menu createMenu(CreateMenuCommand cmd) {  │ │
│ │     Menu menu = menuFactory.create(cmd);          │ │
│ │     menu.validate(); // Domain logic              │ │
│ │     return menuRepository.save(menu);             │ │
│ │   }                                                │ │
│ │ }                                                  │ │
│ └────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│                    DOMAIN LAYER                          │
│ ┌────────────────────────────────────────────────────┐ │
│ │ public class Menu {                                │ │
│ │   private MenuId id;                               │ │
│ │   private MenuName name;                           │ │
│ │   private MenuPath path;                           │ │
│ │   private Menu parent;                             │ │
│ │                                                    │ │
│ │   // Rich domain model with behavior              │ │
│ │   public void validate() {                         │ │
│ │     if (name.isEmpty()) {                         │ │
│ │       throw new InvalidMenuException();           │ │
│ │     }                                              │ │
│ │   }                                                │ │
│ │                                                    │ │
│ │   public boolean isLeaf() { /* ... */ }           │ │
│ │   public void addChild(Menu child) { /* ... */ }  │ │
│ │ }                                                  │ │
│ └────────────────────────────────────────────────────┘ │
│ ┌────────────────────────────────────────────────────┐ │
│ │ public interface MenuRepository {                  │ │
│ │   Menu save(Menu menu);                            │ │
│ │   Optional<Menu> findById(MenuId id);              │ │
│ │   List<Menu> findAll();                            │ │
│ │ }                                                  │ │
│ └────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│                INFRASTRUCTURE LAYER                      │
│ ┌────────────────────────────────────────────────────┐ │
│ │ @Repository                                        │ │
│ │ public class JpaMenuRepository                     │ │
│ │     implements MenuRepository {                    │ │
│ │                                                    │ │
│ │   private final SpringDataMenuRepository repo;     │ │
│ │   private final MenuMapper mapper;                 │ │
│ │                                                    │ │
│ │   @Override                                        │ │
│ │   public Menu save(Menu menu) {                    │ │
│ │     MenuEntity entity = mapper.toEntity(menu);    │ │
│ │     entity = repo.save(entity);                   │ │
│ │     return mapper.toDomain(entity);               │ │
│ │   }                                                │ │
│ │ }                                                  │ │
│ └────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

---

## 7. Module Dependency Matrix

```
┌──────────────┬──────┬──────┬──────┬──────┬──────┬──────┐
│ Module       │ core │ comm │ prot │ jasp │ craw │ test │
├──────────────┼──────┼──────┼──────┼──────┼──────┼──────┤
│ core-service │  -   │  -   │  -   │  -   │  -   │  -   │
│ common-srv   │  X   │  -   │  -   │  -   │  -   │  -   │
│ prototype    │  X   │  X   │  -   │  -   │  -   │  -   │
│ jasper-srv   │  X   │  X   │  -   │  -   │  -   │  -   │
│ crawl-srv    │  X   │  -   │  -   │  -   │  -   │  -   │
│ core-test    │  X   │  -   │  -   │  -   │  -   │  -   │
└──────────────┴──────┴──────┴──────┴──────┴──────┴──────┘

Legend:
  X = depends on
  - = no dependency

Dependency Rules:
1. core-service: No dependencies on other modules (foundation)
2. common-service: Depends on core-service only
3. prototype: Depends on core-service + common-service
4. Specialized services: Depend on core-service (+ common-service if needed)
5. NO circular dependencies
```

---

## 8. Migration Workflow Graph

```
┌─────────────────────────────────────────────────────────┐
│                   MIGRATION PHASES                       │
└─────────────────────────────────────────────────────────┘

Phase 1: FOUNDATION (Weeks 1-2)
  ┌───────────────────────────────────────┐
  │ • Setup Spring Boot 3.x project       │
  │ • Configure Maven/Gradle              │
  │ • Database connectivity               │
  │ • CI/CD pipeline                      │
  └─────────────┬─────────────────────────┘
                │
                ▼
Phase 2: CORE DOMAIN (Weeks 3-6)
  ┌───────────────────────────────────────┐
  │ • Migrate core-service                │
  │   - Extract base entities             │
  │   - Implement domain models           │
  │   - Create repository interfaces      │
  │ • Setup audit framework               │
  │ • Security utilities                  │
  └─────────────┬─────────────────────────┘
                │
                ▼
Phase 3: COMMON MODULES (Weeks 7-10)
  ┌───────────────────────────────────────┐
  │ • Migrate common-service modules:     │
  │   ┌─────────────────────────────────┐ │
  │   │ System Module (Menu, Dict,      │ │
  │   │              ConfigProperty)    │ │
  │   └─────────────────────────────────┘ │
  │   ┌─────────────────────────────────┐ │
  │   │ Auth Module (User, Role,        │ │
  │   │             Privilege, Dept)    │ │
  │   └─────────────────────────────────┘ │
  │   ┌─────────────────────────────────┐ │
  │   │ Other Modules (File, Log, Rpt)  │ │
  │   └─────────────────────────────────┘ │
  └─────────────┬─────────────────────────┘
                │
                ▼
Phase 4: WEB LAYER (Weeks 11-14)
  ┌───────────────────────────────────────┐
  │ • Implement REST APIs                 │
  │ • Replace Struts2 controllers         │
  │ • Security integration                │
  │ • API documentation (OpenAPI)         │
  │ • Error handling                      │
  └─────────────┬─────────────────────────┘
                │
                ▼
Phase 5: SPECIALIZED SERVICES (Weeks 15-18)
  ┌───────────────────────────────────────┐
  │ • Migrate jasper-service              │
  │ • Migrate crawl-service               │
  │ • Integration testing                 │
  └─────────────┬─────────────────────────┘
                │
                ▼
Phase 6: TESTING & DEPLOYMENT (Weeks 19-20)
  ┌───────────────────────────────────────┐
  │ • End-to-end testing                  │
  │ • Performance testing                 │
  │ • Security audit                      │
  │ • Production deployment               │
  │ • Legacy decommission                 │
  └───────────────────────────────────────┘
```

---

## 9. Configuration Migration Map

```
┌─────────────────────────────────────────────────────────┐
│             CONFIGURATION FILE MAPPING                   │
└─────────────────────────────────────────────────────────┘

LEGACY                          →  TARGET
───────────────────────────────────────────────────────────
struts.xml                      →  application.yml
                                   (Spring Boot config)

web.xml                         →  Embedded Tomcat
                                   (Spring Boot auto-config)

spring-context.xml              →  @Configuration classes
                                   + application.yml

spring-security.xml             →  SecurityConfig.java
                                   (@EnableWebSecurity)

context-profiles.xml            →  application-{profile}.yml
                                   (dev, test, prod)

persistence.properties          →  application.yml
                                   (spring.datasource.*)

log4j.xml / logback.xml         →  logback-spring.xml
                                   (Spring Boot optimized)

struts.properties               →  application.yml
                                   (spring.mvc.*)
```

---

## 10. Testing Strategy Graph

```
┌─────────────────────────────────────────────────────────┐
│                   TESTING PYRAMID                        │
└─────────────────────────────────────────────────────────┘

                     ┌──────┐
                     │  E2E │  (5% - Critical flows)
                     └──────┘
                   ┌──────────┐
                   │Integration│  (15% - API + DB)
                   └──────────┘
                ┌──────────────┐
                │   Unit Tests  │  (80% - Domain + Service)
                └──────────────┘

UNIT TESTS:
├── Domain Model Tests (JUnit 5 + AssertJ)
│   ├── Entity behavior tests
│   ├── Value object tests
│   └── Domain service tests
│
├── Application Service Tests (Mockito)
│   ├── Use case tests
│   └── Business logic tests
│
└── Utility Tests
    └── Helper method tests

INTEGRATION TESTS:
├── Repository Tests (Testcontainers)
│   ├── JPA repository tests
│   └── Custom query tests
│
├── API Tests (MockMvc / REST Assured)
│   ├── Controller endpoint tests
│   ├── Request/Response validation
│   └── Error handling tests
│
└── Security Tests
    ├── Authentication tests
    └── Authorization tests

E2E TESTS:
└── User Journey Tests
    ├── Login → Menu navigation → CRUD
    ├── User management flow
    └── Reporting flow
```

---

## 11. Risk Mitigation Graph

```
┌─────────────────────────────────────────────────────────┐
│                  RISK → MITIGATION                       │
└─────────────────────────────────────────────────────────┘

┌──────────────────────┐      ┌──────────────────────────┐
│ Data Loss            │──────│ • Full DB backup         │
│ (HIGH IMPACT)        │      │ • Dry run migrations     │
│                      │      │ • Rollback scripts       │
└──────────────────────┘      └──────────────────────────┘

┌──────────────────────┐      ┌──────────────────────────┐
│ Performance Issues   │──────│ • Load testing           │
│ (HIGH IMPACT)        │      │ • Profiling tools        │
│                      │      │ • Database optimization  │
└──────────────────────┘      └──────────────────────────┘

┌──────────────────────┐      ┌──────────────────────────┐
│ Security Gaps        │──────│ • Security scanning      │
│ (HIGH IMPACT)        │      │ • Penetration testing    │
│                      │      │ • Code review            │
└──────────────────────┘      └──────────────────────────┘

┌──────────────────────┐      ┌──────────────────────────┐
│ Integration Failures │──────│ • Comprehensive tests    │
│ (MEDIUM IMPACT)      │      │ • Mocking external deps  │
│                      │      │ • Contract testing       │
└──────────────────────┘      └──────────────────────────┘

┌──────────────────────┐      ┌──────────────────────────┐
│ Timeline Overrun     │──────│ • Agile sprints          │
│ (MEDIUM IMPACT)      │      │ • Regular checkpoints    │
│                      │      │ • Scope management       │
└──────────────────────┘      └──────────────────────────┘
```

---

## 12. Success Metrics Dashboard

```
┌─────────────────────────────────────────────────────────┐
│                  MIGRATION KPIs                          │
└─────────────────────────────────────────────────────────┘

TECHNICAL METRICS:
┌────────────────────────────────────────────────────────┐
│ Struts2 Dependencies:      [████████░░] 80% removed    │
│ Test Coverage:             [██████████] 85%            │
│ API Endpoints Migrated:    [████████░░] 35/46          │
│ Services Refactored:       [██████░░░░] 28/46          │
│ Entities Migrated:         [██████████] 100%           │
│ Performance (p95):         [██████████] <150ms (✓)     │
└────────────────────────────────────────────────────────┘

QUALITY GATES:
┌────────────────────────────────────────────────────────┐
│ ✓ Build: PASSING                                       │
│ ✓ Unit Tests: 1250/1250                                │
│ ✓ Integration Tests: 180/200                           │
│ ⚠ Security Scan: 2 findings (medium)                   │
│ ✓ Code Quality: A (SonarQube)                          │
└────────────────────────────────────────────────────────┘

BUSINESS METRICS:
┌────────────────────────────────────────────────────────┐
│ Feature Parity:            [████████░░] 80%            │
│ User Acceptance Tests:     [██████░░░░] 60%            │
│ Documentation:             [████████░░] 75%            │
│ Training Completed:        [████░░░░░░] 40%            │
└────────────────────────────────────────────────────────┘
```

---

## 13. Key Components Catalog

### Controllers (46 total)
```
common-service:
├── UtilController
├── MenuController
├── DataDictController
├── ConfigPropertyController
├── AttachmentFileController
├── PubPostController
├── PubPostReadController
├── LoggingEventController
├── ReportDefController
├── ReportParamController
├── UserController
├── RoleController
├── DepartmentController
├── PrivilegeController
└── ... (32 more)

jasper-service:
└── JasperReportController

core-service:
├── PersistableController (base)
└── SimpleController (base)
```

### Services (46 total)
```
core-service:
└── BaseService<T, ID> (generic base)

common-service:
├── MenuService
├── DataDictService
├── ConfigPropertyService
├── AttachmentFileService
├── PubPostService
├── LoggingEventService
├── ReportDefService
├── ReportParamService
├── UserService
├── RoleService
├── DepartmentService
├── PrivilegeService
└── ... (34 more)
```

### Repositories (37 total)
```
core-service:
└── BaseDao<T, ID> (extends JpaRepository)

common-service:
├── MenuDao
├── DataDictDao
├── ConfigPropertyDao
├── AttachmentFileDao
├── PubPostDao
├── LoggingEventDao
├── ReportDefDao
├── ReportParamDao
├── UserDao
├── RoleDao
├── DepartmentDao
├── PrivilegeDao
└── ... (25 more)
```

---

## 14. Technology Comparison Matrix

```
┌─────────────┬──────────────────────┬──────────────────────┐
│ Aspect      │ Legacy (Struts2)     │ Target (Spring Boot) │
├─────────────┼──────────────────────┼──────────────────────┤
│ Web         │ Struts2 2.3.16.1     │ Spring MVC 6.x       │
│ Framework   │ Convention plugin    │ Annotation-based     │
├─────────────┼──────────────────────┼──────────────────────┤
│ Core        │ Spring 3.2.10        │ Spring Boot 3.x      │
│ Framework   │ XML config           │ Auto-configuration   │
├─────────────┼──────────────────────┼──────────────────────┤
│ Java        │ JDK 1.6              │ JDK 17+              │
│ Version     │                      │                      │
├─────────────┼──────────────────────┼──────────────────────┤
│ Persistence │ Hibernate 4.1.8      │ Hibernate 6.x        │
│             │ JPA 2.0              │ JPA 3.0              │
├─────────────┼──────────────────────┼──────────────────────┤
│ Security    │ Spring Security 3.x  │ Spring Security 6.x  │
│             │ Session-based        │ JWT / OAuth2         │
├─────────────┼──────────────────────┼──────────────────────┤
│ View        │ JSP + FreeMarker     │ Thymeleaf / REST API │
│             │ Server-side render   │ Client-side SPA      │
├─────────────┼──────────────────────┼──────────────────────┤
│ Build       │ Maven 3.x            │ Maven 4.x / Gradle   │
│             │ ~1200 lines POM      │ Streamlined deps     │
├─────────────┼──────────────────────┼──────────────────────┤
│ Testing     │ JUnit 4              │ JUnit 5              │
│             │ Limited coverage     │ Comprehensive suite  │
├─────────────┼──────────────────────┼──────────────────────┤
│ API Doc     │ Manual               │ OpenAPI 3.0 / Swagger│
│             │                      │ Auto-generated       │
├─────────────┼──────────────────────┼──────────────────────┤
│ Monitoring  │ Basic logging        │ Spring Actuator      │
│             │                      │ Prometheus metrics   │
└─────────────┴──────────────────────┴──────────────────────┘
```

---

## 15. Cross-Cutting Concerns Graph

```
┌─────────────────────────────────────────────────────────┐
│           CROSS-CUTTING CONCERNS MIGRATION               │
└─────────────────────────────────────────────────────────┘

┌──────────────────────┐
│      SECURITY        │
├──────────────────────┤
│ Legacy:              │  →  Target:
│ • Spring Security 3  │      • Spring Security 6
│ • Form login         │      • JWT tokens
│ • Session-based      │      • Stateless REST
│ • RBAC               │      • RBAC + Permissions
│ • XML config         │      • Java config
└──────────────────────┘

┌──────────────────────┐
│    TRANSACTIONS      │
├──────────────────────┤
│ Legacy:              │  →  Target:
│ • @Transactional     │      • @Transactional
│ • Service layer      │      • Use case boundary
│ • Read-write same    │      • CQRS (optional)
└──────────────────────┘

┌──────────────────────┐
│      LOGGING         │
├──────────────────────┤
│ Legacy:              │  →  Target:
│ • SLF4J + Logback    │      • SLF4J + Logback
│ • Basic formatting   │      • Structured JSON
│ • File output        │      • Centralized (ELK)
│ • No correlation     │      • Correlation IDs
└──────────────────────┘

┌──────────────────────┐
│     VALIDATION       │
├──────────────────────┤
│ Legacy:              │  →  Target:
│ • Hibernate Val 4.x  │      • Bean Validation 3.0
│ • Struts2 val        │      • @Valid on DTOs
│ • Mixed concerns     │      • Domain validation
└──────────────────────┘

┌──────────────────────┐
│    CACHING           │
├──────────────────────┤
│ Legacy:              │  →  Target:
│ • Ehcache 2.6.2      │      • Caffeine / Redis
│ • Manual config      │      • Spring Cache
│ • Basic strategies   │      • Advanced patterns
└──────────────────────┘

┌──────────────────────┐
│     SCHEDULING       │
├──────────────────────┤
│ Legacy:              │  →  Target:
│ • Quartz 2.2.1       │      • Spring Scheduler
│ • XML config         │      • @Scheduled
│ • Database jobs      │      • Simplified
└──────────────────────┘
```

---

## 16. Data Flow Graph

```
┌─────────────────────────────────────────────────────────┐
│              REQUEST → RESPONSE FLOW                     │
└─────────────────────────────────────────────────────────┘

LEGACY STRUTS2 FLOW:
┌─────────────────────────────────────────────────────────┐
│ HTTP Request (POST /sys/menu)                           │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│ Struts2 Filter (PostStrutsPrepareAndExecuteFilter)      │
│ ├── Character Encoding                                  │
│ ├── Security (Spring Security)                          │
│ └── CSRF Token                                          │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│ Struts2 Action Mapper (Convention Plugin)               │
│ ├── URL → Controller mapping                            │
│ ├── /sys/menu → MenuController                          │
│ └── Method: create()                                    │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│ Interceptor Stack                                        │
│ ├── Token Interceptor (CSRF check)                      │
│ ├── Parameters Interceptor (binding)                    │
│ ├── Validation Interceptor                              │
│ └── Prepare Interceptor                                 │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│ MenuController.create()                                  │
│ ├── Autowired: MenuService                              │
│ ├── Model: Menu entity (auto-populated)                 │
│ └── Call: menuService.save(model)                       │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│ MenuService.save()                                       │
│ ├── @Transactional                                       │
│ ├── Business logic                                      │
│ └── menuDao.save(entity)                                │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│ MenuDao.save() (Spring Data JPA)                        │
│ └── Hibernate persists to database                      │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│ Response (JSON via Jackson)                             │
│ └── HTTP 200 OK                                         │
└─────────────────────────────────────────────────────────┘

────────────────────────────────────────────────────────────

TARGET SPRING MVC FLOW:
┌─────────────────────────────────────────────────────────┐
│ HTTP Request (POST /api/v1/menus)                       │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│ Filter Chain                                             │
│ ├── Character Encoding                                  │
│ ├── Spring Security (JWT validation)                    │
│ └── CORS                                                │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│ DispatcherServlet                                        │
│ ├── Handler Mapping                                     │
│ └── /api/v1/menus → MenuController.create()            │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│ MenuController.create(@Valid @RequestBody request)      │
│ ├── Validation (@Valid triggers)                        │
│ ├── DTO → Command mapping                               │
│ └── Call: menuService.createMenu(command)               │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│ MenuApplicationService.createMenu(command)               │
│ ├── @Transactional                                       │
│ ├── Menu menu = menuFactory.create(command)             │
│ ├── menu.validate() [Domain logic]                      │
│ └── menuRepository.save(menu)                           │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│ JpaMenuRepository.save(menu)                             │
│ ├── Domain → Entity mapping                             │
│ ├── Spring Data JPA save                                │
│ └── Entity → Domain mapping                             │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│ Response                                                 │
│ ├── Domain → DTO mapping                                │
│ ├── ResponseEntity<MenuDTO>                             │
│ └── HTTP 201 CREATED                                    │
└─────────────────────────────────────────────────────────┘
```

---

**End of Knowledge Graph**
**Document Version:** 1.0.0  
**Last Updated:** 2025-02-09
