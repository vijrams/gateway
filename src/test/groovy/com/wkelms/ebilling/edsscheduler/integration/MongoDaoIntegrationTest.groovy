package com.wkelms.ebilling.edsscheduler.integration

import com.mongodb.BasicDBObject
import com.mongodb.util.JSON
import com.wkelms.ebilling.edsscheduler.EdsSchedulerApplication
import com.wkelms.ebilling.edsscheduler.util.dao.impl.CoaMongoDao
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
class MongoDaoIntegrationTest {

    @Autowired
    CoaMongoDao md

    @Test
    void test010Init() {
        md.propFile = "db-test.properties"
        def db = md.getDatabase()
        if(db.collectionExists("test")) db.getCollection("test").drop()
        def col = db.createCollection("test",null)
        BasicDBObject obj = new BasicDBObject();
        obj.put("name", "test company");
        obj.put("age", 22);
        obj.put("dob", "2017-01-01");
        obj.put("status", "closed");
        col.insert(obj);
    }

    @Test
    void test020Insert(){
        def c = JSON.parse("[{'name':'test firm','age':41, 'dob':'2018-01-01', 'status':'open'},{'name':'test user','age':19, 'dob':'2001-01-01'}]")
        def rows = md.insert("test",c)
        Assert.assertTrue(rows["test"])
        def d = [['name':'test firm','age':41, 'dob':'2018-01-01', 'status':'open'],['name':'test user','age':29, 'dob':'2089-01-01']]
        def rows1 = md.insert("test",d)
        Assert.assertEquals(5, md.getDatabase().getCollection("test").find(JSON.parse("{}"))?.toArray().size())
    }

    @Test
    void test030Select(){
        def sStr = '{ $or : [   { $and : [  { "name" : "test firm" }, { "age" : 41 } ] } , { "age" : 19} ] }'
        def c = JSON.parse(sStr)
        def rows = md.select("test",c)
        Assert.assertEquals(3, rows["test"].size())
        def d = ['name':'test firm', 'age':41]
        rows = md.select("test",d)
        Assert.assertEquals(2, rows["test"].size())
    }

    @Test
    void test040Update(){
        def sStr = '{ $or : [   { $and : [  { "name" : "test firm" }, { "age" : 41 } ] } , { "age" : 19} ] }'
        def c = JSON.parse(sStr)
        def d = ["dob":"2089-01-01"]
        def rows = md.update("test",d,c)
        Assert.assertEquals(3, rows["test"])
        def res = md.getDatabase().getCollection("test").find(JSON.parse(sStr))?.toArray()?.dob
        Assert.assertEquals(0, res.findAll{!it.contains("2089")}.size())
        c = ["name":"test company", "age": 22]
        rows = md.update("test",d,c)
        Assert.assertEquals(1, rows["test"])
        res = md.getDatabase().getCollection("test").find(JSON.parse('{"name":"test company", "age": 22}'))?.toArray()?.dob
        Assert.assertEquals(0, res.findAll{!it.contains("2089")}.size())
    }

    @Test
    void test050Delete(){
        def sStr = '{ $or : [   { $and : [  { "name" : "test firm" }, { "age" : 41 } ] } , { "age" : 19} ] }'
        def c = JSON.parse(sStr)
        def rows = md.delete("test",c)
        Assert.assertEquals(3, rows["test"])
        Assert.assertEquals(2, md.getDatabase().getCollection("test").find(JSON.parse("{}"))?.toArray().size())
        c = ["dob":"2089-01-01"]
        rows = md.delete("test",c)
        Assert.assertEquals(2, rows["test"])
        Assert.assertEquals(0, md.getDatabase().getCollection("test").find(JSON.parse("{}"))?.toArray().size())
    }

}
