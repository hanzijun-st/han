package com.qianlima.offline.service.han.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.service.PocDataFieldService;
import com.qianlima.offline.service.han.CusDataNewService;
import com.qianlima.offline.service.han.TestFiveService;
import com.qianlima.offline.util.LogUtils;
import com.qianlima.offline.util.OnlineContentSolr;
import com.qianlima.offline.util.QianlimaZTUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Slf4j
public class TestFiveServiceImpl implements TestFiveService {

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    @Qualifier("djeJdbcTemplate")
    private JdbcTemplate djeJdbcTemplate;

    @Autowired
    private OnlineContentSolr onlineContentSolr;

    @Autowired
    private CurrencyServiceImpl currencyService;

    @Autowired
    private CusDataNewService cusDataNewService;

    @Resource
    @Qualifier("qlyMongoTemplate")
    private MongoTemplate qlyDbTemplate;

    @Autowired
    private PocDataFieldService pocDataFieldService;//poc新规则


    /**
     *  入库sql
     */
    public static final String INSERT_ZT_FOUR = "INSERT INTO han_new_data (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time,is_electronic,code,isfile,keyword_term,keywords," +
            " infoTypeSegment,monitorUrl, pocDetailUrl,first,second,allForThreeCode,allForFourCode,allForFiveCode) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";


    /**
     * 入库操作语句
     * @param map
     */
    public void saveIntoMysql(Map<String, Object> map){
        String contentId = map.get("content_id").toString();
        //进行大金额替换操作
        List<Map<String, Object>> maps = djeJdbcTemplate.queryForList("select info_id, winner_amount, budget from amount_code where info_id = ?", contentId);
        if (maps != null && maps.size() > 0) {
            // 由于大金额处理的特殊性，只能用null进行判断
            String winnerAmount = maps.get(0).get("winner_amount") != null ? maps.get(0).get("winner_amount").toString() : null;
            if (winnerAmount != null) {
                map.put("baiLian_amount_unit", winnerAmount);
            }
            String budget = maps.get(0).get("budget") != null ? maps.get(0).get("budget").toString() : null;
            if (budget != null) {
                map.put("baiLian_budget", budget);
            }
        }
        bdJdbcTemplate.update(INSERT_ZT_FOUR,map.get("task_id"), map.get("keyword"), map.get("content_id"), map.get("title"),
                map.get("content"), map.get("province"), map.get("city"), map.get("country"), map.get("url"), map.get("baiLian_budget"),
                map.get("baiLian_amount_unit"), map.get("xmNumber"), map.get("bidding_type"), map.get("progid"), map.get("zhao_biao_unit"),
                map.get("relation_name"), map.get("relation_way"), map.get("agent_unit"), map.get("agent_relation_ame"),
                map.get("agent_relation_way"), map.get("zhong_biao_unit"), map.get("link_man"), map.get("link_phone"),
                map.get("registration_begin_time"), map.get("registration_end_time"), map.get("biding_acquire_time"),
                map.get("biding_end_time"), map.get("tender_begin_time"), map.get("tender_end_time"), map.get("update_time"),
                map.get("type"), map.get("bidder"), map.get("notice_types"), map.get("open_biding_time"), map.get("is_electronic"),
                map.get("code"), map.get("isfile"), map.get("keyword_term"),map.get("keywords"),map.get("infoTypeSegment"),map.get("monitorUrl"), map.get("pocDetailUrl"),
                map.get("first"), map.get("second"),map.get("allForThreeCode"),map.get("allForFourCode"),map.get("allForFiveCode")

        );
    }



    @Override
    public void test5() {
        log.info("五月份接口：{}","没有调取任何接口,只是测试项目启动");
    }

    @Override
    public void getRenMingWeiSheng(Integer type,String date,String s) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据

