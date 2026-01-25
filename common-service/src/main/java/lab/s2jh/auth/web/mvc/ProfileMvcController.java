package lab.s2jh.auth.web.mvc;

import lab.s2jh.auth.entity.User;
import lab.s2jh.auth.security.AuthUserHolder;
import lab.s2jh.auth.service.UserService;
import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.security.AuthContextHolder;
import lab.s2jh.core.security.AuthUserDetails;
import lab.s2jh.core.service.Validation;
import lab.s2jh.core.web.annotation.SecurityControlIgnore;
import lab.s2jh.core.web.mvc.BaseMvcController;
import lab.s2jh.core.web.view.OperationResult;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Profile/password management controller - Spring MVC version
 */
@Controller
@RequestMapping("/admin/auth/profile")
@MetaData(value = "个人配置")
public class ProfileMvcController extends BaseMvcController {

    @Autowired
    private UserService userService;

    @SecurityControlIgnore
    @MetaData(value = "密码修改显示")
    @RequestMapping(value = "passwd", method = RequestMethod.GET)
    public String passwd() {
        return "admin/auth/profile-passwd";
    }

    @SecurityControlIgnore
    @MetaData(value = "密码修改处理")
    @RequestMapping(value = "doPasswd", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doPasswd() {
        Validation.notDemoMode();
        AuthUserDetails authUserDetails = AuthContextHolder.getAuthUserDetails();
        Assert.notNull(authUserDetails);
        
        String oldpasswd = getRequest().getParameter("oldpasswd");
        String newpasswd = getRequest().getParameter("newpasswd");
        Assert.isTrue(StringUtils.isNotBlank(oldpasswd));
        Assert.isTrue(StringUtils.isNotBlank(newpasswd));

        User user = AuthUserHolder.getLogonUser();
        String encodedPasswd = userService.encodeUserPasswd(user, oldpasswd);
        if (!encodedPasswd.equals(user.getPassword())) {
            return OperationResult.buildFailureResult("原密码不正确,请重新输入");
        } else {
            userService.save(user, newpasswd);
            return OperationResult.buildSuccessResult("密码修改成功,请在下次登录使用新密码");
        }
    }
}
