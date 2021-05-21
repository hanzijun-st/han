package com.qianlima.offline.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.offline.bean.NoticeMQ;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 对外提供poc标准字段输出接口 -- 标准字段输出接口
 * 1. 全部自提信息
 * 2. 混合信息（先自提、后百炼）
 */
@Service
@Slf4j
public class PocDataFieldService {

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    @Qualifier("djeJdbcTemplate")
    private JdbcTemplate djeJdbcTemplate;

    // 数据入库操作
    private static final String INSERT_ZT_RESULT_HXR = "INSERT INTO han_data_poc (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time," +
            "is_electronic,code,isfile,keyword_term,keywords, infoTypeSegment,monitorUrl, pocDetailUrl, extract_proj_name, black_word,allForThreeCode,allForFourCode,allForFiveCode) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    //获取数据的status,判断是否为99,
    private static final String SELECT_PHPCMS_CONTENT_BY_CONTENTID = "SELECT status FROM phpcms_content where contentid = ? ";

    // ka_部门内部省、市、县区域联查
    private final static List<String> kaAreaList = new ArrayList<>();

    /**
     * 判断当前数据的数据状态
     */
    public boolean checkStatus(String contentid){
        boolean result = false;
        Map<String, Object> map = gwJdbcTemplate.queryForMap(SELECT_PHPCMS_CONTENT_BY_CONTENTID, contentid);
        if (map != null && map.get("status") != null) {
            int status = Integer.parseInt(map.get("status").toString());
            if (status == 99) {
                result = true;
            }
        }
        return result;
    }


    /**
     * 保存数据入库
     */
    public void saveIntoMysql(Map<String, Object> map, String contentId){
        // 进行大金额替换操作
        List<Map<String, Object>> maps = djeJdbcTemplate.queryForList("select info_id, winner_amount, budget from amount_code where info_id = ?", contentId);
        if (maps != null && maps.size() > 0){
            // 由于大金额处理的特殊性，只能用null进行判断
            String winnerAmount = maps.get(0).get("winner_amount") != null ? maps.get(0).get("winner_amount").toString() : null;
            if (winnerAmount != null){
                map.put("baiLian_amount_unit", winnerAmount);
            }
            String budget = maps.get(0).get("budget") != null ? maps.get(0).get("budget").toString() : null;
            if (budget != null){
                map.put("baiLian_budget", budget);
            }
        }
        bdJdbcTemplate.update(INSERT_ZT_RESULT_HXR,map.get("task_id"), map.get("keyword"), map.get("content_id"), map.get("title"),
                map.get("content"), map.get("province"), map.get("city"), map.get("country"), map.get("url"), map.get("baiLian_budget"),
                map.get("baiLian_amount_unit"), map.get("xmNumber"), map.get("bidding_type"), map.get("progid"), map.get("zhao_biao_unit"),
                map.get("relation_name"), map.get("relation_way"), map.get("agent_unit"), map.get("agent_relation_ame"),
                map.get("agent_relation_way"), map.get("zhong_biao_unit"), map.get("link_man"), map.get("link_phone"),
                map.get("registration_begin_time"), map.get("registration_end_time"), map.get("biding_acquire_time"),
                map.get("biding_end_time"), map.get("tender_begin_time"), map.get("tender_end_time"), map.get("update_time"),
                map.get("type"), map.get("bidder"), map.get("notice_types"), map.get("open_biding_time"), map.get("is_electronic"),
                map.get("code"), map.get("isfile"), map.get("keyword_term"),map.get("keywords"),map.get("infoTypeSegment"),
                map.get("monitorUrl"), map.get("pocDetailUrl"), map.get("extract_proj_name"), map.get("black_word"),
                map.get("allForThreeCode"),map.get("allForFourCode"),map.get("allForFiveCode"));
    }

