package com.qianlima.offline.controller;

import com.qianlima.offline.service.han.Test39Service;
import com.qianlima.offline.service.han.TestFourService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * hanzijun 四月份
 */
@RestController
@RequestMapping("/han")
@Slf4j
@Api("han4月")
public class HanFourController {

    @Autowired
    private TestFourService testFourService;


    @ApiOperation("测试新创建四月controller")
    @PostMapping(value = "/test4",produces = "text/plain;charset=utf-8")
    public String test4() {
        testFourService.test4();
        log.info("===============================数据运行结束===================================");
        return "---四月份项目启动正常---";
    }


}
