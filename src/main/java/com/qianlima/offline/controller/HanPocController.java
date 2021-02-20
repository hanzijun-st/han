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

    @ApiOperation("--补录腾讯--")
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
    @GetMapping("/ids")
    @ResponseBody
    public String toIds() throws Exception{
        testTencentService.toIds();
        return "ids is ok";
    }
}
