package lab.s2jh.auth.web.mvc;

import java.util.List;

import lab.s2jh.auth.entity.Role;
import lab.s2jh.auth.entity.SignupUser;
import lab.s2jh.auth.service.RoleService;
import lab.s2jh.auth.service.SignupUserService;
import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.core.web.view.OperationResult;
import lab.s2jh.web.mvc.BaseMvcController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Signup user management controller - Spring MVC version
 */
@Controller
@RequestMapping("/admin/auth/signup-user")
@MetaData("注册账号管理")
public class SignupUserMvcController extends BaseMvcController<SignupUser, String> {

    @Autowired
    private SignupUserService signupUserService;

    @Autowired
    private RoleService roleService;

    @Override
    protected BaseService<SignupUser, String> getEntityService() {
        return signupUserService;
    }

    @Override
    protected void checkEntityAclPermission(SignupUser entity) {
        // Nothing to do
    }

    public List<Role> getRoles() {
        return roleService.findAllCached();
    }

    @MetaData("审核")
    @RequestMapping(value = "doAudit", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doAudit() {
        SignupUser entity = getBindingEntity();
        String aclCode = this.getParameter("aclCode");
        entity.setAclCode(aclCode);
        signupUserService.audit(entity, roleService.findAll(getParameterIds("r2ids")));
        return OperationResult.buildSuccessResult("已审核处理并创建对应用户记录", entity);
    }

    @Override
    @MetaData("删除")
    @RequestMapping(value = "doDelete", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doDelete() {
        return super.doDelete();
    }

    @Override
    @MetaData("查询")
    @RequestMapping(value = "findByPage", method = RequestMethod.GET)
    @ResponseBody
    public Page<SignupUser> findByPage() {
        return super.findByPage();
    }
}
