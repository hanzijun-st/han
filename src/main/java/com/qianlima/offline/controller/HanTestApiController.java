package com.qianlima.offline.controller;

import com.qianlima.offline.service.han.TestApiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * hanzijun 接口
 */
@RestController
@RequestMapping("/api")
@Slf4j
@Api("han测试api")
public class HanTestApiController {


    @Autowired
    private TestApiService testApiService;//测试api


    @GetMapping("/test1028")
    @ApiOperation("用于添加企业下的数据")
    public String test1028() throws Exception {
        testApiService.testApi();
        log.info("接口执行完毕！！！---------------------data is get ok");
        return "data is get ok";
    }




}
