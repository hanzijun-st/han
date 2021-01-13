package com.qianlima.offline.controller;

import com.qianlima.offline.service.han.AoLinBaSiService;
import com.qianlima.offline.service.han.TestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Administrator on 2021/1/12.
 */
@RestController
@RequestMapping("/aolinbasi")
@Slf4j
@Api("hanpoc")
public class HanTestController {

    @Autowired
    private AoLinBaSiService aoLinBaSiService;
    @Autowired
    private TestService testService;

    @GetMapping("/start/getAolinbasiDatas")
    @ApiOperation("获取奥林巴斯的数据")
    public String getTestAllDatas(){
        aoLinBaSiService.getAoLinBaSiAndSave();
        return "第一次测试数据---获取成功";
    }

    @GetMapping("/start/getUrl/{num}")
    @ApiOperation("获取原链接地址的数据")
    public String getUrl(@PathVariable("num") String num){
        String urlOriginalLink = aoLinBaSiService.getUrlOriginalLink(num);
        return "成功获取url原链接地址---"+urlOriginalLink;
    }

    @GetMapping("/start/getBdw")
    @ApiOperation("获取标的物的数据")
    public String getBdw(){
        testService.getBdw();
        return "请求成功---成功获取标的物";
    }

    @GetMapping("/start/updateKeyword")
    @ApiOperation("修改关键词")
    public String updateKeyword(){
        testService.updateKeyword();
        return "-----------修改关键词成功-----------";
    }

    @PostMapping("/start/test")
    @ApiOperation("测试接口返回")
    public String test(){
        return "-----------测试成功-----------";
    }

    @RequestMapping(value = "/downLoad", method = RequestMethod.POST, produces = "text/plain;charset=utf-8")
    @ApiOperation("将数据导出excel")
    public String downLoad(){
        return testService.downLoad();
    }
}