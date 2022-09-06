package com.wkelms.ebilling.edsscheduler.service

import com.wkelms.ebilling.edsscheduler.repository.JobExecutionRepository
import org.quartz.*
import org.quartz.Trigger.TriggerState
import org.quartz.impl.JobDetailImpl
import org.quartz.impl.matchers.GroupMatcher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Lazy
import org.springframework.scheduling.quartz.QuartzJobBean
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.springframework.stereotype.Service
import com.wkelms.ebilling.edsscheduler.service.util.*
import com.wkelms.ebilling.edsscheduler.entity.JobExecution

import java.util.*

@Service
public class JobServiceImpl implements JobService{

	@Autowired
	@Lazy
    SchedulerFactoryBean schedulerFactoryBean

	@Autowired
	private ApplicationContext context

	@Autowired
	JobExecutionRepository jer

	private final static Logger logger = LoggerFactory.getLogger(JobServiceImpl.class)
	@Override
	public boolean scheduleCronJob(String jobName, Class<? extends QuartzJobBean> jobClass, Date date, String cronExpression, String customClass) {
		logger.debug("Request received to scheduleJob")

		String jobKey = jobName
		String groupKey = "ScheduleGroup"
		String triggerKey = jobName

		// set job data map
		JobDataMap jobDataMap = new JobDataMap()
		jobDataMap.put("className", customClass)
		jobDataMap.put("cronExpr", cronExpression)

		JobDetail jobDetail = JobUtil.createJob(jobClass, true, context, jobKey, groupKey, jobDataMap)
		logger.debug("creating trigger for key :"+jobKey + " at date :"+date)
		Trigger cronTriggerBean = JobUtil.createCronTrigger(triggerKey, date, cronExpression, SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW)

		try {
			Scheduler scheduler = schedulerFactoryBean.getScheduler()
			Date dt = scheduler.scheduleJob(jobDetail, cronTriggerBean)
			logger.info("Job with key jobKey :"+jobKey+ " and group :"+groupKey+ " scheduled successfully for date :"+dt)
			return true
		} catch (SchedulerException e) {
			logger.error("SchedulerException while scheduling job with key :"+jobKey + " message :"+e.getMessage())
			e.printStackTrace()
		}

		return false
	}

	@Override
	public boolean updateCronJob(String jobName, Date date, String cronExpression) {
		logger.debug("Request received for updating cron job.")

		String jobKey = jobName

		logger.debug("Parameters received for updating cron job : jobKey :"+jobKey + ", date: "+date)
		try {
			//Trigger newTrigger = JobUtil.createSingleTrigger(jobKey, date, SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT)
			Trigger newTrigger = JobUtil.createCronTrigger(jobKey, date, cronExpression, SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW)

			Date dt = schedulerFactoryBean.getScheduler().rescheduleJob(TriggerKey.triggerKey(jobKey), newTrigger)
			logger.info("Trigger associated with jobKey :"+jobKey+ " rescheduled successfully for date :"+dt)
			return true
		} catch ( Exception e ) {
			logger.error("SchedulerException while updating cron job with key :"+jobKey + " message :"+e.getMessage())
			e.printStackTrace()
			return false
		}
	}
	
	@Override
	public boolean unScheduleJob(String jobName) {
		logger.debug("Request received for Unscheduleding job.")

		String jobKey = jobName

		TriggerKey tkey = new TriggerKey(jobKey)
		logger.debug("Parameters received for unscheduling job : tkey :"+jobKey)
		try {
			boolean status = schedulerFactoryBean.getScheduler().unscheduleJob(tkey)
			logger.info("Trigger associated with jobKey :"+jobKey+ " unscheduled with status :"+status)
			return status
		} catch (SchedulerException e) {
			logger.error("SchedulerException while unscheduling job with key :"+jobKey + " message :"+e.getMessage())
			e.printStackTrace()
			return false
		}
	}

	@Override
	public boolean deleteJob(String jobName) {
		logger.debug("Request received for deleting job.")

		String jobKey = jobName
		String groupKey = "ScheduleGroup"

		JobKey jkey = new JobKey(jobKey, groupKey)
		logger.debug("Parameters received for deleting job : jobKey :"+jobKey)

		try {
			boolean status = schedulerFactoryBean.getScheduler().deleteJob(jkey)
			logger.info("Job with jobKey :"+jobKey+ " deleted with status :"+status)
			return status
		} catch (SchedulerException e) {
			logger.error("SchedulerException while deleting job with key :"+jobKey + " message :"+e.getMessage())
			e.printStackTrace()
			return false
		}
	}

