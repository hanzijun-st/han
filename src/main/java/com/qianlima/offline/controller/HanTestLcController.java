package com.qianlima.offline.controller;

import com.qianlima.offline.bean.LcDto;
import com.qianlima.offline.bean.LcVo;
import com.qianlima.offline.service.han.HanTestLcService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/han")
@Slf4j
@Api("han3.9")
public class HanTestLcController {
    @Autowired
    private HanTestLcService hanTestLcService;


    @ApiOperation("测试新创建controller")
    @PostMapping(value = "/testLc")
    @ResponseBody
    public List<LcVo> testLc(@RequestBody LcDto lcDto) {
        List<LcVo> lcVos = hanTestLcService.testLc(lcDto);
        return lcVos;
    }

    @ApiOperation("测试新创建controller2")
    @PostMapping(value = "/uptestLc")
    @ResponseBody
    public Boolean uptestLc(Long id) {
        return hanTestLcService.uptestLc(id);
    }




}