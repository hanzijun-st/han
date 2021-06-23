package com.qianlima.offline.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class CleanService {

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    public void getSJQX(String contentid) {

        String result = getQX(contentid);
        JSONObject data = JSON.parseObject(result);
        if (null == data) {
            log.error("异常++++++++++++++++++  getResult");
            return;
        }
        JSONObject info =  JSON.parseObject(data.get("data").toString());
        if (info == null) {
            log.error("异常————————————————————— getData 为空");
            System.out.println("2");
            return;
        }
        JSONObject object = info.getJSONObject("fields");
        if (object == null || object.size() == 0) {
            log.error("异常=============== getFields 为空");
            return;
        }
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
//        String infoUrl = map.get("f").toString();
//        String xlfKeywords = map.get("keyword").toString();
        String infoWebsite = object.getString("target");

        String extractProjName = object.getString("extractProjName");
        String infoTypeSegment = object.getString("infoTypeSegment");
        String areaAreaId = object.getString("areaAreaId");

//        bdJdbcTemplate.update("insert into t_beideng_one (" +
//                        "taskid,infoId,infoTitle,infoType,infoPublishTime,infoQianlimaUrl," +
//                        "areaProvince,areaCity,areaCountry,xmNumber," +
//                        "zhongBiaoUnit,zhongRelationName,zhongRelationWay," +
//                        "openBidingTime,bidingAcquireTime,bidingEndTime,tenderBeginTime,tenderEndTime,isElectronic," +
//                        "biddingType,zhaoBiaoUnit,zhaoRelationName,zhaoRelationWay," +
//                        "agentUnit,agentRelationName," +
//                        "agentRelationWay,budget,budgetUnit,winnerAmount,winnerAmountUnit,infoFile,keywords, infoUrl, infoWebsite" +
//                        ",xlfKeywords) " +
//                        " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
//                "25", infoId, infoTitle, infoType, infoPublishTime, infoQianlimaUrl,
//                areaProvince, areaCity, areaCountry, xmNumber,
//                zhongBiaoUnitStr, zhongRelationNameStr, zhongRelationWayStr,
//                openBidingTime, bidingAcquireTime, bidingEndTime, tenderBeginTime, tenderEndTime, isElectronic,
//                biddingType, zhaoBiaoUnitStr, zhaoRelationNameStr, zhaoRelationWayStr, agentUnitStr, agentRelationNameStr,
//                agentRelationWayStr, budgetStr, budgetUnit, winnerAmountStr, winnerAmountUnit,
//                infoFileStr, keywords, infoUrl, infoWebsite, xlfKeywords);

        log.info("------去重数据存库-infoId:{}",infoId);
        bdJdbcTemplate.update(ALI_OFFLINE_DATA, infoId, infoTitle, infoType, infoPublishTime, infoQianlimaUrl,
                areaProvince, areaCity, areaCountry, xmNumber, zhongBiaoUnitStr, zhongRelationNameStr, zhongRelationWayStr, openBidingTime,
                bidingAcquireTime, bidingEndTime, tenderBeginTime, tenderEndTime, isElectronic,
                biddingType, zhaoBiaoUnitStr, zhaoRelationNameStr, zhaoRelationWayStr, agentUnitStr, agentRelationNameStr, agentRelationWayStr, budgetStr, winnerAmountStr, infoTypeSegment, extractProjName, null, areaAreaId, null, null);

    }

    /**
     * 临时接口
     * @param contentid
     */
    public void getSJQX_ls(String contentid) {

        String result = getQX(contentid);
        JSONObject data = JSON.parseObject(result);
        if (null == data) {
            log.error("异常++++++++++++++++++  getResult");
            return;
        }
        JSONObject info =  JSON.parseObject(data.get("data").toString());
        if (info == null) {
            log.error("异常————————————————————— getData 为空");
            System.out.println("2");
            return;
        }
        JSONObject object = info.getJSONObject("fields");
        if (object == null || object.size() == 0) {
            log.error("异常=============== getFields 为空");
            return;
        }
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
//        String infoUrl = map.get("f").toString();
//        String xlfKeywords = map.get("keyword").toString();
        String infoWebsite = object.getString("target");

        String extractProjName = object.getString("extractProjName");
        String infoTypeSegment = object.getString("infoTypeSegment");
        String areaAreaId = object.getString("areaAreaId");

//        bdJdbcTemplate.update("insert into t_beideng_one (" +
//                        "taskid,infoId,infoTitle,infoType,infoPublishTime,infoQianlimaUrl," +
//                        "areaProvince,areaCity,areaCountry,xmNumber," +
//                        "zhongBiaoUnit,zhongRelationName,zhongRelationWay," +
//                        "openBidingTime,bidingAcquireTime,bidingEndTime,tenderBeginTime,tenderEndTime,isElectronic," +
//                        "biddingType,zhaoBiaoUnit,zhaoRelationName,zhaoRelationWay," +
//                        "agentUnit,agentRelationName," +
//                        "agentRelationWay,budget,budgetUnit,winnerAmount,winnerAmountUnit,infoFile,keywords, infoUrl, infoWebsite" +
//                        ",xlfKeywords) " +
//                        " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
//                "25", infoId, infoTitle, infoType, infoPublishTime, infoQianlimaUrl,
//                areaProvince, areaCity, areaCountry, xmNumber,
//                zhongBiaoUnitStr, zhongRelationNameStr, zhongRelationWayStr,
//                openBidingTime, bidingAcquireTime, bidingEndTime, tenderBeginTime, tenderEndTime, isElectronic,
//                biddingType, zhaoBiaoUnitStr, zhaoRelationNameStr, zhaoRelationWayStr, agentUnitStr, agentRelationNameStr,
//                agentRelationWayStr, budgetStr, budgetUnit, winnerAmountStr, winnerAmountUnit,
//                infoFileStr, keywords, infoUrl, infoWebsite, xlfKeywords);

        log.info("------去重数据存库-infoId:{}",infoId);
        bdJdbcTemplate.update(ALI_OFFLINE_DATA_ls, infoId, infoTitle, infoType, infoPublishTime, infoQianlimaUrl,
                areaProvince, areaCity, areaCountry, xmNumber, zhongBiaoUnitStr, zhongRelationNameStr, zhongRelationWayStr, openBidingTime,
                bidingAcquireTime, bidingEndTime, tenderBeginTime, tenderEndTime, isElectronic,
                biddingType, zhaoBiaoUnitStr, zhaoRelationNameStr, zhaoRelationWayStr, agentUnitStr, agentRelationNameStr, agentRelationWayStr, budgetStr, winnerAmountStr, infoTypeSegment, extractProjName, null, areaAreaId, null, null);

    }

    public static final String ALI_OFFLINE_DATA = "insert into han_ali_offline_data (infoId,infoTitle,infoType,infoPublishTime," +
            "infoQianlimaUrl,areaProvince,areaCity,areaCountry,xmNumber,zhongBiaoUnit,zhongRelationName,zhongRelationWay," +
            "openBidingTime,bidingAcquireTime,bidingEndTime,tenderBeginTime,tenderEndTime,isElectronic,biddingType," +
            "zhaoBiaoUnit,zhaoRelationName,zhaoRelationWay,agentUnit,agentRelationName,agentRelationWay,budget," +
            "winnerAmount, infoTypeSegment,extractProjName,sourceUrl,areaAreaId, monitorUrl, pocDetailUrl) values (?,?,?," +
            "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    /**
     * 临时使用sql
     */
    public static final String ALI_OFFLINE_DATA_ls = "insert into han_ali_offline_data_ls (infoId,infoTitle,infoType,infoPublishTime," +
            "infoQianlimaUrl,areaProvince,areaCity,areaCountry,xmNumber,zhongBiaoUnit,zhongRelationName,zhongRelationWay," +
            "openBidingTime,bidingAcquireTime,bidingEndTime,tenderBeginTime,tenderEndTime,isElectronic,biddingType," +
            "zhaoBiaoUnit,zhaoRelationName,zhaoRelationWay,agentUnit,agentRelationName,agentRelationWay,budget," +
            "winnerAmount, infoTypeSegment,extractProjName,sourceUrl,areaAreaId, monitorUrl, pocDetailUrl) values (?,?,?," +
            "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    //TODO 限号清洗接口
    public String getQX(String contentId) {
        String result = null;
        //创建默认的httpClient实例
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            //用get方法发送http请求
            HttpGet get = new HttpGet("http://monitor.ka.qianlima.com/zt/api/clean/field?contentid=" + contentId);
            CloseableHttpResponse httpResponse = httpClient.execute(get);
            if (httpResponse.getStatusLine().getStatusCode() == org.apache.http.HttpStatus.SC_OK) {
                //response实体
                HttpEntity entity = httpResponse.getEntity();
                if (null != entity) {
                    result = EntityUtils.toString(entity);

                }
            } else {
                log.warn("调用字段库获取正文, http响应失败 contentid：{} httpcode:{}", contentId, httpResponse.getStatusLine().getStatusCode());
            }
            if (httpResponse != null) {
                httpResponse.close();
            }
        } catch (Exception e) {
            log.error("id:{} 调取字段库，获取正文, 异常:{}", contentId, e);
        } finally {
            try {
                closeHttpClient111(httpClient);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;

    }

    private static void closeHttpClient111(CloseableHttpClient client) throws IOException {
        if (client != null) {
            client.close();
        }
    }


}