    /**
     * 获取poc标准字段接口-自提字段
     * @param noticeMQ
     * @return
     */
    public Map<String, Object> getFieldsWithZiTi(NoticeMQ noticeMQ, String contentId){
        JSONObject fieldObject = getJsonObjectWithFields(contentId);
        if (fieldObject == null){
            log.error("contentId:{} 调用数据详情接口异常", contentId);
            return null;
        }
        Map<String, Object> hashMap = getResultMapWithGenPart(fieldObject);
        if (hashMap.isEmpty()){
            log.error("contentId:{} 调用数据详情接口, 获取常用字段异常", contentId);
            return null;
        }
        // 非招标单位、中标单位、金额字段
        hashMap = getResultMapWithZiTi(fieldObject, hashMap);
        hashMap.put("task_id", noticeMQ.getTaskId());
        hashMap.put("keyword", noticeMQ.getKeyword());
        hashMap.put("content_id", noticeMQ.getContentid().toString());
        hashMap.put("code", noticeMQ.getF()); //F词
        hashMap.put("black_word", noticeMQ.getBlackWord());//黑词
        hashMap.put("monitorUrl", "http://monitor.ka.qianlima.com/#/checkDetails?pushId=" + noticeMQ.getContentid());
        hashMap.put("pocDetailUrl", "http://cusdata.qianlima.com/detail/" + noticeMQ.getContentid() + ".html");
        return hashMap;
    }

    /**
     * 获取poc标准字段接口-混合字段
     * @param noticeMQ
     * @return
     */
    public Map<String, Object> getFieldsWithHunHe(NoticeMQ noticeMQ, String contentId){
        JSONObject fieldObject = getJsonObjectWithFields(contentId);
        if (fieldObject == null){
            log.error("contentId:{} 调用数据详情接口异常", contentId);
            return null;
        }
        // 非招标单位、中标单位、金额字段
        Map<String, Object> hashMap = getResultMapWithGenPart(fieldObject);
        if (hashMap.isEmpty()){
            log.error("contentId:{} 调用数据详情接口, 获取常用字段异常", contentId);
            return null;
        }
        hashMap = getResultMapWithHunHe(fieldObject, hashMap);
        hashMap.put("task_id", noticeMQ.getTaskId());
        hashMap.put("keyword", noticeMQ.getKeyword());
        hashMap.put("content_id", noticeMQ.getContentid().toString());
        hashMap.put("code", noticeMQ.getF()); //F词
        hashMap.put("black_word", noticeMQ.getBlackWord());//黑词
        hashMap.put("monitorUrl", "http://monitor.ka.qianlima.com/#/checkDetails?pushId=" + noticeMQ.getContentid());
        hashMap.put("pocDetailUrl", "http://cusdata.qianlima.com/detail/" + noticeMQ.getContentid() + ".html");
        return hashMap;
    }

