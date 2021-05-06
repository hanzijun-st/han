package com.qianlima.offline.service.han;

/**
 * 四月相关接口
 */
public interface TestFourService {
    /**
     * 测试新的controller
     */
    void test4();


    void testTongJi(Integer type,String date,String s);

    /**
     * 济南富利通电气技术有限公司
     * @param type
     * @param date
     * @param s
     */
    void getJiNanFuLiTong(Integer type, String date, String s);

    /**
     * 2016年-中节能（山东）环境服务有限公司
     * @param type
     * @param date
     * @param s
     */
    void getZhongjieNeng_2016(Integer type, String date, String s);

    void getZhongjieNeng_4(Integer type, String date, String s);

    /**
     * 顺丰科技有限公司
     * @param type
     * @param date
     * @param s
     */
    void getShunfeng(Integer type, String date, String s);

    /**
     * 荣安物业
     * @param type
     * @param date
     * @param s
     */
    void getRongAnWuYe(Integer type, String date, String s);

    /**
     * 广州欧科信息技术股份有限公司
     * @param type
     * @param date
     * @param s
     */
    void getGuangZhouOuKe(Integer type, String date, String s);

    /**
     * 广州盗梦信息科技有限公司
     * @param type
     * @param date
     * @param s
     */
    void getDaoMengXinXi(Integer type, String date, String s);

    /**
     * 测试电脑性能-调用中台
     * @param type
     * @param date
     * @param s
     */
    void getTestZhongtai(Integer type, String date, String s);

    /**
     * 熠隆医疗设备(上海)
     * @param type
     * @param date
     * @param s
     */
    void getYilongYiLiao(Integer type, String date, String s);

    /**
     *  中节能（山东）
     * @param type
     * @param date
     * @param s
     */
    void getZhongJieNeng(Integer type, String date, String s);

    /**
     *  中节能-2
     * @param type
     * @param date
     * @param s
     */
    void getZhongJieNeng2(Integer type, String date, String s);

    /**
     *  江苏百瑞赢证券咨询有限公司
     * @param type
     * @param date
     * @param s
     */
    void getJiangSuBaiRui(Integer type, String date, String s);

    /**
     *
     * @param type
     * @param date
     * @param s
     */
    void getPoc(Integer type, String date, String s);

    void getPoc2(Integer type, String date, String s) throws Exception;
}