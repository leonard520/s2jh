# Implementation Plan: Struts 2 to Spring MVC Migration

**Version:** 1.0  
**Date:** 2026-01-24  
**Specification:** [SPEC-struts-to-springmvc.md](SPEC-struts-to-springmvc.md)

---

## Applied Guidelines

- **Guideline**: struts-to-spring
- **Skills Used**: migrate-maven-dependencies, convert-action-to-controller, convert-action-properties, convert-interceptor, migrate-struts-xml, migrate-web-xml
- **Reference**: `.github/skills/guidelines/struts-to-spring/`

---

## Phase 1: Core Infrastructure (core-service)

### 1.1 Add Spring MVC Dependency

**File:** `core-service/pom.xml`

Add spring-webmvc dependency (already in parent dependencyManagement):
```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webmvc</artifactId>
</dependency>
```

### 1.2 Create Spring MVC Base Controllers

**New File:** `core-service/src/main/java/lab/s2jh/core/web/mvc/BaseMvcController.java`

Convert `SimpleController` patterns to Spring MVC:
- Replace `ServletActionContext` with injected `HttpServletRequest`/`HttpServletResponse`
- Remove `ModelDriven` interface
- Use `Model` parameter instead of `setModel()`
- Return `String` for views, `@ResponseBody` for JSON

**New File:** `core-service/src/main/java/lab/s2jh/core/web/mvc/PersistableMvcController.java`

Convert `PersistableController` to Spring MVC:
- Replace `Preparable` with `@ModelAttribute` methods
- Replace `HttpHeaders` return type with `String` or `ResponseEntity`
- Use `@RequestMapping` annotations for URL mapping
- Use `@Valid` + `BindingResult` for validation

### 1.3 Convert Interceptors

**Files to convert:**
- `ExtTokenInterceptor.java` → `TokenHandlerInterceptor.java`
- `ExtParametersInterceptor.java` → Remove (Spring handles parameter binding)
- `ExtPrepareInterceptor.java` → Remove (use `@ModelAttribute`)

**Pattern:**
```java
// FROM: Struts
public class ExtTokenInterceptor extends MethodFilterInterceptor {
    protected String doIntercept(ActionInvocation invocation) { ... }
}

// TO: Spring
@Component
public class TokenHandlerInterceptor implements HandlerInterceptor {
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) { ... }
}
```

### 1.4 Create WebMvcConfigurer

**New File:** `core-service/src/main/java/lab/s2jh/core/web/mvc/WebMvcConfig.java`

```java
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "lab.s2jh")
public class WebMvcConfig implements WebMvcConfigurer {
    // View resolvers
    // Interceptor registration
    // Content negotiation (extensionless)
    // Message converters (Jackson)
}
```

---

## Phase 2: Common Service Controllers (common-service)

### 2.1 Convert BaseController

**File:** `common-service/src/main/java/lab/s2jh/web/action/BaseController.java`

Convert to extend `PersistableMvcController`:
- Update imports
- Change return types
- Remove Struts-specific `@Inject`

### 2.2 Convert Business Controllers

**Controllers to convert (30+):**

| Package | Controllers |
|---------|-------------|
| `auth.web.action` | UserController, RoleController, PrivilegeController, DepartmentController, ProfileController, SignupUserController, UserLogonLogController |
| `sys.web.action` | DataDictController, ConfigPropertyController, AttachmentFileController, UtilController, MenuController, LoggingEventController, UserMessageController |
| `profile.web.action` | ProfileParamDefController, ProfileParamValController, SimpleParamValController, PubPostController |
| `pub.web.action` | SignupController, DataController, GridController, SigninController |
| `rpt.web.action` | ReportDefController, ReportParamController, JasperReportController |
| `schedule.web.action` | JobBeanCfgController |
| `bpm.web.action` | ActivitiController |
| `web.action` | LayoutController |

**Conversion Pattern per Controller:**

1. Add `@Controller` annotation
2. Add `@RequestMapping("/namespace/controller-name")` 
3. Replace `HttpHeaders` return with `String` or `@ResponseBody`
4. Replace `buildDefaultHttpHeaders("view")` with `return "view"`
5. Replace `setModel(object)` with `model.addAttribute()` or `@ResponseBody`
6. Convert `@SecurityControlIgnore` to Spring Security annotations

