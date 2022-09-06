package com.wkelms.ebilling.edsscheduler.util.dao

import com.mongodb.*
import com.mongodb.util.JSON
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class MongoDao {

    private final static Logger logger = LoggerFactory.getLogger(MongoDao.class)

    /***
     * Selects rows from a mongo collection that match the passed in query
     * <br />
     * Example:<br />
     * The following is equivalent to: select * from collection where (id = 'C33000000001')
     * <pre>
     * def c = JSON.parse("{'id':'C33000000001'}")
     * def rows = md.select("collectionName",c)
     * println rows["collectionName"]
     *
     * More complex example:<br />
     * The following is equivalent to: select * from collectionName where (id = 'C33000000001' and status = 'REVO') OR (id = 'C00000000001')
     * <pre>
     * def c = JSON.parse("{ $or : [   { $and : [  { id : 'C33000000001' }, { status : 'REVO' } ] } , { id : 'C00000000001' } ] }")
     * def rows = md.select("collectionName",c)
     * println rows["collectionName"]
     * </pre>
     * @param collection is the collection name to query from
     * @param conditions is a db query object that has a mongo query conditions
     * @returnA Map where the key is a string for the table name that was passed in the table argument.
     * The value is an array of Maps of where the keys are strings for each column name and the value is the column value.
     */
    public Map<String, Map<String, Object>[]> select(String collection, DBObject conditions){
        def r = [:]
        log(collection +" - select", conditions.toString())
        DBCollection col = getDatabase()?.getCollection(collection)
        r[collection] = col?.find(conditions)?.toArray()
        return r
    }

    /**
     * select for simple queries from a mongo collection that only uses the equality operator
     * <br />
     * Example:<br />
     * The following is equivalent to: select * from collection where (id = 'C33000000001')
     * <pre>
     * def c = ["id" : "C33000000001"]
     * def rows = md.select("collectionName",c)
     * println rows["collectionName"]
     * </pre>
     *
     * @param collection is the collection name to query from
     * @param conditions is a Maps where the key is the column name and the value is the expected value
     * to filter on. The column and value is always matched using the equality operator.
     * Multiple columns can be specified by setting more keys in the map which in turn become a logical conjunction.
     * Each array member becomes a logical disjunction with each other.
     * @return A Map where the key is a string for the table name that was passed in the table argument.
     * The value is an array of Maps of where the keys are strings for each column name and the value is the column value.
     */
    public Map<String, Map<String, Object>[]> select(String collection, Map<String, Object> conditions){
        return select(collection, buildQuery(conditions))
    }

     /**
     * Deletes rows from mongo collection that match the condition
     * <br />
     * Example:<br />
     * The following is equivalent to: delete from collection where (id = 'C33000000001')
     * <pre>
     * def c = JSON.parse("{'id':'C33000000001'}")
     * def rows = md.delete("collectionName",c)
     * println rows["collectionName"]
     *
     * More complex example:<br />
     * The following is equivalent to: delete from collectionName where (id = 'C33000000001' and status = 'REVO') OR (id = 'C00000000001')
     * <pre>
     * def c = JSON.parse("{ $or : [   { $and : [  { id : 'C33000000001' }, { status : 'REVO' } ] } , { id : 'C00000000001' } ] }")
     * def rows = md.delete("collectionName",c)
     * println rows["collectionName"]
     * </pre>
     *
     * @param collection is the collection name to delete from
     * @param conditions is a db query object that has a mongo query conditions for deleting
     * @return Map where the key is a string for the table name that was passed in the table argument.
     * The value is the count of deleted rows.
     */
    public Map<String, Map<String, Object>[]> delete(String collection, DBObject conditions){
        def r = [:]
        log(collection +" - delete", conditions.toString())
        DBCollection col = getDatabase()?.getCollection(collection)
        WriteResult result = col.remove(conditions)
        r[collection] = result.getN()
        return r
    }

    /**
     * Delete for simple queries from a mongo collection that only uses the equality operator
     * <br />
     * Example:<br />
     * The following is equivalent to: delete from collection where (id = 'C33000000001')
     * <pre>
     * def c = ["id" : "C33000000001"]
     * def rows = md.delete("collectionName",c)
     * println rows["collectionName"]
     * </pre>
     *
     * @param collection is the collection name to delete from
     * @param conditions is a Maps where the key is the column name and the value is the expected value
     * to filter on. The column and value is always matched using the equality operator.
     * Multiple columns can be specified by setting more keys in the map which in turn become a logical conjunction.
     * Each array member becomes a logical disjunction with each other.
     * @return A Map where the key is a string for the table name that was passed in the table argument.
     * The value is the count of deleted rows.
     */
    public Map<String, Map<String, Object>[]> delete(String collection, Map<String, Object> conditions){
        return delete(collection, buildQuery(conditions))
    }

    /**
     * Updates rows in mongo db that matches the given condition
     * <br />
     * Example:<br />
     * The following is equivalent to: update collectionName set status='closed', updated=getdate() * where id = 'C33000000001'
     * <pre>
     * def c = JSON.parse("{'id':'C33000000001'}")
     * def u = ["status":"closed", "updated":getDate()]
     * def rows = md.update("collectionName",u,c)
     * println rows["collectionName"]
     *
     * More complex example:<br />
     * The following is equivalent to: update collectionName set status='closed', updated=getdate()
     * where (id = 'C33000000001' and status = 'REVO') OR (id = 'C00000000001')
     * <pre>
     * def c = JSON.parse("{ $or : [   { $and : [  { id : 'C33000000001' }, { status : 'REVO' } ] } , { id : 'C00000000001' } ] }")
     * def u = ["status":"closed", "updated":new Date()]
     * def rows = md.update("collectionName",u,c)
     * println rows["collectionName"]
     * </pre>
     * @param collection is the collection name to delete from
     * @param values is a map containing the fields and the values to be updated
     * @param conditions is a db query object that has a mongo query conditions
     * @return Map where the key is a string for the table name that was passed in the table argument.
     * The value is an array of Maps of where the keys are strings for each column name and the value is the column value.
     */
    public Map<String, Map<String, Object>[]> update(String collection, Map<String, Object> values, DBObject conditions){
        def r = [:]
        log(collection +" - update", conditions.toString(),values.toString())
        DBCollection col = getDatabase()?.getCollection(collection)
        BasicDBObject updateValue = new BasicDBObject()
        values.each {k,v->
            updateValue.put(k,v)
        }
        BasicDBObject updateQuery = new BasicDBObject()
        updateQuery.append('$set', updateValue )
        WriteResult result = col.updateMulti(conditions, updateQuery)
        r[collection] = result.getN()
        return r
    }

    /***
     * Updates rows in mongo db that matches the given condition that only uses the equality operator
     * <br />
     * Example:<br />
     *
     * <pre>
     * def c = ['id':'C33000000001']
     * def u = ["status":"closed", "updated":getDate()]
     * def rows = md.update("collectionName",u,c)
     * println rows["collectionName"]
     *
     * </pre>
     * @param collection is the collection name to delete from
     * @param values is a map containing the fields and the values to be updated
     * @param conditions is a Maps where the key is the column name and the value is the expected value
     * to filter on. The column and value is always matched using the equality operator.
     * Multiple columns can be specified by setting more keys in the map which in turn become a logical conjunction.
     * Each array member becomes a logical disjunction with each other.
     * @return Map where the key is a string for the table name that was passed in the table argument.
     * The value is an array of Maps of where the keys are strings for each column name and the value is the column value.
     */
    public Map<String, Map<String, Object>[]> update(String collection, Map<String, Object> values, Map<String, Object> conditions){
        return update(collection, values, buildQuery(conditions))
    }

    /***
     * Inserts rows in mongo db supplied by the db query
     *<br />
     * Example:<br />
     * The following is equivalent to: insert into collectionName (id,name) values(C33000000001','blah')
     * <pre>
     * def c = JSON.parse("{'id':'C33000000001','name':'blah'}")
     * def rows = md.insert("collectionName",c)
     * println rows["collectionName"]
     *
     * More complex example:<br />
     * The following is equivalent inserting multiple rows :
     * insert into collectionName (id,name) values(C33000000001','blah')
     * insert into collectionName (id,name) values(C33000000002','blah blah')
     * <pre>
     * def c = JSON.parse("[{'id':'C33000000001','name':'blah'},{'id':'C33000000002','name':'blah blah'}]")
     * def rows = md.insert("collectionName",c)
     * println rows["collectionName"]
     * @param collection is the collection name to delete from
     * @param values is a db query containing the fields and the values to be inserted
     * @returnMap where the key is a string for the table name that was passed in the table argument.
     * The value is boolean to indicate the record has been inserted or not.
     */
    public Map<String, Map<String, Object>[]> insert(String collection, DBObject values){
        def r = [:]
        log(collection +" - insert", values.toString())
        DBCollection col = getDatabase()?.getCollection(collection)
        WriteResult result = col.insert(values)
        r[collection] = result.wasAcknowledged()
        return r
    }

    /***
     * Inserts rows in mongo db supplied by the db query
     *<br />
     * Example:<br />
     * The following is equivalent to: insert into collectionName (id,name) values(C33000000001','blah')
     * <pre>
     * def c = [{'id':'C33000000001','name':'blah'}]
     * def rows = md.insert("collectionName",c)
     * println rows["collectionName"]
     *
     * @param collection is the collection name to delete from
     * @param values is a list of map containing the fields and the values to be inserted
     * @return  where the key is a string for the table name that was passed in the table argument.
     * The value is boolean to indicate the record has been inserted or not.
     */
    public Map<String, Map<String, Object>[]> insert(String collection, List<Map<String, Object>> values){
        return insert(collection, buildQuery(values))
    }

    private DBObject buildQuery(Map<String, Object> queryFields) {
        DBObject query = new BasicDBObject();
        queryFields.each{ k, v ->
            query.put(k, v);
            if ("_id".equals(k)) {
                query.put(k, new ObjectId(v.toString()));
            }
        }
        return query;
    }

    private DBObject buildQuery(List<Map<String, Object>> queryFields) {
        def json = new groovy.json.JsonBuilder()
        json queryFields
        return JSON.parse(json.toString())
    }

    def log(coll, query, value=null) {
        logger.debug("collection: $coll")
        logger.debug("query: $query")
        if(value) logger.debug("query: $value")
    }

    public DB getDatabase(){
        throw new Exception ("implement get database method")
    }
}
