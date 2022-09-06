package com.wkelms.ebilling.edsscheduler.repository

import com.wkelms.ebilling.edsscheduler.entity.JobDefinition
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(collectionResourceRel = "jobDefinitions", path = "jobDefinitions")
interface JobDefinitionRepository extends CrudRepository<JobDefinition, Long> {
    JobDefinition findByName(String name)
    List<JobDefinition> findAll()
}
