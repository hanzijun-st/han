package com.qianlima.offline.controller;

import com.qianlima.offline.service.han.Test39Service;
import com.qianlima.offline.service.han.TestYiLiaoHaoCaiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * hanzijun 接口---3月9号以后
 */
@RestController
@RequestMapping("/han")
@Slf4j
@Api("han3.9")
public class HanYiLiaoController {

    @Autowired
    private TestYiLiaoHaoCaiService testYiLiaoHaoCaiService;


    @ApiOperation("医疗耗材")
    @PostMapping(value = "/getYiliaohaocai",produces = "text/plain;charset=utf-8")
    //参数：progidStr取值---> 判断 0:0、1:全部、2:招标[0 TO 2]、3:3、4:[0 TO 3]、5:中标[3 OR progid:5]、6:[0 OR progid:3]
    //全部 progidStr 1
    public String getYiliaohaocai(Integer type,String date,String progidStr) {
        testYiLiaoHaoCaiService.getYiliaohaocai(type, date,progidStr);
        log.info("===============================数据运行结束===================================");
        return "---医疗耗材 接口运行结束---";
    }
}
