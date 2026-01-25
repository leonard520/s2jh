package lab.s2jh.sys.web.mvc;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.core.web.view.OperationResult;
import lab.s2jh.sys.entity.ConfigProperty;
import lab.s2jh.sys.service.ConfigPropertyService;
import lab.s2jh.web.mvc.BaseMvcController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Config property management controller - Spring MVC version
 */
@Controller
@RequestMapping("/admin/sys/config-property")
@MetaData(value = "参数属性配置")
public class ConfigPropertyMvcController extends BaseMvcController<ConfigProperty, String> {

    @Autowired
    private ConfigPropertyService configPropertyService;

    @Override
    protected BaseService<ConfigProperty, String> getEntityService() {
        return configPropertyService;
    }

    @Override
    protected void checkEntityAclPermission(ConfigProperty entity) {
        // Nothing to do
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
    @MetaData(value = "查询")
    @RequestMapping(value = "findByPage", method = RequestMethod.GET)
    @ResponseBody
    public Page<ConfigProperty> findByPage() {
        return super.findByPage();
    }
}
