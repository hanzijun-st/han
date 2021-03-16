package com.qianlima.offline.controller;

import com.qianlima.offline.service.han.TestMongoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mongo")
@Slf4j
@Api("mongo中处理对应数据")
public class HanMongoController {
    @Autowired
    private TestMongoService testMongoService;

    @GetMapping("/test")
    @ApiOperation("从mongo中获取数据-然后处理")
    public String getTest(){
        testMongoService.getMongoTest();
        return "-----123456----";
    }

    @GetMapping("/test222")
    @ApiOperation("然后处理")
    public String getTest2(){
        testMongoService.getTest();
        return "-----   000123456----";
    }
}