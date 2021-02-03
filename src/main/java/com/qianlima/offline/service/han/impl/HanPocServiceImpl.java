package com.qianlima.offline.service.han.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.extract.target.TargetExtractService;
import com.qianlima.offline.bean.ConstantBean;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.service.CusDataFieldService;
import com.qianlima.offline.service.han.HanPocService;
import com.qianlima.offline.util.ContentSolr;
import com.qianlima.offline.util.LogUtils;
import com.qianlima.offline.util.StrUtil;
import com.qianlima.offline.util.UpdateContentSolr;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 新接口
 */
@Service
@Slf4j
public class HanPocServiceImpl implements HanPocService {
    @Autowired
    private ContentSolr contentSolr;//solr查询接口

    @Autowired
    private UpdateContentSolr updateSolr;//solr查询接口

    @Autowired
    private CusDataFieldService cusDataFieldService;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;


    //mysql数据库中插入数据
    public static final String INSERT_ZT_RESULT_HAN = "INSERT INTO han_data (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time,is_electronic,code,isfile,keyword_term) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String UPDATA_SQL_01 = "INSERT INTO han_biaodiwu (contentid, serialNumber, name, brand, model, number, numberUnit, price, priceUnit, totalPrice, totalPriceUnit) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    @Override
    public void getNew(Integer type,String date) {
        //ExecutorService executorService1 = Executors.newFixedThreadPool(32);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
       // List<Future> futureList1 = new ArrayList<>();

        try {
            //关键字
            List<String> keyWords = LogUtils.readRule("keyWords");
            for (String keyWord : keyWords) {
                List<NoticeMQ> mqEntities = updateSolr.companyResultsBaoXian("yyyymmdd:["+ date + "] AND (progid:[0 TO 3] OR progid:5) AND allcontent:\"" + keyWord + "\"",keyWord, 1);
                log.info( "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                listAll.add(data);
                                data.setKeyword(keyWord);
                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                    list.add(data);
                                    dataMap.put(data.getContentid().toString(), "0");
                                }
                            }
                        }
                    }
                }
            }
            log.info("全部数据量：" + listAll.size());
            log.info("去重之后的数据量：" + list.size());
            log.info("==========================");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() == 1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(32);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSave(content)));
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
            log.info("数据库保存成功：-->{}",StrUtil.getSaveDataStr());
        }

        log.info("当前接口执行完毕：-->{}",StrUtil.getPutStr());
    }

    @Override
    public void getZheJiangNiuRuoSi(String date,Integer type) {

        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();

        try {
            //关键字a
            List<String> keyWords = LogUtils.readRule("keyWords");
            for (String keyWord : keyWords) {
                List<NoticeMQ> mqEntities = updateSolr.companyResultsBaoXian("yyyymmdd:["+ date + "] AND (progid:3 OR progid:5) AND allcontent:\"" + keyWord + "\"",keyWord, 1);
                log.info(keyWord + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                listAll.add(data);
                                data.setKeyword(keyWord);
                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                    list.add(data);
                                    dataMap.put(data.getContentid().toString(), "0");
                                }
                            }
                        }
                    }
                }
            }

            //关键字b
            List<String> keyWordsB = LogUtils.readRule("keyWordsB");
            for (String keyWord : keyWordsB) {
                List<NoticeMQ> mqEntities = updateSolr.companyResultsBaoXian("yyyymmdd:["+ date + "] AND (progid:3 OR progid:5) AND title:\"" + keyWord + "\"",keyWord, 1);
                log.info( "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                listAll.add(data);
                                data.setKeyword(keyWord);
                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                    list.add(data);
                                    dataMap.put(data.getContentid().toString(), "0");
                                }
                            }
                        }
                    }
                }
            }
            log.info("全部数据量：" + listAll.size());
            log.info("去重之后的数据量：" + list.size());
            log.info("==========================");


            ArrayList<String> arrayList = new ArrayList<>();
            for (String key : keyWords) {
                arrayList.add(key);
            }
            for (String key : keyWordsB) {
                arrayList.add(key);
            }

            for (String str : arrayList) {
                int total = 0;
                for (NoticeMQ noticeMQ : list) {
                    String keyword = noticeMQ.getKeyword();
                    if (keyword.equals(str)) {
                        total++;
                    }
                }
                if (total == 0) {
                    continue;
                }
                System.out.println(str + ": " + total);
            }
            System.out.println("全部数据量：" + listAll.size());
            System.out.println("去重之后的数据量：" + list.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
        /*if (list !=null && list.size() >0){
            for (NoticeMQ noticeMQ : list) {
                try {
                    getAllZhongTaiBiaoDIWu(noticeMQ.getContentid().toString());
                } catch (Exception e) {

                }
            }
        }*/
        //如果参数为1,则进行存表
        if (type.intValue() == 1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(32);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSave(content)));
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
            log.info("数据库保存成功：-->{}",StrUtil.getSaveDataStr());
        }

        log.info("当前接口执行完毕：-->{}",StrUtil.getPutStr());
    }

    @Override
    public Map<String, Object> getSolr(String tiaojian,String date) {
        return updateSolr.getSolr(tiaojian, date);
    }

    //调用中台数据，进行处理
    private void getZhongTaiDatasAndSave(NoticeMQ noticeMQ) {
        boolean b = cusDataFieldService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }

        //全部自提，不需要正文
        Map<String, Object> resultMap = cusDataFieldService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            saveIntoMysql(resultMap,INSERT_ZT_RESULT_HAN);
            log.info("数据存库content_id--->{}",resultMap.get("content_id").toString());
        }
    }

    //存储数据库
    public void saveIntoMysql(Map<String, Object> map ,String table){
        bdJdbcTemplate.update(table,map.get("task_id"), map.get("keyword"), map.get("content_id"), map.get("title"),
                map.get("content"), map.get("province"), map.get("city"), map.get("country"), map.get("url"), map.get("baiLian_budget"),
                map.get("baiLian_amount_unit"), map.get("xmNumber"), map.get("bidding_type"), map.get("progid"), map.get("zhao_biao_unit"),
                map.get("relation_name"), map.get("relation_way"), map.get("agent_unit"), map.get("agent_relation_ame"),
                map.get("agent_relation_way"), map.get("zhong_biao_unit"), map.get("link_man"), map.get("link_phone"),
                map.get("registration_begin_time"), map.get("registration_end_time"), map.get("biding_acquire_time"),
                map.get("biding_end_time"), map.get("tender_begin_time"), map.get("tender_end_time"), map.get("update_time"),
                map.get("type"), map.get("bidder"), map.get("notice_types"), map.get("open_biding_time"), map.get("is_electronic"),
                map.get("code"), map.get("isfile"), map.get("keyword_term"));
    }

    public void getAllZhongTaiBiaoDIWu(String contentId) throws Exception{

        List<Map<String, Object>> contentList = gwJdbcTemplate.queryForList(ConstantBean.SELECT_ITEM_CONTENT_BY_CONTENTID, contentId);
        if (contentList == null && contentList.size() == 0){
            return;
        }
        String content = contentList.get(0).get("content").toString();
        String target = "";
        if (StringUtils.isNotBlank(content)){
            try{
                target = TargetExtractService.getTargetResult("http://47.104.4.12:5001/to_json_v3/", content);
            } catch (Exception e){
                log.error("contentId:{}==========", contentId);
            }

            if (StringUtils.isNotBlank(target)){
                JSONObject targetObject = JSONObject.parseObject(target);
                if (targetObject.containsKey("targetDetails")){
                    JSONArray targetDetails = (JSONArray) targetObject.get("targetDetails");
                    for (Object targetDetail : targetDetails) {
                        String detail = targetDetail.toString();
                        Map detailMap = JSON.parseObject(detail, Map.class);
                        String serialNumber = ""; //标的物序号
                        String name = ""; //名称
                        String brand = ""; //品牌
                        String model = ""; //型号
                        String number = ""; //数量
                        String numberUnit = ""; //数量单位
                        String price = ""; //单价
                        String priceUnit = "";  //单价单位
                        String totalPrice = ""; //总价
                        String totalPriceUnit = ""; //总价单位
                        if (detailMap.containsKey("serialNumber")){
                            serialNumber = (String) detailMap.get("serialNumber");
                        }
                        if (detailMap.containsKey("name")){
                            name = (String) detailMap.get("name");
                        }
                        if (detailMap.containsKey("brand")){
                            brand = (String) detailMap.get("brand");
                        }
                        if (detailMap.containsKey("model")){
                            model = (String) detailMap.get("model");
                        }
                        if (detailMap.containsKey("number")){
                            number = (String) detailMap.get("number");
                        }
                        if (detailMap.containsKey("numberUnit")){
                            numberUnit = (String) detailMap.get("numberUnit");
                        }
                        if (detailMap.containsKey("price")){
                            price = (String) detailMap.get("price");
                        }
                        if (detailMap.containsKey("priceUnit")){
                            priceUnit = (String) detailMap.get("priceUnit");
                        }

                        if (detailMap.containsKey("totalPrice")){
                            totalPrice = (String) detailMap.get("totalPrice");
                        }
                        if (detailMap.containsKey("totalPriceUnit")){
                            totalPriceUnit = (String) detailMap.get("totalPriceUnit");
                        }
                        bdJdbcTemplate.update(UPDATA_SQL_01, contentId, serialNumber, name, brand, model, number, numberUnit, price, priceUnit, totalPrice, totalPriceUnit);
//                        bdJdbcTemplate.update("UPDATE loiloi_biaodiwu SET code = ? WHERE content_id = ? ", 1, contentId);
                        log.info("contentId:{} =========== 标的物解析表数据处理成功！！！ ",contentId);
                    }
                }
            }
        }
    }
}