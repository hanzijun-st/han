package com.qianlima.offline.service.han.impl;

import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.rule02.MyRuleUtils;
import com.qianlima.offline.service.CusDataFieldService;
import com.qianlima.offline.service.han.HanQingDaoService;
import com.qianlima.offline.util.StrUtil;
import com.qianlima.offline.util.TencentContentSolr;
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

@Slf4j
@Service
public class HanQingDaoServiceImpl implements HanQingDaoService {
    @Autowired
    private TencentContentSolr tencentContentSolr;//ICT的solr

    @Autowired
    private CusDataFieldService cusDataFieldService;

    @Autowired
    private MyRuleUtils myRuleUtils;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;
    @Override
    public void getQingdao(Integer type, String date) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(30);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        try {
            futureList1.add(executorService1.submit(() -> {
                List<NoticeMQ> mqEntities = tencentContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:31 OR progid:32 OR progid:33 OR progid:34 OR progid:35 OR progid:36 OR progid:37) AND newCity:\"" + "青岛市" + "\"", "", 1);
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                if (StringUtils.isNotBlank(data.getZhaoBiaoUnit())){
                                    String zhaobiaoindustry = myRuleUtils.getIndustry(data.getZhaoBiaoUnit());
                                    String[] split = zhaobiaoindustry.split("-");
                                    if (StringUtils.isNotBlank(zhaobiaoindustry)) {
                                        if ("政府机构".equals(split[0])) {
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
                    }
                }
            }));
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重之后的数据量：" + list.size());
            log.info("==========================");


