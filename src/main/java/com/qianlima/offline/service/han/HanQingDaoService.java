package com.qianlima.offline.service.han;

public interface HanQingDaoService {

    void getQingdao(Integer type, String date);

    /**
     * 青岛市-教育单位
     * @param type
     * @param date
     */
    void getQingdaoByJy(Integer type, String date);

    /**
     * 青岛市-全部数据
     * @param type
     * @param date
     */
    void getQingdaoAll(Integer type, String date);
}