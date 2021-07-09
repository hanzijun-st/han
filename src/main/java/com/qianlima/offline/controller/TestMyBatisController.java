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

    @ApiOperation("测试-避免重复插入")
    @PostMapping(value = "/saveToDef")
    @ResponseBody
    public Boolean saveToDef(@RequestBody LcDto lcDto) {
        testMyBatisService.saveToDef(lcDto);
        return true;
    }

    @ApiOperation("测试-判断不开启事务-是否回滚")
    @PostMapping(value = "/saveDatas")
    @ResponseBody
    public Boolean saveDatas(@RequestBody LcDto lcDto)  throws Exception{
        testMyBatisService.saveDatas(lcDto);
        return true;
    }

}