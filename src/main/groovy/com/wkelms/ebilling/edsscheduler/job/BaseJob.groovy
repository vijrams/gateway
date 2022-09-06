package com.wkelms.ebilling.edsscheduler.job

import com.wkelms.ebilling.edsscheduler.service.JobService
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired

public abstract class BaseJob implements IBaseJob {

    @Autowired
    JobService jobService

    private volatile boolean toStopFlag = true;
}
