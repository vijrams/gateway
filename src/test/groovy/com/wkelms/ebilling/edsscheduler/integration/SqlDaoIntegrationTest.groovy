package com.wkelms.ebilling.edsscheduler.integration

import com.wkelms.ebilling.edsscheduler.EdsSchedulerApplication
import com.wkelms.ebilling.edsscheduler.util.dao.impl.SharedocDao
import org.junit.Assert
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.web.WebAppConfiguration

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EdsSchedulerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@WebAppConfiguration
@OverrideAutoConfiguration(enabled = true)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SqlDaoIntegrationTest {

    @Autowired
    SharedocDao sd

    @Test
    void test010Init() {
        sd.propFile = "db-test.properties"
        def sql = '''
                    IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[test]') AND type in (N'U'))
                        DROP TABLE [dbo].[test]
                    CREATE TABLE test (name varchar(MAX),age bigint,dob date,status varchar(MAX))
                '''
        sd.getConnection().execute(sql)
        Assert.assertTrue(true)
    }

    @Test
    void test020Insert() {
        def c = [["name": "Test Company", "age": 22, "dob": '2018-01-01', "status": "open"]
                 ,["name": "Test firm", "age": 41, "dob": '2011-01-01', "status": "open"]
                 ,["name": "Test case", "age": 01, "dob": '2003-05-06']]
        sd.insert("test", c)
        def all = sd.getConnection().rows("select * from test where name like 'Test%'")
        Assert.assertEquals(3, all.size())
    }

    @Test
    void test021InvalidInsert() {
        try{
            def c = [["name": "Test Company", "age": 22, "dob": '2018-01-01aa', "status": "open"]]
            sd.insert("test", c)
            Assert.assertTrue(false)
        } catch(Exception e){
            Assert.assertEquals("Conversion failed when converting date and/or time from character string.", e.message)
        }
    }

    @Test
    void test030UpdateAnd() {
        def cond = [["name='Test Company'", "age=22"]]
        def c = ["status" : "closed", "dob" : "2017-01-01"]
        sd.update("test",c,cond)
        sd.getConnection().rows("select * from test where name = 'Test Company'").each{
            Assert.assertEquals("closed", it.status)
            Assert.assertEquals("2017-01-01", it.dob.toString())
        }
    }

    @Test
    void test031UpdateAndOr() {
        def cond = [["name='Test Company'", "age=22"],["age=41"]]
        def c = ["status" : "closed", "dob" : "2017-02-02"]
        sd.update("test",c,cond)
        sd.getConnection().rows("select * from test where (name='Test Company' and age=22) or age=41").each{
            Assert.assertEquals("closed", it.status)
            Assert.assertEquals("2017-02-02", it.dob.toString())
        }
    }

    @Test
    void test032InvalidUpdate() {
        try{
            def cond = [["name='Test Company'"]]
            def c = ["status" : "closed", "dob" : "2017-02-02aa"]
            sd.update("test",c,cond)
            Assert.assertTrue(false)
        } catch(Exception e){
            Assert.assertEquals("Conversion failed when converting date and/or time from character string.", e.message)
        }
    }

    @Test
    void test040DeleteAnd() {
        def cond = [["name='Test Company'", "age=22"]]
        sd.delete("test",cond)
        def all = sd.getConnection().rows("select * from test where name like 'Test%'")
        Assert.assertEquals(2, all.size(), )
    }

    @Test
    void test041DeleteAndOr() {
        def cond = [["name='Test firm'", "age=41"],["age=1"]]
        sd.delete("test",cond)
        def all = sd.getConnection().rows("select * from test where name like 'Test%'")
        Assert.assertEquals(0, all.size(), )
    }

    @Test
    void test042InvalidDelete() {
        try {
            def cond = [["dob=2017-02-02aa"]]
            sd.delete("test", cond)
            def all = sd.getConnection().rows("select * from test where name like 'Test%'")
            Assert.assertTrue(false)
        }catch(Exception e){
            Assert.assertEquals("Incorrect syntax near 'aa'.", e.message)
        }
    }


}
