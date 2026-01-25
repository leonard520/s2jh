package lab.s2jh.core.web.mvc;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.EntityNotFoundException;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.audit.envers.EntityRevision;
import lab.s2jh.core.audit.envers.ExtDefaultRevisionEntity;
import lab.s2jh.core.audit.envers.ExtRevisionListener;
import lab.s2jh.core.entity.BaseEntity;
import lab.s2jh.core.entity.PersistableEntity;
import lab.s2jh.core.entity.def.OperationAuditable;
import lab.s2jh.core.exception.WebException;
import lab.s2jh.core.pagination.GroupPropertyFilter;
import lab.s2jh.core.pagination.PropertyFilter;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.core.util.DateUtils;
import lab.s2jh.core.util.ExtStringUtils;
import lab.s2jh.core.web.json.DateJsonSerializer;
import lab.s2jh.core.web.json.DateTimeJsonSerializer;
import lab.s2jh.core.web.view.OperationResult;
import net.sf.jxls.transformer.XLSTransformer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.envers.RevisionType;
import org.hibernate.proxy.pojo.javassist.JavassistLazyInitializer;
import org.hibernate.validator.constraints.Email;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lab.s2jh.core.entity.annotation.SkipParamBind;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Spring MVC Generic CRUD Controller - replacement for Struts PersistableController
 * Provides standard CRUD operations with pagination, validation, and audit support.
 *
 * @param <T> Entity type extending PersistableEntity
 * @param <ID> Entity ID type
 */