    private Map<String, Object> getResultMapWithHunHe(JSONObject fieldObject, Map<String, Object> hashMap) {
        if (hashMap.isEmpty()){
            return null;
        }
        // 获取自提招标单位、中标单位
        String zhaoBiaoUnit = fieldObject.getString("extract_zhaoBiaoUnit");
        if (StringUtils.isBlank(zhaoBiaoUnit)){
            JSONObject expandField = fieldObject.getJSONObject("expandField");
            if (expandField != null && expandField.get("tenderees") != null) {
                JSONArray tenderees = expandField.getJSONArray("tenderees");
                if (tenderees != null && tenderees.size() > 0){
                    // 获取第一家百炼招标单位
                    zhaoBiaoUnit = tenderees.getString(0);
                }
            }
        }

        String zhongBiaoUnit = fieldObject.getString("extract_zhongBiaoUnit");
        if (StringUtils.isBlank(zhongBiaoUnit)){
            JSONObject expandField = fieldObject.getJSONObject("expandField");
            if (expandField != null && expandField.get("winners") != null) {
                JSONArray winners = expandField.getJSONArray("winners");
                if (winners != null && winners.size() > 0) {
                    out:
                    for (int i = 0; i < winners.size(); i++) {
                        JSONArray bidderDetails = winners.getJSONObject(i).getJSONArray("bidderDetails");
                        if (bidderDetails != null && bidderDetails.size() > 0) {
                            for (int j = 0; j < bidderDetails.size(); j++) {
                                String bidder = bidderDetails.getJSONObject(j).getString("bidder");
                                if (StringUtils.isNotBlank(bidder)) {
                                    zhongBiaoUnit += bidder + ",";
                                }
                            }
                        }
                    }
                }
            }
            if (StringUtils.isNotBlank(zhongBiaoUnit)){
                zhongBiaoUnit = zhongBiaoUnit.substring(0, zhongBiaoUnit.length() - 1);
            }
        }

        // 获取招标预算(自提预算  需要进行补“元”) 金额：extract_budget ，单位：
        String budget = fieldObject.getString("extract_budget");
        if (StringUtils.isBlank(budget)) {
            JSONObject expandField = fieldObject.getJSONObject("expandField");
            if (expandField != null && expandField.get("budgetDetail") != null) {
                JSONObject budgetDetail = expandField.getJSONObject("budgetDetail");
                if (budgetDetail.get("totalBudget") != null) {
                    JSONObject totalBudget = budgetDetail.getJSONObject("totalBudget");
                    if (totalBudget.get("budget") != null) {
                        budget = totalBudget.get("budget") != null ? totalBudget.get("budget").toString() : null;
                    }
                }
            }

        }

        String winnerAmount = fieldObject.getString("extract_amountUnit");
        if (StringUtils.isBlank(winnerAmount)) {
            JSONObject expandField = fieldObject.getJSONObject("expandField");
            if (expandField != null && expandField.get("winners") != null) {
                List<JSONObject> winners = (List<JSONObject>) expandField.get("winners");
                for (JSONObject winner : winners) {
                    if (winner.get("bidderDetails") != null) {
                        List<JSONObject> bidderDetails = (List<JSONObject>) winner.get("bidderDetails");
                        if (bidderDetails != null && bidderDetails.size() > 0){
                            for (int i = 0; i < bidderDetails.size(); i++) {
                                String amount = bidderDetails.get(i).getString("amount");
                                if (StringUtils.isNotBlank(amount)){
                                    winnerAmount += amount + ",";
                                }
                            }
                        }
                    }
                }
            }
            if (StringUtils.isNotBlank(winnerAmount)){
                winnerAmount = winnerAmount.substring(0, winnerAmount.length() - 1);
            }
        }

        hashMap.put("baiLian_budget", budget);
        hashMap.put("baiLian_amount_unit", winnerAmount);
        hashMap.put("zhong_biao_unit", zhongBiaoUnit);
        hashMap.put("zhao_biao_unit", zhaoBiaoUnit);
        return hashMap;
    }

    private Map<String, Object> getResultMapWithZiTi(JSONObject fieldObject, Map<String, Object> hashMap) {
        if (hashMap.isEmpty()){
            return null;
        }
        // 获取自提招标单位、中标单位
        String zhaoBiaoUnit = fieldObject.getString("extract_zhaoBiaoUnit");
        String zhongBiaoUnit = fieldObject.getString("extract_zhongBiaoUnit");
        // 招标预算、中标金额
        String budget = fieldObject.getString("extract_budget");
        String winnerAmount = fieldObject.getString("extract_amountUnit");
        hashMap.put("baiLian_budget", budget);
        hashMap.put("baiLian_amount_unit", winnerAmount);
        hashMap.put("zhong_biao_unit", zhongBiaoUnit);
        hashMap.put("zhao_biao_unit", zhaoBiaoUnit);
        return hashMap;
    }



