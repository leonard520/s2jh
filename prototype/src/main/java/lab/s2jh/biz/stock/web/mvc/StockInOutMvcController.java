package lab.s2jh.biz.stock.web.mvc;

import lab.s2jh.biz.stock.entity.StockInOut;
import lab.s2jh.biz.stock.service.StockInOutService;
import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.web.mvc.BaseMvcController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Stock in/out controller - Spring MVC version
 */
@Controller
@RequestMapping("/admin/biz/stock/stock-in-out")
@MetaData("StockInOutController")
public class StockInOutMvcController extends BaseMvcController<StockInOut, Long> {

    @Autowired
    private StockInOutService stockInOutService;

    @Override
    protected BaseService<StockInOut, Long> getEntityService() {
        return stockInOutService;
    }

    @Override
    protected void checkEntityAclPermission(StockInOut entity) {
        // Nothing to do
    }

    @Override
    @MetaData("查询")
    @RequestMapping(value = "findByPage", method = RequestMethod.GET)
    @ResponseBody
    public Page<StockInOut> findByPage() {
        return super.findByPage();
    }
}
