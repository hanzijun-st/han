package com.qianlima.offline.controller;

import com.qianlima.offline.service.han.HanPocService;
import com.qianlima.offline.service.han.HanQingDaoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/qingdao")
@Slf4j
@Api("青岛国家实验室")
public class HanQingDaoController {
    @Autowired
    private HanQingDaoService hanQingDaoService;

    @ApiOperation("青岛市-政府机构")
    @PostMapping(value = "/getQingdaoByZf",produces = "text/plain;charset=utf-8")
    public String getQingdaoByZf(Integer type,String date) {
        hanQingDaoService.getQingdao(type, date);
        log.info("===============================数据运行结束===================================");
        return "---青岛政府机构 接口运行结束---";
    }
    @ApiOperation("青岛市-教育单位")
    @PostMapping(value = "/getQingdaoByJy",produces = "text/plain;charset=utf-8")
    public String getQingdaoByJy(Integer type,String date) {
        hanQingDaoService.getQingdaoByJy(type, date);
        log.info("===============================数据运行结束===================================");
        return "---青岛市-教育单位 接口运行结束---";
    }
    @ApiOperation("青岛市-全量数据")
    @PostMapping(value = "/getQingdaoAll",produces = "text/plain;charset=utf-8")
    public String getQingdaoAll(Integer type,String date) {
        hanQingDaoService.getQingdaoAll(type, date);
        log.info("===============================数据运行结束===================================");
        return "---青岛市-全部数据 接口运行结束---";
    }
}