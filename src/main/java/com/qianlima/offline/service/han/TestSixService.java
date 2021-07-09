package com.qianlima.offline.service.han;

import java.util.Map;

public interface TestSixService {
    /**
     * 天融信
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getTianRongXin(Integer type, String date, String s, String name);

    /**
     * 中国石油天然气股份有限公司山东销售分公司
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getShiYouTianRanQi(Integer type, String date, String s, String name);

    /**
     * 天融信-通过id输出poc标准字段
     */
    void getDataById() throws Exception;


    /**
     * 临时数据
     * @param date
     */
    void getGsTongJi(String date,String type);

    /**
     * 中信产业基金
     */
    void getZhongXin() throws Exception;

    /**
     * 宁波弘泰空间结构工程有限公司
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getNingBoHongTai(Integer type, String date, String s, String name);

    /**
     * 浙商银行股份有限公司
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getZheShangYingHang(Integer type, String date, String s, String name);

    /**
     * 浙商银行股份有限公司-c词
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getZheShangYingHangC(Integer type, String date, String s, String name);

    /**
     * 森达美信昌机器工程（广东）有限公司
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getSengDaMeiXin(Integer type, String date, String s, String name);

    /**
     * 森达美信昌机器工程（广东）有限公司-规则二
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getSengDaMeiXin2(Integer type, String date, String s, String name);

    /**
     * 中铁建物业
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getZhongTieJian(Integer type, String date, String s, String name);

    void getZhongTieJian_zhongBiao(Integer type, String date, String s, String name);

    /**
     * 中国石油天然气
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getZhongGuoShiYouTianRanQi(Integer type, String date, String s, String name);

    /**
     * 宁波弘泰空间结构工程有限公司-第二回合
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getNingBoHongTai2(Integer type, String date, String s, String name);

    /**
     * 北京国视
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getBeiJingGuoShi(Integer type, String date, String s, String name);

    /**
     * 大华
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getDaHua(Integer type, String date, String s, String name);

    /**
     * 北京数字认证股份有限公司
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getShuZiRenZheng(Integer type, String date, String s, String name);

    /**
     * 追加关键词
     */
    void getKeyWordById() throws Exception;

    /**
     * 贝朗医疗
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getBeiLangYiLiao(Integer type, String date, String s, String name);

    /**
     * 清华大学
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getQingHuaDaXue(Integer type, String date, String s, String name);

    /**
     * 武汉鑫潭环保高科技有限公司
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getWuHanXinTan(Integer type, String date, String s, String name);

    /**
     * 贝朗医疗-第二回合
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getBeiLangYiLiao2(Integer type, String date, String s, String name);

    /**
     * 贝朗医疗-第二回合-规则三
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getBeiLangYiLiao2_3(Integer type, String date, String s, String name);

    /**
     * 广州市天谱电器有限公司
     * @param type
     * @param date
     * @param s
     * @param name
     * @param gz 规则
     */
    void getGuangZhouTianPu(Integer type, String date, String s, String name,Integer gz);

    /**
     * 奥林巴斯-第三回合
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getAoLinBaSi3(Integer type, String date, String s, String name,Integer tp);

    /**
     * 浙江汉略网络科技有限公司
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getZheJiangShuangLue(Integer type, String date, String s, String name);

    /**
     * 青岛海尔生物医疗股份有限公司
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getQingDaoHaiErShengWu(Integer type, String date, String s, String name);


    /**
     * 深圳华大智造
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getShenZhenHuaDaZhiZao(Integer type, String date, String s, String name);

    //regLocation 注册地址
    //company 企业名称
    //有注册地址，走注册地址
    Map<String,Object> getArea(String regLocation,String company);

    /**
     * 无人机
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getWuRenJi(Integer type, String date, String s, String name);

    /**
     * 深圳大疆-无人机
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getShenZhenDaJiang(Integer type, String date, String s, String name);

    /**
     * 无人机统计
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getShenZhenDaJiangTongJi(Integer type, String date, String s, String name);

    /**
     * 浙江汉略-2
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getZheJiangShuangLue2(Integer type, String date, String s, String name);

    /**
     * 上海恒生聚源
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getShangHaiHengSheng(Integer type, String date, String s, String name,String typeName);

    /**
     * 上海恒生聚源-通过id
     */
    void getShangHaiHengShengById(Integer type);

    /**
     * 上海恒生聚源-第三回合
     * @param type
     * @param date
     * @param s
     * @param name
     * @param typeName
     */
    void getShangHaiHengSheng3(Integer type, String date, String s, String name, Integer typeName);

    void getQuChong();

    void getRunnable();

    /**
     * 奥的斯机电电梯有限公司
     * @param type
     * @param date
     * @param s
     * @param name
     * @param typeName
     */
    void getAoDiSiJiDian(Integer type, String date, String s, String name, Integer typeName);

    /**
     * 奥的斯机电电梯有限公司-拟在建
     * @param type
     * @param date
     * @param s
     * @param name
     * @param typeName
     */
    void getAoDiSiJiDianNzj(Integer type, String date, String s, String name, Integer typeName);

    /**
     * 无人机-4
     * @param type
     * @param date
     * @param s
     * @param name
     */
    void getShenZhenDaJiang4(Integer type, String date, String s, String name);

    void getAoDiSiJiDianNzj2(Integer type, String date, String s, String name, Integer typeName);

    void getBiaozhun(Integer type);
}