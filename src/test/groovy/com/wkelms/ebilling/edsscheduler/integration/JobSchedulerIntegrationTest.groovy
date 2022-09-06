package com.wkelms.ebilling.edsscheduler.integration

import com.wkelms.ebilling.edsscheduler.EdsSchedulerApplication
import com.wkelms.ebilling.edsscheduler.entity.JobDefinition
import com.wkelms.ebilling.edsscheduler.repository.JobDefinitionRepository
import com.wkelms.ebilling.edsscheduler.service.JobService
import org.junit.Assert
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Lazy
import org.springframework.http.HttpHeaders
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import groovy.json.JsonSlurper

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest(classes = EdsSchedulerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@WebAppConfiguration
class JobSchedulerIntegrationTest {

    private MockMvc mockMvc

    @Autowired
    private WebApplicationContext webApplicationContext

    @Autowired
    private JobDefinitionRepository jdRepository

    @Autowired
    @Lazy
    JobService jobService

    private String jobName = "Test Name"
    private String dupJobName = "Test Name 1"
    private String cronExpression = "0 * * ? * * *"
    private String pClassName = "com.wkelms.ebilling.edsscheduler.job.CronTestJob"
    private String gClassName = "Test Job"

    @Before
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    @Test
    void test010GetInfoPage() {
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.get("/info")
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        byte[] content = r.getResponse().getContentAsByteArray()
        String json = new String(content, "UTF-8")
        Assert.assertEquals('{}',json)
    }

    @Test
    void test020SchedulePersistentJob() {
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.post("/scheduler/schedule")
                .param("jobName", jobName)
                .param("jobScheduleTime", "2018-04-12T12:40:00.000-0500")
                .param("cronExpression", cronExpression)
                .param("className", pClassName)
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        byte[] content = r.getResponse().getContentAsByteArray()
        String json = new String(content, "UTF-8")
        Assert.assertEquals('{"statusCode":200,"data":true}',json)
        Assert.assertTrue(jobService.isJobWithNamePresent(jobName))
    }

    @Test
    void test021SchedulePersistentDuplicateJob() {
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.post("/scheduler/schedule")
                .param("jobName", jobName)
                .param("jobScheduleTime", "2018-04-12T12:40:00.000-0500")
                .param("cronExpression", cronExpression)
                .param("className", pClassName)
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        byte[] content = r.getResponse().getContentAsByteArray()
        String json = new String(content, "UTF-8")
        Assert.assertEquals('{"statusCode":501,"data":false}',json)
    }

    @Test
    void test022ScheduleJobInvalidCron() {
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.post("/scheduler/schedule")
                .param("jobName", dupJobName)
                .param("jobScheduleTime", "2018-04-12T12:40:00.000-0500")
                .param("cronExpression", "")
                .param("className", pClassName)
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        byte[] content = r.getResponse().getContentAsByteArray()
        String json = new String(content, "UTF-8")
        Assert.assertEquals('{"statusCode":503,"data":false}',json)
    }

    @Test
    void test023ScheduleJobInvalidClass() {
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.post("/scheduler/schedule")
                .param("jobName", dupJobName)
                .param("jobScheduleTime", "2018-04-12T12:40:00.000-0500")
                .param("cronExpression", cronExpression)
                .param("className", gClassName)
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        byte[] content = r.getResponse().getContentAsByteArray()
        String json = new String(content, "UTF-8")
        Assert.assertEquals('{"statusCode":504,"data":false}',json)
    }

    @Test
    void test030ScheduleGroovyJob(){
        JobDefinition j = new JobDefinition()
        j.setName("Test Job")
        j.setPackageName("com.wkelms.ebilling.edsscheduler.job")
        j.setClassName("TestJob")
        j.setGroovyClass(getClassDefinition(true))
        JobDefinition newJob = jdRepository.save(j)
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.post("/scheduler/schedule")
                .param("jobName", dupJobName)
                .param("jobScheduleTime", "2018-04-12T12:40:00.000-0500")
                .param("cronExpression", cronExpression)
                .param("className", gClassName)
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        byte[] content = r.getResponse().getContentAsByteArray()
        String json = new String(content, "UTF-8")
        Assert.assertEquals('{"statusCode":200,"data":true}',json)
        Assert.assertTrue(jobService.isJobWithNamePresent(dupJobName))
    }

    @Test
    void test031ScheduleGroovyJobInvalidClass(){
        JobDefinition j = new JobDefinition()
        j.setName("Test Job1")
        j.setPackageName("com.wkelms.ebilling.edsscheduler.job")
        j.setClassName("TestJob")
        j.setGroovyClass(getClassDefinition(false))
        JobDefinition newJob = jdRepository.save(j)
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.post("/scheduler/schedule")
                .param("jobName", dupJobName+"1")
                .param("jobScheduleTime", "2018-04-12T12:40:00.000-0500")
                .param("cronExpression", cronExpression)
                .param("className", "Test Job1")
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        byte[] content = r.getResponse().getContentAsByteArray()
        String json = new String(content, "UTF-8")
        Assert.assertEquals('{"statusCode":504,"data":false}',json)
    }

    @Test
    void test040UpdateExistingJob(){
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.post("/scheduler/update")
                .param("jobName", jobName)
                .param("jobScheduleTime", "2019-04-12T12:40:00.000-0500")
                .param("cronExpression", cronExpression)
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        def job = jobService.getAllJobs().find{it.jobName == jobName}
        Assert.assertEquals("2019-04-12T12:40:00.000-0500",job.scheduleTime)
    }

    @Test
    void test041UpdateNonExistingJob(){
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.post("/scheduler/update")
                .param("jobName", jobName+"123")
                .param("jobScheduleTime", "2019-04-12T12:40:00.000-0500")
                .param("cronExpression", cronExpression)
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        byte[] content = r.getResponse().getContentAsByteArray()
        String json = new String(content, "UTF-8")
        Assert.assertEquals('{"statusCode":500,"data":false}',json)
    }

    @Test
    void test041UpdateJobInvalidCron(){
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.post("/scheduler/update")
                .param("jobName", jobName)
                .param("jobScheduleTime", "2019-04-12T12:40:00.000-0500")
                .param("cronExpression", "")
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        byte[] content = r.getResponse().getContentAsByteArray()
        String json = new String(content, "UTF-8")
        Assert.assertEquals('{"statusCode":503,"data":false}',json)
    }

    @Test
    void test050UnscheduleExistingJob(){
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.get("/scheduler/unschedule")
                .param("jobName", jobName)
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        byte[] content = r.getResponse().getContentAsByteArray()
        String json = new String(content, "UTF-8")
        Assert.assertEquals('{"statusCode":200,"data":true}',json)
    }

    @Test
    void test051UnscheduleNonexistingJob(){
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.get("/scheduler/unschedule")
                .param("jobName", jobName+"1")
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        byte[] content = r.getResponse().getContentAsByteArray()
        String json = new String(content, "UTF-8")
        Assert.assertEquals('{"statusCode":500,"data":false}',json)
    }

    @Test
    void test060DeleteExistingJob(){
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.get("/scheduler/delete")
                .param("jobName", jobName)
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        byte[] content = r.getResponse().getContentAsByteArray()
        String json = new String(content, "UTF-8")
        Assert.assertEquals('{"statusCode":200,"data":true}',json)
    }

    @Test
    void test061DeleteNonExistingJob(){
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.get("/scheduler/delete")
                .param("jobName", jobName+"1")
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        byte[] content = r.getResponse().getContentAsByteArray()
        String json = new String(content, "UTF-8")
        Assert.assertEquals('{"statusCode":500,"data":false}',json)
    }

    @Test
    void test062DeleteRuningJob(){
        jobService.startJobNow(dupJobName)
        sleep(500)
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.get("/scheduler/delete")
                .param("jobName", dupJobName)
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        byte[] content = r.getResponse().getContentAsByteArray()
        String json = new String(content, "UTF-8")
        Assert.assertEquals('{"statusCode":510,"data":false}',json)
        sleep(2000)
    }

    @Test
    void test070PauseExistingJob(){
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.get("/scheduler/pause")
                .param("jobName", dupJobName)
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        byte[] content = r.getResponse().getContentAsByteArray()
        String json = new String(content, "UTF-8")
        Assert.assertEquals(json, '{"statusCode":200,"data":true}')
        Assert.assertEquals('PAUSED', jobService.getJobState(dupJobName))

    }

    @Test
    void test071PauseNonExistingJob(){
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.get("/scheduler/pause")
                .param("jobName", dupJobName+"1")
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        byte[] content = r.getResponse().getContentAsByteArray()
        String json = new String(content, "UTF-8")
        Assert.assertEquals('{"statusCode":500,"data":false}',json)
    }

    @Test
    void test072PauseRunningJob(){
        jobService.startJobNow(dupJobName)
        sleep(500)
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.get("/scheduler/pause")
                .param("jobName", dupJobName)
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        byte[] content = r.getResponse().getContentAsByteArray()
        String json = new String(content, "UTF-8")
        Assert.assertEquals('{"statusCode":510,"data":false}',json)
        sleep(2000)
    }

    @Test
    void test080ResumeExistingJob(){
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.get("/scheduler/resume")
                .param("jobName", dupJobName)
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        byte[] content = r.getResponse().getContentAsByteArray()
        String json = new String(content, "UTF-8")
        Assert.assertEquals('{"statusCode":200,"data":true}',json)
    }

    @Test
    void test081ResumeNonExistingJob(){
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.get("/scheduler/resume")
                .param("jobName", dupJobName+"1")
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        byte[] content = r.getResponse().getContentAsByteArray()
        String json = new String(content, "UTF-8")
        Assert.assertEquals('{"statusCode":500,"data":false}',json)
    }

    @Test
    void test082ResumeResumedJob(){
        jobService.startJobNow(dupJobName)
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.get("/scheduler/resume")
                .param("jobName", dupJobName)
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        byte[] content = r.getResponse().getContentAsByteArray()
        String json = new String(content, "UTF-8")
        Assert.assertEquals('{"statusCode":520,"data":false}',json)
    }

    @Test
    void test090StopExistingJob(){
        jobService.startJobNow(dupJobName)
        sleep(500)
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.get("/scheduler/stop")
                .param("jobName", dupJobName)
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        byte[] content = r.getResponse().getContentAsByteArray()
        String json = new String(content, "UTF-8")
        Assert.assertEquals('{"statusCode":200,"data":true}',json)
        sleep(2000)
    }

    @Test
    void test091StopNonExistingJob(){
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.get("/scheduler/stop")
                .param("jobName", dupJobName+"1")
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        byte[] content = r.getResponse().getContentAsByteArray()
        String json = new String(content, "UTF-8")
        Assert.assertEquals('{"statusCode":500,"data":false}',json)
    }

    @Test
    void test092StopStoppedJob(){
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.get("/scheduler/stop")
                .param("jobName", dupJobName)
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        byte[] content = r.getResponse().getContentAsByteArray()
        String json = new String(content, "UTF-8")
        Assert.assertEquals('{"statusCode":521,"data":false}',json)
    }

    @Test
    void test100StartExistingJob(){
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.get("/scheduler/start")
                .param("jobName", dupJobName)
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        byte[] content = r.getResponse().getContentAsByteArray()
        String json = new String(content, "UTF-8")
        Assert.assertEquals('{"statusCode":200,"data":true}',json)
    }

    @Test
    void test101StartNonExistingJob(){
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.get("/scheduler/start")
                .param("jobName", dupJobName+"1")
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        byte[] content = r.getResponse().getContentAsByteArray()
        String json = new String(content, "UTF-8")
        Assert.assertEquals('{"statusCode":500,"data":false}',json)
    }

    @Test
    void test102StartStartedJob(){
        jobService.startJobNow(dupJobName)
        sleep(500)
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.get("/scheduler/start")
                .param("jobName", dupJobName)
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        byte[] content = r.getResponse().getContentAsByteArray()
        String json = new String(content, "UTF-8")
        Assert.assertEquals('{"statusCode":510,"data":false}',json)
    }

    @Test
    void test110ViewJobHistory(){
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.get("/scheduler/jobs/history/$dupJobName")
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        byte[] content = r.getResponse().getContentAsByteArray()
        def json = (new JsonSlurper()).parseText(new String(content, "UTF-8"))
        Assert.assertEquals(12, json.data.size())
        Assert.assertEquals(12, json.data.first().id)
        Assert.assertEquals(1, json.data.last().id)
    }

    @Test
    void test111ViewJobLimitedHistory(){
        MvcResult r = mockMvc.perform(MockMvcRequestBuilders.get("/scheduler/jobs/history/$dupJobName?count=5")
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk()).andReturn()
        byte[] content = r.getResponse().getContentAsByteArray()
        def json = (new JsonSlurper()).parseText(new String(content, "UTF-8"))
        Assert.assertEquals(5, json.data.size())
        Assert.assertEquals(12, json.data.first().id)
        Assert.assertEquals(8, json.data.last().id)
    }

    public getClassDefinition(boolean isJob){
        def gClass ='''
        package com.wkelms.ebilling.edsscheduler.job

        import com.wkelms.ebilling.edsscheduler.service.util.BeanUtil
        import org.quartz.JobExecutionContext
        import org.quartz.JobExecutionException
        import org.slf4j.Logger
        import org.springframework.beans.factory.annotation.Autowired
        
        class TestJob extends BaseJob {
        
            @Autowired
            BeanUtil bUtil
        
            @Override
            void execute(JobExecutionContext jobExecutionContext, Logger logger) throws JobExecutionException {
                System.out.println(" executing Bestjob now");
                sleep(2000)
               // System.out.println(bUtil.showMessage("best"));
                
            }
        }
        '''
        if(!isJob) gClass = gClass.replace('extends BaseJob','').replace('@Override','')
        return  gClass
    }


}
