package com.qianlima.offline.controller;

import com.qianlima.offline.service.han.TestHanServcie;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * hanzijun 接口
 */
@RestController
@RequestMapping("/t")
@Slf4j
@Api("t")
public class TestRedisHanController {
    @Autowired
    private TestHanServcie testHanServcie;


    @ApiOperation("test12345")
    @PostMapping("/testRedis")
    public String testRedis(Integer value,String key,Long time) throws Exception{
        testHanServcie.test(key,value,time);
        return "---data is ok---";
    }

    @ApiOperation("存固定值")
    @PostMapping("/saveRedis")
    public String saveRedis(Integer value,String key) throws Exception{
        testHanServcie.saveRedis(key,value);
        return "---data is ok---";
    }

    @ApiOperation("getTest")
    @PostMapping("/getRedis")
    @ResponseBody
    public String getRedis(String key){
        String value = testHanServcie.getRedis(key);
        return value;
    }
}
