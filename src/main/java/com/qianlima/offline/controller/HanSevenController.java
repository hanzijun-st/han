package com.qianlima.offline.controller;

import com.qianlima.offline.service.han.TestSevenService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * hanzijun 七月份数据
 */
@RestController
@RequestMapping("/han7")
@Slf4j
@Api("han7月")
public class HanSevenController {

    @Autowired
    private TestSevenService testSevenService;


    /**
     * 深圳华大智造科技股份有限公司
     * @param type 是否入库
     * @param date 日期
     * @param sType 是否写文件
     * @param name 文件名称
     * @return
     */
    @ApiOperation("深圳华大智造科技股份有限公司")
    @PostMapping(value = "/getShenZhenHuaDa", produces = "text/plain;charset=utf-8")
    public String getShenZhenHuaDa(Integer type, String date, String sType, String name) {
        testSevenService.getShenZhenHuaDa(type, date, sType, name);
        log.info("===============================数据运行结束===================================");
        return "---深圳华大智造科技股份有限公司 is ok---";
    }

    @ApiOperation("solr上获取数据")
    @PostMapping(value = "/getSolrDatas", produces = "text/plain;charset=utf-8")
    public String getSolrDatas() {
        testSevenService.getSolrDatas();
        log.info("===============================数据运行结束===================================");
        return "---solr上获取数据 is ok---";
    }

    @ApiOperation("大金额处理相关数据")
    @PostMapping(value = "/getDaJinEDatas", produces = "text/plain;charset=utf-8")
    public String getDaJinEDatas(Integer type, String date) {
        testSevenService.getDaJinEDatas(type, date);
        log.info("===============================数据运行结束===================================");
        return "---大金额处理相关数据 is ok---";
    }

    @ApiOperation("杭州博日科技股份有限公司")
    @PostMapping(value = "/getHangZhouBoRi", produces = "text/plain;charset=utf-8")
    public String getHangZhouBoRi(Integer type, String date, String sType, String name) {
        testSevenService.getHangZhouBoRi(type, date, sType, name);
        log.info("===============================数据运行结束===================================");
        return "---杭州博日科技股份有限公司 is ok---";
    }
}
