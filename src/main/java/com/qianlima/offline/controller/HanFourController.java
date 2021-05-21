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

    @ApiOperation("中节能（山东）")
    @PostMapping(value = "/getZhongJieNeng",produces = "text/plain;charset=utf-8")
    public String getZhongJieNeng(Integer type,String date,String s) {
        testFourService.getZhongJieNeng(type,date,s);
        log.info("===============================数据运行结束===================================");
        return "---中节能（山东）接口运行结束---";
    }
    @ApiOperation("中节能（山东）-2")
    @PostMapping(value = "/getZhongJieNeng2",produces = "text/plain;charset=utf-8")
    public String getZhongJieNeng2(Integer type,String date,String s) {
        testFourService.getZhongJieNeng2(type,date,s);
        log.info("===============================数据运行结束===================================");
        return "---中节能（山东）-2接口运行结束---";
    }

    @ApiOperation("江苏百瑞赢证券咨询有限公司")
    @PostMapping(value = "/getJiangSuBaiRui",produces = "text/plain;charset=utf-8")
    public String getJiangSuBaiRui(Integer type,String date,String s) {
        testFourService.getJiangSuBaiRui(type,date,s);
        log.info("===============================数据运行结束===================================");
        return "---江苏百瑞赢证券咨询有限公司-接口运行结束---";
    }

    @ApiOperation("临时数据-20210430")
    @PostMapping(value = "/getPoc",produces = "text/plain;charset=utf-8")
    public String getPoc(Integer type,String date,String s) {
        testFourService.getPoc(type,date,s);
        log.info("===============================数据运行结束===================================");
        return "---临时数据-接口运行结束---";
    }

    @ApiOperation("临时数据-20210430")
    @PostMapping(value = "/getPoc2",produces = "text/plain;charset=utf-8")
    public String getPoc2(Integer type,String date,String s) throws Exception{
        testFourService.getPoc2(type,date,s);
        log.info("===============================数据运行结束===================================");
        return "---临时数据2-接口运行结束---";
    }

    @ApiOperation("杭州宏旭建设有限公司")
    @PostMapping(value = "/getHangzhouHongXu",produces = "text/plain;charset=utf-8")
    public String getHangzhouHongXu(Integer type,String date,String str) {
        testFourService.getHangzhouHongXu(type, date,str);
        log.info("===============================数据运行结束===================================");
        return "---杭州宏旭建设有限公司 接口运行结束---";
    }

    @ApiOperation("中信产业基金")
    @PostMapping(value = "/getZhongXinChanYe",produces = "text/plain;charset=utf-8")
    public String getZhongXinChanYe(Integer type,String date,String str) {
        testFourService.getZhongXinChanYe(type, date,str);
        log.info("===============================数据运行结束===================================");
        return "---中信产业基金 接口运行结束---";
    }

    @ApiOperation("河南茂乾电子科技有限公司")
    @PostMapping(value = "/getHeNanMaoQian",produces = "text/plain;charset=utf-8")
    public String getHeNanMaoQian(Integer type,String date,String str) {
        testFourService.getHeNanMaoQian(type, date,str);
        log.info("===============================数据运行结束===================================");
        return "---河南茂乾电子科技有限公司 接口运行结束---";
    }

    @ApiOperation("中国光大银行股份有限公司")
    @PostMapping(value = "/getGuangDaYinHang",produces = "text/plain;charset=utf-8")
    public String getGuangDaYinHang(Integer type,String date,String str) {
        testFourService.getGuangDaYinHang(type, date,str);
        log.info("===============================数据运行结束===================================");
        return "---中国光大银行股份有限公司 接口运行结束---";
    }

    @ApiOperation("广州欧科信息技术股份有限公司-第二回合")
    @PostMapping(value = "/getGuangZhouOuKe2",produces = "text/plain;charset=utf-8")
    public String getGuangZhouOuKe2(Integer type,String date,String s) {
        testFourService.getGuangZhouOuKe2(type,date,s);
        log.info("===============================数据运行结束===================================");
        return "---广州欧科信息技术股份有限公司-第二回合 接口运行结束---";
    }
}
