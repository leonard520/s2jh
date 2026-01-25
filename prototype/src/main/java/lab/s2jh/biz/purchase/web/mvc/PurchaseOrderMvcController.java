package lab.s2jh.biz.purchase.web.mvc;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lab.s2jh.auth.entity.Department;
import lab.s2jh.auth.entity.User;
import lab.s2jh.auth.security.AuthUserHolder;
import lab.s2jh.auth.service.DepartmentService;
import lab.s2jh.auth.service.UserService;
import lab.s2jh.biz.core.constant.VoucherTypeEnum;
import lab.s2jh.biz.core.service.VoucherNumGenerateService;
import lab.s2jh.biz.finance.service.BizTradeUnitService;
import lab.s2jh.biz.md.entity.Commodity;
import lab.s2jh.biz.md.service.CommodityService;
import lab.s2jh.biz.purchase.entity.PurchaseOrder;
import lab.s2jh.biz.purchase.entity.PurchaseOrderDetail;
import lab.s2jh.biz.purchase.service.PurchaseOrderService;
import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.audit.envers.EntityRevision;
import lab.s2jh.core.pagination.GroupPropertyFilter;
import lab.s2jh.core.pagination.PropertyFilter;
import lab.s2jh.core.pagination.PropertyFilter.MatchType;
import lab.s2jh.core.security.AuthContextHolder;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.core.web.view.OperationResult;
import lab.s2jh.web.mvc.BaseMvcController;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Maps;

/**
 * Purchase order controller - Spring MVC version
 */
@Controller
@RequestMapping("/admin/biz/purchase/purchase-order")
@MetaData("采购订单")
public class PurchaseOrderMvcController extends BaseMvcController<PurchaseOrder, Long> {

    private final Logger logger = LoggerFactory.getLogger(PurchaseOrderMvcController.class);

    @Autowired
    private PurchaseOrderService purchaseOrderService;
    @Autowired
    private BizTradeUnitService bizTradeUnitService;
    @Autowired(required = false)
    private TaskService taskService;
    @Autowired
    private CommodityService commodityService;
    @Autowired(required = false)
    private RuntimeService runtimeService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private UserService userService;

    @Autowired
    private VoucherNumGenerateService voucherNumGenerateService;

    @Override
    protected BaseService<PurchaseOrder, Long> getEntityService() {
        return purchaseOrderService;
    }

    @Override
    protected void checkEntityAclPermission(PurchaseOrder entity) {
        // TODO Add acl check code logic
    }

    @Override
    protected void setupDetachedBindingEntity(Long id) {
        bindingEntity = getEntityService().findDetachedOne(id, "purchaseOrderDetails");
    }

    @Override
    @MetaData("查询")
    @RequestMapping(value = "findByPage", method = RequestMethod.GET)
    @ResponseBody
    public Page<PurchaseOrder> findByPage() {
        return super.findByPage();
    }

    public Map<String, Object> getTaskVariables(@RequestParam("taskId") String taskId) {
        Map<String, Object> variables = taskService.getVariables(taskId);
        if (logger.isDebugEnabled()) {
            for (Map.Entry<String, Object> me : variables.entrySet()) {
                logger.debug("{} - {}", me.getKey(), me.getValue());
            }
        }
        return variables;
    }

    public Map<String, String> getDepartmentsMap() {
        Map<String, String> departmentsMap = new LinkedHashMap<String, String>();
        GroupPropertyFilter groupPropertyFilter = GroupPropertyFilter.buildDefaultAndGroupFilter();
        Iterable<Department> departments = departmentService.findByFilters(groupPropertyFilter);
        Iterator<Department> it = departments.iterator();
        while (it.hasNext()) {
            Department department = it.next();
            departmentsMap.put(department.getId(), department.getDisplay());
        }
        return departmentsMap;
    }

    public Map<Long, String> getUsersMap() {
        Map<Long, String> usersMap = new LinkedHashMap<Long, String>();
        GroupPropertyFilter groupPropertyFilter = GroupPropertyFilter.buildDefaultAndGroupFilter();
        groupPropertyFilter.append(new PropertyFilter(MatchType.EQ, "enabled", Boolean.TRUE));
        Iterable<User> users = userService.findByFilters(groupPropertyFilter);
        Iterator<User> it = users.iterator();
        while (it.hasNext()) {
            User user = it.next();
            usersMap.put(user.getId(), user.getDisplay());
        }
        return usersMap;
    }

