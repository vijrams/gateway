package com.wkelms.ebilling.edsscheduler.controller

import com.wkelms.ebilling.edsscheduler.dto.ServerResponse
import com.wkelms.ebilling.edsscheduler.service.util.BeanUtil
import com.wkelms.ebilling.edsscheduler.service.JobService
import com.wkelms.ebilling.edsscheduler.service.util.ServerResponseCode
import org.quartz.CronExpression
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/scheduler/")
class JobController {

    @Autowired
    @Lazy
    JobService jobService

    @Autowired
    BeanUtil bU

    private final static Logger logger = LoggerFactory.getLogger(JobController.class)

    @RequestMapping(value = "/jobs", method = RequestMethod.GET, produces = "application/json")
    public ServerResponse getAllJobs(){
        List<Map<String, Object>> list = jobService.getAllJobs().sort{it.jobName}
        return getServerResponse(ServerResponseCode.SUCCESS, list)
    }

    @RequestMapping(value = "/jobs/{jobName}", method = RequestMethod.GET, produces = "application/json")
    public ServerResponse getJob(@PathVariable String jobName){
        def job = jobService.getAllJobs().find{it.jobName == jobName}
        return getServerResponse(ServerResponseCode.SUCCESS, job)
    }

    @RequestMapping(value = "/jobs/history/{jobName}", method = RequestMethod.GET, produces = "application/json")
    public ServerResponse getJobExecution(@PathVariable String jobName,
                                          @RequestParam(value="count", required=false) Integer count){
        logger.info("${jobName} - /jobs/history")
        def history = jobService.getJobExecutionHistory(jobName)
        if(count) history = history.take(count.toInteger())
        return getServerResponse(ServerResponseCode.SUCCESS, history)
    }

