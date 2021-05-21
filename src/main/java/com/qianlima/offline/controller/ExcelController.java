package com.qianlima.offline.controller;

import com.qianlima.offline.service.han.AoLinBaSiService;
import com.qianlima.offline.service.han.CurrencyService;
import com.qianlima.offline.service.han.TestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Administrator on 2021/1/12.
 */
@RestController
@RequestMapping("/excel")
@Slf4j
@Api("处理Excel")
public class ExcelController {

    @Autowired
    private TestService testService;



    @RequestMapping(value = "/downLoad", method = RequestMethod.POST, produces = "text/plain;charset=utf-8")
    @ApiOperation("将数据导出excel")
    public String downLoad(){
        return testService.downLoad();
    }

    @RequestMapping(value = "/downByModel",method = RequestMethod.GET)
    public void downByModel(){
        testService.downByModel();
    }


}
