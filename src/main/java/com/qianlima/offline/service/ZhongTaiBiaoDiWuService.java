package com.qianlima.offline.service;

import com.qianlima.offline.util.FbsContentSolr;
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
    private YiLiaoBiaoDiWuService yiLiaoBiaoDiWuService;

    @Autowired
    private NewBiaoDiWuService newBiaoDiWuService;

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
    public void getSolrAllField2(Integer type){

        ExecutorService executorService1 = Executors.newFixedThreadPool(4);
        List<Future> futureList = new ArrayList<>();
        //contentid
        List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT id,contentid FROM han_contentid");
        for (Map<String, Object> mapData : mapList) {
            futureList.add(executorService1.submit(() -> {
                yiLiaoBiaoDiWuService.handleForYiLiao(Long.valueOf(mapData.get("contentid").toString()),type);
                log.info("新标的物方法--->:{}",mapData.get("contentid").toString()+"======="+mapData.get("id").toString());
            }));
        }
        for (Future future1 : futureList) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();
        log.info("---------------===============================新标的物方法运行结束==================================");
    }


    public void getAllZhongTaiBiaoDIWu(String contentId,Integer type) throws Exception{

    }

}