    private HashMap<String, Object> getResultMapWithGenPart(JSONObject fieldObject) {
        // 获取省、市、县三级地区
        String provinceName = "";
        String cityName = "";
        String countryName = "";
        String areaAreaId = fieldObject.getString("area_areaid");
        if (StringUtils.isNotBlank(areaAreaId)) {
            provinceName = getAreaMap(areaAreaId).get("areaProvince");
            cityName = getAreaMap(areaAreaId).get("areaCity");
            countryName = getAreaMap(areaAreaId).get("areaCountry");
        }
        // 招标方式、是否点在招标
        String biddingType = "";
        String isElectronic = "";
        if (fieldObject.getJSONObject("biddingTypeDetail") != null) {
            biddingType = fieldObject.getJSONObject("biddingTypeDetail").getString("bidding_type");
            if (StringUtils.isBlank(biddingType)) {
                biddingType = "";
            }
            isElectronic = fieldObject.getJSONObject("biddingTypeDetail").getString("is_electronic");
            if (StringUtils.isBlank(isElectronic)) {
                isElectronic = "";
            }
        }
        // 判断是否包含附件
        String isHasAddition = "0";
        JSONArray attachmentDetail = fieldObject.getJSONArray("attachment_detail");
        if (attachmentDetail != null && attachmentDetail.size() > 0){
            isHasAddition = "1";
        }
        // 招标单位联系人 、招标单位联系方式
        String relationName = "";
        String relationWay = "";
        if (fieldObject.getJSONObject("zhaoBiaoDetail") != null) {
            relationName = fieldObject.getJSONObject("zhaoBiaoDetail").getString("relationName");
            relationWay = fieldObject.getJSONObject("zhaoBiaoDetail").getString("relationWay");
        }
        // 代理单位联系人 、代理单位联系方式
        String agentRelationName = "";
        String agentRelationWay = "";
        if (fieldObject.getJSONObject("agentDetail") != null) {
            agentRelationName = fieldObject.getJSONObject("agentDetail").getString("relationName");
            agentRelationWay = fieldObject.getJSONObject("agentDetail").getString("relationWay");
        }
        // 中标单位联系人 、中标单位联系方式
        String linkMan = "";
        String linkPhone = "";
        if (fieldObject.getJSONObject("zhongbiaoDetail") != null) {
            linkMan = fieldObject.getJSONObject("zhongbiaoDetail").getString("linkman");
            linkPhone = fieldObject.getJSONObject("zhongbiaoDetail").getString("linkphone");
        }
        // 发布时间
        String updatetime = "";
        if (null != fieldObject.getLong("updatetime")) {
            updatetime = DateFormatUtils.format(fieldObject.getLong("updatetime") * 1000L, "yyyy-MM-dd HH:mm:ss");
        }
        // 获取其他时间
        String registrationBeginTime = "";
        String registrationEndTime = "";
        String bidingAcquireTime = "";
        String openBidingTime = "";
        String tenderBeginTime = "";
        String tenderEndTime = "";
        String bidingEndTime = "";
        JSONObject extractDateDetail = fieldObject.getJSONObject("extractDateDetail");
        if (extractDateDetail != null) {
            if (StringUtils.isNotBlank(extractDateDetail.getString("biding_acquire_time"))) {
                bidingAcquireTime = DateFormatUtils.format(Long.valueOf(extractDateDetail.getString("biding_acquire_time")) * 1000L, "yyyy-MM-dd HH:mm:ss");
            }
            if (StringUtils.isNotBlank(extractDateDetail.getString("biding_end_time"))) {
                bidingEndTime = DateFormatUtils.format(Long.valueOf(extractDateDetail.getString("biding_end_time")) * 1000L, "yyyy-MM-dd HH:mm:ss");
            }
            if (StringUtils.isNotBlank(extractDateDetail.getString("tender_begin_time"))) {
                tenderBeginTime = DateFormatUtils.format(Long.valueOf(extractDateDetail.getString("tender_begin_time")) * 1000L, "yyyy-MM-dd HH:mm:ss");
            }
            if (StringUtils.isNotBlank(extractDateDetail.getString("tender_end_time"))) {
                tenderEndTime = DateFormatUtils.format(Long.valueOf(extractDateDetail.getString("tender_end_time")) * 1000L, "yyyy-MM-dd HH:mm:ss");
            }
            if (StringUtils.isNotBlank(extractDateDetail.getString("open_biding_time"))) {
                openBidingTime = DateFormatUtils.format(Long.valueOf(extractDateDetail.getString("open_biding_time")) * 1000L, "yyyy-MM-dd HH:mm:ss");
            }
            if (StringUtils.isNotBlank(extractDateDetail.getString("registration_end_time"))) {
                registrationEndTime = DateFormatUtils.format(Long.valueOf(extractDateDetail.getString("registration_end_time")) * 1000L, "yyyy-MM-dd HH:mm:ss");
            }
        }

        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("catid", fieldObject.getString("catid")); // catId
        resultMap.put("project_invest", fieldObject.getString("project_invest")); // 项目投资 （审批项目专用）
        resultMap.put("project_invest_unit", fieldObject.getString("project_invest_unit")); // 项目投资单位 （审批项目专用）
        resultMap.put("project_nature", fieldObject.getString("project_nature")); // 项目性质（审批项目专用）
        resultMap.put("project_owner", fieldObject.getString("project_owner")); // 业主单位（审批项目专用）
        resultMap.put("title", fieldObject.getString("title")); // 标题
        resultMap.put("province", provinceName); // 省
        resultMap.put("city", cityName); // 市
        resultMap.put("country", countryName); // 县
        resultMap.put("url", fieldObject.getString("url")); // url
        resultMap.put("xmNumber", fieldObject.getString("xmNumber")); //序列号
        resultMap.put("bidding_type", biddingType); // 招标方式
        resultMap.put("progid", fieldObject.getString("progid")); // 信息类型
        resultMap.put("update_time", updatetime);  // 更新时间
        resultMap.put("is_electronic", isElectronic);  // 是否电子招标
        resultMap.put("isfile", isHasAddition); // 是否包含附件
        resultMap.put("relation_name", relationName); // 招标单位联系人
        resultMap.put("relation_way", relationWay); // 招标单位联系电话
        resultMap.put("agent_relation_ame", agentRelationName); // 中标单位联系人
        resultMap.put("agent_relation_way", agentRelationWay); // 中标单位联系电话
        resultMap.put("link_man", linkMan); // 代理机构单位联系人
        resultMap.put("link_phone", linkPhone); // 代理机构单位联系电话
        resultMap.put("infoTypeSegment", fieldObject.getString("notice_segment_type"));
        resultMap.put("extract_proj_name", fieldObject.getString("extract_proj_name")); // 预留字段2
        resultMap.put("registration_begin_time", registrationBeginTime);  //报名开始时间
        resultMap.put("registration_end_time", registrationEndTime); //报名截止时间
        resultMap.put("biding_acquire_time", bidingAcquireTime);  //标书获取时间
        resultMap.put("open_biding_time", openBidingTime);  //开标时间
        resultMap.put("tender_begin_time", tenderBeginTime);  //投标开始时间
        resultMap.put("tender_end_time", tenderEndTime);  //投标截止时间
        resultMap.put("biding_end_time", bidingEndTime); //标书截止时间
        resultMap.put("content", ""); //正文
        resultMap.put("type", "");  // 预留字段1
        resultMap.put("keyword_term", areaAreaId); // 预留字段2-地区id
        return resultMap;
    }