	@Override
	public boolean pauseJob(String jobName) {
		logger.debug("Request received for pausing job.")

		String jobKey = jobName
		String groupKey = "ScheduleGroup"
		JobKey jkey = new JobKey(jobKey, groupKey)
		logger.debug("Parameters received for pausing job : jobKey :"+jobKey+ ", groupKey :"+groupKey)

		try {
			schedulerFactoryBean.getScheduler().pauseJob(jkey)
			logger.info("Job with jobKey :"+jobKey+ " paused succesfully.")
			return true
		} catch (SchedulerException e) {
			logger.error("SchedulerException while pausing job with key :"+jobName + " message :"+e.getMessage())
			e.printStackTrace()
			return false
		}
	}

	@Override
	public boolean resumeJob(String jobName) {
		logger.debug("Request received for resuming job.")

		String jobKey = jobName
		String groupKey = "ScheduleGroup"

		JobKey jKey = new JobKey(jobKey, groupKey)
		logger.debug("Parameters received for resuming job : jobKey :"+jobKey)
		try {
			schedulerFactoryBean.getScheduler().resumeJob(jKey)
			logger.info("Job with jobKey :"+jobKey+ " resumed succesfully.")
			return true
		} catch (SchedulerException e) {
			logger.error("SchedulerException while resuming job with key :"+jobKey+ " message :"+e.getMessage())
			e.printStackTrace()
			return false
		}
	}

	@Override
	public boolean startJobNow(String jobName) {
		logger.debug("Request received for starting job now.")

		String jobKey = jobName
		String groupKey = "ScheduleGroup"

		JobKey jKey = new JobKey(jobKey, groupKey)
		logger.debug("Parameters received for starting job now : jobKey :"+jobKey)
		try {
			schedulerFactoryBean.getScheduler().triggerJob(jKey)
			logger.info("Job with jobKey :"+jobKey+ " started now succesfully.")
			return true
		} catch (SchedulerException e) {
			logger.error("SchedulerException while starting job now with key :"+jobKey+ " message :"+e.getMessage())
			e.printStackTrace()
			return false
		}		
	}

	@Override
	public boolean isJobRunning(String jobName) {
		logger.debug("Request received to check if job is running")

		String jobKey = jobName
		String groupKey = "ScheduleGroup"

		logger.debug("Parameters received for checking job is running now : jobKey :"+jobKey)
		try {

			List<JobExecutionContext> currentJobs = schedulerFactoryBean.getScheduler().getCurrentlyExecutingJobs()
			if(currentJobs!=null){
				for (JobExecutionContext jobCtx : currentJobs) {
					String jobNameDB = jobCtx.getJobDetail().getKey().getName()
					String groupNameDB = jobCtx.getJobDetail().getKey().getGroup()
					if (jobKey.equalsIgnoreCase(jobNameDB) && groupKey.equalsIgnoreCase(groupNameDB)) {
						return true
					}
				}
			}
		} catch (SchedulerException e) {
			logger.error("SchedulerException while checking job with key :"+jobKey+ " is running. error message :"+e.getMessage())
			e.printStackTrace()
			return false
		}
		return false
	}

	@Override
	public List<Map<String, Object>> getAllJobs() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>()
		try {
			Scheduler scheduler = schedulerFactoryBean.getScheduler()

			for (String groupName : scheduler.getJobGroupNames()) {
				for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

					String jobName = jobKey.getName()
					String jobGroup = jobKey.getGroup()
					def jd = scheduler.getJobDetail(jobKey)

					//get job's trigger
					List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey)
					Date scheduleTime = triggers.get(0).getStartTime()
					Date nextFireTime = triggers.get(0).getNextFireTime()
					Date lastFiredTime = triggers.get(0).getPreviousFireTime()
					
					Map<String, Object> map = new HashMap<String, Object>()
					map.put("jobName", jobName)
//					map.put("groupName", jobGroup)
					map.put("className", jd?.jobDataMap?.get("className"))
					map.put("cronExpr", jd?.jobDataMap?.get("cronExpr"))
					map.put("scheduleTime", scheduleTime?.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ"))
					map.put("lastFiredTime", lastFiredTime?.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ"))
					map.put("nextFireTime", nextFireTime?.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ"))
					
					if(isJobRunning(jobName)){
						map.put("jobStatus", "RUNNING")
					}else{
						String jobState = getJobState(jobName)
						map.put("jobStatus", jobState)
					}

					/*					Date currentDate = new Date()
					if (scheduleTime.compareTo(currentDate) > 0) {
						map.put("jobStatus", "scheduled")

					} else if (scheduleTime.compareTo(currentDate) < 0) {
						map.put("jobStatus", "Running")

					} else if (scheduleTime.compareTo(currentDate) == 0) {
						map.put("jobStatus", "Running")
					}*/

					list.add(map)
					logger.debug("Job details:")
					logger.debug("Job Name:"+jobName + ", Group Name:"+ groupName + ", Schedule Time:"+scheduleTime)
				}

			}
		} catch (SchedulerException e) {
			logger.error("SchedulerException while fetching all jobs. error message :"+e.getMessage())
			e.printStackTrace()
		}
		return list
	}

