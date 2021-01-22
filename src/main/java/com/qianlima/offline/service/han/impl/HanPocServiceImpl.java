package com.qianlima.offline.service.han.impl;

import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.service.CusDataFieldService;
import com.qianlima.offline.service.han.HanPocService;
import com.qianlima.offline.util.ContentSolr;
import com.qianlima.offline.util.LogUtils;
import lombok.extern.slf4j.Slf4j;
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
    private CusDataFieldService cusDataFieldService;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;


    //mysql数据库中插入数据
    public static final String INSERT_ZT_RESULT_HAN = "INSERT INTO han_data (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time,is_electronic,code,isfile,keyword_term) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    @Override
    public void getNew(Integer type) {
        //ExecutorService executorService1 = Executors.newFixedThreadPool(32);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
       // List<Future> futureList1 = new ArrayList<>();

        try {
            //关键字
            List<String> keyWords = LogUtils.readRule("keyWords");
            for (String keyWord : keyWords) {
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20210101 TO 20210120] AND (progid:[0 TO 3] OR progid:5) AND allcontent:\"" + keyWord + "\"",keyWord, 1);
                log.info( "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                listAll.add(data);
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
            System.out.println("=============存表结束============");
        }
        StringBuffer s = new StringBuffer();
        s.append("===================================================================");
        s.append("===============================本次任务结束=============================");
        s.append("===================================================================");
        System.out.println(s);
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
}