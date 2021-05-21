package com.qianlima.offline.controller;

import com.qianlima.offline.bean.LcDto;
import com.qianlima.offline.entity.TestUser;
import com.qianlima.offline.service.han.TestMyBatisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/my")
@Slf4j
@Api("myb")
public class TestMyBatisController {
    @Autowired
    private TestMyBatisService testMyBatisService;

   /* @ApiOperation("测试mybatis")
    @PostMapping("/test")
    public List test(){
        List list = testMyBatisService.testMyBatis();

        return list;
    }

    @ApiOperation("csTime")
    @GetMapping("/cs")
    public String cs(){
        testMyBatisService.saveDatas();
        return "cs is ok";
    }*/

    @ApiOperation("测试")
    @PostMapping(value = "/testLc")
    @ResponseBody
    public List testLc(@RequestBody LcDto lcDto) {
        List list = testMyBatisService.testLc(lcDto);
        return list;
    }
}