	@Override
	public boolean isJobWithNamePresent(String jobName) {
		try {
			String groupKey = "ScheduleGroup"
			JobKey jobKey = new JobKey(jobName, groupKey)
			Scheduler scheduler = schedulerFactoryBean.getScheduler()
			if (scheduler.checkExists(jobKey)){
				return true
			}
		} catch (SchedulerException e) {
			logger.error("SchedulerException while checking job with name and group exist:"+e.getMessage())
			e.printStackTrace()
		}
		return false
	}

	public String getJobState(String jobName) {
		logger.debug("JobServiceImpl.getJobState()")

		try {
			String groupKey = "ScheduleGroup"
			JobKey jobKey = new JobKey(jobName, groupKey)

			Scheduler scheduler = schedulerFactoryBean.getScheduler()
			JobDetail jobDetail = scheduler.getJobDetail(jobKey)

			List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobDetail.getKey())
			if(triggers != null && triggers.size() > 0){
				for (Trigger trigger : triggers) {
					TriggerState triggerState = scheduler.getTriggerState(trigger.getKey())

					if (TriggerState.PAUSED.equals(triggerState)) {
						return "PAUSED"
					}else if (TriggerState.BLOCKED.equals(triggerState)) {
						return "BLOCKED"
					}else if (TriggerState.COMPLETE.equals(triggerState)) {
						return "COMPLETE"
					}else if (TriggerState.ERROR.equals(triggerState)) {
						return "ERROR"
					}else if (TriggerState.NONE.equals(triggerState)) {
						return "NONE"
					}else if (TriggerState.NORMAL.equals(triggerState)) {
						return "SCHEDULED"
					}
				}
			}
		} catch (SchedulerException e) {
			logger.error("SchedulerException while checking job with name and group exist:"+e.getMessage())
			e.printStackTrace()
		}
		return null
	}

	@Override
	public boolean stopJob(String jobName) {
		logger.debug("JobServiceImpl.stopJob()")
		try{	
			String jobKey = jobName
			String groupKey = "ScheduleGroup"

			Scheduler scheduler = schedulerFactoryBean.getScheduler()
			JobKey jkey = new JobKey(jobKey, groupKey)

			return scheduler.interrupt(jkey)

		} catch (SchedulerException e) {
			logger.error("SchedulerException while stopping job. error message :"+e.getMessage())
			e.printStackTrace()
		}
		return false
	}

	@Override
	public def createJobExecutionEntry(def context, def duration, def status, def message){
		def now = new Date()
		def jobName = context.getJobDetail().getKey().getGroup()+"-"+context.getJobDetail().getKey().getName()
		def oldJe = jer.findFirstByJobNameOrderByIdDesc(jobName)
		if (status != "Job Starting") duration = (now.getTime() /1000) - oldJe.dateTime
		JobExecution je = new JobExecution()
		je.jobName = jobName
		je.dateTime = new Date().getTime() / 1000
		je.duration = duration
		je.status = status
		je.message = message
		jer.save(je)
	}

	@Override
	public def getJobExecutionHistory(String jobName){
		jobName = "ScheduleGroup-"+jobName
		def tList = jer.findAllByJobName(jobName).collect{it.asMap()}
		tList.collect{it.dateTime =  new Date(((long)it.dateTime) * 1000 ).format("yyyy-MM-dd'T'HH:mm:ss.SSSZ")}
		return tList.each{it.remove("serialVersionUID")}.sort{it.id * -1}
	}
}

