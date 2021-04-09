package com.qianlima.offline.service.han.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.offline.entity.HanJsonBean;
import com.qianlima.offline.entity.HanNewData;
import com.qianlima.offline.service.han.CurrencyService;
import com.qianlima.offline.service.han.TestApiService;
import com.qianlima.offline.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.solr.common.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.qianlima.offline.util.HttpClientUtil.getHttpClient;

/**
 * Created by Administrator on 2021/1/12.
 */

@Service
@Slf4j
public class TestApiServiceImpl implements TestApiService {


    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    private CurrencyService currencyService;

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

    private static final String INSERT_TEST_API2 = "INSERT INTO han_api (qy_id,qy_name)" +
            "VALUES (?,?)";

    @Override
    public void testDaHua() {
        try {
            List<String> ids = LogUtils.readRule("idsFile");
            for (String id : ids) {
                String jsonBean = currencyService.getHttpGet(id);
                JSONObject jsonObject = JSON.parseObject(jsonBean);
                String data = jsonObject.getString("data");
                if (StrUtil.isNotEmpty(data)){
                    JSONObject dataJson = JSON.parseObject(data);
                    if (StrUtil.isNotEmpty(dataJson.getString("extractDateDetail"))){
                        String extractDateDetail = dataJson.getString("extractDateDetail");
                        JSONObject extractJson = JSON.parseObject(extractDateDetail);
                        String registration_end_time = extractJson.getString("registration_end_time");
                        if (StrUtil.isNotEmpty(registration_end_time)){
                            String formatDateStr = DateUtils.getFormatDateStr(Long.valueOf(registration_end_time)* 1000L);
                            bdJdbcTemplate.update(INSERT_TEST_API2,id,formatDateStr);
                            log.info("入库成功---:{}",id);
                        }
                    }
                }
            }
        } catch (IOException e) {
           e.getMessage();
        }
    }

