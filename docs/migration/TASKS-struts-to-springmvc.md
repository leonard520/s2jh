# Task Breakdown: Struts 2 to Spring MVC Migration

**Plan Reference:** [PLAN-struts-to-springmvc.md](PLAN-struts-to-springmvc.md)  
**Date:** 2026-01-24

---

## Phase 1: Core Infrastructure (core-service)

### Task 1.1: Add Spring WebMVC Dependency
- **Priority:** P0
- **Module:** core-service
- **Files:** `core-service/pom.xml`
- **Actions:**
  - Add `spring-webmvc` dependency
- **Acceptance:** Module compiles successfully

### Task 1.2: Create BaseMvcController
- **Priority:** P0
- **Module:** core-service
- **Files:** 
  - NEW: `core-service/src/main/java/lab/s2jh/core/web/mvc/BaseMvcController.java`
- **Actions:**
  - Create abstract base controller class
  - Port `SimpleController` utility methods
  - Use `@Autowired HttpServletRequest/Response` instead of `ServletActionContext`
  - Create helper methods: `getParameter()`, `getRequiredParameter()`, `getParameterIds()`
- **Acceptance:** Class compiles, provides request/response access

### Task 1.3: Create PersistableMvcController
- **Priority:** P0
- **Module:** core-service
- **Files:**
  - NEW: `core-service/src/main/java/lab/s2jh/core/web/mvc/PersistableMvcController.java`
- **Actions:**
  - Create generic CRUD controller extending BaseMvcController
  - Implement `@GetMapping` for: index, view, create, edit, inputTabs, viewTabs
  - Implement `@PostMapping` for: doCreate, doUpdate, doSave, doDelete
  - Implement `@GetMapping` findByPage with `@ResponseBody`
  - Implement `@ModelAttribute` method for entity binding (replace Preparable)
  - Port validation rule building, revision audit methods
- **Acceptance:** Generic CRUD operations available via annotations

### Task 1.4: Create TokenHandlerInterceptor
- **Priority:** P1
- **Module:** core-service
- **Files:**
  - NEW: `core-service/src/main/java/lab/s2jh/core/web/mvc/interceptor/TokenHandlerInterceptor.java`
- **Actions:**
  - Implement `HandlerInterceptor` interface
  - Port duplicate token detection logic from `ExtTokenInterceptor`
  - Add `@Component` annotation
- **Acceptance:** Token validation works for form submissions

### Task 1.5: Create WebMvcConfig
- **Priority:** P0
- **Module:** core-service
- **Files:**
  - NEW: `core-service/src/main/java/lab/s2jh/core/web/mvc/WebMvcConfig.java`
- **Actions:**
  - Create `@Configuration` class implementing `WebMvcConfigurer`
  - Configure JSP view resolver (prefix: `/`, suffix: `.jsp`)
  - Configure content negotiation (extensionless, JSON default for API)
  - Register Jackson message converter
  - Register interceptors
  - Configure multipart resolver for file uploads
- **Acceptance:** Spring MVC configuration loads correctly

### Task 1.6: Create Security Annotation
- **Priority:** P1
- **Module:** core-service
- **Files:**
  - NEW: `core-service/src/main/java/lab/s2jh/core/web/mvc/annotation/PublicAccess.java`
- **Actions:**
  - Create annotation to replace `@SecurityControlIgnore`
  - Integrate with Spring Security configuration
- **Acceptance:** Public endpoints accessible without authentication

---

## Phase 2: Common Service Controllers (common-service)

### Task 2.1: Update common-service pom.xml
- **Priority:** P0
- **Module:** common-service
- **Files:** `common-service/pom.xml`
- **Actions:**
  - Add `spring-webmvc` dependency if not inherited
- **Acceptance:** Module compiles

### Task 2.2: Convert BaseController
- **Priority:** P0
- **Module:** common-service
- **Files:** `common-service/src/main/java/lab/s2jh/web/action/BaseController.java`
- **Actions:**
  - Change to extend `PersistableMvcController`
  - Remove Struts imports
  - Update `doSave()` to use `@PostMapping` + `@ResponseBody`
  - Convert attachment methods to Spring MVC style
  - Remove `@Inject` for Struts configuration
- **Acceptance:** BaseController compiles with Spring MVC

### Task 2.3: Convert Auth Controllers
- **Priority:** P0
- **Module:** common-service
- **Files:**
  - `auth/web/action/UserController.java`
  - `auth/web/action/RoleController.java`
  - `auth/web/action/PrivilegeController.java`
  - `auth/web/action/DepartmentController.java`
  - `auth/web/action/ProfileController.java`
  - `auth/web/action/SignupUserController.java`
  - `auth/web/action/UserLogonLogController.java`
