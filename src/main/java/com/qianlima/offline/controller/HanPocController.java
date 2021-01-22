package com.qianlima.offline.controller;

import com.qianlima.offline.service.han.HanPocService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * hanzijun 接口
 */
@RestController
@RequestMapping("/han")
@Slf4j
@Api("hanpoc")
public class HanPocController {

    @Autowired
    private HanPocService hanPocService;

    @ApiOperation("--新加POC样式模板--")
    @PostMapping("/getNew")
    public String getNew(Integer type){
        hanPocService.getNew(0);
        return "---getNew---";
    }
}
