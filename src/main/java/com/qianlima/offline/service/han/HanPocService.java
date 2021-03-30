package com.qianlima.offline.service.han;

import java.util.List;
import java.util.Map;

public interface HanPocService {
    /**
     * 新接口
     */
    void getNew(Integer type,String date);

    /**
     * 浙江纽若思医疗科技有限公司
     * @param date
     */
    void getZheJiangNiuRuoSi(String date,Integer type);

    Map<String,Object> getSolr(String tiaojian,String date);

    /**
     *  同方威视
     * @param type 1保存数据库; 0不保存
     * @param date solr中查询的时间
     */
    void getTongFangWeiShi(Integer type, String date);
}