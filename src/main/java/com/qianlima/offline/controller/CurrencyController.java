package com.qianlima.offline.controller;

import com.qianlima.offline.bean.Params;
import com.qianlima.offline.service.han.CurrencyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 通用方法
 * Created by Administrator on 2021/1/14.
 */
@RestController
@RequestMapping("currency")
@Slf4j
@Api("通用controller")
public class CurrencyController {

    @Autowired
    private CurrencyService currencyService;

    /**
     * 一个关键词
     *
     * @param params
     * @return
     */
    @ApiOperation("一个关键词的通用 poc")
    @PostMapping("/start/getOne")
    public String getJdglOne(@RequestBody Params params) {

        currencyService.getOnePoc(params);
        return "---=========---";
    }

    @GetMapping("/start/getBdw")
    @ApiOperation("获取标的物的数据-type(1:迈瑞；2:ICT；3:医疗；4:没有侧重点)")
    public String getBdw(Integer type) {
        currencyService.getBdw(type);

        return "请求成功---成功获取标的物";
    }

    /**
     * 获取标的物
     *
     * @param type 1、迈瑞接口地址：http://47.104.4.12:5001/to_json_v3/
     *             2、[模型识别侧重“ICT行业”]：http://47.104.4.12:2022/inspect
     *             3、[模型识别侧重“医疗行业”]：http://47.104.4.12:2023/inspect
     *             4、[模型识别没有侧重点]：http://47.104.4.12:2024/inspect
     * @return
     */
    @GetMapping("/getNewBdw3")
    @ApiOperation("1.3的标的物方式-获取标的物的数据（1:迈瑞；2:ICT；3:医疗；4:没有侧重点；）")
    public String getNewBdw(Integer type) {
        currencyService.getNewBdw3(type);
        return "getNewBdw3请求成功---最新方式-获取标的物的数据";
    }

    @GetMapping("/getPiPeiHangYeBiaoQian")
    @ApiOperation("匹配行业标签--- 一级/二级")
    public String getPiPeiHangYeBiaoQian() {
        currencyService.getPiPeiHangYeBiaoQian();
        return "getNewBdw3请求成功---最新方式-获取标的物的数据";
    }

    @GetMapping("/getPiPeiHangYeBiaoQianById")
    @ApiOperation("匹配行业标签--- 一级/二级-通过id获取")
    public String getPiPeiHangYeBiaoQianById() {
        currencyService.getPiPeiHangYeBiaoQianById();
        return "---最新方式-获取标的物的数据-通过id获取";
    }

    @GetMapping("/getCrmByUserId")
    @ApiOperation("获取用户的指定字段-用户所有数据")
    public String getCrmByUserId() throws Exception {
        currencyService.getCrmByUserId();
        return "getCrmByUserId---获取用户的指定字段";
    }


    @GetMapping("/getCrmByUserIdToMonth")
    @ApiOperation("获取用户的指定字段-某个月")
    public String getCrmByUserIdToMonth() throws Exception {
        currencyService.getCrmByUserIdToMonth();
        return "getCrmByUserId---获取用户的指定字段-某个月";
    }

    @GetMapping("/getProName")
    @ApiOperation("通过id获取项目名称")
    public String getProName() throws Exception {
        currencyService.getProName();
        return "通过id获取项目名称 is ok";
    }


    @GetMapping("/testBj")
    @ApiOperation("比较两组数据不同")
    public String testBj() throws Exception {
        currencyService.testBj();
        return "比较数据结束 is ok";
    }

    @GetMapping("/getAreaByUnit")
    @ApiOperation("通过招标单位获取地区")
    public String getAreaByUnit() throws Exception {
        currencyService.getAreaByUnit();
        return "通过招标单位获取地区 is ok";
    }

    @GetMapping("/getAreaById")
    @ApiOperation("通过contentId获取地区")
    public String getAreaById() throws Exception {
        currencyService.getAreas();
        return "通过contentId获取地区 is ok";
    }

    @ApiOperation("查询单位的工商信息-天眼查")
    @PostMapping(value = "/getZhongXin", produces = "text/plain;charset=utf-8")
    public String getZhongXin() throws Exception{
        currencyService.getZhongXin();
        log.info("===============================数据运行结束===================================");
        return "---查询单位的工商信息 is ok---";
    }
}
