package com.qianlima.offline.controller;

import com.qianlima.offline.service.han.HanPocService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * hanzijun 接口
 */
@RestController
@RequestMapping("/han")
@Slf4j
@Api("同方威视")
public class HanTongFangController {

    @Autowired
    private HanPocService hanPocService;

    @ApiOperation("同方威视数据")
    @PostMapping(value = "/getTongFangWeiShi",produces = "text/plain;charset=utf-8")
    public String getTongFangWeiShi(Integer type,String date) {
        hanPocService.getTongFangWeiShi(type, date);
        log.info("===============================数据运行结束===================================");
        return "---同方威视数据 接口运行结束---";
    }


}
