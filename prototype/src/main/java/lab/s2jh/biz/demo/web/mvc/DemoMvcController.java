package lab.s2jh.biz.demo.web.mvc;

import lab.s2jh.biz.demo.entity.Demo;
import lab.s2jh.biz.demo.service.DemoService;
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
 * Demo controller - Spring MVC version
 */
@Controller
@RequestMapping("/admin/biz/demo")
@MetaData(value = "[TODO控制器名称]")
public class DemoMvcController extends BaseMvcController<Demo, String> {

    @Autowired
    private DemoService demoService;

    @Override
    protected BaseService<Demo, String> getEntityService() {
        return demoService;
    }

    @Override
    protected void checkEntityAclPermission(Demo entity) {
        // TODO Add acl check code logic
    }

    @MetaData(value = "[TODO方法作用]")
    @RequestMapping(value = "todo", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult todo() {
        // TODO
        return OperationResult.buildSuccessResult("TODO操作完成");
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
    public Page<Demo> findByPage() {
        return super.findByPage();
    }
}
