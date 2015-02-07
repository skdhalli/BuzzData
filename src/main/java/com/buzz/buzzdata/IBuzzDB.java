/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.buzz.buzzdata;

import com.mongodb.Bytes;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;
import java.io.File;
import java.io.InputStream;
import org.bson.types.ObjectId;

/**
 *
 * @author sdhalli
 */
public interface IBuzzDB {
    public void Insert(String userid, String header, String content, Double lat, Double lng, String tags, String[] files);
    public void Insert(BuzzInfo buzz);
    public String SearchByLocation(Double lat, Double lng, Double distance,DistanceUnits units,  String tags);
    public String SearchByUserID(String userid, String tags);
    public String SearchByUserIDLocation(Double lat, Double lng, Double distance, DistanceUnits units, String userid, String tags);
    public InputStream GetImgByBuzz(String buzz_id, int pic_num);
}
