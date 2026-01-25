package lab.s2jh.schedule.web.mvc;

import java.util.List;
import java.util.Map;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.core.web.view.OperationResult;
import lab.s2jh.schedule.ExtSchedulerFactoryBean;
import lab.s2jh.schedule.entity.JobBeanCfg;
import lab.s2jh.schedule.service.JobBeanCfgService;
import lab.s2jh.web.mvc.BaseMvcController;

import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Job bean configuration controller - Spring MVC version
 */
@Controller
@RequestMapping("/admin/schedule/job-bean-cfg")
@MetaData(value = "定时任务配置管理")
public class JobBeanCfgMvcController extends BaseMvcController<JobBeanCfg, String> {

    @Autowired
    private JobBeanCfgService jobBeanCfgService;

    @Override
    protected BaseService<JobBeanCfg, String> getEntityService() {
        return jobBeanCfgService;
    }

    @Override
    protected void checkEntityAclPermission(JobBeanCfg entity) {
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
    public Page<JobBeanCfg> findByPage() {
        return super.findByPage();
    }

    @MetaData(value = "Trigger列表")
    @RequestMapping(value = "triggers", method = RequestMethod.GET)
    @ResponseBody
    public Page<Map<String, Object>> triggers() throws IllegalAccessException, SchedulerException {
        List<Map<String, Object>> triggerDatas = Lists.newArrayList();

        Map<Trigger, SchedulerFactoryBean> allTriggers = jobBeanCfgService.findAllTriggers();
        for (Map.Entry<Trigger, SchedulerFactoryBean> me : allTriggers.entrySet()) {
            Trigger trigger = me.getKey();
            ExtSchedulerFactoryBean schedulerFactoryBean = (ExtSchedulerFactoryBean) me.getValue();
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            Map<String, Object> triggerMap = Maps.newHashMap();
            triggerMap.put("id", trigger.getJobKey().getName());
            triggerMap.put("jobName", trigger.getJobKey().getName());
            if (trigger instanceof CronTrigger) {
                CronTrigger cronTrigger = (CronTrigger) trigger;
                triggerMap.put("cronExpression", cronTrigger.getCronExpression());
                triggerMap.put("previousFireTime", cronTrigger.getPreviousFireTime());
                triggerMap.put("nextFireTime", cronTrigger.getNextFireTime());
            }
            triggerMap.put("stateLabel", scheduler.getTriggerState(trigger.getKey()));
            triggerMap.put("runWithinCluster", schedulerFactoryBean.getRunWithinCluster());
            triggerDatas.add(triggerMap);
        }

        return buildPageResultFromList(triggerDatas);
    }

    @MetaData(value = "设置计划任务状态")
    @RequestMapping(value = "doStateTrigger", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doStateTrigger(@RequestParam("state") String state) throws SchedulerException {
        String[] ids = getParameterIds();
        Map<Trigger, SchedulerFactoryBean> allTriggers = jobBeanCfgService.findAllTriggers();
        for (String id : ids) {
            for (Map.Entry<Trigger, SchedulerFactoryBean> me : allTriggers.entrySet()) {
                Trigger trigger = me.getKey();
                if (trigger.getJobKey().getName().equals(id)) {
                    if (state.equals("pause")) {
                        me.getValue().getScheduler().pauseTrigger(trigger.getKey());
                    } else if (state.equals("resume")) {
                        me.getValue().getScheduler().resumeTrigger(trigger.getKey());
                    } else {
                        throw new UnsupportedOperationException("state parameter [" + state
                                + "] not in [pause, resume]");
                    }
                    break;
                }
            }
        }
        return OperationResult.buildSuccessResult("批量状态更新操作完成");
    }

    @MetaData(value = "立即执行计划任务")
    @RequestMapping(value = "doRunTrigger", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doRunTrigger() throws SchedulerException {
        String[] ids = getParameterIds();
        Map<Trigger, SchedulerFactoryBean> allTriggers = jobBeanCfgService.findAllTriggers();
        for (String id : ids) {
            for (Map.Entry<Trigger, SchedulerFactoryBean> me : allTriggers.entrySet()) {
                Trigger trigger = me.getKey();
                if (trigger.getJobKey().getName().equals(id)) {
                    me.getValue().getScheduler().triggerJob(trigger.getJobKey());
                    break;
                }
            }
        }
        return OperationResult.buildSuccessResult("立即执行计划任务作业操作完成");
    }
}
