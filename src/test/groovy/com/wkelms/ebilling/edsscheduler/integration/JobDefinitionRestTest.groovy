package com.wkelms.ebilling.edsscheduler.integration

import com.wkelms.ebilling.edsscheduler.EdsSchedulerApplication
import com.wkelms.ebilling.edsscheduler.entity.JobDefinition
import com.wkelms.ebilling.edsscheduler.repository.JobDefinitionRepository
import com.wkelms.ebilling.edsscheduler.service.JobService
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Lazy
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import java.util.regex.Matcher
import java.util.regex.Pattern

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EdsSchedulerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@WebAppConfiguration
@OverrideAutoConfiguration(enabled = true)
class JobDefinitionRestTest {

    private MockMvc mockMvc

    @Autowired
    private WebApplicationContext webApplicationContext

    @Autowired
    private JobDefinitionRepository jdRepository


    @Autowired
    @Lazy
    JobService jobService

    @Before
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    @Test
    void testCreateJobDefinition() {
        String jobDetail = ''' { "name": "test Job", "packageName": "com.wkelms.ebilling.edsscheduler.job", "className": "testJob", "groovyClass": "" }'''

        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.post("/api/jobDefinitions/")
                .characterEncoding("UTF-8")
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content(jobDetail))
                .andExpect(status().isCreated())
                .andReturn()

        String locationHeader = r.getResponse().getHeader(HttpHeaders.LOCATION)
        Assert.assertNotNull(locationHeader)
        Pattern p = Pattern.compile("http:\\/\\/localhost\\/api\\/jobDefinitions\\/(\\d+)")
        Matcher m = p.matcher(locationHeader)
        def job = jdRepository.findByName('test Job')
        Assert.assertTrue(m.matches())
        Assert.assertTrue(m.hasGroup())
        Assert.assertTrue(m.groupCount() == 1)
        Assert.assertNotNull(job)
    }

    @Test
    void testGetJobDefinitionById() {
        JobDefinition j = new JobDefinition()
        j.setName("Base Job 1")
        j.setPackageName("com.wkelms.job")
        j.setClassName("BaseJob")
        j.setGroovyClass("")
        JobDefinition newJob = jdRepository.save(j)
        long clientId = newJob.getId()
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.get("/api/jobDefinitions/${clientId}")
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andReturn()

        byte[] content = r.getResponse().getContentAsByteArray()
        String json = new String(content, "UTF-8")
        Assert.assertTrue(json.contains("Base Job 1"))
        Assert.assertTrue(json.contains("com.wkelms.job"))
    }

    @Test
    void testEditJobDefinitionById() {
        String jobDetail = ''' { "name": "Base Job 2", "packageName": "com.wkelms.ebilling.edsscheduler.job", "className": "testJob", "groovyClass": "" }'''
        JobDefinition j = new JobDefinition()
        j.setName("Base Job 2")
        j.setPackageName("com.wkelms.job")
        j.setClassName("BaseJob2")
        j.setGroovyClass("")
        JobDefinition newJob = jdRepository.save(j)
        long clientId = newJob.getId()
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.put("/api/jobDefinitions/${clientId}")
                .characterEncoding("UTF-8")
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content(jobDetail))
                .andExpect(status().is2xxSuccessful())
                .andReturn()
        String locationHeader = r.getResponse().getHeader(HttpHeaders.LOCATION)
        Assert.assertNotNull(locationHeader)
        Pattern p = Pattern.compile("http:\\/\\/localhost\\/api\\/jobDefinitions\\/(\\d+)")
        Matcher m = p.matcher(locationHeader)
        Assert.assertTrue(m.matches())
        Assert.assertTrue(m.hasGroup())
        Assert.assertTrue(m.groupCount() == 1)
        JobDefinition job = jdRepository.findByName("Base Job 2")
        Assert.assertNotNull(job)
        Assert.assertEquals(job.getClassName(), "testJob")
    }

    @Test
    void testDeleteJobDefinitionById() {
        JobDefinition j = new JobDefinition()
        j.setName("Base Job 2")
        j.setPackageName("com.wkelms.job")
        j.setClassName("BaseJob2")
        j.setGroovyClass("")
        JobDefinition newJob = jdRepository.save(j)
        long clientId = newJob.getId()
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.delete("/api/jobDefinitions/${clientId}")
                .characterEncoding("UTF-8")
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andReturn()
        JobDefinition job = jdRepository.findByName("Base Job 2")
        Assert.assertNull(job)
    }
}
