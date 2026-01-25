package ${root_package}.web.mvc;

import lab.s2jh.core.annotation.MetaData;
import ${root_package}.entity.${entity_name};
import ${root_package}.service.${entity_name}Service;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.web.mvc.BaseMvcController;
import lab.s2jh.core.web.view.OperationResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/admin/${root_package_path}/${entity_name_path}")
@MetaData("${model_title}管理")
public class ${entity_name}MvcController extends BaseMvcController<${entity_name},${id_type}> {

    @Autowired
    private ${entity_name}Service ${entity_name_uncapitalize}Service;

    @Override
    protected BaseService<${entity_name}, ${id_type}> getEntityService() {
        return ${entity_name_uncapitalize}Service;
    }
    
    @Override
    protected void checkEntityAclPermission(${entity_name} entity) {
        // TODO Add acl check code logic
    }

    @MetaData("[TODO方法作用]")
    @PostMapping("todo")
    @ResponseBody
    public OperationResult todo() {
        //TODO
        return OperationResult.buildSuccessResult("TODO操作完成");
    }
    
    @Override
    @MetaData("创建")
    @PostMapping("doCreate")
    @ResponseBody
    public OperationResult doCreate() {
        return super.doCreate();
    }

    @Override
    @MetaData("更新")
    @PostMapping("doUpdate")
    @ResponseBody
    public OperationResult doUpdate() {
        return super.doUpdate();
    }
    
    @Override
    @MetaData("保存")
    @PostMapping("doSave")
    @ResponseBody
    public OperationResult doSave() {
        return super.doSave();
    }

    @Override
    @MetaData("删除")
    @PostMapping("doDelete")
    @ResponseBody
    public OperationResult doDelete() {
        return super.doDelete();
    }

    @Override
    @MetaData("查询")
    @GetMapping("findByPage")
    @ResponseBody
    public Page<${entity_name}> findByPage() {
        return super.findByPage();
    }
}