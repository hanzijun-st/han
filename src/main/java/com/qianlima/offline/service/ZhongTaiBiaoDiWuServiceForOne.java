package com.qianlima.offline.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.offline.bean.ConstantBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Slf4j
public class ZhongTaiBiaoDiWuServiceForOne {

    private static final String CHECK_SQL = "select status from phpcms_content where contentid = ?";

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;


    public void getSolrAllField() {
        ExecutorService executorService1 = Executors.newFixedThreadPool(64);
        List<Future> futureList1 = new ArrayList<>();

        List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT content_id FROM lala_data where code is null or  code = ''  ");
        for (Map<String, Object> maps : mapList) {

            String contentId = maps.get("content_id") != null ? maps.get("content_id").toString() : "";

            futureList1.add(executorService1.submit(() -> {
                try {



                    getAllZhongTaiBiaoDIWu(contentId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));

        }

        for (Future future1 : futureList1) {
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

    }

    public String getAllZhongTaiBiaoDIWu(String contentId) throws Exception{


        return "";
    }



    private boolean checkPHPContent(String contentid){
        boolean flag = false;
        List<Map<String, Object>> maps = gwJdbcTemplate.queryForList(CHECK_SQL, contentid);
        if (maps != null && maps.size() > 0 ){
            for (Map<String, Object> map : maps) {
                String status = map.get("status").toString();
                if ("99".equals(status)){
                    flag = true;
                }
            }
        }
        return flag;
    }



}
