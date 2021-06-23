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

    /**
     * 人民卫生出版社-院校-第二回合
     * @param type
     * @param date
     * @param s
     */
    void getRenMingWeiSheng2(Integer type, String date, String s);

    /**
     * 人民卫生出版社-经销商-第二回合
     * @param type
     * @param date
     * @param s
     */
    void getRenMingWeiShengJxs2(Integer type, String date, String s);

    /**
     * 广西省产业经济与城乡发展研究会
     * @param type
     * @param date
     * @param s
     */
    void getGuangXiChanYe(Integer type, String date, String s,String name);

    /**
     * 上海磐合科学仪器股份有限公司
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getShangHaiQingHe(Integer type, String date, String s, String name);

    /**
     * 网筑投资管理有限公司
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getWangZhuTouZi(Integer type, String date, String s, String name);

    /**
     * 中铁建物业
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getZhongTieJian(Integer type, String date, String s, String name);

    /**
     * 中铁建物业-规则二
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getZhongTieJian2(Integer type, String date, String s, String name);

    /**
     * 防火墙-通知
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getFangHuoQiang(Integer type, String date, String s, String name);

    /**
     * 存contentId
     */
    void getFangHuoQiangToId(String date);

    /**
     * 天融信
     */
    void getTianRongXin(Integer type,String date,String s,String name);

    /**
     * 通过id获取标准字段
     */
    void getDataById() throws Exception;

    /**
     * 苏州嗨森无人机科技有限公司
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getSuZhouHaiSeng(Integer type, String date, String s, String name);

    /**
     * 浙江大华-poc
     */
    void getZheJiangDaHua() throws Exception;


    void getTongJiZhaoBiao() throws Exception;

    /**
     * 碧桂园
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getBiGuiYuan(Integer type, String date, String s, String name);

    /**
     * 统计中标
     */
    void getTongJiZhongBiao() throws Exception;
}