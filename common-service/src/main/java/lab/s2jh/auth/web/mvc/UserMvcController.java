package lab.s2jh.auth.web.mvc;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lab.s2jh.auth.entity.Department;
import lab.s2jh.auth.entity.Privilege;
import lab.s2jh.auth.entity.Role;
import lab.s2jh.auth.entity.User;
import lab.s2jh.auth.entity.UserR2Role;
import lab.s2jh.auth.service.DepartmentService;
import lab.s2jh.auth.service.PrivilegeService;
import lab.s2jh.auth.service.RoleService;
import lab.s2jh.auth.service.UserService;
import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.audit.envers.EntityRevision;
import lab.s2jh.core.pagination.GroupPropertyFilter;
import lab.s2jh.core.pagination.PropertyFilter;
import lab.s2jh.core.pagination.PropertyFilter.MatchType;
import lab.s2jh.core.security.AclService;
import lab.s2jh.core.security.AuthContextHolder;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.core.service.Validation;
import lab.s2jh.core.web.annotation.SecurityControlIgnore;
import lab.s2jh.core.web.json.ValueLabelBean;
import lab.s2jh.core.web.view.OperationResult;
import lab.s2jh.sys.service.MenuService;
import lab.s2jh.sys.vo.NavMenuVO;
import lab.s2jh.web.mvc.BaseMvcController;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * User management controller - Spring MVC version
 */
@Controller
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
@RequestMapping("/admin/auth/user")
public class UserMvcController extends BaseMvcController<User, Long> {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PrivilegeService privilegeService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private MenuService menuService;

    @Autowired(required = false)
    private AclService aclService;

    @Override
    protected void checkEntityAclPermission(User entity) {
        if (aclService != null) {
            aclService.validateAuthUserAclCodePermission(entity.getAclCode());
        }
    }

    public Map<String, String> getAclTypeMap() {
        Map<String, String> aclTypeMap = Maps.newLinkedHashMap();
        if (aclService != null) {
            // Null check for test environments without security context
            if (AuthContextHolder.getAuthUserDetails() == null) {
                aclTypeMap = aclService.getAclTypeMap();
            } else {
                String authUserAclType = AuthContextHolder.getAuthUserDetails().getAclType();
                if (StringUtils.isBlank(authUserAclType)) {
                    aclTypeMap = aclService.getAclTypeMap();
                } else {
                    Map<String, String> globalAclTypeMap = aclService.getAclTypeMap();
                    for (String aclType : globalAclTypeMap.keySet()) {
                        if (authUserAclType.compareTo(aclType) >= 0) {
                            aclTypeMap.put(aclType, globalAclTypeMap.get(aclType));
                        }
                    }
                }
            }
        }
        return aclTypeMap;
    }

