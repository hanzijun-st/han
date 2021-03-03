package com.qianlima.offline.service;

import com.qianlima.offline.util.CollectionUtils;
import com.qianlima.offline.util.ContentSolr;
import com.qianlima.offline.util.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Slf4j
public class ZhongTaiBiaoDiWuService {


    private static final String UPDATA_SQL_01 = "INSERT INTO han_biaodiwu (contentid, serialNumber, name, brand, model, number, numberUnit, price, priceUnit, totalPrice, totalPriceUnit) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String CHECK_SQL = "select status from phpcms_content where contentid = ?";


    @Autowired
    private ContentSolr contentSolr;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;

    public void getSolrAllField() throws IOException {

        ExecutorService executorService = Executors.newFixedThreadPool(80);
        List<Future> futureList = new ArrayList<>();

        List<String> ids = LogUtils.readRule("smf");

//        List<Map<String, Object>> maps = bdJdbcTemplate.queryForList("SELECT contentid FROM loiloi_biaodiwu GROUP BY contentid");

        for (String id : ids) {
            futureList.add(executorService.submit(() -> {
                try {
                    getAllZhongTaiBiaoDIWu(id,1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }

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
    public void getSolrAllField2(){

        ExecutorService executorService = Executors.newFixedThreadPool(80);
        List<Future> futureList = new ArrayList<>();

        Set<String> ids = new HashSet<>();
        List<Map<String, Object>> maps = bdJdbcTemplate.queryForList("SELECT contentid FROM han_contentid");
        if (!CollectionUtils.isEmpty(maps)){
            for (Map<String, Object> map : maps) {
                ids.add(map.get("contentid").toString());
            }
        }
        for (String id : ids) {
            futureList.add(executorService.submit(() -> {
                try {
                    getAllZhongTaiBiaoDIWu(id,1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }

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
        System.out.println("--------------------------------标的物查询结束---------------------------------------");
    }


    public void getAllZhongTaiBiaoDIWu(String contentId,Integer type) throws Exception{

    }

}
