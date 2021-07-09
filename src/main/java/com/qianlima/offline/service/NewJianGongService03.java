package com.qianlima.offline.service;

import com.qianlima.approval.bean.ExtractInvestAmount;
import com.qianlima.approval.bean.ExtractNature;
import com.qianlima.approval.bean.ExtractOwner;
import com.qianlima.approval.extractor.InvestAmountExtractor;
import com.qianlima.approval.extractor.NatureExtractor;
import com.qianlima.approval.extractor.PropertyOwnerExtractor;
import com.qianlima.offline.bean.ConstantBean;
import com.qianlima.offline.bean.ItemInfo;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.util.OnlineContentSolr;
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
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Slf4j
public class NewJianGongService03 {

    @Autowired
    private OnlineContentSolr onlineContentSolr;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate qlmJdbcTemplate;//未获取

    @Autowired
    private CusDataFieldService cusDataFieldService;

    @Autowired
    private NewZhongTaiService newZhongTaiService;

    private static final String UPDATE_SQL = "INSERT INTO all_item_data( task_id, contentid, title, content, update_time, progid, url, stage, province, city, country, item_linkman, item_linkphone, item_type, owner_unit, item_amount, keyword, code, keyword_term) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";


    public void getSolrAllField() throws Exception{

        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();

        String[] keywords = { "清扫保洁","环卫一体化","垃圾收转运","垃圾转运","垃圾分类服务","垃圾治理","公测管养","市政养护","绿化养护","水域保洁","市容管理","智慧环卫","智能清扫","城市大管家","物业城市","建筑垃圾","垃圾处理","老旧小区服务","三无小区服务"};

        for (String keyword : keywords) {
            List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[20180101 TO 20210601] AND (catid:301 OR catid:601) AND title:\"" + keyword + "\" ", keyword, 3);
            log.info("keyword:{}====查询出了------size：{}条数据", keyword, mqEntities.size());
            if (!mqEntities.isEmpty()) {
                for (NoticeMQ data : mqEntities) {
                    list1.add(data);
                    data.setKeyword(keyword);
                    if (!dataMap.containsKey(data.getContentid().toString())) {
                        list.add(data);
                        dataMap.put(data.getContentid().toString(), "0");
                    }
                }
            }
        }

        log.info("全部数据量："+list1.size());
        log.info("去重之后的数据量："+list.size());
        log.info("==========================");


        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(32);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() ->  getDataFromZhongTaiAndSave(content)));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        }
        log.info("数据全部跑完啦,总数量为：" +1);

    }



    private void getDataFromZhongTaiAndSave(NoticeMQ noticeMQ) {

        boolean result = cusDataFieldService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false){
            log.info("contentid:{} 对应的数据状态不是99, 丢弃" , noticeMQ.getContentid().toString());
            return;
        }
        List<Map<String, Object>> maps = qlmJdbcTemplate.queryForList(ConstantBean.SELECT_TIME_ONE_NOW_02, noticeMQ.getContentid().toString());
        if (maps != null &&maps.size() > 0){
            Map<String, Object> resultMap = maps.get(0);
            ItemInfo itemInfo = getItemDataByZB(resultMap);
            if (itemInfo != null) {

                String linkman = "";
                String linkPhone = "";
                String  extract_proj_name = "";
                try {
                    Map<String, Object> map = null;
                            //newZhongTaiService.handleZhongTaiGetResultMapWithOther(noticeMQ);
                    if (map != null){
                        linkman = map.get("relation_name") != null ? map.get("relation_name").toString() : "";
                        linkPhone = map.get("relation_way") != null ? map.get("relation_way").toString() : "";
                        extract_proj_name = map.get("extract_proj_name") != null ? map.get("extract_proj_name").toString() : "";
                    }
                } catch (Exception e){
                    log.info("调取中台接口异常");
                }
                ExtractNature extractNature = NatureExtractor.exrtractNature(itemInfo.getItemContent());
                ExtractInvestAmount extractInvestAmount = InvestAmountExtractor.extractInvestAmount(itemInfo.getItemContent());
                ExtractOwner extractOwner = PropertyOwnerExtractor.exrtractNature(itemInfo.getItemContent());

                String itemType = extractNature.getNature();
                String itemAmount = extractInvestAmount.getInvestAmount();
                String ownerUnit = extractOwner.getPropertyOwner();
                log.info("插入到数据库中contentid: {}", itemInfo.getItemId());
                bdJdbcTemplate.update(UPDATE_SQL, noticeMQ.getTaskId(), itemInfo.getItemId(), itemInfo.getItemTitle(), itemInfo.getItemContent(), itemInfo.getItemPublishTime(),itemInfo.getItemStage(),
                        itemInfo.getItemQianlimaUrl(), itemInfo.getItemType(), itemInfo.getAreaProvince(), itemInfo.getAreaCity(), itemInfo.getAreaCountry(), linkman, linkPhone, itemType, ownerUnit, itemAmount, noticeMQ.getKeyword(), extract_proj_name, "");

            }
        }
    }



    private ItemInfo getItemDataByZB(Map<String, Object> itemDataMap) {
        ItemInfo itemInfo = new ItemInfo();
        if (itemDataMap != null){
            // 获取省、市代码
            String areaid = itemDataMap.get("areaid").toString();

            if (StringUtils.isNotBlank(areaid)) {
                itemInfo.setAreaProvince(getAreaMap(areaid).get("areaProvince"));
                itemInfo.setAreaCity(getAreaMap(areaid).get("areaCity"));
                itemInfo.setAreaCountry(getAreaMap(areaid).get("areaCountry"));
            }

            // 获取原文信息
            String contentid = itemDataMap.get("contentid").toString();
            if (StringUtils.isNotBlank(contentid)){
                List<Map<String, Object>> contentList = qlmJdbcTemplate.queryForList(ConstantBean.SELECT_ITEM_CONTENT_BY_CONTENTID, contentid);
                if (contentList != null && contentList.size() > 0){
                    String content = contentList.get(0).get("content").toString();
                    itemInfo.setItemContent(content);
                }
            }
            // 获取发布时间
            String updateTimeStr = itemDataMap.get("updatetime").toString();
            if (StringUtils.isNotBlank(updateTimeStr)){
                String itemPublishTime = DateFormatUtils.format(new Date((Long.valueOf(updateTimeStr)) * 1000), "yyyy-MM-dd HH:mm:ss");
                itemInfo.setItemPublishTime(itemPublishTime);
            }
            itemInfo.setItemId(contentid);
            itemInfo.setAreaId(areaid);
            itemInfo.setItemTitle(itemDataMap.get("title").toString());
            itemInfo.setItemType(itemDataMap.get("catid").toString());
            itemInfo.setItemQianlimaUrl(itemDataMap.get("url").toString());
            itemInfo.setItemStage(itemDataMap.get("progid").toString());
        }
        return itemInfo;
    }


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

}