public abstract class PersistableMvcController<T extends PersistableEntity<ID>, ID extends Serializable> 
        extends BaseMvcController {

    /** Autocomplete component query parameter name */
    protected static final String PARAM_NAME_FOR_AUTOCOMPLETE = "term";

    /** Export format parameter name */
    protected static final String PARAM_NAME_FOR_EXPORT_FORMAT = "_format_";

    /** Entity class from generic type */
    protected Class<T> entityClass;

    /** Entity ID class from generic type */
    protected Class<ID> entityIdClass;

    /** Current binding entity for form operations */
    protected T bindingEntity;

    /** Collection of entities for batch operations */
    protected Collection<T> bindingEntities;

    /** Additional control attributes for views */
    private Map<String, Object> controlAttributes = new HashMap<String, Object>();

    /**
     * Subclass must provide the entity service
     */
    protected abstract BaseService<T, ID> getEntityService();

    /**
     * Subclass must implement ACL permission check
     */
    protected abstract void checkEntityAclPermission(T entity);

    /**
     * Initialize generic type classes
     */
    @SuppressWarnings("unchecked")
    public PersistableMvcController() {
        super();
        try {
            Object genericClz = getClass().getGenericSuperclass();
            if (genericClz instanceof ParameterizedType) {
                entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                        .getActualTypeArguments()[0];
                entityIdClass = (Class<ID>) ((ParameterizedType) getClass().getGenericSuperclass())
                        .getActualTypeArguments()[1];
            }
        } catch (Exception e) {
            throw new WebException(e.getMessage(), e);
        }
    }
    
    /**
     * Initialize WebDataBinder to respect @SkipParamBind annotation.
     * This prevents certain fields from being bound from request parameters.
     */
    @InitBinder("bindingEntity")
    public void initBinder(WebDataBinder binder) {
        // Collect fields with @SkipParamBind annotation
        List<String> disallowedFields = new ArrayList<String>();
        if (entityClass != null) {
            collectSkipBindFields(entityClass, disallowedFields, "");
        }
        if (!disallowedFields.isEmpty()) {
            binder.setDisallowedFields(disallowedFields.toArray(new String[0]));
        }
    }
    
    /**
     * Recursively collect fields with @SkipParamBind annotation
     */
    private void collectSkipBindFields(Class<?> clazz, List<String> disallowedFields, String prefix) {
        if (clazz == null || clazz == Object.class) {
            return;
        }
        
        // Check methods for @SkipParamBind
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(SkipParamBind.class)) {
                String methodName = method.getName();
                if (methodName.startsWith("set") && methodName.length() > 3) {
                    String fieldName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                    disallowedFields.add(prefix + fieldName);
                }
            }
        }
        
        // Check superclass
        collectSkipBindFields(clazz.getSuperclass(), disallowedFields, prefix);
    }

    /**
     * Get action name from controller class name (for view resolution)
     */
    public String getActionName() {
        String actionName = this.getClass().getSimpleName();
        String actionSuffix = "Controller";
        if (actionName.equals(actionSuffix)) {
            throw new IllegalStateException("The action name cannot be the same as the action suffix [" + actionSuffix + "]");
        }

        if (actionName.endsWith(actionSuffix)) {
            actionName = actionName.substring(0, actionName.length() - actionSuffix.length());
        }

        // Convert to hyphenated lowercase
        char[] ca = actionName.toCharArray();
        StringBuilder build = new StringBuilder("" + ca[0]);
        boolean lower = true;
        for (int i = 1; i < ca.length; i++) {
            char c = ca[i];
            if (Character.isUpperCase(c) && lower) {
                build.append("-");
                lower = false;
            } else if (!Character.isUpperCase(c)) {
                lower = true;
            }
            build.append(c);
        }

        return build.toString().toLowerCase();
    }

    public Map<String, Object> getControlAttributes() {
        return controlAttributes;
    }

    protected void addControlAttribute(String key, Object value) {
        controlAttributes.put(key, value);
    }

    // ----------------------------------  
    // -----------Entity Binding (replaces Preparable)------------
    // ----------------------------------

    /**
     * Prepare binding entity before request processing.
     * This replaces the Struts Preparable interface.
     * Returns the entity so Spring can bind request parameters to it.
     */
    @ModelAttribute("bindingEntity")
    public T prepareModel(@RequestParam(value = "id", required = false) String idParam,
                            HttpServletRequest request, Model model) {
        ID id = convertId(idParam);
        if (id != null) {
            if (request.getMethod().equalsIgnoreCase("POST")) {
                setupDetachedBindingEntity(id);
            } else {
                bindingEntity = getEntityService().findOne(id);
            }
            if (bindingEntity != null) {
                checkEntityAclPermission(bindingEntity);
            }
        } else {
            if (request.getMethod().equalsIgnoreCase("POST")) {
                newBindingEntity();
            }
        }
        
        // Manually bind request parameters to the entity for POST requests
        if (bindingEntity != null && request.getMethod().equalsIgnoreCase("POST")) {
            bindRequestToEntity(request, bindingEntity);
        }
        
        // Add binding entity to model for view access
        if (bindingEntity != null) {
            model.addAttribute("entity", bindingEntity);
            model.addAttribute("model", bindingEntity);
            // Store in request for reliable access across methods
            request.setAttribute("_bindingEntity", bindingEntity);
        }
        model.addAttribute("controlAttributes", controlAttributes);
        
        return bindingEntity;
    }
    
    /**
     * Manually bind request parameters to entity using Spring's ServletRequestDataBinder.
     * This ensures proper binding regardless of @Scope("request") proxy issues.
     */
    protected void bindRequestToEntity(HttpServletRequest request, T entity) {
        ServletRequestDataBinder binder = new ServletRequestDataBinder(entity, "bindingEntity");
        
        // Collect fields with @SkipParamBind annotation
        List<String> disallowedFields = new ArrayList<String>();
        collectSkipBindFields(entityClass, disallowedFields, "");
        if (!disallowedFields.isEmpty()) {
            binder.setDisallowedFields(disallowedFields.toArray(new String[0]));
        }
        
        // Bind the request parameters to the entity
        binder.bind(request);
    }
    
    /**
     * Get binding entity from request attribute (reliable across proxies)
     */
    @SuppressWarnings("unchecked")
    protected T getBindingEntity() {
        if (bindingEntity != null) {
            return bindingEntity;
        }
        HttpServletRequest request = getRequest();
        if (request != null) {
            T entity = (T) request.getAttribute("_bindingEntity");
            if (entity != null) {
                return entity;
            }
        }
        return bindingEntity;
    }

    @SuppressWarnings("unchecked")
    protected ID convertId(String idParam) {
        if (StringUtils.isBlank(idParam) || idParam.startsWith("-")) {
            return null;
        }
        if (String.class.isAssignableFrom(entityIdClass)) {
            return (ID) idParam;
        } else if (Long.class.isAssignableFrom(entityIdClass)) {
            return (ID) Long.valueOf(idParam);
        } else {
            throw new IllegalStateException("Undefined entity ID class: " + entityIdClass);
        }
    }

    protected ID getId() {
        return convertId(getParameter("id"));
    }

    protected ID getId(String paramName) {
        return convertId(getParameter(paramName));
    }

    protected void setupDetachedBindingEntity(ID id) {
        bindingEntity = getEntityService().findDetachedOne(id);
        // Store in request for reliable access across methods
        HttpServletRequest request = getRequest();
        if (request != null && bindingEntity != null) {
            request.setAttribute("_bindingEntity", bindingEntity);
        }
    }

    protected void newBindingEntity() {
        try {
            bindingEntity = entityClass.newInstance();
            // Store in request for reliable access across methods
            HttpServletRequest request = getRequest();
            if (request != null && bindingEntity != null) {
                request.setAttribute("_bindingEntity", bindingEntity);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public boolean isPersistentedModel() {
        T entity = getBindingEntity();
        return entity != null && entity.getId() != null
                && String.valueOf(entity.getId()).trim().length() > 0;
    }

    // -------------------------------------- 
    // -----------Index/View Operations------------
    // --------------------------------------

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String index(Model model) {
        return resolveView("index");
    }

    @RequestMapping(value = "inputTabs", method = RequestMethod.GET)
    public String inputTabs(Model model) {
        return resolveView("inputTabs");
    }

    @RequestMapping(value = "viewTabs", method = RequestMethod.GET)
    public String viewTabs(Model model) {
        return resolveView("viewTabs");
    }

    @RequestMapping(value = "view", method = RequestMethod.GET)
    public String view(Model model) {
        return resolveView("viewBasic");
    }

    // -------------------------------------- 
    // -----------Create Operations------------
    // --------------------------------------

    @RequestMapping(value = "create", method = RequestMethod.GET)
    public String create(Model model) {
        newBindingEntity();
        model.addAttribute("entity", bindingEntity);
        return resolveView("inputBasic");
    }

    @RequestMapping(value = "edit", method = RequestMethod.GET)
    public String edit(Model model) {
        if (bindingEntity == null) {
            newBindingEntity();
        }
        model.addAttribute("entity", bindingEntity);
        return resolveView("inputBasic");
    }

    protected String isDisallowCreate() {
        return null;
    }

    @MetaData(value = "创建")
    @RequestMapping(value = "doCreate", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doCreate(@ModelAttribute T entity) {
        String msg = isDisallowCreate();
        Assert.isNull(msg, msg);
        checkEntityAclPermission(entity);
        ExtRevisionListener.setOperationEvent(RevisionType.ADD.name());
        getEntityService().save(entity);
        return OperationResult.buildSuccessResult("创建操作成功", entity);
    }

    // -------------------------------------- 
    // -----------Update Operations------------
    // --------------------------------------

    @RequestMapping(value = "update", method = RequestMethod.GET)
    public String update(Model model) {
        return resolveView("inputBasic");
    }

    protected String isDisallowUpdate() {
        return null;
    }

    @MetaData(value = "更新")
    @RequestMapping(value = "doUpdate", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doUpdate() {
        String msg = isDisallowUpdate();
        Assert.isNull(msg, msg);
        getEntityService().save(bindingEntity);
        return OperationResult.buildSuccessResult("更新操作成功", bindingEntity);
    }

    @MetaData(value = "保存")
    @RequestMapping(value = "doSave", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doSave() {
        return doSaveEntity(getBindingEntity());
    }

    /**
     * Internal implementation that can be called with explicit entity
     */
    protected OperationResult doSaveEntity(T entity) {
        if (entity != null) {
            this.bindingEntity = entity;
        }
        ID id = getId();
        if (id == null) {
            String msg = isDisallowCreate();
            Assert.isNull(msg, msg);
        } else {
            String msg = isDisallowUpdate();
            Assert.isNull(msg, msg);
        }
        getEntityService().save(bindingEntity);
        return OperationResult.buildSuccessResult("数据保存成功", bindingEntity);
    }

    // -------------------------------------- 
    // -----------Delete Operations------------
    // --------------------------------------

    protected String isDisallowDelete(T entity) {
        if (entity.isNew()) {
            return "未保存数据";
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected Collection<T> getEntitiesByParameterIds() {
        Collection<T> entities = new ArrayList<T>();
        for (String id : getParameterIds()) {
            Object realId = null;
            if (String.class.isAssignableFrom(entityIdClass)) {
                realId = id;
            } else if (Long.class.isAssignableFrom(entityIdClass)) {
                realId = Long.valueOf(id);
            } else {
                throw new IllegalStateException("Undefined entity ID class: " + entityIdClass);
            }
            T entity = getEntityService().findOne((ID) realId);
            entities.add(entity);
        }
        return entities;
    }

    @MetaData(value = "删除")
    @RequestMapping(value = "doDelete", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doDelete() {
        Map<ID, String> errorMessageMap = Maps.newLinkedHashMap();
        Set<T> enableDeleteEntities = Sets.newHashSet();
        Collection<T> entities = this.getEntitiesByParameterIds();
        
        for (T e : entities) {
            checkEntityAclPermission(e);
            String msg = isDisallowDelete(e);
            if (StringUtils.isBlank(msg)) {
                enableDeleteEntities.add(e);
            } else {
                errorMessageMap.put(e.getId(), msg);
            }
        }
        
        for (T e : enableDeleteEntities) {
            try {
                getEntityService().delete(e);
            } catch (Exception ex) {
                logger.warn("entity delete failure", ex);
                errorMessageMap.put(e.getId(), ex.getMessage());
            }
        }
        
        int rejectSize = errorMessageMap.size();
        if (rejectSize == 0) {
            return OperationResult.buildSuccessResult("成功删除所选选取记录:" + entities.size() + "条");
        } else {
            if (rejectSize == entities.size()) {
                return OperationResult.buildFailureResult("所有选取记录删除操作失败", errorMessageMap);
            } else {
                return OperationResult.buildWarningResult("删除操作已处理. 成功:" + (entities.size() - rejectSize) + "条"
                        + ",失败:" + rejectSize + "条", errorMessageMap);
            }
        }
    }

    // -------------------------------------- 
    // -----------Query Operations------------
    // --------------------------------------

    @MetaData(value = "查询")
    @RequestMapping(value = "findByPage", method = RequestMethod.GET)
    @ResponseBody
    public Page<T> findByPage() {
        Pageable pageable = PropertyFilter.buildPageableFromHttpRequest(getRequest());
        GroupPropertyFilter groupFilter = GroupPropertyFilter.buildFromHttpRequest(entityClass, getRequest());
        appendFilterProperty(groupFilter);
        
        String format = getParameter(PARAM_NAME_FOR_EXPORT_FORMAT);
        if ("xls".equalsIgnoreCase(format)) {
            exportXlsForGrid(groupFilter, pageable.getSort());
            return null;
        }
        
        return getEntityService().findByPage(groupFilter, pageable);
    }

    @MetaData(value = "下拉框选项数据")
    @RequestMapping(value = "selectOptions", method = RequestMethod.GET)
    @ResponseBody
    public List<T> selectOptions() {
        Sort sort = PropertyFilter.buildSortFromHttpRequest(getRequest());
        GroupPropertyFilter groupFilter = GroupPropertyFilter.buildFromHttpRequest(entityClass, getRequest());
        appendFilterProperty(groupFilter);
        return getEntityService().findByFilters(groupFilter, sort);
    }

    /**
     * Override to add additional filter conditions based on current user
     */
    protected void appendFilterProperty(GroupPropertyFilter groupPropertyFilter) {
        // Subclass override
    }

    protected <S> Page<S> buildPageResultFromList(List<S> list) {
        return new PageImpl<S>(list);
    }

    protected void exportXlsForGrid(GroupPropertyFilter groupFilter, Sort sort) {
        throw new UnsupportedOperationException();
    }

    protected void exportExcel(String templateFileName, String exportFileName, Map<String, Object> dataMap) {
        dataMap.put("dateFormatter", new SimpleDateFormat(DateUtils.DEFAULT_DATE_FORMAT));
        dataMap.put("timeFormatter", new SimpleDateFormat(DateUtils.DEFAULT_TIME_FORMAT));

        HttpServletResponse response = getResponse();
        InputStream fis = null;
        OutputStream fos = null;
        try {
            Resource resource = new ClassPathResource("/template/xls/" + templateFileName);
            logger.debug("Open template file inputstream: {}", resource.getURL());
            fis = resource.getInputStream();

            XLSTransformer transformer = new XLSTransformer();
            Workbook workbook = transformer.transformXLS(fis, dataMap);
            String filename = exportFileName;
            filename = new String(filename.getBytes("GBK"), "ISO-8859-1");
            response.setHeader("Content-Disposition", "attachment;filename=" + filename);
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            fos = response.getOutputStream();
            workbook.write(fos);
        } catch (Exception e) {
            throw new WebException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(fos);
        }
    }

    // -------------------------------------- 
    // -----------Validation Operations------------
    // --------------------------------------

    @RequestMapping(value = "checkUnique", method = RequestMethod.GET)
    @ResponseBody
    public Boolean checkUnique() {
        String element = getParameter("element");
        Assert.notNull(element);
        GroupPropertyFilter groupPropertyFilter = GroupPropertyFilter.buildDefaultAndGroupFilter();

        String value = getRequest().getParameter(element);
        if (!ExtStringUtils.hasChinese(value)) {
            value = ExtStringUtils.encodeUTF8(value);
        }

        groupPropertyFilter.append(new PropertyFilter(entityClass, "EQ_" + element, value));

        String additionalName = getRequest().getParameter("additional");
        if (StringUtils.isNotBlank(additionalName)) {
            String additionalValue = getRequest().getParameter(additionalName);
            if (!ExtStringUtils.hasChinese(additionalValue)) {
                additionalValue = ExtStringUtils.encodeUTF8(additionalValue);
            }
            groupPropertyFilter.append(new PropertyFilter(entityClass, additionalName, additionalValue));
        }
        
        String additionalName2 = getRequest().getParameter("additional2");
        if (StringUtils.isNotBlank(additionalName2)) {
            String additionalValue2 = getRequest().getParameter(additionalName2);
            if (!ExtStringUtils.hasChinese(additionalValue2)) {
                additionalValue2 = ExtStringUtils.encodeUTF8(additionalValue2);
            }
            groupPropertyFilter.append(new PropertyFilter(entityClass, additionalName2, additionalValue2));
        }

        List<T> entities = getEntityService().findByFilters(groupPropertyFilter);
        if (entities == null || entities.size() == 0) {
            return Boolean.TRUE;
        } else {
            if (entities.size() == 1) {
                String id = getRequest().getParameter("id");
                if (StringUtils.isNotBlank(id)) {
                    Serializable entityId = entities.get(0).getId();
                    if (id.equals(entityId.toString())) {
                        return Boolean.TRUE;
                    } else {
                        return Boolean.FALSE;
                    }
                } else {
                    return Boolean.FALSE;
                }
            } else {
                return Boolean.FALSE;
            }
        }
    }

    // -------------------------------------- 
    // -----------Revision/Audit Operations------------
    // --------------------------------------

    @RequestMapping(value = "revisionIndex", method = RequestMethod.GET)
    public String revisionIndex(Model model) {
        return resolveView("revisionIndex");
    }

    public Map<Field, String> getRevisionFields() {
        Map<Field, String> revisionFields = Maps.newLinkedHashMap();
        for (Field field : entityClass.getDeclaredFields()) {
            MetaData metaData = field.getAnnotation(MetaData.class);
            if (metaData != null && metaData.comparable()) {
                revisionFields.put(field, metaData != null ? metaData.value() : field.getName().toUpperCase());
            }
        }
        return revisionFields;
    }

    @MetaData(value = "版本数据列表")
    @RequestMapping(value = "revisionList", method = RequestMethod.GET)
    @ResponseBody
    public Page<EntityRevision> revisionList() {
        String property = getParameter("property");
        Boolean hasChanged = null;
        String changed = getParameter("changed");
        if (StringUtils.isNotBlank(changed)) {
            hasChanged = BooleanUtils.toBooleanObject(changed);
        }
        List<EntityRevision> entityRevisions = getEntityService().findEntityRevisions(getId(), property, hasChanged);
        for (EntityRevision entityRevision : entityRevisions) {
            Object entity = entityRevision.getEntity();
            ExtDefaultRevisionEntity revEntity = entityRevision.getRevisionEntity();
            if (entity instanceof OperationAuditable) {
                OperationAuditable aae = (OperationAuditable) entity;
                revEntity.setOldStateDisplay(aae.convertStateToDisplay(revEntity.getOldState()));
                revEntity.setNewStateDisplay(aae.convertStateToDisplay(revEntity.getNewState()));
                revEntity.setOperationEventDisplay(revEntity.getOperationEvent());
            } else {
                revEntity.setOldStateDisplay(revEntity.getOldState());
                revEntity.setNewStateDisplay(revEntity.getNewState());
                revEntity.setOperationEventDisplay(revEntity.getOperationEvent());
            }
        }
        return buildPageResultFromList(entityRevisions);
    }

    @MetaData(value = "版本数据对比")
    @RequestMapping(value = "revisionCompare", method = RequestMethod.GET)
    public String revisionCompare(Model model) {
        ID id = getId();
        Long revLeft = Long.valueOf(getRequiredParameter("revLeft"));
        Long revRight = Long.valueOf(getRequiredParameter("revRight"));
        EntityRevision revLeftEntity = null;
        EntityRevision revRightEntity = null;
        List<EntityRevision> entityRevisions = getEntityService().findEntityRevisions(id, revLeft, revRight);
        for (EntityRevision entityRevision : entityRevisions) {
            if (entityRevision.getRevisionEntity().getRev().equals(revLeft)) {
                revLeftEntity = entityRevision;
            } else if (entityRevision.getRevisionEntity().getRev().equals(revRight)) {
                revRightEntity = entityRevision;
            }
        }

        List<Map<String, String>> revEntityProperties = Lists.newArrayList();
        for (Map.Entry<Field, String> me : getRevisionFields().entrySet()) {
            Field field = me.getKey();
            Map<String, String> revEntityProperty = Maps.newHashMap();
            revEntityProperty.put("name", me.getValue());
            if (revLeftEntity != null) {
                try {
                    Object value = FieldUtils.readDeclaredField(revLeftEntity.getEntity(), field.getName(), true);
                    String valueDisplay = convertPropertyDisplay(revLeftEntity.getEntity(), field, value);
                    revEntityProperty.put("revLeftPropertyValue", valueDisplay);
                } catch (IllegalAccessException e) {
                    throw new WebException(e.getMessage(), e);
                }
            }
            if (revRightEntity != null) {
                try {
                    Object value = FieldUtils.readDeclaredField(revRightEntity.getEntity(), field.getName(), true);
                    String valueDisplay = convertPropertyDisplay(revRightEntity.getEntity(), field, value);
                    revEntityProperty.put("revRightPropertyValue", valueDisplay);
                } catch (IllegalAccessException e) {
                    throw new WebException(e.getMessage(), e);
                }
            }
            revEntityProperties.add(revEntityProperty);
        }
        
        model.addAttribute("revLeftEntity", revLeftEntity);
        model.addAttribute("revRightEntity", revRightEntity);
        model.addAttribute("revEntityProperties", revEntityProperties);
        return resolveView("revisionCompare");
    }

    @SuppressWarnings("rawtypes")
    protected String convertPropertyDisplay(Object entity, Field field, Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof PersistableEntity) {
            PersistableEntity persistableEntity = (PersistableEntity) value;
            String label = "N/A";
            try {
                label = persistableEntity.getDisplay();
            } catch (EntityNotFoundException e) {
                try {
                    JavassistLazyInitializer jli = (JavassistLazyInitializer) FieldUtils.readDeclaredField(value,
                            "handler", true);
                    Class entityClazz = jli.getPersistentClass();
                    Serializable entityId = jli.getIdentifier();
                    Object obj = getEntityService().findEntity(entityClazz, entityId);
                    PersistableEntity auditTargetEntity = (PersistableEntity) obj;
                    label = auditTargetEntity.getDisplay();
                } catch (IllegalAccessException iae) {
                    logger.warn(e.getMessage());
                }
            }
            return label;
        }
        return String.valueOf(value);
    }

    // -------------------------------------- 
    // -----------Validation Rules Building------------
    // --------------------------------------

    private static Map<Class<?>, Map<String, Object>> entityValidationRulesMap = Maps.newHashMap();

    @Override
    @MetaData(value = "表格数据编辑校验规则")
    @RequestMapping(value = "buildValidateRules", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> buildValidateRules() {
        try {
            Map<String, Object> nameRules = entityValidationRulesMap.get(entityClass);
            if (nameRules == null) {
                nameRules = Maps.newHashMap();
                entityValidationRulesMap.put(entityClass, nameRules);

                Class<?> clazz = entityClass;
                Set<Field> fields = Sets.newHashSet(clazz.getDeclaredFields());
                clazz = clazz.getSuperclass();
                while (!clazz.equals(BaseEntity.class) && !clazz.equals(Object.class)) {
                    fields.addAll(Sets.newHashSet(clazz.getDeclaredFields()));
                    clazz = clazz.getSuperclass();
                }

                for (Field field : fields) {
                    if (Modifier.isStatic(field.getModifiers()) || !Modifier.isPrivate(field.getModifiers())
                            || Collection.class.isAssignableFrom(field.getType())) {
                        continue;
                    }
                    String name = field.getName();
                    if ("id".equals(name)) {
                        continue;
                    }
                    Map<String, Object> rules = Maps.newHashMap();

                    MetaData metaData = field.getAnnotation(MetaData.class);
                    if (metaData != null) {
                        String tooltips = metaData.tooltips();
                        if (StringUtils.isNotBlank(tooltips)) {
                            rules.put("tooltips", tooltips);
                        }
                    }

                    Method method = MethodUtils.getAccessibleMethod(entityClass, "get" + StringUtils.capitalize(name));

                    if (method != null) {
                        Class<?> retType = method.getReturnType();
                        Column column = method.getAnnotation(Column.class);

                        if (column != null) {
                            if (retType != Boolean.class && column.nullable() == false) {
                                rules.put("required", true);
                            }
                            if (column.unique() == true) {
                                rules.put("unique", true);
                            }
                            if (column.updatable() == false) {
                                rules.put("readonly", true);
                            }
                            if (column.length() > 0 && retType == String.class
                                    && method.getAnnotation(Lob.class) == null) {
                                rules.put("maxlength", column.length());
                            }
                        }

                        JoinColumn joinColumn = method.getAnnotation(JoinColumn.class);
                        if (joinColumn != null) {
                            if (joinColumn.nullable() == false) {
                                rules.put("required", true);
                            }
                        }

                        if (retType == Date.class) {
                            JsonSerialize jsonSerialize = method.getAnnotation(JsonSerialize.class);
                            if (jsonSerialize != null) {
                                if (DateJsonSerializer.class == jsonSerialize.using()) {
                                    rules.put("date", true);
                                } else if (DateTimeJsonSerializer.class == jsonSerialize.using()) {
                                    rules.put("timestamp", true);
                                }
                            } else {
                                rules.put("date", true);
                            }
                        } else if (retType == BigDecimal.class) {
                            rules.put("number", true);
                        } else if (retType == Integer.class || retType == Long.class) {
                            rules.put("integer", true);
                        }

                        Size size = method.getAnnotation(Size.class);
                        if (size != null) {
                            if (size.min() > 0) {
                                rules.put("minlength", size.min());
                            }
                            if (size.max() < Integer.MAX_VALUE) {
                                rules.put("maxlength", size.max());
                            }
                        }

                        Email email = method.getAnnotation(Email.class);
                        if (email != null) {
                            rules.put("email", true);
                        }

                        Pattern pattern = method.getAnnotation(Pattern.class);
                        if (pattern != null) {
                            rules.put("regex", pattern.regexp());
                        }

                        if (rules.size() > 0) {
                            nameRules.put(name, rules);
                            if (PersistableEntity.class.isAssignableFrom(field.getType())) {
                                nameRules.put(name + ".id", rules);
                            }
                        }
                    }
                }
            }
            return nameRules;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Maps.newHashMap();
        }
    }

    // -------------------------------------- 
    // -----------Group Aggregate Operations------------
    // --------------------------------------

    protected Page<Map<String, Object>> findByGroupAggregate(GroupPropertyFilter groupFilter, Pageable pageable,
            String... properties) {
        return getEntityService().findByGroupAggregate(groupFilter, pageable, properties);
    }

    protected Page<Map<String, Object>> findByGroupAggregate(GroupPropertyFilter groupFilter, Sort sort,
            String... properties) {
        Pageable pageable = new PageRequest(0, Integer.MAX_VALUE, sort);
        return findByGroupAggregate(groupFilter, pageable, properties);
    }

    protected Page<Map<String, Object>> findByGroupAggregate(GroupPropertyFilter groupFilter, String... properties) {
        return getEntityService().findByGroupAggregate(groupFilter, null, properties);
    }

    protected Page<Map<String, Object>> findByGroupAggregate(String... properties) {
        Pageable pageable = PropertyFilter.buildPageableFromHttpRequest(getRequest());
        GroupPropertyFilter groupFilter = GroupPropertyFilter.buildFromHttpRequest(entityClass, getRequest());
        appendFilterProperty(groupFilter);
        return findByGroupAggregate(groupFilter, pageable, properties);
    }

    // -------------------------------------- 
    // -----------Batch Processing Helper------------
    // --------------------------------------

    protected OperationResult processBatchEntities(String op, EntityProcessCallbackHandler<T> entityCallback) {
        Map<ID, String> errorMessageMap = Maps.newLinkedHashMap();

        Collection<T> entities = this.getEntitiesByParameterIds();
        for (T entity : entities) {
            try {
                entityCallback.processEntity(entity);
            } catch (Exception e) {
                logger.warn("entity batch operation failure", e);
                errorMessageMap.put(entity.getId(), e.getMessage());
            }
        }

        int rejectSize = errorMessageMap.size();
        if (rejectSize == 0) {
            return OperationResult.buildSuccessResult("成功" + op + "所选选取记录:" + entities.size() + "条");
        } else {
            if (rejectSize == entities.size()) {
                return OperationResult.buildFailureResult("所有选取记录" + op + "操作失败", errorMessageMap);
            } else {
                return OperationResult.buildWarningResult(op + "操作已处理. 成功:" + (entities.size() - rejectSize) + "条"
                        + ",失败:" + rejectSize + "条", errorMessageMap);
            }
        }
    }

    // -------------------------------------- 
    // -----------Utility Methods------------
    // --------------------------------------

    protected boolean postNotConfirmedByUser() {
        return !BooleanUtils.toBoolean(getParameter("_serverValidationConfirmed_"));
    }

    /**
     * Callback interface for batch entity processing
     */
    public interface EntityProcessCallbackHandler<T> {
        void processEntity(T entity) throws Exception;
    }
}
