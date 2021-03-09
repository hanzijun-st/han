package com.qianlima.offline.service.han;

public interface Test39Service {

    /**
     * 测试接口
     */
    void test();

    /**
     * 中软集团
     * @param type
     * @param date
     */
    void getZhongRuan(Integer type, String date,String progidStr);

    /**
     * 中软集团第二回合
     * @param type
     * @param date
     * @param progidStr
     */
    void getZhongRuan2(Integer type, String date, String progidStr);
}