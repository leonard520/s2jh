package lab.s2jh.auth.web.mvc;

import lab.s2jh.auth.entity.UserLogonLog;
import lab.s2jh.auth.service.UserLogonLogService;
import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.web.mvc.BaseMvcController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;import org.springframework.web.bind.annotation.ResponseBody;

/**
 * User logon log controller - Spring MVC version
 */
@Controller
@RequestMapping("/admin/auth/user-logon-log")
@MetaData(value = "登录记录")
public class UserLogonLogMvcController extends BaseMvcController<UserLogonLog, Long> {

    @Autowired
    private UserLogonLogService userLogonLogService;

    @Override
    protected BaseService<UserLogonLog, Long> getEntityService() {
        return userLogonLogService;
    }

    @Override
    protected void checkEntityAclPermission(UserLogonLog entity) {
        // Allow all
    }

    @Override
    @MetaData(value = "查询")
    @RequestMapping(value = "findByPage", method = RequestMethod.GET)
    @ResponseBody
    public Page<UserLogonLog> findByPage() {
        return super.findByPage();
    }
}
