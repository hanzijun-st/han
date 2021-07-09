package com.qianlima.offline.controller;

import com.qianlima.offline.service.han.TestFiveService;
import com.qianlima.offline.service.han.TestSixService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * hanzijun 六月份数据
 */
@RestController
@RequestMapping("/han6")
@Slf4j
@Api("han6月")
public class HanSixController {

    @Autowired
    private TestSixService testSixService;


    @ApiOperation("天融信")
    @PostMapping(value = "/getTianRongXin", produces = "text/plain;charset=utf-8")
    public String getTianRongXin(Integer type, String date, String s, String name) {
        testSixService.getTianRongXin(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---天融信 is ok---";
    }

    @ApiOperation("天融信-通过标的物去重后id，输出poc标准字段")
    @PostMapping(value = "/getDataById", produces = "text/plain;charset=utf-8")
    public String getDataById() throws Exception {
        testSixService.getDataById();
        log.info("===============================数据运行结束===================================");
        return "---天融信-id is ok---";
    }

    @ApiOperation("中国石油天然气股份有限公司山东销售分公司")
    @PostMapping(value = "/getShiYouTianRanQi", produces = "text/plain;charset=utf-8")
    public String getShiYouTianRanQi(Integer type, String date, String s, String name) {
        testSixService.getShiYouTianRanQi(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---中国石油天然气股份有限公司山东销售分公司 is ok---";
    }

    @ApiOperation("公司-临时统计")
    @PostMapping(value = "/getGsTongJi", produces = "text/plain;charset=utf-8")
    public String getGsTongJi(String date,String type) {
        testSixService.getGsTongJi(date,type);
        log.info("===============================数据运行结束===================================");
        return "---tongji is ok---";
    }

    @ApiOperation("中信产业基金")
    @PostMapping(value = "/getZhongXin", produces = "text/plain;charset=utf-8")
    public String getZhongXin() throws Exception{
        testSixService.getZhongXin();
        log.info("===============================数据运行结束===================================");
        return "---中信产业基金 is ok---";
    }

    @ApiOperation("宁波弘泰空间结构工程有限公司")
    @PostMapping(value = "/getNingBoHongTai", produces = "text/plain;charset=utf-8")
    public String getNingBoHongTai(Integer type, String date, String s, String name) {
        testSixService.getNingBoHongTai(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---宁波弘泰空间结构工程有限公司 is ok---";
    }

    @ApiOperation("浙商银行股份有限公司")
    @PostMapping(value = "/getZheShangYingHang", produces = "text/plain;charset=utf-8")
    public String getZheShangYingHang(Integer type, String date, String s, String name) {
        testSixService.getZheShangYingHang(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---浙商银行股份有限公司 is ok---";
    }
    @ApiOperation("浙商银行股份有限公司-c")
    @PostMapping(value = "/getZheShangYingHangC", produces = "text/plain;charset=utf-8")
    public String getZheShangYingHangC(Integer type, String date, String s, String name) {
        testSixService.getZheShangYingHangC(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---浙商银行股份有限公司-c is ok---";
    }

    @ApiOperation("森达美信昌机器工程（广东）有限公司")
    @PostMapping(value = "/getSengDaMeiXin", produces = "text/plain;charset=utf-8")
    public String getSengDaMeiXin(Integer type, String date, String s, String name) {
        testSixService.getSengDaMeiXin(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---森达美信昌机器工程（广东）有限公司 is ok---";
    }

    @ApiOperation("森达美信昌机器工程（广东）有限公司-规则二")
    @PostMapping(value = "/getSengDaMeiXin2", produces = "text/plain;charset=utf-8")
    public String getSengDaMeiXin2(Integer type, String date, String s, String name) {
        testSixService.getSengDaMeiXin2(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---森达美信昌机器工程（广东）有限公司-规则二 is ok---";
    }

    @ApiOperation("中铁建物业-招标")
    @PostMapping(value = "/getZhongTieJian", produces = "text/plain;charset=utf-8")
    public String getZhongTieJian(Integer type, String date, String s, String name) {
        testSixService.getZhongTieJian(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---中铁建物业 is ok---";
    }

    @ApiOperation("中铁建物业-中标")
    @PostMapping(value = "/getZhongTieJian_zhongBiao", produces = "text/plain;charset=utf-8")
    public String getZhongTieJian_zhongBiao(Integer type, String date, String s, String name) {
        testSixService.getZhongTieJian_zhongBiao(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---中铁建物业-中标 is ok---";
    }
    @ApiOperation("中国石油天然气")
    @PostMapping(value = "/getZhongGuoShiYouTianRanQi", produces = "text/plain;charset=utf-8")
    public String getZhongGuoShiYouTianRanQi(Integer type, String date, String s, String name) {
        testSixService.getZhongGuoShiYouTianRanQi(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---中国石油天然气 is ok---";
    }

    @ApiOperation("宁波弘泰空间结构工程有限公司-第二回合")
    @PostMapping(value = "/getNingBoHongTai2", produces = "text/plain;charset=utf-8")
    public String getNingBoHongTai2(Integer type, String date, String s, String name) {
        testSixService.getNingBoHongTai2(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---宁波弘泰空间结构工程有限公司-第二回合 is ok---";
    }

    @ApiOperation("北京国视无双视频科技有限公司")
    @PostMapping(value = "/getBeiJingGuoShi", produces = "text/plain;charset=utf-8")
    public String getBeiJingGuoShi(Integer type, String date, String s, String name) {
        testSixService.getBeiJingGuoShi(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---北京国视无双视频科技有限公司 is ok---";
    }

    @ApiOperation("大华")
    @PostMapping(value = "/getDaHua", produces = "text/plain;charset=utf-8")
    public String getDaHua(Integer type, String date, String s, String name) {
        testSixService.getDaHua(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---大华 is ok---";
    }

    @ApiOperation("北京数字认证股份有限公司")
    @PostMapping(value = "/getShuZiRenZheng", produces = "text/plain;charset=utf-8")
    public String getShuZiRenZheng(Integer type, String date, String s, String name) {
        testSixService.getShuZiRenZheng(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---北京数字认证股份有限公司 is ok---";
    }

    @ApiOperation("通过id-追加关键词")
    @PostMapping(value = "/getKeyWordById", produces = "text/plain;charset=utf-8")
    public String getKeyWordById() throws Exception{
        testSixService.getKeyWordById();
        log.info("===============================数据运行结束===================================");
        return "---通过id-追加关键词 is ok---";
    }

    @ApiOperation("贝朗医疗")
    @PostMapping(value = "/getBeiLangYiLiao", produces = "text/plain;charset=utf-8")
    public String getBeiLangYiLiao(Integer type, String date, String s, String name) {
        testSixService.getBeiLangYiLiao(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---贝朗医疗 is ok---";
    }

    @ApiOperation("清华大学")
    @PostMapping(value = "/getQingHuaDaXue", produces = "text/plain;charset=utf-8")
    public String getQingHuaDaXue(Integer type, String date, String s, String name) {
        testSixService.getQingHuaDaXue(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---清华大学 is ok---";
    }

    @ApiOperation("武汉鑫潭环保高科技有限公司")
    @PostMapping(value = "/getWuHanXinTan", produces = "text/plain;charset=utf-8")
    public String getWuHanXinTan(Integer type, String date, String s, String name) {
        testSixService.getWuHanXinTan(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---武汉鑫潭环保高科技有限公司 is ok---";
    }

    @ApiOperation("贝朗医疗-第二回合")
    @PostMapping(value = "/getBeiLangYiLiao2", produces = "text/plain;charset=utf-8")
    public String getBeiLangYiLiao2(Integer type, String date, String s, String name) {
        testSixService.getBeiLangYiLiao2(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---贝朗医疗-第二回合 is ok---";
    }

    @ApiOperation("贝朗医疗-第二回合-规则三")
    @PostMapping(value = "/getBeiLangYiLiao2_3", produces = "text/plain;charset=utf-8")
    public String getBeiLangYiLiao2_3(Integer type, String date, String s, String name) {
        testSixService.getBeiLangYiLiao2_3(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---贝朗医疗-第二回合-规则三 is ok---";
    }

    @ApiOperation("广州市天谱电器有限公司")
    @PostMapping(value = "/getGuangZhouTianPu", produces = "text/plain;charset=utf-8")
    public String getGuangZhouTianPu(Integer type, String date, String s, String name,Integer gz) {
        testSixService.getGuangZhouTianPu(type, date, s, name,gz);
        log.info("===============================数据运行结束===================================");
        return "---广州市天谱电器有限公司 is ok---";
    }

    @ApiOperation("奥林巴斯-第三回合(tp 0:全部  2:招标  3:中标)")
    @PostMapping(value = "/getAoLinBaSi3", produces = "text/plain;charset=utf-8")
    public String getAoLinBaSi3(Integer type, String date, String s, String name,Integer tp) {
        testSixService.getAoLinBaSi3(type, date, s, name,tp);
        log.info("===============================数据运行结束===================================");
        return "---奥林巴斯-第三回合 is ok---";
    }

    @ApiOperation("浙江汉略网络科技有限公司")
    @PostMapping(value = "/getZheJiangShuangLue", produces = "text/plain;charset=utf-8")
    public String getZheJiangShuangLue(Integer type, String date, String s, String name) {
        testSixService.getZheJiangShuangLue(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---浙江汉略网络科技有限公司 is ok---";
    }

    @ApiOperation("青岛海尔生物医疗股份有限公司")
    @PostMapping(value = "/getQingDaoHaiErShengWu", produces = "text/plain;charset=utf-8")
    public String getQingDaoHaiErShengWu(Integer type, String date, String s, String name) {
        testSixService.getQingDaoHaiErShengWu(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---青岛海尔生物医疗股份有限公司 is ok---";
    }

    @ApiOperation("深圳华大智造")
    @PostMapping(value = "/getShenZhenHuaDaZhiZao", produces = "text/plain;charset=utf-8")
    public String getShenZhenHuaDaZhiZao(Integer type, String date, String s, String name) {
        testSixService.getShenZhenHuaDaZhiZao(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---深圳华大智造 is ok---";
    }

    @ApiOperation("无人机")
    @PostMapping(value = "/getWuRenJi", produces = "text/plain;charset=utf-8")
    public String getWuRenJi(Integer type, String date, String s, String name) {
        testSixService.getWuRenJi(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---无人机 is ok---";
    }

    @ApiOperation("深圳大疆-无人机")
    @PostMapping(value = "/getShenZhenDaJiang", produces = "text/plain;charset=utf-8")
    public String getShenZhenDaJiang(Integer type, String date, String s, String name) {
        testSixService.getShenZhenDaJiang(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---深圳大疆-无人机 is ok---";
    }

    @ApiOperation("深圳大疆-无人机-关键词统计")
    @PostMapping(value = "/getShenZhenDaJiangTongJi", produces = "text/plain;charset=utf-8")
    public String getShenZhenDaJiangTongJi(Integer type, String date, String s, String name) {
        testSixService.getShenZhenDaJiangTongJi(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---深圳大疆-无人机-统计 is ok---";
    }

    @ApiOperation("浙江汉略网络科技有限公司")
    @PostMapping(value = "/getZheJiangShuangLue2", produces = "text/plain;charset=utf-8")
    public String getZheJiangShuangLue2(Integer type, String date, String s, String name) {
        testSixService.getZheJiangShuangLue2(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---浙江汉略网络科技有限公司_2 is ok---";
    }

    @ApiOperation("上海恒生聚源")
    @PostMapping(value = "/getShangHaiHengSheng", produces = "text/plain;charset=utf-8")
    public String getShangHaiHengSheng(Integer type, String date, String s, String name,String typeName) {
        testSixService.getShangHaiHengSheng(type, date, s, name,typeName);
        log.info("===============================数据运行结束===================================");
        return "---上海恒生聚源 is ok---";
    }

    @ApiOperation("上海恒生聚源-通过id")
    @PostMapping(value = "/getShangHaiHengShengById", produces = "text/plain;charset=utf-8")
    public String getShangHaiHengShengById(Integer type) {
        testSixService.getShangHaiHengShengById(type);
        log.info("===============================数据运行结束===================================");
        return "---上海恒生聚源-通过id is ok---";
    }

    @ApiOperation("上海恒生聚源-第三回合")
    @PostMapping(value = "/getShangHaiHengSheng3", produces = "text/plain;charset=utf-8")
    public String getShangHaiHengSheng3(Integer type, String date, String s, String name,Integer typeName) {
        testSixService.getShangHaiHengSheng3(type, date, s, name,typeName);
        log.info("===============================数据运行结束===================================");
        return "---上海恒生聚源-第三回合 is ok---";
    }

    @ApiOperation("走去重规则-数据导出")
    @PostMapping(value = "/getQuChong", produces = "text/plain;charset=utf-8")
    public String getQuChong() {
        testSixService.getQuChong();
        log.info("===============================数据运行结束===================================");
        return "---走去重规则-数据导出 is ok---";
    }
    @ApiOperation("多线程-runnable")
    @PostMapping(value = "/getRunnable", produces = "text/plain;charset=utf-8")
    public String getRunnable() {
        testSixService.getRunnable();
        log.info("===============================数据运行结束===================================");
        return "---多线程 is ok---";
    }

    @ApiOperation("奥的斯机电电梯有限公司-审批")
    @PostMapping(value = "/getAoDiSiJiDian", produces = "text/plain;charset=utf-8")
    public String getAoDiSiJiDian(Integer type, String date, String s, String name,Integer typeName) {
        testSixService.getAoDiSiJiDian(type, date, s, name,typeName);
        log.info("===============================数据运行结束===================================");
        return "---奥的斯机电电梯有限公司 is ok---";
    }

    @ApiOperation("奥的斯机电电梯有限公司-拟在建")
    @PostMapping(value = "/getAoDiSiJiDianNzj", produces = "text/plain;charset=utf-8")
    public String getAoDiSiJiDianNzj(Integer type, String date, String s, String name,Integer typeName) {
        testSixService.getAoDiSiJiDianNzj(type, date, s, name,typeName);
        log.info("===============================数据运行结束===================================");
        return "---奥的斯机电电梯有限公司-拟在建 is ok---";
    }

    @ApiOperation("奥的斯机电电梯有限公司-拟在建-接口调用")
    @PostMapping(value = "/getAoDiSiJiDianNzj2", produces = "text/plain;charset=utf-8")
    public String getAoDiSiJiDianNzj2(Integer type, String date, String s, String name,Integer typeName) {
        testSixService.getAoDiSiJiDianNzj2(type, date, s, name,typeName);
        log.info("===============================数据运行结束===================================");
        return "---奥的斯机电电梯有限公司-拟在建-接口调用 is ok---";
    }

    @ApiOperation("深圳大疆-无人机-4")
    @PostMapping(value = "/getShenZhenDaJiang4", produces = "text/plain;charset=utf-8")
    public String getShenZhenDaJiang4(Integer type, String date, String s, String name) {
        testSixService.getShenZhenDaJiang4(type, date, s, name);
        log.info("===============================数据运行结束===================================");
        return "---深圳大疆-无人机4 is ok---";
    }

    @ApiOperation("大金额获取标准字段")
    @PostMapping(value = "/getBiaozhun", produces = "text/plain;charset=utf-8")
    public String getBiaozhun(Integer type) {
        testSixService.getBiaozhun(type);
        log.info("===============================数据运行结束===================================");
        return "---大金额获取标准字段 is ok---";
    }



}
