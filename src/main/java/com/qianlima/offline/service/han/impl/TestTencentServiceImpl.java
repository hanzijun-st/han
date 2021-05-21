package com.qianlima.offline.service.han.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.offline.bean.ConstantBean;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.mapper.TestUserMapper;
import com.qianlima.offline.service.CusDataFieldService;
import com.qianlima.offline.service.han.CusDataNewService;
import com.qianlima.offline.service.han.TestMyBatisService;
import com.qianlima.offline.service.han.TestTencentService;
import com.qianlima.offline.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Slf4j
public class TestTencentServiceImpl implements TestTencentService {
    @Autowired
    private OnlineContentSolr onlineContentSolr;

    @Autowired
    private CusDataFieldService cusDataFieldService;

    @Autowired
    private CusDataNewService cusDataNewService;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;

    @Autowired
    @Qualifier("djeJdbcTemplate")
    private JdbcTemplate djeJdbcTemplate;



    @Autowired
    private TestUserMapper testUserMapper;

    //mysql数据库中插入数据
    public static final String INSERT_ZT_RESULT_TYPE = "INSERT INTO han_tencent (type,contentid,title) VALUES (?,?,?)";
    public static final String UPDATE_ZT_RESULT_TYPE = "UPDATE han_new_data_tf SET code2=? WHERE content_id =?";
    @Override
    public void saveTencent() {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<Future> futureList1 = new ArrayList<>();


        /*HashMap<String, String> simpleAreaMap = KeyUtils.getSimpleMap();
        Set<Map.Entry<String, String>> entries = simpleAreaMap.entrySet();//将map的key和value 进行映射成 集合*/
        List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT content_id,title FROM han_new_data_tf");

        String url ="http://monitor.ka.qianlima.com/api/infoType";
        if (mapList !=null && mapList.size() >0){
            for (Map<String, Object> map : mapList) {
                String contentid = map.get("content_id").toString();
                String title = map.get("title").toString();
                futureList1.add(executorService1.submit(() -> {
                    NoticeMQ noticeMQ = new NoticeMQ();
                    noticeMQ.setContentid(Long.valueOf(contentid));
                    // 获取正文字段
                    List<Map<String, Object>> contentList = gwJdbcTemplate.queryForList(ConstantBean.SELECT_ITEM_CONTENT_BY_CONTENTID, noticeMQ.getContentid().toString());
                    if (contentList == null && contentList.size() == 0){
                        return;
                    }
                    String content = contentList.get(0).get("content").toString();
                    //中台获取数据

                    String dataType2 = getDataType2(title, content, Long.valueOf(contentid), url);
                    Map<String,Object> m = new HashMap<>();
                    m.put("code2",dataType2);
                    m.put("content_id",contentid);
                    saveIntoMysqlTenxun(m,UPDATE_ZT_RESULT_TYPE);

                }));
                log.info("-----------------------执行的contentid:{}",contentid);
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
        log.info("-->{}",StrUtil.getPutStr());
    }


    //企业
    public static final String INSERT_QY = "INSERT INTO han_qy (first_qy,end_qy,level,route) VALUES (?,?,?,?)";

    @Override
    public void jsonTo() {
        List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT * FROM han_api");
        for (Map<String, Object> map : mapList) {
            String first_qy = map.get("qy_name").toString();//起始企业
            String end_qy = "";//最大企业
            int level =0;//层级
            String route ="";//路径
            String json_data = map.get("json_data").toString();//json
            Map jsonMap = JsonUtil.jsonToBean(json_data, Map.class);
            if (jsonMap !=null){
                if (jsonMap.get("result") !=null && StringUtils.isNotEmpty(jsonMap.get("result").toString())){
                    String result = jsonMap.get("result").toString();
                    Map m = JsonUtil.jsonToBean(result, Map.class);
                    if (m !=null){
                        String pathMapStr = m.get("pathMap").toString();
                        Map path = JsonUtil.jsonToBean(pathMapStr, Map.class);
                        if (path !=null){
                            String p = path.get("p_1").toString();
                            Map mp = JsonUtil.jsonToBean(p, Map.class);
                            if (mp !=null){
                                String nodes = mp.get("nodes").toString();
                                if (StringUtils.isNotEmpty(nodes)){
                                    List<Map<String, Object>> mapList1 = JsonUtil.jsonToListMap(nodes);
                                    if (!CollectionUtils.isEmpty(mapList1)){
                                        int i =0;
                                        for (Map<String, Object> strMap : mapList1) {
                                            String properties = strMap.get("properties").toString();
                                            if (StringUtils.isNotEmpty(properties)){
                                                Map na = JsonUtil.jsonToBean(properties, Map.class);
                                                if (na !=null){
                                                    String name = na.get("name").toString();
                                                    route += name+"/";
                                                    if (i ==0){
                                                        end_qy=name;
                                                    }
                                                    if (mapList1.size() >0){
                                                        level = mapList1.size()-1;
                                                    }
                                                    i++;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }else{
                log.info("json数据为空");
            }
            if (StringUtils.isNotEmpty(route)){
                route = route.substring(0,route.length()-1);
                route = getString(route);
            }
            //存库
            bdJdbcTemplate.update(INSERT_QY,first_qy,end_qy,level,route);
            log.info("存企业的mysql数据库进度--->{}",first_qy);
        }
    }

    public static final String INSERT_ZT_RESULT = "INSERT INTO han_new_data (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time," +
            " is_electronic,code,isfile,keyword_term,keywords, infoTypeSegment,,monitorUrl, pocDetailUrl) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String INSERT_ZT_DJE = "INSERT INTO han_new_data (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time," +
            "is_electronic,code2,isfile,keyword_term,keywords, infoTypeSegment,monitorUrl, pocDetailUrl) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    /**
     * 大金额专用
     */
    private static final String INSERT_ZT_DJE_ZHUANYONG = "INSERT INTO han_new_dajine (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time," +
            "is_electronic,code2,isfile,keyword_term,keywords, infoTypeSegment,monitorUrl, pocDetailUrl,old_winner_amount,new_winner_amount,old_budget,new_budget) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    @Override
    public void toIds() throws Exception{
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<String> idsFile = LogUtils.readRule("idsFile");

        for (String s : idsFile) {
            futureList1.add(executorService1.submit(() -> {
                boolean b = cusDataFieldService.checkStatus(s);//范围 例如:全国
                if (!b) {
                    log.info("contentid:{} 对应的数据状态不是99, 丢弃", s);
                    return;
                }

                NoticeMQ noticeMQ = new NoticeMQ();
                noticeMQ.setContentid(Long.valueOf(s));
                //全部自提，不需要正文
                try {
                    Map<String, Object> resultMap = cusDataFieldService.getAllFieldsWithZiTi(noticeMQ, false);
                    if (resultMap != null) {
                        //String content = cusDataNewService.getContent(noticeMQ);//获取正文字段
                        //resultMap.put("content",content);
                        //saveIntoMysqlDje(resultMap);
                        cusDataNewService.saveIntoMysql(resultMap);
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }
        log.info("--------该接口运行结束--------");
    }

    public String INSERT_ZT_RESULT_TEST2 = "INSERT INTO han_test2 (task_id,keyword)  VALUES (?,?)";
    @Override
    @Transactional
    public void saveData2(List<Map> maps) {
        if (maps.size()>0){
            for (Map map : maps) {
                bdJdbcTemplate.update(INSERT_ZT_RESULT_TEST2,map.get("task_id").toString(), map.get("keyword").toString());
            }
        }
    }

    @Override
    public void getNewAddress() throws Exception{
        ExecutorService executorService1 = Executors.newFixedThreadPool(30);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT content_id,title FROM han_data");
        for (Map<String, Object> map : mapList) {
            futureList1.add(executorService1.submit(() -> {
                try {
                    String contentid = String.valueOf(map.get("content_id"));
                    NoticeMQ noticeMQ = new NoticeMQ();
                    noticeMQ.setContentid(Long.valueOf(contentid));
                    String content = cusDataNewService.getContent(noticeMQ);//获取正文字段
                    String title = map.get("title").toString();//标题

                    HttpClient client = new DefaultHttpClient();
                    HttpResponse response = null;
                    String url = "http://localhost:8080/area";

                    HttpPost post = new HttpPost(url);
                    post.setHeader("Content-Type", "application/json");

                    JSONObject params = new JSONObject();
                    params.put("contentId", contentid);
                    params.put("content", content);
                    params.put("title", title);
                    post.setEntity(new StringEntity(params.toString(), StandardCharsets.UTF_8));

                    response = client.execute(post);
                    String ret = EntityUtils.toString(response.getEntity(), "UTF-8");
                    Map map1 = JsonUtil.jsonToBean(ret, Map.class);
                    String areaId = map1.get("data").toString();
                    log.info("返回的结果---"+areaId);
                    Map<String, String> areaMap = getAreaMap(areaId);
                    if (areaMap !=null){
                        areaMap.put("contentid",contentid);
                        //saveIntoArea(areaMap,INSERT_AREA);

                    }
                } catch (IOException e) {
                    e.getMessage();
                }
            }));
           };
    }
    public void getNewAddressToQs() throws Exception{

        List<String> qsList = new ArrayList<>();//缺失
        List<String> list = new ArrayList<>();//新接口调用为空
        List<String> dataList = new ArrayList<>();//新接口调用 getData 后为空
        //ExecutorService executorService1 = Executors.newFixedThreadPool(30);//开启线程池
        //List<Future> futureList1 = new ArrayList<>();
        //List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT content_id,title FROM han_data");
        List<String> idsFile = LogUtils.readRule("idsFile");
        //for (Map<String, Object> map : mapList) {\
        for (String s : idsFile) {
            //futureList1.add(executorService1.submit(() -> {
                try {
                    //String contentid = String.valueOf(map.get("content_id"));
                    String contentid = s;
                    NoticeMQ noticeMQ = new NoticeMQ();
                    noticeMQ.setContentid(Long.valueOf(contentid));
                    Map<String, Object> resultMap = cusDataFieldService.getAllFieldsWithZiTi(noticeMQ, false);
                    String content = cusDataNewService.getContent(noticeMQ);//获取正文字段
                    String title = resultMap.get("title").toString();//标题

                    HttpClient client = new DefaultHttpClient();
                    HttpResponse response = null;
                    String url = "http://172.18.30.33:9091/area";

                    HttpPost post = new HttpPost(url);
                    post.setHeader("Content-Type", "application/json");

                    JSONObject params = new JSONObject();
                    params.put("contentId", contentid);
                    params.put("content", content);
                    params.put("title", title);
                    post.setEntity(new StringEntity(params.toString(), StandardCharsets.UTF_8));

                    response = client.execute(post);
                    String ret = EntityUtils.toString(response.getEntity(), "UTF-8");
                    Map map1 = JsonUtil.jsonToBean(ret, Map.class);
                    if (map1.get("data") ==null){
                        list.add(contentid);
                    }else {
                        String areaId = map1.get("data").toString();
                        if (StringUtils.isBlank(areaId)){
                            dataList.add(areaId);
                        }else {
                            Map<String, String> areaMap = getAreaMap(areaId);
                            if (areaMap !=null){
                                qsList.add(contentid);
                                //areaMap.put("contentid",contentid);
                                //saveIntoArea(areaMap,INSERT_AREA);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.getMessage();
                }
            //}));
           };
        log.info("得到的最后集合");
            //bdJdbcTemplate.update(HANG_YE,zhaobiaounit,firstLevel, secondLevel);

    }

    @Override
    public Map getNewAddressByContentId(String contentId) throws Exception{
        try {
            NoticeMQ noticeMQ = new NoticeMQ();
            noticeMQ.setContentid(Long.valueOf(contentId));

            //调用中台接口
            Map<String, Object> map = cusDataFieldService.getAllFieldsWithZiTi(noticeMQ, false);
            if (map !=null){
                //获取正文字段
                String content = cusDataNewService.getContent(noticeMQ);//获取正文字段
                String title = map.get("title").toString();//标题

                HttpClient client = new DefaultHttpClient();
                HttpResponse response = null;
                String url = "http://localhost:8080/area";//调用新地址接口（这个接口需要单独本地启动）

                HttpPost post = new HttpPost(url);
                post.setHeader("Content-Type", "application/json");

                JSONObject params = new JSONObject();
                params.put("contentId", contentId);
                params.put("content", content);
                params.put("title", title);
                post.setEntity(new StringEntity(params.toString(), StandardCharsets.UTF_8));

                response = client.execute(post);
                String ret = EntityUtils.toString(response.getEntity(), "UTF-8");
                Map map1 = JsonUtil.jsonToBean(ret, Map.class);
                String areaId = map1.get("data").toString();
                if (StringUtils.isBlank(areaId)){
                    log.error("获取新地址为空，contentId为:{}",contentId);
                    return null;
                }
                Map<String, String> areaMap = getAreaMap(areaId);
                if (areaMap !=null){
                    areaMap.put("contentid",contentId);
                }
                return areaMap;
            }
            log.error("调用中台接口未获取到对应数据，contentId为:{}",contentId);
            return null;
        } catch (IOException e) {
            e.getMessage();
        }
        return null;
    }


    @Override
    public void getLinShi(String date) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();
        try {
            //关键词a
            List<String> aa = LogUtils.readRule("keyWordsA");

            //关键词b
            List<String> bb = LogUtils.readRule("keyWordsB");

            List<String> blacks = LogUtils.readRule("xlf");

            //自提招标单位检索“行业标签”中标黄部分  AND  标题检索关键词aa
            for (String a : aa) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND title:\"" + a + "\"",a, 1);
                    log.info(a+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                for (String black : blacks) {
                                    if(StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)){
                                        flag = false;
                                        break;
                                    }
                                }
                                if (flag) {
                                    listAll.add(data);
                                    data.setKeyword(a);
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
            for (String b : bb) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + b + "\"",b, 1);
                    log.info(b+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                for (String black : blacks) {
                                    if(StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)){
                                        flag = false;
                                        break;
                                    }
                                }
                                if (flag) {
                                    listAll.add(data);
                                    data.setKeyword(b);
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


           /* ArrayList<String> arrayList = new ArrayList<>();

            //关键词aa
            for (String a : aa) {
                arrayList.add(a);
            }
            for (String b : bb) {
                arrayList.add(b);
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
            System.out.println("去重之后的数据量：" + list.size());*/


            //if (type.intValue() == 1){
                if (list != null && list.size() > 0) {
                    ExecutorService executorService = Executors.newFixedThreadPool(60);
                    List<Future> futureList = new ArrayList<>();

                    for (NoticeMQ content : list) {
                        futureList.add(executorService.submit(() -> {
                            try {
                                getZhongTaiDatasAndSave(content);
                            } catch (Exception e) {

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
          //  }
            System.out.println("==========================================此程序运行结束========================================");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getKaisixuanda(String date, Integer type) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();
        //关键词a
        String[] a ={"CT机","CT定位穿刺","X射线头部CT机","全身CT机","螺旋CT机","螺旋扇扫CT机","数字减影","血管造影","CT影像处理","血管造影导管","血管造影X射线机","血管造影X射线设备","血管造影影像处理","正电子发射系统","磁共振成像设备","永磁型磁共振成像系统","常导型磁共振成像系统","超导型磁共振成像系统","医用磁共振成像系统","磁共振辅助设备","磁共振定位装置","磁共振辅助刺激系统","磁共振造影注射装置","磁共振高压注射器","磁共振造影剂","磁共振用高压注射连接管","磁共振高压造影注射系统","磁共振高压造影注射器","磁共振成像辅助刺激系统","磁共振乳腺线圈穿刺固定架","正电子发射断层扫描","正电子发射磁共振成像系统","磁共振影像处理","CT造影","X射线计算机体层摄影设备","胃肠道造影显像剂","X射线计算机断层成像系统","CT影像处理软件","X射线血管造影影像处理软件","数字化X射线影像处理软件","超声影像引导系统","磁共振成像系统"};

        try {
            for (String str : a) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 3] AND allcontent:\"" + str + "\"", "", 1);
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

            ArrayList<String> arrayList = new ArrayList<>();
            //关键词a
            for (String key : a) {
                arrayList.add(key);
            }

            for (String str : arrayList) {
                int total = 0;
                for (NoticeMQ noticeMQ : listAll) {
                    String keyword = noticeMQ.getKeyword();
                    if (StringUtils.isNotBlank(keyword)){
                        if (keyword.equals(str)) {
                            total++;
                        }
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

        //如果参数为1,则进行存表
        if (type.intValue() ==1) {
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(60);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSaveTy(content)));
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
    public void getKaisixuanda2(String date, Integer type) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();
        try {
            //关键词a
            String[] aa ={"CT","MR","DSA"};

            //关键词b
            String[] bb ={"医院","诊所","门诊","保健院","健康委员会","医学院","体检中心","健康局","医院部","药房","卫生院","医疗保障局","合作医疗","医药服务管理司","兽医实验室","医药","精神病院","防治院","血液中心","眼科中心","治疗中心","保健中心","保健所","血管病研究所","防治所","外科中心","康复中心","透析中心","正畸中心","荣军院","防治中心","保健站","列腺病研究所","职业病院","防治站","产院","急救中心","卫生局","卫生厅","防治办公室","卫生保健中心","医疗中心","卫生中心","门诊部","卫生服务站","医检所","制剂室","药交所","眼科","医保","医疗保障","卫健委","戒毒所","敬老院","疗养院","眼病防治所","矫治所","结核病防治所","休养所","血站","福利院","医疗机构","病防治办公室","计划生育","生育委员","计生委","大健康","同仁堂","江中集团","医学","健康科技","养生堂","保健品","诊断","康宁","制药","药业","药集团","医疗集团","精神卫生","药店","军医","医用","医疗","诊疗","残联","医护","卫生所","卫生院 ","卫生院校","医科大学","妇幼","健康中心","运动康复","中医馆","预防控制","医务室"};


            for (String a : aa) {
                for (String b : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND progid:[0 TO 3]  AND allcontent:\"" + a + "\"  AND zhaoBiaoUnit:\"" + b + "\"", a+"&"+b, 1);
                        log.info(a.trim()+"&"+b + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    if (flag){
                                        listAll.add(data);
                                        data.setKeyword(a+"&"+b);
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
            for (String a : aa) {
                for (String b : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND progid:[0 TO 3]  AND allcontent:\"" + a + "\"  AND title:\"" + b + "\"", a+"&"+b, 2);
                        log.info(a.trim()+"&"+b + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    if (flag){
                                        listAll.add(data);
                                        data.setKeyword(a+"&"+b);
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重之后的数据量：" + list.size());
            log.info("==========================");


            ArrayList<String> arrayList = new ArrayList<>();

            //关键词aa
            for (String a : aa) {
                for(String b : bb){
                    arrayList.add(a+"&"+b);
                }
            }

            for (String str : arrayList) {
                int total = 0;
                for (NoticeMQ noticeMQ : listAll) {
                    String keyword = noticeMQ.getKeyword();
                    if (StringUtils.isNotBlank(keyword)){
                        if (keyword.equals(str)) {
                            total++;
                        }
                    }
                }
                if (total == 0) {
                    continue;
                }
                System.out.println(str + ": " + total);
            }
            System.out.println("全部数据量：" + listAll.size());
            System.out.println("去重之后的数据量：" + list.size());

            if (type.intValue() == 1) {
                if (list != null && list.size() > 0) {
                    ExecutorService executorService = Executors.newFixedThreadPool(60);
                    List<Future> futureList = new ArrayList<>();

                    for (NoticeMQ content : list) {
                        futureList.add(executorService.submit(() -> {
                            try {
                                getZhongTaiDatasAndSaveTy(content);
                            } catch (Exception e) {

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
            }
            System.out.println("==========================================此程序运行结束========================================");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getTongfangWeiShi(String date, Integer type) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();
            //关键词a
            String[] aa ={"海关","广东分署","民航","民用航空","边检","边防检查","检验检疫","缉私","走私","口岸","机场"};

            try {
                for (String str : aa) {
                    futureList1.add(executorService1.submit(() -> {
                        //自提招标单位
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND zhaoBiaoUnit:\"" + str + "\"", "", 1);
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

                ArrayList<String> arrayList = new ArrayList<>();
                //关键词a
                for (String key : aa) {
                    arrayList.add(key);
                }

                for (String str : arrayList) {
                    int total = 0;
                    for (NoticeMQ noticeMQ : listAll) {
                        String keyword = noticeMQ.getKeyword();
                        if (StringUtils.isNotBlank(keyword)){
                            if (keyword.equals(str)) {
                                total++;
                            }
                        }
                    }
                    if (total == 0) {
                        continue;
                    }
                    System.out.println(str + ": " + total);
                }
                System.out.println("全部数据量：" + listAll.size());
                System.out.println("去重之后的数据量：" + list.size());

            if (type.intValue() == 1) {
                if (list != null && list.size() > 0) {
                    ExecutorService executorService = Executors.newFixedThreadPool(60);
                    List<Future> futureList = new ArrayList<>();
                    for (NoticeMQ content : list) {
                        futureList.add(executorService.submit(() -> {
                            try {
                                getZhongTaiDatasAndSaveTy(content);
                            } catch (Exception e) {
                                log.info("调用中台接口异常:{}",e);
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
            }
            System.out.println("==========================================此程序运行结束========================================");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getTongfangWeiShi2(String date, Integer type) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();
        //关键词a
        String[] aa ={"出口加工","跨境电商","中欧班列","中哈霍尔果斯","公共安全","检疫实验室仪器设备","缉私类升级改造项目","监管查验技术设备","海关科技信息化建设","海关监管","保税区","保税港区","综保区","综合保税区","保税物流","珠澳跨境工业区","海关关务"};

        try {
            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND zhaoBiaoUnit:\"" + str + "\"", "", 1);
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

            ArrayList<String> arrayList = new ArrayList<>();
            //关键词a
            for (String key : aa) {
                arrayList.add(key);
            }

            for (String str : arrayList) {
                int total = 0;
                for (NoticeMQ noticeMQ : listAll) {
                    String keyword = noticeMQ.getKeyword();
                    if (StringUtils.isNotBlank(keyword)){
                        if (keyword.equals(str)) {
                            total++;
                        }
                    }
                }
                if (total == 0) {
                    continue;
                }
                System.out.println(str + ": " + total);
            }
            System.out.println("全部数据量：" + listAll.size());
            System.out.println("去重之后的数据量：" + list.size());

            if (type.intValue() == 1) {
                if (list != null && list.size() > 0) {
                    ExecutorService executorService = Executors.newFixedThreadPool(60);
                    List<Future> futureList = new ArrayList<>();
                    for (NoticeMQ content : list) {
                        futureList.add(executorService.submit(() -> {
                            try {
                                getZhongTaiDatasAndSaveTy(content);
                            } catch (Exception e) {
                                log.info("调用中台接口异常:{}",e);
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
            }
            System.out.println("==========================================此程序运行结束========================================");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getKaisixuandaCs(String date, Integer type) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();
        //关键词a
        String[] a ={"CT机","CT定位穿刺","X射线头部CT机","全身CT机","螺旋CT机","螺旋扇扫CT机","数字减影","血管造影","CT影像处理","血管造影导管","血管造影X射线机","血管造影X射线设备","血管造影影像处理","正电子发射系统","磁共振成像设备","永磁型磁共振成像系统","常导型磁共振成像系统","超导型磁共振成像系统","医用磁共振成像系统","磁共振辅助设备","磁共振定位装置","磁共振辅助刺激系统","磁共振造影注射装置","磁共振高压注射器","磁共振造影剂","磁共振用高压注射连接管","磁共振高压造影注射系统","磁共振高压造影注射器","磁共振成像辅助刺激系统","磁共振乳腺线圈穿刺固定架","正电子发射断层扫描","正电子发射磁共振成像系统","磁共振影像处理","CT造影","X射线计算机体层摄影设备","胃肠道造影显像剂","X射线计算机断层成像系统","CT影像处理软件","X射线血管造影影像处理软件","数字化X射线影像处理软件","超声影像引导系统","磁共振成像系统"};

        try {
            for (String str : a) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 3] AND allcontent:\"" + str + "\"", "", 1);
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

            ArrayList<String> arrayList = new ArrayList<>();
            //关键词a
            for (String key : a) {
                arrayList.add(key);
            }

            for (String str : arrayList) {
                int total = 0;
                for (NoticeMQ noticeMQ : listAll) {
                    String keyword = noticeMQ.getKeyword();
                    if (StringUtils.isNotBlank(keyword)){
                        if (keyword.equals(str)) {
                            total++;
                        }
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

        //如果参数为1,则进行存表
        if (type.intValue() ==1) {
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(60);
                List<Future> futureList = new ArrayList<>();
                List<Map<String,Object>> maps = new ArrayList<>();

                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> maps.add(getZhongTaiDatasAndSaveCs(content))));
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

                if (maps !=null && maps.size() >0){
                    testUserMapper.saveList(maps);
                }
            }
        }
        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    @Override
    public void getDajinE() {
        ExecutorService executorService1 = Executors.newFixedThreadPool(10);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        String cursorMark ="";
        //while (true){
            List<Map<String, Object>> maps = getList2(cursorMark);
            if (maps !=null && maps.size() >0){
                for (Map<String, Object> map : maps) {
                    String infoId = map.get("info_id").toString();
                    cursorMark = infoId;
                    futureList1.add(executorService1.submit(() -> {
                        //new_winner_amount,old_winner_amount, new_budget,old_budget
                        //由于大金额处理的特殊性，只能用null进行判断
                        String winnerAmount = map.get("new_winner_amount") != null ? map.get("new_winner_amount").toString() : null;
                        String budget = map.get("new_budget") != null ? map.get("new_budget").toString() : null;

                        boolean b = cusDataFieldService.checkStatus(infoId);//范围 例如:全国
                        if (!b) {
                            log.info("contentid:{} 对应的数据状态不是99, 丢弃", infoId);
                            return;
                        }
                        NoticeMQ noticeMQ = new NoticeMQ();
                        noticeMQ.setContentid(Long.valueOf(infoId));
                        //全部自提，不需要正文
                        try {
                            Map<String, Object> resultMap = cusDataFieldService.getAllFieldsWithZiTi(noticeMQ, false);
                            if (resultMap != null) {
                                //进行大金额替换操作
                                if (winnerAmount != null){
                                    resultMap.put("baiLian_amount_unit", winnerAmount);
                                }
                                if (budget != null){
                                    resultMap.put("baiLian_budget", budget);
                                }
                                resultMap.put("code2", QianlimaZTUtil.getFromUrl("http://datafetcher.intra.qianlima.com/dc/bidding/fromurl",String.valueOf(infoId)));
                                resultMap.put("old_winner_amount",map.get("old_winner_amount"));
                                resultMap.put("old_budget",map.get("old_budget"));
                                resultMap.put("new_winner_amount",map.get("new_winner_amount"));
                                resultMap.put("new_budget",map.get("new_budget"));
                                saveIntoMysqlDaJine(resultMap);
                            }
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }));
                }
          /*  }else {
                break;
            }*/
        }
        log.info("--------该接口运行结束--------");
    }
    public List<Map<String,Object>> getList(String cursorMark) {
        if (StringUtils.isNotBlank(cursorMark)) {
            List<Map<String, Object>> maps = djeJdbcTemplate.queryForList("select info_id, new_winner_amount,old_winner_amount, new_budget,old_budget from amount_for_handle where info_id > ? ORDER BY info_id limit 5000",cursorMark);
            return maps;
        }else {
            return djeJdbcTemplate.queryForList("select info_id, new_winner_amount,old_winner_amount, new_budget,old_budget from amount_for_handle ORDER BY info_id limit 5000");
        }
    }

    public List<Map<String,Object>> getList2(String cursorMark) {
            //List<Map<String, Object>> maps = djeJdbcTemplate.queryForList("SELECT * FROM amount_for_handle where states =1 AND DATE_FORMAT(FROM_UNIXTIME(update_time /1000),'%Y-%m-%d') BETWEEN '2021-04-12' AND '2021-04-20'");
              List<Map<String, Object>> maps = djeJdbcTemplate.queryForList("select info_id, old_winner_amount, old_budget from amount_for_handle where states = 0 group by info_id order by info_id asc");
            return maps;

    }




    private void getZhongTaiDatasAndSave(NoticeMQ noticeMQ) throws Exception{

        List<String> aa = LogUtils.readRule("keyWordsA");
        //关键词b
        List<String> bb = LogUtils.readRule("keyWordsB");

        boolean b = cusDataNewService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        //全部自提，不需要正文
        Map<String, Object> resultMap = cusDataNewService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            String title = resultMap.get("title").toString();
            String content = cusDataNewService.getContent(noticeMQ);//获取正文字段
            if (StringUtils.isNotBlank(content)){
                content = MathUtil.delHTMLAndBlock(content);
            }
            title = title.toUpperCase();
            content = content.toUpperCase();

            boolean flag = false;

            for (String s : aa) {
                if (title.contains(s.toUpperCase())){
                    flag = true;
                    break;
                }
            }


            if (! flag){
                for (String s : bb) {
                    if (content.contains(s.toUpperCase())){
                        flag = true;
                        break;
                    }
                }
            }

            if (flag){
                cusDataNewService.saveIntoMysql(resultMap);//插入数据库操作
            }
            //先去链接
            //content =processAboutContent(content);
            //再判断是否是字母
            //content = checkString(content);



        }
    }

    public String INSERT_AREA = "INSERT INTO han_area (contentid,areaProvince,areaCity,areaCountry)  VALUES (?,?,?,?)";
    /**
     *  获取地区
     */
    private final static List<String> kaAreaList = new ArrayList<>();

    public static synchronized Map<String, String> getAreaMap(String areaId) {
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
    /**
     * 调用中台接口, 获取结果类型细分
     * 06-答疑公告， 07-废标公告， 08-流标公告， 09-开标公示， 10-候选人公示， 11-中标通知， 12-合同公告， 13-验收合同， 14-违规公告， 15-其他公告
     * @param title 标题
     * @param content 正文
     * @param contentid
     * @param infoTypeUrl 获取结果类型细分接口的URI
     * @return
     */
    public static String getDataType2(String title, String content, Long contentid, String infoTypeUrl) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(60000)
                    .setSocketTimeout(60000).setConnectTimeout(60000).build();
            HttpPost post = new HttpPost(infoTypeUrl);
            //创建参数列表
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            list.add(new BasicNameValuePair("title", title));
            list.add(new BasicNameValuePair("content", content));
            post.setEntity(new UrlEncodedFormEntity(list, "UTF-8"));
            //url格式编码
            post.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            //设置超时时间为60秒
            post.setConfig(requestConfig);
            //执行请求
            CloseableHttpResponse httpResponse = httpClient.execute(post);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String entity = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
                JSONObject jsonObject = JSON.parseObject(entity);
                //成功
                if (jsonObject.getInteger("code") == 0) {
                    String data = jsonObject.getString("data");
                    if (StringUtils.isNotEmpty(data)) {
                        return data;
                    }
                } else {
                    log.error("调结果细分服务过程报错: {}, contentid: {}", jsonObject.get("msg"), contentid);
                    throw new RuntimeException("调用结果细分服务报错");
                }
            }
        } catch (Exception e) {
            log.error("结果细分判断出错:{}", e);
            throw new RuntimeException("结果细分判断出错");
        }
        return null;
    }

    //存储数据库
    public void saveIntoArea(Map<String, String> map ,String table){
        bdJdbcTemplate.update(table, map.get("contentid"),map.get("areaProvince"), map.get("areaCity"), map.get("areaCountry"));
        log.info("存mysql数据库进度--->{}",map.get("contentid"));
    }
    public void saveIntoMysql(Map<String, Object> map){
            bdJdbcTemplate.update(INSERT_ZT_DJE,map.get("task_id"), map.get("keyword"), map.get("content_id"), map.get("title"),
                    map.get("content"), map.get("province"), map.get("city"), map.get("country"), map.get("url"), map.get("baiLian_budget"),
                    map.get("baiLian_amount_unit"), map.get("xmNumber"), map.get("bidding_type"), map.get("progid"), map.get("zhao_biao_unit"),
                    map.get("relation_name"), map.get("relation_way"), map.get("agent_unit"), map.get("agent_relation_ame"),
                    map.get("agent_relation_way"), map.get("zhong_biao_unit"), map.get("link_man"), map.get("link_phone"),
                    map.get("registration_begin_time"), map.get("registration_end_time"), map.get("biding_acquire_time"),
                    map.get("biding_end_time"), map.get("tender_begin_time"), map.get("tender_end_time"), map.get("update_time"),
                    map.get("type"), map.get("bidder"), map.get("notice_types"), map.get("open_biding_time"), map.get("is_electronic"),
                    map.get("code"), map.get("isfile"), map.get("keyword_term"),map.get("keywords"),map.get("infoTypeSegment"),map.get("monitorUrl"), map.get("pocDetailUrl"));
        log.info("存mysql数据库进度--->{}",map.get("content_id"));
    }

    public void saveIntoMysqlDje(Map<String, Object> map){
        bdJdbcTemplate.update(INSERT_ZT_DJE,map.get("task_id"), map.get("keyword"), map.get("content_id"), map.get("title"),
                map.get("content"), map.get("province"), map.get("city"), map.get("country"), map.get("url"), map.get("baiLian_budget"),
                map.get("baiLian_amount_unit"), map.get("xmNumber"), map.get("bidding_type"), map.get("progid"), map.get("zhao_biao_unit"),
                map.get("relation_name"), map.get("relation_way"), map.get("agent_unit"), map.get("agent_relation_ame"),
                map.get("agent_relation_way"), map.get("zhong_biao_unit"), map.get("link_man"), map.get("link_phone"),
                map.get("registration_begin_time"), map.get("registration_end_time"), map.get("biding_acquire_time"),
                map.get("biding_end_time"), map.get("tender_begin_time"), map.get("tender_end_time"), map.get("update_time"),
                map.get("type"), map.get("bidder"), map.get("notice_types"), map.get("open_biding_time"), map.get("is_electronic"),
                map.get("code2"), map.get("isfile"), map.get("keyword_term"),map.get("keywords"),map.get("infoTypeSegment"), map.get("monitorUrl"), map.get("pocDetailUrl"));
    }

    /**
     * 大金额专用
     * @param map
     */
    public void saveIntoMysqlDaJine(Map<String, Object> map){
        bdJdbcTemplate.update(INSERT_ZT_DJE_ZHUANYONG,map.get("task_id"), map.get("keyword"), map.get("content_id"), map.get("title"),
                map.get("content"), map.get("province"), map.get("city"), map.get("country"), map.get("url"), map.get("baiLian_budget"),
                map.get("baiLian_amount_unit"), map.get("xmNumber"), map.get("bidding_type"), map.get("progid"), map.get("zhao_biao_unit"),
                map.get("relation_name"), map.get("relation_way"), map.get("agent_unit"), map.get("agent_relation_ame"),
                map.get("agent_relation_way"), map.get("zhong_biao_unit"), map.get("link_man"), map.get("link_phone"),
                map.get("registration_begin_time"), map.get("registration_end_time"), map.get("biding_acquire_time"),
                map.get("biding_end_time"), map.get("tender_begin_time"), map.get("tender_end_time"), map.get("update_time"),
                map.get("type"), map.get("bidder"), map.get("notice_types"), map.get("open_biding_time"), map.get("is_electronic"),
                map.get("code2"), map.get("isfile"), map.get("keyword_term"),map.get("keywords"),map.get("infoTypeSegment"), map.get("monitorUrl"), map.get("pocDetailUrl"),
                map.get("old_winner_amount"),map.get("new_winner_amount"),map.get("old_budget"),map.get("new_budget"));
    }

    public void saveIntoMysqlTenxun(Map<String, Object> map ,String table){
        bdJdbcTemplate.update(table,map.get("code2"), map.get("content_id"));
        log.info("存mysql数据库进度--->{}",map.get("content_id"));
    }


    private void getZhongTaiDatasAndSaveTy(NoticeMQ noticeMQ) {

        boolean b = cusDataNewService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        //全部自提，不需要正文
        Map<String, Object> resultMap = cusDataNewService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            String contentId = resultMap.get("content_id").toString();
            //进行大金额替换操作
                List<Map<String, Object>> maps = djeJdbcTemplate.queryForList("select info_id, winner_amount, budget from amount_code where info_id = ?", contentId);
            if (maps != null && maps.size() > 0){
                // 由于大金额处理的特殊性，只能用null进行判断
                String winnerAmount = maps.get(0).get("winner_amount") != null ? maps.get(0).get("winner_amount").toString() : null;
                if (winnerAmount != null){
                    resultMap.put("baiLian_amount_unit", winnerAmount);
                }
                String budget = maps.get(0).get("budget") != null ? maps.get(0).get("budget").toString() : null;
                if (budget != null){
                    resultMap.put("baiLian_budget", budget);
                }
            }


            //String content = cusDataNewService.getContent(noticeMQ);//获取正文字段
            //先去链接
            //content =processAboutContent(content);
            //再判断是否是字母
            //content = checkString(content);

            cusDataNewService.saveIntoMysql(resultMap);//插入数据库操作

        }
    }
    //只是测试
    private Map<String, Object> getZhongTaiDatasAndSaveCs(NoticeMQ noticeMQ) {

        boolean b = cusDataNewService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return null;
        }
        //全部自提，不需要正文
        Map<String, Object> resultMap = cusDataNewService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            return resultMap;
        }
        return null;
    }










    public String getString(String str) {
        String resultStr ="";
        String[] split = str.split("/");
        for (int i=split.length-2;i>-1;i--) {
            resultStr+=split[i]+"/";
        }
        return resultStr.substring(0,resultStr.length()-1);
    }
}