package com.wkelms.ebilling.edsscheduler.dao

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.Map.Entry
import groovy.sql.Sql
import org.springframework.stereotype.Component

@Component
class SqlDao {

    private final static Logger logger = LoggerFactory.getLogger(SqlDao.class)

    /**
     * Selects rows from a sql table that match the passed in conditions.
     * <br />
     * Example:<br />
     * The following is equivalent to: select * from UserInformation where (id = 'C33000000001')
     * <pre>
     * def c = [["id='C33000000001'"]]
     * def rows = sd.select("UserCompany",c)
     * println rows["UserCompany"]
     * </pre>
     *
     * More complex example:<br />
     * The following is equivalent to: select * from UserInformation where (id = 'C33000000001' and status = 'REVO') OR (createdate < '2018-05-01')
     * <pre>
     * def c = [["id='C33000000001'", "status='REVO'"], ["createdate<'2018-05-01'"]]
     * def rows = sd.select(UserCompany",c)
     * println rows["UserCompany"]
     * </pre>
     *
     * @param table is the table name where you want results from
     * @param conditions is an array of strings with sql conditions to filter on.
     * Multiple conditions can be specified by setting entries in the array which in turn become a logical conjunction.
     * Each array member becomes a logical disjunction with each other.
     * @return A Map where the key is a string for the table name that was passed in the tableAndConditions argument.
     * The value is an array of Maps of where the keys are strings for each column name and the value is the column value.
     */
    public Map<String, Map<String, Object>[]> select(String table, List<List<String>> conditions) {
        def r = [:]
        def sql = "select * from " + table + " where " + buildWhereClause(conditions)
        r[table] = executeSelect(sql)
        return r
    }

    /**
     * Inserts rows into a sql table
     * <br />
     * Example:<br />
     * insert single row
     * <pre>
     * def c = [["id" : "C33000000001", "name" : "Test Company", "authCode" : "sucka"]]
     * println sd.insert("UserCompany", c)
     * </pre>
     *  insert multiple row
     * <pre>
     * def c = [["id" : "C33000000001", "name" : "Test Company", "authCode" : "sucka"],["id" : "C33000000001", "name" : "Test Company", "authCode" : "sucka"]]
     * println sd.insert("UserCompany", c)
     * </pre>
     * @param table is the table name where you want results from
     * @param fieldAndValues is an Map where the keys are strings for each column
     * name and the value is the column value to insert.
     * @return A Map where the key is a string for the table name that was passed in the tableAndValues argument.
     * The value is the result of the insert statement.
     */
    public Map<String, Object> insert(String table, List<Map<String, Object>> fieldAndValues) {
        def r = [:]
        fieldAndValues.each { row->
            def values = []
            def p = ""
            def sql = "insert into " + table + " ("
            row.each{ k,v ->
                sql += k + ","
                p += "?,"
                values << v
            }
            sql = sql.substring(0, sql.length() - 1) + ") values (" + p.substring(0, p.length() - 1) + ")"
            r[table] = executeInsert(sql, values)
        }

        return r
    }

    /**
     * Updates rows in a sql table
     * <br />
     * Example:<br />
     *
     * <pre>
     * def cond = [["id='C33000000001'"]]
     * def c = ["id" : "C33000000001", "authCode" : "fella"]
     * println sd.update("UserCompany",c,cond)
     * </pre>
     *
     * @param table is the table name where you want results from
     * @param fieldAndValues is an Map where the keys are strings for each column
     * name and the value is the column value to insert.
     * @param conditions is an array of strings with sql conditions to filter on.
     * Multiple conditions can be specified by setting entries in the array which in turn become a logical conjunction.
     * Each array member becomes a logical disjunction with each other.
     * @return A Map where the key is a string for the table name that was passed in the tableAndValues argument.
     * The value is the result of the insert statement.
     */
    public Map<String, Map<String, Object>> update(String table, Map<String, Object> fieldAndValues,  List<List<String>> conditions) {
        def r = [:]
        def values = []
        def sql = "update " + table + " set "
        fieldAndValues.each { k,v ->
            sql += k + " = ?,"
            values << v
        }
        def where = buildWhereClause(conditions)
        sql = sql.substring(0, sql.length() - 1) + " where " + where
        r[table] = executeUpdate(sql, values)
        return r
    }

    /**
     * Deletes rows from a sql table that match the passed in conditions.
     * <br />
     * Example:<br />
     *
     * <pre>
     * def c = [["id='C33000000001'"]]
     * def rows = sd.delete("UserCompany",c)
     * println rows["UserCompany"]
     * </pre>
     *
     * @param table is the table name where you want results from
     * @param conditions is an array of strings with sql conditions to filter on.
     * Multiple conditions can be specified by setting entries in the array which in turn become a logical conjunction.
     * Each array member becomes a logical disjunction with each other.
     * @return A Map where the key is a string for the table name that was passed in the table argument.
     * The value is the result of the delete statement.
     */
    public Map<String, Map<String, Object>[]> delete(String table, List<List<String>> conditions) {
        def r = [:]
        def sql = "delete from " + table + " where " + buildWhereClause(conditions)
        r[table] = executeDelete(sql)
        return r
    }

    /**
     * Executes stored procedures
     *
     * @param sproc is the name of the sproc you want to execute
     * @param values is a valid list of parameters for the sproc.
     * @return response of the sproc
     */
    public Map<String, Object[]> sproc(String sproc, Object[] values) {
        def r = [:]
        def rows = []
        getConnection().execute("SET NOCOUNT ON")
        log(sproc, values)
        getConnection().eachRow(sproc, values) {
            rows << it.toRowResult()
        }
        r[sproc] = rows
        getConnection().execute("SET NOCOUNT OFF");
        return r
    }

    /**
     * Executes stored procedures with expectation of result set
     *
     * @param sproc is the name of the sproc you want to execute
     * @param values is a valid list of parameters for the sproc.
     * @return I think nothing
     */
    public Map<String, Object[]> sprocNoResult(String sproc, Object[] values) {
        def r = [:]
        log('sprocNoResult:'+sproc, values)
        getConnection().call(sproc, values, {stuff -> r[sproc] = stuff})
        return r
    }

    def executeInsert(sql, params) {
        log(sql, params)
        return getConnection().executeInsert(sql, params)
    }

    def executeDelete(sql) {
        log(sql, null)
        return getConnection().executeUpdate(sql)
    }

    def executeUpdate(sql, params) {
        log(sql, params)
        return getConnection().executeUpdate(sql, params)
    }

    def executeSelect(sql) {
        log(sql, null)
        def rows = []

        getConnection().eachRow(sql) {
            rows << it.toRowResult()
        }
        return rows
    }

    def buildWhereClause(List<List<String>> conditions){
        def groups = []
        conditions.each{ sub->
            groups << "(" + sub.join(" AND ") + ")"
        }
        return groups.join(" OR ")
    }

    def log(sql, params) {
        logger.debug("SQL: $sql")
        if (params) logger.debug("Params: $params")
    }

    Sql getConnection() {
        throw new Exception ("implement get connection method")
     }
}
