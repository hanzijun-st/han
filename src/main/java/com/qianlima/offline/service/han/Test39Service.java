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


    /**
     * 毕马威中国
     * @param type
     * @param date
     * @param progidStr
     */
    void getBiMaWei(Integer type, String date, String progidStr);

    /**
     *  毕马威中国-标题检索
     * @param type
     * @param date
     * @param progidStr
     */
    void getBiMaWeiByTitle(Integer type, String date, String progidStr);

    /**
     * 陕西星宝莱厨房设备有限公司
     * @param type
     * @param date
     * @param progidStr
     */
    void getShanXiXingBaoLai(Integer type, String date, String progidStr);

    /**
     * 陕西星宝莱厨房设备有限公司-第一回合2
     * @param type
     * @param date
     * @param progidStr
     */
    void getShanXiXingBaoLai2(Integer type, String date, String progidStr);

    /**
     * 毕马威中国-规则三
     * @param type
     * @param date
     * @param progidStr
     */
    void getBiMaWeiByTitle_3(Integer type, String date, String progidStr);

    /**
     *  毕马威中国-规则三-屏蔽词（金融行业）
     * @param type
     * @param date
     * @param progidStr
     */
    void getBiMaWeiByTitle_3_1(Integer type, String date, String progidStr);

    /**
     * 陕西星宝莱厨房设备有限公司-第二回合
     * @param type
     * @param date
     * @param progidStr
     */
    void getShanXiXingBaoLai2_1(Integer type, String date, String progidStr);

    /**
     * 北京三月雨文化传播有限责任公司
     * @param type
     * @param date
     */
    void getBeiJingSanYue(Integer type, String date,String s);

    /**
     * 卫卫阿尔-石家庄
     * @param type
     * @param date
     * @param s
     */
    void getWwAer(Integer type, String date, String s);

    /**
     * 卡瓦盛邦
     * @param type
     * @param date
     * @param s
     */
    void getKaWaSb(Integer type, String date, String s);

    /**
     * 卫卫阿尔2-石家庄-第二回合
     * @param type
     * @param date
     * @param s
     */
    void getWwAer2(Integer type, String date, String s);
}