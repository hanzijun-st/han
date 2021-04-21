package com.qianlima.offline.controller;

import com.qianlima.offline.bean.Params;
import com.qianlima.offline.service.han.AoLinBaSiService;
import com.qianlima.offline.service.han.CurrencyService;
import com.qianlima.offline.service.han.Test39Service;
import com.qianlima.offline.service.han.TestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * hanzijun 接口---3月9号以后
 */
@RestController
@RequestMapping("/han")
@Slf4j
@Api("han3.9")
public class HanTest39Controller {

    @Autowired
    private Test39Service test39Service;


    @ApiOperation("测试新创建controller")
    @PostMapping(value = "/test",produces = "text/plain;charset=utf-8")
    public String test() {
        test39Service.test();
        log.info("===============================数据运行结束===================================");
        return "---han3.9项目启动正常---";
    }

    @ApiOperation("中软集团")
    @PostMapping("/getZhongRuan")
    //参数：progidStr取值---> 判断 0:0、1:全部、2:招标[0 TO 2]、3:3、4:[0 TO 3]、5:中标[3 OR progid:5]、6:[0 OR progid:3]
    public String getZhongRuan(Integer type,String date,String progidStr) {
        test39Service.getZhongRuan(type, date,progidStr);
        log.info("===============================数据运行结束===================================");
        return "---getZhongRuan is ok---";
    }

    @ApiOperation("中软集团-第二回合(测试接口)")
    @PostMapping(value = "/getZhongRuan2",produces = "text/plain;charset=utf-8")
    //参数：progidStr取值---> 判断 0:0、1:全部、2:招标[0 TO 2]、3:3、4:[0 TO 3]、5:中标[3 OR progid:5]、6:[0 OR progid:3]
    public String getZhongRuan2(Integer type,String date,String progidStr) {
        test39Service.getZhongRuan2(type, date,progidStr);
        log.info("===============================数据运行结束===================================");
        return "---中软集团第二回合 接口运行结束---";
    }

    @ApiOperation("阿里标题调查")
    @PostMapping(value = "/getAliBiaoti",produces = "text/plain;charset=utf-8")
    public String getAliBiaoti(Integer type,String date,String progidStr) {
        test39Service.getAliBiaoti(type, date,progidStr);
        log.info("===============================数据运行结束===================================");
        return "---阿里标题调查 接口运行结束---";
    }

    @ApiOperation("测试获取solr中的doc为map结构")
    @PostMapping(value = "/getTest",produces = "text/plain;charset=utf-8")
    public String getTest(Integer type,String date,String progidStr) {
        test39Service.getTest(type, date,progidStr);
        log.info("===============================数据运行结束===================================");
        return "---测试获取solr中的doc为map结构 接口运行结束---";
    }

    @ApiOperation("毕马威中国-allcontent")
    @PostMapping(value = "/getBiMaWei",produces = "text/plain;charset=utf-8")
    public String getBiMaWei(Integer type,String date,String progidStr) {
        test39Service.getBiMaWei(type, date,progidStr);
        log.info("===============================数据运行结束===================================");
        return "---毕马威中国全文检索 接口运行结束---";
    }

    @ApiOperation("毕马威中国-title")
    @PostMapping(value = "/getBiMaWeiByTitle",produces = "text/plain;charset=utf-8")
    public String getBiMaWeiByTitle(Integer type,String date,String progidStr) {
        test39Service.getBiMaWeiByTitle(type, date,progidStr);
        log.info("===============================数据运行结束===================================");
        return "---毕马威中国-标题检索 接口运行结束---";
    }

    @ApiOperation("毕马威中国-规则三")
    @PostMapping(value = "/getBiMaWeiByTitle_3",produces = "text/plain;charset=utf-8")
    public String getBiMaWeiByTitle_3(Integer type,String date,String progidStr) {
        test39Service.getBiMaWeiByTitle_3(type, date,progidStr);
        log.info("===============================数据运行结束===================================");
        return "---毕马威中国-规则三 接口运行结束---";
    }
    @ApiOperation("毕马威中国-规则三-屏蔽词(金融行业)")
    @PostMapping(value = "/getBiMaWeiByTitle_3_1",produces = "text/plain;charset=utf-8")
    public String getBiMaWeiByTitle_3_1(Integer type,String date,String progidStr) {
        test39Service.getBiMaWeiByTitle_3_1(type, date,progidStr);
        log.info("===============================数据运行结束===================================");
        return "---毕马威中国-规则三-屏蔽词(金融行业) 接口运行结束---";
    }


