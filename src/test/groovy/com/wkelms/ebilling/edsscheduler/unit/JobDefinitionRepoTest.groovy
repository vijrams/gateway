package com.wkelms.ebilling.edsscheduler.unit

import com.wkelms.ebilling.edsscheduler.entity.JobDefinition
import com.wkelms.ebilling.edsscheduler.repository.JobDefinitionRepository
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
class JobDefinitionRepoTest {

    @Autowired
    private JobDefinitionRepository jdRepository

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void test01FindByName() {
        JobDefinition j = new JobDefinition()
        j.setName("Base Job")
        j.setPackageName("com.wkelms.job")
        j.setClassName("BaseJob")
        j.setGroovyClass("")
        entityManager.persist(j)

        JobDefinition job = jdRepository.findByName("Base Job")

        Assert.assertNotNull(job)
        Assert.assertNotNull(job.getName())
        Assert.assertEquals(job.getName(), j.getName())
        Assert.assertEquals(job.getClassName(), j.getClassName())
    }

    @Test
    void test02FindAll() {
        JobDefinition j = new JobDefinition()
        j.setName("Base Job1")
        j.setPackageName("com.wkelms.job")
        j.setClassName("BaseJob")
        j.setGroovyClass("")
        entityManager.persist(j)

        def job = jdRepository.findAll()

        Assert.assertNotNull(job)
        Assert.assertEquals(job.size(),1)
        Assert.assertEquals(job.name.toString(), "[Base Job1]")
    }


}
