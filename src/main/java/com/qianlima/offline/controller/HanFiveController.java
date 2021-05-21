package com.qianlima.offline.controller;

import com.qianlima.offline.service.han.TestFiveService;
import com.qianlima.offline.service.han.TestFourService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * hanzijun 五月份数据
 */
@RestController
@RequestMapping("/han5")
@Slf4j
@Api("han5月")
public class HanFiveController {

    @Autowired
    private TestFiveService testFiveService;


    @ApiOperation("测试五月份的controller")
    @PostMapping(value = "/test5",produces = "text/plain;charset=utf-8")
    public String test4() {
        testFiveService.test5();
        log.info("===============================数据运行结束===================================");
        return "---五月份项目启动正常---";
    }

    @ApiOperation("人民卫生出版社-院校")
    @PostMapping(value = "/getRenMingWeiSheng",produces = "text/plain;charset=utf-8")
    public String getRenMingWeiSheng(Integer type,String date,String s) {
        testFiveService.getRenMingWeiSheng(type,date,s);
        log.info("===============================数据运行结束===================================");
        return "---人民卫生出版社-院校 is ok---";
    }
    @ApiOperation("人民卫生出版社-经销商")
    @PostMapping(value = "/getRenMingWeiShengJxs",produces = "text/plain;charset=utf-8")
    public String getRenMingWeiShengJxs(Integer type,String date,String s) {
        testFiveService.getRenMingWeiShengJxs(type,date,s);
        log.info("===============================数据运行结束===================================");
        return "---人民卫生出版社-经销商 is ok---";
    }

    @ApiOperation("华设设计集团股份有限公司")
    @PostMapping(value = "/getHuaSheSj",produces = "text/plain;charset=utf-8")
    public String getHuaSheSj(Integer type,String date,String s) {
        testFiveService.getHuaSheSj(type,date,s);
        log.info("===============================数据运行结束===================================");
        return "---华设设计集团股份有限公司 is ok---";
    }

    @ApiOperation("福建特力惠信息科技股份有限公司-招标")
    @PostMapping(value = "/getFuJianTeLiHui_zhaobiao",produces = "text/plain;charset=utf-8")
    public String getFuJianTeLiHui_zhaobiao(Integer type,String date,String s) {
        testFiveService.getFuJianTeLiHui_zhaobiao(type,date,s);
        log.info("===============================数据运行结束===================================");
        return "---福建特力惠信息科技股份有限公司-招标 is ok---";
    }
    @ApiOperation("福建特力惠信息科技股份有限公司-中标")
    @PostMapping(value = "/getFuJianTeLiHui_zhongbiao",produces = "text/plain;charset=utf-8")
    public String getFuJianTeLiHui_zhongbiao(Integer type,String date,String s) {
        testFiveService.getFuJianTeLiHui_zhongbiao(type,date,s);
        log.info("===============================数据运行结束===================================");
        return "---福建特力惠信息科技股份有限公司-中标 is ok---";
    }

    @ApiOperation("北京和君咨询有限公司")
    @PostMapping(value = "/getBeijingHeJun",produces = "text/plain;charset=utf-8")
    public String getBeijingHeJun(Integer type,String date,String s) {
        testFiveService.getBeijingHeJun(type,date,s);
        log.info("===============================数据运行结束===================================");
        return "---北京和君咨询有限公司 is ok---";
    }

    @ApiOperation("临时拼接中标单位")
    @PostMapping(value = "/getZhongBiaoUnit",produces = "text/plain;charset=utf-8")
    public String getZhongBiaoUnit(Integer type,String date,String s) throws Exception{
        testFiveService.getZhongBiaoUnit(type,date,s);
        log.info("===============================数据运行结束===================================");
        return "---临时拼接中标单位 is ok---";
    }

    @ApiOperation("临时校验url")
    @PostMapping(value = "/getUrl",produces = "text/plain;charset=utf-8")
    public String getUrl() {
        testFiveService.getUrl();
        log.info("===============================数据运行结束===================================");
        return "---临时url is ok---";
    }

    @ApiOperation("临时拼接中标单位-标准字段")
    @PostMapping(value = "/getZhongBiaoUnitZiDuan",produces = "text/plain;charset=utf-8")
    public String getZhongBiaoUnitZiDuan(Integer type,String date,String s) throws Exception{
        testFiveService.getZhongBiaoUnitZiDuan(type,date,s);
        log.info("===============================数据运行结束===================================");
        return "---临时拼接中标单位标准字段 is ok---";
    }

    @ApiOperation("温州设计集团")
    @PostMapping(value = "/getZheJiangWenZhou",produces = "text/plain;charset=utf-8")
    public String getZheJiangWenZhou(Integer type,String date,String s) throws Exception{
        testFiveService.getZheJiangWenZhou(type,date,s);
        log.info("===============================数据运行结束===================================");
        return "---温州设计集团 is ok---";
    }
    @ApiOperation("温州设计集团-规则二")
    @PostMapping(value = "/getZheJiangWenZhou2",produces = "text/plain;charset=utf-8")
    public String getZheJiangWenZhou2(Integer type,String date,String s) throws Exception{
        testFiveService.getZheJiangWenZhou2(type,date,s);
        log.info("===============================数据运行结束===================================");
        return "---温州设计集团-规则二 is ok---";
    }

    @ApiOperation("测试新规则-poc")
    @PostMapping(value = "/testNewPoc",produces = "text/plain;charset=utf-8")
    public String testNewPoc(Integer type,String date,String s) throws Exception{
        testFiveService.testNewPoc(type,date,s);
        log.info("===============================数据运行结束===================================");
        return "---testNewPoc is ok---";
    }

    @ApiOperation("浙江银行")
    @PostMapping(value = "/getZheJiangYingHang",produces = "text/plain;charset=utf-8")
    public String getZheJiangYingHang() throws Exception{
        testFiveService.getZheJiangYingHang();
        log.info("===============================数据运行结束===================================");
        return "---浙江银行 is ok---";
    }
}
