# Feature Specification: Struts 2 to Spring MVC Migration

**Version:** 1.0  
**Date:** 2026-01-24  
**Status:** Draft - Pending Approval  

---

## 1. Executive Summary

This specification outlines the migration of the s2jh application from Apache Struts 2.3.16.1 to Spring MVC, while maintaining the existing Spring 3.2.10 framework and minimizing view layer changes.

### Scope
- **In Scope:** Controller layer migration (Struts Actions → Spring MVC Controllers)
- **Deferred:** Spring Boot upgrade, view template modernization (Thymeleaf)

### Applied Guidelines
- **Guideline**: struts-to-spring
- **Skills Used**: convert-action-to-controller, convert-interceptor, migrate-struts-xml, migrate-web-xml
- **Reference**: `.github/skills/guidelines/struts-to-spring/SKILL.md`

---

## 2. Current Architecture Analysis

### 2.1 Controller Hierarchy

```
SimpleController (core-service)
    └── implements ModelDriven<Object>
    └── Struts REST HttpHeaders return type
    └── ServletActionContext for request/response
    
PersistableController<T, ID> extends SimpleController (core-service)
    └── implements Preparable
    └── Generic CRUD operations (create, update, delete, findByPage)
    └── Hibernate Envers audit support
    └── Spring Data JPA integration
    
BaseController<T, ID> extends PersistableController (common-service)
    └── Attachment file handling
    └── Business-specific extensions
    
46+ Business Controllers extend BaseController (common-service, prototype)
```

### 2.2 Struts-Specific Components to Migrate

| Component | Location | Count | Migration Target |
|-----------|----------|-------|------------------|
| Base Controllers | core-service | 2 | Spring MVC base classes |
| Business Controllers | common-service | 30+ | Spring @Controller classes |
| Prototype Controllers | prototype | 16+ | Spring @Controller classes |
| Interceptors | core-service | 4 | HandlerInterceptor |
| struts.xml | prototype/resources | 3 | WebMvcConfigurer |
| web.xml (Struts filter) | prototype/webapp | 1 | DispatcherServlet |
| REST ActionMapper | core-service | 2 | RequestMappingHandlerMapping |

### 2.3 Key Struts Dependencies to Remove

```xml
<!-- FROM pom.xml - TO BE REMOVED -->
struts2-core (2.3.16.1)
struts2-rest-plugin
struts2-convention-plugin
struts2-spring-plugin
struts2-json-plugin
struts2-jasperreports-plugin
struts2-embeddedjsp-plugin
struts2-config-browser-plugin
```

---

## 3. Requirements

### 3.1 Functional Requirements

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-1 | All existing URL patterns must continue to work | High |
| FR-2 | JSON API responses must maintain same structure | High |
| FR-3 | Form submission and validation must work identically | High |
| FR-4 | File upload/download functionality preserved | High |
| FR-5 | Security annotations (@SecurityControlIgnore) converted | High |
| FR-6 | Pagination and filtering behavior unchanged | Medium |
| FR-7 | Audit (Envers) integration maintained | Medium |

### 3.2 Non-Functional Requirements

| ID | Requirement | Priority |
|----|-------------|----------|
| NFR-1 | Zero business logic changes | High |
| NFR-2 | JSP views work with minimal modification | High |
| NFR-3 | Existing Spring context configuration reused | High |
| NFR-4 | Incremental migration (module by module) | Medium |

---

## 4. Technical Design

### 4.1 Spring MVC Controller Hierarchy (Target)

```java
// core-service: lab.s2jh.core.web.mvc
@Controller
public abstract class BaseMvcController {
    // Request/Response access via injection
    @Autowired protected HttpServletRequest request;
    @Autowired protected HttpServletResponse response;
    
    // Parameter helpers
    protected String getParameter(String name) { ... }
    protected String getRequiredParameter(String name) { ... }
}

@Controller  
public abstract class PersistableMvcController<T extends PersistableEntity<ID>, ID extends Serializable> 
    extends BaseMvcController {
    
    // Generic CRUD endpoints
    @GetMapping("index")
    public String index(Model model) { ... }
    
    @GetMapping("findByPage")
    @ResponseBody
    public Page<T> findByPage(@ModelAttribute GroupPropertyFilter filter, Pageable pageable) { ... }
    
    @PostMapping("doSave")
    @ResponseBody
    public OperationResult doSave(@Valid T entity, BindingResult result) { ... }
    
    @PostMapping("doDelete")
    @ResponseBody
    public OperationResult doDelete(@RequestParam String ids) { ... }
}
```

