package com.wkelms.ebilling.edsscheduler.util

import java.util.Map.Entry

class DbUtil {

    /**
     * Selects rows from a Sharedoc table that match the passed in conditions.
     * <br />
     * Example:<br />
     * The following is equivalent to: select * from UserInformation where (id = 'C33000000001')
     * <pre>
     * def c = [:]
     * c["UserCompany"] = [["id" : "C33000000001"]]
     * def rows = sd.select(c)
     * println rows["UserCompany"]
     * </pre>
     *
     * More complex example:<br />
     * The following is equivalent to: select * from UserInformation where (id = 'C33000000001' and status = 'REVO') OR (id = 'C00000000001')
     * <pre>
     * def c = [:]
     * c["UserCompany"] = [["id" : "C33000000001", "status" : "REVO"], ["id" : "C00000000001"]]
     * def rows = sd.select(c)
     * println rows["UserCompany"]
     * </pre>
     *
     * @param tableAndConditions Each key in the tableAndConditions Map should match the table name to query from.
     * The value associated with the key is an array of Maps where the key is the column name and the value is the expected value
     * to filter on. The column and value is always matched using the equality operator.
     * Multiple columns can be specified by setting more keys in the map which in turn become a logical conjunction.
     * Each array member becomes a logical disjunction with each other.
     * @return A Map where the key is a string for the table name that was passed in the tableAndConditions argument.
     * The value is an array of Maps of where the keys are strings for each column name and the value is the column value.
     */
    public static Map<String, Map<String, Object>[]> select(def conn, Map<String, Map<String, Object>[]> tableAndConditions) {
        def r = [:]
        for(Entry<String, Map<String, Object>[]> entry in tableAndConditions.entrySet()) {
            def table = entry.getKey();
            def values = []
            def sql = "select * from " + table + " where " + getWhereClause(entry.getValue(), values)

            r[table] = executeSelect(conn, sql, values)
        }
        return r
    }

    /**
     * Inserts rows into a Sharedoc table
     * <br />
     * Example:<br />
     *
     * <pre>
     * def c = [:]
     * c["UserCompany"] = ["id" : "C33000000001", "name" : "Test Company", "authCode" : "sucka"]
     * println sd.insert(c)
     * </pre>
     * @param tableAndValues Each key in the tableAndValues Map should match the table name to insert data into.
     * The value associated with the key should be an array of Maps where the keys are strings for each column
     * name and the value is the column value to insert.
     * @return A Map where the key is a string for the table name that was passed in the tableAndValues argument.
     * The value is the result of the insert statement.
     */
    public static Map<String, Object> insert(def conn, Map<String, Map<String, Object>[]> tableAndValues) {
        def r = [:]
        for(Entry<String, String> entry in tableAndValues.entrySet()) {
            def values = []
            def sql = "insert into " + entry.getKey() + " ("
            def p = ""
            for(Map.Entry<String, Object> v in entry.getValue()) {
                sql += v.getKey() + ","
                p += "?,"
                values << v.getValue()
            }
            sql = sql.substring(0, sql.length() - 1) + ") values (" + p.substring(0, p.length() - 1) + ")"


            r[entry.getKey()] = executeInsert(conn, sql, values)
        }
        return r
    }

    /**
     * Updates rows in a Sharedoc table
     * <br />
     * Example:<br />
     *
     * <pre>
     * def c = [:]
     * c["UserCompany"] = [[["id" : "C33000000001"]] : ["authCode" : "fella"]]
     * println sd.update(c)
     * </pre>
     * @param tableAndConditionsAndValues Each key in the tableAndConditionsAndValues Map should match the table name to update data for.
     * The value associated with the key is an array of Maps where the key is the conditions for the update query and the value is an array
     * of Maps where the keys are strings for each column name and the value is the column value to update. The conditions columns and values
     *  is always matched using the equality operator.
     * Multiple columns can be specified by setting more keys in the map which in turn become a logical conjunction.
     * Each array member becomes a logical disjunction with each other.
     * @return A Map where the key is a string for the table name that was passed in the tableAndConditionsAndValues argument.
     * The value is a Map where the key is the conditions passed in the tableAndConditionsAndValues and value is the result of the update statement.
     */
    public static  Map<String, Map<String, Object>> update(def conn, Map<String, Map<Map<String, Object>[], Map<String, Object>[]>> tableAndConditionsAndValues) {
        def r = [:]
        for(Entry<String, Map<Map<String, Object>[], Map<String, Object>[]>> entry in tableAndConditionsAndValues.entrySet()) {
            def table = entry.getKey()
            r[table] = [:]
            for(Entry<Map<String, Object>[], Map<String, Object>[]> conditionsAndValues in entry.getValue()) {
                def values = []
                def sql = "update " + table + " set "
                for(Entry<String, Object> v in conditionsAndValues.getValue()) {
                    sql += v.getKey() + " = ?,"
                    values << v.getValue()
                }
                def where = getWhereClause(conditionsAndValues.getKey(), values)
                sql = sql.substring(0, sql.length() - 1) + " where " + where
                r[table][where] = executeUpdate(conn, sql, values)
            }
        }
        return r
    }

