package com.wkelms.ebilling.edsscheduler.job

import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.slf4j.Logger

public interface IBaseJob {
    void execute(JobExecutionContext jobExecutionContext, Logger logger) throws JobExecutionException
}
