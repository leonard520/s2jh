package lab.s2jh.biz.sale.web.mvc;

import lab.s2jh.biz.sale.entity.SaleDeliveryDetail;
import lab.s2jh.biz.sale.service.SaleDeliveryDetailService;
import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.web.mvc.BaseMvcController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Sale delivery detail controller - Spring MVC version
 */
@Controller
@RequestMapping("/admin/biz/sale/sale-delivery-detail")
@MetaData("销售(发货)单明细管理")
public class SaleDeliveryDetailMvcController extends BaseMvcController<SaleDeliveryDetail, Long> {

    @Autowired
    private SaleDeliveryDetailService saleDeliveryDetailService;

    @Override
    protected BaseService<SaleDeliveryDetail, Long> getEntityService() {
        return saleDeliveryDetailService;
    }

    @Override
    protected void checkEntityAclPermission(SaleDeliveryDetail entity) {
        // TODO Add acl check code logic
    }

    @Override
    @MetaData("查询")
    @RequestMapping(value = "findByPage", method = RequestMethod.GET)
    @ResponseBody
    public Page<SaleDeliveryDetail> findByPage() {
        return super.findByPage();
    }

    @MetaData(value = "销售商品毛利统计", comments = "由于可能出现完全赠品类型的0销售额订单，需要引入case when判断处理否则会出现除零错误")
    @RequestMapping(value = "findByGroupCommodity", method = RequestMethod.GET)
    @ResponseBody
    public Page<?> findByGroupCommodity() {
        return findByGroupAggregate("commodity.id", "commodity.sku", "commodity.title",
                "max(case(equal(amount,0),-1,quot(diff(amount,costAmount),amount))) as maxProfitRate",
                "min(case(equal(amount,0),-1,quot(diff(amount,costAmount),amount))) as minProfitRate",
                "sum(diff(amount,costAmount)) as sumProfitAmount", "sum(amount)", "sum(quantity)",
                "case(equal(sum(amount),0),-1,quot(sum(diff(amount,costAmount)),sum(amount))) as avgProfitRate");
    }
}
