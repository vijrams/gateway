package com.wkelms.ebilling.edsscheduler.util.dao.impl

import com.wkelms.ebilling.edsscheduler.util.DbUtil
import com.wkelms.ebilling.edsscheduler.util.dao.MongoDao
import com.mongodb.DB
import com.mongodb.ServerAddress
import com.mongodb.MongoClient
import com.mongodb.MongoCredential
import org.springframework.stereotype.Component

@Component
class CoaMongoDao extends MongoDao{
    DB db
    String propFile = "db.properties"

    public DB getDatabase(){
        if (db != null) return db
        try {
            String host = DbUtil.getPropertyValue(propFile, "coa.hostName")
            int port = Integer.parseInt(DbUtil.getPropertyValue(propFile, "coa.port"))
            String username = DbUtil.getPropertyValue(propFile, "coa.username")
            String password = DbUtil.getPropertyValue(propFile, "coa.password")
            String dbname = DbUtil.getPropertyValue(propFile, "coa.databasename")
            ServerAddress sa = new ServerAddress(host, port)
            MongoCredential cred = MongoCredential.createScramSha1Credential(username, dbname, password?.toCharArray())
            MongoClient mongoClient = new MongoClient(sa, Arrays.asList(cred))
            db = mongoClient.getDB(dbname)
        }catch(Exception e){
            logger.error(e.message)
        }
        return db
    }

}
