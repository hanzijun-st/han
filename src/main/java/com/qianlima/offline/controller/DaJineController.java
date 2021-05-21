package com.qianlima.offline.controller;

import com.qianlima.offline.middleground.ICTLingYuService;
import com.qianlima.offline.replenish.ZhaoBiaoPhoneService;
import com.qianlima.offline.replenish.ZhongBiaoPhoneService;
import com.qianlima.offline.service.BiaoDiWuService;
import com.qianlima.offline.service.DeliveryPocService;
import com.qianlima.offline.service.han.CurrencyService;
import com.qianlima.offline.service.offline.DaHuaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("dajine")
@Slf4j
public class DaJineController {

    @Autowired
    private ICTLingYuService ictLingYuService;

    @Autowired
    private CurrencyService currencyService;

    //大金额
    @RequestMapping(value = "/start/dajine", method = RequestMethod.GET)
    public String getDaJineSolrAllField() throws Exception {
        ictLingYuService.byTitleAndZhaoLY();
        return "DaJine Normal的数据处理完毕啦啦啦啦啦";
    }

    /**
     * 获取线上大金额需要修改的所有
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getDaJinEdatas", method = RequestMethod.GET)
    public String getDaJinEdatas() throws Exception {
        currencyService.getDaJinEdatas();
        return "DaJine线上数据获取成功";
    }
}
