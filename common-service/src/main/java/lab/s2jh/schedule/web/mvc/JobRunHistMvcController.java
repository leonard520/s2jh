package lab.s2jh.schedule.web.mvc;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.core.web.view.OperationResult;
import lab.s2jh.schedule.entity.JobRunHist;
import lab.s2jh.schedule.service.JobRunHistService;
import lab.s2jh.web.mvc.BaseMvcController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Job run history controller - Spring MVC version
 */
@Controller
@RequestMapping("/admin/schedule/job-run-hist")
@MetaData(value = "定时任务运行记录")
public class JobRunHistMvcController extends BaseMvcController<JobRunHist, String> {

    @Autowired
    private JobRunHistService jobRunHistService;

    @Override
    protected BaseService<JobRunHist, String> getEntityService() {
        return jobRunHistService;
    }

    @Override
    protected void checkEntityAclPermission(JobRunHist entity) {
        // Nothing to do
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
    public Page<JobRunHist> findByPage() {
        return super.findByPage();
    }
}
