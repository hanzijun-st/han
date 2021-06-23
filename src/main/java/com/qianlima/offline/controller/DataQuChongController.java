package com.qianlima.offline.controller;


import com.qianlima.offline.service.CleanService;
import com.qianlima.offline.service.OptimizationDataService;
import com.qianlima.offline.service.OptimizationDataService02;
import com.qianlima.offline.service.ProjectRemoveDataService;
import com.qianlima.offline.service.aquchong.AliDataService;
import com.qianlima.offline.util.LogUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.List;
import java.util.concurrent.Executors;


/**
 * 去重规则
 */

@RestController
@RequestMapping("/data")
@Api("去重规则")
@Slf4j
public class DataQuChongController {

    @Autowired
    private AliDataService aliDataService;

    @Autowired
    private CleanService cleanService;

    @Autowired
    private ProjectRemoveDataService projectRemoveDataService;

    @Autowired
    private OptimizationDataService optimizationDataService;//先运行

    @Autowired
    private OptimizationDataService02 optimizationDataService02;

    @RequestMapping(value = "/getData", method = RequestMethod.GET)
    @ApiOperation("去重接口")
    public void makeFileToServer() throws Exception{

        List<String> contentIdList = LogUtils.readRule("quchongIds");

        ExecutorService executorService = Executors.newFixedThreadPool(8);
        List<Future> futureList = new ArrayList<>();

        futureList.add(executorService.submit(() -> {
            for (String id : contentIdList) {
                cleanService.getSJQX(id);
            }
            try {
                projectRemoveDataService.handleGetTempNum();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        for (Future future : futureList) {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();
    }

    /**
     * 优选接口
     * @throws Exception
     */
    @ApiOperation("优选接口")
    @RequestMapping(value = "/updateData", method = RequestMethod.GET)
    public void update() throws Exception {

        ExecutorService executorService = Executors.newFixedThreadPool(8);
        List<Future> futureList = new ArrayList<>();

        futureList.add(executorService.submit(() -> {
            try {
                optimizationDataService.handleKillRepeat();
            } catch (Exception e) {

            }

            optimizationDataService02.handle();
        }));

        for (Future future : futureList) {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();



    }

}
