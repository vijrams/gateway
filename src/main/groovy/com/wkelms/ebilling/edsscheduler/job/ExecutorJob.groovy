package com.wkelms.ebilling.edsscheduler.job

import com.wkelms.ebilling.edsscheduler.service.util.BeanUtil
import com.wkelms.ebilling.edsscheduler.service.JobService
import org.quartz.InterruptableJob
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.JobKey
import org.quartz.UnableToInterruptJobException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.quartz.QuartzJobBean

class ExecutorJob extends QuartzJobBean implements InterruptableJob{
    private volatile boolean toStopFlag = true

    @Autowired
    JobService jobService

    @Autowired
    BeanUtil bUtil

    private final static Logger logger = LoggerFactory.getLogger(ExecutorJob.class)

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap dataMap = jobExecutionContext.getMergedJobDataMap()
        String jobName = jobExecutionContext.jobDetail.name
        String cClass = dataMap.getString("className")
        MDC.put('logFileName', jobName);
        logger.info("Starting Job execution - $jobName")
        def jobInstance = bUtil.getJobInstance(cClass)
        jobInstance."execute"(jobExecutionContext, logger)
        logger.info("Ending Job execution - $jobName")
        MDC.remove('logFileName');
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        logger.error ("Stopping thread... ")
        toStopFlag = false
    }
}