### 2.3 URL Mapping Convention

Struts convention: `UserController` in `auth.web.action` → `/admin/auth/user/*`

Spring MVC mapping:
```java
@Controller
@RequestMapping("/admin/auth/user")
public class UserController extends BaseMvcController<User, Long> {
    
    @GetMapping("")
    public String index() { return "admin/auth/user-index"; }
    
    @GetMapping("/findByPage")
    @ResponseBody
    public Page<User> findByPage(...) { ... }
    
    @PostMapping("/doSave")
    @ResponseBody
    public OperationResult doSave(...) { ... }
}
```

---

## Phase 3: Prototype Module (prototype)

### 3.1 Convert Prototype Controllers

**Controllers:**
- `CommodityController`
- `DemoController`
- `SaleDeliveryController`, `SaleDeliveryDetailController`
- `PurchaseOrderController`
- `CommodityStockController`, `StorageLocationController`, `StockInOutController`
- `BizTradeUnitController`, `AccountSubjectController`

### 3.2 Update web.xml

**File:** `prototype/src/main/webapp/WEB-INF/web.xml`

**Remove Struts filter:**
```xml
<!-- DELETE -->
<filter>
    <filter-name>struts2</filter-name>
    <filter-class>lab.s2jh.core.web.filter.PostStrutsPrepareAndExecuteFilter</filter-class>
</filter>
<filter-mapping>
    <filter-name>struts2</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

**Add DispatcherServlet:**
```xml
<servlet>
    <servlet-name>dispatcher</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <init-param>
        <param-name>contextConfigLocation</param-name>
        <param-value></param-value>
    </init-param>
    <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>dispatcher</servlet-name>
    <url-pattern>/</url-pattern>
