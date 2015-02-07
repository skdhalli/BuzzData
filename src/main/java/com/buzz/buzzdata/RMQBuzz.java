/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.buzz.buzzdata;

import com.buzz.dbhandlers.RMQHandler;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author saidhalli
 */
public class RMQBuzz implements IBuzzDB {

    private final static String QUEUE_NAME = "Buzz";
    private final static String HOST_NAME = "hyena.rmq.cloudamqp.com";
    private final static String USER_NAME = "ylhcoumt";
    private final static String PASSWORD = "1I4TSsv94TI9Rf-GtTr0FNPMdudIxxnc";
    
    @Override
    public void Insert(BuzzInfo buzz) {
        
        RMQHandler rmq = null;
        try {
            rmq = new RMQHandler(HOST_NAME, USER_NAME, PASSWORD,QUEUE_NAME);
            rmq.PublishMessage("","",null,buzz.ToJSON().getBytes());
        } catch (IOException ex) {
            Logger.getLogger(RMQBuzz.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            if(rmq != null)
            {
                rmq.dispose();
            }
        }
    }
    

    @Override
    public String SearchByLocation(Double lat, Double lng, Double distance, DistanceUnits units, String tags) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String SearchByUserID(String userid, String tags) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String SearchByUserIDLocation(Double lat, Double lng, Double distance, DistanceUnits units, String userid, String tags) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public InputStream GetImgByBuzz(String buzz_id, int pic_num) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    private Channel getChannel(String queue) throws IOException
    {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("hyena.rmq.cloudamqp.com");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(queue, false, false, false, null);
        return channel;
    }

    @Override
    public void Insert(String userid, String header, String content, Double lat, Double lng, String tags, String[] files) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
