package com.wkelms.ebilling.edsscheduler.job

import com.wkelms.ebilling.edsscheduler.service.JobService
import org.quartz.InterruptableJob
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.UnableToInterruptJobException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.quartz.QuartzJobBean

public class CronJob extends QuartzJobBean implements InterruptableJob{
	
	private volatile boolean toStopFlag = true;

	@Autowired
	JobService jobService;
	
	@Override
	protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {

		System.out.println("======================================");
		System.out.println("======================================");
	}

	@Override
	public void interrupt() throws UnableToInterruptJobException {
		System.out.println("Stopping thread... ");
		toStopFlag = false;
	}

}