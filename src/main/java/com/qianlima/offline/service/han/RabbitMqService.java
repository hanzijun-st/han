package com.qianlima.offline.service.han;


public interface RabbitMqService {
    void send() throws Exception;

    void receive() throws Exception;
}