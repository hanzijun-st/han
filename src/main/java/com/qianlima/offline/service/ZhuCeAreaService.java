package com.qianlima.offline.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.offline.bean.Enterprise;
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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Slf4j
//天眼查补充 注册地址及注册地址省市
public class ZhuCeAreaService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    //每次取数据的数量
    private Integer appendLimit = 1000000;

    private String code = "1";

    private static final String SELECT_SQL = "SELECT id, content_id as contentid, zhong_biao_unit as zhongbiaounit from jyf_lala_data";

    private static final String UPDATA_SQL = "update jyf_lala_data set code = ?,keyword_term = ?,task_id = ?,keyword = ?, title = ? where content_id = ?";


    public static Map getRegisterAddr(String unit) {

        try{
            CloseableHttpClient httpClient = HttpClients.createDefault();
            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(60000)
                    .setSocketTimeout(60000).setConnectTimeout(60000).build();
            HttpPost post = new HttpPost("http://cusdata.qianlima.com/api/area");
            //创建参数列表
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            list.add(new BasicNameValuePair("unit", unit));
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
                    Map map = (Map) JSONObject.parse(data);
                    return map;
                } else {
                    log.error("调用省市接口获取数据错误");
                    throw new RuntimeException("调用省市接口服务报错");
                }
                //Map maps = (Map)JSON.parse(entity);
            }
        }catch (Exception e) {
            log.error("调用省市接口判断出错:{}", e);
            throw new RuntimeException("调用省市接口判断出错");
        }
        return null;
    }


    public String getRegLocationHangYe() {
        try {
            boolean idEndFlag = false;
            Integer beginid = 0;
            while (true) {
                idEndFlag = false;
                ExecutorService executorService = Executors.newFixedThreadPool(32);
                List<Future> futureList = new ArrayList<>();
                List<Map<String, Object>> maps = bdJdbcTemplate.queryForList(SELECT_SQL);
                if (maps != null && maps.size() > 0) {
                    log.info("任务查出来了 total：{}", maps.size());
                    if (maps.size() < appendLimit) {
                        idEndFlag = true;
                    }
                    for (Map<String, Object> map : maps) {
                        futureList.add(executorService.submit(() -> insert(map)));
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
                    beginid = (Integer) maps.get(maps.size() - 1).get("id");
                    log.info("任务查出来了的下一页游标beginOid：{}", beginid);
                } else {
                    idEndFlag = true;
                }
                if (idEndFlag) {
                    log.info("任务完成做到最新，跳出");
                    break;
                }
            }
        } catch (Exception e) {
            log.error("任务异常 e:{}", e);
        }
        return "任务完成做到最新";
    }

    private void insert(Map<String, Object> map) {

        String contentid = map.get("contentid").toString();

        if (StringUtils.isBlank((String) map.get("zhongbiaounit"))) {
            return;
        }
        String zhongbiaounit = map.get("zhongbiaounit").toString();

        if (zhongbiaounit.contains("、")) {
            String[] split = zhongbiaounit.split("、");
            zhongbiaounit = split[0];
        } else if (zhongbiaounit.contains("，")) {
            String[] split = zhongbiaounit.split("，");
            zhongbiaounit = split[0];
        }

        //法人
        String legalPersonName = null;
        //联系方式
        String phoneNumber = null;

        Enterprise enterprise = queryForName(zhongbiaounit);
        if (enterprise != null) {
            //法人
            legalPersonName = enterprise.getLegalPersonName() != null ? enterprise.getLegalPersonName() : "";
            //联系方式
            phoneNumber = enterprise.getPhoneNumber() != null ? enterprise.getPhoneNumber() : "";
        }

        try{
            CloseableHttpClient httpClient = HttpClients.createDefault();
            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(60000)
                    .setSocketTimeout(60000).setConnectTimeout(60000).build();
            HttpPost post = new HttpPost("http://cusdata.qianlima.com/api/area");
            //创建参数列表
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            list.add(new BasicNameValuePair("unit", zhongbiaounit));
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
                    if (StringUtils.isNotBlank(data)){
                        JSONObject object = JSONObject.parseObject(data);
                        String province = object.get("province").toString() != null ? object.get("province").toString() : "";
                        String city = object.get("city").toString() != null ? object.get("city").toString() : "";
                        String regLocation = object.get("regLocation").toString() != null ? object.get("regLocation").toString() : "";
                        bdJdbcTemplate.update(UPDATA_SQL, regLocation,province,city,legalPersonName,phoneNumber,contentid);
                        log.info("contentid：{}经过天眼查匹配需求字段后, 重新入库", contentid);
                    }
                } else {
                    log.error("调用省市接口获取数据错误");
                    throw new RuntimeException("调用省市接口服务报错");
                }
                //Map maps = (Map)JSON.parse(entity);
            }
        }catch (Exception e) {
            log.error("调用省市接口判断出错:{}", e);
            throw new RuntimeException("调用省市接口判断出错");
        }
    }

    private Enterprise queryForName(String zhongbiaounit) {
        if (StringUtils.isBlank(zhongbiaounit)) {
            return null;
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("name").is(zhongbiaounit));
        return mongoTemplate.findOne(query, Enterprise.class);
    }
}
