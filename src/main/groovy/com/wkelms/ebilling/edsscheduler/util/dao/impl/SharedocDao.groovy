package com.wkelms.ebilling.edsscheduler.util.dao.impl

import com.wkelms.ebilling.edsscheduler.dao.SqlDao
import com.wkelms.ebilling.edsscheduler.util.DbUtil
import groovy.sql.Sql
import org.springframework.stereotype.Component

@Component
public class SharedocDao extends SqlDao {

    Sql conn
    String propFile = "db.properties"

    Sql getConnection() {
        if (conn != null) return conn
        try {
            String driver = DbUtil.getPropertyValue(propFile, "sharedoc.driver")
            String url = DbUtil.getPropertyValue(propFile, "sharedoc.url")
            String username = DbUtil.getPropertyValue(propFile, "sharedoc.username")
            String password = DbUtil.getPropertyValue(propFile, "sharedoc.password")
            conn = Sql.newInstance(url, username, password, driver)
        }catch(Exception e){
            super.logger.error(e.message)
        }
        return conn
    }
}