- **Actions per file:**
  - Add `@Controller` annotation
  - Add `@RequestMapping("/admin/auth/{name}")` 
  - Replace `HttpHeaders` return with `String` or `@ResponseBody`
  - Replace `buildDefaultHttpHeaders("view")` → `return "view"`
  - Replace `setModel(obj)` → `model.addAttribute()` or `@ResponseBody`
  - Replace `@SecurityControlIgnore` → `@PublicAccess`
  - Update all imports
- **Acceptance:** Auth module CRUD operations work

### Task 2.4: Convert Sys Controllers
- **Priority:** P0
- **Module:** common-service
- **Files:**
  - `sys/web/action/DataDictController.java`
  - `sys/web/action/ConfigPropertyController.java`
  - `sys/web/action/AttachmentFileController.java`
  - `sys/web/action/UtilController.java`
  - `sys/web/action/MenuController.java`
  - `sys/web/action/LoggingEventController.java`
- **Actions:** Same pattern as Task 2.3
- **Acceptance:** Sys module CRUD operations work

### Task 2.5: Convert Profile Controllers
- **Priority:** P1
- **Module:** common-service
- **Files:**
  - `profile/web/action/ProfileParamDefController.java`
  - `profile/web/action/ProfileParamValController.java`
  - `profile/web/action/SimpleParamValController.java`
  - `profile/web/action/PubPostController.java`
- **Actions:** Same pattern as Task 2.3
- **Acceptance:** Profile module operations work

### Task 2.6: Convert Pub Controllers
- **Priority:** P0
- **Module:** common-service
- **Files:**
  - `pub/web/action/SignupController.java`
  - `pub/web/action/DataController.java`
  - `pub/web/action/GridController.java`
  - `pub/web/action/SigninController.java`
- **Actions:** Same pattern as Task 2.3
- **Acceptance:** Public pages work (login, signup)

### Task 2.7: Convert Report Controllers
- **Priority:** P1
- **Module:** common-service
- **Files:**
  - `rpt/web/action/ReportDefController.java`
  - `rpt/web/action/ReportParamController.java`
  - `rpt/web/action/JasperReportController.java`
- **Actions:** Same pattern as Task 2.3
- **Acceptance:** Report generation works

### Task 2.8: Convert Schedule Controller
- **Priority:** P2
- **Module:** common-service
- **Files:**
  - `schedule/web/action/JobBeanCfgController.java`
- **Actions:** Same pattern as Task 2.3
- **Acceptance:** Scheduler management works

### Task 2.9: Convert BPM Controller
- **Priority:** P2
- **Module:** common-service
- **Files:**
  - `bpm/web/action/ActivitiController.java`
- **Actions:** Same pattern as Task 2.3
- **Acceptance:** Activiti integration works

### Task 2.10: Convert Layout Controller
- **Priority:** P0
- **Module:** common-service
- **Files:**
  - `web/action/LayoutController.java`
- **Actions:** Same pattern as Task 2.3
- **Acceptance:** Layout/navigation works

---

## Phase 3: Prototype Module

### Task 3.1: Convert Prototype Controllers
- **Priority:** P1
- **Module:** prototype
- **Files:**
  - `biz/md/web/action/CommodityController.java`
  - `biz/demo/web/action/DemoController.java`
  - `biz/sale/web/action/SaleDeliveryController.java`
  - `biz/sale/web/action/SaleDeliveryDetailController.java`
  - `biz/purchase/web/action/PurchaseOrderController.java`
  - `biz/stock/web/action/CommodityStockController.java`
  - `biz/stock/web/action/StorageLocationController.java`
  - `biz/stock/web/action/StockInOutController.java`
  - `biz/finance/web/action/BizTradeUnitController.java`
  - `biz/finance/web/action/AccountSubjectController.java`
  - `biz/core/web/BaseBizController.java`
- **Actions:** Same pattern as Task 2.3
- **Acceptance:** Business module CRUD operations work

### Task 3.2: Update web.xml - Add DispatcherServlet
- **Priority:** P0
- **Module:** prototype
- **Files:** `prototype/src/main/webapp/WEB-INF/web.xml`
- **Actions:**
  - Add DispatcherServlet configuration
  - Map to `/*` or `/`
  - Configure to use existing Spring context
- **Acceptance:** DispatcherServlet initializes

