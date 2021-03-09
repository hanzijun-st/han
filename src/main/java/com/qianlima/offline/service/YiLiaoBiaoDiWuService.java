package com.qianlima.offline.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.extract.TargetService;
import com.qianlima.offline.rule02.BiaoDiWuRule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class YiLiaoBiaoDiWuService {

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;


    // 标的物匹配到的关键词
    //private String[] keywords = {};
    private String[] keywords = {"鼻咽喉","摄像系统","超声","摄像平台","支气管","输尿管","胃肠","宫腔","腹腔","呼吸","膀胱","消化","胆道","清洗消毒","整体手术室","影像装置","图像处理","摄像头","监视器","保养装置","光源","台车","主机","显示器","适配器"};


    private String SQL = "insert into han_biaodiwu_new(infoId, sum, sum_unit, keyword, serial_number, name, brand, model, " +
            "number, number_unit, price, price_unit, total_price, total_price_unit, configuration, type) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


    public void getSolrAllField() throws IOException {

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        List<Future> futureList = new ArrayList<>();

//        List<String> ids = LogUtils.readRule("smf");

        List<Map<String, Object>> maps = bdJdbcTemplate.queryForList("SELECT content_id FROM loiloi_data where id <= 10000");
        for (Map<String, Object> resultMap : maps) {

            String contentId = resultMap.get("content_id") != null ? resultMap.get("content_id").toString() : "";

            futureList.add(executorService.submit(() -> {
                try {
                   // handleForYiLiao(Long.valueOf(contentId),type);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));

        }

//        for (String id : ids) {
//            futureList.add(executorService.submit(() -> {
//                try {
//                    handleForYiLiao(Long.valueOf(id));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }));
//        }

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

//            1、迈瑞接口地址：http://47.104.4.12:5001/to_json_v3/﻿
//            2、[模型识别侧重“ICT行业”]：http://47.104.4.12:2022/inspect﻿
//            3、[模型识别侧重“医疗行业”]：http://47.104.4.12:2023/inspect﻿
//            4、[模型识别没有侧重点]：http://47.104.4.12:2024/inspect

    AtomicInteger atomicInteger = new AtomicInteger(0);

//    public static void main(String[] args) {
//        String result = TargetService.extract(Long.valueOf(211666351),"http://47.104.4.12:2022/inspect");
//        System.out.println(result);
//
//    }

    public void handleForYiLiao(Long contentId,Integer type){
        JSONObject jsonObject = null;
        try{
            String url = "";
            for (BiaoDiWuRule value : BiaoDiWuRule.values()) {
                if (value.getValue().intValue() == type){
                    url = value.getName();
                }
            }
            String result = TargetService.extract(contentId,url);
            int total = atomicInteger.addAndGet(1);
            log.info("游标获取用户数据，本次获取了size：{} 条", total);
            if (StringUtils.isNotBlank(result)){
                jsonObject = JSONObject.parseObject(result);
                if (jsonObject != null && jsonObject.get("data") != null){
                    JSONObject dataObject = (JSONObject) jsonObject.get("data");
                    if (dataObject.containsKey("content_target")){
                        JSONObject resultObject = dataObject.getJSONObject("content_target");
                        saveBiaoDiWuToMysql(resultObject, contentId, "正文");
                    }
                    if (dataObject.get("attachment_targets") != null){
                        JSONArray jsonArray = dataObject.getJSONArray("attachment_targets");
                        if (jsonArray != null ){
                            for (int i = 0; i < jsonArray.size(); i++) {
                                JSONObject resultObject = jsonArray.getJSONObject(i);
                                Set<String> docNames = resultObject.keySet();
                                if (docNames != null && docNames.size() > 0){
                                    for (String docName : docNames) {
                                        JSONObject object = resultObject.getJSONObject(docName);
                                        saveBiaoDiWuToMysql(object, contentId, "附件");
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            log.error("调用标的物接口异常:{}", e);
        }
//        String code = jsonObject.get("code") != null ? jsonObject.get("code").toString() : "";
//        String message = jsonObject.get("message") != null ? jsonObject.get("message").toString() : "";
//
//        if ("400".equals(code) || "500".equals(code) || "501".equals(code) || "502".equals(code)) {
//            bdJdbcTemplate.update("update loiloi_data set code = ?,keyword = ? where content_id = ?",code,message,contentId.toString());
//            log.error("contentId:{} 调用标的物解析接口异常, 对应的状态码 code ：{} ", contentId, code);
//        }
    }

    private void saveBiaoDiWuToMysql(JSONObject resultObject, Long contentId, String type) {
        if (resultObject != null && resultObject.containsKey("target_details")){
            String sum = resultObject.getString("sum");
            String sum_unit = resultObject.getString("sum_unit");
            JSONArray targetDetails = resultObject.getJSONArray("target_details");
            if (targetDetails != null && targetDetails.size() > 0){
                for (int i = 0; i < targetDetails.size(); i++) {
                    String serial_number = "";
                    String name = "";
                    String brand = "";
                    String model = "";
                    String number = "";
                    String number_unit = "";
                    String price = "";
                    String price_unit = "";
                    String total_price = "";
                    String total_price_unit = "";
                    String configuration = "";
                    String keyword = "";
                    JSONObject finalObject = targetDetails.getJSONObject(i);
                    if (finalObject != null){
                        serial_number = finalObject.getString("serial_number");
                        name = finalObject.getString("name");
                        brand = finalObject.getString("brand");
                        model = finalObject.getString("model");
                        number = finalObject.getString("number");
                        number_unit = finalObject.getString("number_unit");
                        price = finalObject.getString("price");
                        price_unit = finalObject.getString("price_unit");
                        total_price = finalObject.getString("total_price");
                        total_price_unit = finalObject.getString("total_price_unit");
                        JSONArray configurations = finalObject.getJSONArray("configurations");
                        if (configurations != null && configurations.size() > 0){
                            for (int j = 0; j < configurations.size(); j++) {
                                JSONObject jsonObject1 = configurations.getJSONObject(j);
                                String key = jsonObject1.getString("key");
                                String value = jsonObject1.getString("value");
                                configuration += key + "：" + value + "：";
                            }
                        }
                        if (StringUtils.isNotBlank(configuration)){
                            configuration = configuration.substring(0, configuration.length() - 1);
                        }
                        // 进行匹配关键词操作
                        if (keywords != null && keywords.length > 0){
                            String allField = name + "&" + brand + "&" + model + "&" + configuration;
                            for (String key : keywords) {
                                if (allField.toUpperCase().contains(key.toUpperCase())){
                                    keyword += key + "，";
                                }
                            }
                            if (StringUtils.isNotBlank(keyword)){
                                keyword = keyword.substring(0, keyword.length() - 1);
                            }
                        }
                    }
                    // 进行数据库保存操作
                    bdJdbcTemplate.update(SQL, contentId, sum, sum_unit, keyword, serial_number, name, brand, model, number, number_unit, price, price_unit, total_price, total_price_unit, configuration, type);
                    log.info("contentId:{} ==== 获取标的物解析表成功!!!!", contentId);
                }
            }
        }
    }



}