    /**
     * Deletes rows from a Sharedoc table that match the passed in conditions.
     * <br />
     * Example:<br />
     *
     * <pre>
     * def c = [:]
     * c["UserCompany"] = [["id" : "C33000000001"]]
     * def rows = sd.delete(c)
     * println rows["UserCompany"]
     * </pre>
     *
     * @param tableAndConditions Each key in the tableAndConditions Map should match the table name to delete from.
     * The value associated with the key is an array of Maps where the key is the column name and the value is the expected value
     * to filter on. The column and value is always matched using the equality operator.
     * Multiple columns can be specified by setting more keys in the map which in turn become a logical conjunction.
     * Each array member becomes a logical disjunction with each other.
     * @return A Map where the key is a string for the table name that was passed in the tableAndConditions argument.
     * The value is the result of the delete statement.
     */
    public static Map<String, Map<String, Object>[]> delete(def conn, Map<String, Map<String, Object>[]> tableAndConditions) {
        def r = [:]
        for(Entry<String, Map<String, Object>[]> entry in tableAndConditions.entrySet()) {
            def table = entry.getKey();
            def values = []
            def sql = "delete from " + table + " where " + getWhereClause(entry.getValue(), values)

            r[table] = executeInsert(conn, sql, values)
        }
        return r
    }

    /**
     * Executes stored procedures
     *
     * @param sprocAndValues Each key in the sprocAndValues Map should match a valid sproc name to execute.
     * The value associated with the key should be a valid list of parameters for the sproc.
     * @return I think nothing
     */
    public static Map<String, Object[]> sproc(def conn, Map<String, Object[]> sprocAndValues) {
        def r = [:]
        conn.execute("SET NOCOUNT ON");
        for(Entry<String, Object[]> entry in sprocAndValues.entrySet()) {
            def sproc = entry.getKey();
            def rows = []
            conn.eachRow(sproc, entry.getValue()) {
                rows << it.toRowResult()
            }
            r[sproc] = rows;
        }
        conn.execute("SET NOCOUNT OFF");
        return r
    }

    /**
     * Executes stored procedures with expectation of result set
     *
     * @param sprocAndValues Each key in the sprocAndValues Map should match a valid sproc name to execute.
     * The value associated with the key should be a valid list of parameters for the sproc.
     * @return I think nothing
     */
    public static Map<String, Object[]> sprocNoResult(def conn, Map<String, Object[]> sprocAndValues) {
        def r = [:]
        for(Entry<String, Object[]> entry in sprocAndValues.entrySet()) {
            def sproc = entry.getKey();
            conn.call(sproc, entry.getValue(), {stuff -> r[sproc] = stuff})
        }
        return r
    }

    def static executeInsert(def conn, def sql, def params) {
       conn.executeInsert(sql, params)
    }

    def static executeUpdate(def conn, def sql, def params) {
        conn.executeUpdate(sql, params)
    }

    def static executeSelect(def conn, def sql, def params) {
        def rows = []
        conn.eachRow(sql, params) {
            rows << it.toRowResult()
        }
        return rows
    }
/*
    def static sqlEscape(String sqlString) {
        // will be null for is null and is not null comparison types
        if (sqlString != null) {
            // remove most special characters
            sqlString = sqlString.replaceAll("[^a-zA-Z0-9\\s_\\.%-/@']", "")

            // escape single quotes
            sqlString = escapeSql(sqlString)

            if (sqlString.length() > 255) {
                sqlString = sqlString.substring(0, 255)
            }
        }
        return sqlString
    }
*/
    def static getWhereClause(List<Map<String, Object>> conditions, List<Object> values) {
        def groups = []
        for(Map<String, Object> c in conditions) {
            def clauses = []
            for(Entry<String, Object> pair in c.entrySet()) {
                clauses << "${pair.getKey()} = ?"
                values << pair.getValue()
            }
            groups << "(" + clauses.join(" AND ") + ")"
        }
        return groups.join(" OR ")
    }

    static String getPropertyValue(String propertiesFileTitle, String propertyName) {
        ClassLoader classLoader = this.getClassLoader();
        InputStream galileoProperties = classLoader.getResourceAsStream(propertiesFileTitle);
        Properties importFileName = new Properties();
        try {
            importFileName.load(galileoProperties);
        } catch (IOException e) {
            throw new Exception(e)
        }
        return importFileName.getProperty(propertyName);
    }
}