           /* ArrayList<String> arrayList = new ArrayList<>();
            //关键词c
            for (String key : keyWords){
                arrayList.add(key);
            }

            for (String str : arrayList) {
                int total = 0;
                for (NoticeMQ noticeMQ : list) {
                    String keyword = noticeMQ.getKeyword();
                    if (StrUtil.isNotEmpty(keyword)){
                        if (keyword.equals(str)) {
                            total++;
                        }
                    }
                }
                if (total == 0) {
                    continue;
                }
                System.out.println(str + ": " + total);
            }*/
            System.out.println("全部数据量：" + listAll.size());
            System.out.println("去重之后的数据量：" + list.size());

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
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
        }
        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    @Override
    public void getQingdaoByJy(Integer type, String date) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(30);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        try {
            futureList1.add(executorService1.submit(() -> {
                List<NoticeMQ> mqEntities = tencentContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:31 OR progid:32 OR progid:33 OR progid:34 OR progid:35 OR progid:36 OR progid:37) AND newCity:\"" + "青岛市" + "\"", "", 1);
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                if (StringUtils.isNotBlank(data.getZhaoBiaoUnit())){
                                    String zhaobiaoindustry = myRuleUtils.getIndustry(data.getZhaoBiaoUnit());
                                    String[] split = zhaobiaoindustry.split("-");
                                    if (StringUtils.isNotBlank(zhaobiaoindustry)) {
                                        if ("教育单位".equals(split[0])) {
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
                    }
                }
            }));
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重之后的数据量：" + list.size());
            log.info("==========================");


           /* ArrayList<String> arrayList = new ArrayList<>();
            //关键词c
            for (String key : keyWords){
                arrayList.add(key);
            }

            for (String str : arrayList) {
                int total = 0;
                for (NoticeMQ noticeMQ : list) {
                    String keyword = noticeMQ.getKeyword();
                    if (StrUtil.isNotEmpty(keyword)){
                        if (keyword.equals(str)) {
                            total++;
                        }
                    }
                }
                if (total == 0) {
                    continue;
                }
                System.out.println(str + ": " + total);
            }*/
            System.out.println("全部数据量：" + listAll.size());
            System.out.println("去重之后的数据量：" + list.size());

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(32);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSave2(content)));
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
        }
        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    @Override
    public void getQingdaoAll(Integer type, String date) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(30);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        try {
            futureList1.add(executorService1.submit(() -> {
                List<NoticeMQ> mqEntities = tencentContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:31 OR progid:32 OR progid:33 OR progid:34 OR progid:35 OR progid:36 OR progid:37) AND newCity:\"" + "青岛市" + "\"", "", 1);
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
            }));
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重之后的数据量：" + list.size());
            log.info("==========================");


           /* ArrayList<String> arrayList = new ArrayList<>();
            //关键词c
            for (String key : keyWords){
                arrayList.add(key);
            }

            for (String str : arrayList) {
                int total = 0;
                for (NoticeMQ noticeMQ : list) {
                    String keyword = noticeMQ.getKeyword();
                    if (StrUtil.isNotEmpty(keyword)){
                        if (keyword.equals(str)) {
                            total++;
                        }
                    }
                }
                if (total == 0) {
                    continue;
                }
                System.out.println(str + ": " + total);
            }*/
            System.out.println("全部数据量：" + listAll.size());
            System.out.println("去重之后的数据量：" + list.size());

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(32);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSave2(content)));
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
        }
        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    private void getZhongTaiDatasAndSave(NoticeMQ noticeMQ) {
        boolean b = cusDataFieldService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }

        //全部自提，不需要正文
        Map<String, Object> resultMap = cusDataFieldService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            saveIntoMysql(resultMap);
        }
    }


    // 数据入库操作-规则三
    public static final String INSERT_ZT_RESULT_ICT = "INSERT INTO han_new_data_ict (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time,is_electronic,code,isfile,keyword_term,keywords, infoTypeSegment) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public void saveIntoMysql(Map<String, Object> map) {
        bdJdbcTemplate.update(INSERT_ZT_RESULT_ICT, map.get("task_id"), map.get("keyword"), map.get("content_id"), map.get("title"),
                map.get("content"), map.get("province"), map.get("city"), map.get("country"), map.get("url"), map.get("baiLian_budget"),
                map.get("baiLian_amount_unit"), map.get("xmNumber"), map.get("bidding_type"), map.get("progid"), map.get("zhao_biao_unit"),
                map.get("relation_name"), map.get("relation_way"), map.get("agent_unit"), map.get("agent_relation_ame"),
                map.get("agent_relation_way"), map.get("zhong_biao_unit"), map.get("link_man"), map.get("link_phone"),
                map.get("registration_begin_time"), map.get("registration_end_time"), map.get("biding_acquire_time"),
                map.get("biding_end_time"), map.get("tender_begin_time"), map.get("tender_end_time"), map.get("update_time"),
                map.get("type"), map.get("bidder"), map.get("notice_types"), map.get("open_biding_time"), map.get("is_electronic"),
                map.get("code"), map.get("isfile"), map.get("keyword_term"), map.get("keywords"), map.get("infoTypeSegment"));
    }

    //ICT 2
    private void getZhongTaiDatasAndSave2(NoticeMQ noticeMQ) {
        boolean b = cusDataFieldService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }

        //全部自提，不需要正文
        Map<String, Object> resultMap = cusDataFieldService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            saveIntoMysql2(resultMap);
        }
    }


    // 数据入库操作-规则三
    public static final String INSERT_ZT_RESULT_ICT2 = "INSERT INTO han_new_data_ict2 (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time,is_electronic,code,isfile,keyword_term,keywords, infoTypeSegment) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public void saveIntoMysql2(Map<String, Object> map) {
        bdJdbcTemplate.update(INSERT_ZT_RESULT_ICT2, map.get("task_id"), map.get("keyword"), map.get("content_id"), map.get("title"),
                map.get("content"), map.get("province"), map.get("city"), map.get("country"), map.get("url"), map.get("baiLian_budget"),
                map.get("baiLian_amount_unit"), map.get("xmNumber"), map.get("bidding_type"), map.get("progid"), map.get("zhao_biao_unit"),
                map.get("relation_name"), map.get("relation_way"), map.get("agent_unit"), map.get("agent_relation_ame"),
                map.get("agent_relation_way"), map.get("zhong_biao_unit"), map.get("link_man"), map.get("link_phone"),
                map.get("registration_begin_time"), map.get("registration_end_time"), map.get("biding_acquire_time"),
                map.get("biding_end_time"), map.get("tender_begin_time"), map.get("tender_end_time"), map.get("update_time"),
                map.get("type"), map.get("bidder"), map.get("notice_types"), map.get("open_biding_time"), map.get("is_electronic"),
                map.get("code"), map.get("isfile"), map.get("keyword_term"), map.get("keywords"), map.get("infoTypeSegment"));
    }

}