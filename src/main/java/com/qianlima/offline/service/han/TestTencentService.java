package com.qianlima.offline.service.han;

import com.alibaba.fastjson.JSONArray;

import java.util.List;
import java.util.Map;

public interface TestTencentService {

    void saveTencent();

    void jsonTo();

    void toIds() throws Exception;
    //JSONArray getDataType(String title, String content, Long contentid, String infoTypeUrl);

    /**
     *  存库方法2
     */
    void saveData2(List<Map> maps);

    /**
     * 临时调用接口---http调用
     */
    void getNewAddress() throws Exception;

    void getNewAddressToQs() throws Exception;

    Map getNewAddressByContentId(String contentId) throws Exception;

    void getLinShi(String date);

    void getKaisixuanda(String date, Integer type);

    void getKaisixuanda2(String date, Integer type);


    /**
     *  同方威视
     * @param date
     * @param type
     */
    void getTongfangWeiShi(String date, Integer type);

    /**
     * 同方威视-2
     * @param date
     * @param type
     */
    void getTongfangWeiShi2(String date, Integer type);

    void getKaisixuandaCs(String date, Integer type);

    /**
     *  大金额-输出常用字段
     */
    void getDajinE();
}