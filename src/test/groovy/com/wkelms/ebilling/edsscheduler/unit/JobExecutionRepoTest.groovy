package com.wkelms.ebilling.edsscheduler.unit

import com.wkelms.ebilling.edsscheduler.entity.JobExecution
import com.wkelms.ebilling.edsscheduler.repository.JobExecutionRepository
import org.junit.Assert
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@DataJpaTest
class JobExecutionRepoTest {

    @Autowired
    private JobExecutionRepository jeRepository

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void test01FindAllByName() {
        createJobExecution("Job1", 1526315658, 0, "Job starting")
        createJobExecution("Job1", 1526315158, 100, "ERROR")
        createJobExecution("Job1", 1526317658, 0, "Job starting")
        createJobExecution("Job1", 1526317708, 50, "SUCCESS")

        def jobs = jeRepository.findAllByJobName("Job1")

        Assert.assertNotNull(jobs)
        Assert.assertEquals(4, jobs.size())
    }

    @Test
    void test02FindLastByName() {
        createJobExecution("Job1", 1526317658, 0, "Job starting")
        createJobExecution("Job1", 1526317708, 50, "SUCCESS")
        createJobExecution("Job2", 1526327658, 0, "Job starting")
        createJobExecution("Job2", 1526327708, 50, "SUCCESS")

        def job = jeRepository.findFirstByJobNameOrderByIdDesc("Job1")
        Assert.assertNotNull(job)
        Assert.assertEquals(1526317708, job.dateTime )

    }

    def createJobExecution(def name, def dTime, def duration, def status, def message=null){
        JobExecution j = new JobExecution()
        j.setJobName(name)
        j.setDateTime(dTime)
        j.setDuration(duration)
        j.setStatus(status)
        j.setMessage(message)
        entityManager.persist(j)
    }

}