    private JSONObject getJsonObjectWithFields(String infoId){
        JSONObject jsonObject = null;
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(60000)
                    .setSocketTimeout(60000).setConnectTimeout(60000).build();
            //4、创建HttpGet请求
            HttpGet httpGet = new HttpGet("http://monitor.ka.qianlima.com//zt/api/"+infoId);
            httpGet.setConfig(requestConfig);
            CloseableHttpResponse response = client.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                log.info("=====调用分支机构接口====");
                String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                if (StringUtils.isNotBlank(result)){
                    jsonObject = JSON.parseObject(result);
                }
            } else {
                log.info("infoId:{} 调用数据详情接口异常, 返回状态不是 200 ", infoId);
                throw new RuntimeException("调用数据详情接口异常，请联系管理员， 返回状态不是 200 ");
            }
        } catch (Exception e){
            log.error("调用数据详情接口异常:{}, 获取不到详情数据", e);
        }
        if (jsonObject == null){
            log.error("调用数据详情接口异常", infoId);
            throw new RuntimeException("调用数据详情接口异常， 获取到的数据为 空 ");
        }
        String code = jsonObject.getString("code");
        if ("-1".equals(code) || "1".equals(code) || "2".equals(code)) {
            log.error("infoId:{} 调用数据详情接口异常, 对应的状态码 code ：{} ", infoId, code);
            throw new RuntimeException("调用数据详情接口异常");
        }
        return jsonObject.getJSONObject("data");
    }


    // 获取地区映射
    private synchronized Map<String, String> getAreaMap(String areaId) {
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
}