</servlet-mapping>
```

### 3.3 Create Spring MVC Context

**New File:** `prototype/src/main/resources/spring-mvc.xml`

Or add to existing Spring context with component scanning for `@Controller` classes.

### 3.4 Delete Struts Configuration

**Files to delete:**
- `prototype/src/main/resources/struts.xml`
- `prototype/src/main/deploy/*/resources/struts.xml`

---

## Phase 4: Cleanup

### 4.1 Remove Struts Dependencies

**File:** `pom.xml` (parent)

Remove from `<dependencyManagement>`:
```xml
<!-- DELETE ALL -->
struts2-core
struts2-rest-plugin
struts2-convention-plugin
struts2-spring-plugin
struts2-json-plugin
struts2-jasperreports-plugin
struts2-embeddedjsp-plugin
struts2-config-browser-plugin
```

### 4.2 Delete Struts-Specific Classes

**Files to delete from core-service:**
- `lab/s2jh/core/web/rest/ExtRestActionMapper.java`
- `lab/s2jh/core/web/rest/NegotiationRestActionProxyFactory.java`
- `lab/s2jh/core/web/rest/Jackson2LibHandler.java`
- `lab/s2jh/core/web/convention/ExtDefaultResultMapBuilder.java`
- `lab/s2jh/core/web/convention/ExtPackageBasedActionConfigBuilder.java`
- `lab/s2jh/core/web/filter/PostStrutsPrepareAndExecuteFilter.java`
- `lab/s2jh/core/web/interceptor/ExtTokenInterceptor.java` (after Spring version created)
- `lab/s2jh/core/web/interceptor/ExtParametersInterceptor.java`
- `lab/s2jh/core/web/interceptor/ExtPrepareInterceptor.java`
- `lab/s2jh/core/web/interceptor/SmartTokenInterceptor.java`

### 4.3 Update Code Generator

**File:** `project-tools/src/main/resources/lab/s2jh/tool/builder/freemarker/Controller.ftl`

Update template to generate Spring MVC controllers instead of Struts actions.

---

## Phase 5: Test Generation

### 5.1 Create MVC Test Base Class

**New File:** `core-test/src/main/java/lab/s2jh/core/test/SpringMvcTestCase.java`

Base class for controller integration tests using Spring MVC Test framework:
```java
@WebAppConfiguration
@ContextConfiguration(locations = { ... })
public abstract class SpringMvcTestCase {
    @Autowired
    protected WebApplicationContext wac;
    protected MockMvc mockMvc;
    
    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }
}
```

### 5.2 Create Auth Controller Tests

**New Files:**
- `common-service/src/test/java/lab/s2jh/auth/web/mvc/test/UserMvcControllerTest.java`
- `common-service/src/test/java/lab/s2jh/auth/web/mvc/test/RoleMvcControllerTest.java`

Tests for:
- GET `/admin/auth/user/findByPage` - returns paginated JSON
- POST `/admin/auth/user/doSave` - creates/updates user
- POST `/admin/auth/user/doDelete` - deletes user(s)

### 5.3 Create Sys Controller Tests

**New Files:**
- `common-service/src/test/java/lab/s2jh/sys/web/mvc/test/DataDictMvcControllerTest.java`
- `common-service/src/test/java/lab/s2jh/sys/web/mvc/test/MenuMvcControllerTest.java`

### 5.4 Create Prototype Controller Tests

**New Files:**
- `prototype/src/test/java/lab/s2jh/biz/md/web/mvc/test/CommodityMvcControllerTest.java`

---

## Phase 6: Test Execution & Validation

### 6.1 Run Controller Tests

Execute all MVC controller tests:
```bash
mvn test -Dtest=*MvcControllerTest
```

### 6.2 Validate API Responses

Verify JSON responses match original Struts format:
- Page structure: `{ content: [], totalElements: n, ... }`
- OperationResult: `{ type: "success|failure", message: "..." }`

### 6.3 Static Analysis

Verify no Struts imports remain:
```bash
grep -r "import org.apache.struts2" --include="*.java" src/
grep -r "import com.opensymphony" --include="*.java" src/
```

---

## Execution Order

| Step | Phase | Task | Files Affected | Est. Time |
|------|-------|------|----------------|-----------|
| 1 | 1.1 | Add spring-webmvc dependency | 1 pom.xml | 5 min |
| 2 | 1.2 | Create BaseMvcController | 1 new file | 30 min |
| 3 | 1.2 | Create PersistableMvcController | 1 new file | 2 hours |
| 4 | 1.3 | Convert TokenInterceptor | 1 new file | 30 min |
| 5 | 1.4 | Create WebMvcConfig | 1 new file | 1 hour |
| 6 | 2.1 | Convert BaseController | 1 file | 1 hour |
| 7 | 2.2 | Convert auth controllers | 7 files | 3 hours |
| 8 | 2.2 | Convert sys controllers | 6 files | 2 hours |
| 9 | 2.2 | Convert other controllers | 17+ files | 4 hours |
| 10 | 3.1 | Convert prototype controllers | 10 files | 2 hours |
| 11 | 3.2 | Update web.xml | 1 file | 30 min |
| 12 | 3.3 | Create spring-mvc context | 1 new file | 30 min |
| 13 | 3.4 | Delete struts.xml files | 3 files | 5 min |
| 14 | 4.1 | Remove Struts dependencies | 1 pom.xml | 15 min |
| 15 | 4.2 | Delete Struts classes | 10 files | 15 min |
| 16 | 4.3 | Update code generator | 1 template | 30 min |
| 17 | 5.1 | Create MVC test base class | 1 new file | 30 min |
| 18 | 5.2 | Create auth controller tests | 2 new files | 1 hour |
| 19 | 5.3 | Create sys controller tests | 2 new files | 1 hour |
| 20 | 5.4 | Create prototype controller tests | 1 new file | 30 min |
| 21 | 6.1 | Run controller tests | - | 30 min |
| 22 | 6.2 | Validate API responses | - | 30 min |
| 23 | 6.3 | Static analysis (no Struts imports) | - | 15 min |

**Total Estimated Time:** ~22 hours

---

## Verification Checklist

- [ ] Application starts without errors
- [ ] Login/logout works
- [ ] User CRUD operations work
- [ ] Role CRUD operations work
- [ ] All JSON APIs return correct structure
- [ ] File upload/download works
- [ ] Pagination works
- [ ] No Struts imports in codebase
- [ ] No struts.xml files exist
- [ ] All MvcControllerTest tests pass
- [ ] API response format matches specification

---

**Status:** Plan Updated - Test Phases Added
