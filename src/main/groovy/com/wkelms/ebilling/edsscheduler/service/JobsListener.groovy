package com.wkelms.ebilling.edsscheduler.service

import com.wkelms.ebilling.edsscheduler.entity.JobExecution
import com.wkelms.ebilling.edsscheduler.repository.JobExecutionRepository
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.JobListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
public class JobsListener implements JobListener{

	@Autowired
	JobService jobService

	private final static Logger logger = LoggerFactory.getLogger(JobsListener.class)
	@Override
	public String getName() {
		return "globalJob"
	}

	@Override
	public void jobToBeExecuted(JobExecutionContext context) {
		def je = jobService.createJobExecutionEntry(context, 0,"Job Starting", null)
		logger.info("JobsListener.jobToBeExecuted() " + je.jobName)
	}

	@Override
	public void jobExecutionVetoed(JobExecutionContext context) {
		logger.info("JobsListener.jobExecutionVetoed()")
	}

	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		def status = (jobException?.message)?"ERROR":"SUCCESS"
		def message = jobException?.message
		def je = jobService.createJobExecutionEntry(context, 0,status,message)
		logger.info("JobsListener.jobWasExecuted()" + je.jobName)
	}

}
