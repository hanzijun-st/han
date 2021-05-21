package com.qianlima.offline.controller;

import com.qianlima.offline.util.POIUtil;
import com.qianlima.offline.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/excel")
@Slf4j
@Api("read excel")
public class TestReadExcelController {


    @ApiOperation("读取本地Excel")
    @PostMapping(value = "/readExcel",produces = "text/plain;charset=utf-8")
    public String getZheJiangWenZhou(MultipartFile file) throws Exception{
        List<String[]> list = POIUtil.readExcel(file);
        for (String[] s : list) {
            String s1 = StrUtil.listToStr(s);
            System.out.println(s1);
        }

        log.info("===============================数据运行结束===================================");
        return "---file is ok---";
    }
}