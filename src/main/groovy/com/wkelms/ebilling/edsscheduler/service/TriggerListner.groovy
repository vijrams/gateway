package com.wkelms.ebilling.edsscheduler.service

import org.quartz.JobExecutionContext
import org.quartz.Trigger
import org.quartz.Trigger.CompletedExecutionInstruction
import org.quartz.TriggerListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
public class TriggerListner implements TriggerListener {

    private final static Logger logger = LoggerFactory.getLogger(TriggerListner.class)
    @Override
    public String getName() {
        return "globalTrigger"
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
    	logger.info("TriggerListner.triggerFired()")
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        logger.info("TriggerListner.vetoJobExecution()")
        return false
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        logger.info("TriggerListner.triggerMisfired()")
        String jobName = trigger.getJobKey().getName()
        logger.info("Job name: " + jobName + " is misfired")
        
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context, CompletedExecutionInstruction triggerInstructionCode) {
        logger.info("TriggerListner.triggerComplete()")
    }
}
