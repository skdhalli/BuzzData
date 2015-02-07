/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.buzz.buzzdata;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.bson.types.ObjectId;



/**
 *
 * @author sdhalli
 */
public class MongoBuzz implements IBuzzDB {

    public MongoBuzz(String host, int port, String db, String userName, String passwd) throws UnknownHostException
    {
        MongoCredential credential = MongoCredential.createMongoCRCredential(userName, db, passwd.toCharArray());
        mongoClient = new MongoClient(new ServerAddress(host, port), Arrays.asList(credential));
        mongoDB = mongoClient.getDB(db);
    }
    
    protected void finalize()
    {
        mongoClient.close();
    }
    
    private MongoClient mongoClient;
    private DB mongoDB;
    
    @Override
    public void Insert(String userid, String header, String content, Double lat, Double lng, String tags, String[] files) {
        BasicDBObject document = new BasicDBObject();
        document.put("userid", userid);
        document.put("header", header);
        document.put("content", content);
        document.put("tags", tags.split(","));
        document.put("created", (new Date()));
        document.put("modified", (new Date()));
        BasicDBObject lng_obj = new BasicDBObject();
        lng_obj.put("lng", lng);
        BasicDBObject lat_obj = new BasicDBObject();
        lat_obj.put("lat", lat);
        document.put("loc",(new Double[]{lng,lat }));
        document.put("FilesCount", files.length);
        DBCollection coll = mongoDB.getCollection("BuzzInfo");
        coll.insert(document);
        ObjectId buzz_id = (ObjectId)document.get( "_id" );
        int i = 0;
        for(String file:files)
        {
            try {
                GridFS gridFS = new GridFS(mongoDB);
                InputStream file_stream = getFTPInputStream(file);
                String caption_filename = FilenameUtils.removeExtension(file)+"_caption.txt";
                InputStream caption_stream = getFTPInputStream(caption_filename);
                StringWriter writer = new StringWriter();
                Charset par = null;
                IOUtils.copy(caption_stream, writer, par);
                String caption = writer.toString();
                GridFSInputFile in = gridFS.createFile(file_stream);
                in.setFilename(file);
                in.put("BuzzID", buzz_id);
                in.put("Caption", caption);
                in.put("PicNum", i);
                in.save();
            } catch (IOException ex) {
                Logger.getLogger(MongoBuzz.class.getName()).log(Level.SEVERE, null, ex);
            }
            i++;
        }
    }

    @Override
    public String SearchByLocation(Double lat, Double lng, Double distance, DistanceUnits units, String tags) {
        String retval = "";
        DBCollection buzzCollection = mongoDB.getCollection("BuzzInfo");
        double distance_radians = 0;
        
        switch(units)
        {
            case miles:
                distance_radians = distance/3959.00;
                break;
            case kilometers:
                distance_radians = distance/6371.00;
                break;
        }
        BasicDBObject filter = new BasicDBObject("$near",new double[]{lng, lat});
        filter.put("$maxDistance", distance_radians);
        
        BasicDBObject query = new BasicDBObject("loc",filter);
        retval = this.getBuzz(query, lat, lng);
        return retval;
    }

