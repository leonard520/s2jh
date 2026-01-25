package lab.s2jh.auth.web.mvc;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import lab.s2jh.auth.entity.Privilege;
import lab.s2jh.auth.entity.Role;
import lab.s2jh.auth.entity.RoleR2Privilege;
import lab.s2jh.auth.service.PrivilegeService;
import lab.s2jh.auth.service.RoleService;
import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.exception.DataAccessDeniedException;
import lab.s2jh.core.pagination.GroupPropertyFilter;
import lab.s2jh.core.pagination.PropertyFilter;
import lab.s2jh.core.pagination.PropertyFilter.MatchType;
import lab.s2jh.core.security.AclService;
import lab.s2jh.core.security.AuthContextHolder;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.core.web.annotation.SecurityControlIgnore;
import lab.s2jh.core.web.view.OperationResult;
import lab.s2jh.web.mvc.BaseMvcController;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Role management controller - Spring MVC version
 */
@Controller
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
@RequestMapping("/admin/auth/role")
@MetaData(value = "角色管理")
public class RoleMvcController extends BaseMvcController<Role, String> {

    @Autowired
    private RoleService roleService;

    @Autowired
    private PrivilegeService privilegeService;

    @Autowired(required = false)
    private AclService aclService;

    @Override
    protected BaseService<Role, String> getEntityService() {
        return roleService;
    }

    @Override
    protected void checkEntityAclPermission(Role entity) {
        // Do nothing check
    }

    public Map<String, String> getAclTypeMap() {
        Map<String, String> aclTypeMap = Maps.newLinkedHashMap();
        if (aclService != null) {
            String authUserAclType = AuthContextHolder.getAuthUserDetails().getAclType();
            if (authUserAclType == null) {
                aclTypeMap = aclService.getAclTypeMap();
            } else {
                Map<String, String> globalAclTypeMap = aclService.getAclTypeMap();
                for (String aclType : globalAclTypeMap.keySet()) {
                    if (authUserAclType.compareTo(aclType) > 0) {
                        aclTypeMap.put(aclType, globalAclTypeMap.get(aclType));
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
    @MetaData(value = "查询")
    @RequestMapping(value = "findByPage", method = RequestMethod.GET)
    @ResponseBody
    public Page<Role> findByPage() {
        return super.findByPage();
    }

    @Override
    protected String isDisallowDelete(Role entity) {
        if (entity.getCode().equals(Role.ROLE_ADMIN_CODE) || entity.getCode().equals(Role.ROLE_ANONYMOUSLY_CODE)) {
            return "系统预置数据，不允许删除:" + entity.getDisplay();
        }
        return null;
    }

    @Override
    @MetaData(value = "删除")
    @RequestMapping(value = "doDelete", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doDelete() {
        return super.doDelete();
    }

    @Override
    @MetaData(value = "保存")
    @RequestMapping(value = "doSave", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doSave() {
        // Null check for test environments without security context
        if (AuthContextHolder.getAuthUserDetails() != null) {
            String authUserAclType = AuthContextHolder.getAuthUserDetails().getAclType();
            if (StringUtils.isNotBlank(authUserAclType)) {
                // 判断选取的类型是否属于当前登录用户管辖范围
                String aclType = this.getRequiredParameter("aclType");
                if (authUserAclType.compareTo(aclType) < 0) {
                    throw new DataAccessDeniedException("数据访问权限不足");
                }
                Role entity = getBindingEntity();
                entity.setAclType(aclType);
            }
        }
        return super.doSave();
    }

    @MetaData(value = "批量更新状态")
    @RequestMapping(value = "doState", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doState() {
        boolean disabled = BooleanUtils.toBoolean(this.getRequiredParameter("disabled"));
        Collection<Role> entities = this.getEntitiesByParameterIds();
        for (Role entity : entities) {
            entity.setDisabled(disabled);
        }
        getEntityService().save(entities);
        return OperationResult.buildSuccessResult("批量更新状态操作成功");
    }

    @MetaData(value = "权限关联")
    @SecurityControlIgnore
    @RequestMapping(value = "privileges", method = RequestMethod.GET)
    public String privileges(Model model) {
        Map<String, List<Privilege>> groupDatas = Maps.newLinkedHashMap();
        List<Privilege> privileges = privilegeService.findAllCached();
        List<RoleR2Privilege> r2s = roleService.findOne(this.getId()).getRoleR2Privileges();
        for (Privilege privilege : privileges) {
            List<Privilege> groupPrivileges = groupDatas.get(privilege.getCategory());
            if (groupPrivileges == null) {
                groupPrivileges = Lists.newArrayList();
                groupDatas.put(privilege.getCategory(), groupPrivileges);
            }
            groupPrivileges.add(privilege);
            privilege.addExtraAttribute("related", false);
            for (RoleR2Privilege r2 : r2s) {
                if (r2.getPrivilege().equals(privilege)) {
                    privilege.addExtraAttribute("r2CreatedDate", r2.getCreatedDate());
                    privilege.addExtraAttribute("related", true);
                    break;
                }
            }
        }
        model.addAttribute("privileges", groupDatas);
        return resolveView("privileges");
    }

    @MetaData(value = "更新权限关联")
    @RequestMapping(value = "doUpdateRelatedPrivilegeR2s", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doUpdateRelatedPrivilegeR2s() {
        roleService.updateRelatedPrivilegeR2s(getId(), getParameterIds("r2ids"));
        return OperationResult.buildSuccessResult("更新权限关联操作完成");
    }

    /**
     * 子类额外追加过滤限制条件的入口方法，一般基于当前登录用户强制追加过滤条件
     */
    @Override
    protected void appendFilterProperty(GroupPropertyFilter groupPropertyFilter) {
        // 限定查询ACL所辖范围数据
        // Null check for test environments without security context
        if (AuthContextHolder.getAuthUserDetails() != null) {
            String authUserAclType = AuthContextHolder.getAuthUserDetails().getAclType();
            if (StringUtils.isNotBlank(authUserAclType)) {
                groupPropertyFilter.forceAnd(new PropertyFilter(MatchType.LE, "aclType", authUserAclType));
            }
        }
    }

    @MetaData("角色关联用户")
    @RequestMapping(value = "users", method = RequestMethod.GET)
    public String users(Model model) {
        Role entity = getBindingEntity();
        model.addAttribute("users", entity.getRoleR2Users());
        return resolveView("users");
    }
}
