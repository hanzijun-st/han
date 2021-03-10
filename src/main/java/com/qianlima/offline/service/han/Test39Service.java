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

    /**
     * 阿里标题调查
     * @param type
     * @param date
     */
    void getAliBiaoti(Integer type, String date,String progidStr);

    //只是测试数据--删除
    void getTest(Integer type, String date, String progidStr);
}