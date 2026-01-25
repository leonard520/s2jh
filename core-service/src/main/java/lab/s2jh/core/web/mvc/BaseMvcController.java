package lab.s2jh.core.web.mvc;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.exception.WebException;
import lab.s2jh.core.security.AuthContextHolder;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Spring MVC Base Controller - replacement for Struts SimpleController
 * Provides common utilities for request/response handling and parameter access.
 */
public abstract class BaseMvcController {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /** Request URL parameter for forwarding to specific JSP view */
    protected static final String PARAM_NAME_FOR_FORWARD_TO = "_to_";

    /**
     * Get HttpServletRequest from RequestContextHolder
     */
    protected HttpServletRequest getRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    /**
     * Get HttpServletResponse from RequestContextHolder
     */
    protected HttpServletResponse getResponse() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getResponse() : null;
    }

    // ----------------------------------  
    // -----------Request Parameter Helpers--------
    // ----------------------------------

    /**
     * Get required parameter value, throws exception if empty
     * 
     * @param name parameter name
     * @return parameter value
     */
    protected String getRequiredParameter(String name) {
        String value = getRequest().getParameter(name);
        if (StringUtils.isBlank(value)) {
            throw new WebException("web.param.disallow.empty: " + name);
        }
        return value;
    }

    /**
     * Get parameter value with default if blank
     * 
     * @param name parameter name
     * @param defaultValue default value if parameter is blank
     * @return parameter value
     */
    protected String getParameter(String name, String defaultValue) {
        String value = getRequest().getParameter(name);
        if (StringUtils.isBlank(value)) {
            value = defaultValue;
        }
        return value;
    }

    /**
     * Get request parameter value
     * 
     * @param name parameter name
     * @return parameter value
     */
    protected String getParameter(String name) {
        return getRequest().getParameter(name);
    }

    /**
     * Get parameter IDs for batch operations (e.g., delete)
     * Parses comma-separated values from 'ids' parameter
     * 
     * @return array of id strings
     */
    protected String[] getParameterIds() {
        return getParameterIds("ids");
    }

    /**
     * Get parameter IDs for batch operations
     * 
     * @param paramName parameter name containing IDs
     * @return array of id strings
     */
    protected String[] getParameterIds(String paramName) {
        Set<String> idSet = Sets.newHashSet();
        String[] params = getRequest().getParameterValues(paramName);
        if (params != null) {
            for (String param : params) {
                for (String id : param.split(",")) {
                    String trimId = id.trim();
                    if (StringUtils.isNotBlank(trimId)) {
                        idSet.add(trimId);
                    }
                }
            }
        }
        return idSet.toArray(new String[0]);
    }

    // ----------------------------------  
    // -----------OGNL/EL Helper Methods---------
    // ----------------------------------
    
    /**
     * Check if string is not blank (for EL expressions)
     */
    public boolean isNotBlank(String str) {
        return StringUtils.isNotBlank(str);
    }

    /**
     * Check if string is blank (for EL expressions)
     */
    public boolean isBlank(String str) {
        return StringUtils.isBlank(str);
    }

    // -------------------------------------
    // -----------View Resolution Helpers------------
    // -------------------------------------

    /**
     * Resolve view name with optional _to_ parameter override.
     * Builds the full view path based on controller's @RequestMapping value.
     * e.g., /admin/auth/user + inputBasic = admin/auth/user-inputBasic
     * 
     * @param defaultView default view name suffix
     * @return resolved view name with path
     */
    protected String resolveView(String defaultView) {
        String to = getParameter(PARAM_NAME_FOR_FORWARD_TO);
        if (StringUtils.isNotBlank(to)) {
            return to;
        }
        
        // Build view path from controller's @RequestMapping
        String viewPath = getViewPath();
        if (StringUtils.isNotBlank(viewPath)) {
            return viewPath + "-" + defaultView;
        }
        return defaultView;
    }

    /**
     * Get view path from controller's @RequestMapping annotation.
     * Removes leading slash: /admin/auth/user -> admin/auth/user
     */
    protected String getViewPath() {
        org.springframework.web.bind.annotation.RequestMapping mapping = 
            this.getClass().getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class);
        if (mapping != null && mapping.value().length > 0) {
            String path = mapping.value()[0];
            // Remove leading slash
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            return path;
        }
        return null;
    }

    /**
     * Generic forward method based on _to_ parameter
     */
    protected String forward() {
        String to = getRequiredParameter(PARAM_NAME_FOR_FORWARD_TO);
        logger.debug("Direct forward to: {}", to);
        return to;
    }

    /**
     * Get validation rules placeholder - override in subclass
     */
    @MetaData(value = "表格数据编辑校验规则")
    public Object buildValidateRules() {
        return Maps.newHashMap();
    }

    /**
     * Get current logged-in username
     */
    public String getSigninUsername() {
        return AuthContextHolder.getAuthUserPin();
    }
}
