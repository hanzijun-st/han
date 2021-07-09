package com.qianlima.offline.service.han.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.offline.bean.*;
import com.qianlima.offline.rule02.BiaoDiWuRule;
import com.qianlima.offline.service.CusDataFieldService;
import com.qianlima.offline.service.ZhuCeAreaService;
import com.qianlima.offline.service.han.CurrencyService;
import com.qianlima.offline.service.han.TestSixService;
import com.qianlima.offline.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.qianlima.offline.util.HttpClientUtil.getHttpClient;

/**
 * Created by Administrator on 2021/1/14.
 */
@Service
@Slf4j
public class CurrencyServiceImpl implements CurrencyService {

    @Autowired
    private UpdateContentSolr updateSolr;

    @Autowired
    private OnlineNewContentSolr onlineNewContentSolr;

    @Autowired
    private OnlineContentSolr onlineContentSolr;

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    @Qualifier("lsJdbcTemplate")
    private JdbcTemplate lsJdbcTemplate;

    @Autowired
    @Qualifier("crmJdbcTemplate")
    private JdbcTemplate crmJdbcTemplate;

    @Autowired
    private CusDataFieldService cusDataFieldService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private CleanUtils cleanUtils;

    @Autowired
    private ZhuCeAreaService zhuCeAreaService;

    @Autowired
    private TestSixService testSixService;

    @Autowired
    @Qualifier("djeJdbcTemplate")
    private JdbcTemplate djeJdbcTemplate;


    HashMap<Integer, Area> areaMap = new HashMap<>();

    //mysql数据库中插入数据
    public String INSERT_ZT_RESULT_HXR = "INSERT INTO han_data (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time,is_electronic,code,isfile,keyword_term) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public String INSERT_ZT_RESULT_HXR2 = "INSERT INTO zt_data_result_poc_table_2 (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time,is_electronic,code,isfile,keyword_term) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    //测试批量插入
    public String INSERT_HAN_ALL_TEST = "INSERT INTO han_tab_all_copy (id,json_id,contentid,content_source,sum,sumUnit,serialNumber,name," +
            "brand,model,number,numberUnit,price,priceUnit,totalPrice,totalPriceUnit,configuration_key,configuration_value,appendix_suffix) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    //标的物1.3版本
    private String SQL = "insert into han_new_bdw(infoId, sum, sum_unit, keyword, serial_number, name, brand, model, " +
            "number, number_unit, price, price_unit, total_price, total_price_unit, configuration) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    //标的物sql
    private static final String UPDATA_BDW_SQL = "INSERT INTO han_biaodiwu (contentid, serialNumber, name, brand, model, number, numberUnit, price, priceUnit, totalPrice, totalPriceUnit) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    //地区
    @PostConstruct
    public void init() {
        List<Map<String, Object>> maps = gwJdbcTemplate.queryForList("SELECT * FROM phpcms_area");
        for (Map<String, Object> map : maps) {
            Area area = new Area();
            area.setAreaid(Integer.valueOf(map.get("areaid").toString()));
            area.setName(map.get("name").toString());
            area.setParentid(map.get("parentid").toString());
            area.setArrparentid(map.get("arrparentid").toString());
            areaMap.put(Integer.valueOf(map.get("areaid").toString()), area);
        }
    }


    /**
     * 判断 0:0、1:全部、2:招标[0 TO 2]、3:3、4:[0 TO 3]、5:中标[3 OR progid:5]
     *
     * @param str
     * @return
     */
    @Override
    public String getProgidStr(String str) {
        if ("0".equals(str)) {
            //直接返回 0
            str = "0";
        } else if ("1".equals(str)) {
            //全部
            str = "[0 TO 3] OR progid:5";
        } else if ("2".equals(str)) {
            //招标
            str = "[0 TO 2]";
        } else if ("3".equals(str)) {
            //直接返回3
            str = "3";
        } else if ("4".equals(str)) {
            //直接返回 0 到 3
            str = "[0 TO 3]";
        } else if ("5".equals(str)) {
            //中标
            str = "3 OR progid:5";
        } else if ("6".equals(str)) {
            str = "0 OR progid:3";
        }
        return str;
    }

    @Override
    public void getOnePoc(Params params) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String time1 = params.getTime1();
        String time2 = params.getTime2();
        String type = params.getType();
        String titleOrAllcontent = params.getTitle();
        String progidStr = getProgidStr(type);

