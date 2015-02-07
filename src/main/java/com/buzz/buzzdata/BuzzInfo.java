/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.buzz.buzzdata;

/**
 *
 * @author saidhalli
 */
public class BuzzInfo {
    
    public String userid;
    public String header;
    public String content;
    public Double lat;
    public Double lng;
    public String tags;
    public String[] files;
    
    public String ToJSON()
    {
        String retval = "{\n" +
                    "    \"userid\" : \""+userid+"\",\n" +
                    "    \"header\" : \""+header+"\",\n" +
                    "    \"content\" : \""+content+"\",\n" +
                    "    \"tags\" : [ \n" +tags+
                    "    ],\n" +
                    "    \"created\" : "+(new java.util.Date()).toString()+",\n" +
                    "    \"modified\" : "+(new java.util.Date()).toString()+",\n" +
                    "    \"loc\" : [ \n" +
                    "        "+lng.toString()+", \n" +
                    "        "+lat.toString()+"\n" +
                    "    ],\n" +
                    "    \"Files\" : ["+String.join(",", files)+"]\n" +
                    "}";
        return retval;
    }
}
