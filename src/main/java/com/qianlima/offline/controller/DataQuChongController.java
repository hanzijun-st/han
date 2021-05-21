package com.qianlima.offline.controller;


import com.qianlima.offline.service.CleanService;
import com.qianlima.offline.service.ProjectRemoveDataService;
import com.qianlima.offline.service.aquchong.AliDataService;
import com.qianlima.offline.util.LogUtils;
import io.swagger.annotations.Api;
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

    @RequestMapping(value = "/getData", method = RequestMethod.GET)
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

}
