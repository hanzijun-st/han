package com.qianlima.offline.service.han.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.offline.bean.NoticeAllField;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.service.NiZaiJianService;
import com.qianlima.offline.service.PocDataFieldService;
import com.qianlima.offline.service.ShenPiService;
import com.qianlima.offline.service.han.CurrencyService;
import com.qianlima.offline.service.han.CusDataNewService;
import com.qianlima.offline.service.han.TestSixService;
import com.qianlima.offline.util.*;
import io.swagger.models.auth.In;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TestSixServiceImpl implements TestSixService {

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private OnlineContentSolr onlineContentSolr;

    @Autowired
    private OnlineSnContentSolr snContentSolr;//审批、拟在建

    @Autowired
    private CusDataNewService cusDataNewService;

    @Autowired
    private PocDataFieldService pocDataFieldService;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    private ShenPiService shenPiService;

    @Autowired
    private NiZaiJianService niZaiJianService;


    @Override
    public void getTianRongXin(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后的数据-map
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        try {
            String[] aa = {"网关", "VPN", "网络安全防护", "网络安全隔离", "网络安全管理", "上网行为", "网络安全服务", "入侵检测", "网络安全监控", "工控网络", "数据安全", "负载均衡", "超融合", "防火墙", "零信任", "全流量", "基线管理", "风险探知", "入侵防御", "桌面云系统", "日志分析服务", "防病毒系统", "数据脱敏", "舆情监测"};
            String[] bb = {"CAN总线检测工具", "安全虚拟手机平台", "脆弱性合规管理", "脆弱性扫描与管理", "高级威胁检测", "攻防演练平台", "互联网接入口检测器", "计算机终端保密检查", "僵尸网络木马和蠕虫监测与处置", "僵尸网络木马和蠕虫监测", "僵尸网络木马和蠕虫处置", "流量复制汇聚器", "潜听威胁发现", "日志收集与分析", "视频数据防护", "视频专网安全监测与分析", "手机管控系统", "数据防泄漏", "数据脱敏系统", "数据脱敏项目", "数据脱敏设备", "态势分析与安全运营", "网络流量分析", "网络准入控制", "网站安全监控", "网站监测与自动修复", "无人机反制", "物联网安全赋能", "协议转换交付", "移动APP扫描加固", "移动设备管理", "异常流量管理与抗拒绝服务", "云安全管理（安全资源池）", "云安全管理（等保一体机）", "安全资源池", "云安全管理", "等保一体机", "智慧无线管理", "智能内网威胁分析", "终端安全登录与文件保护", "自适应安全防御", "漏洞检测服务", "基线核查服务", "弱口令检查服务", "新上线系统安全检测服务", "应急响应安全服务", "渗透测试服务", "APP安全评估服务", "等级保护咨询服务", "APP违法违规收集使用个人信息检测服务", "信息安全管理体系咨询服务", "互联网暴露面检测服务", "红蓝对抗服务", "重要时期安全保障服务", "大数据安全能力成熟度评估服务", "数据分类分级服务", "运营商安全服务介绍", "新技术新业务安全评估服务", "符合性评测服务", "数据安全评估服务", "电子银行系统安全评估服务", "科技风险指引安全评估服务", "新上线系统报备检测服务", "医疗行业远程安全服务", "舆情监测系统", "舆情监测项目", "舆情监测服务", "舆情监测分析服务", "舆情监测分析系统", "舆情监测分析项目", "威胁防御"};
            String[] cc = {"网络", "网站", "信息化"};
            String[] dd = {"风险评估服务", "安全隔离", "能力提升培训", "安全数据交换", "应急演练服务", "系统安全防护", "安全防护系统", "系统等级保护", "安全等级保护", "安全等保", "等保安全", "安全管理平台", "安全防护平台", "安全日志审计", "安全运营服务", "安全管理系统"};
            String[] ee = {"防火墙"};
            String[] ff = {"上网行为管理"};
            String[] gg = {"安全巡检服务", "安全加固服务", "驻场安全服务", "金融行业安全服务", "海关专项安全服务", "烟草行业安全服务"};
            String[] hh = {"网络安全", "信息安全", "网络设备"};
            String[] blackA = {"光缆", "等级保护测评", "等保测试", "维修", "维保", "保养", "对接", "等保测评", "隔断工程", "起重机", "智能交通", "维护", "养护", "维养", "运行管理平台", "信用卡系统", "整修", "网关键", "回收", "废旧", "测评服务", "测评项目", "奖励计划", "通讯网关", "安防监控系统", "综合监管平台", "防雷安全监管平台", "等保复测", "赁费项目", "报警系统", "检修", "修理", "测评", "失败", "流标", "废标", "租用", "插件", "运维", "A4纸", "复印纸", "打印纸", "硒鼓", "办公桌", "租赁", "网关于", "施工", "监理", "评测", "复测", "网关口", "延保", "续保", "设备升级", "设备更新", "房屋安全", "等级保护测试", "交通安全", "测试服务", "天翼网关", "弓网关系", "安全产业园", "运输安全", "考勤安全", "整改", "防护升级", "防护更新", "数据脱敏脚本", "施工安全", "用电安全", "煤场安全", "电场安全", "实验室安全", "矿安全", "起重机安全", "火灾安全", "防火墙封堵", "铺设防火墙", "森林防火墙", "山洪灾害", "防撞", "防火墙升级", "山石防火墙", "硬件升级", "车辆安全", "汽车智能安全", "后勤安全", "消防安全", "巡护监测安全", "巡检安全", "人员安全", "档案安全", "危险源", "厂防火墙", "工业控制系统", "电力监控系统", "工业系统", "电力系统", "应急管理系统", "播出系统", "大修", "车辆智能", "装置加固", "保修服务", "热电网关", "语音网关", "工业网关", "防火墙改造", "报废", "搬迁服务", "亮化提升工程", "安全防护用品采购", "围栏拆装", "材料设备采购", "老旧小区改造", "边渠回填", "防火封堵", "围栏拆装", "装置升级", "升级改造", "固件升级", "设备提档", "设备改造", "设备升级", "升级服务", "改造服务"};
            String[] blackB = {"光缆", "等级保护测评", "等保测试", "维修", "维保", "保养", "对接", "等保测评", "隔断工程", "起重机", "智能交通", "维护", "养护", "维养", "运行管理平台", "信用卡系统", "整修", "网关键", "回收", "废旧", "测评服务", "测评项目", "奖励计划", "通讯网关", "安防监控系统", "综合监管平台", "防雷安全监管平台", "等保复测", "赁费项目", "报警系统", "检修", "修理", "测评", "失败", "流标", "废标", "租用", "插件", "运维", "A4纸", "复印纸", "打印纸", "硒鼓", "办公桌", "租赁", "网关于", "施工", "监理", "评测", "复测", "网关口", "延保", "续保", "设备升级", "设备更新", "房屋安全", "等级保护测试", "交通安全", "测试服务", "天翼网关", "弓网关系", "安全产业园", "运输安全", "考勤安全", "整改", "防护升级", "防护更新", "数据脱敏脚本", "施工安全", "用电安全", "煤场安全", "电场安全", "实验室安全", "矿安全", "起重机安全", "火灾安全", "防火墙封堵", "铺设防火墙", "森林防火墙", "山洪灾害", "防撞", "防火墙升级", "山石防火墙", "硬件升级", "车辆安全", "汽车智能安全", "后勤安全", "消防安全", "巡护监测安全", "巡检安全", "人员安全", "档案安全", "危险源", "厂防火墙", "工业控制系统", "电力监控系统", "工业系统", "电力系统", "应急管理系统", "播出系统", "大修", "车辆智能", "装置加固", "保修服务", "热电网关", "语音网关", "工业网关", "防火墙改造", "报废", "搬迁服务", "亮化提升工程", "安全防护用品采购", "围栏拆装", "材料设备采购", "老旧小区改造", "边渠回填", "防火封堵", "围栏拆装", "装置升级", "升级改造", "固件升级", "设备提档", "设备改造", "设备升级", "升级服务", "改造服务", "上网行为管理：支持", "支持上网行为管理", "支持用户上网行为管理"};


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
    public void getShiYouTianRanQi(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后的数据-map
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        try {
            String[] aa = {"汽油", "柴油", "成品油", "车用油", "车燃油", "车油料", "车辆购置燃油", "车辆燃油", "车辆油料", "船艇油料", "公车燃油", "船舶燃油", "船舶用油", "艇用油", "艇加油", "油卡", "加油预付卡", "加油卡", "车加油", "车辆加油", "用车加油", "车辆购油", "定点加油", "公车购油", "公车加油", "小车加油", "公务车油", "车辆加油服务", "加油定点"};
            String[] blacks = {"柴油发电机", "柴油机", "非法经营", "运输服务", "系统升级", "柴油车", "柴油一体", "柴油货车", "柴油铲运机", "柴油加氢", "运输", "成品油仓储区", "汽油发电机", "柴油驱动", "汽油车", "设备采购", "柴油家用发电机", "柴油罐", "柴油叉车", "柴油尾气", "汽油大双皮卡", "汽油罐", "柴油货车", "汽油加氢", "抗静电剂", "物流服务", "剂采购", "汽油破碎镐", "叉车", "汽油泵", "柴油泵", "柴油发动机", "柴油滤芯", "检测项目", "质量抽检", "质量检测", "改进剂", "抽检服务", "监督抽查", "手动", "两驱", "除草机", "隐患整改", "改造工程", "捣固机", "柴油发电", "汽油发电", "监测", "座", "储罐", "成品油管道", "柴油铲运机", "柴油粗滤器", "柴油应急发电机", "柴油电焊机", "配件采购", "柴油电动叉车", "设备采购", "框架采购", "管线改造", "油箱", "柴油降凝剂", "柴油成品罐", "柴油抽水机", "汽油面包车", "柴油硫含量", "柴油流量计", "柴油齿轮泵", "柴油齿轮泵", "柴油皮卡车", "柴油翻土机", "成品油翻坝", "成品油库", "皮卡"};

            String[] bb = {"油卡充值", "车辆加油服务", "定点加油", "车加油服务", "汽油采购", "柴油采购"};
            /*for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    //List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 2]  AND title:\"" + str + "\"", "", 1);//招标
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND title:\"" + str + "\"", "", 1);//中标
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
            }*/
            List<String> idList = new ArrayList<>();
            List<Map<String, Object>> maps = bdJdbcTemplate.queryForList("select content_id from han_data_poc_zgsy");
            for (Map<String, Object> map : maps) {
                idList.add(map.get("content_id").toString());
            }

            for (String str : bb) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND allcontent:\"" + str + "\"", "", 1);//中标
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
                                    Long contentid = data.getContentid();
                                    listMap.add(data);
                                    if (!idList.contains(String.valueOf(contentid))) {
                                        data.setKeyword(str);
                                        listAll.add(data);
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
            noticeMQ.setTaskId(6);//标的物
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
    public void getGsTongJi(String date, String type) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        try {
            List<String> qyNames = LogUtils.readRule("qyNames");
            for (String str : qyNames) {
                futureList1.add(executorService1.submit(() -> {
                    try {
                        Long aLong = onlineContentSolr.companyResultsCount("yyyy:" + date + " AND (progid:3 OR progid:5)  AND zhongBiaoUnit:\"" + str + "\"");
                        bdJdbcTemplate.update("INSERT INTO han_ls_tongji (name,num,type) VALUES (?,?,?)", str, aLong, type);
                    } catch (Exception e) {
                        e.getMessage();
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
        } catch (Exception e) {
            e.getMessage();
        }

        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    @Override
    public void getZhongXin() throws Exception {
        try {
            ExecutorService executorService1 = Executors.newFixedThreadPool(5);//开启线程池
            List<Future> futureList1 = new ArrayList<>();
            List<String> list = LogUtils.readRule("qyNames");
            for (String s : list) {
                futureList1.add(executorService1.submit(() -> {
                    JSONObject jsonObject = searchCreditCode(s);
                    if (jsonObject != null) {
                        String actualCapital = "";
                        String regStatus = "";
                        String regCapital = "";
                        String regInstitute = "";
                        String companyName = "";
                        String businessScope = "";
                        String industry = "";
                        String regLocation = "";
                        String regNumber = "";
                        String phoneNumber = "";
                        String creditCode = "";
                        String approvedTime = "";
                        String fromTime = "";
                        String companyOrgType = "";
                        String orgNumber = "";
                        String toTime = "";

                        if (StringUtils.isNotBlank(jsonObject.getString("actualCapital"))) {
                            actualCapital = jsonObject.getString("actualCapital");
                        }
                        if (StringUtils.isNotBlank(jsonObject.getString("regStatus"))) {
                            regStatus = jsonObject.getString("regStatus");
                        }
                        if (StringUtils.isNotBlank(jsonObject.getString("regCapital"))) {
                            regCapital = jsonObject.getString("regCapital");
                        }
                        if (StringUtils.isNotBlank(jsonObject.getString("regInstitute"))) {
                            regInstitute = jsonObject.getString("regInstitute");
                        }
                        if (StringUtils.isNotBlank(jsonObject.getString("companyName"))) {
                            companyName = jsonObject.getString("companyName");
                        }
                        if (StringUtils.isNotBlank(jsonObject.getString("businessScope"))) {
                            businessScope = jsonObject.getString("businessScope");
                        }
                        if (StringUtils.isNotBlank(jsonObject.getString("industry"))) {
                            industry = jsonObject.getString("industry");
                        }
                        if (StringUtils.isNotBlank(jsonObject.getString("regLocation"))) {
                            regLocation = jsonObject.getString("regLocation");
                        }
                        if (StringUtils.isNotBlank(jsonObject.getString("regNumber"))) {
                            regNumber = jsonObject.getString("regNumber");
                        }
                        if (StringUtils.isNotBlank(jsonObject.getString("phoneNumber"))) {
                            phoneNumber = jsonObject.getString("phoneNumber");
                        }
                        if (StringUtils.isNotBlank(jsonObject.getString("creditCode"))) {
                            creditCode = jsonObject.getString("creditCode");
                        }
                        if (StringUtils.isNotBlank(jsonObject.getString("approvedTime"))) {
                            approvedTime = jsonObject.getString("approvedTime");
                        }
                        if (StringUtils.isNotBlank(jsonObject.getString("fromTime"))) {
                            fromTime = jsonObject.getString("fromTime");
                        }
                        if (StringUtils.isNotBlank(jsonObject.getString("companyOrgType"))) {
                            companyOrgType = jsonObject.getString("companyOrgType");
                        }
                        if (StringUtils.isNotBlank(jsonObject.getString("orgNumber"))) {
                            orgNumber = jsonObject.getString("orgNumber");
                        }
                        if (StringUtils.isNotBlank(jsonObject.getString("toTime"))) {
                            toTime = jsonObject.getString("toTime");
                        }

                        bdJdbcTemplate.update("INSERT INTO han_zhongxin (name,actualCapital,regStatus,regCapital,regInstitute,companyName," +
                                        "businessScope,industry,regLocation,regNumber,phoneNumber,creditCode,approvedTime,fromTime,companyOrgType,orgNumber,toTime) " +
                                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", s, actualCapital, regStatus, regCapital, regInstitute, companyName,
                                businessScope, industry, regLocation, regNumber, phoneNumber, creditCode, approvedTime, fromTime, companyOrgType, orgNumber, toTime);
                        log.info("入库成功，name:{}", s);
                    }

                }));
            }


        } catch (Exception e) {
            e.getMessage();
            throw new Exception(e.getMessage());
        } finally {
            log.info("- - -中信企业接口运行结束- - - ");
        }
    }

    @Override
    public void getNingBoHongTai(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后的数据-map
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        try {
            String[] aa = {"膜结构", "钢结构"};
            String[] bb = {"遮阳棚", "雨棚", "停车棚", "张拉膜", "春秋棚", "冬暖棚", "钢结构工程", "大棚", "扣棚", "张拉膜", "钢结构建设", "膜结构工程", "膜结构建设", "密闭罩", "顶面工程", "停车场工程", "遮阳网", "生产棚", "钢结构改造工程", "拉伸膜", "景观棚", "钢结构建设"};
           /* for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 2]  AND allcontent :\"" + str + "\"", "", 1);
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
            }*/

            for (String c : aa) {
                for (String d : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 2]   AND allcontent:\"" + c + "\"  AND allcontent:\"" + d + "\"", c + "&" + d, 4);
                        log.info(c.trim() + "&" + d + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    if (flag) {
                                        data.setKeyword(c + "&" + d);
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
    public void getZheShangYingHang(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后的数据-map
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        try {
            String[] aa = {"创业担保贷款", "存放银行", "贷款银行", "理财产品", "案款专户", "保险开户项目", "保险支出帐户", "保修金专户", "保障金存放", "保证金户服务", "保证金监管银行", "保证金收支", "保证金托管", "保证金银行账户", "保证金帐户", "保证金账户", "保证金专户", "保证金专项资金承办", "保证金专用帐户", "财产归集账户", "财产托管账户", "财政拨款账户", "财政专户", "财政专户服务", "财政专户委托代理", "拆迁专户", "承办银行", "承销", "出让金账户开户", "储备中心账户", "创业贷款银行", "创业担保货款", "存储银行", "存管银行", "存款", "存款存放", "代发工资业务", "代发工资账户", "代发银行", "代管资金", "代理银行", "代收供热费", "贷款承办银行", "贷款合作银行", "贷款经办银行", "单位案款资金专户", "党费账户", "党费专户", "地方债存放", "电子支付银行", "定存", "定点存放", "定点银行", "定期存放", "定期存款服务", "定期账户开设", "对接银行", "房改资金专户", "放贷银行", "非税收入代收银行", "非税收入过渡账户", "非税收入汇缴账户", "非税收入网点代收服务", "非税收入银行", "非税账户开户", "封存账户合并账户资格", "服务银行", "工程专户账户", "工会经费账户", "工会经费专户", "工资存放", "工资代发", "工资统发", "公存储金", "公积金归集业务", "公积金受托", "公款", "公款存放", "公款竞争性存放", "公司债发行", "公司债募集资金监管账户", "公司账户竞争性开户", "归集账户资格", "国库集中支付代理", "国库集中支付定点代理", "国库集中支付委托代理", "国库集中支付业务", "国库现金管理", "合作银行", "惠农补贴代发", "活期账户", "基本存款账户", "基本户", "基本建设账户", "基本建设专户", "基本帐户", "基本账户", "基建户竞争性选择", "基建账户", "基建专户", "基金存放", "基金管理机构", "基金管理项目", "基金归集户", "基金收入户", "基金收入支出户", "基金受托管理人", "基金托管", "基金账户", "基金账户管理", "基金账户开户", "基金账户开设", "基金支出户", "基金支出户开户", "基金专户", "集体资金管理", "监管资金银行服务", "结构性存款", "结算户", "结算平台支出户", "结算账户", "金库寄库服务", "金融服务合作银行", "金暂存专户服务", "经办银行", "经费存放", "竞争性存放", "境外发债服务", "捐款专户", "开发专户", "开户行", "开户项目", "开户银行", "开立银行账户", "开设银行账户", "跨行存放", "跨行结算服务", "跨行结算项目", "跨行支付结算", "跨境汇款服务", "跨银行现金管理", "临时户开立", "临时账户", "零余额账户", "零余额账户服务", "零余额账户开立", "零余额账户开设", "年金归集账户", "年金基金归集账户", "票款存放", "社会保险补缴账户", "社会保障卡合作银行", "涉案款账户服务", "食堂账户", "收单银行", "收费代收银行", "收费银行项目", "收入账户开立", "收入专户开设", "受托银行", "托管银行", "维修资金银行服务", "委托扣缴", "现金存放", "现金管理", "虚拟账户服务", "养老保险开户", "一般账户", "医保收单银行服务", "银行存放", "银行代发", "银行代理", "银行服务", "银行服务机构", "银行服务网点", "银行服务项目", "银行合作", "银行基本帐户", "银行结算账户", "银行经营项目", "银行开户", "银行开户服务", "银行联名卡", "银行迁户业务", "银行授信", "银行托管", "银行选取服务", "银行帐户", "银行帐户开户服务", "银行账户", "银行账户服务", "银行账户竞争性磋商", "银行账户竞争性开户", "银行账户竞争性选择", "银行账户开立", "银行账户设立服务", "银行账户项目", "银行专户", "银企合作", "银企直联", "营收资金归集", "幼儿园账户开户", "战略合作银行", "账号关联银行", "账户存放", "账户服务", "账户集中开户", "账户竞争性选择", "账户开户服务", "账户开户项目", "账户开立", "账户开设", "账户资金服务", "支出户银行", "支出账户", "支付项目落地银行", "执行款专户", "专户存放", "专户管理", "专户开户", "专户开立项目", "专户项目", "专户银行", "专户账户", "专款账户", "专项基金存管", "专项基金账户", "专项维修资金银行", "专项债账户", "专项债专户", "专用存款", "专用银行卡", "专用帐户", "专用账户", "转贷银行", "资本金存放", "资产过户", "资金备用银行服务", "资金承办银行", "资金存储服务", "资金存贷", "资金存放", "资金存管服务", "资金存取账户", "资金代发", "资金第三方托管", "资金定期保值增值业务", "资金管理服务", "资金管理银行", "资金归集服务", "资金过渡专户", "资金集中账户", "资金监管银行", "资金交存", "资金结算账户", "资金竞争性存放", "资金托管服务", "资金托管银行", "资金再分配", "资金增值", "资金账户", "资金支出户", "资金专户", "资金专户代理", "资金专户管理", "账户", "专户", "合作金融机构", "保值增值", "保证金存放", "资金定期", "大额存单"};
            String[] blacks = {"保险公司入围", "黑灰产业", "税务筹划", "笔记本电脑", "PC电脑", "一体机", "劳务外包", "耗材", "大楼勘察", "存款送积分", "积分换豪礼", "带股权证", "电池", "招租", "活动礼品", "营销活动", "存单副柜", "积分有礼", "自动存款机", "自动上料机", "调节表审核", "竞买保证金账户变更", "会计事务", "会计师", "审计项目", "案审计", "会计服务", "会计代理", "会计业务", "审计机构", "审计采购", "审计及财务", "跟踪审计", "审计咨询", "造价审计", "废旧物资", "审计报告", "的审计", "机房设备", "器材装备", "制作安装", "宣传资料", "单元房", "贷款资料", "外包经营", "设备费用", "消防工程", "安防工程", "的股权", "ABN承销", "工程施工", "保证金退款通知", "成交一笔交易", "工作服", "工程设计施工", "公务用车", "台式计算机", "碎纸机", "多功能一体机", "处理器", "库存", "部分房产", "牌匾制作", "接口开发", "服务车采购", "物业服务采购", "人力外包服务", "水塘治理工程", "轴承", "软件运维服务费", "夏装采购", "机房硬件采购", "银行服务车", "设备尾款", "成交一笔订单", "%股权", "律师事务选聘", "公司信息表", "法律顾问服务", "公车加油", "网上超市采购", "边界防护类设备", "存款积分礼品", "路由器", "维保", "维修", "保养", "话费", "餐费", "复印纸", "打印机", "培训班", "开关柜", "台式机", "专用章", "除臭液", "车辆保险", "设备购置", "应用升级", "智能手机", "办公用品", "物资采购", "速递服务", "商城采购", "电话催收", "资料递送", "审计服务", "医疗设备", "官网改造工程", "工程造价咨询", "公共厕所", "场地监控系统工程", "Web应用", "办公室租赁", "车辆采购", "办公家具", "公开招租", "硒鼓", "装修项目", "设备维护", "服务器", "装修工程", "凭证传递", "废酵母", "废轴承", "劳务派遣", "宣传品", "法律", "律师", "律所", "系统", "软件", "硬件", "交换机", "复印机", "流程图", "通用设备", "会计鉴定", "会计代账", "谈话室建设", "审计费", "贷款贴息情况", "专用设备", "账页", "标书费用", "账户单号", "成都段扩容", "环境监测保护", "网络官方账户", "个人大额存单产品"};
            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND title:\"" + str + "\"", "", 1);
                    log.info(str + "- - -" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                String title = data.getTitle();
                                boolean flag = true;
                                for (String black : blacks) {
                                    if (StringUtils.isNotBlank(title) && title.contains(black)) {
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
            futureList1.add(executorService1.submit(() -> {
                List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND zhongFirstIndustry:\"" + "金融企业" + "\" AND (zhongSecondIndustry:\"" + "银行" + "\" OR zhongSecondIndustry:\"" + "合作社" + "\")", "", 2);
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
                                data.setKeyword("");
                                listAll.add(data);
                            }
                        }
                    }
                }
            }));

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
    public void getZheShangYingHangC(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后的数据-map
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        try {
            String[] aa = {"财政", "零余额", "非税", "国库", "代收", "代缴"};
            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND (newProvince:\"" + "北京市" + "\" OR newProvince:\"" + "上海市" + "\" OR newProvince:\"" + "浙江省" + "\" OR newCity:\"" + "深圳市" + "\" OR newCity:\"" + "南京市" + "\" OR newCity:\"" + "苏州市" + "\" OR newCity:\"" + "无锡市" + "\"   ) AND allcontent:\"" + str + "\"", "", 3);
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
    public void getSengDaMeiXin(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        List<NoticeMQ> listMap = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        try {
            String[] aa = {"居住", "住宅", "小区", "别墅", "医院", "酒店", "宾馆", "商场", "商业", "场馆", "音影院", "车站", "机场", "学校", "院校", "小学", "大学", "中学", "研究院", "科学院", "综合楼", "办公楼", "停车场", "安置房", "用房", "楼房", "厂房", "工房", "配套楼", "车库", "物流园", "工业园", "旅馆", "宿舍", "商品房", "公寓", "仓库", "实验楼", "化验楼", "私宅", "加工房", "车间", "剧院", "自行车棚", "水池", "温室", "仓储", "信息园", "产业园", "科技园", "购物中心", "商城", "配套用房", "教学楼", "公租房", "博物馆", "处理厂", "停车楼", "运动场", "招待所", "门诊", "综合大楼", "附属楼", "孵化园", "围墙", "实验园", "实训楼", "便利店", "门诊楼", "新院", "幼儿园", "高中", "卫生院", "保健院", "观景台", "展示馆", "垃圾池", "图书馆", "展览馆", "道路", "公路", "高速路", "水电", "路面", "硬化", "水泥路", "地铁", "桥梁", "隧道", "公园", "水利", "环境", "景观", "电站", "水安全", "农田", "饮用水", "污水", "管道", "管线", "管网", "河道", "取水口", "园林", "路灯", "雨污分流", "垃圾", "体育馆", "卫生站", "场所", "游泳馆", "家属楼", "供水", "热力", "市政", "供暖", "外墙", "地块", "城墙", "基础设施", "房屋", "危房", "棚户区", "安置点", "供热", "宅基地", "农贸市场", "菜市场", "服务中心", "安置区", "家园", "花园", "自来水", "排水", "水渠", "水库", "电厂", "给水", "村道", "围栏", "雨棚", "大道", "街道", "场地", "基地", "道路硬化", "大楼", "地基", "置换房", "公交站", "高铁站", "土建", "电力", "养护", "管沟", "交通枢纽", "实验室", "车棚", "钢结构", "管廊", "研究室", "航站楼", "住房", "土护降", "桥涵", "彩钢屋", "干燥室", "燃气", "食堂", "餐厅", "厂区", "一期", "二期", "三期", "高速", "环路", "天然气", "轨道交通", "地质灾害", "桩基", "搬迁楼", "专用路", "厂棚", "造林", "室外", "幕墙", "标段", "填埋场", "还建", "首末站"};
            String[] bb = {"施工", "工程", "建筑", "项目", "设计", "勘察", "改造", "扩建", "新建", "修缮", "建设", "改建", "监理"};
            String[] cc = {"EPC", "工程施工", "建筑施工", "项目施工", "设计施工", "勘察施工", "改造施工", "扩建施工", "新建施工", "修缮施工", "建设施工", "施工工程", "建筑工程", "项目工程", "设计工程", "勘察工程", "改造工程", "扩建工程", "新建工程", "修缮工程", "建设工程", "施工建筑", "工程建筑", "项目建筑", "设计建筑", "勘察建筑", "改造建筑", "扩建建筑", "新建建筑", "修缮建筑", "建设建筑", "施工项目", "工程项目", "建筑项目", "设计项目", "勘察项目", "改造项目", "扩建项目", "新建项目", "修缮项目", "建设项目", "施工设计", "工程设计", "建筑设计", "项目设计", "勘察设计", "改造设计", "扩建设计", "新建设计", "修缮设计", "建设设计", "施工勘察", "工程勘察", "建筑勘察", "项目勘察", "设计勘察", "改造勘察", "扩建勘察", "新建勘察", "修缮勘察", "建设勘察", "施工改造", "工程改造", "建筑改造", "项目改造", "设计改造", "勘察改造", "扩建改造", "新建改造", "修缮改造", "建设改造", "施工扩建", "工程扩建", "建筑扩建", "项目扩建", "设计扩建", "勘察扩建", "改造扩建", "新建扩建", "修缮扩建", "建设扩建", "施工新建", "工程新建", "建筑新建", "项目新建", "设计新建", "勘察新建", "改造新建", "扩建新建", "修缮新建", "建设新建", "施工修缮", "工程修缮", "建筑修缮", "项目修缮", "设计修缮", "勘察修缮", "改造修缮", "扩建修缮", "新建修缮", "建设修缮", "施工建设", "工程建设", "建筑建设", "项目建设", "设计建设", "勘察建设", "改造建设", "扩建建设", "新建建设", "修缮建设", "施工总承包", "工程总承包", "总承包工程", "改建施工", "改建工程", "改建建筑", "改建项目", "改建设计", "改建勘察", "改建新建", "改建建设", "施工改建", "工程改建", "建筑改建", "项目改建", "设计改建", "勘察改建", "大修施工", "大修工程", "大修建筑", "大修项目", "大修设计", "大修建设", "施工大修", "工程大修", "建筑大修", "项目大修", "修复施工", "修复工程", "修复建筑", "修复项目", "修复设计", "修复勘察", "修复改造", "修复扩建", "修复新建", "修复修缮", "修复建设", "施工修复", "工程修复", "建筑修复", "项目修复", "设计修复", "勘察修复", "改造修复", "扩建修复", "新建修复", "修缮修复", "建设修复", "监理施工", "监理工程", "监理建筑", "监理项目", "监理设计", "监理勘察", "监理改造", "监理扩建", "监理新建", "监理修缮", "监理建设", "施工监理", "工程监理", "建筑监理", "项目监理", "设计监理", "勘察监理", "改造监理", "扩建监理", "新建监理", "修缮监理", "建设监理", "修缮整治工程", "综合治理工程", "加固工程", "加固项目", "护坡工程", "中修工程", "综合治理项目", "项目总包", "工程总包", "防治工程", "施工图设计", "改造提升工程"};
            String[] dd = {"居住建筑", "住宅建筑", "小区建筑", "别墅建筑", "医院建筑", "酒店建筑", "宾馆建筑", "商场建筑", "商业建筑", "场馆建筑", "音影院建筑", "车站建筑", "机场建筑", "学校建筑", "院校建筑", "小学建筑", "大学建筑", "中学建筑", "研究院建筑", "科学院建筑", "综合楼建筑", "办公楼建筑", "停车场建筑", "安置房建筑", "用房建筑", "楼房建筑", "厂房建筑", "工房建筑", "配套楼建筑", "车库建筑", "物流园建筑", "工业园建筑", "旅馆建筑", "宿舍建筑", "商品房建筑", "公寓建筑", "仓库建筑", "实验楼建筑", "化验楼建筑", "私宅建筑", "加工房建筑", "车间建筑", "剧院建筑", "自行车棚建筑", "水池建筑", "温室建筑", "仓储建筑", "信息园建筑", "产业园建筑", "科技园建筑", "购物中心建筑", "商城建筑", "配套用房建筑", "教学楼建筑", "公租房建筑", "博物馆建筑", "处理厂建筑", "停车楼建筑", "运动场建筑", "招待所建筑", "门诊建筑", "综合大楼建筑", "附属楼建筑", "孵化园建筑", "围墙建筑", "实验园建筑", "实训楼建筑", "便利店建筑", "门诊楼建筑", "新院建筑", "幼儿园建筑", "高中建筑", "卫生院建筑", "保健院建筑", "观景台建筑", "展示馆建筑", "垃圾池建筑", "图书馆建筑", "展览馆建筑", "道路建筑", "公路建筑", "高速路建筑", "水电建筑", "路面建筑", "硬化建筑", "水泥路建筑", "地铁建筑", "桥梁建筑", "隧道建筑", "公园建筑", "水利建筑", "环境建筑", "景观建筑", "电站建筑", "水安全建筑", "农田建筑", "饮用水建筑", "污水建筑", "管道建筑", "管线建筑", "管网建筑", "河道建筑", "绿化建筑", "取水口建筑", "园林建筑", "路灯建筑", "雨污分流建筑", "垃圾建筑", "体育馆建筑", "卫生站建筑", "场所建筑", "游泳馆建筑", "家属楼建筑", "供水建筑", "热力建筑", "市政建筑", "供暖建筑", "外墙建筑", "地块建筑", "城墙建筑", "基础设施建筑", "房屋建筑", "危房建筑", "棚户区建筑", "安置点建筑", "供热建筑", "宅基地建筑", "农贸市场建筑", "菜市场建筑", "服务中心建筑", "安置区建筑", "家园建筑", "花园建筑", "自来水建筑", "排水建筑", "水渠建筑", "水库建筑", "电厂建筑", "给水建筑", "村道建筑", "围栏建筑", "雨棚建筑", "大道建筑", "街道建筑", "居住装修", "住宅装修", "小区装修", "别墅装修", "医院装修", "酒店装修", "宾馆装修", "商场装修", "商业装修", "场馆装修", "音影院装修", "车站装修", "机场装修", "学校装修", "院校装修", "小学装修", "大学装修", "中学装修", "研究院装修", "科学院装修", "综合楼装修", "办公楼装修", "停车场装修", "安置房装修", "用房装修", "楼房装修", "厂房装修", "工房装修", "配套楼装修", "车库装修", "物流园装修", "工业园装修", "旅馆装修", "宿舍装修", "商品房装修", "公寓装修", "仓库装修", "实验楼装修", "化验楼装修", "私宅装修", "加工房装修", "车间装修", "剧院装修", "自行车棚装修", "水池装修", "温室装修", "仓储装修", "信息园装修", "产业园装修", "科技园装修", "购物中心装修", "商城装修", "配套用房装修", "教学楼装修", "公租房装修", "博物馆装修", "处理厂装修", "停车楼装修", "运动场装修", "招待所装修", "门诊装修", "综合大楼装修", "附属楼装修", "孵化园装修", "围墙装修", "实验园装修", "实训楼装修", "便利店装修", "门诊楼装修", "新院装修", "幼儿园装修", "高中装修", "卫生院装修", "保健院装修", "观景台装修", "展示馆装修", "垃圾池装修", "图书馆装修", "展览馆装修", "道路装修", "公路装修", "高速路装修", "水电装修", "路面装修", "硬化装修", "水泥路装修", "地铁装修", "桥梁装修", "隧道装修", "公园装修", "水利装修", "环境装修", "景观装修", "电站装修", "水安全装修", "农田装修", "饮用水装修", "污水装修", "管道装修", "管线装修", "管网装修", "河道装修", "绿化装修", "取水口装修", "园林装修", "路灯装修", "雨污分流装修", "垃圾装修", "体育馆装修", "卫生站装修", "场所装修", "游泳馆装修", "家属楼装修", "供水装修", "热力装修", "市政装修", "供暖装修", "外墙装修", "地块装修", "城墙装修", "基础设施装修", "房屋装修", "危房装修", "棚户区装修", "安置点装修", "供热装修", "宅基地装修", "农贸市场装修", "菜市场装修", "服务中心装修", "安置区装修", "家园装修", "花园装修", "自来水装修", "排水装修", "水渠装修", "水库装修", "电厂装修", "给水装修", "村道装修", "围栏装修", "雨棚装修", "大道装修", "街道装修", "居住改造", "住宅改造", "小区改造", "别墅改造", "医院改造", "酒店改造", "宾馆改造", "商场改造", "商业改造", "场馆改造", "音影院改造", "车站改造", "机场改造", "学校改造", "院校改造", "小学改造", "大学改造", "中学改造", "研究院改造", "科学院改造", "综合楼改造", "办公楼改造", "停车场改造", "安置房改造", "用房改造", "楼房改造", "厂房改造", "工房改造", "配套楼改造", "车库改造", "物流园改造", "工业园改造", "旅馆改造", "宿舍改造", "商品房改造", "公寓改造", "仓库改造", "实验楼改造", "化验楼改造", "私宅改造", "加工房改造", "车间改造", "剧院改造", "自行车棚改造", "水池改造", "温室改造", "仓储改造", "信息园改造", "产业园改造", "科技园改造", "购物中心改造", "商城改造", "配套用房改造", "教学楼改造", "公租房改造", "博物馆改造", "处理厂改造", "停车楼改造", "运动场改造", "招待所改造", "门诊改造", "综合大楼改造", "附属楼改造", "孵化园改造", "围墙改造", "实验园改造", "实训楼改造", "便利店改造", "门诊楼改造", "新院改造", "幼儿园改造", "高中改造", "卫生院改造", "保健院改造", "观景台改造", "展示馆改造", "垃圾池改造", "图书馆改造", "展览馆改造", "道路改造", "公路改造", "高速路改造", "水电改造", "路面改造", "硬化改造", "水泥路改造", "地铁改造", "桥梁改造", "隧道改造", "公园改造", "水利改造", "环境改造", "景观改造", "电站改造", "水安全改造", "农田改造", "饮用水改造", "污水改造", "管道改造", "管线改造", "管网改造", "河道改造", "绿化改造", "取水口改造", "园林改造", "路灯改造", "雨污分流改造", "垃圾改造", "体育馆改造", "卫生站改造", "场所改造", "游泳馆改造", "家属楼改造", "供水改造", "热力改造", "市政改造", "供暖改造", "外墙改造", "地块改造", "城墙改造", "基础设施改造", "房屋改造", "危房改造", "棚户区改造", "安置点改造", "供热改造", "宅基地改造", "农贸市场改造", "菜市场改造", "服务中心改造", "安置区改造", "家园改造", "花园改造", "自来水改造", "排水改造", "水渠改造", "水库改造", "电厂改造", "给水改造", "村道改造", "围栏改造", "雨棚改造", "大道改造", "街道改造", "居住扩建", "住宅扩建", "小区扩建", "别墅扩建", "医院扩建", "酒店扩建", "宾馆扩建", "商场扩建", "商业扩建", "场馆扩建", "音影院扩建", "车站扩建", "机场扩建", "学校扩建", "院校扩建", "小学扩建", "大学扩建", "中学扩建", "研究院扩建", "科学院扩建", "综合楼扩建", "办公楼扩建", "停车场扩建", "安置房扩建", "用房扩建", "楼房扩建", "厂房扩建", "工房扩建", "配套楼扩建", "车库扩建", "物流园扩建", "工业园扩建", "旅馆扩建", "宿舍扩建", "商品房扩建", "公寓扩建", "仓库扩建", "实验楼扩建", "化验楼扩建", "私宅扩建", "加工房扩建", "车间扩建", "剧院扩建", "自行车棚扩建", "水池扩建", "温室扩建", "仓储扩建", "信息园扩建", "产业园扩建", "科技园扩建", "购物中心扩建", "商城扩建", "配套用房扩建", "教学楼扩建", "公租房扩建", "博物馆扩建", "处理厂扩建", "停车楼扩建", "运动场扩建", "招待所扩建", "门诊扩建", "综合大楼扩建", "附属楼扩建", "孵化园扩建", "围墙扩建", "实验园扩建", "实训楼扩建", "便利店扩建", "门诊楼扩建", "新院扩建", "幼儿园扩建", "高中扩建", "卫生院扩建", "保健院扩建", "观景台扩建", "展示馆扩建", "垃圾池扩建", "图书馆扩建", "展览馆扩建", "道路扩建", "公路扩建", "高速路扩建", "水电扩建", "路面扩建", "硬化扩建", "水泥路扩建", "地铁扩建", "桥梁扩建", "隧道扩建", "公园扩建", "水利扩建", "环境扩建", "景观扩建", "电站扩建", "水安全扩建", "农田扩建", "饮用水扩建", "污水扩建", "管道扩建", "管线扩建", "管网扩建", "河道扩建", "绿化扩建", "取水口扩建", "园林扩建", "路灯扩建", "雨污分流扩建", "垃圾扩建", "体育馆扩建", "卫生站扩建", "场所扩建", "游泳馆扩建", "家属楼扩建", "供水扩建", "热力扩建", "市政扩建", "供暖扩建", "外墙扩建", "地块扩建", "城墙扩建", "基础设施扩建", "房屋扩建", "危房扩建", "棚户区扩建", "安置点扩建", "供热扩建", "宅基地扩建", "农贸市场扩建", "菜市场扩建", "服务中心扩建", "安置区扩建", "家园扩建", "花园扩建", "自来水扩建", "排水扩建", "水渠扩建", "水库扩建", "电厂扩建", "给水扩建", "村道扩建", "围栏扩建", "雨棚扩建", "大道扩建", "街道扩建", "居住新建", "住宅新建", "小区新建", "别墅新建", "医院新建", "酒店新建", "宾馆新建", "商场新建", "商业新建", "场馆新建", "音影院新建", "车站新建", "机场新建", "学校新建", "院校新建", "小学新建", "大学新建", "中学新建", "研究院新建", "科学院新建", "综合楼新建", "办公楼新建", "停车场新建", "安置房新建", "用房新建", "楼房新建", "厂房新建", "工房新建", "配套楼新建", "车库新建", "物流园新建", "工业园新建", "旅馆新建", "宿舍新建", "商品房新建", "公寓新建", "仓库新建", "实验楼新建", "化验楼新建", "私宅新建", "加工房新建", "车间新建", "剧院新建", "自行车棚新建", "水池新建", "温室新建", "仓储新建", "信息园新建", "产业园新建", "科技园新建", "购物中心新建", "商城新建", "配套用房新建", "教学楼新建", "公租房新建", "博物馆新建", "处理厂新建", "停车楼新建", "运动场新建", "招待所新建", "门诊新建", "综合大楼新建", "附属楼新建", "孵化园新建", "围墙新建", "实验园新建", "实训楼新建", "便利店新建", "门诊楼新建", "新院新建", "幼儿园新建", "高中新建", "卫生院新建", "保健院新建", "观景台新建", "展示馆新建", "垃圾池新建", "图书馆新建", "展览馆新建", "道路新建", "公路新建", "高速路新建", "水电新建", "路面新建", "硬化新建", "水泥路新建", "地铁新建", "桥梁新建", "隧道新建", "公园新建", "水利新建", "环境新建", "景观新建", "电站新建", "水安全新建", "农田新建", "饮用水新建", "污水新建", "管道新建", "管线新建", "管网新建", "河道新建", "绿化新建", "取水口新建", "园林新建", "路灯新建", "雨污分流新建", "垃圾新建", "体育馆新建", "卫生站新建", "场所新建", "游泳馆新建", "家属楼新建", "供水新建", "热力新建", "市政新建", "供暖新建", "外墙新建", "地块新建", "城墙新建", "基础设施新建", "房屋新建", "危房新建", "棚户区新建", "安置点新建", "供热新建", "宅基地新建", "农贸市场新建", "菜市场新建", "服务中心新建", "安置区新建", "家园新建", "花园新建", "自来水新建", "排水新建", "水渠新建", "水库新建", "电厂新建", "给水新建", "村道新建", "围栏新建", "雨棚新建", "大道新建", "街道新建", "居住修缮", "住宅修缮", "小区修缮", "别墅修缮", "医院修缮", "酒店修缮", "宾馆修缮", "商场修缮", "商业修缮", "场馆修缮", "音影院修缮", "车站修缮", "机场修缮", "学校修缮", "院校修缮", "小学修缮", "大学修缮", "中学修缮", "研究院修缮", "科学院修缮", "综合楼修缮", "办公楼修缮", "停车场修缮", "安置房修缮", "用房修缮", "楼房修缮", "厂房修缮", "工房修缮", "配套楼修缮", "车库修缮", "物流园修缮", "工业园修缮", "旅馆修缮", "宿舍修缮", "商品房修缮", "公寓修缮", "仓库修缮", "实验楼修缮", "化验楼修缮", "私宅修缮", "加工房修缮", "车间修缮", "剧院修缮", "自行车棚修缮", "水池修缮", "温室修缮", "仓储修缮", "信息园修缮", "产业园修缮", "科技园修缮", "购物中心修缮", "商城修缮", "配套用房修缮", "教学楼修缮", "公租房修缮", "博物馆修缮", "处理厂修缮", "停车楼修缮", "运动场修缮", "招待所修缮", "门诊修缮", "综合大楼修缮", "附属楼修缮", "孵化园修缮", "围墙修缮", "实验园修缮", "实训楼修缮", "便利店修缮", "门诊楼修缮", "新院修缮", "幼儿园修缮", "高中修缮", "卫生院修缮", "保健院修缮", "观景台修缮", "展示馆修缮", "垃圾池修缮", "图书馆修缮", "展览馆修缮", "道路修缮", "公路修缮", "高速路修缮", "水电修缮", "路面修缮", "硬化修缮", "水泥路修缮", "地铁修缮", "桥梁修缮", "隧道修缮", "公园修缮", "水利修缮", "环境修缮", "景观修缮", "电站修缮", "水安全修缮", "农田修缮", "饮用水修缮", "污水修缮", "管道修缮", "管线修缮", "管网修缮", "河道修缮", "绿化修缮", "取水口修缮", "园林修缮", "路灯修缮", "雨污分流修缮", "垃圾修缮", "体育馆修缮", "卫生站修缮", "场所修缮", "游泳馆修缮", "家属楼修缮", "供水修缮", "热力修缮", "市政修缮", "供暖修缮", "外墙修缮", "地块修缮", "城墙修缮", "基础设施修缮", "房屋修缮", "危房修缮", "棚户区修缮", "安置点修缮", "供热修缮", "宅基地修缮", "农贸市场修缮", "菜市场修缮", "服务中心修缮", "安置区修缮", "家园修缮", "花园修缮", "自来水修缮", "排水修缮", "水渠修缮", "水库修缮", "电厂修缮", "给水修缮", "村道修缮", "围栏修缮", "雨棚修缮", "大道修缮", "街道修缮"};

            List<String> blacks = LogUtils.readRule("blockKeys");
            for (String a : aa) {
                for (String b : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        //标题-a    标题-b
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND newProvince:\"" + "新疆维吾尔自治区" + "\"  AND title:\"" + a + "\"  AND title:\"" + b + "\"", a + "&" + b, 1);
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

            for (String d : dd) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND newProvince:\"" + "新疆维吾尔自治区" + "\"  AND allcontent:\"" + d + "\"", d, 1);
                    log.info(d + "- - -" + mqEntities.size());
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
                                    data.setKeyword(d);
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
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND  newProvince:\"" + "新疆维吾尔自治区" + "\"   AND title:\"" + c + "\"", c, 1);
                    log.info(c + "- - -" + mqEntities.size());
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
                                    data.setKeyword(c);
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


            log.info("去重数据量-Map：" + listMap.size());
            list.addAll(listMap);
            /*int sum = listAll.stream().collect(Collectors.groupingBy(NoticeMQ::getContentid)).size();
            log.info("直接手动输入去重数据量: {}",sum);
            System.out.println("=============================+++++"+sum);


            if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }*/

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + list.size());

            //currencyService.soutKeywords(listAll, list.size(), s, name, date);

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
    public void getSengDaMeiXin2(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后的数据-map
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        try {
            String[] c1 = {"挖掘机", "装载机", "起重机", "路面机械", "桩工机械", "自卸车", "重卡", "煤炭机械", "港口机械", "混砂车", "仪表车", "混配车", "泵车", "车载泵", "搅拌车", "砂浆泵", "喷浆车", "湿喷机", "拖泵", "管桩泵", "作业车", "摊铺机", "压路机", "平地机", "铣刨机", "搅拌站", "牵引车", "载货车", "燃气车", "叉车", "正面吊", "堆高机", "运输车"};
            String[] c2 = {"微型挖掘机", "小型挖掘机", "中型挖掘机", "大型挖掘机", "轮式挖掘机", "轮式装载机", "多功能油田专用车", "机械式压裂车", "液压式压裂车", "电驱压裂撬", "机械驱动压裂泵", "液压驱动压裂泵", "油管作业机", "液氮泵车", "高压直管", "混凝土泵车", "混凝土喷浆车", "底盘湿喷机", "氢燃料搅拌车", "汽车超重机", "全地面起重机", "履带起重机", "伸缩臂履带起重机", "多功能履带起重机", "特种起重机", "风电特种起重机", "尖头塔式起重机", "塔式起重机", "平头塔式起重机", "直臂式随车起重机", "折臂式随车起重机", "剪叉式高空作业车", "多功能摊铺机", "伸缩迷你型摊铺机", "超级摊铺机", "沥青摊铺机", "单钢轮压路机", "单钢轮双驱压路机", "双钢轮压路机", "轮胎式压路机", "组合式压路机", "矿用平地机", "沥青搅拌站", "旋挖钻机", "宽体自卸车", "矿用自卸车", "智能自卸车", "半挂牵引车", "电动正面吊", "铁路专用正面吊", "电动堆高机", "集装箱空箱堆高机", "抓钢料机", "电动抓钢料机", "纯电动港口牵引车", "伸缩臂叉车", "集装箱起重机", "箱门式起重机", "门座式起重机", "预制件专用运输车"};
            for (String str : c1) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND newProvince:\"" + "新疆维吾尔自治区" + "\"   AND title:\"" + str + "\"", str, 1);
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
            for (String str : c2) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND newProvince:\"" + "新疆维吾尔自治区" + "\"  AND allcontent:\"" + str + "\"", str, 1);
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
                int sum = listAll.stream().collect(Collectors.groupingBy(NoticeMQ::getContentid)).size();
                log.info("直接手动输入去重数据量: {}", sum);
                System.out.println("=============================+++++" + sum);
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
    public void getZhongTieJian(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后的数据-map
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        try {
            String[] aa = {"创意信息技术股份有限公司", "杭州安恒信息技术股份有限公司", "安徽省交通建设股份有限公司", "筑博设计股份有限公司", "江西绿巨人生态环境股份有限公司", "北京新时空科技股份有限公司", "南京市测绘勘察研究院股份有限公司", "金现代信息产业股份有限公司", "郑州捷安高科股份有限公司", "正业设计股份有限公司", "深圳市城市交通规划设计研究中心股份有限公司", "北京铜牛信息科技股份有限公司", "广州地铁设计研究院股份有限公司", "华泰永创(北京)科技股份有限公司", "上海太和水环境科技发展股份有限公司", "苏文电能科技股份有限公司", "重庆市城市建设投资(集团)有限公司", "深圳市金证科技股份有限公司", "上海隧道工程股份有限公司", "中国建筑集团有限公司", "广东水电二局股份有限公司", "中铁三局集团有限公司", "中国铁建股份有限公司", "浙江东南网架股份有限公司", "深圳市市政工程总公司", "苏州金螳螂企业(集团)有限公司", "中国水利水电第十四工程局有限公司", "安徽中电兴发与鑫龙科技股份有限公司", "中铁十局集团有限公司", "重庆建工集团股份有限公司", "中国化学工程股份有限公司", "北京九恒星科技股份有限公司", "岳阳市城市建设投资有限公司", "中国核工业建设集团有限公司", "上海吉联新软件股份有限公司", "北京银信长远科技股份有限公司", "漳州市路桥经营有限公司", "泰兴市中兴国有资产经营投资有限公司", "安徽省华安外经建设(集团)有限公司", "南京大贺装饰工程有限公司", "大医科技股份有限公司", "乌鲁木齐高新投资发展集团有限公司", "多伦科技股份有限公司", "泉州市国有资产投资经营有限责任公司", "中交第二公路工程局有限公司", "中国铁建港航局集团有限公司", "四川省铁路集团有限责任公司", "森特士兴集团股份有限公司", "中国电建市政建设集团有限公司", "株洲市水利建设投资有限公司", "兰州市轨道交通有限公司", "中国二十冶集团有限公司", "中国电建集团中南勘测设计研究院有限公司", "合肥紫金钢管股份有限公司", "镇江市绿城园林建设有限公司", "中交第四公路工程局有限公司", "中国能源建设集团湖南火电建设有限公司", "中建三局第三建设工程有限责任公司", "中铁开发投资集团有限公司"};
            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND zhaoBiaoUnit:\"" + str + "\"", "", 3);
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
    public void getZhongTieJian_zhongBiao(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后的数据-map
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        try {
            String[] aa = {"创意信息技术股份有限公司", "杭州安恒信息技术股份有限公司", "安徽省交通建设股份有限公司", "筑博设计股份有限公司", "江西绿巨人生态环境股份有限公司", "北京新时空科技股份有限公司", "南京市测绘勘察研究院股份有限公司", "金现代信息产业股份有限公司", "郑州捷安高科股份有限公司", "正业设计股份有限公司", "深圳市城市交通规划设计研究中心股份有限公司", "北京铜牛信息科技股份有限公司", "广州地铁设计研究院股份有限公司", "华泰永创(北京)科技股份有限公司", "上海太和水环境科技发展股份有限公司", "苏文电能科技股份有限公司", "重庆市城市建设投资(集团)有限公司", "深圳市金证科技股份有限公司", "上海隧道工程股份有限公司", "中国建筑集团有限公司", "广东水电二局股份有限公司", "中铁三局集团有限公司", "中国铁建股份有限公司", "浙江东南网架股份有限公司", "深圳市市政工程总公司", "苏州金螳螂企业(集团)有限公司", "中国水利水电第十四工程局有限公司", "安徽中电兴发与鑫龙科技股份有限公司", "中铁十局集团有限公司", "重庆建工集团股份有限公司", "中国化学工程股份有限公司", "北京九恒星科技股份有限公司", "岳阳市城市建设投资有限公司", "中国核工业建设集团有限公司", "上海吉联新软件股份有限公司", "北京银信长远科技股份有限公司", "漳州市路桥经营有限公司", "泰兴市中兴国有资产经营投资有限公司", "安徽省华安外经建设(集团)有限公司", "南京大贺装饰工程有限公司", "大医科技股份有限公司", "乌鲁木齐高新投资发展集团有限公司", "多伦科技股份有限公司", "泉州市国有资产投资经营有限责任公司", "中交第二公路工程局有限公司", "中国铁建港航局集团有限公司", "四川省铁路集团有限责任公司", "森特士兴集团股份有限公司", "中国电建市政建设集团有限公司", "株洲市水利建设投资有限公司", "兰州市轨道交通有限公司", "中国二十冶集团有限公司", "中国电建集团中南勘测设计研究院有限公司", "合肥紫金钢管股份有限公司", "镇江市绿城园林建设有限公司", "中交第四公路工程局有限公司", "中国能源建设集团湖南火电建设有限公司", "中建三局第三建设工程有限责任公司", "中铁开发投资集团有限公司"};
            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    //List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND blZhongBiaoUnit:\"" + str + "\"", "", 3);
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND zhongBiaoUnit:\"" + str + "\"", "", 3);
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


            int sum = listAll.stream().collect(Collectors.groupingBy(NoticeMQ::getContentid)).size();
            log.info("新去重数据量New：" + sum);

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
    public void getZhongGuoShiYouTianRanQi(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(1);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后的数据-map
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        try {
            List<String> keyWords = LogUtils.readRule("keyWords");
            for (String str : keyWords) {
                futureList1.add(executorService1.submit(() -> {
                    //List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:3 OR progid:5)  AND zhongBiaoUnit:\"" + str + "\"", "", 1);//中标
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "]  AND newZhongBiaoUnit:\"" + str + "\"", "", 1);//中标
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
    public void getNingBoHongTai2(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后的数据-map
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        try {
            String[] aa = {"膜结构", "钢结构"};
            String[] bb = {"遮阳棚", "雨棚", "停车棚", "张拉膜", "张拉膜", "膜结构工程", "膜结构建设", "停车场工程", "拉伸膜", "钢结构总承包工程", "钢结构保温房", "钢结构部分工程", "钢结构工程（一标段）", "装配式钢结构工程"};
            String[] cc = {"钢结构"};
            String[] dd = {"工程", "制作安装"};
            String[] blacks = {"监测", "检测", "设备采购", "油漆", "涂料", "显示屏", "led", "桌", "设备", "租赁", "结构工程有限公司", "结构工程公司", "劳务分包", "钢材招标", "车亭采购", "材料采购", "安装服务", "劳务", "钢材采购", "物资采购", "结构建筑有限公司", "结构建筑公司", "辅材", "钢材询价", "钢材招采", "刷漆", "除锈", "喷砂"};
            for (String c : aa) {
                for (String d : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 2]   AND allcontent:\"" + c + "\"  AND allcontent:\"" + d + "\"", c + "&" + d, 4);
                        log.info(c.trim() + "&" + d + "————" + mqEntities.size());
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
                                        data.setKeyword(c + "&" + d);
                                        listAll.add(data);
                                    }
                                }
                            }
                        }
                    }));
                }
            }
            for (String c : cc) {
                for (String d : dd) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 2]   AND title:\"" + c + "\"  AND title:\"" + d + "\"", c + "&" + d, 4);
                        log.info(c.trim() + "&" + d + "————" + mqEntities.size());
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
                                        data.setKeyword(c + "&" + d);
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
    public void getBeiJingGuoShi(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(1);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后的数据-map
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        try {
            String[] keyWords = {"专题片", "纪录片", "广告片", "宣传片", "形象片", "三维制作", "动画片", "视频拍摄", "汇报片", "微电影", "视频服务", "影视拍摄", "庆典视频", "教学片"};
            for (String str : keyWords) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 2] AND allcontent:\"" + str + "\"", "", 1);//中标
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
    public void getDaHua(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后的数据-map
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        try {
            List<String> keyWords = LogUtils.readRule("keyWords");
            for (String str : keyWords) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[:" + date + "] AND progid:3 AND zhongBiaoUnit:\"" + str + "\"", "", 1);//中标
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
    public void getShuZiRenZheng(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后的数据-map
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        try {
            String[] aa = {"电子签名", "电子签章", "电子票据", "电子保单系统", "电子合同", "电子认证", "电子印章", "协同签名", "时间戳", "信手书", "统一身份认证", "身份管理", "CA数字证书", "法人一证通", "可信电子凭证", "可信电子成绩单", "签名服务器", "云章系统", "云签名", "密钥系统", "密钥分发系统", "密钥管理", "医疗信息化", "医院信息化", "HIS", "HRP", "CDSS", "PACS", "远程诊疗", "数字化医院", "治疗数字化", "诊疗数字化", "远程诊疗数字化", "远程医疗数字化", "医院云数字化", "医联体数字化", "医共体数字化", "PACS数字化", "健康医疗云", "大数据云医疗", "医疗云服务", "医疗上云", "医疗云平台", "远程医疗", "网上就诊", "网上就医", "远程手术", "远程B超", "EMR", "LIS", "医院上云", "云上医院", "云上医疗", "中医信息化", "医院信息系统", "医疗信息系统", "医疗软件", "医用软件", "医院软件", "医保软件", "智能康复", "医院智能化", "医保智能审核", "医院智能通讯", "医疗人工智能", "人工智能医疗", "医院智能化建设", "智慧医疗", "数字化医院软件", "医护软件", "医疗管理软件", "医疗行业软件", "医学软件", "医学院软件平台", "医药软件", "医院管理软件", "医院软件升级", "医院软件系统", "医院软件政府采购", "铸造软件", "专科学校软件开发", "医院信息管理软件", "医院信息系统软件", "诊改平台软件", "数据医院", "医疗数据", "医疗卫生数据", "保健所数字", "保健院数字", "保健站数字", "保健中心数字", "病案数字化", "病历数字化", "病历数字化加工", "电子病历", "电子病例", "防治办公室数字", "防治所数字", "防治所数字化", "防治院数字", "防治站数字", "防治中心数字", "福利院数字", "急救中心数字", "健康局数字", "健康委员会数字", "健康中心数字", "康复数字化", "康复中心数字", "门诊部数字", "门诊数字", "门诊数字化", "抢救数字化", "数字病房", "数字病理", "数字化病案", "数字化病理", "数字化医院建设项目", "数字化医院信息", "体检数字化", "体检中心数字", "体能数字化", "透析中心数字", "拓片数字化", "外科中心数字", "卫生保健中心数字", "卫生服务站数字", "卫生局数字", "卫生所数字", "卫生厅数字", "卫生院数字", "卫生院校数字", "卫生站数字", "卫生中心数字", "医护数字", "医检所数字", "医疗机构数字化", "医疗数字", "医疗卫生数字化", "医疗中心数字", "医务室数字", "医学院数字", "医药数字", "医用数字", "医院部数字", "预防接种数字化", "预防控制数字", "院区数字化", "诊疗数字", "诊数字化", "诊所数字", "职业病院数字", "治疗中心数字", "中医馆数字", "中医数字化", "住院楼数字", "电子病历系统", "诊断数字化", "远程数字化", "智慧云医院", "智慧云医院服务", "云医院", "云医疗", "数字认证", "数字证书认证", "图纸数字化", "数字签名", "医院无纸化", "医疗无纸化", "卫生计生信息系统", "医药集中采购信息平台", "智慧医院", "智能医院", "智能医疗", "远程会诊"};
            String[] bb = {"零信任", "电子病历", "电子病案", "互联网医院", "互联网医疗", "病案数字化", "医院集成平台", "健康信息平台", "卫生信息平台", "统一帐号管理", "可信身份认证", "SSL网关"};
            String[] cc = {"信息交互", "信息管理", "信息系统", "信息化建设", "信息平台", "信息化系统", "信息服务平台", "信息化升级", "信息化改造", "信息化工程", "综合信息平台", "信息化集成", "互联互通", "叫号系统", "超融合系统", "电子信息服务", "信息签核系统", "信息综合系统", "管理系统", "信息发布系统", "预约数字化", "叫号数字化", "数据安全", "云安全", "安全云", "云容灾", "云巡检", "安防云", "大数云防", "云防系统", "云监控", "信息安全", "运维软件", "信息软件", "集成平台", "智慧中台", "信息化", "智能信息化", "医联体", "桌面系统", "电子签名系统", "知识系统", "标测系统", "导向系统", "监控系统", "支持系统", "系统开发", "平台系统", "一卡通系统", "考核系统", "导航系统", "审计系统", "考试系统", "筛查系统", "安检系统", "共享系统", "防控系统", "系统平台", "巡更系统", "归档系统", "发展系统", "测评系统", "传输系统采购", "上报系统", "报告系统", "干预系统", "教学系统", "监管系统", "管控系统", "改造系统", "业务系统", "系统功能", "核算系统", "采集系统", "培训系统", "探视系统", "题库系统", "服务系统", "审核系统", "上传系统", "呼叫系统", "转播系统", "内控系统", "办公系统", "票据系统实施", "查询系统", "导检系统", "定位系统", "发布系统", "发票系统", "仓储系统", "事件系统", "调度系统", "示教系统", "统计系统", "报销系统", "核对系统", "分配系统", "评审系统", "防护系统", "支撑系统", "警报系统", "引导系统", "收据系统", "接警系统", "层析系统", "应急系统", "接口系统", "结算系统", "溯源系统", "物资系统", "展示系统", "安全系统", "人事系统", "监视系统", "系统开发项目", "核查系统", "核签系统", "建设系统", "合一系统", "缴费系统", "环境系统", "互动系统", "安保系统", "ERP系统", "技术系统", "登记系统", "备份系统", "应急指挥系统", "协同系统", "财务系统", "前端系统", "监控安防系统", "排队系统", "医疗大数据", "数据库", "数据资源", "数据迁移", "数据服务", "数据共享", "数据整合", "数据治理", "数据交换", "数据专线", "数据备份", "数据传输", "数据加工", "数据存储", "数据质量", "数据整理", "数据接口", "数据取证", "数据接入", "数据监测", "分布式计算", "智能化医疗", "智能化", "智能运维", "智能培训", "智能门禁", "智能客服", "数据智能", "人体分析", "人脸支付", "人脸识别", "人脸融合", "人脸取样", "人脸检测", "人工智能", "融合大数据", "智慧健康", "智能识别", "后勤管理", "健康管理", "云计算", "云应用", "医疗云", "医院云", "云盘", "桌面云", "专有云", "云服务", "云租赁", "云桌面", "云主机", "云终端", "云平台", "云存储", "公有云", "私有云", "混合云", "专属云", "云大数据", "云网融合", "云管平台", "云化项目", "云客服", "电信云上", "云网负载", "云盘建设", "云盘扩容", "企业云盘", "云盘系统", "内部云盘", "数据云盘", "网络云盘", "网站安全云", "会诊会议", "诊断会议", "监控工程", "集中监控", "机房监控", "医疗监控报警", "医疗监控", "病房监控", "诊所监控", "云服务器", "远程", "医疗网", "医院网络", "医疗网站", "云专线", "SDAN", "等保", "灾备", "容灾", "网络安全", "主机防护", "入侵检测", "入侵防御", "安全网关", "安全网络", "安全隔离与信息交换", "等保测评", "等级保护", "边界安全产品", "终端安全", "应用安全", "反欺诈", "登录保护", "注册保护", "内容安全", "号码安全", "信息安全测评", "网站信息安全", "应用软件", "杀毒软件", "软件", "软件集成", "系统支撑软件", "办公软件", "财务软件", "企业管理软件", "在线办公", "在线会议", "计算机软件", "预警软件", "处理软件", "监控软件", "专业软件", "数字软件", "加密软件", "工作软件", "定制软件", "测算软件", "备份软件", "社保软件", "工程软件", "审计软件", "软件采购", "软硬件", "软件升级", "软件服务", "软件测试", "模拟软件", "评估软件", "软件扩容", "评测软件", "测评管理软件", "软件监控", "配置管理软件", "虚拟存储软件", "云运维管理软件", "监控运维", "信息运维", "托管运维", "自然语言处理", "物联网", "事务式中间件", "面向对象中间件", "虚拟化"};
            String[] hy1 = {"医疗单位", "政府机构", "商业公司"};
            //String[] hy2={"血站","急救中心","疾控中心","卫生院","疗养院","专科医院","中医院","综合医院","医疗","医疗服务"};
            String[] blacks = {"硬件", "设备", "合同签署", "冷柜EMR", "EMR粉尘", "EMR机框", "EMR线体", "U型端头LIS", "医疗器械", "医疗配套设施", "检查仪", "印刷制品", "印刷品", "血管造影机", "维修保养", "维保", "捐赠", "反馈仪", "反馈仪", "货物", "治疗仪", "耗材", "电子合同签订", "电子合同印刷", "签订电子合同", "签章要求", "环境整治", "林地", "颗粒采购", "改造工程", "后勤物业", "物流配送", "办公家具", "窗帘采购", "修缮工程", "触控一体机", "医院建设", "出租", "租赁", "维修", "保养", "文艺汇演", "招聘", "仪器", "密度仪", "电话", "厅建设", "物资", "废料", "批量采购", "信息管理科", "装修", "医学影像科", "耗材", "机房建设", "设施采购", "库", "施工", "牌照"};
            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    //List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND title:\"" + str + "\"", "", 1);//招中标
                    //List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 2] AND title:\"" + str + "\"", "", 1);//招标
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:3 OR progid:5) AND title:\"" + str + "\"", "", 1);//中标
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
            for (String str : bb) {
                futureList1.add(executorService1.submit(() -> {
                    //List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND allcontent:\"" + str + "\"", "", 1);//招中标
                    //List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 2]  AND allcontent:\"" + str + "\"", "", 1);//招标
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:3 OR progid:5)  AND allcontent:\"" + str + "\"", "", 1);//中标
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
            for (String s1 : hy1) {
                for (String c : cc) {
                    futureList1.add(executorService1.submit(() -> {
                        //List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND zhaoFirstIndustry:\""+s1+"\"  AND (zhaoSecondIndustry:血站 OR zhaoSecondIndustry:急救中心 OR zhaoSecondIndustry:疾控中心 OR zhaoSecondIndustry:卫生院 OR zhaoSecondIndustry:疗养院 OR zhaoSecondIndustry:专科医院 OR zhaoSecondIndustry:中医院 OR zhaoSecondIndustry:综合医院 OR zhaoSecondIndustry:医疗 OR zhaoSecondIndustry:医疗服务)  AND title:\"" + c + "\"", "", 1);//招中标
                        //List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 2] AND zhaoFirstIndustry:\""+s1+"\"  AND (zhaoSecondIndustry:血站 OR zhaoSecondIndustry:急救中心 OR zhaoSecondIndustry:疾控中心 OR zhaoSecondIndustry:卫生院 OR zhaoSecondIndustry:疗养院 OR zhaoSecondIndustry:专科医院 OR zhaoSecondIndustry:中医院 OR zhaoSecondIndustry:综合医院 OR zhaoSecondIndustry:医疗 OR zhaoSecondIndustry:医疗服务)  AND title:\"" + c + "\"", "", 1);//招标
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:3 OR progid:5) AND zhaoFirstIndustry:\"" + s1 + "\"  AND (zhaoSecondIndustry:血站 OR zhaoSecondIndustry:急救中心 OR zhaoSecondIndustry:疾控中心 OR zhaoSecondIndustry:卫生院 OR zhaoSecondIndustry:疗养院 OR zhaoSecondIndustry:专科医院 OR zhaoSecondIndustry:中医院 OR zhaoSecondIndustry:综合医院 OR zhaoSecondIndustry:医疗 OR zhaoSecondIndustry:医疗服务)  AND title:\"" + c + "\"", "", 1);//中标

                        log.info(c + "- - -" + mqEntities.size());
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
                                        data.setKeyword(c);
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
    public void getKeyWordById() throws Exception {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        //关键词a
        try {
            List<String> idsFile = LogUtils.readRule("idsFile");
            for (String id : idsFile) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("id:\"" + id + "\"", "", 1);
                    log.info(id + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    //data.setKeyword(id);
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

        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> getKeyWordByIdAndSave(content)));
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

        System.out.println("--------------------------------kawa本次任务结束---------------------------------------");
    }

    @Override
    public void getBeiLangYiLiao(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后的数据-map
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        try {
            String[] aa = {"手术显微镜", "医用显微镜", "倒置生物显微镜", "正置生物显微镜", "数码生物显微镜", "光学生物显微镜", "荧光生物显微镜", "LED生物显微镜", "自动扫描显微镜", "电子阴道显微镜", "显微注射用显微镜", "时差倒置显微镜", "裂隙灯显微镜", "角膜内皮细胞显微镜", "角膜共焦显微镜", "眼科超声生物显微镜", "手持式裂隙灯显微镜", "眼科裂隙灯显微镜", "手持裂隙灯显微镜", "焦数码裂隙灯显微镜", "角膜内皮显微镜", "根管显微镜", "荧光显微镜", "医用专用显微镜", "偏光显微镜", "电子显微镜", "原子力显微镜", "生物显微镜", "红外显微镜", "体式显微镜", "拉曼显微镜", "正置显微镜", "金相显微镜", "谱激光共聚焦显微镜", "倒置显微镜", " 医用专用显微镜", "包裹体显微镜", "体视显微镜", "数码显微镜", "连续变倍体视显微镜", "光片显微镜", "视频显微镜", "口腔显微镜", "显微镜及配件", "高倍显微镜", "大视野显微镜", "读数显微镜", "眼科显微镜", "聚焦显微镜", "智能显微镜", "手持显微镜", "显微镜（带ccd", "内窥显微镜 ", "角膜显微镜", "解剖显微镜", "卧式显微镜", "立式显微镜", "电子扫描显微镜", "双目显微镜 ", "300g显微镜", "单人显微镜 ", "外科显微镜", "实验室显微镜", "显微镜及成像系统", "双目显微镜", "三目显微镜", "光学显微镜物镜", "多头显微镜", "进口显微镜 ", "进口显微镜 ", "三维显微镜", "双管显微镜", "相差显微镜", "多媒体显微镜", "材料显微镜", "成像显微镜", "一体化显微镜", "相衬显微镜", "显微镜等多种", "显微镜等国产", "红外显微镜 ", "光子显微镜", "多功能显微镜", "教师显微镜", "科研级显微镜", "照明显微镜", "隧道显微镜", "立体显微镜", "研究型显微镜", "光学仪器/显微镜", "偏振显微镜", "聚焦显微镜 ", "光纤显微镜", "pentero", "Kinevo"};
            String[] fza = {"显微镜"};
            String[] bb = {"医院", "诊所", "门诊", "保健院", "健康委员会", "医学院", "体检中心", "健康局", "医院部", "药房", "卫生院", "医疗保障局", "合作医疗", "医药服务管理司", "兽医实验室", "医药", "精神病院", "防治院", "血液中心", "眼科中心", "治疗中心", "保健中心", "保健所", "血管病研究所", "防治所", "外科中心", "康复中心", "透析中心", "正畸中心", "荣军院", "防治中心", "保健站", "列腺病研究所", "职业病院", "防治站", "产院", "急救中心", "卫生局", "卫生厅", "防治办公室", "卫生保健中心", "医疗中心", "卫生中心", "门诊部", "卫生服务站", "医检所", "制剂室", "药交所", "眼科", "医保", "医疗保障", "卫健委", "戒毒所", "敬老院", "疗养院", "眼病防治所", "矫治所", "结核病防治所", "休养所", "血站", "福利院", "医疗机构", "病防治办公室", "计划生育", "生育委员", "计生委", "大健康", "同仁堂", "江中集团", "医学", "健康科技", "养生堂", "保健品", "诊断", "康宁", "制药", "药业", "药集团", "医疗集团", "精神卫生", "药店", "军医", "医用", "医疗", "诊疗", "残联", "医护", "卫生所", "卫生院 ", "卫生院校", "医科大学", "妇幼", "健康中心", "运动康复", "中医馆", "预防控制", "医务室"};
            for (String a : aa) {
                for (String b : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 3]   AND allcontent:\"" + a + "\"  AND zhaoBiaoUnit:\"" + b + "\"", "", 1);
                        log.info(a.trim() + "&" + b + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    if (flag) {
                                        data.setKeyword(a + "&" + b);
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
            for (String a : aa) {
                for (String b : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 3]   AND allcontent:\"" + a + "\"  AND title:\"" + b + "\"", "", 1);
                        log.info(a.trim() + "&" + b + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    if (flag) {
                                        data.setKeyword(a + "&" + b);
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

            for (String a : fza) {
                for (String b : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 3]   AND title:\"" + a + "\"  AND zhaoBiaoUnit:\"" + b + "\"", "", 2);
                        log.info(a.trim() + "&" + b + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    if (flag) {
                                        data.setKeyword(a + "&" + b);
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
            for (String a : fza) {
                for (String b : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 3]   AND title:\"" + a + "\"  AND title:\"" + b + "\"", "", 2);
                        log.info(a.trim() + "&" + b + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    if (flag) {
                                        data.setKeyword(a + "&" + b);
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
                ExecutorService executorService = Executors.newFixedThreadPool(8);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getKeyWordByIdAndSave2(content)));
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
    public void getQingHuaDaXue(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后的数据-map
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, Object> dataMap = new HashMap<>();
        try {
            String[] aa = {"硅片", "电池片", "电池组件", "硅料", "拉棒", "多晶硅", "晶体硅", "薄膜电池", "太阳能板", "太阳能单晶板", "蓄电池", "太阳板", "铜芯导线", "铜芯花线", "单晶硅", "非晶硅", "电池板", "光伏板"};
            String[] bb = {"光伏"};
            String[] cc = {"设备", "组件", "配件", "支架", "打桩机", "电池", "逆变器", "汇流箱", "太阳能组件", "充电板", "控制器", "配电柜", "电池方阵", "扩散炉", "零部件", "硅棒", "硅锭", "晶圆", "薄膜", "电缆", "配电箱", "部件", "零件", "单多晶", "玻璃", "胶膜"};
            String[] dd = {"采购光伏", "购置光伏", "光伏设备采购", "光伏组件采购", "光伏配件采购", "光伏支架采购", "光伏打桩机采购", "光伏电池采购", "光伏逆变器采购", "光伏汇流箱采购", "光伏太阳能组件采购", "光伏充电板采购", "光伏电池采购"};
            String[] ee = {"采购", "购置", "购买", "采买", "询价"};
            String[] blacks = {"光伏设备有限公司", "多晶硅有限公司", "系统采购", "加固", "紧固", "测试仪", "蓄电池除外", "污水处理", "清洗", "半导体生产设备", "系统设备", "网络设备", "滤芯采购", "电机车", "供水设备", "吊机车", "叉车", "充电机", "镗床采购", "检测装置", "切机设备", "搅拌装置", "维护保养", "车", "回收处置", "硅片项目", "检测设备", "测试服务", "变压器采购", "消缺工程", "改造工程", "调整工程", "安装工程", "预防性试验", "光伏有限公司", "清扫", "软件升级", "清理", "擦洗服务", "核算项目", "台式机", "（多晶硅）", "蓄电池间", "蓄电池室", "废物处理", "钢材采购", "设备改造", "施工", "多晶硅二期", "多晶硅产能", "单晶硅项目", "考核设备", "整治工程"};

            //e词拼a
            List<String> eea = new ArrayList<>();
            for (String e : ee) {
                for (String a : aa) {
                    String str = e + a;
                    eea.add(str);
                }
            }

            //a词拼e
            List<String> aae = new ArrayList<>();
            for (String a : aa) {
                for (String e : ee) {
                    String str = a + e;
                    aae.add(str);
                }
            }

            for (String a : aa) {
                for (String e : ee) {
                    futureList1.add(executorService1.submit(() -> {
                        //List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND title:\"" + a + "\"  AND title:\"" + e + "\"", "", 1);
                        //List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 2]   AND title:\"" + a + "\"  AND title:\"" + e + "\"", "", 1);
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:3 OR progid:5)  AND title:\"" + a + "\"  AND title:\"" + e + "\"", "", 1);
                        log.info(a + "&" + e + "————" + mqEntities.size());
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
                                        data.setKeyword(a + "&" + e);
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

            for (String b : bb) {
                for (String c : cc) {
                    for (String e : ee) {
                        futureList1.add(executorService1.submit(() -> {
                            //List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND title:\"" + b + "\"  AND title:\"" + c + "\" AND title:\"" + e + "\"", "", 2);
                            //List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 2]  AND title:\"" + b + "\"  AND title:\"" + c + "\" AND title:\"" + e + "\"", "", 2);
                            List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:3 OR progid:5)  AND title:\"" + b + "\"  AND title:\"" + c + "\" AND title:\"" + e + "\"", "", 2);
                            log.info(b + "&" + c + "&" + e + "————" + mqEntities.size());
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
                                            data.setKeyword(b + "&" + c + "&" + e);
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
            }

            //全文检索 a拼e
            for (String str : aae) {
                futureList1.add(executorService1.submit(() -> {
                    //List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND allcontent:\"" + str + "\"", "", 3);
                    //List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 2] AND allcontent:\"" + str + "\"", "", 3);
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:3 OR progid:5) AND allcontent:\"" + str + "\"", "", 3);
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
            //全文检索 e拼a
            for (String str : eea) {
                futureList1.add(executorService1.submit(() -> {
                    //List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND allcontent:\"" + str + "\"", "", 4);
                    //List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 2] AND allcontent:\"" + str + "\"", "", 4);
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:3 OR progid:5) AND allcontent:\"" + str + "\"", "", 4);
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
            for (String str : dd) {
                futureList1.add(executorService1.submit(() -> {
                    //List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND allcontent:\"" + str + "\"", "", 5);
                    //List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 2] AND allcontent:\"" + str + "\"", "", 5);
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:3 OR progid:5) AND allcontent:\"" + str + "\"", "", 5);
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
    public void getWuHanXinTan(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后的数据-map
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        try {
            String[] aa = {"外墙清洗", "外墙清洁", "楼宇清洗", "外墙及玻璃清洗", "外墙玻璃清洗", "玻璃清洗", "外立面清洗", "外立面清洁", "玻璃清洁"};
            String[] blacks = {"清洗剂", "废碎玻璃", "清洗机", "清洗台", "清洗干燥机", "清洗加工", "清洗液", "清洁器", "清洁液", "清洁剂", "清洁工具", "不含外墙清洗", "不包括外墙清洁", "汽车维修"};
            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5) AND newProvince:\"" + "广东省" + "\" AND allcontent:\"" + str + "\"", "", 1);
                    log.info(str + "- - -" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
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
                ExecutorService executorService = Executors.newFixedThreadPool(8);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getKeyWordByIdAndSave2(content)));
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
    public void getBeiLangYiLiao2(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后的数据-map
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        try {
            String[] aa = {"手术显微镜", "医用显微镜", "自动扫描显微镜", "电子阴道显微镜", "显微注射用显微镜", "根管显微镜", "医用专用显微镜", "偏光显微镜", "电子显微镜", "原子力显微镜", "红外显微镜", "拉曼显微镜", "金相显微镜", " 医用专用显微镜", "包裹体显微镜", "体视显微镜", "连续变倍体视显微镜", "光片显微镜", "视频显微镜", "口腔显微镜", "显微镜及配件", "大视野显微镜", "读数显微镜", "智能显微镜", "手持显微镜", "显微镜（带ccd", "内窥显微镜 ", "解剖显微镜", "卧式显微镜", "立式显微镜", "电子扫描显微镜", "300g显微镜", "单人显微镜 ", "外科显微镜", "实验室显微镜", "显微镜及成像系统", "三目显微镜", "光学显微镜物镜", "多头显微镜", "进口显微镜 ", "三维显微镜", "双管显微镜", "多媒体显微镜", "材料显微镜", "成像显微镜", "一体化显微镜", "相衬显微镜", "显微镜等多种", "显微镜等国产", "红外显微镜 ", "光子显微镜", "多功能显微镜", "教师显微镜", "科研级显微镜", "隧道显微镜", "立体显微镜", "研究型显微镜", "光学仪器/显微镜", "偏振显微镜", "光纤显微镜", "pentero", "Kinevo"};
            String[] fza = {"显微镜"};
            String[] bb = {"医院", "诊所", "门诊", "保健院", "健康委员会", "医学院", "体检中心", "健康局", "医院部", "药房", "卫生院", "医疗保障局", "合作医疗", "医药服务管理司", "兽医实验室", "医药", "精神病院", "防治院", "血液中心", "眼科中心", "治疗中心", "保健中心", "保健所", "血管病研究所", "防治所", "外科中心", "康复中心", "透析中心", "正畸中心", "荣军院", "防治中心", "保健站", "列腺病研究所", "职业病院", "防治站", "产院", "急救中心", "卫生局", "卫生厅", "防治办公室", "卫生保健中心", "医疗中心", "卫生中心", "门诊部", "卫生服务站", "医检所", "制剂室", "药交所", "眼科", "医保", "医疗保障", "卫健委", "戒毒所", "敬老院", "疗养院", "眼病防治所", "矫治所", "结核病防治所", "休养所", "血站", "福利院", "医疗机构", "病防治办公室", "计划生育", "生育委员", "计生委", "大健康", "同仁堂", "江中集团", "医学", "健康科技", "养生堂", "保健品", "诊断", "康宁", "制药", "药业", "药集团", "医疗集团", "精神卫生", "药店", "军医", "医用", "医疗", "诊疗", "残联", "医护", "卫生所", "卫生院 ", "卫生院校", "医科大学", "妇幼", "健康中心", "运动康复", "中医馆", "预防控制", "医务室"};
            for (String a : aa) {
                for (String b : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 3]   AND allcontent:\"" + a + "\"  AND zhaoBiaoUnit:\"" + b + "\"", "", 1);
                        log.info(a.trim() + "&" + b + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    if (flag) {
                                        data.setKeyword(a + "&" + b);
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
            for (String a : aa) {
                for (String b : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 3]   AND allcontent:\"" + a + "\"  AND title:\"" + b + "\"", "", 1);
                        log.info(a.trim() + "&" + b + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    if (flag) {
                                        data.setKeyword(a + "&" + b);
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

            for (String a : fza) {
                for (String b : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 3]   AND title:\"" + a + "\"  AND zhaoBiaoUnit:\"" + b + "\"", "", 2);
                        log.info(a.trim() + "&" + b + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    if (flag) {
                                        data.setKeyword(a + "&" + b);
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
            for (String a : fza) {
                for (String b : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 3]   AND title:\"" + a + "\"  AND title:\"" + b + "\"", "", 2);
                        log.info(a.trim() + "&" + b + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    if (flag) {
                                        data.setKeyword(a + "&" + b);
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


            System.out.println("- - - 统计开始- - - - - - - - - - - - - - - - - - - - ");
            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量-Map：" + listMap.size());

            int sum = listAll.stream().collect(Collectors.groupingBy(NoticeMQ::getContentid)).size();
            log.info("新去重数据量-sum：" + sum);

            if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("去重数据量-List：" + list.size());

            if ("1".equals(s) || "2".equals(s)) {
                currencyService.soutKeywords(listAll, list.size(), s, name, date);
            }

        } catch (Exception e) {
            e.getMessage();
        }
        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(8);
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
    public void getBeiLangYiLiao2_3(Integer type, String date, String s, String name) {
        ExecutorService executorService = Executors.newFixedThreadPool(20);//开启线程池
        List<Future> futureList = new ArrayList<>();

        try {
            List<String> ids = LogUtils.readRule("idsFile");
            for (String id : ids) {
                NoticeMQ noticeMQ = new NoticeMQ();
                noticeMQ.setContentid(Long.valueOf(id));
                futureList.add(executorService.submit(() -> getKeyWordByIdAndSave3(noticeMQ)));
            }
            for (Future future1 : futureList) {
                try {
                    future1.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    executorService.shutdown();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        } catch (Exception e) {
            e.getMessage();
        }

        System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    @Override
    public void getGuangZhouTianPu(Integer type, String date, String s, String name, Integer gz) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后的数据-map
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        try {
            String[] aa = {"音响", "电子白板", "交互式白板", "多媒体教学", "视频展台", "交互智能平板", "多媒体讲台", "多媒体投影机", "教学一体机", "互动教学设备", "广播系统"};
            String[] bb = {"大学", "中学", "小学", "幼儿园", "培训", "学校", "学院", "高中", "初中"};
            String[] blacks = {"修缮工程", "维修", "维护", "保洁", "物业"};
            for (String a : aa) {
                for (String b : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = null;
                        if (gz == 1) {
                            mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:3 OR progid:5) AND amountNumber:[300000 TO *]  AND allcontent:\"" + a + "\" AND zhongBiaoUnit:*  AND zhaoBiaoUnit:\"" + b + "\"", "", 1);
                        } else {
                            mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:3 OR progid:5) AND amountNumber:[300000 TO *]  AND allcontent:\"" + a + "\" AND zhongBiaoUnit:*  AND zhongRelationName:* AND zhongRelationWay:*  AND zhaoBiaoUnit:\"" + b + "\"", "", 2);
                        }
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


            System.out.println("- - - 统计开始- - - - - - - - - - - - - - - - - - - - ");
            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量-Map：" + listMap.size());

            int sum = listAll.stream().collect(Collectors.groupingBy(NoticeMQ::getContentid)).size();
            log.info("新去重数据量-sum：" + sum);

            if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("去重数据量-List：" + list.size());

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
                    futureList.add(executorService.submit(() -> pocDataFieldService.getZiDuanKu_ziTi(content)));
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
    public void getAoLinBaSi3(Integer type, String date, String s, String name, Integer tp) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后的数据-map
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        try {
            String[] aa = {"内窥镜", "胃镜", "肠镜", "腹腔镜", "胃肠镜", "宫腔镜", "支气管镜", "鼻咽喉镜", "胆道镜", "耳鼻喉镜", "宫腔镜", "电切镜", "耳鼻喉镜", "腹腔镜", "腔镜", "十二指肠镜", "超声镜", "小肠镜", "胸腔镜", "宫腔电切", "宫腔电切镜", "内镜", "电子镜", "气管镜", "输尿管镜", "电子胃镜", "电子腹腔镜", "电子结肠镜", "电切镜", "纤维支气管镜", "膀胱镜", "呼吸镜", "窥镜", "电子膀胱镜", "输尿管软镜", "电子内窥镜", "电子支气管镜", "腔镜", "电子肠镜", "电子胃肠镜", "超声刀", "能量平台", "小探头", "测漏器", "电刀", "光学视管", "气腹机", "肾盂镜", "探头驱动器", "纤维镜", "胸腔镜", "硬性镜"};
            String[] bb = {"鼻咽喉", "摄像系统", "超声", "摄像平台", "支气管", "输尿管", "胃肠", "宫腔", "腹腔", "呼吸", "膀胱", "消化", "胆道", "清洗消毒", "整体手术室", "影像装置", "图像处理", "摄像头", "监视器", "保养装置", "光源", "台车", "主机", "显示器", "适配器", "维护保养装置"};
            String[] hybq = {"医疗单位"};
            String[] blacks = {"EPC", "HIS系统", "办公耗材", "办公家具", "保洁", "保洁服务", "备灾", "被服", "布草洗涤", "餐饮服务", "承包", "初步设计", "畜牧", "床上用品", "磁共振成像系统", "地块", "电梯", "高清摄像系统", "后勤", "呼叫系统", "环境保护验收", "环境检测", "环境评估", "环境评价", "环境影响", "环境影响评估", "监理", "建设项目勘察", "经营", "勘察服务", "劳务外包", "遴选合作银行", "农业", "报废", "设备维保", "设计施工", "社会化服务", "社会化项目", "审计服务", "食堂", "食堂餐饮", "试剂", "体检服务", "体检项目", "网络信息系统", "卧推床", "无纺布", "午休床", "物业", "物业管理", "物证检验鉴定设备", "洗涤", "洗脱烘一体机", "项目管理服务", "信息化", "信息化（社区）软件", "信息化升级", "信息化系统建设", "信息平台", "信息系统", "信息系统集成平台", "印刷品", "印刷制品", "应用软件", "运送服务", "运营", "运营服务", "运营管理", "运营项目", "招租", "智能化系统", "中央监护系统", "租赁服务", "智能制造", "电脑主机", "UPS电池", "清洗机", "电厂", "电场", "锅炉系统", "水泥", "重齿齿轮箱", "行车配件", "IT相关维修配件", "保安", "消化酶", "低压配电", "配电工程", "装修改造", "勘察", "档案数字化", "复合钢管", "不锈钢", "窗帘", "床帘", "自助终端", "电脑显示器", "电脑", "打印机", "液晶显示器", "火花塞光纤传感器", "潍柴内窥镜", "潍柴", "装饰工程", "中央空调", "中央空调主机", "监控摄像头", "摄像头安装", "质量检测", "过氧乙酸", "仪表配件", "红外热像仪", "车辆维修", "信息系统驻场", "消毒烘干机", "救护车", "呼吸湿化治疗仪", "紫外线空气消毒机", "科室维修", "大楼综合服务", "装修工程", "空调", "手术室装修", "图书购置", "监控机房", "LED屏", "工业设计大赛", "标识标牌", "通风系统清洗", "锅炉", "淘汰拍卖", "地面坍塌", "法兰闸阀", "工程结算", "污水泵站", "热电", "燃机内窥镜", "工作服", "汽车专业", "水箱清洗", "春来人间暖 ", "风机齿轮箱", "消防主机", "污水处理设施", "园林景观设计", "二次精装修", "计算机设备维修", "新风管道", "新风系统", "排风系统", "服务器", "外包", "打印", "印刷", "安保", "护工", "生活助理", "终端系统", "云终端", "护理终端", "显示智能终端", "体重秤", "云系统"};
            for (String a : aa) {
                for (String b : hybq) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = null;
                        if (tp == 0) {
                            mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND allcontent:\"" + a + "\"  AND zhaoFirstIndustry:\"" + b + "\"", "", 1);//全部
                        } else if (tp == 2) {
                            mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 2]  AND allcontent:\"" + a + "\"  AND zhaoFirstIndustry:\"" + b + "\"", "", 1);//招标
                        } else if (tp == 3) {
                            mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:3 OR progid:5)  AND allcontent:\"" + a + "\"  AND zhaoFirstIndustry:\"" + b + "\"", "", 1);//中标
                        }
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
            for (String bc : bb) {
                for (String q : hybq) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = null;
                        if (tp == 0) {
                            mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND title:\"" + bc + "\"  AND zhaoFirstIndustry:\"" + q + "\"", "", 2);//全部
                        } else if (tp == 2) {
                            mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 2]  AND title:\"" + bc + "\"  AND zhaoFirstIndustry:\"" + q + "\"", "", 2);//招标
                        } else if (tp == 3) {
                            mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:3 OR progid:5)  AND title:\"" + bc + "\"  AND zhaoFirstIndustry:\"" + q + "\"", "", 2);//中标
                        }
                        log.info(bc.trim() + "&" + q + "————" + mqEntities.size());
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
                                        data.setKeyword(bc + "&" + q);
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


            System.out.println("- - - 统计开始- - - - - - - - - - - - - - - - - - - - ");
            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量-Map：" + listMap.size());

            int sum = listAll.stream().collect(Collectors.groupingBy(NoticeMQ::getContentid)).size();
            log.info("新去重数据量-sum：" + sum);

            if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("去重数据量-List：" + list.size());

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
                    futureList.add(executorService.submit(() -> pocDataFieldService.getZiDuanKu_ziTi(content)));
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
    public void getZheJiangShuangLue(Integer type, String date, String s, String name) {
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后的数据-map
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        try {
            List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:3 OR progid:5)  AND amountNumber:[50000 TO *] AND zhongBiaoUnit:*", "", 1);
            //List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:3 OR progid:5)  AND amountNumber:[50000 TO *] AND zhongRelationWay:* AND zhongBiaoUnit:*", "", 1);
            log.info("————" + mqEntities.size());
            if (!mqEntities.isEmpty()) {
                for (NoticeMQ data : mqEntities) {
                    if (data.getTitle() != null) {
                        boolean flag = true;
                        if (flag) {
                            data.setKeyword("");
                            listAll.add(data);
                            if (!dataMap.containsKey(data.getContentid().toString())) {
                                listMap.add(data);
                                dataMap.put(data.getContentid().toString(), "0");
                            }
                        }
                    }
                }
            }

            System.out.println("- - - 统计开始- - - - - - - - - - - - - - - - - - - - ");
            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量-Map：" + listMap.size());

            int sum = listAll.stream().collect(Collectors.groupingBy(NoticeMQ::getContentid)).size();
            log.info("新去重数据量-sum：" + sum);

            if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("去重数据量-List：" + list.size());

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
                    futureList.add(executorService.submit(() -> getZiDuanKu_ziTi_17(content)));
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
     * 青岛海尔生物医疗股份有限公司
     *
     * @param type
     * @param date
     * @param s
     * @param name
     */
    @Override
    public void getQingDaoHaiErShengWu(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        List<NoticeMQ> listMap = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        try {
            String[] jzc = {"医疗冷箱", "医疗冷柜", "医用冷箱", "医用冷柜", "医疗冰箱", "医疗冰柜", "医用冰箱", "医用冰柜", "医用冷藏箱", "医用冷藏柜", "医疗冷藏箱", "医疗冷藏柜", "医用冷冻箱", "医用冷冻柜", "医疗冷冻箱", "医疗冷冻柜", "医用冷藏冰箱", "医用冷藏冰柜", "医疗冷藏冰箱", "医疗冷藏冰柜", "医用冷冻冰箱", "医用冷冻冰柜", "医疗冷冻冰箱", "医疗冷冻冰柜", "超低温冷藏冰箱", "超低温冷藏冰柜", "超低温冰箱", "超低温冰柜", "超低温冷箱", "超低温冷柜", "超低温冷冻箱", "超低温冷冻柜", "超低温保存箱", "超低温保存柜 "};
            String[] kzc = {"冷冻箱", "冷冻柜", "冷藏箱", "冷藏冰箱", "低温保存箱", "低温保存柜", "低温冰箱", "低温冰柜", "低温冷箱", "低温冷柜"};
            String[] fzc = {"血站", "血液", "急救", "医疗", "疾控", "病防治", "疾病", "病预防", "卫生院", "保健", "疗养院", "康复", "医院", "生命", "医学", "卫生", "医科", "医工", "制药", "实验室", "药品", "临床", "生物", "疫苗", "药物", "防治", "实验所", "医用"};
            String[] blacks = {"维保", "保洁", "温度计", "系统", "车采购"};

            for (String str : jzc) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND allcontent:\"" + str + "\"", "", 1);
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

            for (String a : kzc) {
                for (String b : fzc) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND allcontent:\"" + a + "\"   AND zhaoBiaoUnit:\"" + b + "\"", "", 2);
                        log.info(a + "&" + b + "————" + mqEntities.size());
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
            for (String a : kzc) {
                for (String b : fzc) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND allcontent:\"" + a + "\"   AND title:\"" + b + "\"", "", 2);
                        log.info(a + "&" + b + "————" + mqEntities.size());
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


            System.out.println("- - - 统计开始- - - - - - - - - - - - - - - - - - - - ");
            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量-Map：" + listMap.size());

            int sum = listAll.stream().collect(Collectors.groupingBy(NoticeMQ::getContentid)).size();
            log.info("新去重数据量-sum：" + sum);

            if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("去重数据量-List：" + list.size());

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
                    futureList.add(executorService.submit(() -> pocDataFieldService.getZiDuanKu_ziTi(content)));
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
    public void getShenZhenHuaDaZhiZao(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        List<NoticeMQ> listMap = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        try {
            String[] aa = {"测序仪","基因测序仪","细菌全基因组测序仪","高通量基因测序系统","高通量测序仪","微生物宏基因二代测序仪","分子核酸测序系统","高通量测序平台","二代基因测序仪","基因分析仪","高通量二代测序仪","高通量基因测序仪","二代测序仪","新一代测序仪","高通量测序系统","高通量基因测序仪系统","产前诊断高通量测序仪","二代基因测序","DNA测序仪","全自动基因分析仪","第二代基因测序仪","基因测序仪升级","测序服务","全自动核酸提取仪","PCR方舱","PCR检测车","方舱核酸","核酸检测车","生物样本库","病原微生物鉴定  ","宏基因组 ","微生物基因组","新冠溯源","MGISP","MGISEQ","华大智造","自动化冷库"};
            String[] bb = {"海关","科学技术","医疗","大学","中学","小学","幼儿园","培训","学校","血站","急救中心","疾控中心","卫生院","疗养院","专科医院","中医院","综合医院","医疗服务"};

            String[] cc = {"分析加速软件-测序","分析加速器-测序","MGI-基因","MGI-测序"};
            //String[] blacks = {"维保", "保洁", "温度计", "系统", "车采购"};

            for (String a : aa) {
                for (String b : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 3]  AND allcontent:\"" + a + "\"   AND zhaoSecondIndustry:\"" + b + "\"", "", 2);
                        log.info(a + "&" + b + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    /*for (String black : blacks) {
                                        if (StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)) {
                                            flag = false;
                                            break;
                                        }
                                    }*/
                                    if (flag) {
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
                    }));
                }
            }
            for (String c : cc) {
                String[] split = c.split("-");
                for (String b : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 3]  AND (allcontent:\"" + split[0] + "\" AND allcontent:\"" + split[1] + "\")  AND zhaoSecondIndustry:\"" + b + "\"", "", 2);
                        log.info(a + "&" + b + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    /*for (String black : blacks) {
                                        if (StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)) {
                                            flag = false;
                                            break;
                                        }
                                    }*/
                                    if (flag) {
                                        data.setKeyword(c);
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


            System.out.println("- - - 统计开始- - - - - - - - - - - - - - - - - - - - ");
            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量-Map：" + listMap.size());

            int sum = listAll.stream().collect(Collectors.groupingBy(NoticeMQ::getContentid)).size();
            log.info("新去重数据量-sum：" + sum);

            if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }

            log.info("去重数据量-List：" + list.size());

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
                    futureList.add(executorService.submit(() -> pocDataFieldService.getZiDuanKu_ziTi(content)));
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
    public Map<String, Object> getArea(String regLocation, String company) {
        //172.18.30.13:8070/api/address?regLocation=北京北安时代电梯安装工程有限公司&company=
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(60000)
                    .setSocketTimeout(60000).setConnectTimeout(60000).build();
            //4、创建HttpGet请求
            HttpGet httpGet = new HttpGet("http://172.18.30.13:8070/api/address?regLocation=" + regLocation + "&company=" + company);
            httpGet.setConfig(requestConfig);
            CloseableHttpResponse response = client.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                log.info("企业名称company:{}", company);
                String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                if (StringUtils.isNotBlank(result)) {
                    JSONObject jsonObject = JSON.parseObject(result);
                    return MapUtil.getJsonObjToMap(jsonObject);
                }
            } else {
                log.info("企业名称company:{} 调用数据详情接口异常, 返回状态不是 200 ", company);
                throw new RuntimeException("调用数据详情接口异常，请联系管理员， 返回状态不是 200 ");
            }
        } catch (Exception e) {
            log.error("调用数据详情接口异常:{}, 获取不到详情数据", e);
        }
        return null;
    }

    @Override
    public void getWuRenJi(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所有数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后得到所有数据-map
        HashMap<String, String> dataMap = new HashMap<>();
        try {
            String[] keywords = {"无人机", "大疆", "飞行器", "航拍设备", "多旋翼", "无人驾驶飞行器", "无人飞行器", "无人飞机"};
            String[] blacks = {"建模", "撒药", "维修", "无人机生产项目", "网络课程", "操作培训", "植保", "打药", "飞防", "施药", "喷药", "农药", "统防", "维保"};
            for (String str : keywords) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND title:\"" + str + "\"", "", 1);
                    //List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:3 OR progid:5) AND newProvince:\"" + "江苏省" + "\"  AND title:\"" + str + "\"", "", 1);
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

            /*if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }*/

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + listMap.size());

            if ("1".equals(s) || "2".equals(s)) {
                currencyService.soutKeywords(listAll, listMap.size(), s, name, date);
            }

        } catch (Exception e) {
            e.getMessage();
        }
        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
            if (listMap != null && listMap.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(16);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : listMap) {
                    futureList.add(executorService.submit(() -> pocDataFieldService.getZiDuanKu_ziTi(content)));
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
    public void getShenZhenDaJiang(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所有数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后得到所有数据-map
        HashMap<String, String> dataMap = new HashMap<>();
        try {
            String[] aa = {"大疆"};
            String[] bb = {"无人机","航拍设备","无人驾驶飞行器","无人飞行器","无人飞机","航拍器","多旋翼无人机","航拍飞行器","无人机巡检","固定翼无人机","无人机电力巡检","无人机应急","无人机监控","无人机精细化巡检","无人机安防","垂直起降无人机","无人机反制","航拍无人机","航拍机"};
            String[] blacksA = {"建模","撒药","维修","无人机生产项目","网络课程","操作培训","植保","打药","飞防","施药","喷药","农药","统防","清运","库卡","装修","水泵","大疆创新中心","大疆天空之城","飞行器总装厂","无人机试验测试中心","无人机产业园","无人机实训室","无人机巡护监测指挥中心","飞行器研究所","药剂","无人机仿真实训室","无人机灭虫","培训","问题","餐饮","监理","升级改造","方法研究","飞行器学院","无人机检验站","作业车","手持云台","手机云台","无人机除外"};
            //String[] blacksC ={"无人机生产项目","飞行器总装厂","无人机试验测试中心","无人机产业园","无人机实训室","无人机巡护监测指挥中心","飞行器研究所","无人机仿真实训室","飞行器学院","无人机检验站","无人机除外"};

            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND title:\"" + str + "\"", "", 1);
                    log.info(str + "- - -" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                for (String black : blacksA) {
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
            for (String str : bb) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND allcontent:\"" + str + "\"", "", 2);
                    log.info(str + "- - -" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
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

            /*if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }*/

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + listMap.size());

            if ("1".equals(s) || "2".equals(s)) {
                currencyService.soutKeywords(listAll, listMap.size(), s, name, date);
            }

        } catch (Exception e) {
            e.getMessage();
        }
        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
            if (listMap != null && listMap.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(16);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : listMap) {
                    futureList.add(executorService.submit(() -> getKeyWordByIdAndSave(content)));
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
    public void getShenZhenDaJiangTongJi(Integer type, String date, String s, String name) {
        getWuRenJiZhuanYongSave();
    }

    @Override
    public void getZheJiangShuangLue2(Integer type, String date, String s, String name) {

        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所有数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后得到所有数据-map
        HashMap<String, String> dataMap = new HashMap<>();
        try {
            List<String> keywords = LogUtils.readRule("keyWords");
            for (String str : keywords) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:3 OR progid:5)  AND zhongRelationWay:* AND zhongBiaoUnit:\"" + str + "\"", str, 1);
                    log.info(str + "- - -" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
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

            /*if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }*/

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + listMap.size());

            if ("1".equals(s) || "2".equals(s)) {
                currencyService.soutKeywords(listAll, listMap.size(), s, name, date);
            }

        } catch (Exception e) {
            e.getMessage();
        }
        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
            if (listMap != null && listMap.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(16);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : listMap) {
                    futureList.add(executorService.submit(() -> pocDataFieldService.getZiDuanKu_ziTi(content)));
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
    public void getShangHaiHengSheng(Integer type, String date, String s, String name, String typeName) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所有数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后得到所有数据-map
        HashMap<String, String> dataMap = new HashMap<>();
        try {
            String[] keywords = {"北京新时空科技股份有限公司", "北京九恒星科技股份有限公司", "北京铜牛信息科技股份有限公司"};
            for (String str : keywords) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND blZhongBiaoUnit:\"" + str + "\"", str, 1);
                    log.info(str + "- - -" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
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
            for (String str : keywords) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND zhongBiaoUnit:\"" + str + "\"", str, 2);
                    log.info(str + "- - -" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
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
            for (String str : keywords) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND allcontent:\"" + str + "\"", str, 3);
                    log.info(str + "- - -" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
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

            /*if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }*/

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + listMap.size());

            if ("1".equals(s) || "2".equals(s)) {
                currencyService.soutKeywords(listAll, listMap.size(), s, name, date);
            }

        } catch (Exception e) {
            e.getMessage();
        }
        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
            if (listMap != null && listMap.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(16);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : listMap) {
                    futureList.add(executorService.submit(() -> pocDataFieldService.getZiDuanKu_ziTi(content)));
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
    public void getShangHaiHengShengById(Integer type) {
        //String[] list = {"224470080"};
        List<String> list =null;
        try {
           list  = LogUtils.readRule("idsFile");
        } catch (IOException e) {

        }
        //如果参数为1,则进行存表
        if (type == 1) { //自提接口
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(16);
                List<Future> futureList = new ArrayList<>();
                for (String str : list) {
                    NoticeMQ noticeMQ = new NoticeMQ();
                    noticeMQ.setContentid(Long.valueOf(str));
                    futureList.add(executorService.submit(() -> pocDataFieldService.getZiDuanKu_ziTi(noticeMQ)));
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
        } else {
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(16);
                List<Future> futureList = new ArrayList<>();
                for (String str : list) {
                    NoticeMQ noticeMQ = new NoticeMQ();
                    noticeMQ.setContentid(Long.valueOf(str));
                    futureList.add(executorService.submit(() -> pocDataFieldService.getZiDuanKu_hunHeBaiLian(noticeMQ)));
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
    public void getShangHaiHengSheng3(Integer type, String date, String s, String name, Integer typeName) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所有数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后得到所有数据-map
        HashMap<String, String> dataMap = new HashMap<>();
        try {
            List<String> keywords = LogUtils.readRule("keyWords");
            if (typeName == 1) { //规则一
                for (String str : keywords) {
                    futureList1.add(executorService1.submit(() -> {
                        //自提招标单位
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 2]   AND zhaoBiaoUnit:\"" + str + "\"", str, 1);
                        log.info(str + "- - -" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
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
            } else if (typeName == 2) { //规则二
                //全文
                for (String str : keywords) {
                    futureList1.add(executorService1.submit(() -> {
                        //自提招标单位
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:3 OR progid:5)  AND allcontent:\"" + str + "\"", str, 1);
                        log.info(str + "- - -" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
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
                //自提-中标单位
                for (String str : keywords) {
                    futureList1.add(executorService1.submit(() -> {
                        //自提招标单位
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND zhongBiaoUnit:\"" + str + "\"", str, 2);
                        log.info(str + "- - -" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
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
            } else if (typeName == 3) { //规则三

                //百炼-中标单位
                for (String str : keywords) {
                    futureList1.add(executorService1.submit(() -> {
                        //自提招标单位
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND blZhongBiaoUnit:\"" + str + "\"", str, 1);
                        log.info(str + "- - -" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    if (flag) {
                                        data.setKeyword(str);
                                        data.setKeywordTerm(data.getZhongBiaoUnit());//中标单位
                                        data.setKeywords(data.getBlzhongBiaoUnit());//百炼中标单位
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

            /*if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }*/

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + listMap.size());

            if ("1".equals(s) || "2".equals(s)) {
                currencyService.soutKeywords(listAll, listMap.size(), s, name, date);
            }

        } catch (Exception e) {
            e.getMessage();
        }
        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
            if (listMap != null && listMap.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(16);
                List<Future> futureList = new ArrayList<>();
                if (typeName == 1 || typeName == 2) {
                    for (NoticeMQ content : listMap) {
                        futureList.add(executorService.submit(() -> pocDataFieldService.getZiDuanKu_ziTi(content)));
                    }
                } else {
                    for (NoticeMQ content : listMap) {
                        futureList.add(executorService.submit(() -> pocDataFieldService.getZiDuanKu_hunHeBaiLian(content)));
                    }
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
    public void getQuChong() {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所有数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后得到所有数据-map
        HashMap<String, String> dataMap = new HashMap<>();
        try {
            futureList1.add(executorService1.submit(() -> {
                //自提招标单位
                List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[20210501 TO 20210531] AND segmentType:11 AND  amountUnit:* AND zhongBiaoUnit:*", "", 1);
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag) {
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


            if (listMap != null && listMap.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(16);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : listMap) {
                    futureList.add(executorService.submit(() -> pocDataFieldService.getZiDuanKu_ziTi(content)));
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


    }

    @Override
    public void getRunnable() {
        for (int i = 0; i < 100; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    doSomeThing();
                }
            }).start();
        }
    }
    public  int a =100;
    private Integer doSomeThing(){
        synchronized("1"){
            a --;
            System.out.println(a);
            return a;
        }
    }

    @Override
    public void getAoDiSiJiDian(Integer type, String date, String s, String name, Integer typeName) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所有数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后得到所有数据-map
        HashMap<String, String> dataMap = new HashMap<>();
        try {
            String[] keywords = {"地铁","公交场站","社区改造","机场","高铁站","改建项目","学校建设","图书馆","教学楼","新建商业","宿舍楼","博物馆","公寓","医院","综合枢纽","火车站","棚户区改造","轨道交通","安置房","住宅区改造","管制塔台","景区","小区","epc工程","酒店","人防工程","电梯","体育中心","万达广场","科技园","大厦公寓","体育场","住宅小区","电影院"};
            String[] blacks ={"天桥","共保共治","下水道","高速公路","桥梁","道路","路网","截污工程","输变电","平房","方案","报告书","生产线","大道","车间","迁改工程","道路建设","养殖基地","管廊工程"};
            for (String str : keywords) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = snContentSolr.companyResultsBaoXian("yyyymmdd:[20210501 TO 20210531] AND (catid:301 OR catid:601) AND progid:[31 TO 36]  AND title:\"" + str + "\"", str, 1);
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

            /*if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }*/

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + listMap.size());

            if ("1".equals(s) || "2".equals(s)) {
                currencyService.soutKeywords(listAll, listMap.size(), s, name, date);
            }

        } catch (Exception e) {
            e.getMessage();
        }
        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
            if (listMap != null && listMap.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(6);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : listMap) {
                    futureList.add(executorService.submit(() -> shenPiService.getDataFromZhongTaiAndSave(content)));
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
    public void getAoDiSiJiDianNzj(Integer type, String date, String s, String name, Integer typeName) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所有数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后得到所有数据-map
        HashMap<String, String> dataMap = new HashMap<>();
        try {
            String[] keywords = {"地铁","公交场站","社区改造","机场","高铁站","改建项目","学校建设","图书馆","教学楼","新建商业","宿舍楼","博物馆","公寓","医院","综合枢纽","火车站","棚户区改造","轨道交通","安置房","住宅区改造","管制塔台","景区","小区","epc工程","酒店","人防工程","电梯","体育中心","万达广场","科技园","大厦公寓","体育场","住宅小区","电影院"};
            String[] blacks ={"天桥","共保共治","下水道","高速公路","桥梁","道路","路网","截污工程","输变电","平房","方案","报告书","生产线","大道","车间","迁改工程","道路建设","养殖基地","管廊工程"};
            for (String str : keywords) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = snContentSolr.companyResultsBaoXian("yyyymmdd:[20210501 TO 20210531] AND catid:101 AND title:\"" + str + "\"", str, 1);
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

            /*if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }*/

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + listMap.size());

            if ("1".equals(s) || "2".equals(s)) {
                currencyService.soutKeywords(listAll, listMap.size(), s, name, date);
            }

        } catch (Exception e) {
            e.getMessage();
        }
        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
            if (listMap != null && listMap.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(16);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : listMap) {
                    futureList.add(executorService.submit(() -> niZaiJianService.guanWangData(content)));
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
    public void getShenZhenDaJiang4(Integer type, String date, String s, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所有数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后得到所有数据-map
        HashMap<String, String> dataMap = new HashMap<>();
        try {
            String[] aa = {"大疆","航拍"};
            String[] bb = {"无人"};
            String[] cc = {"反制","航空器","飞行平台"};

            String[] aKeyWords = {"无人机","航拍设备","无人驾驶飞行器","无人飞行器","无人飞机","航拍器","多旋翼无人机","航拍飞行器","无人机巡检","固定翼无人机","无人机电力巡检","无人机应急","无人机监控","无人机精细化巡检","无人机安防","垂直起降无人机","无人机反制","航拍无人机","航拍机","无人驾驶飞机","无人驾驶航空器","无人飞","无人航","无人直","无人侦","反制枪","飞行平台","翼飞行器"};
            String[] titleBlack ={"维修","网络课程","清运","库卡","装修","水泵","大疆创新中心","大疆天空之城","飞行器总装厂","无人机试验测试中心","无人机产业园","无人机实训室","无人机巡护监测指挥中心","飞行器研究所","药剂","无人机仿真实训室","无人机灭虫","培训","问题","餐饮","监理","方法研究","飞行器学院","无人机检验站","作业车","手持云台","手机云台","无人机除外","图书","LED","航拍机位","无人机除外","水表","天平"};
            String[] allConBlack = {"无人机除外","水表","天平"};

            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND title:\"" + str + "\"", "", 1);
                    log.info(str + "- - -" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                for (String black : titleBlack) {
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
                for (String c : cc) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND title:\"" + b + "\"  AND title:\"" + c + "\"", b + "&" + c, 1);
                        log.info(b + "&" + c + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    for (String black : titleBlack) {
                                        if (StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)) {
                                            flag = false;
                                            break;
                                        }
                                    }
                                    if (flag) {
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
                    }));
                }
            }

            //全文关键词a
            for (String str : aKeyWords) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (progid:[0 TO 3] OR progid:5)  AND allcontent:\"" + str + "\"", "", 2);
                    log.info(str + "- - -" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                for (String black : titleBlack) {
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

            /*if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }*/

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + listMap.size());

            if ("1".equals(s) || "2".equals(s)) {
                currencyService.soutKeywords(listAll, listMap.size(), s, name, date);
            }

        } catch (Exception e) {
            e.getMessage();
        }
        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
            if (listMap != null && listMap.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(16);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : listMap) {
                    futureList.add(executorService.submit(() -> getKeyWordByIdAndSave4(content)));
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
    public void getAoDiSiJiDianNzj2(Integer type, String date, String s, String name, Integer typeName) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所有数据
        List<NoticeMQ> listMap = new ArrayList<>();//去重后得到所有数据-map
        HashMap<String, String> dataMap = new HashMap<>();
        try {
            String[] keywords = {"地铁","公交场站","社区改造","机场","高铁站","改建项目","学校建设","图书馆","教学楼","新建商业","宿舍楼","博物馆","公寓","医院","综合枢纽","火车站","棚户区改造","轨道交通","安置房","住宅区改造","管制塔台","景区","小区","epc工程","酒店","人防工程","电梯","体育中心","万达广场","科技园","大厦公寓","体育场","住宅小区","电影院"};
            String[] blacks ={"天桥","共保共治","下水道","高速公路","桥梁","道路","路网","截污工程","输变电","平房","方案","报告书","生产线","大道","车间","迁改工程","道路建设","养殖基地","管廊工程"};
            for (String str : keywords) {
                futureList1.add(executorService1.submit(() -> {
                    //自提招标单位
                    List<NoticeMQ> mqEntities = snContentSolr.companyResultsBaoXian("yyyymmdd:[20210501 TO 20210531] AND catid:101 AND title:\"" + str + "\"", str, 1);
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

            /*if (listAll.size() > 0) {
                list.addAll(currencyService.getNoticeMqList(listAll));
            }*/

            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + listMap.size());

            /*if ("1".equals(s) || "2".equals(s)) {
                currencyService.soutKeywords(listAll, listMap.size(), s, name, date);
            }*/
            if ("1".equals(s) || "2".equals(s)) {
                List<String> sList = StrUtil.strListToList(keywords);
                currencyService.getNewTypeReadFile(s,name,sList,listAll,listMap.size(),date);
            }


        } catch (Exception e) {
            e.getMessage();
        }
        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
            if (listMap != null && listMap.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(16);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : listMap) {
                    futureList.add(executorService.submit(() -> niZaiJianService.getNiZaiJianData(content)));
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
    public void getBiaozhun(Integer type) {
        List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("select info_id,user_id from han_xs_dje where user_id =?",type);
        if (mapList != null && mapList.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(16);
            List<Future> futureList = new ArrayList<>();
            for (Map m : mapList) {
                NoticeMQ noticeMQ = new NoticeMQ();
                noticeMQ.setContentid(Long.valueOf(m.get("info_id").toString()));
                futureList.add(executorService.submit(() -> pocDataFieldService.getZiDuanKu_ziTi(noticeMQ)));
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


    /**
     * 单独封装使用-贝朗医疗
     *
     * @param noticeMQ
     */

    private void getKeyWordByIdAndSave3(NoticeMQ noticeMQ) {
        String[] keyWords = {"手术显微镜", "医用显微镜", "自动扫描显微镜", "电子阴道显微镜", "显微注射用显微镜", "根管显微镜", "医用专用显微镜", "偏光显微镜", "电子显微镜", "原子力显微镜", "红外显微镜", "拉曼显微镜", "金相显微镜", " 医用专用显微镜", "包裹体显微镜", "体视显微镜", "连续变倍体视显微镜", "光片显微镜", "视频显微镜", "口腔显微镜", "显微镜及配件", "大视野显微镜", "读数显微镜", "智能显微镜", "手持显微镜", "显微镜（带ccd", "内窥显微镜 ", "解剖显微镜", "卧式显微镜", "立式显微镜", "电子扫描显微镜", "300g显微镜", "单人显微镜 ", "外科显微镜", "实验室显微镜", "显微镜及成像系统", "三目显微镜", "光学显微镜物镜", "多头显微镜", "进口显微镜 ", "三维显微镜", "双管显微镜", "多媒体显微镜", "材料显微镜", "成像显微镜", "一体化显微镜", "相衬显微镜", "显微镜等多种", "显微镜等国产", "红外显微镜 ", "光子显微镜", "多功能显微镜", "教师显微镜", "科研级显微镜", "隧道显微镜", "立体显微镜", "研究型显微镜", "光学仪器/显微镜", "偏振显微镜", "光纤显微镜", "pentero", "Kinevo"};
        String[] pbc = {"数码显微镜", "生物显微镜", "荧光显微镜", "正置显微镜", "眼科显微镜", "聚焦显微镜", "双目显微镜", "照明显微镜", "裂隙灯显微镜", "角膜内皮显微镜", "体式显微镜", "倒置显微镜", "相差显微镜", "高倍显微镜", "眼科手术显微镜", "体视显微镜", "荧光手术显微镜", "生物手术显微镜", "正置手术显微镜", "聚焦手术显微镜", "双目手术显微镜", "照明手术显微镜", "倒置手术显微镜", "相差手术显微镜", "高倍手术显微镜", "体视手术显微镜"};
        boolean b = cusDataNewService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        //全部自提，不需要正文
        Map<String, Object> resultMap = pocDataFieldService.getFieldsWithZiTi(noticeMQ, noticeMQ.getContentid().toString());
        if (resultMap != null) {
            String content = cusDataNewService.getContent(noticeMQ);//获取正文字段
            String title = resultMap.get("title").toString();//标题
            content = content + "&" + title;

            String keyWord = "";//追加正文、标题

            for (String p : pbc) {
                if (content.contains(p)) {
                    content = content.replaceAll(p, "");
                }
            }
            // 进行匹配关键词操作
            if (keyWords != null && keyWords.length > 0) {
                for (String kw : keyWords) {
                    if (content.toUpperCase().contains(kw.toUpperCase())) {
                        keyWord = kw;
                        break;
                    }
                }
                if (StringUtils.isNotBlank(keyWord)) {
                    resultMap.put("keywords", keyWord);
                    pocDataFieldService.saveIntoMysql(resultMap, noticeMQ.getContentid().toString());
                    log.info("追加关键词入库成功，infoId:{}", noticeMQ.getContentid());
                } else {
                    log.info("关键词数据不存在，infoId:{}", noticeMQ.getContentid());
                }
            }
        }

    }

    private void getKeyWordByIdAndSave2(NoticeMQ noticeMQ) {
        String[] keyWords = {};
        String[] blacks = {"清洗剂", "废碎玻璃", "清洗机", "清洗台", "清洗干燥机", "清洗加工", "清洗液", "清洁器", "清洁液", "清洁剂", "清洁工具", "不含外墙清洗", "不包括外墙清洁", "汽车维修"};

        boolean b = cusDataNewService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        //全部自提，不需要正文
        Map<String, Object> resultMap = pocDataFieldService.getFieldsWithZiTi(noticeMQ, noticeMQ.getContentid().toString());
        if (resultMap != null) {
            String content = cusDataNewService.getContent(noticeMQ);//获取正文字段
            String title = resultMap.get("title").toString();//标题

            content = content + "&" + title;

            String keyWord = "";//追加正文、标题
            String blk = "";//追加黑词
            // 进行匹配关键词操作
            if (keyWords != null && keyWords.length > 0) {
                for (String kw : keyWords) {
                    if (content.toUpperCase().contains(kw.toUpperCase())) {
                        keyWord = kw;
                        break;
                    }
                }
                //去掉最后一个字符
                //resultMap.put("keywords", keyWord);
                //pocDataFieldService.saveIntoMysql(resultMap, noticeMQ.getContentid().toString());
                //log.info("追加关键词入库成功，infoId:{}", noticeMQ.getContentid());
            }

            if (blacks != null && blacks.length > 0) {
                for (String black : blacks) {
                    if (content.toUpperCase().contains(black.toUpperCase())) {
                        blk += black + ",";
                    }
                }
                if (StringUtils.isNotBlank(blk)) {
                    blk = blk.substring(0, blk.length() - 1);
                }
                resultMap.put("keywords", blk);
                pocDataFieldService.saveIntoMysql(resultMap, noticeMQ.getContentid().toString());
                log.info("追加关键词入库成功，infoId:{}", noticeMQ.getContentid());
            }

        }

    }

    private void getKeyWordByIdAndSave(NoticeMQ noticeMQ) {
        String[] bb = {"无人机", "航拍设备", "无人驾驶飞行器", "无人飞行器", "无人飞机", "航拍器", "多旋翼无人机", "航拍飞行器", "无人机巡检", "固定翼无人机", "无人机电力巡检", "无人机应急", "无人机监控", "无人机精细化巡检", "无人机安防", "垂直起降无人机", "无人机反制", "航拍无人机", "航拍机"};
        String[] blacksB = {"撒药", "植保", "打药", "飞防", "施药", "喷药", "农药", "统防", "灭虫"};

        //关键词b + 黑词b
        List<String> bList = new ArrayList<>();
        for (String s1 : bb) {
            for (String s2 : blacksB) {
                bList.add(s1 + s2);
            }
        }

        // 黑词b + 关键词b
        for (String s2 : blacksB) {
            for (String s1 : bb) {
                bList.add(s2 + s1);
            }
        }

       /* boolean b = cusDataNewService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }*/
        //全部自提，不需要正文
        Map<String, Object> resultMap = pocDataFieldService.getFieldsWithZiTi(noticeMQ, noticeMQ.getContentid().toString());
        if (resultMap != null) {
            //if (noticeMQ.getTaskId().intValue() == 1){
            //    pocDataFieldService.saveIntoMysql(resultMap, noticeMQ.getContentid().toString());
            //    log.info("追加关键词入库成功，infoId:{}", noticeMQ.getContentid());
            //}else {
                String content = cusDataNewService.getContent(noticeMQ);//获取正文字段
                String title = resultMap.get("title").toString();//标题

                content = content + "&" + title;
                boolean flag = true;
                // 进行匹配关键词操作
                for (String kw : bList) {
                    //if (content.toUpperCase().contains(kw.toUpperCase())) {
                    if (content.contains(kw)) {
                        flag = false;
                        break;
                    }
                }
                //去掉最后一个字符
                //resultMap.put("keyword", keyWord);
                if (flag) {
                    pocDataFieldService.saveIntoMysql(resultMap, noticeMQ.getContentid().toString());
                    log.info("追加关键词入库成功，infoId:{}", noticeMQ.getContentid());
                }
           // }
        }

    }
    private void getKeyWordByIdAndSave4(NoticeMQ noticeMQ) {
        String[] aKeyWords = {"无人机","航拍设备","无人驾驶飞行器","无人飞行器","无人飞机","航拍器","多旋翼无人机","航拍飞行器","无人机巡检","固定翼无人机","无人机电力巡检","无人机应急","无人机监控","无人机精细化巡检","无人机安防","垂直起降无人机","无人机反制","航拍无人机","航拍机","无人驾驶飞机","无人驾驶航空器","无人飞","无人航","无人直","无人侦","反制枪","飞行平台","翼飞行器"};
        String[] allConBlack = {"无人机除外","水表","天平"};

       /* boolean b = cusDataNewService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }*/
        //全部自提，不需要正文
        Map<String, Object> resultMap = pocDataFieldService.getFieldsWithZiTi(noticeMQ, noticeMQ.getContentid().toString());
        if (resultMap != null) {
            //if (noticeMQ.getTaskId().intValue() == 1){
            //    pocDataFieldService.saveIntoMysql(resultMap, noticeMQ.getContentid().toString());
            //    log.info("追加关键词入库成功，infoId:{}", noticeMQ.getContentid());
            //}else {
                //String content = cusDataNewService.getContent(noticeMQ);//获取正文字段
                String content = resultMap.get("content").toString();
                String title = resultMap.get("title").toString();//标题

                content = content + "&" + title;

                String keyWord ="";
                boolean flag = true;
                // 进行匹配关键词操作
               /* for (String k : aKeyWords) {
                    if (content.contains(k)) {
                        keyWord = k;
                        break;
                    }
                }*/
                if (allConBlack !=null && allConBlack.length > 0){
                    for (String black : allConBlack) {
                        if(content.contains(black)){
                           flag = false;
                            break;
                        }
                    }
                }
                //resultMap.put("keyword", keyWord);
                if (flag) {
                    pocDataFieldService.saveIntoMysql(resultMap, noticeMQ.getContentid().toString());
                    log.info("追加关键词入库成功，infoId:{}", noticeMQ.getContentid());
                }
           // }
        }

    }

    private void getWuRenJiZhuanYongSave() {
        AtomicInteger wrj = new AtomicInteger(0);//无人机
        AtomicInteger hpsb = new AtomicInteger(0);//航拍设备
        AtomicInteger wrjsfxq = new AtomicInteger(0);//无人驾驶飞行器
        AtomicInteger wrfxq = new AtomicInteger(0);//无人飞行器
        AtomicInteger wrfj = new AtomicInteger(0);//无人飞机
        AtomicInteger hpq = new AtomicInteger(0);//航拍器
        AtomicInteger dxywrj = new AtomicInteger(0);//多旋翼无人机
        AtomicInteger hpfxq = new AtomicInteger(0);//航拍飞行器
        AtomicInteger wrjxj = new AtomicInteger(0);//无人机巡检
        AtomicInteger gdywrj = new AtomicInteger(0);//固定翼无人机
        AtomicInteger wrjdlxj = new AtomicInteger(0);//无人机电力巡检
        AtomicInteger wrjyj = new AtomicInteger(0);//无人机应急
        AtomicInteger wrjjk = new AtomicInteger(0);//无人机监控
        AtomicInteger jxhxj = new AtomicInteger(0);//无人机精细化巡检
        AtomicInteger wrjaf = new AtomicInteger(0);//无人机安防
        AtomicInteger qjwrj = new AtomicInteger(0);//垂直起降无人机
        AtomicInteger wrjfz = new AtomicInteger(0);//无人机反制
        AtomicInteger hpwrj = new AtomicInteger(0);//航拍无人机
        AtomicInteger hpj = new AtomicInteger(0);//航拍机


        String[] bb = {"无人机", "航拍设备", "无人驾驶飞行器", "无人飞行器", "无人飞机", "航拍器", "多旋翼无人机", "航拍飞行器", "无人机巡检", "固定翼无人机", "无人机电力巡检", "无人机应急", "无人机监控", "无人机精细化巡检", "无人机安防", "垂直起降无人机", "无人机反制", "航拍无人机", "航拍机"};
        ExecutorService executorService = Executors.newFixedThreadPool(30);
        List<Future> futureList = new ArrayList<>();
        try {
            List<String> list = LogUtils.readRule("idsFile");
            if (list != null && list.size() > 0) {
                for (String id : list) {
                    futureList.add(executorService.submit(() -> {
                        NoticeMQ noticeMQ = new NoticeMQ();
                        noticeMQ.setContentid(Long.valueOf(id));
                    /*boolean b = cusDataNewService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
                    if (!b) {
                        log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
                        return;
                    }*/
                        //全部自提，不需要正文
                        Map<String, Object> resultMap = pocDataFieldService.getFieldsWithZiTi(noticeMQ, noticeMQ.getContentid().toString());
                        if (resultMap != null) {
                            String content = cusDataNewService.getContent(noticeMQ);//获取正文字段
                            String title = resultMap.get("title").toString();//标题

                            content = content + "&" + title;
                            boolean flag = true;
                            if (flag) {
                                if (content.contains(bb[0])) {
                                    wrj.getAndAdd(1);
                                }
                                if (content.contains(bb[1])) {
                                    hpsb.getAndAdd(1);
                                }
                                if (content.contains(bb[2])) {
                                    wrjsfxq.getAndAdd(1);
                                }
                                if (content.contains(bb[3])) {
                                    wrfxq.getAndAdd(1);
                                }
                                if (content.contains(bb[4])) {
                                    wrfj.getAndAdd(1);
                                }
                                if (content.contains(bb[5])) {
                                    hpq.getAndAdd(1);
                                }
                                if (content.contains(bb[6])) {
                                    dxywrj.getAndAdd(1);
                                }
                                if (content.contains(bb[7])) {
                                    hpfxq.getAndAdd(1);
                                }
                                if (content.contains(bb[8])) {
                                    wrjxj.getAndAdd(1);
                                }
                                if (content.contains(bb[9])) {
                                    gdywrj.getAndAdd(1);
                                }
                                if (content.contains(bb[10])) {
                                    wrjdlxj.getAndAdd(1);
                                }
                                if (content.contains(bb[11])) {
                                    wrjyj.getAndAdd(1);
                                }
                                if (content.contains(bb[12])) {
                                    wrjjk.getAndAdd(1);
                                }
                                if (content.contains(bb[13])) {
                                    jxhxj.getAndAdd(1);
                                }
                                if (content.contains(bb[14])) {
                                    wrjaf.getAndAdd(1);
                                }
                                if (content.contains(bb[15])) {
                                    qjwrj.getAndAdd(1);
                                }
                                if (content.contains(bb[16])) {
                                    wrjfz.getAndAdd(1);
                                }
                                if (content.contains(bb[17])) {
                                    hpwrj.getAndAdd(1);
                                }
                                if (content.contains(bb[18])) {
                                    hpj.getAndAdd(1);
                                }
                                log.info("数据量统计，数据id为：{}", id);
                                //pocDataFieldService.saveIntoMysql(resultMap, noticeMQ.getContentid().toString());
                                //log.info("追加关键词入库成功，infoId:{}", noticeMQ.getContentid());
                            }
                        }
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

                log.info("无人机:{}", wrj);
                log.info("航拍设备:{}", hpsb);
                log.info("无人驾驶飞行器:{}", wrjsfxq);
                log.info("无人飞行器:{}", wrfxq);
                log.info("无人飞机:{}", wrfj);
                log.info("航拍器:{}", hpq);
                log.info("多旋翼无人机:{}", dxywrj);
                log.info("航拍飞行器:{}", hpfxq);
                log.info("无人机巡检:{}", wrjxj);
                log.info("固定翼无人机:{}", gdywrj);
                log.info("无人机电力巡检:{}", wrjdlxj);
                log.info("无人机应急:{}", wrjyj);
                log.info("无人机监控:{}", wrjjk);
                log.info("无人机精细化巡检:{}", jxhxj);
                log.info("无人机安防:{}", wrjaf);
                log.info("垂直起降无人机{}", qjwrj);
                log.info("无人机反制:{}", wrjfz);
                log.info("航拍无人机:{}", hpwrj);
                log.info("航拍机:{}", hpj);

                Map<String, Object> map = new HashMap<>();
                map.put("无人机:", wrj);
                map.put("航拍设备:", hpsb);
                map.put("无人驾驶飞行器:", wrjsfxq);
                map.put("无人飞行器:", wrfxq);
                map.put("无人飞机:", wrfj);
                map.put("航拍器:", hpq);
                map.put("多旋翼无人机:", dxywrj);
                map.put("航拍飞行器:", hpfxq);
                map.put("无人机巡检:", wrjxj);
                map.put("固定翼无人机:", gdywrj);
                map.put("无人机电力巡检:", wrjdlxj);
                map.put("无人机应急:", wrjyj);
                map.put("无人机监控:", wrjjk);
                map.put("无人机精细化巡检:", jxhxj);
                map.put("无人机安防:", wrjaf);
                map.put("垂直起降无人机", qjwrj);
                map.put("无人机反制:", wrjfz);
                map.put("航拍无人机:", hpwrj);
                map.put("航拍机:", hpj);
                currencyService.readFileByMapObj("招标数据-23", map, "2021");
            }
        } catch (IOException e) {
            e.getMessage();
        }

    }

    private void getZhongTaiDatasAndSave(NoticeMQ noticeMQ) {
        boolean bl = cusDataNewService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!bl) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        //调用中台接口，全部自提
        Map<String, Object> resultMap = pocDataFieldService.getFieldsWithZiTi(noticeMQ, String.valueOf(noticeMQ.getContentid()));
        //Map<String, Object> resultMap = pocDataFieldService.getFieldsWithHunHe(noticeMQ, String.valueOf(noticeMQ.getContentid()));//混合百炼
       /* String[] aa = {"膜结构", "钢结构"};
        String[] bb = {"遮阳棚", "雨棚", "停车棚", "张拉膜", "春秋棚", "冬暖棚", "钢结构工程", "大棚", "扣棚", "张拉膜", "钢结构建设", "膜结构工程", "膜结构建设", "密闭罩", "顶面工程", "停车场工程", "遮阳网", "生产棚", "钢结构改造工程", "拉伸膜", "景观棚", "钢结构建设"};
        String[] pbc = {"钢结构工程有限公司", "钢结构有限公司", "膜结构工程有限公司", "膜结构有限公司"};
        if (resultMap != null) {
            String content = cusDataNewService.getContent(noticeMQ);//获取正文字段
            String title = resultMap.get("title").toString();
            content = content + "&" + title;

            String str = "";//正文
            for (String p : pbc) {
                if (content.contains(p)) {
                    str = content.replaceAll(p, "");
                }
            }

            String keyword = "";
            boolean kb = false;
            out:
            for (String a : aa) {
                for (String b : bb) {
                    if (str.contains(a) && str.contains(b)) {
                        kb = true;
                        keyword = a + "&" + b;
                        break out;
                    }
                }
            }
            if (kb) {
                resultMap.put("keyword", keyword);
                pocDataFieldService.saveIntoMysql(resultMap, String.valueOf(resultMap.get("content_id")));
                log.info("进行入库操作，contentId:{}", resultMap.get("content_id").toString());
            }*/
        if (resultMap != null) {
            pocDataFieldService.saveIntoMysql(resultMap, String.valueOf(resultMap.get("content_id")));
            log.info("进行入库操作，contentId:{}", resultMap.get("content_id").toString());
        }

    }


    /**
     * 调用天眼查接口 获取对应字段
     *
     * @param company
     * @return
     */
    public static JSONObject searchCreditCode(String company) {
        try {
            if (StringUtils.isBlank(company)) {
                return null;
            }
            //删除特殊字符
            company = company.replaceAll("\\\\", "");
            company = company.replaceAll("\\/", "");
            // 根据地址获取请求
            HttpGet request = new HttpGet("http://qly-data.qianlima.com/qianliyan/task/name/" + URLEncoder.encode(company, "utf-8"));
            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(60000)
                    .setSocketTimeout(60000).setConnectTimeout(60000).build();
            request.setConfig(requestConfig);
            HttpClient httpClient = HttpClients.createDefault();
            // 通过请求对象获取响应对象
            HttpResponse response = httpClient.execute(request);
            // 判断网络连接状态码是否正常(0--200都数正常)
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String creditCode = "";
                String entity = EntityUtils.toString(response.getEntity(), "utf-8");
                if (StringUtils.isNotBlank(entity)) {
                    JSONObject object = JSON.parseObject(entity);
                    if (object != null) {
                        String code = object.getString("code");
                        if ("0".equals(code)) {
                            if (object.getString("result") != null) {
                                JSONArray result = object.getJSONArray("result");
                                if (result != null && result.size() > 0) {
                                    JSONObject jsonObject = result.getJSONObject(0);
                                    return jsonObject;
                                }
                            }
                        }
                    }
                }
                return null;
            } else {
                log.error("调用天眼查接口报错 company:{}, 返回httpcode: {}", company, response.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            log.error("通过单位名称去千里眼查找 统一社会信用代码失败， 失败原因：{} ", e);
        }
        return null;
    }

    public void getZiDuanKu_ziTi_17(NoticeMQ noticeMQ) {
        boolean bl = pocDataFieldService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!bl) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        //调用中台接口，全部自提
        Map<String, Object> resultMap = pocDataFieldService.getFieldsWithZiTi(noticeMQ, String.valueOf(noticeMQ.getContentid()));
        if (resultMap != null) {
            pocDataFieldService.saveIntoMysql_17(resultMap, String.valueOf(resultMap.get("content_id")));
            log.info("进行入库操作，contentId:{}", resultMap.get("content_id").toString());
        }
    }
}