    @RequestMapping(value = "/schedule", method = RequestMethod.POST, produces = "application/json")
    public ServerResponse schedule(@RequestParam(value="jobName", required = true) String jobName,
                                   @RequestParam(value="className", required = true) String className,
                                   @RequestParam(value="jobScheduleTime", required = true) @DateTimeFormat(iso= DateTimeFormat.ISO.DATE_TIME) Date jobScheduleTime,
                                   @RequestParam(value="cronExpression", required = true) String cronExpression){
        if(!CronExpression.isValidExpression(cronExpression))
            return getServerResponse(ServerResponseCode.CRON_EXPRESSION_INVALID, false)

        if(jobService.isJobWithNamePresent(jobName))
            return getServerResponse(ServerResponseCode.JOB_WITH_SAME_NAME_EXIST, false)
        try {
            def c = bU.getGroovyClass(className)
            boolean status = jobService.scheduleCronJob(jobName, c, jobScheduleTime, cronExpression, className)
            return getResponse(status)
        }catch(Exception ex){
            if (logger.isErrorEnabled()) {
                logger.error("$jobName - /schedule " + ex.message)
            }
            return getServerResponse(ServerResponseCode.CRON_CLASS_INVALID, false)
        }
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST, produces = "application/json")
    public ServerResponse updateJob(@RequestParam("jobName") String jobName,
                                    @RequestParam("jobScheduleTime") @DateTimeFormat(iso= DateTimeFormat.ISO.DATE_TIME) Date jobScheduleTime,
                                    @RequestParam("cronExpression") String cronExpression){
        logger.info("${jobName} - /update")
        if(jobName == null || jobName.trim()==""){
            return getServerResponse(ServerResponseCode.JOB_NAME_NOT_PRESENT, false)
        }

        if(jobService.isJobWithNamePresent(jobName)){
            if(!CronExpression.isValidExpression(cronExpression))
                return getServerResponse(ServerResponseCode.CRON_EXPRESSION_INVALID, false)
            boolean status = jobService.updateCronJob(jobName, jobScheduleTime, cronExpression)
            return getResponse(status)
        }else{
            return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false)
        }
    }

    @RequestMapping(value = "/unschedule", method = RequestMethod.GET, produces = "application/json")
    public ServerResponse unschedule(@RequestParam("jobName") String jobName) {
        logger.info("${jobName} - /unschedule")
        if(jobService.isJobWithNamePresent(jobName)){
            boolean status = jobService.unScheduleJob(jobName)
            return getResponse(status)
        }else{
            return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false)
        }
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET, produces = "application/json")
    public ServerResponse delete(@RequestParam("jobName") String jobName) {
        logger.info("${jobName} - /delete")
        if(jobService.isJobWithNamePresent(jobName)){
            boolean isJobRunning = jobService.isJobRunning(jobName)
            if(!isJobRunning){
                boolean status = jobService.deleteJob(jobName)
                return getResponse(status)
            }else{
                return getServerResponse(ServerResponseCode.JOB_ALREADY_IN_RUNNING_STATE, false)
            }
        }else{
            return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false)
        }
    }

    @RequestMapping(value = "/pause", method = RequestMethod.GET, produces = "application/json")
    public ServerResponse pause(@RequestParam("jobName") String jobName) {
        logger.info("${jobName} - /pause")
        if(jobService.isJobWithNamePresent(jobName)){
            boolean isJobRunning = jobService.isJobRunning(jobName)
            if(!isJobRunning){
                boolean status = jobService.pauseJob(jobName)
                return getResponse(status)
            }else{
                return getServerResponse(ServerResponseCode.JOB_ALREADY_IN_RUNNING_STATE, false)
            }
        }else{
            return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false)
        }
    }

    @RequestMapping(value = "/resume", method = RequestMethod.GET, produces = "application/json")
    public ServerResponse resume(@RequestParam("jobName") String jobName) {
        logger.info("${jobName} - /resume")
        if(jobService.isJobWithNamePresent(jobName)){
            String jobState = jobService.getJobState(jobName)
            if(jobState=="PAUSED"){
                logger.info("${jobName} - Job current state is PAUSED, Resuming job...")
                boolean status = jobService.resumeJob(jobName)
                return getResponse(status)
            }else{
                return getServerResponse(ServerResponseCode.JOB_NOT_IN_PAUSED_STATE, false)
            }
        }else{
            return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false)
        }
    }

    @RequestMapping(value = "/stop", method = RequestMethod.GET, produces = "application/json")
    public ServerResponse stopJob(@RequestParam("jobName") String jobName) {
        logger.info("${jobName} - /stop")
        if(jobService.isJobWithNamePresent(jobName)){
            if(jobService.isJobRunning(jobName)){
                boolean status = jobService.stopJob(jobName)
                return getResponse(status)
            }else{
                return getServerResponse(ServerResponseCode.JOB_NOT_IN_RUNNING_STATE, false)
            }
        }else{
            return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false)
        }
    }

    @RequestMapping(value = "/start", method = RequestMethod.GET, produces = "application/json")
    public ServerResponse startJobNow(@RequestParam("jobName") String jobName) {
        logger.info("${jobName} - /start")
        if(jobService.isJobWithNamePresent(jobName)){
            if(!jobService.isJobRunning(jobName)){
                boolean status = jobService.startJobNow(jobName)
                return getResponse(status)
            }else{
                return getServerResponse(ServerResponseCode.JOB_ALREADY_IN_RUNNING_STATE, false)
            }
        }else{
            return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false)
        }
    }

    public def getResponse(def status){
        if(status){
            return getServerResponse(ServerResponseCode.SUCCESS, true)
        }else{
            return getServerResponse(ServerResponseCode.ERROR, false)
        }
    }

    public ServerResponse getServerResponse(int responseCode, Object data){
        ServerResponse serverResponse = new ServerResponse()
        serverResponse.setStatusCode(responseCode)
        serverResponse.setData(data)
        return serverResponse
    }

}