### Task 3.3: Update web.xml - Remove Struts Filter
- **Priority:** P0
- **Module:** prototype
- **Files:** `prototype/src/main/webapp/WEB-INF/web.xml`
- **Actions:**
  - Remove `struts2` filter definition
  - Remove `struts2` filter-mapping
- **Acceptance:** No Struts filter in web.xml

### Task 3.4: Create/Update Spring MVC Context
- **Priority:** P0
- **Module:** prototype
- **Files:** 
  - `prototype/src/main/resources/context/spring-mvc.xml` (NEW or update existing)
- **Actions:**
  - Enable `<mvc:annotation-driven/>`
  - Add component-scan for controller packages
  - Import WebMvcConfig
- **Acceptance:** Controllers are discovered and mapped

### Task 3.5: Delete struts.xml Files
- **Priority:** P0
- **Module:** prototype
- **Files:**
  - DELETE: `prototype/src/main/resources/struts.xml`
  - DELETE: `prototype/src/main/deploy/oschina/resources/struts.xml`
  - DELETE: `prototype/src/main/deploy/paas/resources/struts.xml`
  - DELETE: `prototype/src/main/deploy/production/resources/struts.xml` (if exists)
  - DELETE: `prototype/src/main/deploy/standalone/resources/struts.xml` (if exists)
- **Actions:** Delete all struts.xml files
- **Acceptance:** No struts.xml files in project

---

## Phase 4: Cleanup

### Task 4.1: Remove Struts Dependencies from Parent POM
- **Priority:** P0
- **Module:** parent
- **Files:** `pom.xml`
- **Actions:**
  - Remove all struts2-* dependencies from dependencyManagement
  - Remove struts2.version property
- **Acceptance:** No Struts dependencies declared

### Task 4.2: Remove Struts Dependencies from Module POMs
- **Priority:** P0
- **Module:** core-service, common-service, prototype
- **Files:** 
  - `core-service/pom.xml`
  - `common-service/pom.xml`
  - `prototype/pom.xml`
- **Actions:** Remove any direct Struts dependency declarations
- **Acceptance:** No Struts dependencies in any module

### Task 4.3: Delete Struts-Specific Classes
- **Priority:** P1
- **Module:** core-service
- **Files:**
  - DELETE: `core/web/rest/ExtRestActionMapper.java`
  - DELETE: `core/web/rest/NegotiationRestActionProxyFactory.java`
  - DELETE: `core/web/rest/Jackson2LibHandler.java`
  - DELETE: `core/web/convention/ExtDefaultResultMapBuilder.java`
  - DELETE: `core/web/convention/ExtPackageBasedActionConfigBuilder.java`
  - DELETE: `core/web/filter/PostStrutsPrepareAndExecuteFilter.java`
  - DELETE: `core/web/interceptor/ExtTokenInterceptor.java`
  - DELETE: `core/web/interceptor/ExtParametersInterceptor.java`
  - DELETE: `core/web/interceptor/ExtPrepareInterceptor.java`
  - DELETE: `core/web/interceptor/SmartTokenInterceptor.java`
- **Actions:** Delete files
- **Acceptance:** No Struts-specific classes remain

### Task 4.4: Delete Old Base Controllers
- **Priority:** P1
- **Module:** core-service
- **Files:**
  - DELETE: `core/web/SimpleController.java`
  - DELETE: `core/web/PersistableController.java`
- **Actions:** Delete after all controllers migrated
- **Acceptance:** No old controller base classes

### Task 4.5: Update Code Generator Template
- **Priority:** P2
- **Module:** project-tools
- **Files:** `project-tools/src/main/resources/lab/s2jh/tool/builder/freemarker/Controller.ftl`
- **Actions:**
  - Update to generate Spring MVC controller pattern
  - Use `@Controller`, `@RequestMapping`, `@ResponseBody`
- **Acceptance:** Generated controllers use Spring MVC

### Task 4.6: Verify No Struts Imports
- **Priority:** P0
- **Module:** all
- **Files:** all .java files
- **Actions:**
  - Search for `org.apache.struts2`
  - Search for `com.opensymphony.xwork2`
  - Remove any remaining imports
- **Acceptance:** Zero Struts imports in codebase

---

## Phase 5: Test Generation

### Task 5.1: Create MVC Test Base Class
- **Priority:** P0
- **Module:** core-test
- **Files:**
  - NEW: `core-test/src/main/java/lab/s2jh/core/test/SpringMvcTestCase.java`
- **Actions:**
  - Create abstract base class for MVC controller tests
  - Configure `@WebAppConfiguration` and `MockMvc`
  - Add helper methods for flush/clear EntityManager
