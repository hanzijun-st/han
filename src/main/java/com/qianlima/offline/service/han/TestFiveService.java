package com.qianlima.offline.service.han;

/**
 * 四月相关接口
 */
public interface TestFiveService {
    /**
     * 测试新的controller
     */
    void test5();


    /**
     * 人民卫生出版社-院校
     */
    void getRenMingWeiSheng(Integer type,String date,String s);

    /**
     * 人民卫生出版社-经销商
     * @param type
     * @param date
     * @param s
     */
    void getRenMingWeiShengJxs(Integer type, String date, String s);

    /**
     * 华设设计集团股份有限公司
     * @param type
     * @param date
     * @param s
     */
    void getHuaSheSj(Integer type, String date, String s);

    /**
     * 福建特力惠信息科技股份有限公司-招标
     * @param type
     * @param date
     * @param s
     */
    void getFuJianTeLiHui_zhaobiao(Integer type, String date, String s);

    /**
     *  福建特力惠信息科技股份有限公司-中标
     * @param type
     * @param date
     * @param s
     */
    void getFuJianTeLiHui_zhongbiao(Integer type, String date, String s);

    /**
     * 北京和君咨询有限公司
     * @param type
     * @param date
     * @param s
     */
    void getBeijingHeJun(Integer type, String date, String s);

    void getZhongBiaoUnit(Integer type, String date, String s) throws Exception;

    void getUrl();

    void getZhongBiaoUnitZiDuan(Integer type, String date, String s) throws Exception;

    /**
     * 温州设计集团
     * @param type
     * @param date
     * @param s
     */
    void getZheJiangWenZhou(Integer type, String date, String s);

    void getZheJiangWenZhou2(Integer type, String date, String s);

    /**
     * 测试新规则
     * @param type
     * @param date
     * @param s
     */
    void testNewPoc(Integer type, String date, String s) throws Exception;

    /**
     * 浙江银行
     */
    void getZheJiangYingHang() throws Exception;
}