    @Override
    @MetaData("保存")
    @RequestMapping(value = "doSave", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doSave() {
        PurchaseOrder entity = getBindingEntity();
        if (entity.isNew()) {
            List<PurchaseOrderDetail> purchaseOrderDetails = entity.getPurchaseOrderDetails();
            for (PurchaseOrderDetail purchaseOrderDetail : purchaseOrderDetails) {
                purchaseOrderDetail.setPurchaseOrder(entity);
            }
        }
        return super.doSave();
    }

    private void prepareBpmNew(boolean clone) {
        PurchaseOrder entity = getBindingEntity();
        if (clone) {
            entity.resetCommonProperties();
        } else {
            newBindingEntity();
            entity = getBindingEntity();
        }
        entity.setVoucher(voucherNumGenerateService.getVoucherNumByType(VoucherTypeEnum.JHD));
        entity.setVoucherDate(new Date());
        entity.setVoucherUser(AuthUserHolder.getLogonUser());
        entity.setVoucherDepartment(AuthUserHolder.getLogonUserDepartment());
    }

    @MetaData("采购订单创建初始化")
    @RequestMapping(value = "bpmNew", method = RequestMethod.GET)
    public String bpmNew(@RequestParam(required = false) String clone, Model model) {
        prepareBpmNew(BooleanUtils.toBoolean(clone));
        PurchaseOrder entity = getBindingEntity();
        model.addAttribute("entity", entity);
        return resolveView("bpmInput");
    }

    @MetaData("采购订单保存")
    @RequestMapping(value = "bpmSave", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult bpmSave(
            @RequestParam(required = false) String submitToAudit,
            @RequestParam(required = false) String taskId) {

        PurchaseOrder entity = getBindingEntity();
        Map<String, Object> variables = Maps.newHashMap();
        if (BooleanUtils.toBoolean(submitToAudit)) {
            entity.setLastOperationSummary(entity.buildLastOperationSummary("提交"));
            entity.setSubmitDate(new Date());
        }

        List<PurchaseOrderDetail> purchaseOrderDetails = entity.getPurchaseOrderDetails();
        for (PurchaseOrderDetail purchaseOrderDetail : purchaseOrderDetails) {
            purchaseOrderDetail.setPurchaseOrder(entity);
        }
        if (StringUtils.isBlank(entity.getTitle())) {
            Commodity commodity = commodityService.findOne(purchaseOrderDetails.get(0).getCommodity().getId());
            String commodityTitle = commodity.getTitle();
            commodityTitle = StringUtils.substring(commodityTitle, 0, 30);
            entity.setTitle(commodityTitle + "...等" + purchaseOrderDetails.size() + "项商品");
        }

        if (entity.isNew()) {
            purchaseOrderService.bpmCreate(entity, variables);
            return OperationResult.buildSuccessResult("采购订单创建完成，并同步启动处理流程", entity);
        } else {
            purchaseOrderService.bpmUpdate(entity, taskId, variables);
            return OperationResult.buildSuccessResult("采购订单任务提交完成", entity);
        }
    }

    @MetaData("采购订单定价")
    @RequestMapping(value = "bpmPrice", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult bpmPrice(@RequestParam("taskId") String taskId) {
        PurchaseOrder entity = getBindingEntity();
        Map<String, Object> variables = Maps.newHashMap();
        List<PurchaseOrderDetail> purchaseOrderDetails = entity.getPurchaseOrderDetails();
        for (PurchaseOrderDetail purchaseOrderDetail : purchaseOrderDetails) {
            purchaseOrderDetail.setPurchaseOrder(entity);
        }
        purchaseOrderService.bpmUpdate(entity, taskId, variables);
        return OperationResult.buildSuccessResult("采购订单定价完成", entity);
    }

    @MetaData("一线审核")
    @RequestMapping(value = "bpmLevel1Audit", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult bpmLevel1Audit(
            @RequestParam("taskId") String taskId,
            @RequestParam("auditLevel1Pass") Boolean auditLevel1Pass,
            @RequestParam(required = false) String auditLevel1Explain) {
        PurchaseOrder entity = getBindingEntity();
        Map<String, Object> variables = Maps.newHashMap();
        variables.put("auditLevel1Time", new Date());
        variables.put("auditLevel1User", AuthContextHolder.getAuthUserPin());
        variables.put("auditLevel1Pass", auditLevel1Pass);
        variables.put("auditLevel1Explain", auditLevel1Explain);
        entity.setLastOperationSummary(entity.buildLastOperationSummary("审核"));
        if (!auditLevel1Pass) {
            entity.setSubmitDate(null);
        }
        purchaseOrderService.bpmUpdate(entity, taskId, variables);
        return OperationResult.buildSuccessResult("采购订单一线审核完成", entity);
    }

    @MetaData("二线审核")
    @RequestMapping(value = "bpmLevel2Audit", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult bpmLevel2Audit(
            @RequestParam("taskId") String taskId,
            @RequestParam("auditLevel2Pass") Boolean auditLevel2Pass,
            @RequestParam(required = false) String auditLevel2Explain) {
        PurchaseOrder entity = getBindingEntity();
        Map<String, Object> variables = Maps.newHashMap();
        variables.put("auditLevel2Time", new Date());
        variables.put("auditLevel2User", AuthContextHolder.getAuthUserPin());
        variables.put("auditLevel2Pass", auditLevel2Pass);
        variables.put("auditLevel2Explain", auditLevel2Explain);
        if (!auditLevel2Pass) {
            entity.setSubmitDate(null);
        }
        purchaseOrderService.bpmUpdate(entity, taskId, variables);
        return OperationResult.buildSuccessResult("采购订单二线审核完成", entity);
    }

    @MetaData("采购订单付款界面显示")
    @RequestMapping(value = "bpmPayInput", method = RequestMethod.GET)
    public String bpmPayInput(Model model) {
        PurchaseOrder entity = getBindingEntity();
        model.addAttribute("entity", entity);
        return resolveView("bpmPay");
    }

    @MetaData("(预)付款任务")
    @RequestMapping(value = "bpmPay", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult bpmPay(@RequestParam("taskId") String taskId) {
        PurchaseOrder entity = getBindingEntity();
        purchaseOrderService.bpmPay(entity, taskId);
        return OperationResult.buildSuccessResult("采购(预)付款任务处理完成", entity);
    }

    @MetaData("保存卖家发货信息")
    @RequestMapping(value = "bpmDelivery", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult bpmDelivery(@RequestParam("taskId") String taskId) {
        PurchaseOrder entity = getBindingEntity();
        entity.setDeliveryTime(new Date());
        purchaseOrderService.bpmDelivery(entity, taskId);
        return OperationResult.buildSuccessResult("录入卖家发货信息完成", entity);
    }

    @MetaData("行项数据")
    @RequestMapping(value = "purchaseOrderDetails", method = RequestMethod.GET)
    @ResponseBody
    public Page<PurchaseOrderDetail> purchaseOrderDetails(@RequestParam(required = false) String clone) {
        PurchaseOrder entity = getBindingEntity();
        List<PurchaseOrderDetail> purchaseOrderDetails = entity.getPurchaseOrderDetails();
        if (BooleanUtils.toBoolean(clone)) {
            for (PurchaseOrderDetail purchaseOrderDetail : purchaseOrderDetails) {
                purchaseOrderDetail.resetCommonProperties();
            }
        }
        return buildPageResultFromList(purchaseOrderDetails);
    }

    @MetaData("单据红冲")
    @RequestMapping(value = "doRedword", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doRedword() {
        PurchaseOrder entity = getBindingEntity();
        Assert.isTrue(entity.getRedwordDate() == null);
        purchaseOrderService.redword(entity);
        return OperationResult.buildSuccessResult("红冲完成");
    }

    @RequestMapping(value = "revisionList", method = RequestMethod.GET)
    @ResponseBody
    public Page<EntityRevision> revisionList() {
        return super.revisionList();
    }

    @RequestMapping(value = "revisionCompare", method = RequestMethod.GET)
    public String revisionCompare(Model model) {
        return super.revisionCompare(model);
    }
}