    /*@Override
    public void jsonToData() throws Exception{
        try {
           // List<String> list = LogUtils.readRule("json");
            //创建默认的httpClient实例
            CloseableHttpClient httpClient = getHttpClient();
            //用get方法发送http请求
            HttpGet get = new HttpGet("http://cusdata.qianlima.com/crm/info/page?userId=38&cursorMark=*&pageSize=200");

            CloseableHttpResponse httpResponse = null;
            //发送get请求
            httpResponse = httpClient.execute(get);
            //response实体
            HttpEntity entity = httpResponse.getEntity();
            //http://cusdata.qianlima.com/crm/info/page?userId=38&cursorMark=*&pageSize=200

            JSONObject jsonObject = JSON.parseObject(EntityUtils.toString(entity));

            String data = jsonObject.getString("data");
            if (StrUtil.isNotEmpty(data)){
                JSONObject dataJson = JSON.parseObject(data);
                JSONArray list2 = dataJson.getJSONArray("list");
                List<Map> resultMaps = new ArrayList<>();
                if (list2 !=null) {
                    for (Object obj : list2) {
                        //JSONObject jsonObj = JSON.parseObject(obj.toString());
                        HanNewData hanNewData = JsonUtil.jsonToBean(obj.toString(), HanNewData.class);
                        Map<String, Object> map = MapUtil.beanToMap(hanNewData);
                        try {
                            if (map.get("zhaoBiaoUnit") !=null){
                                JSONArray zhaoBiaoUnit = JSONObject.parseArray(map.get("zhaoBiaoUnit").toString());
                                if (zhaoBiaoUnit !=null){
                                    map.put("zhaoBiaoUnit",zhaoBiaoUnit.get(0));
                                }
                            }

                            if (map.get("zhaoRelationWay") !=null){
                                List<String> zhaoRelationWayList = StrUtil.objToList(map.get("zhaoRelationWay"));
                                if (zhaoRelationWayList !=null && zhaoRelationWayList.size() >0){
                                    String str ="";
                                    for (String s : zhaoRelationWayList) {
                                        str +=s+",";
                                    }
                                    if (StrUtil.isNotEmpty(str)){

                                        map.put("zhaoRelationWay", str.substring(0,str.length()-1));
                                    }
                                }
                            }
                            if (map.get("zhongBiaoUnit") !=null){
                                List<String> list1 = StrUtil.objToList(map.get("zhongBiaoUnit"));
                                if (list1 !=null && list1.size() >0){
                                    map.put("zhongBiaoUnit",list1.get(0));

                                }
                            }
                            if (map.get("agentRelationWay") !=null){
                                List<String> agentRelationWayList = StrUtil.objToList(map.get("agentRelationWay"));
                                if (agentRelationWayList !=null && agentRelationWayList.size() >0){
                                    String str ="";
                                    for (String s : agentRelationWayList) {
                                        str +=s+",";
                                    }
                                    if (StrUtil.isNotEmpty(str)){

                                        map.put("agentRelationWay", str.substring(0,str.length()-1));
                                    }
                                }
                            }
                            if (map.get("infoFile") !=null){
                                List<String> infoFileList = StrUtil.objToList(map.get("infoFile"));
                                if (infoFileList !=null && infoFileList.size() >0){
                                    String str ="";
                                    for (String s : infoFileList) {
                                        str +=s+",";
                                    }
                                    if (StrUtil.isNotEmpty(str)){
                                        map.put("infoFile", str.substring(0,str.length()-1));
                                    }
                                }
                            }
                            if (map.get("budget") !=null){
                                List<String> budgetLList = StrUtil.objToList(map.get("budget"));
                                if (budgetLList !=null && budgetLList.size() >0){
                                    String str ="";
                                    for (String s : budgetLList) {
                                        str +=s+",";
                                    }
                                    if (StrUtil.isNotEmpty(str)){

                                        map.put("budget", str.substring(0,str.length()-1));
                                    }
                                }
                            }
                            if (map.get("agentRelationName") !=null){
                                List<String> agentRelationNameList = StrUtil.objToList(map.get("agentRelationName"));
                                if (agentRelationNameList !=null && agentRelationNameList.size() >0){
                                    String str ="";
                                    for (String s : agentRelationNameList) {
                                        str +=s+",";
                                    }
                                    if (StrUtil.isNotEmpty(str)){

                                        map.put("agentRelationName", str.substring(0,str.length()-1));
                                    }
                                }
                            }
                            if (map.get("winnerAmount") !=null){
                                List<String> winnerAmountList = StrUtil.objToList(map.get("winnerAmount"));
                                if (winnerAmountList !=null && winnerAmountList.size() >0){
                                    String s = winnerAmountList.get(0);

                                    JSONObject sJson = JSON.parseObject(s);
                                    String amount = sJson.getString("amount");
                                    map.put("winnerAmount", amount);
                                }
                            }
                            if (map.get("zhongRelationWay") !=null){
                                List<String> zhongRelationWayList = StrUtil.objToList(map.get("zhongRelationWay"));
                                if (zhongRelationWayList !=null && zhongRelationWayList.size() >0){
                                    String str ="";
                                    for (String s : zhongRelationWayList) {
                                        str +=s+",";
                                    }
                                    if (StrUtil.isNotEmpty(str)){

                                        map.put("zhongRelationWay", str.substring(0,str.length()-1));
                                    }
                                }
                            }
                            if (map.get("agentUnit") !=null){
                                List<String> agentUnitList = StrUtil.objToList(map.get("agentUnit"));
                                if (agentUnitList !=null && agentUnitList.size() >0){
                                    String str ="";
                                    for (String s : agentUnitList) {
                                        str +=s+",";
                                    }
                                    if (StrUtil.isNotEmpty(str)){

                                        map.put("agentUnit", str.substring(0,str.length()-1));
                                    }
                                }
                            }
                            if (map.get("zhaoRelationName") !=null){
                                List<String> zhaoRelationNameList = StrUtil.objToList(map.get("zhaoRelationName"));
                                if (zhaoRelationNameList !=null && zhaoRelationNameList.size() >0){
                                    String str ="";
                                    for (String s : zhaoRelationNameList) {
                                        str +=s+",";
                                    }
                                    if (StrUtil.isNotEmpty(str)){

                                        map.put("zhaoRelationName", str.substring(0,str.length()-1));
                                    }
                                }
                            }
                            if (map.get("zhongRelationName") !=null){
                                List<String> zhongRelationNameList = StrUtil.objToList(map.get("zhongRelationName"));
                                if (zhongRelationNameList !=null && zhongRelationNameList.size() >0){
                                    String str ="";
                                    for (String s : zhongRelationNameList) {
                                        str +=s+",";
                                    }
                                    if (StrUtil.isNotEmpty(str)){
                                        map.put("zhongRelationName", str.substring(0,str.length()-1));
                                    }
                                }
                            }
                            if (map.get("target") !=null){
                                String target = map.get("target").toString();
                                JSONObject targetJson = JSON.parseObject(target);
                                String sumUnit = targetJson.getString("sumUnit");
                                map.put("b_sumUnit",sumUnit);
                                String sum = targetJson.getString("sum");
                                map.put("b_sum",sum);
                                String targetDetails = targetJson.getString("targetDetails");
                                JSONArray jsonArray = JSONObject.parseArray(targetDetails);
                                if (jsonArray !=null){
                                    Object o = jsonArray.get(0);
                                    JSONObject detailJson = JSON.parseObject(o.toString());
                                    String number = detailJson.getString("number");
                                    String priceUnit = detailJson.getString("priceUnit");
                                    String keywords = detailJson.getString("keywords");
                                    String totalPrice = detailJson.getString("totalPrice");
                                    String price = detailJson.getString("price");
                                    String totalPriceUnit = detailJson.getString("totalPriceUnit");
                                    String name = detailJson.getString("name");
                                    String model = detailJson.getString("model");
                                    String brand = detailJson.getString("brand");
                                    String numberUnit = detailJson.getString("numberUnit");

                                    map.put("b_name",name);
                                    map.put("b_keywords",keywords);
                                    map.put("b_brand",brand);
                                    map.put("b_model",model);
                                    map.put("b_number",number);
                                    map.put("b_numberUnit",numberUnit);
                                    map.put("b_price",price);
                                    map.put("b_priceUnit",priceUnit);
                                    map.put("b_totalPrice",totalPrice);
                                    map.put("b_totalPriceUnit",totalPriceUnit);
                                }

                            }
                            //saveIntoMysql(map);
                        } catch (Exception e) {
                            e.getMessage();
                        }
                        resultMaps.add(map);
                    }

                    log.info("返回的结果：{}",resultMaps);
                }
            }
        } catch (IOException e) {

        }
    }*/

