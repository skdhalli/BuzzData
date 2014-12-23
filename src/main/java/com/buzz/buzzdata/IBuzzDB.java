/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.buzz.buzzdata;

/**
 *
 * @author sdhalli
 */
public interface IBuzzDB {
    public void Insert(String userid, String header, String content, Double lat, Double lng, String tags);
    public String SearchByLocation(Double lat, Double lng, Double distance,DistanceUnits units,  String tags);
    public String SearchByUserID(String userid, String tags);
    public String SearchByUserIDLocation(Double lat, Double lng, Double distance, DistanceUnits units, String userid, String tags);
}
