package com.qianlima.offline.service.han.impl;

import com.qianlima.offline.service.han.RabbitMqService;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class RabbitMqServiceImpl implements RabbitMqService{



    private final static String QUEUE_NAME ="qq";

    @Override
    public void send() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("123");
        factory.setVirtualHost("/");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME,false,false,false,null);
        String message ="Hello world";

        channel.basicPublish("",QUEUE_NAME,null,message.getBytes());
        channel.close();
        connection.close();
    }

    @Override
    public void receive() throws Exception {

        //定义一个连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //设置服务器地址
        factory.setHost("localhost");
        //设置端口号
        factory.setPort(5672);
        //设置vhost
        factory.setVirtualHost("/");

        factory.setConnectionTimeout(10000);
        factory.setUsername("admin");
        factory.setPassword("123");

        //获取一个连接
        Connection connection = factory.newConnection();
        //获取一个通道
        Channel channel = connection.createChannel();
        //定义队列的消费者
        DefaultConsumer defaultConsumer = new DefaultConsumer(channel) {
            //获取到达的消息
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                super.handleDelivery(consumerTag, envelope, properties, body);
                String msg = new String(body,"utf-8");
                System.out.println(msg);
            }
        };
        //监听队列
        channel.basicConsume(QUEUE_NAME,true,defaultConsumer);
    }
}