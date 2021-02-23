package com.qianlima.offline.service.han;

/**
 * Created by Administrator on 2021/1/12.
 */
public interface TestService {
    /**
     * 之前的方式获取标的物
     */
    void getBdw();

    /**
     * 新方式获取标的物
     */
    void getNewBdw(Integer type);

    void updateKeyword();

    String downLoad();

    /**
     * 上海联影医疗
     */
    void getShangHaiLy();

    /**
     * 重庆地区
     */
    void getChongqi();

    /**
     * 无人机规则三
     * @param type
     * @param date
     */
    void getZongHengDaPeng3(Integer type, String date);

    /**
     * 合肥航联
     * @param type
     * @param date
     */
    void getHefeiHanglian(Integer type, String date);

    /**
     * 邯郸开发区中电环境科技有限公司
     * @param type
     * @param date
     */
    void getHanDanKaiFaQu(Integer type, String date);

    /**
     * 四川羽医医疗管理有限公司
     * @param type
     * @param date
     */
    void getSiChuanYuYiYiLiao(Integer type, String date);

    /**
     * 北京金万维科技有限公司
     * @param type
     * @param date
     */
    void getJingWanWei(Integer type, String date) throws Exception;


    void getDaoJinSolrAllField();

    /**
     * 北京宇信科技集团股份有限公司
     * @param type
     * @param date
     */
    void getYuxin(Integer type, String date);

    /**
     * 北京宇信科技集团股份有限公司-第二回合
     * @param type
     * @param date
     */
    void getYuxin2(Integer type, String date);


    void getYuxin3(Integer type, String date);

    /**
     * 第一回合4.0
     * @param type
     * @param date
     */
    void getYuxin1_4(Integer type, String date);
}
