package com.qianlima.offline.service.han;

import java.util.Map;

/**
 * Created by Administrator on 2021/1/12.
 */
public interface TestService {
    /**
     * 最新方式获取标的物
     */
    void getBdw(Integer type);

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


    /**
     * 北京金万维科技第三回合-1
     * @param type
     * @param date
     */
    void getYuxin3(Integer type, String date) throws  Exception;

    /**
     * 第一回合4.0
     * @param type
     * @param date
     */
    void getYuxin1_4(Integer type, String date);


    void getError(Integer type, String date);

    Map getBeiJianGong(String units);

    /**
     * 大金额
     */
    void getKaHangYeSolrAllField();

    /**
     * 文思海辉
     * @param type
     * @param date
     */
    void getWenSiHaiHuib(Integer type, String date) throws Exception;

    /**
     * 文思海辉第二回合
     * @param type
     * @param date
     */
    void getWenSiHaiHuib2_1(Integer type, String date);

    /**
     * 文思海辉第二回合-规则二
     * @param type
     * @param date
     */
    void getWenSiHaiHuib2_2(Integer type, String date);

    /**
     * 奥林巴斯第二回合
     * @param type
     * @param date
     */
    void getAolinbasi2(Integer type, String date);

    /**
     * 奥林巴斯第二回合-全文检索关键词b
     * @param type
     * @param date
     */
    void getAolinbasi2_qw(Integer type, String date);

    void getAolinbasi2_3(Integer type, String date);

    /**
     * 贝登
     * @param type
     * @param date
     */
    void getBeiDeng(Integer type, String date) throws Exception;

    /**
     * 文思海辉-交付数据
     * @param type
     * @param date
     */
    void getWensihaihui_Jiaofu(Integer type, String date);

    /**
     * 贝登第二次
     * @param type
     * @param date
     */
    void getBeiDeng2(Integer type, String date)  throws Exception;

    /**
     * 云南獾少科技
     * @param type
     * @param date
     */
    void getYuNanMaoShao(Integer type, String date) throws Exception;

    /**
     * 通过模板导出excel
     */
    void downByModel();
}
