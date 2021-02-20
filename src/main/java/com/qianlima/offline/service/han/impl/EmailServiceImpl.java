package com.qianlima.offline.service.han.impl;

import com.qianlima.offline.service.han.EmailService;
import com.qianlima.offline.util.SendEmail;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class EmailServiceImpl implements EmailService {

    @Override
    public void sendEmail() {
        SendEmail sendEmail=new SendEmail();
        //设置要发送的邮箱
        sendEmail.setReceiveMailAccount("han229329@163.com");
        //创建10位发验证码
        Random random=new Random();
        String str="";
        for(int i=0;i<4;i++) {
            int n=random.nextInt(4);
            str+=n;
        }
        sendEmail.setInfo(str);
        try {
            sendEmail.Send();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}