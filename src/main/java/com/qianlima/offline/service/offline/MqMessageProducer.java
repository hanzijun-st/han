package com.qianlima.offline.service.offline;

import com.qianlima.offline.bean.Constant;
import com.qianlima.offline.bean.NoticeMQ;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: hjx
 **/
@Component
@Slf4j
public class MqMessageProducer implements RabbitTemplate.ConfirmCallback {
    private RabbitTemplate rabbitTemplate;

    @Autowired
    public MqMessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        rabbitTemplate.setConfirmCallback(this); //rabbitTemplate如果为单例的话，那回调就是最后设置的内容
    }

    public void ack(Channel channel, Message message, boolean unAck) {
        //ack
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            if (unAck) {
                //nack第一步
                log.error("nack第一步:::::::::::mq ack 报错", e);
                return;  //后续不进行publish
            } else {
                //正常ack
                log.error("mq ack报错", e);
            }
        }
    }

    /**
     * 回调
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        log.info("回调id:" + correlationData);
        if (ack) {
            log.info("消息发送成功");
        } else {
            log.info("消息发送失败:" + cause);
        }
    }


    /**
     * 发生异常时，重新发送到MQ中
     *
     * @param noticeMQ
     * @param nowFlag  true:实时队列   false：历史队列
     */
    public void backQueue(NoticeMQ noticeMQ, boolean nowFlag) {
        log.info("NoticeMQ当前消息重新回到队列: {}", noticeMQ);
        if (nowFlag) {
            //实时数据处理
            rabbitTemplate.convertAndSend(Constant.NOW_EXCHANGE_NAME, Constant.NOW_QUEUE_KEY, noticeMQ);
        } else {
            //历史数据处理
            rabbitTemplate.convertAndSend(Constant.HSITORY_EXCHANGE_NAME, Constant.HSITORY_QUEUE_KEY, noticeMQ);
        }
    }
}
