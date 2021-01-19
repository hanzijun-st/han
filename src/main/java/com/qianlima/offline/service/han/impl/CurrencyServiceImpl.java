package com.qianlima.offline.service.han.impl;

import com.qianlima.offline.bean.Area;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.bean.Params;
import com.qianlima.offline.middleground.NewZhongTaiService;
import com.qianlima.offline.rule02.MyRuleUtils;
import com.qianlima.offline.service.CusDataFieldService;
import com.qianlima.offline.service.ZhongTaiBiaoDiWuService;
import com.qianlima.offline.service.han.CurrencyService;
import com.qianlima.offline.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Administrator on 2021/1/14.
 */
@Service
@Slf4j
public class CurrencyServiceImpl implements CurrencyService {
    @Autowired
    private ContentSolr contentSolr;

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;

    @Autowired
    private NewZhongTaiService newZhongTaiService;

    @Autowired
    private ZhongTaiBiaoDiWuService bdwService;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    private CusDataFieldService cusDataFieldService;

    @Autowired
    private CleanUtils cleanUtils;

    @Autowired
    private MyRuleUtils myRuleUtils;

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
    public String INSERT_ZT_RESULT_HXR3 = "INSERT INTO zt_data_result_poc_table_2_copy (id,task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time,is_electronic,code,isfile,keyword_term) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    //测试批量插入
    public String INSERT_HAN_ALL_TEST = "INSERT INTO han_tab_all_copy (id,json_id,contentid,content_source,sum,sumUnit,serialNumber,name," +
            "brand,model,number,numberUnit,price,priceUnit,totalPrice,totalPriceUnit,configuration_key,configuration_value,appendix_suffix) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
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
     * 判断 1：全部、2招标、3中标
     * @param str
     * @return
     */
    @Override
    public String getProgidStr(String str) {
        if ("1".equals(str)){
            //全部
            str = "[0 TO 3] OR progid:5";
        }else if ("2".equals(str)){
            //招标
            str = "[0 TO 2]";
        }else if ("3".equals(str)){
            //中标
            str = "3 OR progid:5";
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
            String string = "yyyymmdd:["+time1 + " TO "+time2 + "] AND (progid:"+progidStr+")"+" AND catid:[* TO 100] AND "+titleOrAllcontent;
            for (String str : keyWords) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian(string+":\""+str+"\"", str, 2);
                    log.info(str.trim() + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (params.getIsHave() !=null && params.getIsHave().intValue() ==1){
                                    for (String black : blacks) {
                                        if(StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)){
                                            flag = false;
                                            break;
                                        }
                                    }
                                }
                                if (flag){
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

            //如果参数为1,则进行存表
            if (params.getIsSave() !=null && params.getIsSave().intValue() == 1){
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
            System.out.println("--------------------------------本次任务结束---------------------------------------");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getBdw() {
        try {
            bdwService.getSolrAllField2("hBdw");
        } catch (IOException e) {
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
            DBUtil.insertAll(INSERT_HAN_ALL_TEST,maps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getBiaoQian() {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20201222 TO 20201231] AND (progid:3 OR progid:5) AND zhaoBiaoUnit:*", "", 1);
        if (!mqEntities.isEmpty()) {
            for (NoticeMQ data : mqEntities) {
                if (data.getTitle() != null) {
                    futureList1.add(executorService1.submit(() -> {
                        boolean flag = true;
                        if (flag) {
                            String zhaobiaoindustry = myRuleUtils.getIndustry(data.getZhaoBiaoUnit());
                            if (StringUtils.isNotBlank(zhaobiaoindustry)){
                                if ("商业公司-文化".equals(zhaobiaoindustry) || "商业公司-旅游".equals(zhaobiaoindustry) || "政府机构-文化和旅游".equals(zhaobiaoindustry)){
                                    list1.add(data);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
                                }
                            }
                        }
                    }));
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

    @Override
    public void getPpei() {
        try {
            List<String> listIds = new ArrayList<>();//所有的id
            List<String> listIds2 = new ArrayList<>();//已经存在的数据
            List<String> resultList = new ArrayList<>();
            List<Map<String, Object>> maps = bdJdbcTemplate.queryForList("SELECT * FROM ali_item_info");
            List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT * FROM zt_data_result_poc_table");
            if(maps !=null){
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
            if (keys !=null && keys.size() >0){
                for (String key : keys) {
                    futureList1.add(executorService1.submit(() -> {
                        NoticeMQ noticeMQ = new NoticeMQ();
                        noticeMQ.setContentid(Long.valueOf(key));
                        //中台获取数据
                        Map<String, Object> allFieldsWithOther = cusDataFieldService.getAllFieldsWithOther(noticeMQ, false);
                        if (allFieldsWithOther != null && allFieldsWithOther.size() >0) {
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
                    log.info("-----------------------执行的contentid:{}",key);
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
            System.out.println("==============("+resultList.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getPpeiJy() {
        //读取到mysql数据
        List<Map<String, Object>> maps = bdJdbcTemplate.queryForList("SELECT * FROM zt_data_result_poc_table");
        // task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
        //"xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way," +
        //  " agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<Future> futureList1 = new ArrayList<>();

        for (Map<String, Object> map : maps) {
            futureList1.add(executorService1.submit(() -> {
                if (map.get("title") !=null){
                    map.put("title",cleanUtils.cleanTitle(map.get("title").toString()));//标题
                }
                if (map.get("agent_unit") !=null){
                    map.put("agent_unit",cleanUtils.cleanAgentUnit(map.get("agent_unit").toString()));//代理机构
                }
                if (map.get("zhao_biao_unit") !=null){
                    map.put("zhao_biao_unit",cleanUtils.cleanZhaoBiaoUnit(map.get("zhao_biao_unit").toString()));//招标单位
                }
                if (map.get("zhong_biao_unit") !=null){
                    map.put("zhong_biao_unit",cleanUtils.cleanZhongBiaoUnit(map.get("zhong_biao_unit").toString()));//中标单位
                }
                if (map.get("xmNumber") !=null){
                    map.put("xmNumber",cleanUtils.cleanXmNumber(map.get("xmNumber").toString()));//项目编号
                }
                if (map.get("province") !=null){
                    map.put("province",cleanUtils.cleanProvince(map.get("province").toString()));//处理省市县
                }
                if (map.get("amount_unit") !=null){
                    map.put("amount_unit",cleanUtils.cleanAmount(map.get("amount_unit").toString()));//中标金额
                }
                if (map.get("baiLian_amount_unit") !=null){
                    map.put("baiLian_amount_unit",cleanUtils.cleanAmount(map.get("baiLian_amount_unit").toString()));//中标金额（百炼）
                }
                if (map.get("budget") !=null){
                    map.put("budget",cleanUtils.cleanAmount(map.get("budget").toString()));//招标预算
                }
                if (map.get("baiLian_budget") !=null){
                    map.put("baiLian_budget",cleanUtils.cleanAmount(map.get("baiLian_budget").toString()));//招标预算（百炼）
                }
                if (map.get("relation_name") !=null){
                    map.put("relation_name",cleanUtils.cleanLinkMan(map.get("relation_name").toString()));//招标单位联系人
                }
                if (map.get("link_man") !=null){
                    map.put("link_man",cleanUtils.cleanLinkMan(map.get("link_man").toString()));//招标单位联系人
                }
                if (map.get("relation_way") !=null){
                    map.put("relation_way",cleanUtils.cleanLinkWay(map.get("relation_way").toString()));//招标单位联系人电话
                }
                if (map.get("link_phone") !=null){
                    map.put("link_phone",cleanUtils.cleanLinkWay(map.get("link_phone").toString()));//中标单位联系人电话
                }

                if (map.get("agent_relation_ame") !=null){
                    map.put("agent_relation_ame",cleanUtils.cleanLinkMan(map.get("agent_relation_ame").toString()));//代理单位联系人
                }
                if (map.get("agent_relation_way") !=null){
                    map.put("agent_relation_way",cleanUtils.cleanLinkWay(map.get("agent_relation_way").toString()));//代理单位联系人电话
                }
                if (map.get("registration_begin_time") !=null){
                    map.put("registration_begin_time",DateUtils.parseDateFromDateStr(cleanUtils.cleanDateTime(map.get("registration_begin_time").toString())));//时间处理
                }
                if (map.get("registration_end_time") !=null){
                    map.put("registration_end_time",DateUtils.parseDateFromDateStr(cleanUtils.cleanDateTime(map.get("registration_end_time").toString())));//时间处理
                }
                if (map.get("biding_acquire_time") !=null){
                    map.put("biding_acquire_time",DateUtils.parseDateFromDateStr(cleanUtils.cleanDateTime(map.get("biding_acquire_time").toString())));//时间处理
                }
                if (map.get("biding_end_time") !=null){
                    map.put("biding_end_time",DateUtils.parseDateFromDateStr(cleanUtils.cleanDateTime(map.get("biding_end_time").toString())));//时间处理
                }
                if (map.get("tender_begin_time") !=null){
                    map.put("tender_begin_time",DateUtils.parseDateFromDateStr(cleanUtils.cleanDateTime(map.get("tender_begin_time").toString())));//时间处理
                }
                if (map.get("tender_end_time") !=null){
                    map.put("tender_end_time",DateUtils.parseDateFromDateStr(cleanUtils.cleanDateTime(map.get("tender_end_time").toString())));//时间处理
                }
                if (map.get("update_time") !=null){
                    map.put("update_time",DateUtils.parseDateFromDateStr(cleanUtils.cleanDateTime(map.get("update_time").toString())));//时间处理
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

        if (maps !=null && maps.size() >0){
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (Map<String, Object> map : maps) {
                futureList.add(executorService.submit(() ->  saveIntoMysql2(map)));
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
        /*try {
            List<Map<String,Object>> mList = new ArrayList<>();
            for (Map<String, Object> map : maps) {
                Map<String, Object> m = new HashMap<>();
                m.put("id",map.get("id"));
                m.put("task_id",map.get("task_id"));
                m.put("keyword",map.get("keyword"));
                m.put("content_id",map.get("content_id"));
                m.put("title",map.get("title"));
                m.put("content",map.get("content"));
                m.put("province",map.get("province"));
                m.put("city",map.get("city"));
                m.put("country",map.get("country"));
                m.put("url",map.get("url"));
                m.put("baiLian_budget",map.get("baiLian_budget"));
                m.put("baiLian_amount_unit",map.get("baiLian_amount_unit"));
                m.put("xmNumber",map.get("xmNumber"));
                m.put("bidding_type",map.get("bidding_type"));
                m.put("progid",map.get("progid"));
                m.put("zhao_biao_unit",map.get("zhao_biao_unit"));
                m.put("relation_name",map.get("relation_name"));
                m.put("relation_way",map.get("relation_way"));
                m.put("agent_unit",map.get("agent_unit"));
                m.put("agent_relation_ame",map.get("agent_relation_ame"));
                m.put("agent_relation_way",map.get("agent_relation_way"));
                m.put("zhong_biao_unit",map.get("zhong_biao_unit"));
                m.put("link_man",map.get("link_man"));
                m.put("link_phone",map.get("link_phone"));
                m.put("registration_begin_time",map.get("registration_begin_time"));
                m.put("registration_end_time",map.get("registration_end_time"));
                m.put("biding_acquire_time",map.get("biding_acquire_time"));
                m.put("biding_end_time",map.get("biding_end_time"));
                m.put("tender_begin_time",map.get("tender_begin_time"));
                m.put("tender_end_time",map.get("tender_end_time"));
                m.put("update_time",map.get("update_time"));
                m.put("type",map.get("type"));
                m.put("bidder",map.get("bidder"));
                m.put("notice_types",map.get("notice_types"));
                m.put("open_biding_time",map.get("open_biding_time"));
                m.put("is_electronic",map.get("is_electronic"));
                m.put("code",map.get("code"));
                m.put("isfile",map.get("isfile"));
                m.put("keyword_term",map.get("keyword_term"));
                mList.add(m);
            }
            DBUtil.insertAll(INSERT_ZT_RESULT_HXR3,mList);
            log.info("批量插入成功-------");
        } catch (SQLException e) {
            e.printStackTrace();
        }*/

    }

    @Override
    public String getLishiGetZhongTai(String contentId) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(60000)
                    .setSocketTimeout(60000).setConnectTimeout(60000).build();
            HttpGet get = new HttpGet("http://cusdata.qianlima.com/zt/api/"+contentId);
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

    public void saveIntoMysql2(Map<String, Object> map){
        bdJdbcTemplate.update(INSERT_ZT_RESULT_HXR2,map.get("task_id"), map.get("keyword"), map.get("content_id"), map.get("title"),
                map.get("content"), map.get("province"), map.get("city"), map.get("country"), map.get("url"), map.get("baiLian_budget"),
                map.get("baiLian_amount_unit"), map.get("xmNumber"), map.get("bidding_type"), map.get("progid"), map.get("zhao_biao_unit"),
                map.get("relation_name"), map.get("relation_way"), map.get("agent_unit"), map.get("agent_relation_ame"),
                map.get("agent_relation_way"), map.get("zhong_biao_unit"), map.get("link_man"), map.get("link_phone"),
                map.get("registration_begin_time"), map.get("registration_end_time"), map.get("biding_acquire_time"),
                map.get("biding_end_time"), map.get("tender_begin_time"), map.get("tender_end_time"), map.get("update_time"),
                map.get("type"), map.get("bidder"), map.get("notice_types"), map.get("open_biding_time"), map.get("is_electronic"),
                map.get("code"), map.get("isfile"), map.get("keyword_term"));
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
            saveIntoMysql(resultMap);
        }
    }


    /**
     * jdbc存表接口
     * @param map
     */
    public void saveIntoMysql(Map<String, Object> map){
        bdJdbcTemplate.update(INSERT_ZT_RESULT_HXR,map.get("task_id"), map.get("keyword"), map.get("content_id"), map.get("title"),
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
     * 去除标签
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
            if (StringUtils.isNotBlank(element.attr("href"))) {
                if (element.is("a")) {
                    element.remove();
                }
            }
        }
        return document.body().html();
    }

    //调取中台数据 并 匹配行业标签
    public void getDataFromZhongTaiAndSave6(NoticeMQ noticeMQ) {
        boolean result = cusDataFieldService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> map = cusDataFieldService.getAllFieldsWithZiTi(noticeMQ, false);
        if (map != null) {
            String zhaobiaounit = map.get("zhao_biao_unit") != null ? map.get("zhao_biao_unit").toString() : "";
            String task_id = map.get("task_id") != null ? map.get("task_id").toString() : "";
            if (task_id.equals("2")){
                String zhaobiaoindustry = myRuleUtils.getIndustry(zhaobiaounit);
                String[] zhaobiaosplit = zhaobiaoindustry.split("-");
                if (StringUtils.isNotBlank(zhaobiaounit)){
                    if ("商业公司-文化".equals(zhaobiaoindustry) || "商业公司-旅游".equals(zhaobiaoindustry) || "政府机构-文化和旅游".equals(zhaobiaoindustry)){
                        newZhongTaiService.saveIntoMysql(map);
                    }
                }
            }else {
                newZhongTaiService.saveIntoMysql(map);
            }
        }
    }
}
