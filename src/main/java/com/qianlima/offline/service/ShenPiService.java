package com.qianlima.offline.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.approval.bean.ExtractInvestAmount;
import com.qianlima.approval.bean.ExtractNature;
import com.qianlima.approval.bean.ExtractOwner;
import com.qianlima.approval.extractor.InvestAmountExtractor;
import com.qianlima.approval.extractor.NatureExtractor;
import com.qianlima.approval.extractor.PropertyOwnerExtractor;
import com.qianlima.offline.bean.Area;
import com.qianlima.offline.bean.ConstantBean;
import com.qianlima.offline.bean.ItemInfo;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.util.QianlimaZTUtil2;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

@Service
@Slf4j
public class ShenPiService {

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;

    @Autowired
    @Qualifier("lsJdbcTemplate")
    private JdbcTemplate lsJdbcTemplate;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    private CusDataFieldService cusDataFieldService;

    @Autowired
    private NewZhongTaiService newZhongTaiService;

    @Autowired
    private PocDataFieldService pocDataFieldService;

    HashMap<Integer, Area> areaMap = new HashMap<>();

    //获取KA行业标签
    public String searchingHyAllData(String zhaobiaounit) throws IOException {

        HttpClient client = new DefaultHttpClient();
        HttpResponse response = null;
        String url = "http://cusdata.qianlima.com/api/ka/industry?unit=" + zhaobiaounit + "";
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type", "application/json");

        response = client.execute(post);
        String ret = null;
        ret = EntityUtils.toString(response.getEntity(), "UTF-8");


        JSONObject parseObject = JSON.parseObject(ret);
        JSONObject data = parseObject.getJSONObject("data");
        String firstLevel = data.getString("firstLevel") != null ? data.getString("firstLevel") : "";
//        String secondLevel = data.getString("secondLevel");

        return firstLevel;

    }

    @PostConstruct
    public void init() throws IOException {
        List<Map<String, Object>> maps = gwJdbcTemplate.queryForList("SELECT * FROM phpcms_area");
        for (Map<String, Object> map : maps) {
            Area area = new Area();
            area.setAreaid(Integer.valueOf(map.get("areaid").toString()));
            area.setName(map.get("name").toString());
            area.setParentid(map.get("parentid").toString());
            area.setArrparentid(map.get("arrparentid").toString());
            areaMap.put(Integer.valueOf(map.get("areaid").toString()), area);
        }

    }

    private static final String UPDATE_SQL = "INSERT INTO han_shenpi( task_id, contentid, title, content, update_time, progid, url, stage, province, city, country, item_linkman, item_linkphone, item_type, owner_unit, item_amount, keyword, code, keyword_term) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";


    public void getDataFromZhongTaiAndSave(NoticeMQ noticeMQ) {

        boolean result = cusDataFieldService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        List<Map<String, Object>> maps = lsJdbcTemplate.queryForList(ConstantBean.SELECT_TIME_ONE_NOW_02, noticeMQ.getContentid().toString());
        if (maps != null && maps.size() > 0) {
            Map<String, Object> resultMap = maps.get(0);
            ItemInfo itemInfo = getItemDataByZB(resultMap);
            if (itemInfo != null) {

                String linkman = "";
                String linkPhone = "";
                String extract_proj_name = "";
                try {
                    Map<String, Object> map = newZhongTaiService.handleZhongTaiGetResultMapWithOther(noticeMQ);
                    if (map != null) {
                        linkman = map.get("relation_name") != null ? map.get("relation_name").toString() : "";
                        linkPhone = map.get("relation_way") != null ? map.get("relation_way").toString() : "";
                        extract_proj_name = map.get("extract_proj_name") != null ? map.get("extract_proj_name").toString() : "";
                    }
                } catch (Exception e) {
                    log.info("调取中台接口异常");
                }
                ExtractNature extractNature = NatureExtractor.exrtractNature(itemInfo.getItemContent());
                ExtractInvestAmount extractInvestAmount = InvestAmountExtractor.extractInvestAmount(itemInfo.getItemContent());
                ExtractOwner extractOwner = PropertyOwnerExtractor.exrtractNature(itemInfo.getItemContent());

                String itemType = extractNature.getNature();
                String itemAmount = extractInvestAmount.getInvestAmount();
                String ownerUnit = extractOwner.getPropertyOwner();
                log.info("插入到数据库中contentid: {}", itemInfo.getItemId());

                bdJdbcTemplate.update(UPDATE_SQL, noticeMQ.getTaskId(), itemInfo.getItemId(), itemInfo.getItemTitle(), itemInfo.getItemContent(), itemInfo.getItemPublishTime(), itemInfo.getItemStage(),
                        itemInfo.getItemQianlimaUrl(), itemInfo.getItemType(), itemInfo.getAreaProvince(), itemInfo.getAreaCity(), itemInfo.getAreaCountry(), linkman, linkPhone, itemType, ownerUnit, itemAmount, noticeMQ.getKeyword(), extract_proj_name, "");

            }
        }
    }

//    public void getDataFromZhongTaiAndSave(NoticeMQ noticeMQ) throws IOException {
//        boolean result = cusDataFieldService.checkStatus(noticeMQ.getContentid().toString());
//        if (result == false){
//            log.info("contentid:{} 对应的数据状态不是99, 丢弃" , noticeMQ.getContentid().toString());
//            return;
//        }
//        List<Map<String, Object>> maps = gwJdbcTemplate.queryForList(ConstantBean.SELECT_TIME_ONE_NOW_02, noticeMQ.getContentid().toString());
//        if (maps != null &&maps.size() > 0){
//            Map<String, Object> resultMap = maps.get(0);
//            ItemInfo itemInfo = getItemDataByZB(resultMap);
//            if (itemInfo != null) {
//                String linkman = "";
//                String linkPhone = "";
//                try {
//                    Map<String, Object> map = newZhongTaiService.handleZhongTaiGetResultMapWithOther(noticeMQ);
////                    Map<String, Object> map = cusDataFieldService.getAllFieldsWithZiTi(noticeMQ, false);
//                    if (map != null){
//                        linkman = map.get("relation_name") != null ? map.get("relation_name").toString() : "";
//                        linkPhone = map.get("relation_way") != null ? map.get("relation_way").toString() : "";
//                    }
//                } catch (Exception e){
//                    log.info("调取中台接口异常");
//                }
//                ExtractNature extractNature = NatureExtractor.exrtractNature(itemInfo.getItemContent());
//                ExtractInvestAmount extractInvestAmount = InvestAmountExtractor.extractInvestAmount(itemInfo.getItemContent());
//                ExtractOwner extractOwner = PropertyOwnerExtractor.exrtractNature(itemInfo.getItemContent());
//
//                String itemType = extractNature.getNature();
//                String itemAmount = extractInvestAmount.getInvestAmount();
//                //业主单位
//                String ownerUnit = extractOwner.getPropertyOwner();
//
////                String firstLevel = searchingHyAllData(ownerUnit);
//
//                bdJdbcTemplate.update(UPDATE_SQL, noticeMQ.getTaskId(), itemInfo.getItemId(), itemInfo.getItemTitle(), itemInfo.getItemContent(), itemInfo.getItemPublishTime(),itemInfo.getItemStage(),
//                        itemInfo.getItemQianlimaUrl(), itemInfo.getItemType(), itemInfo.getAreaProvince(), itemInfo.getAreaCity(), itemInfo.getAreaCountry(), linkman, linkPhone, itemType, ownerUnit, itemAmount, noticeMQ.getKeyword(), null, null);
//                log.info("contentid:{} 数据处理成功!!!!!!!!!!!!!" , noticeMQ.getContentid().toString());
//            }
//        }
//    }


