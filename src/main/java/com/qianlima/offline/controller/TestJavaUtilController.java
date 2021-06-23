package com.qianlima.offline.controller;

import com.qianlima.offline.util.AreaCodeUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/card")
@Slf4j
@Api("testUtil")
@CrossOrigin
public class TestJavaUtilController {

    @ApiOperation("验证身份信息是否有效")
    @GetMapping(value = "/yzIdCard", produces = "text/plain;charset=utf-8")
    public String testLc(String iDCard) {
        String s = AreaCodeUtil.resultStr(iDCard);
        return s;
    }
}