package com.qianlima.offline.service;


import com.qianlima.offline.entity.Score;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OptimizationDataService02 {


    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    private String SELECT_SQL = "select code from han_ali_offline_data group by code";

    private String SELECT_SQL_INFO_ID = "select infoId,infoTitle,infoType,infoPublishTime,areaProvince,areaCity,areaCountry,xmNumber,zhongBiaoUnit,zhaoBiaoUnit,budget,winnerAmount, infoTypeSegment,extractProjName,biddingType from han_ali_offline_data where code = ? order by infoId asc";

    private AtomicInteger atomicInteger = new AtomicInteger(0);

    public void handle(){
        List<Map<String, Object>> maps = bdJdbcTemplate.queryForList(SELECT_SQL);
        log.info("共查询到了size：{} 条记录", maps.size());
        for (Map<String, Object> map : maps) {
            log.info("处理到了num：{} 条", atomicInteger.addAndGet(1));
            String code = map.get("code").toString();
            List<Map<String, Object>> resultMap = bdJdbcTemplate.queryForList(SELECT_SQL_INFO_ID, code);
            if (resultMap != null && resultMap.size() > 0){
                if (resultMap.size() == 1){
                    bdJdbcTemplate.update("update han_ali_offline_data set isOptimization = ? where infoId = ?", "是", code);
                } else {
                    List<Score> scores = new ArrayList<>();
                    for (Map<String, Object> objectMap : resultMap) {
                        String infoId = objectMap.get("infoId") != null ? objectMap.get("infoId").toString() : "";
                        Integer score = judgeBJGField(objectMap);
                        scores.add(new Score(infoId, score));
                    }
                    // 获取得分最高的 infoId
                    scores = scores.stream().sorted(Comparator.comparing(Score::getNum).reversed()).collect(Collectors.toList());

                    for (int i = 0; i < scores.size(); i++) {
                        Score score = scores.get(i);
                        if (i == 0){
                            bdJdbcTemplate.update("update han_ali_offline_data set isOptimization = ? where infoId = ?", "是", score.getInfoId());
                        } else {
                            bdJdbcTemplate.update("update han_ali_offline_data set isOptimization = ? where infoId = ?", "否", score.getInfoId());
                        }
                    }
                }
            }
        }
    }


    /**
     * 字段分级
     */
    private Integer judgeBJGField(Map<String, Object> objectMap) {
        Integer judgeScore = 0;
        // 获取 字段分级 相关信息
        String zhongBiaoUnit = objectMap.get("zhongBiaoUnit") != null ? objectMap.get("zhongBiaoUnit").toString() : "";
        String infoType = objectMap.get("infoType") != null ? objectMap.get("infoType").toString() : "";
        String winnerAmount = objectMap.get("winnerAmount") != null ? objectMap.get("winnerAmount").toString() : "";
        String budget = objectMap.get("budget") != null ? objectMap.get("budget").toString() : "";
        String zhaoBiaoUnit = objectMap.get("zhaoBiaoUnit") != null ? objectMap.get("zhaoBiaoUnit").toString() : "";
        // 对于 中标类型的数据
        if ("0".equals(infoType) || "1".equals(infoType) || "2".equals(infoType)){
            // 一级信息类型（3、5）AND 中标金额为空 AND 中标单位为空
            judgeScore = 1;
            if (StringUtils.isNotBlank(budget)){
                // 3.3.2、一级信息类型（3、5） AND 中标金额不为空
                judgeScore = 2;
            }
            if (StringUtils.isNotBlank(zhaoBiaoUnit)){
                // 3.2.2、一级信息类型（3、5） AND 中标单位不为空
                judgeScore = 3;
            }
            if (StringUtils.isNotBlank(budget) && StringUtils.isNotBlank(zhaoBiaoUnit)){
                // 3.1.2、一级信息类型（3、5） AND  中标金额不为空 AND 中标单位不为空
                judgeScore = 4;
            }
        } else if ("3".equals(infoType) || "5".equals(infoType)){
            // 一级信息类型（3、5）AND 中标金额为空 AND 中标单位为空
            judgeScore = 1;
            if (StringUtils.isNotBlank(winnerAmount)){
                // 3.3.2、一级信息类型（3、5） AND 中标金额不为空
                judgeScore = 2;
            }
            if (StringUtils.isNotBlank(zhongBiaoUnit)){
                // 3.2.2、一级信息类型（3、5） AND 中标单位不为空
                judgeScore = 3;
            }
            if (StringUtils.isNotBlank(winnerAmount) && StringUtils.isNotBlank(zhongBiaoUnit)){
                // 3.1.2、一级信息类型（3、5） AND  中标金额不为空 AND 中标单位不为空
                judgeScore = 4;
            }
        }
        return judgeScore;
    }
}
