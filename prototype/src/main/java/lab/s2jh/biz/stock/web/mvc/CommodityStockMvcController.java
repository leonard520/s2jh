package lab.s2jh.biz.stock.web.mvc;

import java.math.BigDecimal;

import lab.s2jh.biz.core.constant.VoucherTypeEnum;
import lab.s2jh.biz.md.entity.Commodity;
import lab.s2jh.biz.md.service.CommodityService;
import lab.s2jh.biz.stock.entity.CommodityStock;
import lab.s2jh.biz.stock.entity.StockInOut;
import lab.s2jh.biz.stock.entity.StorageLocation;
import lab.s2jh.biz.stock.service.CommodityStockService;
import lab.s2jh.biz.stock.service.StockInOutService;
import lab.s2jh.biz.stock.service.StorageLocationService;
import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.core.service.Validation;
import lab.s2jh.core.web.view.OperationResult;
import lab.s2jh.web.mvc.BaseMvcController;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Commodity stock controller - Spring MVC version
 */
@Controller
@RequestMapping("/admin/biz/stock/commodity-stock")
@MetaData("商品库存")
public class CommodityStockMvcController extends BaseMvcController<CommodityStock, Long> {

    @Autowired
    private CommodityStockService commodityStockService;
    @Autowired
    private CommodityService commodityService;
    @Autowired
    private StorageLocationService storageLocationService;
    @Autowired
    private StockInOutService stockInOutService;

    @Override
    protected BaseService<CommodityStock, Long> getEntityService() {
        return commodityStockService;
    }

    @Override
    protected void checkEntityAclPermission(CommodityStock entity) {
        // TODO Auto-generated method stub
    }

    @Override
    @MetaData("保存")
    @RequestMapping(value = "doSave", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doSave() {
        CommodityStock entity = getBindingEntity();
        if (entity.isNotNew()) {
            CommodityStock oldCommodityStock = commodityStockService.findOne(entity.getId());
            if (!oldCommodityStock.getCurStockQuantity().equals(entity.getCurStockQuantity())
                    || !oldCommodityStock.getSalingTotalQuantity().equals(entity.getSalingTotalQuantity())
                    || !oldCommodityStock.getPurchasingTotalQuantity().equals(
                            entity.getPurchasingTotalQuantity())) {
                StockInOut stockInOut = new StockInOut();
                stockInOut.setCommodityStock(oldCommodityStock);
                stockInOut.setDiffQuantity(entity.getCurStockQuantity().subtract(
                        oldCommodityStock.getCurStockQuantity()));
                stockInOut.setDiffPurchasingQuantity(entity.getPurchasingTotalQuantity().subtract(
                        oldCommodityStock.getPurchasingTotalQuantity()));
                stockInOut.setDiffSalingQuantity(entity.getSalingTotalQuantity().subtract(
                        oldCommodityStock.getSalingTotalQuantity()));
                stockInOut.setOperationSummary("直接变更库存量数据");
                stockInOutService.saveCascade(stockInOut);
            } else {
                entity.setCurStockAmount(entity.getCostPrice().multiply(
                        entity.getCurStockQuantity()));
                getEntityService().save(entity);
            }
        } else {
            StockInOut stockInOut = new StockInOut();
            stockInOut.setCommodityStock(entity);
            stockInOut.setDiffQuantity(entity.getCurStockQuantity());
            stockInOut.setDiffPurchasingQuantity(entity.getPurchasingTotalQuantity());
            stockInOut.setDiffSalingQuantity(entity.getSalingTotalQuantity());
            entity.setCurStockQuantity(BigDecimal.ZERO);
            entity.setPurchasingTotalQuantity(BigDecimal.ZERO);
            entity.setSalingTotalQuantity(BigDecimal.ZERO);
            stockInOut.setOperationSummary("直接初始化库存量数据");
            stockInOutService.saveCascade(stockInOut);
        }
        return OperationResult.buildSuccessResult("数据保存成功", entity);
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
    public Page<CommodityStock> findByPage() {
        return super.findByPage();
    }

    @MetaData("盘存显示")
    @RequestMapping(value = "inventory", method = RequestMethod.GET)
    public String inventory(Model model) {
        return resolveView("inventory");
    }

    @RequestMapping(value = "findForInventory", method = RequestMethod.GET)
    @ResponseBody
    public Object findForInventory(
            @RequestParam("barcode") String barcode,
            @RequestParam(required = false) String batchNo,
            @RequestParam("storageLocationId") String storageLocationId) {
        Commodity commodity = commodityService.findByBarcode(barcode);
        if (commodity == null) {
            return OperationResult.buildFailureResult("未知商品数据: " + barcode);
        } else {
            StorageLocation storageLocation = storageLocationService.findOne(storageLocationId);
            CommodityStock commodityStock = commodityStockService.findBy(commodity, storageLocation, batchNo);
            Validation.isTrue(commodityStock != null, "无库存数据，请检查录入数据或先初始化库存数据");
            return commodityStock;
        }
    }

    @MetaData("盘存")
    @RequestMapping(value = "doInventory", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doInventory(@RequestParam(required = false) String inventoryExplain) {
        CommodityStock entity = getBindingEntity();
        CommodityStock commodityStock = commodityStockService.findBy(entity.getCommodity(),
                entity.getStorageLocation(), entity.getBatchNo());
        StockInOut stockInOut = new StockInOut();
        stockInOut.setCommodityStock(commodityStock);
        stockInOut.setVoucherType(VoucherTypeEnum.PC);
        stockInOut.setDiffQuantity(entity.getCurStockQuantity().subtract(commodityStock.getCurStockQuantity()));
        if (StringUtils.isNotBlank(inventoryExplain)) {
            stockInOut.setOperationSummary("移动盘存:" + inventoryExplain);
        } else {
            stockInOut.setOperationSummary("移动盘存: 无变更登记");
        }
        stockInOutService.saveCascade(stockInOut);
        return OperationResult.buildSuccessResult("数据保存成功", entity);
    }

    @MetaData(value = "按库存地汇总库存量")
    @RequestMapping(value = "findByGroupStorageLocation", method = RequestMethod.GET)
    @ResponseBody
    public Page<?> findByGroupStorageLocation() {
        return findByGroupAggregate("commodity.id", "commodity.sku", "commodity.barcode", "commodity.title",
                "storageLocation.id", "sum(curStockQuantity)", "sum(salingTotalQuantity)",
                "sum(purchasingTotalQuantity)", "sum(stockThresholdQuantity)", "sum(availableQuantity) as sumAvailableQuantity");
    }

    @MetaData(value = "按商品汇总库存量")
    @RequestMapping(value = "findByGroupCommodity", method = RequestMethod.GET)
    @ResponseBody
    public Page<?> findByGroupCommodity() {
        return findByGroupAggregate("commodity.id", "commodity.sku", "commodity.barcode", "commodity.title",
                "sum(curStockQuantity)", "sum(salingTotalQuantity)", "sum(purchasingTotalQuantity)",
                "sum(stockThresholdQuantity)", "sum(availableQuantity)");
    }
}