### 4.2 URL Mapping Strategy

Current Struts convention maps `UserController` to `/admin/auth/user/*`.

Spring MVC equivalent:
```java
@Controller
@RequestMapping("/admin/auth/user")
public class UserController extends PersistableMvcController<User, Long> {
    
    @GetMapping("")  // /admin/auth/user
    public String index() { return "admin/auth/user-index"; }
    
    @GetMapping("findByPage")  // /admin/auth/user/findByPage
    @ResponseBody
    public Page<User> findByPage(...) { ... }
}
```

### 4.3 Return Type Conversion

| Struts Pattern | Spring MVC Pattern |
|----------------|-------------------|
| `return buildDefaultHttpHeaders("viewName")` | `return "viewName"` |
| `return buildDefaultHttpHeaders()` (JSON) | `@ResponseBody` + return object |
| `setModel(object)` + return | `model.addAttribute()` or `@ResponseBody` |
| `HttpHeaders` with status | `ResponseEntity<T>` |

### 4.4 Interceptor Conversion

```java
// FROM: Struts Interceptor
public class ExtTokenInterceptor extends MethodFilterInterceptor {
    protected String doIntercept(ActionInvocation invocation) { ... }
}

// TO: Spring HandlerInterceptor
@Component
public class TokenInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) { ... }
}
```

### 4.5 Configuration Migration

**FROM: struts.xml**
```xml
<constant name="struts.convention.action.suffix" value="Controller" />
<constant name="struts.rest.defaultExtension" value="xhtml" />
<package name="crud-default" extends="rest-default">
    <interceptors>...</interceptors>
</package>
```

**TO: WebMvcConfig.java**
```java
@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.jsp("/", ".jsp");
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor).addPathPatterns("/**");
    }
}
```

**FROM: web.xml (Struts filter)**
```xml
<filter>
    <filter-name>struts2</filter-name>
    <filter-class>lab.s2jh.core.web.filter.PostStrutsPrepareAndExecuteFilter</filter-class>
</filter>
```

**TO: web.xml (DispatcherServlet)**
```xml
<servlet>
    <servlet-name>dispatcher</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <init-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>/WEB-INF/spring-mvc.xml</param-value>
    </init-param>
    <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>dispatcher</servlet-name>
    <url-pattern>/</url-pattern>
</servlet-mapping>
```

---

## 5. Migration Phases

### Phase 1: Core Infrastructure (core-service)
1. Create Spring MVC base controller classes
2. Convert interceptors to HandlerInterceptor
3. Add spring-webmvc dependency
4. Create WebMvcConfigurer configuration

### Phase 2: Common Controllers (common-service)
1. Convert BaseController to extend Spring MVC base
2. Migrate each business controller (auth, sys, profile, pub, etc.)
3. Update import statements and annotations

### Phase 3: Prototype Controllers & Configuration (prototype)
1. Convert remaining controllers
2. Replace Struts filter with DispatcherServlet in web.xml
3. Remove struts.xml files
4. Minimal JSP updates (only where breaking)

### Phase 4: Cleanup
1. Remove Struts dependencies from pom.xml
2. Delete Struts-specific classes (ActionMapper, ResultMapBuilder)
3. Update code generator templates

---

## 6. Acceptance Criteria

| # | Criteria | Verification Method |
|---|----------|---------------------|
| 1 | All existing URLs return same responses | Integration tests |
| 2 | Login/logout flow works | Manual testing |
| 3 | CRUD operations work for all entities | Automated tests |
| 4 | File upload/download works | Manual testing |
| 5 | Pagination and sorting work | Automated tests |
| 6 | JSON API responses unchanged | API comparison |
| 7 | No Struts imports remain in codebase | Static analysis |
| 8 | Application starts without errors | Smoke test |

---

## 7. Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| URL pattern mismatch | Medium | High | Comprehensive URL mapping tests |
| ModelDriven behavior differences | Medium | Medium | Careful @ModelAttribute handling |
| Interceptor execution order | Low | Medium | Document and test interceptor chain |
| JSP expression differences | Medium | Low | Keep JSP changes minimal, test thoroughly |

---

## 8. Design Decisions (Confirmed)

| Decision | Choice | Rationale |
|----------|--------|-----------|
| URL Extensions | **Extensionless URLs** | Modern REST style, cleaner URLs |
| Spring Version | **Stay on 3.2.10** | Minimize risk, separate upgrade later |
| Content Negotiation | Accept header based | Use Spring ContentNegotiationConfigurer |

---

**Status:** Specification Approved - 2026-01-24