    @Override
    public String SearchByUserID(String userid, String tags) {
        String retval = "";
        BasicDBObject query = new BasicDBObject("userid",userid);
        retval = this.getBuzz(query,0.0,0.0);
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
    
    @Override
    public InputStream GetImgByBuzz(String buzz_id, int pic_num)
    {
        InputStream retval = null;
        GridFS gridFS = new GridFS(mongoDB);
        ObjectId _id = new ObjectId(buzz_id);
        BasicDBObject buzz_query = new BasicDBObject("BuzzID",_id);
        GridFSDBFile file =null;
        List<GridFSDBFile> files = gridFS.find(buzz_query);
        for(GridFSDBFile file_buzz:files)
        {
            if((int)file_buzz.get("PicNum") == pic_num)
            {
                file = file_buzz;
            }
        }
        retval = file.getInputStream();
        return retval;
    }
    
    private InputStream getFTPInputStream(String ftp_location)
    {
        InputStream retval = null;
            
        String server = "162.219.245.61";
        int port = 21;
        String user = "jelastic-ftp";
        String pass = "HeZCHxeefB";
        FTPClient ftpClient = new FTPClient();
        
        try 
        {
            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            retval = ftpClient.retrieveFileStream(ftp_location);
        } catch (IOException ex) {
            Logger.getLogger(MongoBuzz.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally 
        {
            try 
            {
                if (ftpClient.isConnected()) {
                    ftpClient.disconnect();
                }
            } 
            catch (IOException ex) 
            {
                Logger.getLogger(MongoBuzz.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return retval;
    }
    
    private String getBuzz(BasicDBObject query, Double lat, Double lng)
    {
        String retval = "";
        DBCollection buzzCollection = mongoDB.getCollection("BuzzInfo");
        DBObject sort = new BasicDBObject();
        sort.put("modified", -1);
        DBCursor cursor = buzzCollection.find(query).sort(sort);
        try 
        {
            while(cursor.hasNext()) 
            {
                //get buzzid
                DBObject buzz_obj = cursor.next();
                ObjectId buzz_id =  (ObjectId) buzz_obj.get("_id");
                //get images for buzzid
                GridFS gridFS = new GridFS(mongoDB);
                BasicDBObject check_images = new BasicDBObject("BuzzID", buzz_id);
                List<GridFSDBFile> dbfiles = gridFS.find(check_images);
                String image_links = "";
                for(GridFSDBFile file:dbfiles)
                {
                    String _buzz_id = buzz_id.toString();
                    String pic_num = file.get("PicNum").toString();
                    
                    if(!image_links.equals(""))
                    {    
                       image_links += ",http://192.168.0.11:8080/BuzzRestAPI/webresources/buzz/Image?buzzid="+_buzz_id+"&pic_num="+pic_num;
                    }
                    else
                    {
                        image_links += "http://192.168.0.11:8080/BuzzRestAPI/webresources/buzz/Image?buzzid="+_buzz_id+"&pic_num="+pic_num;
                    }
                }
                String imgs = "\"Images\": "+"\""+image_links+"\"";
                retval += buzz_obj;
                retval = retval.substring(0, retval.length()-1);
                retval += ", "+imgs;
                double lat2 =(double)((BasicDBList)buzz_obj.get("loc")).get(0);
                double lng2 = (double)((BasicDBList)buzz_obj.get("loc")).get(1);
                double dist = this.haversine(lat, lng, lat2, lng2);
                retval += ", \"Distance\": "+dist;
                String directions_url = "https://maps.google.com/maps?saddr="+lat+","+lng+"&daddr="+lat2+","+lng2+"&hl=en&sll="+lat+","+lng+"&sspn="+lat2+","+lng2+"&t=m&mra=mift&mrsp=1&sz=5&z=18";
                retval += ", \"Directions\": \""+directions_url+"\"";
                retval += "},";
            }
        }
        catch(Exception exc)
        {}
        finally 
        {
            cursor.close();
        }
        retval = retval.substring(0, retval.length()-1);
        retval = "callback({\"Buzzes\": ["+retval+"]})";
        return retval;
    }
    
    
    public double haversine(
        double lat1, double lng1, double lat2, double lng2) {
    double r = 6371/1.6; // average radius of the earth in miles
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lng2 - lng1);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
       Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) 
      * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double d = r * c;
    return round(d,1);
}
    double round(double value, int places) {
    if (places < 0) throw new IllegalArgumentException();

    long factor = (long) Math.pow(10, places);
    value = value * factor;
    long tmp = Math.round(value);
    return (double) tmp / factor;
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

    @Override
    public void Insert(BuzzInfo buzz) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
