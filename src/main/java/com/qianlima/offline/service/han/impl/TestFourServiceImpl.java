package com.qianlima.offline.service.han.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.entity.Enterprise;
import com.qianlima.offline.service.han.CusDataNewService;
import com.qianlima.offline.service.han.TestFourService;
import com.qianlima.offline.util.LogUtils;
import com.qianlima.offline.util.OnlineContentSolr;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.qianlima.offline.util.HttpClientUtil.getHttpClient;

@Service
@Slf4j
public class TestFourServiceImpl implements TestFourService {

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    @Qualifier("djeJdbcTemplate")
    private JdbcTemplate djeJdbcTemplate;

    @Autowired
    private OnlineContentSolr onlineContentSolr;

    @Autowired
    private CurrencyServiceImpl currencyService;

    @Autowired
    private CusDataNewService cusDataNewService;

    @Resource
    @Qualifier("qlyMongoTemplate")
    private MongoTemplate qlyDbTemplate;


    /**
     *  入库sql
     */
    public static final String INSERT_ZT_FOUR = "INSERT INTO han_new_data (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time,is_electronic,code,isfile,keyword_term,keywords," +
            " infoTypeSegment,monitorUrl, pocDetailUrl,first,second) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    /**
     * 入库操作语句
     * @param map
     */
    public void saveIntoMysql(Map<String, Object> map){
        String contentId = map.get("content_id").toString();
        //进行大金额替换操作
        List<Map<String, Object>> maps = djeJdbcTemplate.queryForList("select info_id, winner_amount, budget from amount_code where info_id = ?", contentId);
        if (maps != null && maps.size() > 0) {
            // 由于大金额处理的特殊性，只能用null进行判断
            String winnerAmount = maps.get(0).get("winner_amount") != null ? maps.get(0).get("winner_amount").toString() : null;
            if (winnerAmount != null) {
                map.put("baiLian_amount_unit", winnerAmount);
            }
            String budget = maps.get(0).get("budget") != null ? maps.get(0).get("budget").toString() : null;
            if (budget != null) {
                map.put("baiLian_budget", budget);
            }
        }
        bdJdbcTemplate.update(INSERT_ZT_FOUR,map.get("task_id"), map.get("keyword"), map.get("content_id"), map.get("title"),
                map.get("content"), map.get("province"), map.get("city"), map.get("country"), map.get("url"), map.get("baiLian_budget"),
                map.get("baiLian_amount_unit"), map.get("xmNumber"), map.get("bidding_type"), map.get("progid"), map.get("zhao_biao_unit"),
                map.get("relation_name"), map.get("relation_way"), map.get("agent_unit"), map.get("agent_relation_ame"),
                map.get("agent_relation_way"), map.get("zhong_biao_unit"), map.get("link_man"), map.get("link_phone"),
                map.get("registration_begin_time"), map.get("registration_end_time"), map.get("biding_acquire_time"),
                map.get("biding_end_time"), map.get("tender_begin_time"), map.get("tender_end_time"), map.get("update_time"),
                map.get("type"), map.get("bidder"), map.get("notice_types"), map.get("open_biding_time"), map.get("is_electronic"),
                map.get("code"), map.get("isfile"), map.get("keyword_term"),map.get("keywords"),map.get("infoTypeSegment"),map.get("monitorUrl"), map.get("pocDetailUrl"),
                map.get("first"), map.get("second")
        );
    }



    @Override
    public void test4() {
        log.info("四月份接口：{}","没有调取任何接口,只是测试项目启动");
    }