- **Acceptance:** Test base class compiles and provides MockMvc

### Task 5.2: Add Test Dependencies
- **Priority:** P0
- **Module:** core-test
- **Files:** `core-test/pom.xml`
- **Actions:**
  - Add `spring-webmvc` dependency
  - Add `javax.servlet-api` dependency
- **Acceptance:** Test module compiles

### Task 5.3: Create Auth Controller Tests
- **Priority:** P1
- **Module:** common-service
- **Files:**
  - NEW: `common-service/src/test/java/lab/s2jh/auth/web/mvc/test/UserMvcControllerTest.java`
  - NEW: `common-service/src/test/java/lab/s2jh/auth/web/mvc/test/RoleMvcControllerTest.java`
- **Actions:**
  - Test findByPage returns Page JSON
  - Test doSave creates/updates entity
  - Test doDelete removes entity
  - Test edit returns view name
- **Acceptance:** Auth controller tests pass

### Task 5.4: Create Sys Controller Tests
- **Priority:** P1
- **Module:** common-service
- **Files:**
  - NEW: `common-service/src/test/java/lab/s2jh/sys/web/mvc/test/DataDictMvcControllerTest.java`
  - NEW: `common-service/src/test/java/lab/s2jh/sys/web/mvc/test/MenuMvcControllerTest.java`
- **Actions:**
  - Test CRUD operations
  - Test tree data for Menu
- **Acceptance:** Sys controller tests pass

### Task 5.5: Create MVC Test Spring Context
- **Priority:** P0
- **Module:** common-service
- **Files:**
  - NEW: `common-service/src/test/resources/context/spring-mvc-test.xml`
- **Actions:**
  - Enable `<mvc:annotation-driven/>`
  - Scan for @Controller classes
  - Configure view resolver for testing
- **Acceptance:** MVC test context loads

---

## Phase 6: Test Execution & Validation

### Task 6.1: Run MVC Controller Tests
- **Priority:** P0
- **Module:** all
- **Files:** None
- **Actions:**
  - Run `mvn test -Dtest=*MvcControllerTest`
  - Fix any failing tests
- **Acceptance:** All MvcControllerTest tests pass

### Task 6.2: Validate API Response Format
- **Priority:** P1
- **Module:** all
- **Files:** None
- **Actions:**
  - Verify Page JSON structure
  - Verify OperationResult JSON structure
- **Acceptance:** Response format matches specification

### Task 6.3: Static Analysis - No Struts Imports
- **Priority:** P0
- **Module:** all
- **Files:** None
- **Actions:**
  - `grep -r "import org.apache.struts2" --include="*.java"`
  - `grep -r "import com.opensymphony" --include="*.java"`
- **Acceptance:** Both commands return empty

---

## Task Dependencies

```
Phase 1:
  1.1 ─┬─► 1.2 ─► 1.3 ─┬─► 1.5
       │              │
       └─► 1.4 ───────┘
       │
       └─► 1.6

Phase 2 (depends on Phase 1):
  2.1 ─► 2.2 ─┬─► 2.3 ─► 2.4 ─► 2.5 ─► 2.6 ─► 2.7 ─► 2.8 ─► 2.9 ─► 2.10
              │
              └─► (parallel execution possible for independent controllers)

Phase 3 (depends on Phase 2):
  3.1 ─► 3.2 ─► 3.3 ─► 3.4 ─► 3.5

Phase 4 (depends on Phase 3):
  4.1 ─► 4.2 ─► 4.3 ─► 4.4 ─► 4.5 ─► 4.6

Phase 5 (depends on Phase 1):
  5.1 ─► 5.2 ─► 5.3 ─┬─► 5.4
                     │
                     └─► 5.5

Phase 6 (depends on Phase 4 and 5):
  6.1 ─► 6.2 ─► 6.3
```

---

## Summary

| Phase | Tasks | Priority P0 | Priority P1 | Priority P2 |
|-------|-------|-------------|-------------|-------------|
| 1 | 6 | 4 | 2 | 0 |
| 2 | 10 | 5 | 4 | 1 |
| 3 | 5 | 4 | 1 | 0 |
| 4 | 6 | 3 | 2 | 1 |
| 5 | 5 | 3 | 2 | 0 |
| 6 | 3 | 2 | 1 | 0 |
| **Total** | **35** | **21** | **12** | **2** |

---

**Status:** Implementation Complete - Tests Generated

**Completion:** Phases 1-6 implemented. All tasks completed.
