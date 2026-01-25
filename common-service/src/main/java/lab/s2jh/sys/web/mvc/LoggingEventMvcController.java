package lab.s2jh.sys.web.mvc;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.core.web.mvc.PersistableMvcController;
import lab.s2jh.core.web.view.OperationResult;
import lab.s2jh.sys.entity.LoggingEvent;
import lab.s2jh.sys.service.LoggingEventService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Logging event controller - Spring MVC version
 * Note: Extends PersistableMvcController directly because LoggingEvent doesn't extend BaseEntity
 */
@Controller
@RequestMapping("/admin/sys/logging-event")
@MetaData(value = "日志处理")
public class LoggingEventMvcController extends PersistableMvcController<LoggingEvent, Long> {

    @Autowired
    private LoggingEventService loggingEventService;

    @Override
    protected BaseService<LoggingEvent, Long> getEntityService() {
        return loggingEventService;
    }

    @Override
    protected void checkEntityAclPermission(LoggingEvent entity) {
        // TODO Add acl check code logic
    }

    @MetaData(value = "更新")
    @RequestMapping(value = "doUpdate", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doUpdate() {
        getEntityService().save(bindingEntity);
        return OperationResult.buildSuccessResult("更新操作成功", bindingEntity);
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
    public Page<LoggingEvent> findByPage() {
        return super.findByPage();
    }
}
