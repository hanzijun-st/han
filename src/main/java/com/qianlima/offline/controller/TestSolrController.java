package com.qianlima.offline.controller;

import com.qianlima.offline.service.han.TestService;
import com.qianlima.offline.service.han.TestSolrService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@Slf4j
@Api("测试test")
public class TestSolrController {
    @Autowired
    private TestSolrService testSolrService;

    @ApiOperation("solr")
    @PostMapping(value = "/testSolr")
    public String testSolr() {
        testSolrService.testSolr();
        return "123 solr is ok";
    }
}