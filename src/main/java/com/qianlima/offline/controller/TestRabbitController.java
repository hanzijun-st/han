package com.qianlima.offline.controller;

import com.qianlima.offline.service.han.RabbitMqService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mq")
@Slf4j
@Api("测试mq")
public class TestRabbitController {
    @Autowired
    private RabbitMqService rabbitMqService;

    @ApiOperation("测试一下")
    @PostMapping(value = "/testMq")
    public String testSolr() throws Exception{
        rabbitMqService.send();
        return "测试 is 成功";
    }
}