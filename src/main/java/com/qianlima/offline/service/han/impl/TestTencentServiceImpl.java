package com.qianlima.offline.service.han.impl;

import com.alibaba.druid.sql.visitor.functions.Char;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.offline.bean.ConstantBean;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.service.CusDataFieldService;
import com.qianlima.offline.service.han.TestTencentService;
import com.qianlima.offline.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
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
public class TestTencentServiceImpl implements TestTencentService {
    @Autowired
    private ContentSolr contentSolr;

    @Autowired
    private CusDataFieldService cusDataFieldService;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;

    //mysql数据库中插入数据
    public static final String INSERT_ZT_RESULT_TYPE = "INSERT INTO han_tencent (type,contentid,title) VALUES (?,?,?)";
    public static final String UPDATE_ZT_RESULT_TYPE = "UPDATE han_data SET code =? WHERE content_id =?";
    @Override
    public void saveTencent() {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<Future> futureList1 = new ArrayList<>();


        /*HashMap<String, String> simpleAreaMap = KeyUtils.getSimpleMap();
        Set<Map.Entry<String, String>> entries = simpleAreaMap.entrySet();//将map的key和value 进行映射成 集合*/
        List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT content_id,title FROM han_data");

        String url ="http://cusdata.qianlima.com/api/infoType";
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
                    m.put("type",dataType2);
                    m.put("contentid",contentid);
                    m.put("title",title);
                    //System.out.println(dataType2);
                    //Map<String, Object> allFieldsWithOther = cusDataFieldService.getDataType(title,content,url,noticeMQ, false);
                    //if (allFieldsWithOther != null && allFieldsWithOther.size() >0) {
                    //    saveIntoMysql(allFieldsWithOther,INSERT_ZT_RESULT_HAN);
                    //}
                    saveIntoMysql(m,UPDATE_ZT_RESULT_TYPE);

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

    public String INSERT_ZT_RESULT_HXR = "INSERT INTO han_data (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time,is_electronic,code,isfile,keyword_term) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    @Override
    public void toIds() throws Exception{
        List<String> idsFile = LogUtils.readRule("idsFile");
        for (String s : idsFile) {
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
                    saveIntoMysql(resultMap,INSERT_ZT_RESULT_HXR);
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
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
    public void saveIntoMysql(Map<String, Object> map ,String table){
        bdJdbcTemplate.update(table, map.get("type"),map.get("contentid"));
        log.info("存mysql数据库进度--->{}",map.get("contentid"));
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