    @Override
    public void testTongJi(Integer type, String date, String s) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        //关键词a
        try {
            String[] a ={"路边泊车","路内停车","路内泊车","停车管理"};
            for (String str : a) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND allcontent:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll,list.size(),s,"ceshuju",date);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(80);
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
    public void getJiNanFuLiTong(Integer type, String date, String s) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        //关键词a
        try {
            //String[] aa ={"母线"};
            String[] aa ={"管型母线","管形母线","管母线","铜管母线","铜母线","浇注母线","耐火母线","母线槽","封闭母线","密集型母线","密集型母线槽","高压共箱母线"};
            //String[] bb ={"管型母线","管形母线","管母线","铜管母线","铜母线","浇注母线","耐火母线","母线槽","封闭母线","密集型母线","密集型母线槽","高压共箱母线"};
            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND title:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    //data.setKeyword(str);
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
            }
           /* for (String str : bb) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND allcontent:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
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
            }*/
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
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll,list.size(),s,"济南富利通-标的物",date);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(80);
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
    public void getZhongjieNeng_2016(Integer type, String date, String s) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        //关键词a
        try {
            String[] aa ={"环卫一体化","道路清扫","道路保洁","道路清扫保洁","垃圾收集","垃圾转运","垃圾收转运","垃圾清运","智慧环卫"};
            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid: 3 OR progid:5) AND newProvince:\""+"新疆维吾尔自治区"+"\" AND title:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll,list.size(),s,"新疆",date);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(80);
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
    public void getZhongjieNeng_4(Integer type, String date, String s) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        //关键词a
        try {
            String[] aa ={"环卫一体化","道路清扫","道路保洁","道路清扫保洁","垃圾收集","垃圾转运","垃圾收转运","垃圾清运","智慧环卫"};
            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid: 3 OR progid:5) AND newProvince:\""+"新疆维吾尔自治区"+"\" AND title:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll,list.size(),s,"新疆",date);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(80);
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
    public void getShunfeng(Integer type, String date, String s) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        //关键词a
        try {
            String[] aa ={"快递","物流","供应链","运输","配送","仓储","冷链","速投","速递","邮递","邮政","快件","快寄","包裹","专送","快运","货运","货站","派送","转运","运送","输送","送货","装运","储运","仓配","搬运","仓库","冷运","集散中心","海运","陆运","空运"};
            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3])  AND title:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll,list.size(),s,"顺丰科技",date);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(80);
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

    /**
     *  荣安物业
     */
    @Override
    public void getRongAnWuYe(Integer type, String date, String s) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aaa = {"物业"};
        String[] bbb = {"安保","安全","保安","保洁","保修","保障","承包","承接","范围","服务","管护","管理","管辖","经营","绿化","劳务","维护","维系","维修","项目","修缮","运维","运营","招标","装饰","综合","开办服务","一体化"};

        for (String aa : aaa) {
            for (String bb : bbb) {
                futureList1.add(executorService1.submit(() -> {
                    String key = aa + "&" + bb;
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND progid:3 AND title:\"" + aa + "\" AND title:\"" + bb + "\" ", key, 4);
                    log.info(key.trim() + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    list1.add(data);
                                    data.setKeyword(key);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
                                }
                            }
                        }
                    }
                }));
            }
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


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        currencyService.soutKeywords(list1,list.size(),s,"荣安物业",date);

        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(60);
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
    }

    @Override
    public void getGuangZhouOuKe(Integer type, String date, String s) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        //关键词a
        try {
            String[] aa ={"测绘","不动产","双评价","地形图","城市更新","档案整理","房地一体","拆旧复垦","土地规划","城乡规划","村庄规划","技术服务","技术审查"};
            String[] bb ={"垦造水田","权属调查","生态修复","林权调查","土地调查","权籍调查","测绘系统","用地预审","地名普查","勘测定界","多测合一","不动产平台","报批技术服务","林权数据建库","地理信息系统","测绘信息平台","城市更新研究","生态修复研究","土地规划研究","复垦咨询服务","不动产数据整合","不动产数据建库","不动产数据整合","一张图信息平台","建设用地增减挂钩","全域土地综合整治","城市更新数据调查","城乡规划信息系统","垦造水田研究报告","生态修复咨询服务","城乡规划咨询服务","城市更新咨询服务","垦造水田咨询服务","国土规划数据治理"};
            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    //List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid: 3 OR progid:5) AND newProvince:\""+"新疆维吾尔自治区"+"\" AND title:\"" + str + "\"", "", 1);
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid: 3 OR progid:5) AND (newProvince:\""+"广东省"+"\" OR newProvince:\"" + "广西壮族自治区" + "\" OR newProvince:\"" + "湖南省" + "\" )AND title:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
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
            }
            for (String str : bb) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    //List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid: 3 OR progid:5) AND newProvince:\""+"新疆维吾尔自治区"+"\" AND title:\"" + str + "\"", "", 1);
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid: 3 OR progid:5) AND (newProvince:\""+"广东省"+"\" OR newProvince:\"" + "广西壮族自治区" + "\" OR newProvince:\"" + "湖南省" + "\" )AND allcontent:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll,list.size(),s,"广州欧科",date);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(80);
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
    public void getDaoMengXinXi(Integer type, String date, String s) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        //关键词a
        try {
            String[] aa ={"交通安全教育体验馆","交通安全馆","交通安全警示教育基地","VR交通安全体验馆","交通安全体验馆","交通安全科普体验馆","青少年道路交通安全教育基地","交通安全宣教基地","交通治理工作站","应急安全体验馆","应急救援实训基地","安全体验馆","红十字会生命体验馆","红十字应急救护培训基地","红十字生命安全健康体验教室","应急安全科普馆","消防体验馆","应急体验馆","消防安全体验馆","模拟汽车驾驶体验","模拟报警电话体验","动感驾驶VR体验","模拟红绿灯过马路","VR机动车模拟驾驶","VR步行机体验系统","车祸碰撞体验","红绿灯斑马线体验","模拟交警指挥学习考核系统","安全驾驶模拟培训设备","模拟醉酒体验设备","公交车砸玻璃体验系统","交通安全标识学习","防碰撞安全带体验装置","交通事故责任判别系统","车辆认知互动体验装置","公交车逃生模拟体验装置","自行车VR仿真模拟体验装置","步行机VR仿真模拟体验装置","闯红灯警示教育设备","交警指挥手势体验","交通安全VR体验系统","电子模拟灭火体验","模拟火场烟雾逃生体验","多场景仿真灭火体验","厨房隐患排查系统","VR火场逃生体验","家庭隐患排查体验系统","消防交通标识翻牌","厨房安全平台","结绳训练体验","居家安全场景模拟","四合一模拟电话报警","模拟烟雾逃生","模拟厨房灭火体验系统","消防VR体验系统","地震平台","VR地震避险自救体验","5D动感影院","吸毒后的你","体感模拟吸毒人脸变化","禁毒宣传VR体验系统","溺水救助","心肺复苏体验系统","VR应急救护","溺水救护体验","VR多场景CPR教学系统","防溺水演示系统（虚拟）","模拟被困电梯训练装置","防范电信诈骗体验系统"};
            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND allcontent:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll,list.size(),s,"广州盗梦信息",date);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(80);
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
    public void getTestZhongtai(Integer type, String date, String s) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        //关键词a
        try {
            String[] aa ={"衬衣"};
            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND allcontent:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll,list.size(),s,"简单测试数据",date);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(80);
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
    public void getYilongYiLiao(Integer type, String date, String s) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        //HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        //关键词a
        try {
            //String[] aa ={"产床","病床"};
            String[] bb ={"产床","病床","家庭户产房","一体化产房","分娩床","电动床","电动病床","电动产床","分娩产床","儿童病床","手动病床","儿科病床","医用病床","普通病床","塑钢病床","电动护理病床","高级病床","监护病床","双摇病床","医用护理病床","多可能诊疗病床","病床采购","采购病床","手动三摇病床","产病床","购置病床","病床购置","采购产床","产床采购","购置产床","产床购置"};
           /* for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND title:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
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
            }*/
            for (String str : bb) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND allcontent:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
                                    listAll.add(data);
                                    /*if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }*/
                                }
                            }
                        }
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

            if (listAll.size() >0){
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll,list.size(),s,"熠隆医疗设备",date);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(10);
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
    public void getZhongJieNeng(Integer type, String date, String s) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        //HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        //关键词a
        try {
            String[] bb ={"垃圾分类","垃圾自动分类","垃圾集中分类","垃圾精准分类","垃圾智慧分类","垃圾定时定点分类","垃圾投放分类","垃圾干湿分类","垃圾清运分类"};

            for (String str : bb) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 2] OR progid:5) AND (zhongBiaoUnit:* OR amountUnit:*)  AND allcontent:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
                                    listAll.add(data);
                                    /*if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }*/
                                }
                            }
                        }
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

            if (listAll.size() >0){
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll,list.size(),s,"中节能",date);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(10);
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
    public void getZhongJieNeng2(Integer type, String date, String s) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        //HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        //关键词a
        try {
            String[] bb ={"垃圾分类","垃圾自动分类","垃圾集中分类","垃圾精准分类","垃圾智慧分类","垃圾定时定点分类","垃圾投放分类","垃圾干湿分类","垃圾清运分类"};

            for (String str : bb) {
                futureList1.add(executorService1.submit(() -> {
                    //List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND (zhongBiaoUnit:* OR amountUnit:*)  AND allcontent:\"" + str + "\"", "", 1);
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:3  AND allcontent:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
                                    listAll.add(data);
                                    /*if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }*/
                                }
                            }
                        }
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

            if (listAll.size() >0){
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll,list.size(),s,"中节能2",date);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(10);
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
    public void getJiangSuBaiRui(Integer type, String date, String s) {
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        try {

            List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND newProvince:\"" + "广东省" + "\"", "", 1);
            if (!mqEntities.isEmpty()) {
                for (NoticeMQ data : mqEntities) {
                    if (data.getTitle() != null) {
                        boolean flag = true;
                        if (flag) {
                            listAll.add(data);
                            /*if (!dataMap.containsKey(data.getContentid().toString())) {
                                list.add(data);
                                dataMap.put(data.getContentid().toString(), "0");
                            }*/
                        }
                    }
                }
            }



            if (listAll.size() >0){
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            //currencyService.soutKeywords(listAll,list.size(),s,"中节能2");

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(10);
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
    public void getPoc(Integer type, String date, String s) {
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        try {

            List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:20210430 AND progid:3 AND zhongBiaoUnit:* AND amountUnit:*", "", 1);
            if (!mqEntities.isEmpty()) {
                for (NoticeMQ data : mqEntities) {
                    if (data.getTitle() != null) {
                        boolean flag = true;
                        if (flag) {
                            listAll.add(data);
                            /*if (!dataMap.containsKey(data.getContentid().toString())) {
                                list.add(data);
                                dataMap.put(data.getContentid().toString(), "0");
                            }*/
                        }
                    }
                }
            }

            if (listAll.size() >0){
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            //currencyService.soutKeywords(listAll,list.size(),s,"中节能2");

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(10);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSaveMongo(content)));
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
    public void getPoc2(Integer type, String date, String s) throws Exception{
        Map<String, Object> resultMap =new HashMap<>();

        List<String> names = LogUtils.readRule("hBdw");
        for (String name : names) {
            String entity = getHttp(name);
            JSONObject jsonObject = JSON.parseObject(entity);
            //成功
            if (jsonObject.getInteger("code") == 0) {
                JSONArray result = jsonObject.getJSONArray("result");
                if (result.size() >0){
                    Object o = result.get(0);
                    JSONObject obj= JSON.parseObject(o.toString());
                    if (obj.get("industry") !=null){
                        String sql ="INSERT into han_new_data (keywords,zhong_biao_unit) VALUES (?,?)";
                        bdJdbcTemplate.update(sql,obj.get("industry"), name);
                    }
                }
            }
        }

    }

    @Override
    public void getHangzhouHongXu(Integer type, String date, String s) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        try {
            String[] bb ={"中国电建集团中南勘测设计研究院有限公司","中国电建集团贵州勘测设计研究院有限公司","中国电建集团成都勘测设计研究院有限公司","中国电建集团昆明勘测设计研究院有限公司","中国电建集团西北勘测设计研究院有限公司","中国电建集团北京勘测设计研究院有限公司","上海市政工程设计研究总院（集团）有限公司","中国市政工程华北设计研究总院有限公司","中国市政工程中南设计研究总院有限公司","中国市政工程西北设计研究院有限公司","长江勘测规划设计研究有限责任公司","黄河勘测规划设计有限公司","华设设计集团股份有限公司","中设设计集团股份有限公司","苏交科集团股份有限公司","浙江省水利水电勘测设计研究院","浙江省水利水电勘测设计院","上海市水利工程设计研究院有限公司","中水珠江规划勘测设计有限公司","中水北方勘测设计研究有限责任公司","中国联合工程有限公司","中国联合工程公司","上海市城市建设设计研究总院（集团）有限公司","上海市城市建设设计研究总院","浙江大学建筑设计研究院有限公司","浙江省建筑设计研究院","浙江工业大学工程设计集团有限公司","中南建筑设计院股份有限公司","悉地（苏州）勘察设计顾问有限公司","悉地国际","华汇工程设计集团股份有限公司","中国能源建设集团江苏省电力设计院有限公司","中国能源建设集团浙江省电力设计院有限公司","中国能源建设集团广东省电力设计研究院有限公司"};

            for (String str : bb) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND newZhongBiaoUnit:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
                                    listAll.add(data);
                                }
                            }
                        }
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

            if (listAll.size() >0){
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll,list.size(),s,"杭州宏旭建设",date);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(10);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSaveHunHe(content)));
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
    public void getZhongXinChanYe(Integer type, String date, String s) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        try {
            List<String> keyWords = LogUtils.readRule("keyWords");
            for (String str : keyWords) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:3 OR progid:5) AND zhaoBiaoUnit:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
                                    listAll.add(data);
                                }
                            }
                        }
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

            if (listAll.size() >0){
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            //currencyService.soutKeywords(listAll,list.size(),s,"杭州宏旭建设");

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(10);
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
    public void getHeNanMaoQian(Integer type, String date, String s) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        try {
            List<String> keyWords = LogUtils.readRule("keyWords");
            for (String str : keyWords) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:3 OR progid:5) AND zhongRelationWay:*  AND newProvince:\"" + "河南省" + "\" AND allcontent:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
                                    listAll.add(data);
                                }
                            }
                        }
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

            if (listAll.size() >0){
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll,list.size(),s,"河南茂乾电子科技有限公司",date);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(10);
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

    /**
     * 中国光大银行股份有限公司
     * @param type
     * @param date
     * @param s
     */
    @Override
    public void getGuangDaYinHang(Integer type, String date, String s) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据

        try {
            String[] bb ={"资金来源","资金来自","项目建设所需资金由","资金由","资金源于"};

            List<String> aa = LogUtils.readRule("keyWords");
            for (String a : aa) {
                for (String b : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND progid:[0 TO 2] AND allcontent:\"" + a + "\"  AND allcontent:\"" + b + "\"", a+"&"+b, 2);
                        log.info(a.trim()+"&"+b + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    if (flag){
                                        data.setKeyword(a+"&"+b);
                                        listAll.add(data);
                                    }
                                }
                            }
                        }
                    }));
                }
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

            if (listAll.size() >0){
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll,list.size(),s,"中国光大银行股份有限公司",date);

        } catch (IOException e) {
            e.getMessage();
        }
        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(16);
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
    public void getGuangZhouOuKe2(Integer type, String date, String s) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        //关键词a
        try {
            String[] aa ={"测绘","不动产","双评价","地形图","城市更新","档案整理","房地一体","拆旧复垦","土地规划","城乡规划","村庄规划","技术服务","技术审查"};
            String[] bb ={"垦造水田","权属调查","生态修复","林权调查","土地调查","权籍调查","测绘系统","用地预审","地名普查","勘测定界","多测合一","不动产平台","报批技术服务","林权数据建库","地理信息系统","测绘信息平台","城市更新研究","生态修复研究","土地规划研究","复垦咨询服务","不动产数据整合","不动产数据建库","不动产数据整合","一张图信息平台","建设用地增减挂钩","全域土地综合整治","城市更新数据调查","城乡规划信息系统","垦造水田研究报告","生态修复咨询服务","城乡规划咨询服务","城市更新咨询服务","垦造水田咨询服务","国土规划数据治理"};
            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid: 3 OR progid:5) AND (newProvince:\""+"广东省"+"\" OR newProvince:\"" + "广西壮族自治区" + "\" OR newProvince:\"" + "湖南省" + "\" )AND title:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
                                    listAll.add(data);
                                }
                            }
                        }
                    }
                }));
            }
            for (String str : bb) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid: 3 OR progid:5) AND (newProvince:\""+"广东省"+"\" OR newProvince:\"" + "广西壮族自治区" + "\" OR newProvince:\"" + "湖南省" + "\" )AND allcontent:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
                                    listAll.add(data);
                                }
                            }
                        }
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

            if (listAll.size() >0){
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll,list.size(),s,"广州欧科第二回合",date);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(16);
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


    /**
     * 存储数据接口
     */
    private void getZhongTaiDatasAndSaveHunHe(NoticeMQ noticeMQ) {
        boolean b = cusDataNewService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        //String[] aa ={};
        //调用字段库接口，混合
        Map<String, Object> resultMap = cusDataNewService.getAllFieldsWithHunHe(noticeMQ, false);//混合
        if (resultMap != null) {
            //String content = cusDataNewService.getContent(noticeMQ);//获取正文字段
            //content =cusDataNewService.processAboutContent(content);//去除链接
            //String[] keywords ={""};//英文的关键词
            //content =cusDataNewService.checkString(content,keywords);//再判断是否是字母

            /**
             *  通用方法-插入数据库操作
             *  //cusDataNewService.saveIntoMysql(resultMap);
             */

           /*String title = resultMap.get("title").toString();
           if (aa !=null && aa.length >0){

                for (String s : aa) {
                    if (title.contains(s)){
                        resultMap.put("keyword",s);
                        break;
                    }
                }
            }*/

            /**
             * 自行封装方法
             */
            saveIntoMysql(resultMap);
            log.info("进行入库操作，contentId:{}",resultMap.get("content_id").toString());
        }
    }
    /**
     * 存储数据接口-自提（默认）
     */
    private void getZhongTaiDatasAndSave(NoticeMQ noticeMQ) {
        boolean b = cusDataNewService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        //String[] aa ={};
        //调用中台接口，全部自提
        Map<String, Object> resultMap = cusDataNewService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            //String content = cusDataNewService.getContent(noticeMQ);//获取正文字段
            //content =cusDataNewService.processAboutContent(content);//去除链接
            //String[] keywords ={""};//英文的关键词
            //content =cusDataNewService.checkString(content,keywords);//再判断是否是字母

            /**
             *  通用方法-插入数据库操作
             *  //cusDataNewService.saveIntoMysql(resultMap);
             */

           /*String title = resultMap.get("title").toString();
           if (aa !=null && aa.length >0){

                for (String s : aa) {
                    if (title.contains(s)){
                        resultMap.put("keyword",s);
                        break;
                    }
                }
            }*/

          /* //追加资金来源
            String infoId = noticeMQ.getContentid().toString();
            Map<String, Object> extractFundsSource = currencyService.getExtractFundsSource(infoId);
            if (extractFundsSource !=null){
                resultMap.put("first",extractFundsSource.get("proportion"));
                resultMap.put("second",extractFundsSource.get("source"));
            }

            if (resultMap.get("zhao_biao_unit") !=null) {
                String zhao_biao_unit = searchKAIndustry(resultMap.get("zhao_biao_unit").toString());
                if (StringUtils.isNotBlank(zhao_biao_unit)){
                    resultMap.put("keywords",zhao_biao_unit);
                }
            }*/
            /**
             * 自行封装方法
             */
            saveIntoMysql(resultMap);
            log.info("进行入库操作，contentId:{}",resultMap.get("content_id").toString());
        }
    }
    private void getZhongTaiDatasAndSaveMongo(NoticeMQ noticeMQ) {
        boolean b = cusDataNewService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        //调用中台接口，全部自提
        Map<String, Object> resultMap = cusDataNewService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            String zhong_biao_unit = resultMap.get("zhong_biao_unit").toString();
            //中标单位的天眼查行业
            Query query = new Query();
            query.addCriteria(Criteria.where("name").is(zhong_biao_unit));
            List<Enterprise> list = qlyDbTemplate.find(query, Enterprise.class);
            if (list.size() >0){
                Enterprise enterprise = list.get(0);
                if (StringUtils.isBlank(enterprise.getIndustry())){
                    //如果天眼查中标单位为空，则通过链接继续查询   http://qly-data.qianlima.com/qianliyan/task/name/
                    String entity = getHttp(zhong_biao_unit);
                    JSONObject jsonObject = JSON.parseObject(entity);
                    //成功
                    if (jsonObject.getInteger("code") == 0) {
                        JSONArray result = jsonObject.getJSONArray("result");
                        if (result.size() >0){
                            Object o = result.get(0);
                            JSONObject obj= JSON.parseObject(o.toString());
                            if (obj.get("industry") !=null){
                                resultMap.put("keywords",obj.get("industry"));
                            }
                        }

                    } 
                }else {
                    resultMap.put("keywords",enterprise.getIndustry());//天眼查中标单位
                }
            }else {
                //如果天眼查中标单位为空，则通过链接继续查询   http://qly-data.qianlima.com/qianliyan/task/name/
                String entity = getHttp(zhong_biao_unit);
                JSONObject jsonObject = JSON.parseObject(entity);
                //成功
                if (jsonObject.getInteger("code") == 0) {
                    JSONArray result = jsonObject.getJSONArray("result");
                    if (result.size() >0){
                        Object o = result.get(0);
                        JSONObject obj= JSON.parseObject(o.toString());
                        if (obj.get("industry") !=null){
                            resultMap.put("keywords",obj.get("industry"));
                        }
                    }

                }
            }

            saveIntoMysql(resultMap);
            log.info("进行入库操作，contentId:{}",resultMap.get("content_id").toString());
        }
    }
    public String getHttp(String zhongbiaoUnit) {
        String result = null;

        //创建默认的httpClient实例
        CloseableHttpClient httpClient = getHttpClient();
        try {
            //用get方法发送http请求
            HttpGet get = new HttpGet("http://qly-data.qianlima.com/qianliyan/task/name/"+zhongbiaoUnit);

            CloseableHttpResponse httpResponse = null;
            //发送get请求
            httpResponse = httpClient.execute(get);
            try {
                //response实体
                HttpEntity entity = httpResponse.getEntity();
                if (null != entity) {
                    log.info("获取迈瑞数据接口  响应状态码:" + httpResponse.getStatusLine());
                    result = EntityUtils.toString(entity);
                }
            } finally {
                httpResponse.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }

    /**
     * 通过单位名称匹配KA-自用行业（一级行业、二级行业）
     *
     */
    public String searchKAIndustry(String company) {
        try {
            if (StringUtils.isBlank(company)) {
                return null;
            }
            //删除特殊字符
            company = company.replaceAll("\\\\", "");
            company = company.replaceAll("\\/", "");
            String kaIndustryUrl = "http://cusdata.qianlima.com/api/ka/industry?unit="+company+"";
            HttpPost request = new HttpPost(kaIndustryUrl);
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            list.add(new BasicNameValuePair("unit", company));
            request.setEntity(new UrlEncodedFormEntity(list, "UTF-8"));
            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(60000)
                    .setSocketTimeout(60000).setConnectTimeout(60000).build();
            request.setConfig(requestConfig);
            HttpClient httpClient = HttpClients.createDefault();
            // 通过请求对象获取响应对象
            HttpResponse response = httpClient.execute(request);
            // 判断网络连接状态码是否正常(0--200都数正常)
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String industry = "";
                String entity = EntityUtils.toString(response.getEntity(), "utf-8");
                if (StringUtils.isNotBlank(entity)) {
                    JSONObject object = JSON.parseObject(entity);
                    JSONObject data = object.getJSONObject("data");
                    if (data != null){
                        String firstLevel = data.getString("firstLevel");
                        if ("行业待分类".equals(firstLevel)){
                            industry = firstLevel;
                        } else {
                            industry = firstLevel + "-" + data.getString("secondLevel");
                        }
                    }
                }
                return industry;
            } else {
                log.error("单位名称匹配KA行业报错 company:{}, 返回httpcode: {}", company, response.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            log.error("单位名称匹配KA行业（一级、二级行业），异常原因： {} ", e);
        }
        return null;
    }

}