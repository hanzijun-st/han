package com.qianlima.offline.service.han;

public interface TestSevenService {
    /**
     * 深圳华大智造科技股份有限公司
     * @param type
     * @param date
     * @param sType
     * @param name
     */
    void getShenZhenHuaDa(Integer type, String date, String sType, String name);

    /**
     * solr上获取数据
     */
    void getSolrDatas();

    void getDaJinEDatas(Integer type, String date);

    /**
     * 杭州博日科技股份有限公司
     * @param type
     * @param date
     * @param sType
     * @param name
     */
    void getHangZhouBoRi(Integer type, String date, String sType, String name);
}