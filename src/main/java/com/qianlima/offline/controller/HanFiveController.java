package com.qianlima.offline.controller;

import com.qianlima.offline.service.han.TestFiveService;
import com.qianlima.offline.service.han.TestFourService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
    @PostMapping(value = "/test5", produces = "text/plain;charset=utf-8")
    public String test4() {
        testFiveService.test5();
        log.info("===============================数据运行结束===================================");
        return "---五月份项目启动正常---";
    }

    @ApiOperation("人民卫生出版社-院校")
    @PostMapping(value = "/getRenMingWeiSheng", produces = "text/plain;charset=utf-8")
    public String getRenMingWeiSheng(Integer type, String date, String s) {
        testFiveService.getRenMingWeiSheng(type, date, s);
        log.info("===============================数据运行结束===================================");
        return "---人民卫生出版社-院校 is ok---";
    }

    @ApiOperation("人民卫生出版社-经销商")
    @PostMapping(value = "/getRenMingWeiShengJxs", produces = "text/plain;charset=utf-8")
    public String getRenMingWeiShengJxs(Integer type, String date, String s) {
        testFiveService.getRenMingWeiShengJxs(type, date, s);
        log.info("===============================数据运行结束===================================");
        return "---人民卫生出版社-经销商 is ok---";
    }

    @ApiOperation("华设设计集团股份有限公司")
    @PostMapping(value = "/getHuaSheSj", produces = "text/plain;charset=utf-8")
    public String getHuaSheSj(Integer type, String date, String s) {
        testFiveService.getHuaSheSj(type, date, s);
        log.info("===============================数据运行结束===================================");
        return "---华设设计集团股份有限公司 is ok---";
    }

    @ApiOperation("福建特力惠信息科技股份有限公司-招标")
    @PostMapping(value = "/getFuJianTeLiHui_zhaobiao", produces = "text/plain;charset=utf-8")
    public String getFuJianTeLiHui_zhaobiao(Integer type, String date, String s) {
        testFiveService.getFuJianTeLiHui_zhaobiao(type, date, s);
        log.info("===============================数据运行结束===================================");
        return "---福建特力惠信息科技股份有限公司-招标 is ok---";
    }

    @ApiOperation("福建特力惠信息科技股份有限公司-中标")
    @PostMapping(value = "/getFuJianTeLiHui_zhongbiao", produces = "text/plain;charset=utf-8")
    public String getFuJianTeLiHui_zhongbiao(Integer type, String date, String s) {
        testFiveService.getFuJianTeLiHui_zhongbiao(type, date, s);
        log.info("===============================数据运行结束===================================");
        return "---福建特力惠信息科技股份有限公司-中标 is ok---";
    }

    @ApiOperation("北京和君咨询有限公司")
    @PostMapping(value = "/getBeijingHeJun", produces = "text/plain;charset=utf-8")
    public String getBeijingHeJun(Integer type, String date, String s) {
        testFiveService.getBeijingHeJun(type, date, s);
        log.info("===============================数据运行结束===================================");
        return "---北京和君咨询有限公司 is ok---";
    }

    @ApiOperation("临时拼接中标单位")
    @PostMapping(value = "/getZhongBiaoUnit", produces = "text/plain;charset=utf-8")
    public String getZhongBiaoUnit(Integer type, String date, String s) throws Exception {
        testFiveService.getZhongBiaoUnit(type, date, s);
        log.info("===============================数据运行结束===================================");
        return "---临时拼接中标单位 is ok---";
    }

    @ApiOperation("临时校验url")
    @PostMapping(value = "/getUrl", produces = "text/plain;charset=utf-8")
    public String getUrl() {
        testFiveService.getUrl();
        log.info("===============================数据运行结束===================================");
        return "---临时url is ok---";
    }

    @ApiOperation("临时拼接中标单位-标准字段")
    @PostMapping(value = "/getZhongBiaoUnitZiDuan", produces = "text/plain;charset=utf-8")
    public String getZhongBiaoUnitZiDuan(Integer type, String date, String s) throws Exception {
        testFiveService.getZhongBiaoUnitZiDuan(type, date, s);
        log.info("===============================数据运行结束===================================");
        return "---临时拼接中标单位标准字段 is ok---";
    }

    @ApiOperation("温州设计集团")
    @PostMapping(value = "/getZheJiangWenZhou", produces = "text/plain;charset=utf-8")
    public String getZheJiangWenZhou(Integer type, String date, String s) throws Exception {
        testFiveService.getZheJiangWenZhou(type, date, s);
        log.info("===============================数据运行结束===================================");
        return "---温州设计集团 is ok---";
    }

    @ApiOperation("温州设计集团-规则二")
    @PostMapping(value = "/getZheJiangWenZhou2", produces = "text/plain;charset=utf-8")
    public String getZheJiangWenZhou2(Integer type, String date, String s) throws Exception {
        testFiveService.getZheJiangWenZhou2(type, date, s);
        log.info("===============================数据运行结束===================================");
        return "---温州设计集团-规则二 is ok---";
    }

    @ApiOperation("测试新规则-poc")
    @PostMapping(value = "/testNewPoc", produces = "text/plain;charset=utf-8")
    public String testNewPoc(Integer type, String date, String s) throws Exception {
        testFiveService.testNewPoc(type, date, s);
        log.info("===============================数据运行结束===================================");
        return "---testNewPoc is ok---";
    }

    @ApiOperation("浙江银行")
    @PostMapping(value = "/getZheJiangYingHang", produces = "text/plain;charset=utf-8")
    public String getZheJiangYingHang() throws Exception {
        testFiveService.getZheJiangYingHang();
        log.info("===============================数据运行结束===================================");
        return "---浙江银行 is ok---";
    }

    @ApiOperation("人民卫生出版社-院校-第二回合")
    @PostMapping(value = "/getRenMingWeiSheng2", produces = "text/plain;charset=utf-8")
    public String getRenMingWeiSheng2(Integer type, String date, String s) {
        testFiveService.getRenMingWeiSheng2(type, date, s);
        log.info("===============================数据运行结束===================================");
        return "---人民卫生出版社-院校-第二回合 is ok---";
    }

    @ApiOperation("人民卫生出版社-经销商-第二回合")
    @PostMapping(value = "/getRenMingWeiShengJxs2", produces = "text/plain;charset=utf-8")
    public String getRenMingWeiShengJxs2(Integer type, String date, String s) {
        testFiveService.getRenMingWeiShengJxs2(type, date, s);
        log.info("===============================数据运行结束===================================");
        return "---人民卫生出版社-经销商-第二回合 is ok---";
    }

    @ApiOperation("广西省产业经济与城乡发展研究会")
    @PostMapping(value = "/getGuangXiChanYe", produces = "text/plain;charset=utf-8")
    public String getGuangXiChanYe(Integer type, String date, String s, String name) {
        testFiveService.getGuangXiChanYe(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---广西省产业经济与城乡发展研究会 is ok---";
    }

    @ApiOperation("上海磐合科学仪器股份有限公司")
    @PostMapping(value = "/getShangHaiQingHe", produces = "text/plain;charset=utf-8")
    public String getShangHaiQingHe(Integer type, String date, String s, String name) {
        testFiveService.getShangHaiQingHe(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---上海磐合科学仪器股份有限公司 is ok---";
    }

    @ApiOperation("网筑投资管理有限公司")
    @PostMapping(value = "/getWangZhuTouZi", produces = "text/plain;charset=utf-8")
    public String getWangZhuTouZi(Integer type, String date, String s, String name) {
        testFiveService.getWangZhuTouZi(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---网筑投资管理有限公司 is ok---";
    }

    @ApiOperation("中铁建物业")
    @PostMapping(value = "/getZhongTieJian", produces = "text/plain;charset=utf-8")
    public String getZhongTieJian(Integer type, String date, String s, String name) {
        testFiveService.getZhongTieJian(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---中铁建物业 is ok---";
    }

    @ApiOperation("中铁建物业-规则二")
    @PostMapping(value = "/getZhongTieJian2", produces = "text/plain;charset=utf-8")
    public String getZhongTieJian2(Integer type, String date, String s, String name) {
        testFiveService.getZhongTieJian2(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---中铁建物业-规则二 is ok---";
    }

    @ApiOperation("防火墙-通知")
    @PostMapping(value = "/getFangHuoQiang", produces = "text/plain;charset=utf-8")
    public String getFangHuoQiang(Integer type, String date, String s, String name) {
        testFiveService.getFangHuoQiang(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---防火墙-通知 is ok---";
    }

    @ApiOperation("防火墙-通知--存contentId")
    @PostMapping(value = "/getFangHuoQiangToId", produces = "text/plain;charset=utf-8")
    public String getFangHuoQiangToId(String date) {
        testFiveService.getFangHuoQiangToId(date);
        log.info("===============================数据运行结束===================================");
        return "---存id is ok---";
    }

    @ApiOperation("天融信")
    @PostMapping(value = "/getTianRongXin", produces = "text/plain;charset=utf-8")
    public String getTianRongXin(Integer type, String date, String s, String name) {
        testFiveService.getTianRongXin(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---天融信 is ok---";
    }

    @ApiOperation("天融信-id")
    @PostMapping(value = "/getDataById", produces = "text/plain;charset=utf-8")
    public String getDataById() throws Exception {
        testFiveService.getDataById();
        log.info("===============================数据运行结束===================================");
        return "---天融信-id is ok---";
    }

    @ApiOperation("苏州嗨森无人机科技有限公司")
    @PostMapping(value = "/getSuZhouHaiSeng", produces = "text/plain;charset=utf-8")
    public String getSuZhouHaiSeng(Integer type, String date, String s, String name) throws Exception {
        testFiveService.getSuZhouHaiSeng(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---苏州嗨森无人机科技有限公司 is ok---";
    }

    @ApiOperation("浙江大华")
    @PostMapping(value = "/getZheJiangDaHua", produces = "text/plain;charset=utf-8")
    public String getZheJiangDaHua() throws Exception {
        testFiveService.getZheJiangDaHua();
        log.info("===============================数据运行结束===================================");
        return "---浙江大华 is ok---";
    }


    @ApiOperation("统计-招标")
    @PostMapping(value = "/getTongJiZhaoBiao", produces = "text/plain;charset=utf-8")
    public String getTongJiZhaoBiao() throws Exception {
        testFiveService.getTongJiZhaoBiao();
        log.info("===============================数据运行结束===================================");
        return "---统计招标 is ok---";
    }
    @ApiOperation("统计-中标")
    @PostMapping(value = "/getTongJiZhongBiao", produces = "text/plain;charset=utf-8")
    public String getTongJiZhongBiao() throws Exception {
        testFiveService.getTongJiZhongBiao();
        log.info("===============================数据运行结束===================================");
        return "---统计中标 is ok---";
    }

    @ApiOperation("碧桂园")
    @PostMapping(value = "/getBiGuiYuan", produces = "text/plain;charset=utf-8")
    public String getBiGuiYuan(Integer type, String date, String s, String name) throws Exception {
        testFiveService.getBiGuiYuan(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---碧桂园 is ok---";
    }


}
