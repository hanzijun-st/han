package com.qianlima.offline.controller;

import com.qianlima.offline.service.han.EmailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mail")
@Slf4j
@Api("hanmail")
public class HanEmailController {

    @Autowired
    private EmailService emailService;


    @ApiOperation("--发送验证码--")
    @GetMapping("/sendCode")
    public String sendCode(){
        emailService.sendEmail();
        return "---yzm is ok---";
    }
}