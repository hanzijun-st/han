package com.qianlima.offline.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import static com.qianlima.offline.util.HttpClientUtil.getHttpClient;

public class GetUtil {

    public static JSONObject getHttp(String url,String paranName,String params) throws Exception{
        //创建默认的httpClient实例
        CloseableHttpClient httpClient = getHttpClient();
        //用get方法发送http请求
        HttpGet get = new HttpGet(url);

        CloseableHttpResponse httpResponse = null;
        //发送get请求
        httpResponse = httpClient.execute(get);
        //response实体
        HttpEntity entity = httpResponse.getEntity();
        //http://monitor.ka.qianlima.com/crm/info/page?userId=38&cursorMark=*&pageSize=200

        JSONObject jsonObject = JSON.parseObject(EntityUtils.toString(entity));
        return jsonObject;
    }
}