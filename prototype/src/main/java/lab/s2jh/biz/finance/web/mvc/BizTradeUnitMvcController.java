package lab.s2jh.biz.finance.web.mvc;

import java.util.List;
import java.util.Map;
import java.util.Set;

import lab.s2jh.biz.finance.entity.BizTradeUnit;
import lab.s2jh.biz.finance.service.BizTradeUnitService;
import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.core.web.annotation.SecurityControlIgnore;
import lab.s2jh.core.web.view.OperationResult;
import lab.s2jh.web.mvc.BaseMvcController;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Business trade unit controller - Spring MVC version
 */
@Controller
@RequestMapping("/admin/biz/finance/biz-trade-unit")
@MetaData("业务往来单位管理")
public class BizTradeUnitMvcController extends BaseMvcController<BizTradeUnit, Long> {

    @Autowired
    private BizTradeUnitService bizTradeUnitService;

    @Override
    protected BaseService<BizTradeUnit, Long> getEntityService() {
        return bizTradeUnitService;
    }

    @Override
    protected void checkEntityAclPermission(BizTradeUnit entity) {
        // TODO Add acl check code logic
    }

    @MetaData("[TODO方法作用]")
    @RequestMapping(value = "todo", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult todo() {
        // TODO
        return OperationResult.buildSuccessResult("TODO操作完成");
    }

    @MetaData("创建")
    @RequestMapping(value = "doCreate", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doCreate() {
        getEntityService().save(bindingEntity);
        return OperationResult.buildSuccessResult("创建操作成功", bindingEntity);
    }

    @MetaData("更新")
    @RequestMapping(value = "doUpdate", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doUpdate() {
        getEntityService().save(bindingEntity);
        return OperationResult.buildSuccessResult("更新操作成功", bindingEntity);
    }

    @Override
    @MetaData("保存")
    @RequestMapping(value = "doSave", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doSave() {
        return super.doSave();
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
    public Page<BizTradeUnit> findByPage() {
        return super.findByPage();
    }

    @MetaData(value = "常用数据")
    @SecurityControlIgnore
    @RequestMapping(value = "frequentUsedDatas", method = RequestMethod.GET)
    @ResponseBody
    public Set<Map<String, Object>> frequentUsedDatas(@RequestParam(required = false) String type) {
        Set<Map<String, Object>> datas = Sets.newHashSet();
        List<BizTradeUnit> entities = bizTradeUnitService.findFrequentUsedDatas();
        for (BizTradeUnit entity : entities) {
            if (StringUtils.isNotBlank(type)) {
                if (!type.equals(entity.getType().name())) {
                    continue;
                }
            }
            Map<String, Object> item = Maps.newHashMap();
            item.put("id", entity.getId());
            item.put("display", entity.getDisplay());
            datas.add(item);
        }
        return datas;
    }
}
