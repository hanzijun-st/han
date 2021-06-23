package com.qianlima.offline.schedule;

import com.qianlima.offline.service.CleanService;
import com.qianlima.offline.service.ProjectRemoveDataLsService;
import com.qianlima.offline.service.ProjectRemoveDataService;
import com.qianlima.offline.service.han.TestSixService;
import com.qianlima.offline.util.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


//@Component
@Slf4j
public class PocSchedule {

    @Autowired
    private TestSixService testSixService;
    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    private CleanService cleanService;

    @Autowired
    private ProjectRemoveDataService projectRemoveDataService;

    @Autowired
    private ProjectRemoveDataLsService lsService;

    /**
     * 浙江汉略-poc 定时任务
     */
    @Scheduled(cron = "0 15 4 * * ?")
    public void deleteCount() {
        try {
            log.info("浙江汉略poc任务执行开始- - -");
            testSixService.getZheJiangShuangLue(1, "20210617 TO 20210617", "1", "浙江汉略-17号");
        } catch (Exception e) {
            log.error("deleteCount,{}", e);
        }
    }

    /**
     * 规则二
     * 浙江汉略-数据清洗和去重 定时任务
     */
    @Scheduled(cron = "0 15 5 * * ?")
    public void dataQingXi_2() {
        try {
            log.info("数据清洗和去重定时,规则二任务开始- - -");
            List<String> contentIdList = new ArrayList<>();
            List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT content_id FROM  han_data_poc_17");
            if (mapList != null && mapList.size() > 0) {
                for (Map<String, Object> map : mapList) {
                    String contentid = map.get("content_id").toString();
                    contentIdList.add(contentid);
                }
            }

            if (contentIdList.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(10);
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
        } catch (Exception e) {
            log.error("deleteCount,{}", e);
        }
    }
    /**
     * 规则一
     * 浙江汉略-数据清洗和去重 定时任务
     */
    @Scheduled(cron = "0 35 6 * * ?")
    public void dataQingXi_1() {
        try {
            log.info("数据清洗和去重定时,规则一任务开始- - -");
            List<String> contentIdList = new ArrayList<>();
            List<Map<String, Object>> mapList =bdJdbcTemplate.queryForList("SELECT  content_id FROM  han_data_poc_17 WHERE link_phone !='' and link_phone is not null");
            if (mapList != null && mapList.size() > 0) {
                for (Map<String, Object> map : mapList) {
                    String contentid = map.get("content_id").toString();
                    contentIdList.add(contentid);
                }
            }

            if (contentIdList.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(10);
                List<Future> futureList = new ArrayList<>();

                futureList.add(executorService.submit(() -> {
                    for (String id : contentIdList) {
                        cleanService.getSJQX_ls(id);
                    }
                    try {
                        lsService.handleGetTempNum();
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
        } catch (Exception e) {
            log.error("deleteCount,{}", e);
        }
    }

}