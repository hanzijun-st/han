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

    @ApiOperation("中软集团-第二回合")
    @PostMapping(value = "/getZhongRuan2",produces = "text/plain;charset=utf-8")
    //参数：progidStr取值---> 判断 0:0、1:全部、2:招标[0 TO 2]、3:3、4:[0 TO 3]、5:中标[3 OR progid:5]、6:[0 OR progid:3]
    public String getZhongRuan2(Integer type,String date,String progidStr) {
        test39Service.getZhongRuan2(type, date,progidStr);
        log.info("===============================数据运行结束===================================");
        return "---中软集团第二回合 接口运行结束---";
    }
}