        try {
            String[] aa ={"山东大学齐鲁医学院","山东第一医科大学","滨州医学院","济宁医学院","潍坊医学院","德州学院","山东中医药大学","齐鲁医药学院","青岛大学医学院","潍坊科技学院","山东协和学院","山东力明科技职业学院","山东英才学院","齐鲁理工学院","山东医学高等专科学校","山东中医药高等专科学校","菏泽医学专科学校","济南护理职业学院","泰山护理职业学院","潍坊护理职业学院","枣庄职业学院","枣庄科技职业学院","聊城职业技术学院","滨州职业学院","中国石油大学胜利学院","淄博职业学院","山东药品食品职业学院","菏泽家政学院","山东省济宁卫生学校","山东省烟台护士学校","山东省青岛第二卫生学校","日照市卫生学校","曲阜中医药学校","平阴县职业教育中心","山东省临沂卫生学校","山东煤炭卫生学校","山东省青岛卫生学校","山东省莱阳卫生学校","华中科技大学同济医学院","武汉大学医学部","湖北医药学院","湖北中医药大学","湖北科技学院医学院","湖北理工学院医学部","武汉科技大学医学院","江汉大学医学院","武汉轻工大学医学院","长江大学医学院","湖北民族大学医学部","三峡大学医学院","三峡大学科技学院医学院","湖北文理学院医学院","荆楚理工学院医学院","武汉城市学院医学院","湖北民族大学科技学院","武汉大学医学职业技术学院","咸宁职业技术学院医学院","鄂州职业大学医学院","黄冈职业技术学院","武汉民政职业学院","武汉设计工程学院","湖北职业技术学院","仙桃职业学院医学院","襄阳职业技术学院医学院","随州职业技术学院","湖北中医药高等专科学校","湖北三峡职业技术学院医学院","武汉第二卫生学校","武汉市黄陂区卫生学校","武汉市江夏区卫生学校","恩施州卫生学校","十堰市医药卫生学校","咸宁职业教育（集团）学校","湖北新产业技师学院","长江职业学院","荆州职业技术学院","天门职业学院","恩施职业技术学院","湖北省潜江市卫生学校","湖北省十堰市医学科技学校","武汉铁路职业技术学院","武汉助产学校","梅州市卫生学校","韶关学院医学院","佛山市南海区卫生职业技术学校","广东黄埔卫生职业技术学校","广东省连州卫生学校","广东省新兴中药学校","广州卫生职业技术学院","广东茂名健康职业学院","河源市卫生学校","惠州卫生职业技术学院","珠海市卫生学校","廉江市卫生职业技术学校","中山大学新华学院","肇庆医学高等专科学校","韶关学院医学院","汕头市卫生学校","清远市德圣健康职业技术学校","梅州市卫生职业技术学校","江门市新会区卫生成人中等专业学校","嘉应学院医学院","高州市卫生学校","揭阳市卫生学校","广东医科大学","广州中医药大学","广东省食品药品学校","黄埔卫生学校","广宁卫校","韶关学院医学院","江门中医职业学院","阳江卫生学校","湛江卫生学校","湛江中医学校","新会卫校","南方医科大学","广东药科大学","广州医科大学"};
            String[] bb ={"图书","书籍","书店","借阅","文献","小说","绘本","读物","书刊","书本","史籍","书库","音像制品","看书","旧书","图画书","文集","阅读","教程","国学","购书","讲书","电子书","教材","课件","课本","大纲","教科书","用书","辅导书","辅导资料","课程标准","参考书","教辅","童书","编著","书目","读书","泛读","杂志","真题","试题","参考书","工具书","音像","CD","唱片","碟片","影碟","音象","专辑","视听","唱碟","音频","录像","光盘","媒体","影像","DVD","光碟","磁碟","视频","影音","播音","特效","影视","动画","专辑","画册","语音","家庭影院","图像","图形","宣传片","拍摄","教学资源","课程制作","专刊","期刊"};
            String[] blacks ={"维修","搬迁","图书楼","装修","扩建","监控","施工","安装","建设","改造","修缮","设备"};
            for (String a : aa) {
                for (String b : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        //自提招标单位-a    标题-b
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5) AND (newProvince:\"" + "山东省" + "\" OR newProvince:\"" + "广东省" + "\" OR newProvince:\"" + "湖北省" + "\")  AND zhaoBiaoUnit:\"" + a + "\"  AND title:\"" + b + "\"", a+"&"+b, 2);
                        log.info(a.trim()+"&"+b + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    for (String black : blacks) {
                                        if(StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)){
                                            flag = false;
                                            break;
                                        }
                                    }
                                    if (flag){
                                        data.setKeyword(a+"&"+b);
                                        listAll.add(data);
                                    }
                                }
                            }
                        }
                    }));
                }
            }
            for (Future future1 : futureList1) {
                try {
                    future1.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    executorService1.shutdown();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService1.shutdown();

            if (listAll.size() >0){
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll,list.size(),s,"人民卫生出版社-院校",date);

        } catch (Exception e) {
            e.getMessage();
        }
        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(16);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSave(content)));
                }
                for (Future future : futureList) {
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                executorService.shutdown();
            }
        }
        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    @Override
    public void getRenMingWeiShengJxs(Integer type, String date, String s) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据

        try {
            String[] aa ={"济南曙光图书有限公司","济南齐鲁文苑教育实业有限公司","山东杏林图书有限公司","山东新华书店集团有限公司","山东新华书店集团有限公司连锁分公司","山东新华书店集团有限公司济南分公司","青岛新华书店有限责任公司","湖北校苑科技传媒有限公司","湖北省新华书店（集团）有限公司","湖北诚信易科传媒有限公司","湖北三新文化传媒有限公司","武汉协同创新科技有限公司","湖北蓝色畅想图书发行有限公司","武汉北斗星书业有限公司","湖北楚地畅行文化传播有限公司","武汉鑫众邦书业有限公司","武汉朗朗文化传媒有限公司","华师书刊发行中心","华中科技大学图书代办站","广东校卫图书有限公司","广州市方凯科技发展有限公司","广东新华发行集团股份有限公司","广州科信文化发展有限公司","广东蓝色畅想图书发行有限公司"};
            String[] bb ={"图书","书籍","书店","借阅","文献","小说","绘本","读物","书刊","书本","史籍","书库","音像制品","看书","旧书","图画书","文集","阅读","教程","国学","购书","讲书","电子书","教材","课件","课本","大纲","教科书","用书","辅导书","辅导资料","课程标准","参考书","教辅","童书","编著","书目","读书","泛读","杂志","真题","试题","参考书","工具书","音像","CD","唱片","碟片","影碟","音象","专辑","视听","唱碟","音频","录像","光盘","媒体","影像","DVD","光碟","磁碟","视频","影音","播音","特效","影视","动画","专辑","画册","语音","家庭影院","图像","图形","宣传片","拍摄","教学资源","课程制作","专刊","期刊"};
            String[] blacks ={"维修","搬迁","图书楼","装修","扩建","监控","施工","安装","建设","改造","修缮","设备"};
            for (String a : aa) {
                for (String b : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        //自提中标单位-a    标题-b
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5) AND (newProvince:\"" + "山东省" + "\" OR newProvince:\"" + "广东省" + "\" OR newProvince:\"" + "湖北省" + "\")  AND zhongBiaoUnit:\"" + a + "\"  AND title:\"" + b + "\"", a+"&"+b, 2);
                        log.info(a.trim()+"&"+b + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    for (String black : blacks) {
                                        if(StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)){
                                            flag = false;
                                            break;
                                        }
                                    }
                                    if (flag){
                                        data.setKeyword(a+"&"+b);
                                        listAll.add(data);
                                    }
                                }
                            }
                        }
                    }));
                }
            }
            for (Future future1 : futureList1) {
                try {
                    future1.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    executorService1.shutdown();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService1.shutdown();

            if (listAll.size() >0){
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll,list.size(),s,"人民卫生出版社-经销商",date);

        } catch (Exception e) {
            e.getMessage();
        }
        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(16);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSave(content)));
                }
                for (Future future : futureList) {
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                executorService.shutdown();
            }
        }
        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    @Override
    public void getHuaSheSj(Integer type, String date, String s) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        List<Future> futureList1 = new ArrayList<>();

        //关键词a
        try {
            String[] aa ={"综合性公园景观设计","综合交通和换乘枢纽工程设计","智慧建筑设计","岩土工程勘察","铁路勘察","铁路工程设计","隧道设计","水运工程设计","水利设计","市政设计","市政勘察","市政规划设计","生态修复工程","桥梁设计","桥梁勘察","快速路设计设计","景观设计","交通设计","交通建筑设计","建筑设计","环境勘察","海洋勘察","公路设计","工程地质勘察","高铁和城际铁路工程设计","高速公路工程设计","高速公路改扩建工程设计","地下空间和人防工程设计","城市水体综合治理","城市立城市生态道路设计交设计","城市轨道设计","滨水景观设计","山区公路设计","综合管廊"};
            String[] blacks ={"电力设计","中央空调","展览设计","展板设计","平面设计","印刷设计","供热设计","通信系统","版面","宣传","广告设计","设备采购","装修","装饰","招商","测绘","钢板采购","电梯","招租","招商","维保","设备","钢筋","钢板","井盖","材料采购","化验试剂","保洁服务","汽车维保","一体机电脑","智慧交通","网路安全","招聘","信息技术","IT环境","消杀服务","车维保","广告","垃圾应急转运","文书整理","监理","结算设计","通信设备","光缆","保健服务","调查","空气监测","地质工程勘察院","工程地质大队","自动化设备","便携式","专用仪器","复印纸","招待所","培训中心","PPP项目","绿化工程"};
            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    //List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 2]) AND allcontent:\"" + str + "\"", "", 1);//招标
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND allcontent:\"" + str + "\"", "", 1);//中标
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                for (String black : blacks) {
                                    if(StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)){
                                        flag = false;
                                        break;
                                    }
                                }
                                if (flag) {
                                    data.setKeyword(str);
                                    listAll.add(data);
                                }
                            }
                        }
                    }
                }));
            }
            for (Future future1 : futureList1) {
                try {
                    future1.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    executorService1.shutdown();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService1.shutdown();

            if (listAll.size() >0){
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll,list.size(),s,"华设设计集团股份有限公司",date);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(16);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSave(content)));
                }
                for (Future future : futureList) {
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                executorService.shutdown();
            }
        }
        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    @Override
    public void getFuJianTeLiHui_zhaobiao(Integer type, String date, String s) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        List<Future> futureList1 = new ArrayList<>();

        //关键词a
        try {
            String[] aa ={"自然资源"};
            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND allcontent:\"" + str + "\"", "", 1);//招标
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
                                    listAll.add(data);
                                }
                            }
                        }
                    }
                }));
            }
            for (Future future1 : futureList1) {
                try {
                    future1.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    executorService1.shutdown();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService1.shutdown();

            if (listAll.size() >0){
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll,list.size(),s,"福建特力惠信息科技股份有限公司-招标",date);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(16);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSave(content)));
                }
                for (Future future : futureList) {
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                executorService.shutdown();
            }
        }
        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    @Override
    public void getFuJianTeLiHui_zhongbiao(Integer type, String date, String s) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        List<Future> futureList1 = new ArrayList<>();

        //关键词a
        try {
            String[] aa ={"自然资源"};
            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:3 OR progid:5) AND allcontent:\"" + str + "\"", "", 1);//中标
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
                                    listAll.add(data);
                                }
                            }
                        }
                    }
                }));
            }
            for (Future future1 : futureList1) {
                try {
                    future1.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    executorService1.shutdown();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService1.shutdown();

            if (listAll.size() >0){
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll,list.size(),s,"福建特力惠信息科技股份有限公司-中标",date);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(16);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSave(content)));
                }
                for (Future future : futureList) {
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                executorService.shutdown();
            }
        }
        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    /**
     * 北京和君咨询有限公司
     * @param type
     * @param date
     * @param s
     */
    @Override
    public void getBeijingHeJun(Integer type, String date, String s) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        List<Future> futureList1 = new ArrayList<>();

        //关键词a
        try {
            String[] aa ={"北大纵横管理咨询","北京北大纵横管理咨询有限责任公司","北京北大纵横管理咨询有限公司","北大纵横","北京纵横联合","中大咨询","中大管理咨询","广东中大管理咨询集团","中大创新咨询","广东中大管理咨询集团股份有限公司","正略钧策","正略集团","正略钧策企业管理","正略钧策集团股份有限公司","华彩咨询","华彩管理","上海华彩管理咨询有限公司","华夏基石","北京华夏基石企业管理咨询有限公司","和君集团","和君集团有限公司","和君咨询","和君咨询有限公司","和君商学","北京和君商学在线科技股份有限公司","北京纵横联合投资有限公司","广州市中大管理咨询有限公司","北京正略钧策咨询股份有限公司","北京和君咨询有限公司","北京北大保得利投资顾问有限公司","北京凯德欧亚咨询中心有限公司","烟台三校科技园置业有限公司","中大市场调研（深圳）有限公司","广州黑岩股权投资管理有限公司","北京中大创新咨询有限公司","中大管理咨询（深圳）有限公司","中大人才发展（深圳）有限公司","广州市中大信息技术有限公司","广西中大管理咨询有限公司","深圳正略百川信息技术有限公司","武汉正略百川企业管理咨询有限公司","成都正略企业管理咨询有限公司","广州正略钧策企业管理咨询有限公司","天津正略钧策企业管理咨询有限公司","天津正略百川企业管理咨询有限公司","上海正略钧策企业管理咨询有限公司","上海正略企业管理咨询有限公司","上海华彩企业顾问有限公司","北京中人基石文化发展有限公司","嘉兴和君清基股权投资合伙企业","中科建发通信科技有限公司","天津和君企业管理咨询有限公司","和君集团福建企业管理咨询有限公司","北京华职基业教育科技有限公司","和君集团安徽三度企业管理咨询有限公司","和君集团云南企业管理咨询有限公司","和君聚成无锡企业管理咨询有限公司","北京和君新媒体信息技术有限公司","新疆和君咨询有限公司","广州和君商学教育科技有限公司","宁波梅山保税港区三度斋股权投资合伙企业","江西和君商学在线科技有限公司","北京北大纵横管理咨询有限责任公司长沙办事处","广东中大管理咨询集团股份有限公司南京分公司","广东中大管理咨询集团股份有限公司成都分公司","广东中大管理咨询集团股份有限公司长沙分公司","上海华彩管理咨询有限公司第一分公司","北京和君咨询有限公司上海分公司"};
            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND allcontent:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
                                    listAll.add(data);
                                }
                            }
                        }
                    }
                }));
            }
            for (Future future1 : futureList1) {
                try {
                    future1.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    executorService1.shutdown();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService1.shutdown();

            if (listAll.size() >0){
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll,list.size(),s,"北京和君咨询有限公司",date);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(16);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSave(content)));
                }
                for (Future future : futureList) {
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                executorService.shutdown();
            }
        }
        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    public static final String ZHONG_BIAO = "INSERT INTO han_zhongbiaoUnit (content_id,zhaobiaoUnit,zhongbiaoUnit)  VALUES (?,?,?)";
    public static final String ZHONG_BIAO_QC = "INSERT INTO han_zhongbiaoUnit_qc (content_id,zhaobiaoUnit,zhongbiaoUnit)  VALUES (?,?,?)";
    @Override
    public void getZhongBiaoUnit(Integer type, String date, String s) throws Exception{
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<String> keywords = LogUtils.readRule("keyWords");
        //招标单位
        for (String str : keywords) {
            futureList1.add(executorService1.submit(() -> {
                //String zhongbiaoUnit = "";
                Set<String> setList = new HashSet<>();
                List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyy:2020 AND progid:3 AND (segmentType:11 OR segmentType:12) AND zhaoBiaoUnit:\"" + str + "\"", "", 1);
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            if (StringUtils.isNotBlank(data.getZhongBiaoUnit())) {
                                bdJdbcTemplate.update(ZHONG_BIAO,data.getContentid(), str, data.getZhongBiaoUnit());
                                setList.add(data.getZhaoBiaoUnit());
                            }
                        }
                    }
                }
                //String join = StringUtils.join(setList, ",");
                //String replace = join.replace(",", ";");
                //bdJdbcTemplate.update(ZHONG_BIAO, str, zhongbiaoUnit);
                //bdJdbcTemplate.update(ZHONG_BIAO_QC,"", str, replace);
            }));
        }
        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();

     }
    public static final String INSERT_URL ="INSERT INTO han_wz_data (infoId,yUrl,name) VALUES (?,?,?)";
    @Override
    public void getUrl() {
        ExecutorService executorService1 = Executors.newFixedThreadPool(5);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        try {
            Map<String,String> map = new HashMap<>();
            //获取
            List<Map<String, Object>> maps = bdJdbcTemplate.queryForList("select name,url from han_wz");
            /*if (maps !=null && maps.size() >0){
                for (Map<String, Object> m : maps) {
                    map.put(m.get("url").toString(),m.get("name").toString());
                }
            }*/
            List<String> list = LogUtils.readRule("idsFile");//infoId集合
            if (list !=null && list.size() >0){
                for (String infoId : list) {
                    futureList1.add(executorService1.submit(() -> {
                        String url = "";//源头地址
                        String name = "";//网站名称

                        String fromUrl = QianlimaZTUtil.getFromUrl("http://datafetcher.intra.qianlima.com/dc/bidding/fromurl", infoId);
                        if (StringUtils.isNotBlank(fromUrl)) {
                            for (Map<String, Object> m : maps) {
                                String u = m.get("url").toString();
                                if (fromUrl.contains(u)) {
                                    name = m.get("name").toString();//企业名称
                                    url = fromUrl;
                                }
                            }
                        }
                        log.info("存库infoId:{}",infoId);
                        bdJdbcTemplate.update(INSERT_URL, infoId, url, name);
                    }));
                }
                for (Future future1 : futureList1) {
                    try {
                        future1.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        executorService1.shutdown();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                executorService1.shutdown();
            }

        } catch (IOException e) {
            log.info("id集合获取失败");
        }

    }

    @Override
    public void getZhongBiaoUnitZiDuan(Integer type, String date, String s) throws Exception {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        List<Future> futureList1 = new ArrayList<>();

        //关键词a
        List<String> keywords = LogUtils.readRule("keyWords");
        for (String str : keywords) {
            futureList1.add(executorService1.submit(() -> {
                //自提招标单位
                List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyy:2020 AND progid:3 AND (segmentType:11 OR segmentType:12) AND zhaoBiaoUnit:\"" + str + "\"", "", 1);
                log.info(str+"————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                data.setKeyword(str);
                                listAll.add(data);
                            }
                        }
                    }
                }
            }));
        }
        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();

        if (listAll.size() >0){
            list.addAll(currencyService.getNoticeMqList(listAll));
        }

        log.info("全部数据量：" + listAll.size());
        log.info("去重数据量：" + list.size());

        currencyService.soutKeywords(listAll,list.size(),s,"招标单位下标准字段",date);


        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(16);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSave(content)));
                }
                for (Future future : futureList) {
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                executorService.shutdown();
            }
        }
        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    @Override
    public void getZheJiangWenZhou(Integer type, String date, String s) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        List<Future> futureList1 = new ArrayList<>();

        //关键词a
        String[] keywords ={"EPC","工程总承包","设计施工","施工设计","设计与施工","施工与设计","设施及施工","施工及设计","设计和施工","施工和设计","施工一体化"};
        for (String str : keywords) {
            futureList1.add(executorService1.submit(() -> {
                //自提招标单位
                List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND newCity:\""+"温州市"+"\"+ AND title:\"" + str + "\"", "", 1);
                log.info(str+"————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                data.setKeyword(str);
                                listAll.add(data);
                            }
                        }
                    }
                }
            }));
        }
        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();

        if (listAll.size() >0){
            list.addAll(currencyService.getNoticeMqList(listAll));
        }

        log.info("全部数据量：" + listAll.size());
        log.info("去重数据量：" + list.size());

        currencyService.soutKeywords(listAll,list.size(),s,"温州设计集团-规则一",date);


        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(16);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSave(content)));
                }
                for (Future future : futureList) {
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                executorService.shutdown();
            }
        }
        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    @Override
    public void getZheJiangWenZhou2(Integer type, String date, String s) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        List<Future> futureList1 = new ArrayList<>();

        //关键词b
        String[] keywords ={"设计","施工图设","全过程工程咨询"};
        String[] blacks ={"施工设计","监理","勘察","勘探","设计施工","测绘","和施工","施工和","与施工","施工与","及施工","施工及","施工总承包","采购施工","施工一体化","广告设计","文化设计","软装设计","系统","项目验收","防护措施","印刷品","印刷采购","印刷项目","动漫设计","验收全过程","形象片","舞美设计","包装设计"};
        for (String str : keywords) {
            futureList1.add(executorService1.submit(() -> {
                //自提招标单位
                List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND newCity:\""+"温州市"+"\"+ AND title:\"" + str + "\"", "", 1);
                log.info(str+"————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            for (String black : blacks) {
                                if(StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)){
                                    flag = false;
                                    break;
                                }
                            }
                            if (flag) {
                                data.setKeyword(str);
                                listAll.add(data);
                            }
                        }
                    }
                }
            }));
        }
        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();

        if (listAll.size() >0){
            list.addAll(currencyService.getNoticeMqList(listAll));
        }

        log.info("全部数据量：" + listAll.size());
        log.info("去重数据量：" + list.size());

        currencyService.soutKeywords(listAll,list.size(),s,"温州设计集团-规则二",date);


        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(16);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSave(content)));
                }
                for (Future future : futureList) {
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                executorService.shutdown();
            }
        }
        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    @Override
    public void testNewPoc(Integer type, String date, String s) throws Exception{
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        List<Future> futureList1 = new ArrayList<>();

        //关键词b
        List<String> keywords = LogUtils.readRule("");
        for (String str : keywords) {
            futureList1.add(executorService1.submit(() -> {
                //自提招标单位
                List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("id:\"" + str + "\"", "", 1);
                log.info(str);
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                data.setKeyword(str);
                                listAll.add(data);
                            }
                        }
                    }
                }
            }));
        }
        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();

        if (listAll.size() >0){
            list.addAll(currencyService.getNoticeMqList(listAll));
        }

        log.info("全部数据量：" + listAll.size());
        log.info("去重数据量：" + list.size());

        currencyService.soutKeywords(listAll,list.size(),s,"浙商银行",date);


        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(16);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSaveNew(content)));
                }
                for (Future future : futureList) {
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                executorService.shutdown();
            }
        }
        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    @Override
    public void getZheJiangYingHang() throws Exception {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        List<Future> futureList1 = new ArrayList<>();

        //关键词b
        List<String> keywords = LogUtils.readRule("idsFile");
        for (String str : keywords) {
            futureList1.add(executorService1.submit(() -> {
                //自提招标单位
                List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("id:\"" + str + "\"", "", 1);
                log.info(str);
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                data.setKeyword(str);
                                listAll.add(data);
                            }
                        }
                    }
                }
            }));
        }
        for (Future future1 : futureList1) {
            try {
                future1.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                executorService1.shutdown();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService1.shutdown();

        if (listAll.size() >0){
            list.addAll(currencyService.getNoticeMqList(listAll));
        }

        log.info("全部数据量：" + listAll.size());
        log.info("去重数据量：" + list.size());

        //currencyService.soutKeywords(listAll,list.size(),s,"浙商银行",date);


        //如果参数为1,则进行存表
        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(16);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getZhongTaiDatasAndSaveNew(content)));
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        }
        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    private void getZhongTaiDatasAndSave(NoticeMQ noticeMQ) {
        boolean b = cusDataNewService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        //调用中台接口，全部自提
        //Map<String, Object> resultMap = cusDataNewService.getAllFieldsWithZiTi(noticeMQ, false);
        Map<String, Object> resultMap = pocDataFieldService.getFieldsWithZiTi(noticeMQ, String.valueOf(noticeMQ.getContentid()));
        if (resultMap != null) {
            //自行封装方法
            //saveIntoMysql(resultMap);
            pocDataFieldService.saveIntoMysql(resultMap,String.valueOf(resultMap.get("content_id")));
            log.info("进行入库操作，contentId:{}",resultMap.get("content_id").toString());
        }
    }

    /**
     * 最新poc 方法 -自提
     * @param noticeMQ
     */
    private void getZhongTaiDatasAndSaveNew(NoticeMQ noticeMQ) {
        boolean b = cusDataNewService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        //调用中台接口，全部自提
        Map<String, Object> resultMap = pocDataFieldService.getFieldsWithZiTi(noticeMQ, String.valueOf(noticeMQ.getContentid()));
        if (resultMap != null) {
            String content = cusDataNewService.getContent(noticeMQ);//获取正文字段
            Map<String,Object> map = new HashMap<>();
            map.put("contentid",noticeMQ.getContentid());
            map.put("title",resultMap.get("title"));
            map.put("zhaoBiaoUnit",resultMap.get("zhao_biao_unit"));
            map.put("content",content);
            map.put("ztProvince",resultMap.get("province"));
            map.put("ztCity",resultMap.get("city"));
            map.put("ztCountry",resultMap.get("country"));
            map.put("ztAreaid",resultMap.get("keyword_term"));
            String httpNewArea = cusDataNewService.getHttpNewArea(map);
            if (StringUtils.isNotBlank(httpNewArea)){
                JSONObject jsonObject = JSON.parseObject(httpNewArea);
                if ("0".equals(String.valueOf(jsonObject.get("code")))){
                    if (jsonObject.get("data") !=null){
                        JSONObject jsonObject1 = JSON.parseObject(String.valueOf(jsonObject.get("data")));
                        if (jsonObject1 !=null){
                            resultMap.put("allForThreeCode",jsonObject1.get("province"));//省
                            resultMap.put("allForFourCode",jsonObject1.get("city"));//市
                            resultMap.put("allForFiveCode",jsonObject1.get("country"));//县
                        }
                    }
                }
            }

            //自行封装方法
            pocDataFieldService.saveIntoMysql(resultMap,String.valueOf(resultMap.get("content_id")));
            log.info("进行入库操作，contentId:{}",resultMap.get("content_id").toString());
        }
    }


}