    @Override
    public void jsonToData2() throws Exception{

        ClassPathResource resource = new ClassPathResource("/source/fileJson.json");
        File filePath = resource.getFile();
        JSONArray btnArray = null;

        //读取文件
        String input = FileUtils.readFileToString(filePath, "UTF-8");
        //将读取的数据转换为JSONObject
        JSONObject jsonObject = JSONObject.parseObject(input);
        if (jsonObject != null) {
            //取出data
            String data = jsonObject.getString("data");
            JSONObject dataObj = JSONObject.parseObject(data);
            btnArray = dataObj.getJSONArray("list");
        }
        Map<String, List<Map>> btnMap = new HashMap<>();
        Iterator<Object> num = btnArray.iterator();
        //遍历JSONArray，转换格式。按按钮集合按模块（name）放入map中
        while (num.hasNext()) {
            HanJsonBean hanJsonBean = new HanJsonBean();

            JSONObject btn = (JSONObject) num.next();
            JSONArray zhaoBiaoUnit = btn.getJSONArray("zhaoBiaoUnit");
            if (zhaoBiaoUnit !=null && zhaoBiaoUnit.size() >0){
                String str ="";
                for (Object o : zhaoBiaoUnit) {
                    str += o+",";
                }
                if (StrUtil.isNotEmpty(str)){
                    hanJsonBean.setZhaoBiaoUnit(str.substring(0,str.length()-1));
                }
            }
            JSONArray zhaoRelationWay = btn.getJSONArray("zhaoRelationWay");
            if (zhaoRelationWay !=null && zhaoRelationWay.size() >0){
                String str ="";
                for (Object o : zhaoRelationWay) {
                    str += o+",";
                }
                if (StrUtil.isNotEmpty(str)){
                    hanJsonBean.setZhaoRelationWay(str.substring(0,str.length()-1));
                }
            }
            JSONArray zhongBiaoUnit = btn.getJSONArray("zhongBiaoUnit");
            if (zhongBiaoUnit !=null && zhongBiaoUnit.size() >0){
                String str ="";
                for (Object o : zhongBiaoUnit) {
                    str += o+",";
                }
                if (StrUtil.isNotEmpty(str)){
                    hanJsonBean.setZhongBiaoUnit(str.substring(0,str.length()-1));
                }
            }
            JSONArray agentRelationWay = btn.getJSONArray("agentRelationWay");
            if (agentRelationWay !=null && agentRelationWay.size() >0){
                String str ="";
                for (Object o : agentRelationWay) {
                    str += o+",";
                }
                if (StrUtil.isNotEmpty(str)){
                    hanJsonBean.setAgentRelationWay(str.substring(0,str.length()-1));
                }
            }
            JSONArray infoFile = btn.getJSONArray("infoFile");
            if (infoFile !=null && infoFile.size() >0){
                String str ="";
                for (Object o : infoFile) {
                    str += o+",";
                }
                if (StrUtil.isNotEmpty(str)){
                    hanJsonBean.setInfoFile(str.substring(0,str.length()-1));
                }
            }

            JSONArray budget = btn.getJSONArray("budget");
            if (budget !=null && budget.size() >0){
                for (Object o : budget) {
                    Map<String, Object> map = MapUtil.beanToMapNew(o);
                    String amount = map.get("amount").toString();
                    String unit = map.get("unit").toString();
                    hanJsonBean.setBudgetAmount(amount);
                    hanJsonBean.setBudgetUnit(unit);
                }
            }
            JSONArray agentRelationName = btn.getJSONArray("agentRelationName");
            if (agentRelationName !=null && agentRelationName.size() >0){
                String str ="";
                for (Object o : agentRelationName) {
                    str += o+",";
                }
                if (StrUtil.isNotEmpty(str)){
                    hanJsonBean.setAgentRelationName(str.substring(0,str.length()-1));
                }
            }
            JSONArray winnerAmount = btn.getJSONArray("winnerAmount");
            if (winnerAmount !=null && winnerAmount.size() >0){
                for (Object o : winnerAmount) {
                    Map<String, Object> map = MapUtil.beanToMapNew(o);
                    String amount = map.get("amount").toString();
                    String unit = map.get("unit").toString();
                    hanJsonBean.setWinnerAmount(amount);
                    hanJsonBean.setWinnerUnit(unit);
                }
            }

            JSONArray zhongRelationWay = btn.getJSONArray("zhongRelationWay");
            if (zhongRelationWay !=null && zhongRelationWay.size() >0){
                String str ="";
                for (Object o : zhongRelationWay) {
                    str += o+",";
                }
                if (StrUtil.isNotEmpty(str)){
                    hanJsonBean.setZhongRelationWay(str.substring(0,str.length()-1));
                }
            }
            JSONArray agentUnit = btn.getJSONArray("agentUnit");
            if (agentUnit !=null && agentUnit.size() >0){
                String str ="";
                for (Object o : agentUnit) {
                    str += o+",";
                }
                if (StrUtil.isNotEmpty(str)){
                    hanJsonBean.setAgentUnit(str.substring(0,str.length()-1));
                }
            }
            JSONArray zhaoRelationName = btn.getJSONArray("zhaoRelationName");
            if (zhaoRelationName !=null && zhaoRelationName.size() >0){
                String str ="";
                for (Object o : zhaoRelationName) {
                    str += o+",";
                }
                if (StrUtil.isNotEmpty(str)){
                    hanJsonBean.setZhaoRelationName(str.substring(0,str.length()-1));
                }
            }
            JSONArray zhongRelationName = btn.getJSONArray("zhongRelationName");
            if (zhongRelationName !=null && zhongRelationName.size() >0){
                String str ="";
                for (Object o : zhongRelationName) {
                    str += o+",";
                }
                if (StrUtil.isNotEmpty(str)){
                    hanJsonBean.setZhongRelationName(str.substring(0,str.length()-1));
                }
            }
            hanJsonBean.setInfoId(btn.getString("infoId"));
            hanJsonBean.setProjectId(btn.getString("projectId"));
            hanJsonBean.setKeywords(btn.getString("keywords"));
            hanJsonBean.setInfoTitle(btn.getString("infoTitle"));
            hanJsonBean.setInfoType(btn.getString("infoType"));
            hanJsonBean.setInfoHtml(btn.getString("infoHtml"));
            hanJsonBean.setInfoPublishTime(btn.getString("infoPublishTime"));
            hanJsonBean.setInfoQianlimaUrl(btn.getString("infoQianlimaUrl"));
            hanJsonBean.setAreaProvince(btn.getString("areaProvince"));
            hanJsonBean.setAreaCity(btn.getString("areaCity"));
            hanJsonBean.setAreaCountry(btn.getString("areaCountry"));
            hanJsonBean.setXmNumber(btn.getString("xmNumber"));
            hanJsonBean.setBidingAcquireTime(btn.getString("bidingAcquireTime"));
            hanJsonBean.setBidingEndTime(btn.getString("bidingEndTime"));
            hanJsonBean.setTenderBeginTime(btn.getString("tenderBeginTime"));
            hanJsonBean.setTenderEndTime(btn.getString("tenderEndTime"));
            hanJsonBean.setOpenBidingTime(btn.getString("openBidingTime"));
            hanJsonBean.setBiddingType(btn.getString("biddingType"));
            hanJsonBean.setIsElectronic(btn.getString("isElectronic"));
            String target1 = btn.getString("target");
            if (StrUtil.isNotEmpty(target1)) {
                    JSONObject targetJson = JSON.parseObject(target1);
                    String sumUnit = targetJson.getString("sumUnit");
                    hanJsonBean.setB_sumUnit(sumUnit);
                    String sum = targetJson.getString("sum");
                    hanJsonBean.setB_sum(sum);
                    String targetDetails = targetJson.getString("targetDetails");
                    JSONArray jsonArray = JSONObject.parseArray(targetDetails);
                    if (jsonArray != null) {
                        Object o = jsonArray.get(0);
                        JSONObject detailJson = JSON.parseObject(o.toString());
                        String number = detailJson.getString("number");
                        String priceUnit = detailJson.getString("priceUnit");
                        String keywords = detailJson.getString("keywords");
                        String totalPrice = detailJson.getString("totalPrice");
                        String price = detailJson.getString("price");
                        String totalPriceUnit = detailJson.getString("totalPriceUnit");
                        String name = detailJson.getString("name");
                        String model = detailJson.getString("model");
                        String brand = detailJson.getString("brand");
                        String numberUnit = detailJson.getString("numberUnit");

                        hanJsonBean.setB_name(name);
                        hanJsonBean.setB_keywords(keywords);
                        hanJsonBean.setB_brand(brand);
                        hanJsonBean.setB_model(model);
                        hanJsonBean.setB_number( number);
                        hanJsonBean.setB_numberUnit(numberUnit);
                        hanJsonBean.setB_price(price);
                        hanJsonBean.setB_priceUnit(priceUnit);
                        hanJsonBean.setB_totalPrice(totalPrice);
                        hanJsonBean.setB_totalPriceUnit(totalPriceUnit);
                    }

                }
            saveIntoMysql(MapUtil.beanToMap(hanJsonBean));
        }

    }
    private static final String HAN_JSON_JX = "INSERT INTO han_json_jx (infoId,projectId,keywords,infoTitle,infoHtml,infoFile,infoType,infoPublishTime," +
            "infoQianlimaUrl,areaProvince,areaCity,areaCountry,xmNumber,bidingAcquireTime,bidingEndTime,tenderBeginTime,tenderEndTime,openBidingTime ," +
            "biddingType,isElectronic,zhaoBiaoUnit,zhaoRelationName,zhaoRelationWay,zhongBiaoUnit,zhongRelationName,zhongRelationWay,agentUnit," +
            "agentRelationName,agentRelationWay,budgetAmount,budgetUnit,winnerAmount,winnerUnit,b_sum,b_sumUnit,b_name,b_keywords,b_brand,b_model,b_number,b_numberUnit,b_price,b_priceUnit,b_totalPrice,b_totalPriceUnit) " +
            " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    public void saveIntoMysql(Map<String, Object> map){
        bdJdbcTemplate.update(HAN_JSON_JX,map.get("infoId"),map.get("projectId"),map.get("keywords"),map.get("infoTitle"),map.get("infoHtml"),map.get("infoFile"),map.get("infoType"),
                map.get("infoPublishTime"), map.get("infoQianlimaUrl"),map.get("areaProvince"),map.get("areaCity"),map.get("areaCountry"),map.get("xmNumber"),
                map.get("bidingAcquireTime"),map.get("bidingEndTime"),map.get("tenderBeginTime"),map.get("tenderEndTime"),map.get("openBidingTime"),
                map.get("biddingType"),map.get("isElectronic"),map.get("zhaoBiaoUnit"),map.get("zhaoRelationName"),map.get("zhaoRelationWay"),map.get("zhongBiaoUnit"),
                map.get("zhongRelationName"),map.get("zhongRelationWay"),map.get("agentUnit"),map.get("agentRelationName"),map.get("agentRelationWay"),map.get("budgetAmount"),map.get("budgetUnit"),
                map.get("winnerAmount"),map.get("winnerUnit"),map.get("b_sum"),map.get("b_sumUnit"),map.get("b_name"),map.get("b_keywords"),map.get("b_brand"),map.get("b_model"),map.get("b_number"),
                map.get("b_numberUnit"),map.get("b_price"),map.get("b_priceUnit"),map.get("b_totalPrice"),map.get("b_totalPriceUnit"));
    }

}
