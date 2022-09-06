package com.wkelms.ebilling.edsscheduler.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import com.wkelms.ebilling.edsscheduler.entity.JobExecution

@RepositoryRestResource(collectionResourceRel = "jobExecutions", path = "jobExecutions")
interface  JobExecutionRepository  extends CrudRepository<JobExecution, Long> {
    List<JobExecution> findAllByJobName(String jobName)
    JobExecution findFirstByJobNameOrderByIdDesc(String jobName)
}
