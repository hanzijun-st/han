package com.qianlima.offline.controller;

import com.qianlima.offline.service.han.HanPocService;
import com.qianlima.offline.service.han.TestMongoService;
import com.qianlima.offline.service.han.TestTencentService;
import com.qianlima.offline.util.MapUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @Autowired
    private TestMongoService testMongoService;

    @Autowired
    private TestTencentService testTencentService;

    @ApiOperation("--新加POC样式模板--")
    @PostMapping("/getNew")
    public String getNew(Integer type,String date){
        hanPocService.getNew(0,date);
        return "---getNew---";
    }

    @ApiOperation("--mongoDB测试获取数据--")
    @PostMapping("/getMongo")
    public String getMongo(String str){
        testMongoService.getMongo(str);
        return "---getMongo---";
    }

    @ApiOperation("----腾讯细分类型---")
    @PostMapping("/saveTencent")
    public String saveTencent(String str){
        testTencentService.saveTencent();
        return "---saveTencent---";
    }

    @ApiOperation("--浙江纽若思医疗科技有限公司--")
    @PostMapping("/getZheJiangNiuRuoSi")
    public String getZheJiangNiuRuoSi(String date,Integer type){
        hanPocService.getZheJiangNiuRuoSi(date,type);
        return "---getZheJiangNiuRuoSi---";
    }

    @ApiOperation("通用---直接查询solr(12),参数：tiaojian为solr查询条件，date为年")
    @PostMapping("/getSolrByMonth")
    @ResponseBody
    public Map<String,Object> getSolrByMonth(String tiaojian,String date){
        Map<String, Object> solr = hanPocService.getSolr(tiaojian, date);
        System.out.println("时间："+ MapUtil.getMapToKeyOrValue(solr,1)+"---"+MapUtil.getMapToKeyOrValue(solr,2));
        return solr;
    }

    @ApiOperation("json")
    @PostMapping("/toJson")
    @ResponseBody
    public String toJson(){
        testTencentService.jsonTo();
        return "json is ok";
    }
    @ApiOperation("通过单独contentid/id 走默认的自提获取数据")
    @GetMapping(value="/ids",produces = "text/plain;charset=utf-8")
    public String toIds() throws Exception{
        testTencentService.toIds();
        log.info("---通过id获取基础字段接口运行结束---");
        return "ids is ok";
    }

    @ApiOperation("新的地址获取方式---暂时调用中台服务接口")
    @GetMapping("/getNewAddress")
    @ResponseBody
    public String getNewAddress() throws Exception{
        testTencentService.getNewAddress();
        return "getNewAddress is ok";
    }

    @ApiOperation("新的地址获取方式---缺失数据")
    @GetMapping("/getNewAddressToQs")
    @ResponseBody
    public String getNewAddressToQs() throws Exception{
        testTencentService.getNewAddressToQs();
        return "getNewAddress is ok";
    }

    @ApiOperation("新的地址获取方式---通用方法（contentId）")
    @PostMapping("/getNewAddressByContentId")
    @ResponseBody
    public Map getNewAddressByContentId(String contentId) throws Exception{
        Map newAddressByContentId = testTencentService.getNewAddressByContentId(contentId);
        return newAddressByContentId;
    }

    @ApiOperation("信立方-临时数据进行导出")
    @PostMapping("/getLinShi")
    public String getLinShi(String date) throws Exception{
        testTencentService.getLinShi(date);
        return "getLinShi is ok";
    }

    @ApiOperation("凯思轩达医疗")
    @PostMapping("/getKaisixuanda")
    public String getKaisixuanda(String date,Integer type) throws Exception{
        testTencentService.getKaisixuanda(date,type);
        return "getKaisixuanda is ok";
    }
    @ApiOperation("凯思轩达医疗2")
    @PostMapping("/getKaisixuanda2")
    public String getKaisixuanda2(String date,Integer type) throws Exception{
        testTencentService.getKaisixuanda2(date,type);
        return "getKaisixuanda2 is ok";
    }
    @ApiOperation("同方威视")
    @PostMapping("/getTongfangWeiShi")
    public String getTongfangWeiShi(String date,Integer type) throws Exception{
        testTencentService.getTongfangWeiShi(date,type);
        return "getTongfangWeiShi is ok";
    }
    @ApiOperation("同方威视-2")
    @PostMapping("/getTongfangWeiShi2")
    public String getTongfangWeiShi2(String date,Integer type) throws Exception{
        testTencentService.getTongfangWeiShi2(date,type);
        return "getTongfangWeiShi2 is ok";
    }
    @ApiOperation("测试批量")
    @PostMapping("/getCs")
    public String getCs(String date,Integer type) throws Exception{
        testTencentService.getKaisixuandaCs(date,type);
        return "getKaisixuandaCs is ok";
    }

    @ApiOperation("大金额-输出常用字段")
    @PostMapping("/getDajinE")
    public String getDajinE() throws Exception{
        testTencentService.getDajinE();
        return "getDajinE is ok";
    }
}
