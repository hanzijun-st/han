package com.qianlima.offline.controller;

import com.qianlima.offline.bean.Params;
import com.qianlima.offline.service.han.CurrencyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通用方法
 * Created by Administrator on 2021/1/14.
 */
@RestController
@RequestMapping("currency")
@Slf4j
@Api("通用controller")
public class CurrencyController {

    @Autowired
    private CurrencyService currencyService;
    /**
     * 一个关键词
     * @param params
     * @return
     */
    @ApiOperation("一个关键词的通用 poc")
    @PostMapping("/start/getOne")
    public String getJdglOne(@RequestBody Params params){

        currencyService.getOnePoc(params);
        return "---=========---";
    }
}