    @ApiOperation("陕西星宝莱厨房设备有限公司")
    @PostMapping(value = "/getShanXiXingBaoLai",produces = "text/plain;charset=utf-8")
    public String getShanXiXingBaoLai(Integer type,String date,String progidStr) {
        test39Service.getShanXiXingBaoLai(type, date,progidStr);
        log.info("===============================数据运行结束===================================");
        return "---陕西星宝莱厨房设备有限公司 接口运行结束---";
    }
    @ApiOperation("陕西星宝莱厨房设备有限公司-第一回合2")
    @PostMapping(value = "/getShanXiXingBaoLai2",produces = "text/plain;charset=utf-8")
    public String getShanXiXingBaoLai2(Integer type,String date,String progidStr) {
        //参数：progidStr取值---> 判断 0:0、1:全部、2:招标[0 TO 2]、3:3、4:[0 TO 3]、5:中标[3 OR progid:5]、6:[0 OR progid:3]
        test39Service.getShanXiXingBaoLai2(type, date,progidStr);
        log.info("===============================数据运行结束===================================");
        return "---陕西星宝莱厨房设备有限公司-第一回合2 接口运行结束---";
    }

    @ApiOperation("陕西星宝莱厨房设备有限公司-第二回合")
    @PostMapping(value = "/getShanXiXingBaoLai2_1",produces = "text/plain;charset=utf-8")
    public String getShanXiXingBaoLai2_1(Integer type,String date,String progidStr) {
        test39Service.getShanXiXingBaoLai2_1(type, date,progidStr);
        log.info("===============================数据运行结束===================================");
        return "---陕西星宝莱厨房设备有限公司-第二回合 接口运行结束---";
    }

    /**
     *  三月雨
     * @param type 是否存库
     * @param date 日期
     * @param str  是否打印日志
     * @return
     */
    @ApiOperation("北京三月雨文化传播有限责任公司")
    @PostMapping(value = "/getBeiJingSanYue",produces = "text/plain;charset=utf-8")
    public String getBeiJingSanYue(Integer type,String date,String str) {
        test39Service.getBeiJingSanYue(type, date,str);
        log.info("===============================数据运行结束===================================");
        return "---北京三月雨文化传播有限责任公司 接口运行结束---";
    }

    @ApiOperation("卫卫阿尔-石家庄")
    @PostMapping(value = "/getWwAer",produces = "text/plain;charset=utf-8")
    public String getWwAer(Integer type,String date,String str) {
        test39Service.getWwAer(type, date,str);
        log.info("===============================数据运行结束===================================");
        return "---卫卫阿尔-石家庄 接口运行结束---";
    }
    @ApiOperation("卫卫阿尔2-石家庄-第二回合")
    @PostMapping(value = "/getWwAer2",produces = "text/plain;charset=utf-8")
    public String getWwAer2(Integer type,String date,String s) {
        test39Service.getWwAer2(type, date,s);
        log.info("===============================数据运行结束===================================");
        return "---卫卫阿尔2-石家庄-第二回合 接口运行结束---";
    }

    @ApiOperation("卡瓦盛邦")
    @PostMapping(value = "/getKaWaSb",produces = "text/plain;charset=utf-8")
    public String getKaWaSb(Integer type,String date,String str) {
        test39Service.getKaWaSb(type, date,str);
        log.info("===============================数据运行结束===================================");
        return "---卡瓦盛邦 接口运行结束---";
    }

    @ApiOperation("临时数据")
    @PostMapping(value = "/getLinS",produces = "text/plain;charset=utf-8")
    public String getLinS(Integer type,String date,String str) {
        test39Service.getLinS(type, date,str);
        log.info("===============================数据运行结束===================================");
        return "--- 接口运行结束---";
    }
}
