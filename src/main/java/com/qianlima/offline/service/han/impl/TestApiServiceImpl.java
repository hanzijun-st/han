package com.qianlima.offline.service.han.impl;

import com.qianlima.offline.service.han.TestApiService;
import com.qianlima.offline.util.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2021/1/12.
 */

@Service
@Slf4j
public class TestApiServiceImpl implements TestApiService {


    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;



    //保存数据入库
    public void saveApiMysql(Map<String, Object> map){
        try{
            bdJdbcTemplate.update(INSERT_TEST_API,map.get("qy_name"),map.get("json_data"));
        }catch (Exception e){
            log.error("数据库存储异常，企业号为:{}", map.get("qy_name"));
        }
    }

    /**
     * 通过get请求，获取数据
     * @param name
     * @return
     */
    private String test1028(String name) {
        /*BasicHttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 1000);
        HttpConnectionParams.setSoTimeout(httpParams, 1000);
        HttpClient httpClient = new DefaultHttpClient(httpParams);*/

        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(60000)
                .setSocketTimeout(60000).setConnectTimeout(60000).build();
        String result = null;
        try {

            HttpGet get = new HttpGet("http://open.api.tianyancha.com/services/open/ic/actualControl/2.0?keyword="+name);
            // 设置header
            //get.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            //10.183.16.208    token:130aa541-6e6e-4b75-9a66-ec9d56e47bd3
             get.setHeader("Authorization", "130aa541-6e6e-4b75-9a66-ec9d56e47bd3");
            //设置超时时间为60秒
            get.setConfig(requestConfig);
            // 设置类型
            HttpResponse response = httpClient.execute(get);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "utf-8");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    private static final String INSERT_TEST_API = "INSERT INTO han_api (qy_name,json_data)" +
            "VALUES (?,?)";

    @Override
    public void testApi() throws Exception{
        List<String> nameList = new ArrayList<>();

        //查询已存在的企业名称
        List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT qy_name FROM han_api");
        for (Map<String, Object> map : mapList) {
            nameList.add(map.get("qy_name").toString());
        }
        //通过文件获取企业名称
        List<String> hBdw = LogUtils.readRule("qyNames");
        for (String name : hBdw) {
            if (nameList.contains(name)){
                continue;
            }
            String s = test1028(name);
            bdJdbcTemplate.update(INSERT_TEST_API,name,s);
        }
    }
}
