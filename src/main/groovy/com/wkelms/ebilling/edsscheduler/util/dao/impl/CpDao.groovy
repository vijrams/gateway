package com.wkelms.ebilling.edsscheduler.util.dao.impl

import com.wkelms.ebilling.edsscheduler.dao.SqlDao
import com.wkelms.ebilling.edsscheduler.util.DbUtil
import groovy.sql.Sql
import org.springframework.stereotype.Component

@Component
public class CpDao extends SqlDao {

    Sql conn
    String propFile = "db.properties"

    Sql getConnection() {
        if (conn != null) return conn
        try{
            String driver = DbUtil.getPropertyValue(propFile, "cp.driver")
            String url = DbUtil.getPropertyValue(propFile, "cp.url")
            String username = DbUtil.getPropertyValue(propFile, "cp.username")
            String password = DbUtil.getPropertyValue(propFile, "cp.password")
            conn = Sql.newInstance(url, username, password, driver)
        }catch(Exception e){
            logger.error(e.message)
        }
        conn
    }
}