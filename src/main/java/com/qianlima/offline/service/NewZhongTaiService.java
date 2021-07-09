package com.qianlima.offline.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.util.LogUtils;
import com.qianlima.offline.util.QianlimaZTUtil;
import com.qianlima.offline.util.QianlimaZTUtil2;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
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
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class NewZhongTaiService {

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;

    private AtomicInteger atomicInteger = new AtomicInteger(0);

    /**
     * poc、线下交付使用，无需进行实时性校验（获取非content的全字段内容）
     */
    public Map<String, Object> handleZhongTaiGetResultMapWithOther(NoticeMQ noticeMQ){
        Long contentid = Long.valueOf(noticeMQ.getContentid());
        Map<String, Object> resultMap = null;
        try {
            String fileName = "id,title,content,area_areaid,progid,catid,url,updatetime,zhaoBiaoDetail,project_invest,project_invest_unit,project_nature,project_owner" ;
            String validateFields = "id,title,content,area_areaid,progid,catid,url,updatetime,zhaoBiaoDetail,project_invest,project_invest_unit,project_nature,project_owner";
            Map<String, Object> map = QianlimaZTUtil2.getFields( "http://datafetcher.intra.qianlima.com/dc/bidding/fields", String.valueOf(contentid), fileName, validateFields);
            if (map == null) {
                log.error("获取中台接口失败", String.valueOf(contentid));
                throw new RuntimeException("调取中台失败");
            }
            String returnCode = (String) map.get("returnCode");
            if ("500".equals(returnCode) || "1".equals(returnCode)) {
                log.error("该条 info_id：{}，数据调取中台额外字段失败", String.valueOf(contentid));
                throw new RuntimeException("数据调取中台失败");
            } else if ("0".equals(returnCode)) {
                JSONObject data = (JSONObject) map.get("data");
                if (data == null) {
                    log.error("该条 info_id：{}，数据调取中台额外字段失败", String.valueOf(contentid));
                    throw new RuntimeException("数据调取中台失败");
                }
                resultMap = new HashMap<>();
                // 判断中台提取状态
                JSONArray fileds = data.getJSONArray("fields");
                // 是否电子招标
                String isElectronic = null;
                String biddingType = null;
                String areaid = null;
                String title = null;
                String url = null;
                String progid = null;
                String updatetime = null;
                String xmNumber = null;
                String bidingEndTime = null;
                String registrationBeginTime = null;
                String registrationEndTime = null;
                String bidingAcquireTime = null;
                String openBidingTime = null;
                String tenderBeginTime = null;
                String tenderEndTime = null;

                String blBudget = null; //获取百炼预算金额
                String blZhongbiaoAmount = null; //获取百炼中标金额
                StringBuilder blZhaoBiaoUnit = new StringBuilder(); //获取百炼招标单位
                StringBuilder blZhongBiaoUnit = new StringBuilder(); //获取百炼中标单位
                StringBuilder blBidder = new StringBuilder(); //获取候选人
                StringBuilder blAgents = new StringBuilder(); //百炼代理机构
                String content = null;
                String relationName = null;
                String relationWay = null;
                String agentRelationName = null;
                String agentRelationWay = null;
                String linkMan = null;
                String linkPhone = null;

                String projectInvest = null;
                String projectInvestUnit = null;
                String projectNature = null;
                String projectOwner = null;
                String catid = null;
                String extract_proj_name = "";
                String extract_start_date = null;
                String extract_complete_date = null;
                String extract_period = null;

                if (fileds != null && fileds.size() > 0) {
                    for (int d = 0; d < fileds.size(); d++) {
                        JSONObject object = fileds.getJSONObject(d);
                        if (null != object.get("title")) {
                            title = object.getString("title");
                        } else if (null != object.get("url")) {
                            url = object.getString("url");
                        } else if (null != object.get("progid")) {
                            progid = object.getString("progid");
                        } else if (null != object.get("updatetime")) {
                            if (StringUtils.isNotBlank(object.getString("updatetime"))) {
                                updatetime = DateFormatUtils.format(Long.valueOf(object.getString("updatetime")) * 1000L, "yyyy-MM-dd HH:mm:ss");
                            }
                        } if (null != object.get("content")) {
                            content = object.getString("content");
                        }
                        else if (null != object.get("xmNumber")) {
                            xmNumber = object.getString("xmNumber");

                        }else if (null != object.get("extract_proj_name")) {
                            extract_proj_name = object.getString("extract_proj_name");

                        } else if (null != object.get("extract_start_date")) {
                            extract_start_date = object.getString("extract_start_date");

                        } else if (null != object.get("extract_complete_date")) {
                            extract_complete_date = object.getString("extract_complete_date");

                        } else if (null != object.get("extract_period")) {
                            extract_period = object.getString("extract_period");

                        } else if (null != object.get("area_areaid")) {
                            areaid = object.getString("area_areaid");

                        }  else if (null != object.get("project_invest")) {
                            projectInvest = object.getString("project_invest");
                        } else if (null != object.get("project_invest_unit")) {
                            projectInvestUnit = object.getString("project_invest_unit");
                        } else if (null != object.get("project_nature")) {
                            projectNature = object.getString("project_nature");
                        } else if (null != object.get("project_owner")) {
                            projectOwner = object.getString("project_owner");
                        } else if (null != object.get("project_owner")) {
                            projectOwner = object.getString("project_owner");
                        } else if (null != object.get("catid")) {
                            catid = object.getString("catid");
                        } else if (null != object.get("extractDateDetail")) {
                            JSONObject extractDateDetail = object.getJSONObject("extractDateDetail");
                            if (extractDateDetail != null){
                                if (extractDateDetail.get("registration_begin_time") != null) {
                                    registrationBeginTime = extractDateDetail.getString("registration_begin_time");
                                    if (StringUtils.isNotBlank(registrationBeginTime)) {
                                        registrationBeginTime = DateFormatUtils.format(Long.valueOf(registrationBeginTime) * 1000L, "yyyy-MM-dd HH:mm:ss");
                                    }
                                }
                                if (extractDateDetail.get("registration_end_time") != null) {
                                    registrationEndTime = extractDateDetail.getString("registration_end_time");
                                    if (StringUtils.isNotBlank(registrationEndTime)) {
                                        registrationEndTime = DateFormatUtils.format(Long.valueOf(registrationEndTime) * 1000L, "yyyy-MM-dd HH:mm:ss");
                                    }
                                }
                                if (extractDateDetail.get("biding_acquire_time") != null) {
                                    bidingAcquireTime = extractDateDetail.getString("biding_acquire_time");
                                    if (StringUtils.isNotBlank(bidingAcquireTime)) {
                                        bidingAcquireTime = DateFormatUtils.format(Long.valueOf(bidingAcquireTime) * 1000L, "yyyy-MM-dd HH:mm:ss");
                                    }
                                }
                                if (extractDateDetail.get("biding_end_time") != null) {
                                    bidingEndTime = extractDateDetail.getString("biding_end_time");
                                    if (StringUtils.isNotBlank(bidingEndTime)) {
                                        bidingEndTime = DateFormatUtils.format(Long.valueOf(bidingEndTime) * 1000L, "yyyy-MM-dd HH:mm:ss");
                                    }
                                }
                                if (extractDateDetail.get("tender_begin_time") != null) {
                                    tenderBeginTime = extractDateDetail.getString("tender_begin_time");
                                    if (StringUtils.isNotBlank(tenderBeginTime)) {
                                        tenderBeginTime = DateFormatUtils.format(Long.valueOf(tenderBeginTime) * 1000L, "yyyy-MM-dd HH:mm:ss");
                                    }
                                }
                                if (extractDateDetail.get("tender_end_time") != null) {
                                    tenderEndTime = extractDateDetail.getString("tender_end_time");
                                    if (StringUtils.isNotBlank(tenderEndTime)) {
                                        tenderEndTime = DateFormatUtils.format(Long.valueOf(tenderEndTime) * 1000L, "yyyy-MM-dd HH:mm:ss");
                                    }
                                }
                                if (extractDateDetail.get("open_biding_time") != null) {
                                    openBidingTime = extractDateDetail.getString("open_biding_time");
                                    if (StringUtils.isNotBlank(openBidingTime)) {
                                        openBidingTime = DateFormatUtils.format(Long.valueOf(openBidingTime) * 1000L, "yyyy-MM-dd HH:mm:ss");
                                    }
                                }
                            }
                        } else if (null != object.get("biddingTypeDetail")) {
                            JSONObject biddingTypeDetail = object.getJSONObject("biddingTypeDetail");
                            isElectronic = biddingTypeDetail.getString("is_electronic");
                            biddingType = biddingTypeDetail.getString("bidding_type");

                        } else if (null != object.getJSONObject("expandField")) {
                            JSONObject expandField = object.getJSONObject("expandField");
                            if (expandField != null) {
                                //获取百炼预算金额
                                if (expandField.get("budgetDetail") != null) {
                                    JSONObject budgetDetail = (JSONObject) expandField.get("budgetDetail");
                                    if (budgetDetail.get("totalBudget") != null) {
                                        JSONObject totalBudget = (JSONObject) budgetDetail.get("totalBudget");
                                        if (totalBudget.get("budget") != null) {
                                            blBudget = totalBudget.get("budget") != null ? totalBudget.get("budget").toString() : null;
                                        }
                                    }
                                }
                                //获取百炼中标单位
                                int winnersSize = 0;
                                if (expandField.get("winners") != null) {
                                    List<JSONObject> winners = (List<JSONObject>) expandField.get("winners");
                                    for (JSONObject winner : winners) {
                                        winnersSize++;
                                        if (winner.get("bidderDetails") != null) {
                                            List<JSONObject> bidderDetails = (List<JSONObject>) winner.get("bidderDetails");
                                            for (int i = 0; i < bidderDetails.size(); i++) {
                                                if (bidderDetails.get(i).get("bidder") != null) {
                                                    if (i + 1 != bidderDetails.size() || winnersSize != winners.size()) {
                                                        blZhongBiaoUnit.append(bidderDetails.get(i).get("bidder") != null ? bidderDetails.get(i).get("bidder").toString() : null);
                                                        blZhongBiaoUnit.append("、");
                                                    } else {
                                                        blZhongBiaoUnit.append(bidderDetails.get(i).get("bidder") != null ? bidderDetails.get(i).get("bidder").toString() : null);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    //获取候选人
                                    int biddersSize = 0;
                                    if (expandField.get("bidders") != null) {
                                        List<JSONObject> bidders = (List<JSONObject>) expandField.get("bidders");
                                        for (JSONObject bidder : bidders) {
                                            biddersSize++;
                                            if (bidder.get("bidderDetails") != null) {
                                                List<JSONObject> bidderDetails = (List<JSONObject>) bidder.get("bidderDetails");
                                                for (int i = 0; i < bidderDetails.size(); i++) {
                                                    if (bidderDetails.get(i).get("bidder") != null) {
                                                        if (i + 1 != bidderDetails.size() || biddersSize != winners.size()) {
                                                            blBidder.append(bidderDetails.get(i).get("bidder") != null ? bidderDetails.get(i).get("bidder").toString() : null);
                                                            blBidder.append("、");
                                                        } else {
                                                            blBidder.append(bidderDetails.get(i).get("bidder") != null ? bidderDetails.get(i).get("bidder").toString() : null);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    //获取百炼中标金额
                                    if (winners.get(0).get("bidderDetails") != null) {
                                        JSONArray bidderDetails = (JSONArray) winners.get(0).get("bidderDetails");
                                        JSONObject jsonObject = (JSONObject) bidderDetails.get(0);
                                        if (jsonObject.get("amount") != null) {
                                            blZhongbiaoAmount = jsonObject.get("amount") != null ? jsonObject.get("amount").toString() : null;
                                        }
                                    }
                                }
                                //获取百炼招标单位
                                if (expandField.get("tenderees") != null) {
                                    List<JSONObject> tenderees = (List<JSONObject>) expandField.get("tenderees");
                                    for (int i = 0; i < tenderees.size(); i++) {
                                        if (i + 1 != tenderees.size()) {
                                            blZhaoBiaoUnit.append(tenderees.get(i) != null ? tenderees.get(i) : null);
                                            blZhaoBiaoUnit.append("、");
                                        } else {
                                            blZhaoBiaoUnit.append(tenderees.get(i) != null ? tenderees.get(i) : null);
                                        }
                                    }
                                }
                                //获取百炼代理机构
                                if (expandField.get("agents") != null) {
                                    List<JSONObject> agents = (List<JSONObject>) expandField.get("agents");
                                    for (int i = 0; i < agents.size(); i++) {
                                        if (i + 1 != agents.size()) {
                                            blAgents.append(agents.get(i) != null ? agents.get(i) : null);
                                            blAgents.append("、");
                                        } else {
                                            blAgents.append(agents.get(i) != null ? agents.get(i) : null);
                                        }
                                    }
                                }
                            }
                        } else if (null != object.getJSONObject("zhaoBiaoDetail")) {
                            JSONObject zhaoBiaoDetail = object.getJSONObject("zhaoBiaoDetail");
                            if (zhaoBiaoDetail != null) {
                                relationName = zhaoBiaoDetail.get("relationName") != null ? zhaoBiaoDetail.get("relationName").toString() : null;
                                relationWay = zhaoBiaoDetail.get("relationWay") != null ? zhaoBiaoDetail.get("relationWay").toString() : null;
                            }
                        } else if (null != object.getJSONObject("agentDetail")) {
                            JSONObject agentDetail = object.getJSONObject("agentDetail");
                            if (agentDetail != null) {
                                agentRelationName = agentDetail.get("relationName") != null ? agentDetail.get("relationName").toString() : null;
                                agentRelationWay = agentDetail.get("relationWay") != null ? agentDetail.get("relationWay").toString() : null;
                            }
                        } else if (null != object.getJSONObject("zhongbiaoDetail")) {
                            JSONObject zhongbiaoDetail = object.getJSONObject("zhongbiaoDetail");
                            if (zhongbiaoDetail != null) {
                                linkMan = zhongbiaoDetail.get("linkman") != null ? zhongbiaoDetail.get("linkman").toString() : null;
                                linkPhone = zhongbiaoDetail.get("linkphone") != null ? zhongbiaoDetail.get("linkphone").toString() : null;
                            }
                        }
                    }

                    String provinceName = null;
                    String cityName = null;
                    String countryName = null;
                    if (StringUtils.isNotBlank(areaid)) {
                        provinceName = getAreaMap(areaid).get("areaProvince");
                        cityName = getAreaMap(areaid).get("areaCity");
                        countryName = getAreaMap(areaid).get("areaCountry");
                    }

                    // 判断是否包含附件
                    String isHasAddition = "否";
                    StringBuilder urls = new StringBuilder();
                    //List<Map<String, Object>> jobList1 = gwJdbcTemplate.queryForList("select * from phpcms_c_zb_file where contentid = ?", contentid);
                    //for (Map<String, Object> stringObjectMap : jobList1) {
                    //    urls.append("http://file.qianlima.com:11180/ae_ids/download/download_out.jsp?id=" + stringObjectMap.get("fileid").toString());
                    //}
                    if (StringUtils.isNotBlank(urls.toString())) {
                        isHasAddition = "是";
                    }
                    log.info("处理到非content外全字段信息dao:{}", atomicInteger.incrementAndGet());
                    resultMap.put("catid", catid); //
                    resultMap.put("project_invest", projectInvest); //
                    resultMap.put("project_invest_unit", projectInvestUnit);
                    resultMap.put("project_nature", projectNature); //
                    resultMap.put("project_owner", projectOwner); //
                    resultMap.put("task_id", noticeMQ.getTaskId());
                    resultMap.put("keyword", noticeMQ.getKeyword());
                    resultMap.put("content_id", contentid); // contentId
                    resultMap.put("code", noticeMQ.getF()); //F词
                    resultMap.put("title", title); // 标题
                    resultMap.put("content", content); //正文
                    resultMap.put("province", provinceName); // 省
                    resultMap.put("city", cityName); // 市
                    resultMap.put("country", countryName); // 县
                    resultMap.put("url", url); // url
                    resultMap.put("xmNumber", xmNumber); //序列号
                    resultMap.put("bidding_type", biddingType); // 招标方式
                    resultMap.put("progid", progid); // 信息类型
                    resultMap.put("baiLian_budget", blBudget); //获取百炼预算金额
                    resultMap.put("baiLian_amount_unit", blZhongbiaoAmount);//获取百炼中标金额
                    resultMap.put("zhong_biao_unit", blZhongBiaoUnit); //获取百炼中标单位
                    resultMap.put("zhao_biao_unit", blZhaoBiaoUnit);//获取百炼招标单位
                    resultMap.put("bidder", blBidder);  //候选人
                    resultMap.put("agent_unit", blAgents);  //百炼代理机构
                    resultMap.put("relation_name", LogUtils.format(relationName));
                    resultMap.put("relation_way", LogUtils.format(relationWay));
                    resultMap.put("agent_relation_ame", agentRelationName);
                    resultMap.put("agent_relation_way", agentRelationWay);
                    resultMap.put("link_man", LogUtils.format(linkMan));
                    resultMap.put("link_phone", LogUtils.format(linkPhone));
                    resultMap.put("registration_begin_time", registrationBeginTime);  //报名开始时间
                    resultMap.put("registration_end_time", registrationEndTime); //报名截止时间
                    resultMap.put("biding_acquire_time", bidingAcquireTime);  //标书获取时间
                    resultMap.put("open_biding_time", openBidingTime);  //开标时间
                    resultMap.put("tender_begin_time", tenderBeginTime);  //投标开始时间
                    resultMap.put("tender_end_time", tenderEndTime);  //投标截止时间
                    resultMap.put("biding_end_time", bidingEndTime); //标书截止时间
                    resultMap.put("update_time", updatetime);  // 更新时间
                    resultMap.put("is_electronic", isElectronic);  // 是否电子招标
                    resultMap.put("isfile", isHasAddition); // 是否包含附件
                    resultMap.put("type", "");  // 预留字段1
                    resultMap.put("keyword_term", ""); // 预留字段2
                    resultMap.put("extract_proj_name", extract_proj_name);
                    resultMap.put("extract_start_date", extract_start_date);
                    resultMap.put("extract_complete_date", extract_complete_date);
                    resultMap.put("extract_period", extract_period);
                }
            }
        } catch (Exception e) {
            log.error("异常contentid:{} 原因:{}", contentid, e);
            throw new RuntimeException("数据调取中台失败");
        }
        return resultMap;
    }

    //获取数据的status,判断是否为99
    private static final String SELECT_PHPCMS_CONTENT_BY_CONTENTID = "SELECT status FROM phpcms_content where contentid = ? ";

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


    // ka_部门内部省、市、县区域联查
    private final static List<String> kaAreaList = new ArrayList<>();


    public synchronized Map<String, String> getAreaMap(String areaId) {
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