    // ka_部门内部省、市、县区域联查
    private final static List<String> kaAreaList = new ArrayList<>();

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


    private ItemInfo getItemDataByZB(Map<String, Object> itemDataMap) {
        ItemInfo itemInfo = new ItemInfo();
        if (itemDataMap != null) {
            // 获取省、市代码
            String areaid = itemDataMap.get("areaid").toString();
            String provinceName = null;
            String cityName = null;
            String countryName = null;
            if (StringUtils.isNotBlank(areaid)) {
                provinceName = getAreaMap(areaid).get("areaProvince");
                cityName = getAreaMap(areaid).get("areaCity");
                countryName = getAreaMap(areaid).get("areaCountry");
            }

            // 获取原文信息
            String contentid = itemDataMap.get("contentid").toString();
            if (StringUtils.isNotBlank(contentid)) {
                Map<String, Object> contentMap = QianlimaZTUtil2.getSingleField("http://datafetcher.intra.qianlima.com/dc/bidding/field", contentid, "content");
                if (contentMap !=null){
                    String returnCode = (String) contentMap.get("returnCode");
                    JSONObject jsonObject = (JSONObject) contentMap.get("data");

                    if ("0".equals(returnCode) && jsonObject != null) {
                        String content = jsonObject.getString("content");
                        itemInfo.setItemContent(content);
                    }
                }
               /* List<Map<String, Object>> contentList = lsJdbcTemplate.queryForList(ConstantBean.SELECT_ITEM_CONTENT_BY_CONTENTID, contentid);
                if (contentList != null && contentList.size() > 0) {
                    String content = contentList.get(0).get("content").toString();
                    itemInfo.setItemContent(content);
                }*/
            }
            // 获取发布时间
            String updateTimeStr = itemDataMap.get("updatetime").toString();
            if (StringUtils.isNotBlank(updateTimeStr)) {
                String itemPublishTime = DateFormatUtils.format(new Date((Long.valueOf(updateTimeStr)) * 1000), "yyyy-MM-dd HH:mm:ss");
                itemInfo.setItemPublishTime(itemPublishTime);
            }
            itemInfo.setItemId(contentid);
//            itemInfo.setAreaId(areaid);
            itemInfo.setItemTitle(itemDataMap.get("title").toString());
            itemInfo.setItemType(itemDataMap.get("catid").toString());
            itemInfo.setItemQianlimaUrl(itemDataMap.get("url").toString());
            itemInfo.setItemStage(itemDataMap.get("progid").toString());
            itemInfo.setAreaProvince(provinceName);
            itemInfo.setAreaCity(cityName);
            itemInfo.setAreaCountry(countryName);

        }
        return itemInfo;
    }

}
