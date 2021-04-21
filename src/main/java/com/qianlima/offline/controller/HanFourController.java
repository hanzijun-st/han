package com.qianlima.offline.controller;

import com.qianlima.offline.service.han.Test39Service;
import com.qianlima.offline.service.han.TestFourService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * hanzijun 四月份
 */
@RestController
@RequestMapping("/han")
@Slf4j
@Api("han4月")
public class HanFourController {

    @Autowired
    private TestFourService testFourService;


    @ApiOperation("测试新创建四月controller")
    @PostMapping(value = "/test4",produces = "text/plain;charset=utf-8")
    public String test4() {
        testFourService.test4();
        log.info("===============================数据运行结束===================================");
        return "---四月份项目启动正常---";
    }
    @ApiOperation("测试统计")
    @PostMapping(value = "/testTongJi",produces = "text/plain;charset=utf-8")
    public String testTongJi(Integer type,String date,String s) {
        testFourService.testTongJi(type,date,s);
        log.info("===============================数据运行结束===================================");
        return "---测试统计数据---";
    }

    @ApiOperation("济南富利通电气技术有限公司")
    @PostMapping(value = "/getJiNanFuLiTong",produces = "text/plain;charset=utf-8")
    public String getJiNanFuLiTong(Integer type,String date,String s) {
        testFourService.getJiNanFuLiTong(type,date,s);
        log.info("===============================数据运行结束===================================");
        return "---济南富利通电气技术有限公司 接口运行结束---";
    }

    @ApiOperation("2016年-中节能（山东）环境服务有限公司")
    @PostMapping(value = "/getZhongjieNeng_2016",produces = "text/plain;charset=utf-8")
    public String getZhongjieNeng_2016(Integer type,String date,String s) {
        testFourService.getZhongjieNeng_2016(type,date,s);
        log.info("===============================数据运行结束===================================");
        return "---2016年-中节能（山东）环境服务有限公司 接口运行结束---";
    }

    @ApiOperation("4年-中节能（山东）环境服务有限公司")
    @PostMapping(value = "/getZhongjieNeng_4",produces = "text/plain;charset=utf-8")
    public String getZhongjieNeng_4(Integer type,String date,String s) {
        testFourService.getZhongjieNeng_4(type,date,s);
        log.info("===============================数据运行结束===================================");
        return "---4年-中节能（山东）环境服务有限公司 接口运行结束---";
    }

    @ApiOperation("顺丰科技有限公司")
    @PostMapping(value = "/getShunfeng",produces = "text/plain;charset=utf-8")
    public String getShunfeng(Integer type,String date,String s) {
        testFourService.getShunfeng(type,date,s);
        log.info("===============================数据运行结束===================================");
        return "---顺丰科技有限公司 接口运行结束---";
    }

    @ApiOperation("荣安物业")
    @PostMapping(value = "/getRongAnWuYe",produces = "text/plain;charset=utf-8")
    public String getRongAnWuYe(Integer type,String date,String s) {
        testFourService.getRongAnWuYe(type,date,s);
        log.info("===============================数据运行结束===================================");
        return "---荣安物业 接口运行结束---";
    }
    @ApiOperation("广州欧科信息技术股份有限公司")
    @PostMapping(value = "/getGuangZhouOuKe",produces = "text/plain;charset=utf-8")
    public String getGuangZhouOuKe(Integer type,String date,String s) {
        testFourService.getGuangZhouOuKe(type,date,s);
        log.info("===============================数据运行结束===================================");
        return "---广州欧科信息技术股份有限公司 接口运行结束---";
    }

    @ApiOperation("广州盗梦信息科技有限公司")
    @PostMapping(value = "/getDaoMengXinXi",produces = "text/plain;charset=utf-8")
    public String getDaoMengXinXi(Integer type,String date,String s) {
        testFourService.getDaoMengXinXi(type,date,s);
        log.info("===============================数据运行结束===================================");
        return "---广州盗梦信息科技有限公司 接口运行结束---";
    }

    @ApiOperation("测试电脑性能-调用中台")
    @PostMapping(value = "/getTestZhongtai",produces = "text/plain;charset=utf-8")
    public String getTestZhongtai(Integer type,String date,String s) {
        testFourService.getTestZhongtai(type,date,s);
        log.info("===============================数据运行结束===================================");
        return "---测试电脑性能-调用中台 接口运行结束---";
    }

    @ApiOperation("熠隆医疗设备(上海)")
    @PostMapping(value = "/getYilongYiLiao",produces = "text/plain;charset=utf-8")
    public String getYilongYiLiao(Integer type,String date,String s) {
        testFourService.getYilongYiLiao(type,date,s);
        log.info("===============================数据运行结束===================================");
        return "---熠隆医疗设备(上海)接口运行结束---";
    }


}
