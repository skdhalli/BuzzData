/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.buzz.dbhandlers;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author saidhalli
 */
public class RMQHandler implements IDisposable {

    private Connection connection;
    private Channel channel;
    private String Queue;
    
    public RMQHandler(String host,String username, String passwd, String queue) throws IOException
    {
        Queue = queue;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setUsername(username);
        factory.setPassword(passwd);
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(Queue, false, false, false, null);
    }
    
    public void PublishMessage(String exchangeName, String routingKey, BasicProperties bp, byte[] message) throws IOException
    {
        channel.basicPublish(exchangeName, routingKey, bp, message);
    }
    
    @Override
    public void dispose() {
        try {
            channel.close();
            connection.close();
        } catch (IOException ex) {
            Logger.getLogger(RMQHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