    @SecurityControlIgnore
    @RequestMapping(value = "aclTypeMapData", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> aclTypeMapData() {
        return getAclTypeMap();
    }

    @Override
    protected BaseService<User, Long> getEntityService() {
        return userService;
    }

    @MetaData(value = "角色关联")
    @SecurityControlIgnore
    @RequestMapping(value = "roles", method = RequestMethod.GET)
    public String roles(Model model) {
        List<Role> roles = Lists.newArrayList();
        List<Role> allRoles = roleService.findAllCached();
        List<UserR2Role> r2s = userService.findOne(this.getId()).getUserR2Roles();
        for (Role role : allRoles) {
            if (Role.ROLE_ANONYMOUSLY_CODE.equals(role.getCode())) {
                continue;
            }
            role.addExtraAttribute("related", false);
            for (UserR2Role r2 : r2s) {
                if (r2.getRole().equals(role)) {
                    role.addExtraAttribute("r2CreatedDate", r2.getCreatedDate());
                    role.addExtraAttribute("related", true);
                    break;
                }
            }
            roles.add(role);
        }
        model.addAttribute("roles", roles);
        return resolveView("roles");
    }

    @MetaData(value = "更新角色关联")
    @RequestMapping(value = "doUpdateRelatedRoleR2s", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doUpdateRelatedRoleR2s() {
        User entity = getBindingEntity();
        if (entity != null && entity.getSigninid().equals("admin")) {
            Validation.notDemoMode();
        }
        userService.updateRelatedRoleR2s(getId(), getParameterIds("r2ids"));
        return OperationResult.buildSuccessResult("更新角色关联操作完成");
    }

    @Override
    @MetaData(value = "保存")
    @RequestMapping(value = "doSave", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doSave() {
        User entity = getBindingEntity();
        if (entity != null && entity.getSigninid() != null && entity.getSigninid().equals("admin")) {
            Validation.notDemoMode();
        }
        /**
         * 判断选取的用户机构代码是否属于当前登录用户管辖范围
         * 该属性设定为不允许自动绑定，则需要手工从请求参数获取设置 @see lab.s2jh.auth.entity.User#setAclCode
         */
        String aclCode = this.getParameter("aclCode");
        if (entity != null) {
            entity.setAclCode(aclCode);
        }
        if (entity != null && entity.isNew()) {
            userService.save(entity, this.getRequiredParameter("newpassword"));
            return OperationResult.buildSuccessResult("创建操作成功", entity);
        } else if (entity != null) {
            String newpassword = this.getParameter("newpassword");
            if (StringUtils.isNotBlank(newpassword)) {
                userService.save(entity, newpassword);
            } else {
                userService.save(entity);
            }
            return OperationResult.buildSuccessResult("更新操作成功", entity);
        } else {
            return OperationResult.buildFailureResult("无法绑定实体对象");
        }
    }

    @Override
    @MetaData(value = "查询")
    @RequestMapping(value = "findByPage", method = RequestMethod.GET)
    @ResponseBody
    public Page<User> findByPage() {
        GroupPropertyFilter groupFilter = GroupPropertyFilter.buildFromHttpRequest(entityClass, getRequest());
        if (AuthContextHolder.getAuthUserDetails() != null) {
            Collection<String> aclCodePrefixs = AuthContextHolder.getAuthUserDetails().getAclCodePrefixs();
            if (!CollectionUtils.isEmpty(aclCodePrefixs)) {
                groupFilter.forceAnd(new PropertyFilter(MatchType.ACLPREFIXS, "aclCode", aclCodePrefixs));
            }
            String authUserAclType = AuthContextHolder.getAuthUserDetails().getAclType();
            if (StringUtils.isNotBlank(authUserAclType)) {
                groupFilter.forceAnd(new PropertyFilter(MatchType.LE, "aclType", authUserAclType));
            }
        }
        String departmentId = getParameter("departmentId");
        if (StringUtils.isNotBlank(departmentId)) {
            String searchType = getParameter("searchType", "current");
            if ("current".equals(searchType)) {
                groupFilter.forceAnd(new PropertyFilter(MatchType.EQ, "department.id", departmentId));
            } else {
                Department current = departmentService.findOne(departmentId);
                List<Department> departments = departmentService.findChildrenCascade(current);
                departments.add(current);
                groupFilter.forceAnd(new PropertyFilter(MatchType.IN, "department", departments));
            }
        }

        Pageable pageable = PropertyFilter.buildPageableFromHttpRequest(getRequest());
        Page<User> page = this.getEntityService().findByPage(groupFilter, pageable);
        if (aclService != null) {
            Map<String, String> globalAclTypeMap = aclService.getAclTypeMap();
            for (User user : page.getContent()) {
                user.addExtraAttribute("aclTypeLabel", globalAclTypeMap.get(user.getAclType()));
            }
        }
        return page;
    }

    @Override
    @MetaData(value = "删除")
    @RequestMapping(value = "doDelete", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doDelete() {
        User entity = getBindingEntity();
        if (entity != null && entity.getSigninid() != null && entity.getSigninid().equals("admin")) {
            Validation.notDemoMode();
        }
        return super.doDelete();
    }

    @MetaData(value = "机构选取的Autocomplete数据")
    @SecurityControlIgnore
    @RequestMapping(value = "aclCodes", method = RequestMethod.GET)
    @ResponseBody
    public List<ValueLabelBean> aclCodes() {
        List<ValueLabelBean> lvList = Lists.newArrayList();
        if (aclService != null && AuthContextHolder.getAuthUserDetails() != null) {
            String term = this.getParameter("term");
            if (term != null && term.length() >= 2) {
                Map<String, String> keyValueMap = aclService.findAclCodesMap();
                Collection<String> aclCodePrefixs = AuthContextHolder.getAuthUserDetails().getAclCodePrefixs();

                for (Map.Entry<String, String> me : keyValueMap.entrySet()) {
                    String key = me.getKey();
                    if (key.startsWith(term)) {
                        for (String aclCodePrefix : aclCodePrefixs) {
                            if (key.startsWith(aclCodePrefix)) {
                                lvList.add(new ValueLabelBean(me.getKey(), me.getValue()));
                            }
                        }
                    }
                }
            }
        }
        return lvList;
    }

    @MetaData(value = "汇总用户关联权限集合")
    @RequestMapping(value = "privileges", method = RequestMethod.GET)
    public String privileges(Model model) {
        List<Privilege> privileges = privilegeService.findAllCached();
        List<Role> roles = roleService.findR2RolesForUser(bindingEntity);
        boolean isAdmin = false;
        for (Role role : roles) {
            if (role.getCode().equals(Role.ROLE_ADMIN_CODE)) {
                isAdmin = true;
                break;
            }
        }

        Map<String, List<Privilege>> groupDatas = Maps.newLinkedHashMap();
        List<Privilege> r2s = null;
        if (isAdmin) {
            r2s = privileges;
        } else {
            r2s = userService.findRelatedPrivilegesForUser(bindingEntity);
        }
        for (Privilege privilege : privileges) {
            List<Privilege> groupPrivileges = groupDatas.get(privilege.getCategory());
            if (groupPrivileges == null) {
                groupPrivileges = Lists.newArrayList();
                groupDatas.put(privilege.getCategory(), groupPrivileges);
            }
            groupPrivileges.add(privilege);
            privilege.addExtraAttribute("related", false);
            if (r2s.contains(privilege)) {
                privilege.addExtraAttribute("related", true);
            }
        }
        model.addAttribute("privileges", groupDatas);
        return resolveView("privileges");
    }

    @MetaData(value = "汇总用户关联菜单集合")
    @RequestMapping(value = "menus", method = RequestMethod.GET)
    @ResponseBody
    public List<NavMenuVO> menus() {
        Set<GrantedAuthority> authsSet = new HashSet<GrantedAuthority>();
        List<Role> roles = roleService.findR2RolesForUser(bindingEntity);
        for (Role role : roles) {
            authsSet.add(new SimpleGrantedAuthority(role.getCode()));
        }
        return menuService.authUserMenu(authsSet, this.getRequest().getContextPath());
    }

    @MetaData(value = "版本数据列表")
    @RequestMapping(value = "revisionList", method = RequestMethod.GET)
    @ResponseBody
    public Page<EntityRevision> revisionList() {
        return super.revisionList();
    }

    @MetaData(value = "版本数据对比")
    @RequestMapping(value = "revisionCompare", method = RequestMethod.GET)
    public String revisionCompare(Model model) {
        return super.revisionCompare(model);
    }

    @RequestMapping(value = "resetActivitiIndentityData", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult resetActivitiIndentityData() {
        userService.resetActivitiIndentityData();
        return OperationResult.buildSuccessResult("工作流用户群组数据重置操作完成");
    }
}
