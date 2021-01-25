package com.qianlima.offline.service.han.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.offline.bean.ConstantBean;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.service.CusDataFieldService;
import com.qianlima.offline.service.han.TestTencentService;
import com.qianlima.offline.util.ContentSolr;
import com.qianlima.offline.util.KeyUtils;
import com.qianlima.offline.util.LogUtils;
import com.qianlima.offline.util.StrUtil;
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
    @Override
    public void saveTencent() {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<Future> futureList1 = new ArrayList<>();


        /*HashMap<String, String> simpleAreaMap = KeyUtils.getSimpleMap();
        Set<Map.Entry<String, String>> entries = simpleAreaMap.entrySet();//将map的key和value 进行映射成 集合*/
        List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT * FROM han_tencent_copy2");

        String url ="http://cusdata.qianlima.com/api/infoType";
        if (mapList !=null && mapList.size() >0){
            for (Map<String, Object> map : mapList) {
                String contentid = map.get("contentid").toString();
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
                    saveIntoMysql(m,INSERT_ZT_RESULT_TYPE);

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
        bdJdbcTemplate.update(table, map.get("type"),map.get("contentid"),map.get("title"));
        log.info("存mysql数据库进度--->{}",map.get("contentid"));
    }
}