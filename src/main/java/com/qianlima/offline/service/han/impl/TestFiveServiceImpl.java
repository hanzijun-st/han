package com.qianlima.offline.service.han.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.service.PocDataFieldService;
import com.qianlima.offline.service.han.CusDataNewService;
import com.qianlima.offline.service.han.TestFiveService;
import com.qianlima.offline.util.ContentFiveSolr;
import com.qianlima.offline.util.LogUtils;
import com.qianlima.offline.util.OnlineContentSolr;
import com.qianlima.offline.util.QianlimaZTUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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

    @Autowired
    private ContentFiveSolr contentFiveSolr;


    /**
     * 入库sql
     */
    public static final String INSERT_ZT_FOUR = "INSERT INTO han_new_data (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time,is_electronic,code,isfile,keyword_term,keywords," +
            " infoTypeSegment,monitorUrl, pocDetailUrl,first,second,allForThreeCode,allForFourCode,allForFiveCode) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";


    /**
     * 入库操作语句
     *
     * @param map
     */
    public void saveIntoMysql(Map<String, Object> map) {
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
        bdJdbcTemplate.update(INSERT_ZT_FOUR, map.get("task_id"), map.get("keyword"), map.get("content_id"), map.get("title"),
                map.get("content"), map.get("province"), map.get("city"), map.get("country"), map.get("url"), map.get("baiLian_budget"),
                map.get("baiLian_amount_unit"), map.get("xmNumber"), map.get("bidding_type"), map.get("progid"), map.get("zhao_biao_unit"),
                map.get("relation_name"), map.get("relation_way"), map.get("agent_unit"), map.get("agent_relation_ame"),
                map.get("agent_relation_way"), map.get("zhong_biao_unit"), map.get("link_man"), map.get("link_phone"),
                map.get("registration_begin_time"), map.get("registration_end_time"), map.get("biding_acquire_time"),
                map.get("biding_end_time"), map.get("tender_begin_time"), map.get("tender_end_time"), map.get("update_time"),
                map.get("type"), map.get("bidder"), map.get("notice_types"), map.get("open_biding_time"), map.get("is_electronic"),
                map.get("code"), map.get("isfile"), map.get("keyword_term"), map.get("keywords"), map.get("infoTypeSegment"), map.get("monitorUrl"), map.get("pocDetailUrl"),
                map.get("first"), map.get("second"), map.get("allForThreeCode"), map.get("allForFourCode"), map.get("allForFiveCode")

        );
    }


    @Override
    public void test5() {
        log.info("五月份接口：{}", "没有调取任何接口,只是测试项目启动");
    }

    @Override
    public void getRenMingWeiSheng(Integer type, String date, String s) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据

        try {
            String[] aa = {"山东大学齐鲁医学院","山东第一医科大学","滨州医学院","济宁医学院","潍坊医学院","德州学院","山东中医药大学","齐鲁医药学院","青岛大学医学院","潍坊科技学院","山东协和学院","山东力明科技职业学院","山东英才学院","齐鲁理工学院","山东医学高等专科学校","山东中医药高等专科学校","菏泽医学专科学校","济南护理职业学院","泰山护理职业学院","潍坊护理职业学院","枣庄职业学院","枣庄科技职业学院","聊城职业技术学院","滨州职业学院","中国石油大学胜利学院","淄博职业学院","山东药品食品职业学院","菏泽家政学院","山东省济宁卫生学校","山东省烟台护士学校","山东省青岛第二卫生学校","日照市卫生学校","曲阜中医药学校","平阴县职业教育中心","山东省临沂卫生学校","山东煤炭卫生学校","山东省青岛卫生学校","山东省莱阳卫生学校","华中科技大学同济医学院","武汉大学医学部","湖北医药学院","湖北中医药大学","湖北科技学院医学院","湖北理工学院医学部","武汉科技大学医学院","江汉大学医学院","武汉轻工大学医学院","长江大学医学院","湖北民族大学医学部","三峡大学医学院","三峡大学科技学院医学院","湖北文理学院医学院","荆楚理工学院医学院","武汉城市学院医学院","湖北民族大学科技学院","武汉大学医学职业技术学院","咸宁职业技术学院医学院","鄂州职业大学医学院","黄冈职业技术学院","武汉民政职业学院","武汉设计工程学院","湖北职业技术学院","仙桃职业学院医学院","襄阳职业技术学院医学院","随州职业技术学院","湖北中医药高等专科学校","湖北三峡职业技术学院医学院","武汉第二卫生学校","武汉市黄陂区卫生学校","武汉市江夏区卫生学校","恩施州卫生学校","十堰市医药卫生学校","咸宁职业教育（集团）学校","湖北新产业技师学院","长江职业学院","荆州职业技术学院","天门职业学院","恩施职业技术学院","湖北省潜江市卫生学校","湖北省十堰市医学科技学校","武汉铁路职业技术学院","武汉助产学校","梅州市卫生学校","韶关学院医学院","佛山市南海区卫生职业技术学校","广东黄埔卫生职业技术学校","广东省连州卫生学校","广东省新兴中药学校","广州卫生职业技术学院","广东茂名健康职业学院","河源市卫生学校","惠州卫生职业技术学院","珠海市卫生学校","廉江市卫生职业技术学校","中山大学新华学院","肇庆医学高等专科学校","韶关学院医学院","汕头市卫生学校","清远市德圣健康职业技术学校","梅州市卫生职业技术学校","江门市新会区卫生成人中等专业学校","嘉应学院医学院","高州市卫生学校","揭阳市卫生学校","广东医科大学","广州中医药大学","广东省食品药品学校","黄埔卫生学校","广宁卫校","韶关学院医学院","江门中医职业学院","阳江卫生学校","湛江卫生学校","湛江中医学校","新会卫校","南方医科大学","广东药科大学","广州医科大学"};
            String[] bb = {"图书","书籍","借阅","文献","小说","绘本","读物","书刊","书本","史籍","书库","音像制品","文集","国学","购书","讲书","电子书","教材","课件","课本","大纲","教科书","用书","辅导书","辅导资料","参考书","教辅","编著","书目","真题","试题","音像","音象","教学资源","专刊","期刊"};
            String[] blacks = {"维修","搬迁","装修","扩建","监控","施工","安装","建设","改造","修缮","设备","家具","书架","数据库","印刷"};
            String[] pbc = {"图书馆","图书楼","图书信息大楼"};
            for (String a : aa) {
                for (String b : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        //自提招标单位-a    标题-b
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND (newProvince:\"" + "山东省" + "\" OR newProvince:\"" + "广东省" + "\" OR newProvince:\"" + "湖北省" + "\")  AND zhaoBiaoUnit:\"" + a + "\"  AND title:\"" + b + "\"", a + "&" + b, 2);
                        log.info(a.trim() + "&" + b + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                String title = data.getTitle();
                                if (StringUtils.isNotBlank(title)) {
                                    boolean flag = true;
                                    for (String black : blacks) {
                                        if (StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)) {
                                            flag = false;
                                            break;
                                        }
                                    }
                                    if (flag) {
                                        for (String p : pbc) {
                                            if (title.contains(p)) {
                                                title = title.replaceAll(p, "");
                                            }
                                        }

                                        for (String s1 : bb) {
                                            if (title.contains(s1)) {
                                                data.setKeyword(a + "&" + b);
                                                listAll.add(data);
                                                break;
                                            }
                                        }
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

            if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll, list.size(), s, "人民卫生出版社-院校-21年", date);

        } catch (Exception e) {
            e.getMessage();
        }
        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
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
            String[] aa = {"济南曙光图书有限公司","济南齐鲁文苑教育实业有限公司","山东杏林图书有限公司","山东新华书店集团有限公司","山东新华书店集团有限公司连锁分公司","山东新华书店集团有限公司济南分公司","青岛新华书店有限责任公司","湖北校苑科技传媒有限公司","湖北省新华书店（集团）有限公司","湖北诚信易科传媒有限公司","湖北三新文化传媒有限公司","武汉协同创新科技有限公司","湖北蓝色畅想图书发行有限公司","武汉北斗星书业有限公司","湖北楚地畅行文化传播有限公司","武汉鑫众邦书业有限公司","武汉朗朗文化传媒有限公司","华师书刊发行中心","华中科技大学图书代办站","广东校卫图书有限公司","广州市方凯科技发展有限公司","广东新华发行集团股份有限公司","广州科信文化发展有限公司","广东蓝色畅想图书发行有限公司"};
            String[] bb = {"图书","书籍","借阅","文献","小说","绘本","读物","书刊","书本","史籍","书库","音像制品","文集","国学","购书","讲书","电子书","教材","课件","课本","大纲","教科书","用书","辅导书","辅导资料","参考书","教辅","编著","书目","真题","试题","音像","音象","教学资源","专刊","期刊"};
            String[] blacks = {"维修","搬迁","装修","扩建","监控","施工","安装","建设","改造","修缮","设备","印刷"};
            String[] pbc = {"图书馆","图书楼","图书大楼","图书信息大楼"};
            for (String a : aa) {
                for (String b : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        //自提中标单位-a    标题-b
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND (newProvince:\"" + "山东省" + "\" OR newProvince:\"" + "广东省" + "\" OR newProvince:\"" + "湖北省" + "\")  AND zhongBiaoUnit:\"" + a + "\"  AND title:\"" + b + "\"", a + "&" + b, 2);
                        log.info(a.trim() + "&" + b + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                String title = data.getTitle();
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    for (String black : blacks) {
                                        if (StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)) {
                                            flag = false;
                                            break;
                                        }
                                    }
                                    if (flag) {
                                        for (String p : pbc) {
                                            if (title.contains(p)) {
                                                title = title.replaceAll(p, "");
                                            }
                                        }
                                        for (String s1 : bb) {
                                            if (title.contains(s1)) {
                                                data.setKeyword(a + "&" + b);
                                                listAll.add(data);
                                                break;
                                            }
                                        }
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

            if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll, list.size(), s, "人民卫生出版社-经销商-21年", date);

        } catch (Exception e) {
            e.getMessage();
        }
        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
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
            String[] aa = {"综合性公园景观设计", "综合交通和换乘枢纽工程设计", "智慧建筑设计", "岩土工程勘察", "铁路勘察", "铁路工程设计", "隧道设计", "水运工程设计", "水利设计", "市政设计", "市政勘察", "市政规划设计", "生态修复工程", "桥梁设计", "桥梁勘察", "快速路设计设计", "景观设计", "交通设计", "交通建筑设计", "建筑设计", "环境勘察", "海洋勘察", "公路设计", "工程地质勘察", "高铁和城际铁路工程设计", "高速公路工程设计", "高速公路改扩建工程设计", "地下空间和人防工程设计", "城市水体综合治理", "城市立城市生态道路设计交设计", "城市轨道设计", "滨水景观设计", "山区公路设计", "综合管廊"};
            String[] blacks = {"电力设计", "中央空调", "展览设计", "展板设计", "平面设计", "印刷设计", "供热设计", "通信系统", "版面", "宣传", "广告设计", "设备采购", "装修", "装饰", "招商", "测绘", "钢板采购", "电梯", "招租", "招商", "维保", "设备", "钢筋", "钢板", "井盖", "材料采购", "化验试剂", "保洁服务", "汽车维保", "一体机电脑", "智慧交通", "网路安全", "招聘", "信息技术", "IT环境", "消杀服务", "车维保", "广告", "垃圾应急转运", "文书整理", "监理", "结算设计", "通信设备", "光缆", "保健服务", "调查", "空气监测", "地质工程勘察院", "工程地质大队", "自动化设备", "便携式", "专用仪器", "复印纸", "招待所", "培训中心", "PPP项目", "绿化工程"};
            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    //List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 2]) AND allcontent:\"" + str + "\"", "", 1);//招标
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND allcontent:\"" + str + "\"", "", 1);//中标
                    log.info(str + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                for (String black : blacks) {
                                    if (StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)) {
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

            if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll, list.size(), s, "华设设计集团股份有限公司", date);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
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
            String[] aa = {"自然资源"};
            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND allcontent:\"" + str + "\"", "", 1);//招标
                    log.info(str + "————" + mqEntities.size());
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

            if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll, list.size(), s, "福建特力惠信息科技股份有限公司-招标", date);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
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
            String[] aa = {"自然资源"};
            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:3 OR progid:5) AND allcontent:\"" + str + "\"", "", 1);//中标
                    log.info(str + "————" + mqEntities.size());
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

            if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll, list.size(), s, "福建特力惠信息科技股份有限公司-中标", date);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
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
     *
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
            String[] aa = {"北大纵横管理咨询", "北京北大纵横管理咨询有限责任公司", "北京北大纵横管理咨询有限公司", "北大纵横", "北京纵横联合", "中大咨询", "中大管理咨询", "广东中大管理咨询集团", "中大创新咨询", "广东中大管理咨询集团股份有限公司", "正略钧策", "正略集团", "正略钧策企业管理", "正略钧策集团股份有限公司", "华彩咨询", "华彩管理", "上海华彩管理咨询有限公司", "华夏基石", "北京华夏基石企业管理咨询有限公司", "和君集团", "和君集团有限公司", "和君咨询", "和君咨询有限公司", "和君商学", "北京和君商学在线科技股份有限公司", "北京纵横联合投资有限公司", "广州市中大管理咨询有限公司", "北京正略钧策咨询股份有限公司", "北京和君咨询有限公司", "北京北大保得利投资顾问有限公司", "北京凯德欧亚咨询中心有限公司", "烟台三校科技园置业有限公司", "中大市场调研（深圳）有限公司", "广州黑岩股权投资管理有限公司", "北京中大创新咨询有限公司", "中大管理咨询（深圳）有限公司", "中大人才发展（深圳）有限公司", "广州市中大信息技术有限公司", "广西中大管理咨询有限公司", "深圳正略百川信息技术有限公司", "武汉正略百川企业管理咨询有限公司", "成都正略企业管理咨询有限公司", "广州正略钧策企业管理咨询有限公司", "天津正略钧策企业管理咨询有限公司", "天津正略百川企业管理咨询有限公司", "上海正略钧策企业管理咨询有限公司", "上海正略企业管理咨询有限公司", "上海华彩企业顾问有限公司", "北京中人基石文化发展有限公司", "嘉兴和君清基股权投资合伙企业", "中科建发通信科技有限公司", "天津和君企业管理咨询有限公司", "和君集团福建企业管理咨询有限公司", "北京华职基业教育科技有限公司", "和君集团安徽三度企业管理咨询有限公司", "和君集团云南企业管理咨询有限公司", "和君聚成无锡企业管理咨询有限公司", "北京和君新媒体信息技术有限公司", "新疆和君咨询有限公司", "广州和君商学教育科技有限公司", "宁波梅山保税港区三度斋股权投资合伙企业", "江西和君商学在线科技有限公司", "北京北大纵横管理咨询有限责任公司长沙办事处", "广东中大管理咨询集团股份有限公司南京分公司", "广东中大管理咨询集团股份有限公司成都分公司", "广东中大管理咨询集团股份有限公司长沙分公司", "上海华彩管理咨询有限公司第一分公司", "北京和君咨询有限公司上海分公司"};
            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND allcontent:\"" + str + "\"", "", 1);
                    log.info(str + "————" + mqEntities.size());
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

            if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll, list.size(), s, "北京和君咨询有限公司", date);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
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
    public void getZhongBiaoUnit(Integer type, String date, String s) throws Exception {
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
                                bdJdbcTemplate.update(ZHONG_BIAO, data.getContentid(), str, data.getZhongBiaoUnit());
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

    public static final String INSERT_URL = "INSERT INTO han_wz_data (infoId,yUrl,name) VALUES (?,?,?)";

    @Override
    public void getUrl() {
        ExecutorService executorService1 = Executors.newFixedThreadPool(5);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        try {
            Map<String, String> map = new HashMap<>();
            //获取
            List<Map<String, Object>> maps = bdJdbcTemplate.queryForList("select name,url from han_wz");
            /*if (maps !=null && maps.size() >0){
                for (Map<String, Object> m : maps) {
                    map.put(m.get("url").toString(),m.get("name").toString());
                }
            }*/
            List<String> list = LogUtils.readRule("idsFile");//infoId集合
            if (list != null && list.size() > 0) {
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
                        log.info("存库infoId:{}", infoId);
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
                log.info(str + "————" + mqEntities.size());
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

        if (listAll.size() > 0) {
            list.addAll(currencyService.getNoticeMqList(listAll));
        }

        log.info("全部数据量：" + listAll.size());
        log.info("去重数据量：" + list.size());

        currencyService.soutKeywords(listAll, list.size(), s, "招标单位下标准字段", date);


        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
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
        String[] keywords = {"EPC", "工程总承包", "设计施工", "施工设计", "设计与施工", "施工与设计", "设施及施工", "施工及设计", "设计和施工", "施工和设计", "施工一体化"};
        for (String str : keywords) {
            futureList1.add(executorService1.submit(() -> {
                //自提招标单位
                List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND newCity:\"" + "温州市" + "\"+ AND title:\"" + str + "\"", "", 1);
                log.info(str + "————" + mqEntities.size());
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

        if (listAll.size() > 0) {
            list.addAll(currencyService.getNoticeMqList(listAll));
        }

        log.info("全部数据量：" + listAll.size());
        log.info("去重数据量：" + list.size());

        currencyService.soutKeywords(listAll, list.size(), s, "温州设计集团-规则一", date);


        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
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
        String[] keywords = {"设计", "施工图设", "全过程工程咨询"};
        String[] blacks = {"施工设计", "监理", "勘察", "勘探", "设计施工", "测绘", "和施工", "施工和", "与施工", "施工与", "及施工", "施工及", "施工总承包", "采购施工", "施工一体化", "广告设计", "文化设计", "软装设计", "系统", "项目验收", "防护措施", "印刷品", "印刷采购", "印刷项目", "动漫设计", "验收全过程", "形象片", "舞美设计", "包装设计"};
        for (String str : keywords) {
            futureList1.add(executorService1.submit(() -> {
                //自提招标单位
                List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND newCity:\"" + "温州市" + "\"+ AND title:\"" + str + "\"", "", 1);
                log.info(str + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            for (String black : blacks) {
                                if (StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)) {
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

        if (listAll.size() > 0) {
            list.addAll(currencyService.getNoticeMqList(listAll));
        }

        log.info("全部数据量：" + listAll.size());
        log.info("去重数据量：" + list.size());

        currencyService.soutKeywords(listAll, list.size(), s, "温州设计集团-规则二", date);


        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
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
    public void testNewPoc(Integer type, String date, String s) throws Exception {
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

        if (listAll.size() > 0) {
            list.addAll(currencyService.getNoticeMqList(listAll));
        }

        log.info("全部数据量：" + listAll.size());
        log.info("去重数据量：" + list.size());

        currencyService.soutKeywords(listAll, list.size(), s, "浙商银行", date);


        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
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

        if (listAll.size() > 0) {
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

    @Override
    public void getRenMingWeiSheng2(Integer type, String date, String s) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据

        try {
            String[] aa = {"山东大学齐鲁医学院", "山东第一医科大学", "滨州医学院", "济宁医学院", "潍坊医学院", "德州学院", "山东中医药大学", "齐鲁医药学院", "青岛大学医学院", "潍坊科技学院", "山东协和学院", "山东力明科技职业学院", "山东英才学院", "齐鲁理工学院", "山东医学高等专科学校", "山东中医药高等专科学校", "菏泽医学专科学校", "济南护理职业学院", "泰山护理职业学院", "潍坊护理职业学院", "枣庄职业学院", "枣庄科技职业学院", "聊城职业技术学院", "滨州职业学院", "中国石油大学胜利学院", "淄博职业学院", "山东药品食品职业学院", "菏泽家政学院", "山东省济宁卫生学校", "山东省烟台护士学校", "山东省青岛第二卫生学校", "日照市卫生学校", "曲阜中医药学校", "平阴县职业教育中心", "山东省临沂卫生学校", "山东煤炭卫生学校", "山东省青岛卫生学校", "山东省莱阳卫生学校", "华中科技大学同济医学院", "武汉大学医学部", "湖北医药学院", "湖北中医药大学", "湖北科技学院医学院", "湖北理工学院医学部", "武汉科技大学医学院", "江汉大学医学院", "武汉轻工大学医学院", "长江大学医学院", "湖北民族大学医学部", "三峡大学医学院", "三峡大学科技学院医学院", "湖北文理学院医学院", "荆楚理工学院医学院", "武汉城市学院医学院", "湖北民族大学科技学院", "武汉大学医学职业技术学院", "咸宁职业技术学院医学院", "鄂州职业大学医学院", "黄冈职业技术学院", "武汉民政职业学院", "武汉设计工程学院", "湖北职业技术学院", "仙桃职业学院医学院", "襄阳职业技术学院医学院", "随州职业技术学院", "湖北中医药高等专科学校", "湖北三峡职业技术学院医学院", "武汉第二卫生学校", "武汉市黄陂区卫生学校", "武汉市江夏区卫生学校", "恩施州卫生学校", "十堰市医药卫生学校", "咸宁职业教育（集团）学校", "湖北新产业技师学院", "长江职业学院", "荆州职业技术学院", "天门职业学院", "恩施职业技术学院", "湖北省潜江市卫生学校", "湖北省十堰市医学科技学校", "武汉铁路职业技术学院", "武汉助产学校", "梅州市卫生学校", "韶关学院医学院", "佛山市南海区卫生职业技术学校", "广东黄埔卫生职业技术学校", "广东省连州卫生学校", "广东省新兴中药学校", "广州卫生职业技术学院", "广东茂名健康职业学院", "河源市卫生学校", "惠州卫生职业技术学院", "珠海市卫生学校", "廉江市卫生职业技术学校", "中山大学新华学院", "肇庆医学高等专科学校", "韶关学院医学院", "汕头市卫生学校", "清远市德圣健康职业技术学校", "梅州市卫生职业技术学校", "江门市新会区卫生成人中等专业学校", "嘉应学院医学院", "高州市卫生学校", "揭阳市卫生学校", "广东医科大学", "广州中医药大学", "广东省食品药品学校", "黄埔卫生学校", "广宁卫校", "韶关学院医学院", "江门中医职业学院", "阳江卫生学校", "湛江卫生学校", "湛江中医学校", "新会卫校", "南方医科大学", "广东药科大学", "广州医科大学"};
            String[] bb = {"图书", "书籍", "书店", "借阅", "文献", "小说", "绘本", "读物", "书刊", "书本", "史籍", "书库", "音像制品", "看书", "文集", "阅读", "教程", "国学", "购书", "讲书", "电子书", "教材", "课件", "课本", "大纲", "教科书", "用书", "辅导书", "辅导资料", "课程标准", "参考书", "教辅", "编著", "书目", "读书", "真题", "试题", "工具书", "音像", "音象", "音频", "影像", "视频", "图像", "教学资源", "课程制作", "专刊", "期刊"};
            String[] blacks = {"维修", "搬迁", "图书楼", "装修", "扩建", "监控", "施工", "安装", "建设", "改造", "修缮", "设备", "家具", "书架", "数据库"};
            for (String a : aa) {
                for (String b : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        //自提招标单位-a    标题-b
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND (newProvince:\"" + "山东省" + "\" OR newProvince:\"" + "广东省" + "\" OR newProvince:\"" + "湖北省" + "\")  AND zhaoBiaoUnit:\"" + a + "\"  AND title:\"" + b + "\"", a + "&" + b, 2);
                        log.info(a.trim() + "&" + b + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    for (String black : blacks) {
                                        if (StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)) {
                                            flag = false;
                                            break;
                                        }
                                    }
                                    if (flag) {
                                        data.setKeyword(a + "&" + b);
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

            if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll, list.size(), s, "人民卫生出版社-院校-第二回合", date);

        } catch (Exception e) {
            e.getMessage();
        }
        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
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
    public void getRenMingWeiShengJxs2(Integer type, String date, String s) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据

        try {
            String[] aa = {"济南曙光图书有限公司", "济南齐鲁文苑教育实业有限公司", "山东杏林图书有限公司", "山东新华书店集团有限公司", "山东新华书店集团有限公司连锁分公司", "山东新华书店集团有限公司济南分公司", "青岛新华书店有限责任公司", "湖北校苑科技传媒有限公司", "湖北省新华书店（集团）有限公司", "湖北诚信易科传媒有限公司", "湖北三新文化传媒有限公司", "武汉协同创新科技有限公司", "湖北蓝色畅想图书发行有限公司", "武汉北斗星书业有限公司", "湖北楚地畅行文化传播有限公司", "武汉鑫众邦书业有限公司", "武汉朗朗文化传媒有限公司", "华师书刊发行中心", "华中科技大学图书代办站", "广东校卫图书有限公司", "广州市方凯科技发展有限公司", "广东新华发行集团股份有限公司", "广州科信文化发展有限公司", "广东蓝色畅想图书发行有限公司"};
            String[] bb = {"图书", "书籍", "书店", "借阅", "文献", "小说", "绘本", "读物", "书刊", "书本", "史籍", "书库", "音像制品", "看书", "文集", "阅读", "教程", "国学", "购书", "讲书", "电子书", "教材", "课件", "课本", "大纲", "教科书", "用书", "辅导书", "辅导资料", "课程标准", "参考书", "教辅", "编著", "书目", "读书", "真题", "试题", "工具书", "音像", "音象", "音频", "影像", "视频", "图像", "教学资源", "课程制作", "专刊", "期刊"};
            String[] blacks = {"维修", "搬迁", "图书楼", "装修", "扩建", "监控", "施工", "安装", "建设", "改造", "修缮", "设备"};
            for (String a : aa) {
                for (String b : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        //自提中标单位-a    标题-b
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND (newProvince:\"" + "山东省" + "\" OR newProvince:\"" + "广东省" + "\" OR newProvince:\"" + "湖北省" + "\")  AND zhongBiaoUnit:\"" + a + "\"  AND title:\"" + b + "\"", a + "&" + b, 2);
                        log.info(a.trim() + "&" + b + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    for (String black : blacks) {
                                        if (StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)) {
                                            flag = false;
                                            break;
                                        }
                                    }
                                    if (flag) {
                                        data.setKeyword(a + "&" + b);
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

            if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll, list.size(), s, "人民卫生出版社-经销商-第二回合", date);

        } catch (Exception e) {
            e.getMessage();
        }
        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
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
    public void getGuangXiChanYe(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据

        try {
            String[] aa = {"十四个五年规划", "国民经济", "社会发展", "十四五", "产业发展", "经济发展"};
            String[] bb = {"八步区", "巴马瑶族自治县", "百色", "百色市", "北海", "北海市", "北流市", "宾阳县", "博白县", "苍梧县", "岑溪市", "城中区", "崇左", "崇左市", "大化瑶族自治县", "大新县", "德保县", "叠彩区", "东兰县", "东兴市", "都安瑶族自治县", "防城港", "防城港市", "防城区", "凤山县", "扶绥县", "福绵区", "富川瑶族自治县", "港北区", "港口区", "港南区", "恭城瑶族自治县", "灌阳县", "广西", "贵港", "贵港市", "桂林", "桂林市", "桂平市", "海城区", "合浦县", "合山市", "河池", "河池市", "贺州", "贺州市", "横县", "环江毛南族自治县", "江南区", "江州区", "金城江区", "金秀瑶族自治县", "靖西市", "来宾", "来宾市", "乐业县", "荔浦市", "良庆区", "临桂区", "灵川县", "灵山县", "凌云县", "柳北区", "柳城县", "柳江区", "柳南区", "柳州", "柳州市", "龙胜各族自治县", "龙圩区", "龙州县", "隆安县", "隆林各族自治县", "陆川县", "鹿寨县", "罗城仫佬族自治县", "马山县", "蒙山县", "那坡县", "南丹县", "南宁", "南宁市", "宁明县", "平桂区", "平果县", "平乐县", "平南县", "凭祥市", "浦北县", "七星区", "钦北区", "钦南区", "钦州", "钦州市", "青秀区", "全州县", "容县", "融安县", "融水苗族自治县", "三江侗族自治县", "上林县", "上思县", "覃塘区", "藤县", "天等县", "天峨县", "田东县", "田林县", "田阳县", "铁山港区", "万秀区", "梧州", "梧州市", "武鸣区", "武宣县", "西林县", "西乡塘区", "象山区", "象州县", "忻城县", "兴安县", "兴宾区", "兴宁区", "兴业县", "秀峰区", "雁山区", "阳朔县", "宜州区", "银海区", "邕宁区", "永福县", "右江区", "鱼峰区", "玉林", "玉林市", "玉州区", "长洲区", "昭平县", "钟山县", "资源县", "规划编制", "规划纲要"};
            String[] blacks = {"办公耗材", "技术咨询", "物品采购", "印刷服务", "印刷", "物料采购", "复印", "办公设备", "制品制作", "设备采购", "空调", "设计制作", "发展用地"};
            for (String a : aa) {
                for (String b : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        //自提中标单位-a    标题-b
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND title:\"" + a + "\"  AND title:\"" + b + "\"", a + "&" + b, 2);
                        log.info(a.trim() + "&" + b + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    for (String black : blacks) {
                                        if (StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)) {
                                            flag = false;
                                            break;
                                        }
                                    }
                                    if (flag) {
                                        data.setKeyword(a + "&" + b);
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

            if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll, list.size(), s, name, date);

        } catch (Exception e) {
            e.getMessage();
        }
        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
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
    public void getShangHaiQingHe(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据

        try {
            String[] aa = {"热脱附", "苏玛罐", "热解析", "吸附管", "手工监测", "挥发性有机物监测", "VOCs自动监测", "固定点监测", "在线监测设备", "监测车", "走航车", "臭氧监测", "非甲烷总烃", "光化学污染", "水质重金属", "空气重金属", "便携VOCs", "便携式挥发性有机物", "便携气相", "便携GC"};
            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:3  AND allcontent:\"" + str + "\"", "", 1);
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

            if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            currencyService.soutKeywords(listAll, list.size(), s, name, date);

        } catch (Exception e) {
            e.getMessage();
        }
        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
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
    public void getWangZhuTouZi(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        try {
            String[] aa = {"迁建项目", "提档工程", "新建项目", "重建工程", "建设工程", "空调采购", "暖通", "修缮改造", "建设项目", "扩建工程", "施工总承包", "EPC总承包", "内部装修", "改造工程", "新型墙材", "玻璃幕墙", "工程建材", "商硂", "建材询价", "热力工程", "建筑材料供货", "建筑材料供应", "瓷砖采购", "水泥采购", "门窗采购", "管道采购", "地板采购", "涂料采购", "住宅工程", "回迁安置工程", "回迁安置项目", "电梯加装", "电梯采购", "工程施工", "施工工程", "项目施工", "建筑施工", "施工一体化"};
            String[] bb = {"综合楼", "综合大楼", "自行车棚", "住宅", "中学", "招待所", "展示馆", "展览馆", "运动场", "院校", "园林", "雨污分流", "幼儿园", "游泳馆", "用房", "硬化", "饮用水", "音影院", "医院", "研究院", "学校", "信息园", "新院", "校区", "小学", "小区", "物流园", "污水", "温室", "卫生站", "卫生院", "围墙", "图书馆", "停车楼", "停车场", "体育馆", "隧道", "宿舍", "私宅", "水泥路", "水利", "水电", "水池", "水安全", "实验园", "实验楼", "实训楼", "商业", "商品房", "商城", "商场", "取水口", "桥梁", "配套用房", "配套楼", "农田", "门诊楼", "门诊", "绿化", "旅馆", "路面", "路灯", "楼房", "垃圾池", "垃圾", "科学院", "科技园", "剧院", "居住", "酒店", "景观", "教学楼", "家属楼", "加工房", "机场", "环境", "化验楼", "河道", "管线", "管网", "管道", "观景台", "购物中心", "公租房", "公园", "公寓", "公路", "工业园", "工房", "高中", "高速路", "附属楼", "孵化园", "电站", "地铁", "道路", "大学", "处理厂", "车站", "车库", "车间", "场所", "场馆", "厂房", "产业园", "仓库", "仓储", "博物馆", "宾馆", "别墅", "便利店", "保健院", "办公楼", "安置房"};
            String[] cc = {"装修", "修缮", "新建", "项目", "施工", "扩建", "建筑", "建设", "工程", "改造"};
            String[] blacks = {"化验试剂", "保洁服务", "汽车维保", "电脑", "智慧交通", "网路安全", "招聘", "信息技术", "IT环境", "消杀服务", "广告", "垃圾应急转运", "文书整理", "监理", "通信设备", "保健服务", "调查", "空气监测", "自动化设备", "便携式", "专用仪器", "复印纸", "招待所", "培训中心", "超声波清洗机", "葡萄糖", "氘灯", "液压车", "手套", "无线耳麦", "硒鼓", "道路监控系统", "打印耗材", "纸管袋", "中介服务", "液碱", "小锁", "松紧带", "大扫把", "询价采购公告", "口罩", "全彩LED", "楼顶发光字", "车位", "拍卖", "高铁沿线造林", "高速气浮系统", "跟踪审计", "模拟训练装置", "雨污水管网清疏修缮工程", "应急投加系统", "绿化养护", "软件采购", "配电工程", "文物影响评估", "旧址建筑修缮", "物业管理", "垃圾分类服务", "信息系统集成", "数据库服务器", "接线工程", "运营管理", "商业策划", "数字展示", "办公家具", "布展服务", "定制服务", "印刷服务", "消防检测", "氛围营造", "活动看台", "采购PCR", "街道服务", "系统集成", "CAD软件", "多联机空调", "花卉租赁", "耗材采购", "实录制作", "文化专项", "虚拟现实景", "消防救援器", "专项审计", "公共厕所采购", "防空发展规划", "工程浮桥", "法治阵地", "平台账号", "文化建设", "燃气报警器", "影剧院座椅", "展览馆提升改造", "走航监测系统", "语音室", "BIM技术", "火灾探测系统", "智能化", "弱电系统", "疏浚工程", "AR教学系统", "运营物资", "校园文化", "WIFI租赁", "智能交通", "管件采购", "大屏采购", "玩具采购", "会场服务", "委托运营", "Candence软件", "装配式厕所", "一体化公厕", "电热水器", "仿真软件", "数字电视播出系统", "网络升级", "高低床", "复核服务", "布展", "走班智能化管理", "智能展柜", "磁盘阵列扩容", "会议音视频系统", "电脑采购", "静态标识", "权籍管理子系统", "配套控制系统", "文旅项目", "臭气检测仪", "智能听证系统", "设备租赁", "管理系统", "分析系统", "信息化系统", "指挥调度子系统", "安防系统", "系统迁移", "配电系统", "监测系统", "喷泉", "LED大屏", "地质信息系统", "弱电", "视频监控", "硬盘", "技防校园", "播出系统", "综合业务", "集成系统", "资源演播制作", "自动控制系统", "监控智能", "智能化系统", "自动报警", "变电所增容", "供配电项目", "供电接入", "系统维护", "能耗统计", "碱蓬种子", "互动教室", "废水处理", "3D视频", "垃圾压缩系统", "大数据分析", "智慧旅游", "试验平台", "接入蒸汽", "智能档案", "安全管控平台", "服务器", "耗材", "打印机", "企业信用", "车辆采购", "消防隐患整治", "安保服务", "食堂合作服务", "技防小区", "技防", "观摩活动", "隐患整治", "电子交易平台", "智能系统实训室", "标识", "分析软件", "信息管理平台", "一件事办结", "考核系统", "信息化", "直播系统", "会议系统", "CAD系统", "信息系统", "污水处理系统", "煤炭输送系统", "监督信息系统", "作业管控系统", "特殊消防系统", "智慧就业系统", "智能管控系统", "智慧公寓系统", "应急信息系统", "觉识别系统", "油气回收系统", "医疗气体及配套系统", "疫苗存储系统", "微型电网", "智慧应急系统", "火灾报警系统", "指挥系统", "勤务信息系统", "显示控制系统", "车牌识别", "数字庭审", "实验室系统", "安全等级保护", "灭火系统", "监控系统", "LED显示屏", "技侦决策", "自动升降舞台", "渗滤液处理系统", "虚拟仿真教学系统", "出租", "经营权主体变更", "出租项目", "更换工程", "BIM实验室", "摄影企业库", "土地使用权挂牌出让", "配送采购", "租用服务", "外购件采购", "文化墙采购", "劳务分包", "烘烤器", "电视播控系统", "医用气体采购", "使用权挂牌", "多媒体音视频系统", "展陈服务", "软装", "太阳能热水器制造", "配餐项目", "食品肉蛋米面油", "幼儿照护", "BIM市场信息", "市场禁入名单", "叉车", "声测管", "水稳料", "机加工件", "焊接钢管", "盘条", "疫情防控采购", "专业书籍", "智能巡检机器人", "消防设施维护", "名录库", "工艺系统", "系统建设", "居配工程", "脱水器", "AUTOCAD", "复印机", "租赁服务", "厨余垃圾综合处理", "图文店", "自动化终端采购", "存储扩容", "锚链", "技术配合服务", "安全鉴定检测服务", "形象提升工程", "干燥机", "反应器", "划拨用地补办出让", "地价评估", "资质代理服务", "物资类", "发光字", "印刷", "农网工程", "装载机", "视觉系统", "展陈提升", "岸电设施系统", "软件开发", "外包服务", "utonomy平台", "股权", "文化用品", "自动化控制系统", "商业包装", "LOGO", "软件", "采购车辆", "重检测系统", "后勤智能保障平台", "验证系统", "视觉识别应用系统", "人物形象库", "光影系统", "外线供电工程", "收纳系统", "校园形象识别系统", "智能灌溉系统", "气体回收系统", "系统建模", "视觉形象", "路演活动", "打印纸", "数据勘察取证", "视觉样板间工程", "手机信令数据采购", "无人机", "挖机租赁", "书籍印制", "需求物项采购", "电气专业", "体系模型", "交通控制系统", "智能夹具", "包装策划", "web应用", "办公文具", "木方采购", "物资采购", "搬运劳务", "印制", "打印服务", "画册策划", "网络安全加固", "目视化系统"};
            String[] pbc = {"设计院", "咨询有限公司", "装饰设计公司", "工程管理公司", "暖通工程有限公司", "建筑材料供应商", "建筑材料有限公司"};

            for (String a : aa) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND title:\"" + a + "\"", "", 1);
                    log.info(a + "- - -" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                String title = data.getTitle();
                                boolean flag = true;
                                for (String black : blacks) {
                                    if (StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)) {
                                        flag = false;
                                        break;
                                    }
                                }
                                if (flag) {
                                    String pbtitle = "";
                                    boolean f = false;
                                    for (String pb : pbc) {
                                        if (title.contains(pb)) {
                                            pbtitle = title.replaceAll(pb, "");
                                            f = true;
                                            break;
                                        }
                                    }
                                    if (f) {
                                        for (String a1 : aa) {
                                            if (pbtitle.contains(a1)) {
                                                data.setKeyword(a1);
                                                listAll.add(data);
                                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                                    listMap.add(data);
                                                    dataMap.put(data.getContentid().toString(), "0");
                                                }
                                                break;
                                            }
                                        }
                                    } else {
                                        data.setKeyword(a);
                                        listAll.add(data);
                                        if (!dataMap.containsKey(data.getContentid().toString())) {
                                            listMap.add(data);
                                            dataMap.put(data.getContentid().toString(), "0");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }));
            }

            for (String b : bb) {
                for (String c : cc) {
                    futureList1.add(executorService1.submit(() -> {
                        //自提中标单位-a    标题-b
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND title:\"" + b + "\"  AND title:\"" + c + "\"", b + "&" + c, 2);
                        log.info(b.trim() + "&" + c + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    String title = data.getTitle();
                                    boolean flag = true;
                                    for (String black : blacks) {
                                        if (StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)) {
                                            flag = false;
                                            break;
                                        }
                                    }
                                    if (flag) {
                                        String pbtitle = "";
                                        boolean f = false;
                                        for (String pb : pbc) {
                                            if (title.contains(pb)) {
                                                pbtitle = title.replaceAll(pb, "");
                                                f = true;
                                                break;
                                            }
                                        }
                                        if (f) {
                                            out:
                                            for (String b1 : bb) {
                                                for (String c1 : cc) {
                                                    if (pbtitle.contains(b1) && pbtitle.contains(c1)) {
                                                        data.setKeyword(b1 + "&" + c1);
                                                        listAll.add(data);
                                                        if (!dataMap.containsKey(data.getContentid().toString())) {
                                                            listMap.add(data);
                                                            dataMap.put(data.getContentid().toString(), "0");
                                                        }
                                                        break out;
                                                    }
                                                }
                                            }
                                        } else {
                                            data.setKeyword(b + "&" + c);
                                            listAll.add(data);
                                            if (!dataMap.containsKey(data.getContentid().toString())) {
                                                listMap.add(data);
                                                dataMap.put(data.getContentid().toString(), "0");
                                            }
                                        }
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

            log.info("去重数据量Map：" + listMap.size());

            if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());
            System.out.println("去重后数据：-----" + list.size());
            //currencyService.soutKeywords(listAll,list.size(),s,name,date);
            log.info("去重数据量：" + list.size());


        } catch (Exception e) {
            e.getMessage();
        }
        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
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
    public void getZhongTieJian(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        try {
            //List<String> aa = LogUtils.readRule("keyWordsA");
            List<String> bb = LogUtils.readRule("keyWordsB");
            for (String str : bb) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND zhaoBiaoUnit:\"" + str + "\"", "", 1);
                    log.info(str + "- - -" + mqEntities.size());
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

            if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            if ("1".equals(s) || "2".equals(s)) {
                currencyService.soutKeywords(listAll, list.size(), s, name, date);
            }

        } catch (Exception e) {
            e.getMessage();
        }
        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
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
    public void getZhongTieJian2(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        try {
            //List<String> aa = LogUtils.readRule("keyWordsA");
            List<String> bb = LogUtils.readRule("keyWordsB");
            for (String str : bb) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND newZhongBiaoUnit:\"" + str + "\"", "", 1);
                    log.info(str + "- - -" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    data.setKeyword(str);
                                    listAll.add(data);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
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

            if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            if ("1".equals(s) || "2".equals(s)) {
                currencyService.soutKeywords(listAll, list.size(), s, name, date);
            }

        } catch (Exception e) {
            e.getMessage();
        }
        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
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
    public void getFangHuoQiang(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(10);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        try {
            String[] bb = {"防火墙"};
            for (String str : bb) {
                //自提招标单位
                List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "]   AND allcontent:\"" + str + "\"", "", 1);
                log.info(str + "- - -" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        futureList1.add(executorService1.submit(() -> {
                            Long contentid = data.getContentid();
                            getTongZhi(contentid.toString(), "2022");
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
            }

        } catch (Exception e) {
            e.getMessage();
        }

        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    @Override
    public void getFangHuoQiangToId(String date) {
        ExecutorService executorService = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList = new ArrayList<>();
        List<NoticeMQ> listAll = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();
        try {
            String[] bb = {"防火墙"};
            String[] blackA = {"光缆", "等级保护测评", "等保测试", "维修", "维保", "保养", "对接", "等保测评", "隔断工程", "起重机", "智能交通", "维护", "养护", "维养", "运行管理平台", "信用卡系统", "整修", "网关键", "回收", "废旧", "测评服务", "测评项目", "奖励计划", "通讯网关", "安防监控系统", "综合监管平台", "防雷安全监管平台", "等保复测", "赁费项目", "报警系统", "检修", "修理", "测评", "失败", "流标", "废标", "租用", "插件", "运维", "A4纸", "复印纸", "打印纸", "硒鼓", "办公桌", "租赁", "网关于", "施工", "监理", "评测", "复测", "网关口", "延保", "续保", "设备升级", "设备更新", "房屋安全", "等级保护测试", "交通安全", "测试服务", "天翼网关", "弓网关系", "安全产业园", "运输安全", "考勤安全", "整改", "防护升级", "防护更新", "数据脱敏脚本", "施工安全", "用电安全", "煤场安全", "电场安全", "实验室安全", "矿安全", "起重机安全", "火灾安全", "防火墙封堵", "铺设防火墙", "森林防火墙", "山洪灾害", "防撞", "防火墙升级", "山石防火墙", "硬件升级", "车辆安全", "汽车智能安全", "后勤安全", "消防安全", "巡护监测安全", "巡检安全", "人员安全", "档案安全", "危险源", "厂防火墙", "工业控制系统", "电力监控系统", "工业系统", "电力系统", "应急管理系统", "播出系统", "大修", "车辆智能", "装置加固", "保修服务", "热电网关", "语音网关", "工业网关", "防火墙改造", "报废", "搬迁服务", "亮化提升工程", "安全防护用品采购", "围栏拆装", "材料设备采购", "老旧小区改造", "边渠回填", "防火封堵", "围栏拆装", "装置升级", "升级改造", "固件升级", "设备提档", "设备改造", "设备升级", "升级服务", "改造服务"};
            for (String str : bb) {
                //自提招标单位
                List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "]  AND -title:\"" + str + "\" AND allcontent:\"" + str + "\"", "", 1);
                log.info(str + "- - -" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            for (String black : blackA) {
                                if (StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)) {
                                    flag = false;
                                    break;
                                }
                            }
                            if (flag) {
                                listAll.add(data);
                            }
                        }
                    }
                }
            }

            if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            //存库
            if (list != null && list.size() > 0) {
                for (NoticeMQ noticeMQ : list) {
                    futureList.add(executorService.submit(() -> {
                        log.info("入库操作-contentId{}", noticeMQ.getContentid());
                        bdJdbcTemplate.update("INSERT INTO han_data_contentid (content_id) VALUES (?)", noticeMQ.getContentid());
                    }));
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
        } catch (Exception e) {
            e.getMessage();
        }

        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    @Override
    public void getTianRongXin(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后的数据-map
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        try {
            String[] aa = {"网关", "VPN", "网络安全防护", "网络安全隔离", "安全管理系统", "网络安全管理", "上网行为", "网络安全服务", "入侵检测", "网络安全监控", "工控网络", "数据安全", "负载均衡", "超融合", "防火墙", "零信任", "全流量", "基线管理", "风险探知", "入侵防御", "桌面云系统", "日志分析服务", "防病毒系统", "数据脱敏", "舆情监测"};
            String[] bb = {"CAN总线检测工具", "安全虚拟手机平台", "脆弱性合规管理", "脆弱性扫描与管理", "高级威胁检测", "攻防演练平台", "互联网接入口检测器", "计算机终端保密检查", "僵尸网络木马和蠕虫监测与处置", "僵尸网络木马和蠕虫监测", "僵尸网络木马和蠕虫处置", "流量复制汇聚器", "潜听威胁发现", "日志收集与分析", "视频数据防护", "视频专网安全监测与分析", "手机管控系统", "数据防泄漏", "数据脱敏系统", "数据脱敏项目", "数据脱敏设备", "态势分析与安全运营", "网络流量分析", "网络准入控制", "网站安全监控", "网站监测与自动修复", "无人机反制", "物联网安全赋能", "协议转换交付", "移动APP扫描加固", "移动设备管理", "异常流量管理与抗拒绝服务", "云安全管理（安全资源池）", "云安全管理（等保一体机）", "安全资源池", "云安全管理", "等保一体机", "智慧无线管理", "智能内网威胁分析", "终端安全登录与文件保护", "威胁防御", "自适应安全防御", "漏洞检测服务", "基线核查服务", "弱口令检查服务", "新上线系统安全检测服务", "应急响应安全服务", "渗透测试服务", "APP安全评估服务", "等级保护咨询服务", "APP违法违规收集使用个人信息检测服务", "信息安全管理体系咨询服务", "互联网暴露面检测服务", "红蓝对抗服务", "重要时期安全保障服务", "大数据安全能力成熟度评估服务", "数据分类分级服务", "运营商安全服务介绍", "新技术新业务安全评估服务", "符合性评测服务", "数据安全评估服务", "电子银行系统安全评估服务", "科技风险指引安全评估服务", "新上线系统报备检测服务", "医疗行业远程安全服务", "舆情监测系统", "舆情监测项目", "舆情监测服务", "舆情监测分析服务", "舆情监测分析系统", "舆情监测分析项目"};
            String[] cc = {"网络", "网站", "信息化"};
            String[] dd = {"风险评估服务", "安全隔离", "能力提升培训", "安全数据交换", "应急演练服务", "系统安全防护", "安全防护系统", "系统等级保护", "安全等级保护", "安全等保", "等保安全", "安全管理平台", "安全防护平台", "安全日志审计", "安全运营服务"};
            String[] ee = {"防火墙"};
            String[] ff = {"上网行为管理"};
            String[] gg = {"安全巡检服务", "安全加固服务", "驻场安全服务", "金融行业安全服务", "海关专项安全服务", "烟草行业安全服务"};
            String[] hh = {"网络安全", "信息安全", "网络设备"};
            String[] blackA = {"光缆", "等级保护测评", "等保测试", "维修", "维保", "保养", "对接", "等保测评", "隔断工程", "起重机", "智能交通", "维护", "养护", "维养", "运行管理平台", "信用卡系统", "整修", "网关键", "回收", "废旧", "测评服务", "测评项目", "奖励计划", "通讯网关", "安防监控系统", "综合监管平台", "防雷安全监管平台", "等保复测", "赁费项目", "报警系统", "检修", "修理", "测评", "失败", "流标", "废标", "租用", "插件", "运维", "A4纸", "复印纸", "打印纸", "硒鼓", "办公桌", "租赁", "网关于", "施工", "监理", "评测", "复测", "网关口", "延保", "续保", "设备升级", "设备更新", "房屋安全", "等级保护测试", "交通安全", "测试服务", "天翼网关", "弓网关系", "安全产业园", "运输安全", "考勤安全", "整改", "防护升级", "防护更新", "数据脱敏脚本", "施工安全", "用电安全", "煤场安全", "电场安全", "实验室安全", "矿安全", "起重机安全", "火灾安全", "防火墙封堵", "铺设防火墙", "森林防火墙", "山洪灾害", "防撞", "防火墙升级", "山石防火墙", "硬件升级", "车辆安全防护", "汽车智能安全", "后勤安全", "消防安全", "巡护监测安全", "巡检安全", "人员安全", "档案安全", "危险源", "厂防火墙", "工业控制系统", "电力监控系统", "工业系统", "电力系统", "应急管理系统", "播出系统", "大修", "车辆智能", "装置加固", "保修服务", "热电网关", "语音网关", "工业网关", "防火墙改造", "报废"};
            String[] blackB = {"光缆", "等级保护测评", "等保测试", "维修", "维保", "保养", "对接", "等保测评", "隔断工程", "起重机", "智能交通", "维护", "养护", "维养", "运行管理平台", "信用卡系统", "整修", "网关键", "回收", "废旧", "测评服务", "测评项目", "奖励计划", "通讯网关", "安防监控系统", "综合监管平台", "防雷安全监管平台", "等保复测", "赁费项目", "报警系统", "检修", "修理", "测评", "失败", "流标", "废标", "租用", "插件", "运维", "A4纸", "复印纸", "打印纸", "硒鼓", "办公桌", "租赁", "网关于", "施工", "监理", "评测", "复测", "网关口", "延保", "续保", "设备升级", "设备更新", "房屋安全", "等级保护测试", "交通安全", "测试服务", "天翼网关", "弓网关系", "安全产业园", "运输安全", "考勤安全", "整改", "防护升级", "防护更新", "数据脱敏脚本", "施工安全", "用电安全", "煤场安全", "电场安全", "实验室安全", "矿安全", "起重机安全", "火灾安全", "防火墙封堵", "铺设防火墙", "森林防火墙", "山洪灾害", "防撞", "防火墙升级", "山石防火墙", "硬件升级", "车辆安全防护", "汽车智能安全", "后勤安全", "消防安全", "巡护监测安全", "巡检安全", "人员安全", "档案安全", "危险源", "厂防火墙", "工业控制系统", "电力监控系统", "工业系统", "电力系统", "应急管理系统", "播出系统", "大修", "车辆智能", "装置加固", "保修服务", "热电网关", "语音网关", "工业网关", "防火墙改造", "报废", "上网行为管理：支持", "支持上网行为管理", "支持用户上网行为管理"};


            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND title:\"" + str + "\"", "", 1);
                    log.info(str + "- - -" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                for (String black : blackA) {
                                    if (StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)) {
                                        flag = false;
                                        break;
                                    }
                                }
                                if (flag) {
                                    data.setKeyword(str);
                                    listAll.add(data);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        listMap.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
                                }
                            }
                        }
                    }
                }));
            }
            for (String b : bb) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND allcontent:\"" + b + "\"", "", 2);
                    log.info(b + "- - -" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                for (String black : blackA) {
                                    if (StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)) {
                                        flag = false;
                                        break;
                                    }
                                }
                                if (flag) {
                                    data.setKeyword(b);
                                    listAll.add(data);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        listMap.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
                                }
                            }
                        }
                    }
                }));
            }
            for (String f : ff) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND allcontent:\"" + f + "\"", "", 3);
                    log.info(f + "- - -" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                for (String black : blackB) {
                                    if (StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)) {
                                        flag = false;
                                        break;
                                    }
                                }
                                if (flag) {
                                    data.setKeyword(f);
                                    listAll.add(data);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        listMap.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
                                }
                            }
                        }
                    }
                }));
            }

            for (String c : cc) {
                for (String d : dd) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND title:\"" + c + "\"  AND title:\"" + d + "\"", c + "&" + d, 4);
                        log.info(c.trim() + "&" + d + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    for (String black : blackA) {
                                        if (StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)) {
                                            flag = false;
                                            break;
                                        }
                                    }
                                    if (flag) {
                                        data.setKeyword(c + "&" + d);
                                        listAll.add(data);
                                        if (!dataMap.containsKey(data.getContentid().toString())) {
                                            listMap.add(data);
                                            dataMap.put(data.getContentid().toString(), "0");
                                        }
                                    }
                                }
                            }
                        }
                    }));
                }
            }
            for (String g : gg) {
                for (String h : hh) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND allcontent:\"" + g + "\"  AND allcontent:\"" + h + "\"", g + "&" + h, 5);
                        log.info(g.trim() + "&" + h + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    for (String black : blackA) {
                                        if (StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)) {
                                            flag = false;
                                            break;
                                        }
                                    }
                                    if (flag) {
                                        data.setKeyword(g + "&" + h);
                                        listAll.add(data);
                                        if (!dataMap.containsKey(data.getContentid().toString())) {
                                            listMap.add(data);
                                            dataMap.put(data.getContentid().toString(), "0");
                                        }
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

            if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());
            log.info("去重数据量-Map：" + listMap.size());

            if ("1".equals(s) || "2".equals(s)) {
                currencyService.soutKeywords(listAll, list.size(), s, name, date);
            }

        } catch (Exception e) {
            e.getMessage();
        }
        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
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
    public void getDataById() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(16);
        List<Future> futureList = new ArrayList<>();
        List<String> list = LogUtils.readRule("idsFile");
        for (String id : list) {
            NoticeMQ noticeMQ = new NoticeMQ();
            noticeMQ.setContentid(Long.valueOf(id));
            futureList.add(executorService.submit(() -> getZhongTaiDatasAndSave(noticeMQ)));
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

    @Override
    public void getSuZhouHaiSeng(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        try {
            String[] keywords = {"无人机", "大疆", "飞行器", "无人植保机", "航拍设备", "多旋翼", "无人驾驶飞行器", "无人飞行器", "无人飞机"};
            String[] blacks = {"建模", "撒药", "无人机维修", "无人机生产项目"};
            for (String str : keywords) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:3 OR progid:5) AND newProvince:\"" + "江苏省" + "\"  AND title:\"" + str + "\"", "", 1);
                    log.info(str + "- - -" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                for (String black : blacks) {
                                    if (StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)) {
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

            if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            if ("1".equals(s) || "2".equals(s)) {
                currencyService.soutKeywords(listAll, list.size(), s, name, date);
            }

        } catch (Exception e) {
            e.getMessage();
        }
        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
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
    public void getZheJiangDaHua() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(16);
        List<Future> futureList = new ArrayList<>();
        List<String> list = LogUtils.readRule("idsFile");
        for (String id : list) {
            NoticeMQ noticeMQ = new NoticeMQ();
            noticeMQ.setContentid(Long.valueOf(id));
            futureList.add(executorService.submit(() -> getZhongTaiDatasAndSave(noticeMQ)));
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

    @Override
    public void getTongJiZhaoBiao() throws Exception {
        Map<String, Long> zhaoBiaoUnit_complete = contentFiveSolr.companyResultsZhaoBiao("yyyymmdd:[20210501 TO 20210531]", "zhaoBiaoUnit_complete");//招标单位
    }

    @Override
    public void getBiGuiYuan(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重数据
        HashMap<String, String> dataMap = new HashMap<>();

        try {
            String[] aa = {"英语培训", "英文培训", "英语在线培训", "英文在线培训", "培训英语", "培训英文", "英语口语培训", "英文口语培训", "英语辅导", "英文辅导", "英语课程培训", "英文课程培训", "英语课程辅导", "英文课程辅导", "英语听力培训", "英语听力辅导", "英语口语辅导"};
            String[] pbc = {"培训中心", "培训机构", "培训学校", "培训班", "辅导班", "辅导中心", "辅导学校", "辅导机构"};
            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND title:\"" + str + "\"", "", 1);
                    log.info(str + "- - -" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                String title = data.getTitle();
                                boolean flag = false;
                                String pbtitle = "";
                                for (String pb : pbc) {
                                    if (title.contains(pb)) {
                                        pbtitle = title.replaceAll(pb, "");
                                        flag = true;
                                        break;
                                    }
                                }
                                if (flag) {
                                    for (String s1 : aa) {
                                        if (pbtitle.contains(s1)) {
                                            data.setKeyword(s1);
                                            listAll.add(data);

                                            if (!dataMap.containsKey(data.getContentid().toString())) {
                                                listMap.add(data);
                                                dataMap.put(data.getContentid().toString(), "0");
                                            }
                                            break;
                                        }
                                    }
                                } else {
                                    data.setKeyword(str);
                                    listAll.add(data);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        listMap.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
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

            if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());
            log.info("去重数据量map：" + listMap.size());

            currencyService.soutKeywords(listAll, list.size(), s, name, date);

        } catch (Exception e) {
            e.getMessage();
        }
        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
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
    public void getTongJiZhongBiao() throws Exception {
        Map<String, Long> zhaoBiaoUnit_complete = contentFiveSolr.companyResultsBaoXian("yyyymmdd:[20210501 TO 20210531]", "zhongBiaoUnit_complete");//中标单位
    }

    private void getZhongTaiDatasAndSave(NoticeMQ noticeMQ) {
        boolean b = cusDataNewService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        //调用中台接口，全部自提
        Map<String, Object> resultMap = pocDataFieldService.getFieldsWithZiTi(noticeMQ, String.valueOf(noticeMQ.getContentid()));
        //Map<String, Object> resultMap = pocDataFieldService.getFieldsWithHunHe(noticeMQ, String.valueOf(noticeMQ.getContentid()));//混合百炼
        if (resultMap != null) {
            //自行封装方法
            //saveIntoMysql(resultMap);
            pocDataFieldService.saveIntoMysql(resultMap, String.valueOf(resultMap.get("content_id")));
            log.info("进行入库操作，contentId:{}", resultMap.get("content_id").toString());
        }
    }

    /**
     * 最新poc 方法 -自提
     *
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
           /* String content = cusDataNewService.getContent(noticeMQ);//获取正文字段
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
            }*/

            //自行封装方法
            pocDataFieldService.saveIntoMysql(resultMap, String.valueOf(resultMap.get("content_id")));
            log.info("进行入库操作，contentId:{}", resultMap.get("content_id").toString());
        }
    }

    private void getTongZhi(String contentid, String version) {
        //先通知
        version = "2022";
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(60000)
                    .setSocketTimeout(60000).setConnectTimeout(60000).build();
            //创建HttpGet请求
            HttpGet httpGet = new HttpGet("http://cusdata.qianlima.com/zt/api/target/notify?contentid=" + contentid + "&version=" + version);
            httpGet.setConfig(requestConfig);
            CloseableHttpResponse response = client.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                if (StringUtils.isNotBlank(result)) {
                    JSONObject jsonObject = JSON.parseObject(result);
                    if ("0".equals(String.valueOf(jsonObject.get("code")))) {
                        log.info("contentid:{} 手动调用标的物通知接口成功,version为:{}", contentid, version);
                    } else {
                        log.info("contentid:{} 手动调用标的物通知接口失败,状态为：{},version为:{}", contentid, jsonObject.get("code"), version);
                    }
                }
            } else {
                log.info("contentid:{} 调用数据详情接口异常, 返回状态不是 200 ,version为:{}", contentid, version);
                throw new RuntimeException("调用数据详情接口异常， 返回状态不是 200 ");
            }
        } catch (Exception e) {
            log.error("调用数据详情接口异常:{}, 获取不到详情数据", e);
        }
    }

}