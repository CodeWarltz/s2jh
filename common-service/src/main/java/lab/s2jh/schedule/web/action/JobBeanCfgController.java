package lab.s2jh.schedule.web.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.core.web.BaseController;
import lab.s2jh.core.web.view.OperationResult;
import lab.s2jh.schedule.ExtSchedulerFactoryBean;
import lab.s2jh.schedule.entity.JobBeanCfg;
import lab.s2jh.schedule.service.JobBeanCfgService;

import org.apache.struts2.rest.HttpHeaders;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@MetaData(title = "定时任务配置管理")
public class JobBeanCfgController extends BaseController<JobBeanCfg, String> {

    @Autowired
    private JobBeanCfgService jobBeanCfgService;

    @Override
    protected BaseService<JobBeanCfg, String> getEntityService() {
        return jobBeanCfgService;
    }

    private static Map<Integer, String> triggerStateMap;
    static {
        triggerStateMap = new HashMap<Integer, String>();
        triggerStateMap.put(Trigger.STATE_NORMAL, "正常运行");
        triggerStateMap.put(Trigger.STATE_BLOCKED, "被阻断");
        triggerStateMap.put(Trigger.STATE_PAUSED, "已暂停");
        triggerStateMap.put(Trigger.STATE_COMPLETE, "已完成");
        triggerStateMap.put(Trigger.STATE_NONE, "不存在");
        triggerStateMap.put(Trigger.STATE_ERROR, "错误异常");
    }

    @Override
    protected void checkEntityAclPermission(JobBeanCfg entity) {
        // TODO Add acl check code logic
    }

    @Override
    @MetaData(title = "创建")
    public HttpHeaders doCreate() {
        return super.doCreate();
    }

    @Override
    @MetaData(title = "更新")
    public HttpHeaders doUpdate() {
        return super.doUpdate();
    }

    @Override
    @MetaData(title = "删除")
    public HttpHeaders doDelete() {
        return super.doDelete();
    }

    @Override
    @MetaData(title = "查询")
    public HttpHeaders findByPage() {
        return super.findByPage();
    }

    @MetaData(title = "Trigger列表")
    public HttpHeaders triggers() throws IllegalAccessException, SchedulerException {
        List<Map<String, Object>> triggerDatas = Lists.newArrayList();

        Map<Trigger, SchedulerFactoryBean> allTriggers = jobBeanCfgService.findAllTriggers();
        for (Map.Entry<Trigger, SchedulerFactoryBean> me : allTriggers.entrySet()) {
            Trigger trigger = me.getKey();
            ExtSchedulerFactoryBean schedulerFactoryBean=(ExtSchedulerFactoryBean)me.getValue();
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            Map<String, Object> triggerMap = Maps.newHashMap();
            triggerMap.put("id", trigger.getJobName());
            triggerMap.put("jobName", trigger.getJobName());
            if (trigger instanceof CronTrigger) {
                CronTrigger cronTrigger = (CronTrigger) trigger;
                triggerMap.put("cronExpression", cronTrigger.getCronExpression());
                triggerMap.put("previousFireTime", cronTrigger.getPreviousFireTime());
                triggerMap.put("nextFireTime", cronTrigger.getNextFireTime());
            }
            int triggerState = scheduler.getTriggerState(trigger.getName(), trigger.getGroup());
            triggerMap.put("stateLabel", triggerStateMap.get(Integer.valueOf(triggerState)));
            triggerMap.put("runWithinCluster", schedulerFactoryBean.getRunWithinCluster());
            triggerDatas.add(triggerMap);
        }
       
        setModel(buildPageResultFromList(triggerDatas));
        return buildDefaultHttpHeaders();
    }

    @MetaData(title = "设置计划任务状态")
    public HttpHeaders doStateTrigger() throws SchedulerException {
        Set<String> ids = getParameterIds();
        String state = this.getRequiredParameter("state");
        Map<Trigger, SchedulerFactoryBean> allTriggers = jobBeanCfgService.findAllTriggers();
        for (String id : ids) {
            for (Map.Entry<Trigger, SchedulerFactoryBean> me : allTriggers.entrySet()) {
                Trigger trigger = me.getKey();
                if (trigger.getJobName().equals(id)) {
                    if (state.equals("pause")) {
                        me.getValue().getScheduler().pauseTrigger(trigger.getName(), trigger.getGroup());
                    } else if (state.equals("resume")) {
                        me.getValue().getScheduler().resumeTrigger(trigger.getName(), trigger.getGroup());
                    } else {
                        throw new UnsupportedOperationException("state parameter [" + state
                                + "] not in [pause, resume]");
                    }
                    break;
                }
            }
        }
        setModel(OperationResult.buildSuccessResult("批量状态更新操作完成"));
        return buildDefaultHttpHeaders();
    }

    @MetaData(title = "立即执行计划任务")
    public HttpHeaders doRunTrigger() throws SchedulerException {
        Set<String> ids = getParameterIds();
        Map<Trigger, SchedulerFactoryBean> allTriggers = jobBeanCfgService.findAllTriggers();
        for (String id : ids) {
            for (Map.Entry<Trigger, SchedulerFactoryBean> me : allTriggers.entrySet()) {
                Trigger trigger = me.getKey();
                if (trigger.getJobName().equals(id)) {
                    me.getValue().getScheduler().triggerJob(trigger.getJobName(), trigger.getJobGroup());
                    break;
                }
            }
        }
        setModel(OperationResult.buildSuccessResult("立即执行计划任务作业操作完成"));
        return buildDefaultHttpHeaders();
    }
}