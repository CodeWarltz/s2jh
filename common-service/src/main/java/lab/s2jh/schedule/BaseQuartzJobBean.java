package lab.s2jh.schedule;

import lab.s2jh.core.exception.ServiceException;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * 自定义Quartz Job业务对象的基类定义
 * 业务Job继承此抽象基类，获得Spring ApplicationContext的能力从而可以获取Spring声明的Bean对象
 * 同时实现QuartzJobBean约定接口，编写定时处理逻辑
 */
public abstract class BaseQuartzJobBean extends QuartzJobBean {

    private static Logger logger = LoggerFactory.getLogger(BaseQuartzJobBean.class);

    protected ApplicationContext applicationContext;

    /**
     * 从SchedulerFactoryBean注入的applicationContext.
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        logger.debug("Set applicationContext for QuartzJobBean");
        this.applicationContext = applicationContext;
    }

    protected <X> X getSpringBean(Class<X> clazz) {
        return this.applicationContext.getBean(clazz);
    }

    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            logger.debug("Invoking executeInternalBiz for {}", this.getClass());
            executeInternalBiz(context);
            logger.debug("Job execution result: {}", context.getResult());
        } catch (Exception e) {
            logger.error("Quartz job execution error", e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    protected abstract void executeInternalBiz(JobExecutionContext context);
}
