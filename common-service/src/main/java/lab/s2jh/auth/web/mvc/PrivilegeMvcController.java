package lab.s2jh.auth.web.mvc;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lab.s2jh.auth.entity.Privilege;
import lab.s2jh.auth.entity.Role;
import lab.s2jh.auth.entity.RoleR2Privilege;
import lab.s2jh.auth.service.PrivilegeService;
import lab.s2jh.auth.service.RoleService;
import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.core.util.UidUtils;
import lab.s2jh.core.web.annotation.SecurityControlIgnore;
import lab.s2jh.core.web.view.OperationResult;
import lab.s2jh.sys.service.DataDictService;
import lab.s2jh.web.mvc.BaseMvcController;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Privilege management controller - Spring MVC version
 */
@Controller
@RequestMapping("/admin/auth/privilege")
@MetaData(value = "权限")
public class PrivilegeMvcController extends BaseMvcController<Privilege, String> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private PrivilegeService privilegeService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private DataDictService dataDictService;

    @Autowired
    private ApplicationContext applicationContext;

    private static AntPathMatcher urlMatcher = new AntPathMatcher();

    @Override
    protected BaseService<Privilege, String> getEntityService() {
        return privilegeService;
    }

    @Override
    protected void checkEntityAclPermission(Privilege entity) {
        // Do nothing check
    }

    private static List<PrivilegeUrlVO> urls;

    @MetaData(value = "计算显示可控权限URL列表")
    @RequestMapping(value = "urls", method = RequestMethod.GET)
    @ResponseBody
    public Page<PrivilegeUrlVO> urls() {
        if (urls == null) {
            urls = Lists.newArrayList();
            
            // Get RequestMappingHandlerMapping from Spring context
            RequestMappingHandlerMapping handlerMapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();
            
            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
                RequestMappingInfo mappingInfo = entry.getKey();
                HandlerMethod handlerMethod = entry.getValue();
                
                Class<?> controllerClass = handlerMethod.getBeanType();
                String className = controllerClass.getName();
                
                // Skip framework classes
                if (className.startsWith("org.springframework")) {
                    continue;
                }
                
                // Get URL patterns
                Set<String> patterns = mappingInfo.getPatternsCondition().getPatterns();
                for (String pattern : patterns) {
                    if (StringUtils.isBlank(pattern) || pattern.startsWith("/pub")) {
                        continue;
                    }
                    
                    // Check for SecurityControlIgnore annotation
                    SecurityControlIgnore ignoreAnnotation = handlerMethod.getMethodAnnotation(SecurityControlIgnore.class);
                    if (ignoreAnnotation != null) {
                        continue;
                    }
                    
                    PrivilegeUrlVO privilegeUrlVO = new PrivilegeUrlVO();
                    
                    // Get namespace (controller path prefix)
                    RequestMapping classMapping = controllerClass.getAnnotation(RequestMapping.class);
                    String namespace = "";
                    if (classMapping != null && classMapping.value().length > 0) {
                        namespace = classMapping.value()[0];
                    }
                    
                    // Get labels from MetaData annotations
                    String actionNameLabel = null;
                    MetaData actionMetaData = controllerClass.getAnnotation(MetaData.class);
                    if (actionMetaData != null) {
                        actionNameLabel = actionMetaData.value();
                    } else {
                        Object genericClz = controllerClass.getGenericSuperclass();
                        if (genericClz instanceof ParameterizedType) {
                            Class<?> entityClass = (Class<?>) ((ParameterizedType) controllerClass
                                    .getGenericSuperclass()).getActualTypeArguments()[0];
                            MetaData entityClassMetaData = entityClass.getAnnotation(MetaData.class);
                            if (entityClassMetaData != null) {
                                actionNameLabel = entityClassMetaData.value();
                            }
                        }
                    }
                    
                    String namespaceLabel = null;
                    MetaData namespaceMetaData = controllerClass.getPackage().getAnnotation(MetaData.class);
                    if (namespaceMetaData != null) {
                        namespaceLabel = namespaceMetaData.value();
                    } else {
                        namespaceLabel = namespace;
                    }
                    
                    privilegeUrlVO.setNamespace(namespace);
                    privilegeUrlVO.setNamespaceLabel(namespaceLabel);
                    privilegeUrlVO.setActionName(controllerClass.getSimpleName());
                    privilegeUrlVO.setActionNameLabel(actionNameLabel);
                    privilegeUrlVO.setMethodName(handlerMethod.getMethod().getName());
                    
                    MetaData methodMetaData = handlerMethod.getMethodAnnotation(MetaData.class);
                    if (methodMetaData != null) {
                        privilegeUrlVO.setMethodNameLabel(methodMetaData.value());
                    }
                    
                    privilegeUrlVO.setUrl(pattern);
                    urls.add(privilegeUrlVO);
                }
            }
        }

        Iterable<Privilege> privileges = privilegeService.findAllCached();
        for (PrivilegeUrlVO url : urls) {
            url.setControlled(false);
            url.setControllPrivileges(new HashSet<String>());
            for (Privilege privilege : privileges) {
                String privilegeUrl = privilege.getUrl();
                if (StringUtils.isNotBlank(privilegeUrl)) {
                    for (String splitUrl : privilegeUrl.split("\n")) {
                        if (!splitUrl.endsWith("**")) {
                            splitUrl = splitUrl + "**";
                        }
                        if (urlMatcher.match(splitUrl, url.getUrl())) {
                            url.getControllPrivileges().add(privilege.getCode() + ": " + splitUrl);
                            url.setControlled(true);
                        }
                    }
                }
            }
        }

        return buildPageResultFromList(urls);
    }

    public Map<String, String> getUrlMapFromParameters() {
        Map<String, String> dataMap = Maps.newLinkedHashMap();
        String namespace = this.getParameter("namespace");
        String actionName = this.getParameter("actionName");
        String url = this.getParameter("url");
        if (StringUtils.isNotBlank(namespace)) {
            dataMap.put(namespace, namespace);
        }
        dataMap.put(namespace + "/" + actionName, namespace + "/" + actionName);
        dataMap.put(url, url);
        return dataMap;
    }

    @MetaData("去重权限分类数据")
    @RequestMapping(value = "distinctCategories", method = RequestMethod.GET)
    @ResponseBody
    public List<String> distinctCategories() {
        return privilegeService.findDistinctCategories();
    }

    /**
     * 用于计算存储基于Controller方法列表的URL列表数据VO对象
     */
    public static class PrivilegeUrlVO {
        private String id = UidUtils.UID();
        private String namespace;
        private String namespaceLabel;
        private String actionName;
        private String actionNameLabel;
        private String methodName;
        private String methodNameLabel;
        private String url;
        private boolean controlled = false;
        private Set<String> controllPrivileges;

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getActionName() {
            return actionName;
        }

        public void setActionName(String actionName) {
            this.actionName = actionName;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public boolean isControlled() {
            return controlled;
        }

        public void setControlled(boolean controlled) {
            this.controlled = controlled;
        }

        public Set<String> getControllPrivileges() {
            return controllPrivileges;
        }

        public void setControllPrivileges(Set<String> controllPrivileges) {
            this.controllPrivileges = controllPrivileges;
        }

        public String getControllPrivilegesJoin() {
            return StringUtils.join(controllPrivileges, "<br>");
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getActionNameLabel() {
            return actionNameLabel;
        }

        public void setActionNameLabel(String actionNameLabel) {
            this.actionNameLabel = actionNameLabel;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public String getMethodNameLabel() {
            return methodNameLabel;
        }

        public void setMethodNameLabel(String methodNameLabel) {
            this.methodNameLabel = methodNameLabel;
        }

        public String getNamespaceLabel() {
            return namespaceLabel;
        }

        public void setNamespaceLabel(String namespaceLabel) {
            this.namespaceLabel = namespaceLabel;
        }
    }

    @MetaData(value = "计算显示角色关联数据")
    @SecurityControlIgnore
    @RequestMapping(value = "roles", method = RequestMethod.GET)
    public String roles(Model model) {
        List<Role> roles = roleService.findAllCached();
        List<RoleR2Privilege> r2s = privilegeService.findRelatedRoleR2PrivilegesForPrivilege(this.getId());
        for (Role role : roles) {
            role.addExtraAttribute("related", false);
            for (RoleR2Privilege r2 : r2s) {
                if (r2.getRole().equals(role)) {
                    role.addExtraAttribute("r2CreatedDate", r2.getCreatedDate());
                    role.addExtraAttribute("related", true);
                    break;
                }
            }
        }

        model.addAttribute("roles", roles);
        return resolveView("roles");
    }

    @MetaData(value = "更新角色关联")
    @SecurityControlIgnore
    @RequestMapping(value = "doUpdateRelatedRoleR2s", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doUpdateRelatedRoleR2s() {
        privilegeService.updateRelatedRoleR2s(getId(), getParameterIds("r2ids"));
        return OperationResult.buildSuccessResult("更新角色关联操作完成");
    }

    @Override
    @MetaData(value = "查询")
    @RequestMapping(value = "findByPage", method = RequestMethod.GET)
    @ResponseBody
    public Page<Privilege> findByPage() {
        return super.findByPage();
    }

    @Override
    @MetaData(value = "保存")
    @RequestMapping(value = "doSave", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doSave() {
        return super.doSave();
    }

    @Override
    @MetaData(value = "删除")
    @RequestMapping(value = "doDelete", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doDelete() {
        return super.doDelete();
    }

    @Override
    protected void newBindingEntity() {
        super.newBindingEntity();
        Privilege entity = getBindingEntity();
        entity.setCode("P" + RandomStringUtils.randomNumeric(6));
    }
}
