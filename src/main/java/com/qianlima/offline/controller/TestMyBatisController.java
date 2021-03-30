package com.qianlima.offline.controller;

import com.qianlima.offline.entity.TestUser;
import com.qianlima.offline.service.han.TestMyBatisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/my")
@Slf4j
@Api("myb")
public class TestMyBatisController {
    @Autowired
    private TestMyBatisService testMyBatisService;

    @ApiOperation("测试mybatis")
    @PostMapping("/test")
    public List test(){
        List list = testMyBatisService.testMyBatis();

        return list;
    }
}