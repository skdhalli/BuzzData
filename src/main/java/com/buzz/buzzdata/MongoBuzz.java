/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.buzz.buzzdata;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;

/**
 *
 * @author sdhalli
 */
public class MongoBuzz implements IBuzzDB {

    public MongoBuzz(String host, int port, String db, String userName, String passwd) throws UnknownHostException
    {
        MongoCredential credential = MongoCredential.createMongoCRCredential(userName, "admin", passwd.toCharArray());
        MongoClient mongoClient = new MongoClient(new ServerAddress(host, port), Arrays.asList(credential));
        mongoDB = mongoClient.getDB(db);
    }
    
    protected void finalize()
    {
        mongoClient.close();
    }
    
    private MongoClient mongoClient;
    private DB mongoDB;
    
    @Override
    public void Insert(String userid, String header, String content, Double lat, Double lng, String tags) {
        BasicDBObject document = new BasicDBObject();
        document.put("userid", userid);
        document.put("header", header);
        document.put("content", content);
        document.put("tags", tags.split(","));
        document.put("created", (new Date()));
        document.put("modified", (new Date()));
        document.put("loc",(new double[]{lat,lng }));
        
        DBCollection coll = mongoDB.getCollection("BuzzInfo");
        coll.insert(document);
    }

    @Override
    public String SearchByLocation(Double lat, Double lng, Double distance, DistanceUnits units, String tags) {
        String retval = "";
        DBCollection buzzCollection = mongoDB.getCollection("BuzzInfo");
        double distance_radians = 0;
        switch(units)
        {
            case miles:
                distance_radians = distance/3959;
                break;
            case kilometers:
                distance_radians = distance/6371;
                break;
        }
        BasicDBObject query = new BasicDBObject("loc",new BasicDBObject("$geoWithin",new BasicDBObject("$center",new Object[]{(new double[]{lat,lng}), distance_radians})));
        retval = this.executeQuery(query, "BuzzInfo");
        return retval;
    }

    @Override
    public String SearchByUserID(String userid, String tags) {
        String retval = "";
        BasicDBObject query = new BasicDBObject("userid",userid);
        retval = this.executeQuery(query, "BuzzInfo");
        return retval;
    }

    @Override
    public String SearchByUserIDLocation(Double lat, Double lng, Double distance, DistanceUnits units, String userid, String tags) {
        String retval = "";
        double distance_radians = 0;
        switch(units)
        {
            case miles:
                distance_radians = distance/3959;
                break;
            case kilometers:
                distance_radians = distance/6371;
                break;
        }
        BasicDBObject query1 = new BasicDBObject("loc",new BasicDBObject("$geoWithin",new BasicDBObject("$center",new Object[]{(new double[]{lat,lng}), distance_radians})));
        BasicDBObject query2 = new BasicDBObject("userid",userid);
        retval = this.executeQueries(new BasicDBObject[]{query1,query2}, "$and", "BuzzInfo");
        return retval;
    }
    
    private String executeQuery(BasicDBObject query, String collName)
    {
        String retval = "";
        DBCollection buzzCollection = mongoDB.getCollection(collName);
        DBCursor cursor = buzzCollection.find(query);
        try {
                while(cursor.hasNext()) {
                    retval += cursor.next();
                    retval += "\n";
                }
            } 
        finally 
            {
                cursor.close();
            }
        return retval;
    }
    
    private String executeQueries(BasicDBObject[] queries, String combine, String collName)
    {
        String retval = "";
        DBCollection buzzCollection = mongoDB.getCollection(collName);
        BasicDBObject filters = new BasicDBObject(combine, queries);
        DBCursor cursor = buzzCollection.find(filters);
        try {
                while(cursor.hasNext()) {
                    retval += cursor.next();
                    retval += "\n";
                }
            } 
        finally 
            {
                cursor.close();
            }
        return retval;
    }
}