        try {
            //读取配置文件中的黑词
            List<String> blacks = LogUtils.readRule("blockKeys");
            //关键词
            List<String> keyWords = LogUtils.readRule("keyWords");
            //String string = "yyyymmdd:[20200101 TO 20201231] AND (progid:[0 TO 2]) AND catid:[* TO 100] AND title:\"" + str + "\" ";
            String string = "yyyymmdd:[" + time1 + " TO " + time2 + "] AND (progid:" + progidStr + ")" + " AND catid:[* TO 100] AND " + titleOrAllcontent;
            for (String str : keyWords) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = updateSolr.companyResultsBaoXian(string + ":\"" + str + "\"", str, 2);
                    log.info(str.trim() + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (params.getIsHave() != null && params.getIsHave().intValue() == 1) {
                                    for (String black : blacks) {
                                        if (StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)) {
                                            flag = false;
                                            break;
                                        }
                                    }
                                }
                                if (flag) {
                                    listAll.add(data);
                                    data.setKeyword(str);
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
            log.info("去重之后的数据量：" + list.size());
            log.info("==========================");


            ArrayList<String> arrayList = new ArrayList<>();
            for (String key : keyWords) {
                arrayList.add(key);
            }

            List<String> listStr = new ArrayList<>();
            for (String str : arrayList) {
                int total = 0;
                for (NoticeMQ noticeMQ : listAll) {
                    String keyword = noticeMQ.getKeyword();
                    if (keyword.equals(str)) {
                        total++;
                    }
                }
                if (total == 0) {
                    continue;
                }
                //System.out.println(str + ": " + total);
                listStr.add(str + ": " + total);
            }
            //System.out.println("全部数据量：" + listAll.size());
            //System.out.println("去重之后的数据量：" + list.size());
            listStr.add("全部数据量：" + listAll.size());
            listStr.add("去重之后的数据量：" + list.size());
            readFileByNameBd("oneFile", listStr);
            //如果参数为1,则进行存表
            if (params.getIsSave() != null && params.getIsSave().intValue() == 1) {
                if (list != null && list.size() > 0) {
                    ExecutorService executorService = Executors.newFixedThreadPool(80);
                    List<Future> futureList = new ArrayList<>();
                    for (NoticeMQ content : list) {
                        futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
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
            if (params.getIsSaveContentId().intValue() == 1) {
                if (list != null && list.size() > 0) {
                    ExecutorService executorService = Executors.newFixedThreadPool(80);
                    List<Future> futureList = new ArrayList<>();
                    for (NoticeMQ content : list) {
                        futureList.add(executorService.submit(() -> saveContentId(content.getContentid().toString())));
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getBdw(Integer type) {
        try {
            //bdwService.getSolrAllField2(type);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 批量导入
     */
    @Override
    public void saveList() {

        List<Map<String, Object>> maps = bdJdbcTemplate.queryForList("SELECT * FROM han_tab_all");

        try {
            //DBUtil.insertAll(INSERT_HAN_ALL_TEST,maps);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getBiaoQian(Integer type) throws Exception {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        List<String> hybq = LogUtils.readRule("hybq");
        if (hybq != null && hybq.size() > 0) {
            for (String s : hybq) {
                Map hashMap = HybqUtil.getHashMap(s);
                List<String> mapToKeyOrValue = MapUtil.getMapToKeyOrValue(hashMap);
                String key = mapToKeyOrValue.get(0);
                String value = mapToKeyOrValue.get(1);

                List<NoticeMQ> mqEntities = updateSolr.companyResultsBaoXian("yyyymmdd:[20201222 TO 20201231] AND progid:3 AND zhaoFirstIndustry:\"" + key + "\"   AND zhaoSecondIndustry:\"" + value + "\"  AND zhaoBiaoUnit:*", "", 1);
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            futureList1.add(executorService1.submit(() -> {
                                boolean flag = true;
                                if (flag) {
                                    //通过读取配置文件
                                    list1.add(data);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
                                }
                            }));
                        }
                    }
                }
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
        log.info("==========================");

        if (type.intValue() == 1) {
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(80);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
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
    public void getPpei() {
        try {
            List<String> listIds = new ArrayList<>();//所有的id
            List<String> listIds2 = new ArrayList<>();//已经存在的数据
            List<String> resultList = new ArrayList<>();
            List<Map<String, Object>> maps = bdJdbcTemplate.queryForList("SELECT * FROM ali_item_info");
            List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT * FROM zt_data_result_poc_table");
            if (maps != null) {
                for (Map<String, Object> map : maps) {
                    listIds.add(map.get("infoId").toString());
                }
            }
            for (Map<String, Object> map : mapList) {
                listIds2.add(map.get("content_id").toString());
            }
            ExecutorService executorService1 = Executors.newFixedThreadPool(32);
            List<Future> futureList1 = new ArrayList<>();

            List<String> keys = LogUtils.readRule("ppei");
            if (keys != null && keys.size() > 0) {
                for (String key : keys) {
                    futureList1.add(executorService1.submit(() -> {
                        NoticeMQ noticeMQ = new NoticeMQ();
                        noticeMQ.setContentid(Long.valueOf(key));
                        //中台获取数据
                        Map<String, Object> allFieldsWithOther = cusDataFieldService.getAllFieldsWithOther(noticeMQ, false);
                        if (allFieldsWithOther != null && allFieldsWithOther.size() > 0) {
                            String contentInfo = allFieldsWithOther.get("content").toString();
                            String content = processAboutContent(contentInfo);
                            if (StringUtils.isNotBlank(content)) {
                                allFieldsWithOther.put("content", content);
                            }
                            String contentid = allFieldsWithOther.get("content_id").toString();
                            String task_id = "";
                            if (listIds.contains(contentid)) {
                                task_id = "1";
                            } else {
                                task_id = "2";
                            }
                            allFieldsWithOther.put("task_id", task_id);

                                /*if (listIds2.contains(contentid)){
                                    //resultList.add(contentid);
                                }else{
                                    //cusDataFieldService.saveIntoMysql(allFieldsWithOther);
                                    resultList.add(contentid);
                                }*/
                            cusDataFieldService.saveIntoMysql(allFieldsWithOther);
                        }
                    }));
                    log.info("-----------------------执行的contentid:{}", key);
                }
            }
            for (Future future : futureList1) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService1.shutdown();
            System.out.println("==============(" + resultList.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getPpeiJy() {
        //读取到mysql数据
        List<Map<String, Object>> maps = bdJdbcTemplate.queryForList("SELECT * FROM zt_data_result_poc_table");
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<Future> futureList1 = new ArrayList<>();

        for (Map<String, Object> map : maps) {
            futureList1.add(executorService1.submit(() -> {
                if (map.get("title") != null) {
                    map.put("title", cleanUtils.cleanTitle(map.get("title").toString()));//标题
                }
                if (map.get("agent_unit") != null) {
                    map.put("agent_unit", cleanUtils.cleanAgentUnit(map.get("agent_unit").toString()));//代理机构
                }
                if (map.get("zhao_biao_unit") != null) {
                    map.put("zhao_biao_unit", cleanUtils.cleanZhaoBiaoUnit(map.get("zhao_biao_unit").toString()));//招标单位
                }
                if (map.get("zhong_biao_unit") != null) {
                    map.put("zhong_biao_unit", cleanUtils.cleanZhongBiaoUnit(map.get("zhong_biao_unit").toString()));//中标单位
                }
                if (map.get("xmNumber") != null) {
                    map.put("xmNumber", cleanUtils.cleanXmNumber(map.get("xmNumber").toString()));//项目编号
                }
                if (map.get("province") != null) {
                    map.put("province", cleanUtils.cleanProvince(map.get("province").toString()));//处理省市县
                }
                if (map.get("amount_unit") != null) {
                    map.put("amount_unit", cleanUtils.cleanAmount(map.get("amount_unit").toString()));//中标金额
                }
                if (map.get("baiLian_amount_unit") != null) {
                    map.put("baiLian_amount_unit", cleanUtils.cleanAmount(map.get("baiLian_amount_unit").toString()));//中标金额（百炼）
                }
                if (map.get("budget") != null) {
                    map.put("budget", cleanUtils.cleanAmount(map.get("budget").toString()));//招标预算
                }
                if (map.get("baiLian_budget") != null) {
                    map.put("baiLian_budget", cleanUtils.cleanAmount(map.get("baiLian_budget").toString()));//招标预算（百炼）
                }
                if (map.get("relation_name") != null) {
                    map.put("relation_name", cleanUtils.cleanLinkMan(map.get("relation_name").toString()));//招标单位联系人
                }
                if (map.get("link_man") != null) {
                    map.put("link_man", cleanUtils.cleanLinkMan(map.get("link_man").toString()));//招标单位联系人
                }
                if (map.get("relation_way") != null) {
                    map.put("relation_way", cleanUtils.cleanLinkWay(map.get("relation_way").toString()));//招标单位联系人电话
                }
                if (map.get("link_phone") != null) {
                    map.put("link_phone", cleanUtils.cleanLinkWay(map.get("link_phone").toString()));//中标单位联系人电话
                }

                if (map.get("agent_relation_ame") != null) {
                    map.put("agent_relation_ame", cleanUtils.cleanLinkMan(map.get("agent_relation_ame").toString()));//代理单位联系人
                }
                if (map.get("agent_relation_way") != null) {
                    map.put("agent_relation_way", cleanUtils.cleanLinkWay(map.get("agent_relation_way").toString()));//代理单位联系人电话
                }
                if (map.get("registration_begin_time") != null) {
                    map.put("registration_begin_time", DateUtils.parseDateFromDateStr(cleanUtils.cleanDateTime(map.get("registration_begin_time").toString())));//时间处理
                }
                if (map.get("registration_end_time") != null) {
                    map.put("registration_end_time", DateUtils.parseDateFromDateStr(cleanUtils.cleanDateTime(map.get("registration_end_time").toString())));//时间处理
                }
                if (map.get("biding_acquire_time") != null) {
                    map.put("biding_acquire_time", DateUtils.parseDateFromDateStr(cleanUtils.cleanDateTime(map.get("biding_acquire_time").toString())));//时间处理
                }
                if (map.get("biding_end_time") != null) {
                    map.put("biding_end_time", DateUtils.parseDateFromDateStr(cleanUtils.cleanDateTime(map.get("biding_end_time").toString())));//时间处理
                }
                if (map.get("tender_begin_time") != null) {
                    map.put("tender_begin_time", DateUtils.parseDateFromDateStr(cleanUtils.cleanDateTime(map.get("tender_begin_time").toString())));//时间处理
                }
                if (map.get("tender_end_time") != null) {
                    map.put("tender_end_time", DateUtils.parseDateFromDateStr(cleanUtils.cleanDateTime(map.get("tender_end_time").toString())));//时间处理
                }
                if (map.get("update_time") != null) {
                    map.put("update_time", DateUtils.parseDateFromDateStr(cleanUtils.cleanDateTime(map.get("update_time").toString())));//时间处理
                }
            }));

        }
        for (Future future : futureList1) {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();

        if (maps != null && maps.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (Map<String, Object> map : maps) {
                futureList.add(executorService.submit(() -> saveIntoMysql(map, INSERT_ZT_RESULT_HXR)));
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

    @Override
    public String getHttpGet(String contentId) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(60000)
                    .setSocketTimeout(60000).setConnectTimeout(60000).build();
            HttpGet get = new HttpGet("http://cusdata.qianlima.com/zt/api/" + contentId);
            //url格式编码
            get.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            //设置超时时间为60秒
            get.setConfig(requestConfig);
            //执行请求
            CloseableHttpResponse httpResponse = httpClient.execute(get);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String entity = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
                return entity;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void saveTyInto(Map<String, Object> map, String sql) {
        bdJdbcTemplate.update(sql, map.get("task_id"), map.get("keyword"), map.get("content_id"), map.get("title"),
                map.get("content"), map.get("province"), map.get("city"), map.get("country"), map.get("url"), map.get("baiLian_budget"),
                map.get("baiLian_amount_unit"), map.get("xmNumber"), map.get("bidding_type"), map.get("progid"), map.get("zhao_biao_unit"),
                map.get("relation_name"), map.get("relation_way"), map.get("agent_unit"), map.get("agent_relation_ame"),
                map.get("agent_relation_way"), map.get("zhong_biao_unit"), map.get("link_man"), map.get("link_phone"),
                map.get("registration_begin_time"), map.get("registration_end_time"), map.get("biding_acquire_time"),
                map.get("biding_end_time"), map.get("tender_begin_time"), map.get("tender_end_time"), map.get("update_time"),
                map.get("type"), map.get("bidder"), map.get("notice_types"), map.get("open_biding_time"), map.get("is_electronic"),
                map.get("code"), map.get("isfile"), map.get("keyword_term"));
    }

    @Override
    public void saveContentId(String contentid) {
        //mysql数据库中插入数据
        String sql = "INSERT INTO han_contentid (contentid) VALUES (?)";
        bdJdbcTemplate.update(sql, contentid);
        log.info("contentid--->:{}", contentid, "存入成功");
    }

    @Override
    public List<Map<String, Object>> getListMap(String sql) {
        return bdJdbcTemplate.queryForList(sql);
    }

    @Override
    public void getNewBdw3(Integer type) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<Future> futureList = new ArrayList<>();
        //contentid
        List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT id,contentid FROM han_contentid");
        for (Map<String, Object> mapData : mapList) {
            futureList.add(executorService1.submit(() -> {
                handleForData(Long.valueOf(mapData.get("contentid").toString()), type);
                log.info("新标的物方法--->:{}", mapData.get("contentid").toString() + "=======" + mapData.get("id").toString());
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

    public String INSERT_ZT_RESULT_TEST1 = "INSERT INTO han_test1 (task_id,keyword) VALUES (?,?)";

    @Override
    @Transactional
    public void saveData1(List<Map> maps) {
        if (maps.size() > 0) {
            for (Map map : maps) {
                bdJdbcTemplate.update(INSERT_ZT_RESULT_TEST1, map.get("task_id"), map.get("keyword"));
            }
        }
    }

    public String HANG_YE = "INSERT INTO han_new_hangyequfen (contentId,hyname,yi,er) VALUES (?,?,?,?)";

    @Override
    public void getPiPeiHangYeBiaoQian() {
        try {
            List<String> list = LogUtils.readRule("hybq");
            ExecutorService executorService = Executors.newFixedThreadPool(32);
            List<Future> futureList = new ArrayList<>();

            for (String zhaobiaounit : list) {
                String[] split = zhaobiaounit.split(":");
                String s1 = split[0];//contentId
                String s2 = split[1];//招标单位

                futureList.add(executorService.submit(() -> {
                    getHangYeBy(s1, s2);
                }));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    executorService.shutdown();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
            log.info("---------------追加行业标签成功，运行结束-------------");

        } catch (IOException e) {
            e.getMessage();
        }
    }

    @Override
    public void readFileByName(String name, List<String> list) {
        ReadFileUtil.readFile(ConstantBean.FILL_URL, name + ".txt", list);
    }

    @Override
    public void readFileByName(String name, List<String> list, Integer type) {
        if (type == 1) {
            ReadFileUtil.readFile(ConstantBean.FILL_URL, name + ".txt", list);
        } else if (type == 2) {
            ReadFileUtil.readFile(ConstantBean.FILL_URL_BD, name + ".txt", list);
        }
    }

    @Override
    public void readFileByMap(String name, Map<String, Long> map, String date) {
        ReadFileUtil.readFileByMap(ConstantBean.FILL_URL, name + ".txt", map, date, false);
    }

    @Override
    public void readFileByMapObj(String name, Map<String, Object> map, String date) {
        ReadFileUtil.readFileByMapObj(ConstantBean.FILL_URL_BD, name + ".txt", map, date, false);
    }

    @Override
    public void soutKeywords(List<NoticeMQ> listAll, Integer listSize, String s, String name, String date) {
        Map<String, Long> mapCount = listAll.stream().collect(Collectors.groupingBy(NoticeMQ::getKeyword, Collectors.counting()));
        Map<String, Long> linkedHashMap = new LinkedHashMap<>(mapCount);

        linkedHashMap.put("全部数据量", Long.valueOf(listAll.size()));
        linkedHashMap.put("去重数据量", Long.valueOf(listSize));

        if ("1".equals(s)) {
            currencyService.readFileByMap(name, linkedHashMap, date);
            log.info("统计数据写入linux文件成功");
        } else if ("2".equals(s)) {
            currencyService.readFileByMapToBd(name, linkedHashMap, date);
            log.info("统计数据写入本地文件成功");
        }
    }

    @Override
    public String getCrmByUserId() throws Exception {
        String cursorMark = "*";
        String format = "yyyy-MM-dd HH:mm:ss";
        int count = 0;
        int tiaoShu = 0;
        List<String> list = new ArrayList<>();
        //开始时间
        Date startTime = new SimpleDateFormat(format).parse("2020-06-01 00:00:00");
        //结束时间
        Date endTime = new SimpleDateFormat(format).parse("2020-06-30 23:59:59");
        while (true) {
            String result = getMaiRui(cursorMark);
            JSONObject data = JSON.parseObject(result);
            if (null == data) {
                log.error("异常++++++++++++++++++");
                System.out.println("1");
            }
            JSONObject info = (JSONObject) data.get("data");
            if (info == null) {
                log.error("异常——————————————————————");
                System.out.println("2");
                log.info("数据跑完了");
                log.info("一共：{}", tiaoShu);
                log.info("3.19-5.19的有：{}", count);
            }
            cursorMark = info.getString("cursorMark");
            JSONArray jsonArray = info.getJSONArray("list");
            if (jsonArray == null || jsonArray.size() == 0) {
                log.info("数据跑完了");
                log.info("一共：{}", tiaoShu);
                log.info("3.19-5.19的有：{}", count);
                return count + "";
            }
            for (Object o : jsonArray) {
                tiaoShu = tiaoShu + 1;
                JSONObject object = (JSONObject) o;
                //当前时间
                Date nowTime = new SimpleDateFormat(format).parse(object.get("infoPublishTime").toString());
                if (isEffectiveDate(nowTime, startTime, endTime)) {
                    String infoId = object.getString("infoId");
                    //中标单位
                    JSONArray zhongBiaoUnit = object.getJSONArray("zhongBiaoUnit");
                    String zhongBiaoUnitStr = null;
                    if (zhongBiaoUnit != null && zhongBiaoUnit.size() > 0) {
                        zhongBiaoUnitStr = "";
                        for (int i = 0; i < zhongBiaoUnit.size(); i++) {
                            zhongBiaoUnitStr += zhongBiaoUnit.getString(i);
                            zhongBiaoUnitStr += ",";
                        }
                        zhongBiaoUnitStr = zhongBiaoUnitStr.substring(0, zhongBiaoUnitStr.length() - 1);
                    }
                    //中标单位联系人
                    JSONArray zhongRelationName = object.getJSONArray("zhongRelationName");
                    String zhongRelationNameStr = null;
                    if (zhongRelationName != null && zhongRelationName.size() > 0) {
                        zhongRelationNameStr = "";
                        for (int i = 0; i < zhongRelationName.size(); i++) {
                            zhongRelationNameStr += zhongRelationName.getString(i);
                            zhongRelationNameStr += ",";
                        }
                        zhongRelationNameStr = zhongRelationNameStr.substring(0, zhongRelationNameStr.length() - 1);
                    }
                    //中标单位联系电话
                    JSONArray zhongRelationWay = object.getJSONArray("zhongRelationWay");
                    String zhongRelationWayStr = null;
                    if (zhongRelationWay != null && zhongRelationWay.size() > 0) {
                        zhongRelationWayStr = "";
                        for (int i = 0; i < zhongRelationWay.size(); i++) {
                            zhongRelationWayStr += zhongRelationWay.getString(i);
                            zhongRelationWayStr += ",";
                        }
                        zhongRelationWayStr = zhongRelationWayStr.substring(0, zhongRelationWayStr.length() - 1);
                    }

                    //招标单位
                    JSONArray zhaoBiaoUnit = object.getJSONArray("zhaoBiaoUnit");
                    String zhaoBiaoUnitStr = null;
                    if (zhaoBiaoUnit != null && zhaoBiaoUnit.size() > 0) {
                        zhaoBiaoUnitStr = "";
                        for (int i = 0; i < zhaoBiaoUnit.size(); i++) {
                            zhaoBiaoUnitStr += zhaoBiaoUnit.getString(i);
                            zhaoBiaoUnitStr += ",";
                        }
                        zhaoBiaoUnitStr = zhaoBiaoUnitStr.substring(0, zhaoBiaoUnitStr.length() - 1);
                    }
                    //招标单位联系人
                    JSONArray zhaoRelationName = object.getJSONArray("zhaoRelationName");
                    String zhaoRelationNameStr = null;
                    if (zhaoRelationName != null && zhaoRelationName.size() > 0) {
                        zhaoRelationNameStr = "";
                        for (int i = 0; i < zhaoRelationName.size(); i++) {
                            zhaoRelationNameStr += zhaoRelationName.getString(i);
                            zhaoRelationNameStr += ",";
                        }
                        zhaoRelationNameStr = zhaoRelationNameStr.substring(0, zhaoRelationNameStr.length() - 1);
                    }
                    //招标单位联系电话
                    JSONArray zhaoRelationWay = object.getJSONArray("zhaoRelationWay");
                    String zhaoRelationWayStr = null;
                    if (zhaoRelationWay != null && zhaoRelationWay.size() > 0) {
                        zhaoRelationWayStr = "";
                        for (int i = 0; i < zhaoRelationWay.size(); i++) {
                            zhaoRelationWayStr += zhaoRelationWay.getString(i);
                            zhaoRelationWayStr += ",";
                        }
                        zhaoRelationWayStr = zhaoRelationWayStr.substring(0, zhaoRelationWayStr.length() - 1);
                    }

                    //代理单位
                    JSONArray agentUnit = object.getJSONArray("agentUnit");
                    String agentUnitStr = null;
                    if (agentUnit != null && agentUnit.size() > 0) {
                        agentUnitStr = "";
                        for (int i = 0; i < agentUnit.size(); i++) {
                            agentUnitStr += agentUnit.getString(i);
                            agentUnitStr += ",";
                        }
                        agentUnitStr = agentUnitStr.substring(0, agentUnitStr.length() - 1);
                    }
                    //代理单位联系人
                    JSONArray agentRelationName = object.getJSONArray("agentRelationName");
                    String agentRelationNameStr = null;
                    if (agentRelationName != null && agentRelationName.size() > 0) {
                        agentRelationNameStr = "";
                        for (int i = 0; i < agentRelationName.size(); i++) {
                            agentRelationNameStr += agentRelationName.getString(i);
                            agentRelationNameStr += ",";
                        }
                        agentRelationNameStr = agentRelationNameStr.substring(0, agentRelationNameStr.length() - 1);
                    }
                    //代理单位联系电话
                    JSONArray agentRelationWay = object.getJSONArray("agentRelationWay");
                    String agentRelationWayStr = null;
                    if (agentRelationWay != null && agentRelationWay.size() > 0) {
                        agentRelationWayStr = "";
                        for (int i = 0; i < agentRelationWay.size(); i++) {
                            agentRelationWayStr += agentRelationWay.getString(i);
                            agentRelationWayStr += ",";
                        }
                        agentRelationWayStr = agentRelationWayStr.substring(0, agentRelationWayStr.length() - 1);
                    }
                    //预算
                    JSONArray budget = object.getJSONArray("budget");
                    String budgetStr = null;
                    String budgetUnit = null;
                    if (budget != null && budget.size() > 0) {
                        budgetStr = "";
                        budgetUnit = "";
                        for (int i = 0; i < budget.size(); i++) {
                            budgetStr += budget.getJSONObject(i).getString("amount");
                            budgetStr += ",";
                            budgetUnit += budget.getJSONObject(i).getString("unit");
                            budgetUnit += ",";
                        }
                        budgetStr = budgetStr.substring(0, budgetStr.length() - 1);
                        budgetUnit = budgetUnit.substring(0, budgetUnit.length() - 1);
                    }
                    //中标金额
                    JSONArray winnerAmount = object.getJSONArray("winnerAmount");
                    String winnerAmountStr = null;
                    String winnerAmountUnit = null;
                    if (winnerAmount != null && winnerAmount.size() > 0) {
                        winnerAmountStr = "";
                        winnerAmountUnit = "";
                        for (int i = 0; i < winnerAmount.size(); i++) {
                            winnerAmountStr += winnerAmount.getJSONObject(i).getString("amount");
                            winnerAmountStr += ",";
                            winnerAmountUnit += winnerAmount.getJSONObject(i).getString("unit");
                            winnerAmountUnit += ",";
                        }
                        winnerAmountStr = winnerAmountStr.substring(0, winnerAmountStr.length() - 1);
                        winnerAmountUnit = winnerAmountUnit.substring(0, winnerAmountUnit.length() - 1);
                    }

                    JSONArray infoFile = object.getJSONArray("infoFile");
                    String infoFileStr = null;
                    if (infoFile != null && infoFile.size() > 0) {
                        infoFileStr = "";
                        for (int i = 0; i < infoFile.size(); i++) {
                            infoFileStr += infoFile.getString(i);
                            infoFileStr += ",";
                        }
                        infoFileStr = infoFileStr.substring(0, infoFileStr.length() - 1);
                    }

                    String openBidingTime = object.getString("openBidingTime");
                    String bidingAcquireTime = object.getString("bidingAcquireTime");
                    String bidingEndTime = object.getString("bidingEndTime");
                    String tenderBeginTime = object.getString("tenderBeginTime");
                    String tenderEndTime = object.getString("tenderEndTime");
                    String isElectronic = object.getString("isElectronic");
                    String infoType = object.getString("infoType");

                    String infoTitle = object.getString("infoTitle");
                    String infoPublishTime = object.getString("infoPublishTime");
                    String infoQianlimaUrl = object.getString("infoQianlimaUrl");
                    String areaProvince = object.getString("areaProvince");
                    String areaCity = object.getString("areaCity");
                    String areaCountry = object.getString("areaCountry");
                    String xmNumber = object.getString("xmNumber");
                    String biddingType = object.getString("biddingType");
                    String keywords = object.getString("keywords");
                    String infoUrl = object.getString("keywordsCode");
                    String xlfKeywords = object.getString("xlfKeywords");
                    String infoWebsite = object.getString("target");

                    //中国电信独有
//                    String amountTag = object.getString("amountTag");
//                    openBidingTime = amountTag;
//
//
//                    String opportunityTag = object.getString("opportunityTag");
//                    bidingAcquireTime = opportunityTag;
//
//                    String competorTag = object.getString("competorTag");
//                    bidingEndTime = competorTag;
//
//                    String dataType = object.getString("dataType");
//                    tenderBeginTime = dataType;
//
//                    String infoTypeSegment = object.getString("infoTypeSegment");
//                    tenderEndTime = infoTypeSegment;

                    // if(infoTitle.contains("...")){
                    bdJdbcTemplate.update("insert into han_crm_user (" +
                                    "taskid,infoId,infoTitle,infoType,infoPublishTime,infoQianlimaUrl," +
                                    "areaProvince,areaCity,areaCountry,xmNumber," +
                                    "zhongBiaoUnit,zhongRelationName,zhongRelationWay," +
                                    "openBidingTime,bidingAcquireTime,bidingEndTime,tenderBeginTime,tenderEndTime,isElectronic," +
                                    "biddingType,zhaoBiaoUnit,zhaoRelationName,zhaoRelationWay," +
                                    "agentUnit,agentRelationName," +
                                    "agentRelationWay,budget,budgetUnit,winnerAmount,winnerAmountUnit,infoFile,keywords, infoUrl, infoWebsite" +
                                    ",xlfKeywords) " +
                                    " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                            "21", infoId, infoTitle, infoType, infoPublishTime, infoQianlimaUrl,
                            areaProvince, areaCity, areaCountry, xmNumber,
                            zhongBiaoUnitStr, zhongRelationNameStr, zhongRelationWayStr,
                            openBidingTime, bidingAcquireTime, bidingEndTime, tenderBeginTime, tenderEndTime, isElectronic,
                            biddingType, zhaoBiaoUnitStr, zhaoRelationNameStr, zhaoRelationWayStr, agentUnitStr, agentRelationNameStr,
                            agentRelationWayStr, budgetStr, budgetUnit, winnerAmountStr, winnerAmountUnit,
                            infoFileStr, keywords, infoUrl, infoWebsite, xlfKeywords);

                    count = count + 1;
                    list.add(infoId);
                    //  }
                }
            }
            log.info("第：{}条～～～～～～～～～～～～～～～～", tiaoShu);
        }
    }

    @Override
    public void getCrmByUserIdToMonth() {
        ExecutorService executorService1 = Executors.newFixedThreadPool(10);//开启线程池
        List<Future> futureList1 = new ArrayList<>();

        List<Map<String, Object>> mapList = crmJdbcTemplate.queryForList("select infoId from t_bd_gw where infoPublishTime ='2020-06'");
        if (mapList.size() > 0) {
            for (Map<String, Object> map : mapList) {

                String infoId = map.get("infoId").toString();
                String dataByInfoId = getDataByInfoId(infoId);

                JSONObject data = JSON.parseObject(dataByInfoId);
                if (null == data) {
                    log.error("异常++++++++++++++++++");
                    System.out.println("1");
                }
                JSONObject info = (JSONObject) data.get("data");
                if (info == null) {
                    log.error("异常——————————————————————");
                    continue;
                }
                JSONArray jsonArray = info.getJSONArray("list");
                if (jsonArray == null || jsonArray.size() == 0) {
                    log.info("数据为空，以至于跑完了");
                    continue;
                }
                futureList1.add(executorService1.submit(() -> {
                    for (Object o : jsonArray) {
                        JSONObject object = (JSONObject) o;
                        //中标单位
                        JSONArray zhongBiaoUnit = object.getJSONArray("zhongBiaoUnit");
                        String zhongBiaoUnitStr = null;
                        if (zhongBiaoUnit != null && zhongBiaoUnit.size() > 0) {
                            zhongBiaoUnitStr = "";
                            for (int i = 0; i < zhongBiaoUnit.size(); i++) {
                                zhongBiaoUnitStr += zhongBiaoUnit.getString(i);
                                zhongBiaoUnitStr += ",";
                            }
                            zhongBiaoUnitStr = zhongBiaoUnitStr.substring(0, zhongBiaoUnitStr.length() - 1);
                        }
                        //中标单位联系人
                        JSONArray zhongRelationName = object.getJSONArray("zhongRelationName");
                        String zhongRelationNameStr = null;
                        if (zhongRelationName != null && zhongRelationName.size() > 0) {
                            zhongRelationNameStr = "";
                            for (int i = 0; i < zhongRelationName.size(); i++) {
                                zhongRelationNameStr += zhongRelationName.getString(i);
                                zhongRelationNameStr += ",";
                            }
                            zhongRelationNameStr = zhongRelationNameStr.substring(0, zhongRelationNameStr.length() - 1);
                        }
                        //中标单位联系电话
                        JSONArray zhongRelationWay = object.getJSONArray("zhongRelationWay");
                        String zhongRelationWayStr = null;
                        if (zhongRelationWay != null && zhongRelationWay.size() > 0) {
                            zhongRelationWayStr = "";
                            for (int i = 0; i < zhongRelationWay.size(); i++) {
                                zhongRelationWayStr += zhongRelationWay.getString(i);
                                zhongRelationWayStr += ",";
                            }
                            zhongRelationWayStr = zhongRelationWayStr.substring(0, zhongRelationWayStr.length() - 1);
                        }

                        //招标单位
                        JSONArray zhaoBiaoUnit = object.getJSONArray("zhaoBiaoUnit");
                        String zhaoBiaoUnitStr = null;
                        if (zhaoBiaoUnit != null && zhaoBiaoUnit.size() > 0) {
                            zhaoBiaoUnitStr = "";
                            for (int i = 0; i < zhaoBiaoUnit.size(); i++) {
                                zhaoBiaoUnitStr += zhaoBiaoUnit.getString(i);
                                zhaoBiaoUnitStr += ",";
                            }
                            zhaoBiaoUnitStr = zhaoBiaoUnitStr.substring(0, zhaoBiaoUnitStr.length() - 1);
                        }
                        //招标单位联系人
                        JSONArray zhaoRelationName = object.getJSONArray("zhaoRelationName");
                        String zhaoRelationNameStr = null;
                        if (zhaoRelationName != null && zhaoRelationName.size() > 0) {
                            zhaoRelationNameStr = "";
                            for (int i = 0; i < zhaoRelationName.size(); i++) {
                                zhaoRelationNameStr += zhaoRelationName.getString(i);
                                zhaoRelationNameStr += ",";
                            }
                            zhaoRelationNameStr = zhaoRelationNameStr.substring(0, zhaoRelationNameStr.length() - 1);
                        }
                        //招标单位联系电话
                        JSONArray zhaoRelationWay = object.getJSONArray("zhaoRelationWay");
                        String zhaoRelationWayStr = null;
                        if (zhaoRelationWay != null && zhaoRelationWay.size() > 0) {
                            zhaoRelationWayStr = "";
                            for (int i = 0; i < zhaoRelationWay.size(); i++) {
                                zhaoRelationWayStr += zhaoRelationWay.getString(i);
                                zhaoRelationWayStr += ",";
                            }
                            zhaoRelationWayStr = zhaoRelationWayStr.substring(0, zhaoRelationWayStr.length() - 1);
                        }

                        //代理单位
                        JSONArray agentUnit = object.getJSONArray("agentUnit");
                        String agentUnitStr = null;
                        if (agentUnit != null && agentUnit.size() > 0) {
                            agentUnitStr = "";
                            for (int i = 0; i < agentUnit.size(); i++) {
                                agentUnitStr += agentUnit.getString(i);
                                agentUnitStr += ",";
                            }
                            agentUnitStr = agentUnitStr.substring(0, agentUnitStr.length() - 1);
                        }
                        //代理单位联系人
                        JSONArray agentRelationName = object.getJSONArray("agentRelationName");
                        String agentRelationNameStr = null;
                        if (agentRelationName != null && agentRelationName.size() > 0) {
                            agentRelationNameStr = "";
                            for (int i = 0; i < agentRelationName.size(); i++) {
                                agentRelationNameStr += agentRelationName.getString(i);
                                agentRelationNameStr += ",";
                            }
                            agentRelationNameStr = agentRelationNameStr.substring(0, agentRelationNameStr.length() - 1);
                        }
                        //代理单位联系电话
                        JSONArray agentRelationWay = object.getJSONArray("agentRelationWay");
                        String agentRelationWayStr = null;
                        if (agentRelationWay != null && agentRelationWay.size() > 0) {
                            agentRelationWayStr = "";
                            for (int i = 0; i < agentRelationWay.size(); i++) {
                                agentRelationWayStr += agentRelationWay.getString(i);
                                agentRelationWayStr += ",";
                            }
                            agentRelationWayStr = agentRelationWayStr.substring(0, agentRelationWayStr.length() - 1);
                        }
                        //预算
                        JSONArray budget = object.getJSONArray("budget");
                        String budgetStr = null;
                        String budgetUnit = null;
                        if (budget != null && budget.size() > 0) {
                            budgetStr = "";
                            budgetUnit = "";
                            for (int i = 0; i < budget.size(); i++) {
                                budgetStr += budget.getJSONObject(i).getString("amount");
                                budgetStr += ",";
                                budgetUnit += budget.getJSONObject(i).getString("unit");
                                budgetUnit += ",";
                            }
                            budgetStr = budgetStr.substring(0, budgetStr.length() - 1);
                            budgetUnit = budgetUnit.substring(0, budgetUnit.length() - 1);
                        }
                        //中标金额
                        JSONArray winnerAmount = object.getJSONArray("winnerAmount");
                        String winnerAmountStr = null;
                        String winnerAmountUnit = null;
                        if (winnerAmount != null && winnerAmount.size() > 0) {
                            winnerAmountStr = "";
                            winnerAmountUnit = "";
                            for (int i = 0; i < winnerAmount.size(); i++) {
                                winnerAmountStr += winnerAmount.getJSONObject(i).getString("amount");
                                winnerAmountStr += ",";
                                winnerAmountUnit += winnerAmount.getJSONObject(i).getString("unit");
                                winnerAmountUnit += ",";
                            }
                            winnerAmountStr = winnerAmountStr.substring(0, winnerAmountStr.length() - 1);
                            winnerAmountUnit = winnerAmountUnit.substring(0, winnerAmountUnit.length() - 1);
                        }

                        JSONArray infoFile = object.getJSONArray("infoFile");
                        String infoFileStr = null;
                        if (infoFile != null && infoFile.size() > 0) {
                            infoFileStr = "";
                            for (int i = 0; i < infoFile.size(); i++) {
                                infoFileStr += infoFile.getString(i);
                                infoFileStr += ",";
                            }
                            infoFileStr = infoFileStr.substring(0, infoFileStr.length() - 1);
                        }

                        String openBidingTime = object.getString("openBidingTime");
                        String bidingAcquireTime = object.getString("bidingAcquireTime");
                        String bidingEndTime = object.getString("bidingEndTime");
                        String tenderBeginTime = object.getString("tenderBeginTime");
                        String tenderEndTime = object.getString("tenderEndTime");
                        String isElectronic = object.getString("isElectronic");
                        String infoType = object.getString("infoType");

                        String infoTitle = object.getString("infoTitle");
                        String infoPublishTime = object.getString("infoPublishTime");
                        String infoQianlimaUrl = object.getString("infoQianlimaUrl");
                        String areaProvince = object.getString("areaProvince");
                        String areaCity = object.getString("areaCity");
                        String areaCountry = object.getString("areaCountry");
                        String xmNumber = object.getString("xmNumber");
                        String biddingType = object.getString("biddingType");
                        String keywords = object.getString("keywords");
                        String infoUrl = object.getString("keywordsCode");
                        String xlfKeywords = object.getString("xlfKeywords");
                        String infoWebsite = object.getString("target");

                        //中国电信独有
//                    String amountTag = object.getString("amountTag");
//                    openBidingTime = amountTag;
//
//
//                    String opportunityTag = object.getString("opportunityTag");
//                    bidingAcquireTime = opportunityTag;
//
//                    String competorTag = object.getString("competorTag");
//                    bidingEndTime = competorTag;
//
//                    String dataType = object.getString("dataType");
//                    tenderBeginTime = dataType;
//
//                    String infoTypeSegment = object.getString("infoTypeSegment");
//                    tenderEndTime = infoTypeSegment;

                        // if(infoTitle.contains("...")){
                        bdJdbcTemplate.update("insert into han_crm_user (" +
                                        "taskid,infoId,infoTitle,infoType,infoPublishTime,infoQianlimaUrl," +
                                        "areaProvince,areaCity,areaCountry,xmNumber," +
                                        "zhongBiaoUnit,zhongRelationName,zhongRelationWay," +
                                        "openBidingTime,bidingAcquireTime,bidingEndTime,tenderBeginTime,tenderEndTime,isElectronic," +
                                        "biddingType,zhaoBiaoUnit,zhaoRelationName,zhaoRelationWay," +
                                        "agentUnit,agentRelationName," +
                                        "agentRelationWay,budget,budgetUnit,winnerAmount,winnerAmountUnit,infoFile,keywords, infoUrl, infoWebsite" +
                                        ",xlfKeywords) " +
                                        " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                                "21", infoId, infoTitle, infoType, infoPublishTime, infoQianlimaUrl,
                                areaProvince, areaCity, areaCountry, xmNumber,
                                zhongBiaoUnitStr, zhongRelationNameStr, zhongRelationWayStr,
                                openBidingTime, bidingAcquireTime, bidingEndTime, tenderBeginTime, tenderEndTime, isElectronic,
                                biddingType, zhaoBiaoUnitStr, zhaoRelationNameStr, zhaoRelationWayStr, agentUnitStr, agentRelationNameStr,
                                agentRelationWayStr, budgetStr, budgetUnit, winnerAmountStr, winnerAmountUnit,
                                infoFileStr, keywords, infoUrl, infoWebsite, xlfKeywords);

                        //  }
                    }
                    log.info("运行的infoId：{}～～～～～～～～～～～～～～～～", infoId);
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
    }

    @Override
    public List<NoticeMQ> getNoticeMqList(List<NoticeMQ> listAll) {
        List<NoticeMQ> result = listAll.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<NoticeMQ>(Comparator.comparing(p -> p.getContentid()))),
                        ArrayList::new));
        log.info("去重数据量-stream：" + result.size());
        return result;
    }

    /**
     * 资金来源
     *
     * @param infoId
     * @return
     */
    @Override
    public Map<String, Object> getExtractFundsSource(String infoId) {
        Map<String, Object> map = QianlimaZTUtil.getFields("http://datafetcher.intra.qianlima.com/dc/bidding/fields", infoId, "extract_funds_source", null);
        if (map != null) {
            if ("0".equals(map.get("returnCode").toString())) {
                if (map.get("data") != null) {
                    JSONObject json = (JSONObject) JSON.toJSON(map.get("data"));
                    JSONArray fields = json.getJSONArray("fields");
                    if (fields != null && fields.size() > 0) {
                        JSONObject jsonObject = (JSONObject) fields.get(0);
                        if (jsonObject.get("extract_funds_source") != null) {
                            JSONArray jsonArray = JSONArray.parseArray(jsonObject.get("extract_funds_source").toString());
                            if (jsonArray != null && jsonArray.size() > 0) {
                                JSONObject obj = (JSONObject) jsonArray.get(0);
                                Map<String, Object> m = new HashMap<>();
                                m.put("proportion", obj.get("proportion"));
                                m.put("source", obj.get("source"));
                                return m;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public String INSERT_HAN_DJE = "INSERT INTO han_xs_dje (info_id,old_winner_amount,old_budget,url,user_id,new_winner_amount,new_budget) " +
            "VALUES (?,?,?,?,?,?,?)";

    @Override
    public void getDaJinEdatas() {
        List<Map<String, Object>> mapList = djeJdbcTemplate.queryForList("select info_id, old_winner_amount, old_budget,new_winner_amount,new_budget,user_id from amount_for_handle where  states = 0 group by info_id order by info_id asc");
        if (mapList != null && mapList.size() > 0) {
            for (Map<String, Object> map : mapList) {
                //String url = "http://monitor.ka.qianlima.com/#/checkDetails?pushId=/" + map.get("info_id");
                //bdJdbcTemplate.update(INSERT_HAN_DJE, map.get("info_id"), map.get("old_winner_amount"), map.get("old_budget"), url,map.get("user_id"));
                bdJdbcTemplate.update(INSERT_HAN_DJE, map.get("info_id"), null, null, null,map.get("user_id"));
                log.info("- - -大金额入库成功");
            }
        }
    }

    /**
     * 通过infoId获取项目名称
     */
    public String INSERT_PRO_NAME = "INSERT INTO han_new_proname (infoId,proName) VALUES (?,?)";

    @Override
    public void getProName() throws Exception {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();

        //关键词a
        List<String> keywords = LogUtils.readRule("idsFile");
        for (String str : keywords) {
            futureList1.add(executorService1.submit(() -> {
                List<NoticeAllField> mqEntities = onlineNewContentSolr.companyResultsBaoXian("id:\"" + str + "\"", "", 1);
                log.info("infoId:{}", str);
                if (!mqEntities.isEmpty()) {
                    for (NoticeAllField field : mqEntities) {
                        String projName = field.getProjName();
                        if (StringUtils.isNotBlank(projName)) {
                            bdJdbcTemplate.update(INSERT_PRO_NAME, str, projName);
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
        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }


    @Override
    public void testBj() {
        ExecutorService executorService1 = Executors.newFixedThreadPool(10);//开启线程池
        List<Future> futureList1 = new ArrayList<>();

        List<Map<String, Object>> tb1Maps = bdJdbcTemplate.queryForList("SELECT * FROM han_tb1");


        if (tb1Maps != null && tb1Maps.size() > 0) {
            for (Map<String, Object> tb1Map : tb1Maps) {
                futureList1.add(executorService1.submit(() -> {

                    String contentid = tb1Map.get("contentid").toString();
                    String name = tb1Map.get("name").toString();
                    String brand = String.valueOf(tb1Map.get("brand"));
                    String model = String.valueOf(tb1Map.get("model"));
                    String number = String.valueOf(tb1Map.get("number"));
                    String price = String.valueOf(tb1Map.get("price"));
                    String totalPrice = String.valueOf(tb1Map.get("totalPrice"));

                    String ms = "";

                    List<Map<String, Object>> tb2Maps = bdJdbcTemplate.queryForList("SELECT * FROM han_tb2 where contentid =? AND name=?", contentid, name);
                    if (tb2Maps != null && tb2Maps.size() > 0) {
                        Map<String, Object> tb2Map = tb2Maps.get(0);
                        String brand2 = String.valueOf(tb2Map.get("brand"));
                        String model2 = String.valueOf(tb2Map.get("model"));
                        String number2 = String.valueOf(tb2Map.get("number"));
                        String price2 = String.valueOf(tb2Map.get("price"));
                        String totalPrice2 = String.valueOf(tb2Map.get("totalPrice"));

                        if (!brand.equals(brand2)) {
                            ms += "brand ";
                        }
                        if (!model.equals(model2)) {
                            ms += "model ";
                        }
                        if (!number.equals(number2)) {
                            ms += "number ";
                        }
                        if (!price.equals(price2)) {
                            ms += "price ";
                        }
                        if (!totalPrice.equals(totalPrice2)) {
                            ms += "totalPrice ";
                        }
                    } else {
                        ms = "不存在";
                    }

                    if (StringUtils.isBlank(ms)) {
                        ms = "一样";
                    }
                    bdJdbcTemplate.update("INSERT INTO han_tb (contentid,name,ms) VALUES (?,?,?)", contentid, name, ms);
                    log.info("入库成功contentid：{}", contentid);
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
    }

    @Override
    public void getAreaByUnit() {
        ExecutorService executorService = Executors.newFixedThreadPool(10);//开启线程池
        List<Future> futureList = new ArrayList<>();
        try {
            List<String> keyWords = LogUtils.readRule("areaUnit");
            for (String keyWord : keyWords) {
                futureList.add(executorService.submit(() -> {
                    Map registerAddr = zhuCeAreaService.getRegisterAddr(keyWord);
                    if (registerAddr != null) {
                        bdJdbcTemplate.update("INSERT INTO han_area_unit (name,areaProvince,areaCity,areaCountry) VALUES (?,?,?,?)", keyWord, registerAddr.get("province"), registerAddr.get("city"), null);
                        log.info("地区入库成功，name:{}", keyWord);
                    }
                }));
            }
            for (Future future1 : futureList) {
                try {
                    future1.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    executorService.shutdown();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        } catch (IOException e) {
            e.getMessage();
        }
    }


    public String getMaiRui(String cursorMark) {
        String result = null;

        //创建默认的httpClient实例
        CloseableHttpClient httpClient = getHttpClient();
        try {
            //用get方法发送http请求
            HttpGet get = new HttpGet("http://monitor.ka.qianlima.com/crm/info/page" +
                    "?userId=13&pageSize=200&cursorMark=" + cursorMark);

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

    @Override
    public void getZhongXin() throws Exception {
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(5);//开启线程池
            List<Future> futureList = new ArrayList<>();
            List<String> list = LogUtils.readRule("qyNames");
            for (String s : list) {
                futureList.add(executorService.submit(() -> {
                    getData(s, 0);
                }));
            }
            for (Future future1 : futureList) {
                try {
                    future1.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    executorService.shutdown();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
            log.info("- - -工商信息获取结束- - -");

        } catch (Exception e) {
            e.getMessage();
            throw new Exception(e.getMessage());
        } finally {
            log.info("- - -中信企业接口运行结束- - - ");
        }

    }

    @Override
    public void getZhongXinZiDong1(List<String> list) throws Exception {
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(5);//开启线程池
            List<Future> futureList = new ArrayList<>();

            for (String s : list) {
                futureList.add(executorService.submit(() -> {
                    getData(s, 1);
                }));
            }
            for (Future future1 : futureList) {
                try {
                    future1.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    executorService.shutdown();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
            log.info("- - -工商信息获取结束- - -");

        } catch (Exception e) {
            e.getMessage();
            throw new Exception(e.getMessage());
        } finally {
            log.info("- - -中信企业接口运行结束- - - ");
        }
    }

    @Override
    public void getZhongXinZiDong2(List<String> list) throws Exception {
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(5);//开启线程池
            List<Future> futureList = new ArrayList<>();

            for (String s : list) {
                futureList.add(executorService.submit(() -> {
                    getData(s, 2);
                }));
            }
            for (Future future1 : futureList) {
                try {
                    future1.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    executorService.shutdown();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
            log.info("- - -工商信息获取结束- - -");

        } catch (Exception e) {
            e.getMessage();
            throw new Exception(e.getMessage());
        } finally {
            log.info("- - -中信企业接口运行结束- - - ");
        }
    }

    @Override
    public List<String> getKeyWordsByList(List<String> keywords, List<NoticeMQ> listAll, String date) {

        List<String> readList = new ArrayList<>();
        readList.add("- - - - -" + date + "- - - - -");
        for (String str : keywords) {
            int total = 0;
            for (NoticeMQ noticeMQ : listAll) {
                String keyword = noticeMQ.getKeyword();
                if (StrUtil.isNotEmpty(keyword)) {
                    if (keyword.equals(str)) {
                        total++;
                    }
                }
            }
            if (total == 0) {
                continue;
            }
            System.out.println(str + ": " + total);
            readList.add(str + ": " + total);
        }
        return readList;
    }

    @Override
    public void getNewTypeReadFile(String s, String name, List<String> sList, List<NoticeMQ> listAll, Integer listMapSize, String date) {
        List<String> keyWordsByList = currencyService.getKeyWordsByList(sList, listAll, date);
        keyWordsByList.add("全部数据量:" + listAll.size());
        keyWordsByList.add("去重数据量:" + listMapSize);

        if ("1".equals(s)) {
            currencyService.readFileByName(name, keyWordsByList, 1);
            log.info("统计数据写入linux文件成功");
        } else if ("2".equals(s)) {
            currencyService.readFileByName(name, keyWordsByList, 2);
            log.info("统计数据写入本地文件成功");
        }
    }

    @Override
    public void getPiPeiHangYeBiaoQianById() {
        try {
            List<String> list = LogUtils.readRule("hybqIds");
            ExecutorService executorService = Executors.newFixedThreadPool(32);
            List<Future> futureList = new ArrayList<>();

            AtomicInteger atomicInteger = new AtomicInteger(list.size() + 1);
            for (String id : list) {
                futureList.add(executorService.submit(() -> {
                    List<NoticeMQ> listDatas = onlineContentSolr.hybqSolr("id:\"" + id + "\"", "", 1);
                    if (listDatas != null && listDatas.size() > 0) {
                        for (NoticeMQ listData : listDatas) {
                            Long contentid = listData.getContentid();
                            String zhaoFirstIndustry = listData.getZhaoFirstIndustry();
                            String zhaoSecondIndustry = listData.getZhaoSecondIndustry();

                            bdJdbcTemplate.update(HANG_YE, contentid, null, zhaoFirstIndustry, zhaoSecondIndustry);
                            log.info("contentId:{} ,处理数据剩余数量:{} ", contentid, atomicInteger.addAndGet(-1));
                        }
                    }
                }));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    executorService.shutdown();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
            log.info("---------------追加行业标签成功，运行结束-------------");

        } catch (IOException e) {
            e.getMessage();
        }
    }

    @Override
    public synchronized Map<String, String> getAreaMap(String areaId) {
        // ka_部门内部省、市、县区域联查
        List<String> kaAreaList = new ArrayList<>();

        // 获取地区映射
        Map<String, String> resultMap = new HashMap<>();
        if (kaAreaList == null || kaAreaList.size() == 0) {
            try {
                ClassPathResource classPathResource = new ClassPathResource("area/ka_area.txt");
                InputStream inputStream = classPathResource.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String line = bufferedReader.readLine();
                while (StringUtils.isNotBlank(line)) {//BufferedReader有readLine()，可以实现按行读取
                    kaAreaList.add(line);
                    line = bufferedReader.readLine();
                }
            } catch (Exception e) {
                log.error("读取ka_area 失败, 请查证原因");
            }
        }
        for (String kaArea : kaAreaList) {
            String[] areaList = kaArea.split(":", -1);
            if (areaList != null && areaList.length == 4) {
                if (areaList[0].equals(areaId)) {
                    resultMap.put("areaProvince", areaList[1]);
                    resultMap.put("areaCity", areaList[2]);
                    resultMap.put("areaCountry", areaList[3]);
                }
            }
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getAreaData(String contentId) {
        //通过id 获取区域id
        Map<String, Object> md = lsJdbcTemplate.queryForMap(ConstantBean.SELECT_TIME_ONE_NOW_02, contentId);
        if (md != null && md.size() > 0) {
            String areaid = String.valueOf(md.get("areaid"));
            // 获取省、市代码
            String provinceName = null;
            String cityName = null;
            String countryName = null;
            if (StringUtils.isNotBlank(areaid)) {
                provinceName = getAreaMap(areaid).get("areaProvince");
                cityName = getAreaMap(areaid).get("areaCity");
                countryName = getAreaMap(areaid).get("areaCountry");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("province", provinceName);
            map.put("city", cityName);
            map.put("country", countryName);
            return map;
        }
        return null;
    }

    @Override
    public void getAreas() {
        ExecutorService executorService = Executors.newFixedThreadPool(6);
        List<Future> futureList = new ArrayList<>();
        try {
            List<String> list = LogUtils.readRule("areaIds");
            AtomicInteger atomicInteger = new AtomicInteger(list.size() + 1);
            for (String id : list) {
                futureList.add(executorService.submit(() -> {
                    Map<String, Object> areaData = getAreaData(id);
                    if (areaData != null) {
                        String provinceName = String.valueOf(areaData.get("province"));
                        String cityName = String.valueOf(areaData.get("city"));
                        String countryName = String.valueOf(areaData.get("country"));
                        bdJdbcTemplate.update("INSERT INTO han_area_unit (name,areaProvince,areaCity,areaCountry) VALUES (?,?,?,?)", id, provinceName, cityName, countryName);
                        log.info("地区入库成功-id:{},剩余数据量:{}", id,atomicInteger.addAndGet(-1));
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
        } catch (IOException e) {
            e.getMessage();
        }
    }

    /**
     * 调用天眼查接口-获取数据-存库
     *
     * @param s
     */
    private void getData(String s, Integer type) {
        JSONObject jsonObject = searchCreditCode(s);
        if (jsonObject != null) {
            String actualCapital = "";
            String regStatus = "";
            String regCapital = "";
            String regInstitute = "";
            String companyName = "";
            String businessScope = "";
            String industry = "";
            String regLocation = "";
            String regNumber = "";
            String phoneNumber = "";
            String creditCode = "";
            String approvedTime = "";
            String fromTime = "";
            String companyOrgType = "";
            String orgNumber = "";
            String toTime = "";
            String legalPersonName = "";
            String province = "";
            String city = "";
            String country = "";

            if (StringUtils.isNotBlank(jsonObject.getString("actualCapital"))) {
                actualCapital = jsonObject.getString("actualCapital");
            }
            if (StringUtils.isNotBlank(jsonObject.getString("regStatus"))) {
                regStatus = jsonObject.getString("regStatus");
            }
            if (StringUtils.isNotBlank(jsonObject.getString("regCapital"))) {
                regCapital = jsonObject.getString("regCapital");
            }
            if (StringUtils.isNotBlank(jsonObject.getString("regInstitute"))) {
                regInstitute = jsonObject.getString("regInstitute");
            }
            if (StringUtils.isNotBlank(jsonObject.getString("companyName"))) {
                companyName = jsonObject.getString("companyName");
            }
            if (StringUtils.isNotBlank(jsonObject.getString("businessScope"))) {
                businessScope = jsonObject.getString("businessScope");
            }
            if (StringUtils.isNotBlank(jsonObject.getString("industry"))) {
                industry = jsonObject.getString("industry");
            }
            if (StringUtils.isNotBlank(jsonObject.getString("regLocation"))) {
                regLocation = jsonObject.getString("regLocation");
            }
            if (StringUtils.isNotBlank(jsonObject.getString("regNumber"))) {
                regNumber = jsonObject.getString("regNumber");
            }
            if (StringUtils.isNotBlank(jsonObject.getString("phoneNumber"))) {
                phoneNumber = jsonObject.getString("phoneNumber");
            }
            if (StringUtils.isNotBlank(jsonObject.getString("creditCode"))) {
                creditCode = jsonObject.getString("creditCode");
            }
            if (StringUtils.isNotBlank(jsonObject.getString("approvedTime"))) {
                approvedTime = jsonObject.getString("approvedTime");
            }
            if (StringUtils.isNotBlank(jsonObject.getString("fromTime"))) {
                fromTime = jsonObject.getString("fromTime");
            }
            if (StringUtils.isNotBlank(jsonObject.getString("companyOrgType"))) {
                companyOrgType = jsonObject.getString("companyOrgType");
            }
            if (StringUtils.isNotBlank(jsonObject.getString("orgNumber"))) {
                orgNumber = jsonObject.getString("orgNumber");
            }
            if (StringUtils.isNotBlank(jsonObject.getString("toTime"))) {
                toTime = jsonObject.getString("toTime");
            }
            if (StringUtils.isNotBlank(jsonObject.getString("legalPersonName"))) {
                legalPersonName = jsonObject.getString("legalPersonName");
            }


            //Map<String, Object> area = testSixService.getArea(regLocation.trim(), companyName);
            //province = String.valueOf(area.getOrDefault("province",""));
            //city = String.valueOf(area.getOrDefault("city",""));
            //country = String.valueOf(area.getOrDefault("country",""));


            //if (StringUtils.isNotBlank(legalPersonName) && StringUtils.isNotBlank(phoneNumber)) {//联系人和联系方式不为空

            if (type == 0) {
                bdJdbcTemplate.update("INSERT INTO han_zhongxin (name,actualCapital,regStatus,regCapital,regInstitute,companyName," +
                                "businessScope,industry,regLocation,regNumber,phoneNumber,creditCode,approvedTime,fromTime,companyOrgType,orgNumber,toTime,legalPersonName," +
                                "province,city,country) " +
                                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", s, actualCapital, regStatus, regCapital, regInstitute, companyName,
                        businessScope, industry, regLocation, regNumber, phoneNumber, creditCode, approvedTime, fromTime, companyOrgType, orgNumber, toTime, legalPersonName, province, city, country);
                log.info("入库成功，name:{}", s);
            } else if (type == 1) {
                bdJdbcTemplate.update("INSERT INTO han_zhongxin_gy1 (name,actualCapital,regStatus,regCapital,regInstitute,companyName," +
                                "businessScope,industry,regLocation,regNumber,phoneNumber,creditCode,approvedTime,fromTime,companyOrgType,orgNumber,toTime,legalPersonName," +
                                "province,city,country) " +
                                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", s, actualCapital, regStatus, regCapital, regInstitute, companyName,
                        businessScope, industry, regLocation, regNumber, phoneNumber, creditCode, approvedTime, fromTime, companyOrgType, orgNumber, toTime, legalPersonName, province, city, country);
                log.info("入库成功，name:{}", s);
            } else if (type == 2) {
                bdJdbcTemplate.update("INSERT INTO han_zhongxin_gy2 (name,actualCapital,regStatus,regCapital,regInstitute,companyName," +
                                "businessScope,industry,regLocation,regNumber,phoneNumber,creditCode,approvedTime,fromTime,companyOrgType,orgNumber,toTime,legalPersonName," +
                                "province,city,country) " +
                                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", s, actualCapital, regStatus, regCapital, regInstitute, companyName,
                        businessScope, industry, regLocation, regNumber, phoneNumber, creditCode, approvedTime, fromTime, companyOrgType, orgNumber, toTime, legalPersonName, province, city, country);
                log.info("入库成功，name:{}", s);
            }
            //}
        }
    }

    /**
     * 调用天眼查接口 获取对应字段
     *
     * @param company
     * @return
     */
    public static JSONObject searchCreditCode(String company) {
        try {
            if (StringUtils.isBlank(company)) {
                return null;
            }
            //删除特殊字符
            company = company.replaceAll("\\\\", "");
            company = company.replaceAll("\\/", "");
            // 根据地址获取请求
            HttpGet request = new HttpGet("http://qly-data.qianlima.com/qianliyan/task/name/" + URLEncoder.encode(company, "utf-8"));
            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(60000)
                    .setSocketTimeout(60000).setConnectTimeout(60000).build();
            request.setConfig(requestConfig);
            HttpClient httpClient = HttpClients.createDefault();
            // 通过请求对象获取响应对象
            HttpResponse response = httpClient.execute(request);
            // 判断网络连接状态码是否正常(0--200都数正常)
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String creditCode = "";
                String entity = EntityUtils.toString(response.getEntity(), "utf-8");
                if (StringUtils.isNotBlank(entity)) {
                    JSONObject object = JSON.parseObject(entity);
                    if (object != null) {
                        String code = object.getString("code");
                        if ("0".equals(code)) {
                            if (object.getString("result") != null) {
                                JSONArray result = object.getJSONArray("result");
                                if (result != null && result.size() > 0) {
                                    JSONObject jsonObject = result.getJSONObject(0);
                                    return jsonObject;
                                }
                            }
                        }
                    }
                }
                return null;
            } else {
                log.error("调用天眼查接口报错 company:{}, 返回httpcode: {}", company, response.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            log.error("通过单位名称去千里眼查找 统一社会信用代码失败， 失败原因：{} ", e);
        }
        return null;
    }

    private boolean isEffectiveDate(Date nowTime, Date startTime, Date endTime) {
        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);
        Calendar begin = Calendar.getInstance();
        begin.setTime(startTime);
        Calendar end = Calendar.getInstance();
        end.setTime(endTime);
        if (date.after(begin) && date.before(end)) {
            return true;
        } else if (nowTime.compareTo(startTime) == 0 || nowTime.compareTo(endTime) == 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void readFileByNameBd(String name, List<String> list) {
        ReadFileUtil.readFile(ConstantBean.FILL_URL_BD, name + ".txt", list);
    }

    @Override
    public void readFileByMapToBd(String name, Map<String, Long> map, String date) {
        ReadFileUtil.readFileByMap(ConstantBean.FILL_URL_BD, name + ".txt", map, date, false);
    }

    private void getHangYeBy(String contentId, String zhaobiaounit) {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpResponse response = null;
            // --KA自用行业
            // http://monitor.ka.qianlima.com/api/ka/industry?unit=上海市公安局国际机场分局
            String url = "http://monitor.ka.qianlima.com/api/ka/industry?unit=" + zhaobiaounit;
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json");

            response = client.execute(post);
            String ret = EntityUtils.toString(response.getEntity(), "UTF-8");

            JSONObject parseObject = JSON.parseObject(ret);
            JSONObject data = parseObject.getJSONObject("data");
            String firstLevel = data.getString("firstLevel");//一级行业标签
            String secondLevel = data.getString("secondLevel");//二级行业标签

            bdJdbcTemplate.update(HANG_YE, contentId, zhaobiaounit, firstLevel, secondLevel);
            log.info("contentId:{} =========== KA自用行业数据处理成功！！！ ", contentId);
        } catch (Exception e) {
            e.getMessage();
        }
    }


    public void handleForData(Long contentId, Integer type) {

        String[] keywords = {"鼻咽喉", "摄像系统", "超声", "摄像平台", "支气管", "输尿管", "胃肠", "宫腔", "腹腔", "呼吸", "膀胱", "消化", "胆道", "清洗消毒", "整体手术室", "影像装置", "图像处理", "摄像头", "监视器", "保养装置", "光源", "台车", "主机", "显示器", "适配器"};

        String url = "";
        for (BiaoDiWuRule value : BiaoDiWuRule.values()) {
            if (value.getValue().intValue() == type) {
                url = value.getName();
            }
        }
        //String result = TargetService.extract(contentId,url);
        String result = null;
        if (StringUtils.isNotBlank(result)) {
            JSONObject jsonObject = JSONObject.parseObject(result);
            if (jsonObject != null && jsonObject.containsKey("content_target")) {
                JSONObject resultObject = jsonObject.getJSONObject("content_target");
                if (resultObject != null && resultObject.containsKey("target_details")) {
                    String sum = resultObject.getString("sum");
                    String sum_unit = resultObject.getString("sum_unit");
                    JSONArray targetDetails = resultObject.getJSONArray("target_details");
                    if (targetDetails != null && targetDetails.size() > 0) {
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
                            if (finalObject != null) {
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
                                if (configurations != null && configurations.size() > 0) {
                                    for (int j = 0; j < configurations.size(); j++) {
                                        JSONObject jsonObject1 = configurations.getJSONObject(j);
                                        String key = jsonObject1.getString("key");
                                        String value = jsonObject1.getString("value");
                                        configuration += key + "：" + value + "：";
                                    }
                                }
                                if (StringUtils.isNotBlank(configuration)) {
                                    configuration = configuration.substring(0, configuration.length() - 1);
                                }
                                // 进行匹配关键词操作
                                if (keywords != null && keywords.length > 0) {
                                    String allField = name + "&" + brand + "&" + model + "&" + configuration;
                                    for (String key : keywords) {
                                        if (allField.toUpperCase().contains(key.toUpperCase())) {
                                            keyword += key + "，";
                                        }
                                    }
                                    if (StringUtils.isNotBlank(keyword)) {
                                        keyword = keyword.substring(0, keyword.length() - 1);
                                    }
                                }
                                // 进行数据保存操作
                            }
                            // 进行数据库保存操作
                            bdJdbcTemplate.update(SQL, contentId, sum, sum_unit, keyword, serial_number, name, brand, model, number, number_unit, price, price_unit, total_price, total_price_unit, configuration);
                        }
                    }
                }
            }
        } else {
            log.info("标的物不存在");
        }
    }

    //调取中台数据
    public void getDataFromZhongTaiAndSave(NoticeMQ noticeMQ) {
        boolean result = cusDataFieldService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = cusDataFieldService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
          /*  String contentInfo = resultMap.get("content").toString();
            String content = processAboutContent(contentInfo);
            if (StringUtils.isNotBlank(content)) {
                resultMap.put("content", content);
            }*/
            saveIntoMysql(resultMap, INSERT_ZT_RESULT_HXR);
        }
    }


    /**
     * jdbc存表接口
     *
     * @param map
     */
    public void saveIntoMysql(Map<String, Object> map, String table) {
        bdJdbcTemplate.update(table, map.get("task_id"), map.get("keyword"), map.get("content_id"), map.get("title"),
                map.get("content"), map.get("province"), map.get("city"), map.get("country"), map.get("url"), map.get("baiLian_budget"),
                map.get("baiLian_amount_unit"), map.get("xmNumber"), map.get("bidding_type"), map.get("progid"), map.get("zhao_biao_unit"),
                map.get("relation_name"), map.get("relation_way"), map.get("agent_unit"), map.get("agent_relation_ame"),
                map.get("agent_relation_way"), map.get("zhong_biao_unit"), map.get("link_man"), map.get("link_phone"),
                map.get("registration_begin_time"), map.get("registration_end_time"), map.get("biding_acquire_time"),
                map.get("biding_end_time"), map.get("tender_begin_time"), map.get("tender_end_time"), map.get("update_time"),
                map.get("type"), map.get("bidder"), map.get("notice_types"), map.get("open_biding_time"), map.get("is_electronic"),
                map.get("code"), map.get("isfile"), map.get("keyword_term"));
    }


    /**
     * 判断是否是字母
     *
     * @param str      传入字符串
     * @param keywords 英文的关键词
     * @return 是字母返回true，否则返回false
     */
    public static String checkString(String str, String[] keywords) {
        str = str.toUpperCase();
        //英文的关键词 keywords
        for (String Keyword : keywords) {
            Keyword = Keyword.toUpperCase();
            boolean flag = true;
            int key = str.indexOf(Keyword);
            if (key != -1) {
                if (key != 0) {
                    String substring1 = str.substring(key - 1, key);
                    Pattern p = Pattern.compile("[A-Z]");
                    Matcher m = p.matcher(substring1);
                    if (m.find() == true) flag = false;
                }
                if (key + Keyword.length() < str.length()) {
                    String substring1 = str.substring(key + Keyword.length(), key + Keyword.length() + 1);
                    Pattern p = Pattern.compile("[A-Z]");
                    Matcher m = p.matcher(substring1);
                    if (m.find() == true) flag = false;
                }
            }
            if (flag == false) {
                str = str.replace(Keyword.toUpperCase(), "");
            }
        }
        return str.toUpperCase();
    }

    /**
     * 去链接
     *
     * @param content
     * @return
     */
    public static String processAboutContent(String content) {
        Document document = Jsoup.parse(content);
        Elements elements = document.select("a[href]");
        Integer elementSize = elements.size();
        for (Integer i = 0; i < elementSize; i++) {
            Element element = elements.get(i);
            if (element == null || document.select("a[href]") == null || document.select("a[href]").size() == 0) {
                break;
            }
            String elementStr = element.attr("href");
            if (StringUtils.isNotBlank(elementStr) && elementStr.contains("www.qianlima.com")) {
                if (element.is("a")) {
                    element.remove();
                }
            }
        }
        return document.body().html();
    }

    /**
     * http://monitor.ka.qianlima.com/crm/info/detail?userId=?&infoId=
     */
    public String getDataByInfoId(String infoId) {
        String result = null;

        //创建默认的httpClient实例
        CloseableHttpClient httpClient = getHttpClient();
        try {
            //用get方法发送http请求
            HttpGet get = new HttpGet("http://monitor.ka.qianlima.com/crm/info/field?userId=13&infoId=" + infoId);

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
     * 临时数据-通过某个用户输出某些字段
     *
     * @return
     * @throws Exception
     */
    private String getCrmByUserIdLs() throws Exception {
        String cursorMark = "*";
        String format = "yyyy-MM-dd HH:mm:ss";
        int count = 0;
        int tiaoShu = 0;
        List<String> list = new ArrayList<>();
        //开始时间
        //Date startTime = new SimpleDateFormat(format).parse("2020-06-01 00:00:00");
        //结束时间
        //Date endTime = new SimpleDateFormat(format).parse("2020-06-30 23:59:59");
        while (true) {
            String result = getMaiRui(cursorMark);
            JSONObject data = JSON.parseObject(result);
            if (null == data) {
                log.error("异常++++++++++++++++++");
                System.out.println("1");
            }
            JSONObject info = (JSONObject) data.get("data");
            if (info == null) {
                log.error("异常——————————————————————");
                System.out.println("2");
                log.info("数据跑完了");
                log.info("一共：{}", tiaoShu);
                log.info("3.19-5.19的有：{}", count);
            }
            cursorMark = info.getString("cursorMark");
            JSONArray jsonArray = info.getJSONArray("list");
            if (jsonArray == null || jsonArray.size() == 0) {
                log.info("数据跑完了");
                log.info("一共：{}", tiaoShu);
                log.info("3.19-5.19的有：{}", count);
                return count + "";
            }
            for (Object o : jsonArray) {
                tiaoShu = tiaoShu + 1;
                JSONObject object = (JSONObject) o;
                //当前时间
                //Date nowTime = new SimpleDateFormat(format).parse(object.get("infoPublishTime").toString());
                //if(isEffectiveDate(nowTime, startTime, endTime)){
                String infoId = object.getString("infoId");
                //中标单位
                    /*JSONArray zhongBiaoUnit = object.getJSONArray("zhongBiaoUnit");
                    String zhongBiaoUnitStr = null;
                    if (zhongBiaoUnit != null && zhongBiaoUnit.size() > 0) {
                        zhongBiaoUnitStr = "";
                        for (int i = 0; i < zhongBiaoUnit.size(); i++) {
                            zhongBiaoUnitStr += zhongBiaoUnit.getString(i);
                            zhongBiaoUnitStr += ",";
                        }
                        zhongBiaoUnitStr = zhongBiaoUnitStr.substring(0, zhongBiaoUnitStr.length() - 1);
                    }
                    //中标单位联系人
                    JSONArray zhongRelationName = object.getJSONArray("zhongRelationName");
                    String zhongRelationNameStr = null;
                    if (zhongRelationName != null && zhongRelationName.size() > 0) {
                        zhongRelationNameStr = "";
                        for (int i = 0; i < zhongRelationName.size(); i++) {
                            zhongRelationNameStr += zhongRelationName.getString(i);
                            zhongRelationNameStr += ",";
                        }
                        zhongRelationNameStr = zhongRelationNameStr.substring(0, zhongRelationNameStr.length() - 1);
                    }
                    //中标单位联系电话
                    JSONArray zhongRelationWay = object.getJSONArray("zhongRelationWay");
                    String zhongRelationWayStr = null;
                    if (zhongRelationWay != null && zhongRelationWay.size() > 0) {
                        zhongRelationWayStr = "";
                        for (int i = 0; i < zhongRelationWay.size(); i++) {
                            zhongRelationWayStr += zhongRelationWay.getString(i);
                            zhongRelationWayStr += ",";
                        }
                        zhongRelationWayStr = zhongRelationWayStr.substring(0, zhongRelationWayStr.length() - 1);
                    }

                    //招标单位
                    JSONArray zhaoBiaoUnit = object.getJSONArray("zhaoBiaoUnit");
                    String zhaoBiaoUnitStr = null;
                    if (zhaoBiaoUnit != null && zhaoBiaoUnit.size() > 0) {
                        zhaoBiaoUnitStr = "";
                        for (int i = 0; i < zhaoBiaoUnit.size(); i++) {
                            zhaoBiaoUnitStr += zhaoBiaoUnit.getString(i);
                            zhaoBiaoUnitStr += ",";
                        }
                        zhaoBiaoUnitStr = zhaoBiaoUnitStr.substring(0, zhaoBiaoUnitStr.length() - 1);
                    }
                    //招标单位联系人
                    JSONArray zhaoRelationName = object.getJSONArray("zhaoRelationName");
                    String zhaoRelationNameStr = null;
                    if (zhaoRelationName != null && zhaoRelationName.size() > 0) {
                        zhaoRelationNameStr = "";
                        for (int i = 0; i < zhaoRelationName.size(); i++) {
                            zhaoRelationNameStr += zhaoRelationName.getString(i);
                            zhaoRelationNameStr += ",";
                        }
                        zhaoRelationNameStr = zhaoRelationNameStr.substring(0, zhaoRelationNameStr.length() - 1);
                    }
                    //招标单位联系电话
                    JSONArray zhaoRelationWay = object.getJSONArray("zhaoRelationWay");
                    String zhaoRelationWayStr = null;
                    if (zhaoRelationWay != null && zhaoRelationWay.size() > 0) {
                        zhaoRelationWayStr = "";
                        for (int i = 0; i < zhaoRelationWay.size(); i++) {
                            zhaoRelationWayStr += zhaoRelationWay.getString(i);
                            zhaoRelationWayStr += ",";
                        }
                        zhaoRelationWayStr = zhaoRelationWayStr.substring(0, zhaoRelationWayStr.length() - 1);
                    }

                    //代理单位
                    JSONArray agentUnit = object.getJSONArray("agentUnit");
                    String agentUnitStr = null;
                    if (agentUnit != null && agentUnit.size() > 0) {
                        agentUnitStr = "";
                        for (int i = 0; i < agentUnit.size(); i++) {
                            agentUnitStr += agentUnit.getString(i);
                            agentUnitStr += ",";
                        }
                        agentUnitStr = agentUnitStr.substring(0, agentUnitStr.length() - 1);
                    }
                    //代理单位联系人
                    JSONArray agentRelationName = object.getJSONArray("agentRelationName");
                    String agentRelationNameStr = null;
                    if (agentRelationName != null && agentRelationName.size() > 0) {
                        agentRelationNameStr = "";
                        for (int i = 0; i < agentRelationName.size(); i++) {
                            agentRelationNameStr += agentRelationName.getString(i);
                            agentRelationNameStr += ",";
                        }
                        agentRelationNameStr = agentRelationNameStr.substring(0, agentRelationNameStr.length() - 1);
                    }
                    //代理单位联系电话
                    JSONArray agentRelationWay = object.getJSONArray("agentRelationWay");
                    String agentRelationWayStr = null;
                    if (agentRelationWay != null && agentRelationWay.size() > 0) {
                        agentRelationWayStr = "";
                        for (int i = 0; i < agentRelationWay.size(); i++) {
                            agentRelationWayStr += agentRelationWay.getString(i);
                            agentRelationWayStr += ",";
                        }
                        agentRelationWayStr = agentRelationWayStr.substring(0, agentRelationWayStr.length() - 1);
                    }
                    //预算
                    JSONArray budget = object.getJSONArray("budget");
                    String budgetStr = null;
                    String budgetUnit = null;
                    if (budget != null && budget.size() > 0) {
                        budgetStr = "";
                        budgetUnit = "";
                        for (int i = 0; i < budget.size(); i++) {
                            budgetStr += budget.getJSONObject(i).getString("amount");
                            budgetStr += ",";
                            budgetUnit += budget.getJSONObject(i).getString("unit");
                            budgetUnit += ",";
                        }
                        budgetStr = budgetStr.substring(0, budgetStr.length() - 1);
                        budgetUnit = budgetUnit.substring(0, budgetUnit.length() - 1);
                    }
                    //中标金额
                    JSONArray winnerAmount = object.getJSONArray("winnerAmount");
                    String winnerAmountStr = null;
                    String winnerAmountUnit = null;
                    if (winnerAmount != null && winnerAmount.size() > 0) {
                        winnerAmountStr = "";
                        winnerAmountUnit = "";
                        for (int i = 0; i < winnerAmount.size(); i++) {
                            winnerAmountStr += winnerAmount.getJSONObject(i).getString("amount");
                            winnerAmountStr += ",";
                            winnerAmountUnit += winnerAmount.getJSONObject(i).getString("unit");
                            winnerAmountUnit += ",";
                        }
                        winnerAmountStr = winnerAmountStr.substring(0, winnerAmountStr.length() - 1);
                        winnerAmountUnit = winnerAmountUnit.substring(0, winnerAmountUnit.length() - 1);
                    }

                    JSONArray infoFile = object.getJSONArray("infoFile");
                    String infoFileStr = null;
                    if (infoFile != null && infoFile.size() > 0) {
                        infoFileStr = "";
                        for (int i = 0; i < infoFile.size(); i++) {
                            infoFileStr += infoFile.getString(i);
                            infoFileStr += ",";
                        }
                        infoFileStr = infoFileStr.substring(0, infoFileStr.length() - 1);
                    }*/

                String openBidingTime = object.getString("openBidingTime");
                String bidingAcquireTime = object.getString("bidingAcquireTime");
                String bidingEndTime = object.getString("bidingEndTime");
                String tenderBeginTime = object.getString("tenderBeginTime");
                String tenderEndTime = object.getString("tenderEndTime");
                String isElectronic = object.getString("isElectronic");
                String infoType = object.getString("infoType");

                String infoTitle = object.getString("infoTitle");
                String infoPublishTime = object.getString("infoPublishTime");
                String infoQianlimaUrl = object.getString("infoQianlimaUrl");
                String areaProvince = object.getString("areaProvince");
                String areaCity = object.getString("areaCity");
                String areaCountry = object.getString("areaCountry");
                String xmNumber = object.getString("xmNumber");
                String biddingType = object.getString("biddingType");
                String keywords = object.getString("keywords");
                String infoUrl = object.getString("keywordsCode");
                String xlfKeywords = object.getString("xlfKeywords");
                String infoWebsite = object.getString("target");

                //中国电信独有
//                    String amountTag = object.getString("amountTag");
//                    openBidingTime = amountTag;
//
//
//                    String opportunityTag = object.getString("opportunityTag");
//                    bidingAcquireTime = opportunityTag;
//
//                    String competorTag = object.getString("competorTag");
//                    bidingEndTime = competorTag;
//
//                    String dataType = object.getString("dataType");
//                    tenderBeginTime = dataType;
//
//                    String infoTypeSegment = object.getString("infoTypeSegment");
//                    tenderEndTime = infoTypeSegment;

                // if(infoTitle.contains("...")){
                bdJdbcTemplate.update("insert into han_crm_user (" +
                                "taskid,infoId,infoTitle,infoType,infoPublishTime,infoQianlimaUrl," +
                                "areaProvince,areaCity,areaCountry,xmNumber," +
                                "zhongBiaoUnit,zhongRelationName,zhongRelationWay," +
                                "openBidingTime,bidingAcquireTime,bidingEndTime,tenderBeginTime,tenderEndTime,isElectronic," +
                                "biddingType,zhaoBiaoUnit,zhaoRelationName,zhaoRelationWay," +
                                "agentUnit,agentRelationName," +
                                "agentRelationWay,budget,budgetUnit,winnerAmount,winnerAmountUnit,infoFile,keywords, infoUrl, infoWebsite" +
                                ",xlfKeywords) " +
                                " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        "21", infoId, infoTitle, infoType, infoPublishTime, infoQianlimaUrl,
                        areaProvince, areaCity, areaCountry, xmNumber,
                        "", "", "",
                        openBidingTime, bidingAcquireTime, bidingEndTime, tenderBeginTime, tenderEndTime, isElectronic,
                        biddingType, "", "", "", "", "",
                        "", "", "", "", "",
                        "", keywords, infoUrl, infoWebsite, xlfKeywords);
/*
                    bdJdbcTemplate.update("insert into han_crm_user (" +
                                    "taskid,infoId,infoTitle,infoType,infoPublishTime,infoQianlimaUrl," +
                                    "areaProvince,areaCity,areaCountry,xmNumber," +
                                    "zhongBiaoUnit,zhongRelationName,zhongRelationWay," +
                                    "openBidingTime,bidingAcquireTime,bidingEndTime,tenderBeginTime,tenderEndTime,isElectronic," +
                                    "biddingType,zhaoBiaoUnit,zhaoRelationName,zhaoRelationWay," +
                                    "agentUnit,agentRelationName," +
                                    "agentRelationWay,budget,budgetUnit,winnerAmount,winnerAmountUnit,infoFile,keywords, infoUrl, infoWebsite" +
                                    ",xlfKeywords) " +
                                    " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                            "21",infoId, infoTitle, infoType, infoPublishTime, infoQianlimaUrl,
                            areaProvince, areaCity, areaCountry, xmNumber,
                            zhongBiaoUnitStr, zhongRelationNameStr, zhongRelationWayStr,
                            openBidingTime, bidingAcquireTime, bidingEndTime, tenderBeginTime, tenderEndTime, isElectronic,
                            biddingType,zhaoBiaoUnitStr,zhaoRelationNameStr,zhaoRelationWayStr,agentUnitStr,agentRelationNameStr,
                            agentRelationWayStr,budgetStr,budgetUnit,winnerAmountStr,winnerAmountUnit,
                            infoFileStr,keywords,infoUrl, infoWebsite,xlfKeywords);
*/

                count = count + 1;
                list.add(infoId);
                //  }
            }
            //}
            log.info("第：{}条～～～～～～～～～～～～～～～～", tiaoShu);
        }
    }
}
