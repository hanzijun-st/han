package com.qianlima.offline.service;


import com.qianlima.offline.bean.Area;
import com.qianlima.offline.bean.ConstantBean;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.middleground.BaiLianZhongTaiService;
import com.qianlima.offline.middleground.NewZhongTaiService;
import com.qianlima.offline.middleground.NotBaiLianZhongTaiService;
import com.qianlima.offline.middleground.ZhongTaiService;
import com.qianlima.offline.rule02.MyRuleUtils;
import com.qianlima.offline.util.ContentSolr;
import com.qianlima.offline.util.IctContentSolr;
import com.qianlima.offline.util.LogUtils;
import com.qianlima.offline.util.MathUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Slf4j
public class PocService2 {

    @Autowired
    private ContentSolr contentSolr;

    @Autowired
    private IctContentSolr ictContentSolr;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;

    @Autowired
    private NotBaiLianZhongTaiService notBaiLianZhongTaiService;

    @Autowired
    private ZhongTaiBiaoDiWuServiceForOne zhongTaiBiaoDiWuServiceForOne;

    @Autowired
    private NewZhongTaiService newZhongTaiService;

    @Autowired
    private ZhongTaiService zhongTaiService;

    @Autowired
    private BaiLianZhongTaiService baiLianZhongTaiService;

    @Autowired
    private MyRuleUtils myRuleUtils;

    @Autowired
    private ZhongTaiBiaoDiWuService zhongTaiBiaoDiWuService;


    HashMap<Integer, Area> areaMap = new HashMap<>();

    //地区
    @PostConstruct
    public void init() {
        List<Map<String, Object>> maps = gwJdbcTemplate.queryForList("SELECT * FROM phpcms_area");
        for (Map<String, Object> map : maps) {
            Area area = new Area();
            area.setAreaid(Integer.valueOf(map.get("areaid").toString()));
            area.setName(map.get("name").toString());
            area.setParentid(map.get("parentid").toString());
            area.setArrparentid(map.get("arrparentid").toString());
            areaMap.put(Integer.valueOf(map.get("areaid").toString()), area);
        }
    }

    String ids[] = {"182001854","182296542","191670686","181982147","191595470","192746693","183769738","191670418","191670593"};

    //调取中台数据
    public void getDataFromZhongTaiAndSave(NoticeMQ noticeMQ) {
        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
        if (resultMap != null) {
            String contentInfo = resultMap.get("content").toString();
            String content = processAboutContent(contentInfo);
            if (StringUtils.isNotBlank(content)) {
                resultMap.put("content", content);
            }
            newZhongTaiService.saveIntoMysql(resultMap);
        }
    }

    // 只去除<a>标签所有信息
    public static String processAboutContent(String content) {
        Document document = Jsoup.parse(content);
        Elements elements = document.select("a[href]");
        Integer elementSize = elements.size();
        for (Integer i = 0; i < elementSize; i++) {
            Element element = elements.get(i);
            if (element == null || document.select("a[href]") == null || document.select("a[href]").size() == 0) {
                break;
            }
            if (StringUtils.isNotBlank(element.attr("href"))) {
                if (element.is("a")) {
                    element.remove();
                }
            }
        }
        return document.body().html();
    }

    //调取中台数据 二次 进行对比数据
    public void getDataFromZhongTaiAndSave2(NoticeMQ noticeMQ) {

        String[] aaa = {"联通","联合网络通信","联合通信"};

        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
        if (resultMap != null) {
//            String zhao_biao_unit = resultMap.get("zhao_biao_unit").toString();
            String zhong_biao_unit = resultMap.get("zhong_biao_unit").toString();
            String keyword = "";
            for (String aa : aaa) {
                //精确用.equals(aa)  模糊用.contains(aa)
                if (zhong_biao_unit.contains(aa)){
                    keyword += (aa + "、");
                }
            }
            if (StringUtils.isNotBlank(keyword)) {
                keyword = keyword.substring(0, keyword.length() - 1);
                resultMap.put("keyword", keyword);
            }
            newZhongTaiService.saveIntoMysql(resultMap);
        }
    }

    //调取中台数据 多个关键词后追加(包含黑词) && 匹配行业标签 && 二次进行对比数据(招标单位包含关键词)
    public void getDataFromZhongTaiAndSave3(NoticeMQ noticeMQ) throws IOException {

        String[] aaa = {"医","护理","护士","整形","健康","诊治","急救","治疗"};
        String[] bbb = {"内镜","会诊","诊断","胶片","超声","医共体","医联体","医疗云","云医院","云医疗","医院云","PACS","公共卫生","集成平台","智慧医院","远程医疗","远程会诊","远程诊疗","电子病历","区域医疗","医学影像","预约系统","影像系统","叫号系统","预约平台","叫号平台","人工智能","医疗上云","超声系统","挂号平台","挂号系统","在线挂号","数字医疗","诊断系统","医联预约","医疗信息化","医院信息化","数字化医疗","医院智能化","智能化医疗","健康医疗云","医疗云服务","医疗云平台","信息化咨询","信息化项目","信息化建设","信息化系统","信息化平台","大超声系统","挂号app","医疗数字化","智慧云医院","智能云系统","信息安全测评","医疗人工智能","人工智能医疗","大数据云医疗","远程医疗协作","远程服务系统","超声诊断系统","彩银超声系统","远程超声系统","远程会诊系统","微信预约挂号","预约门诊挂号","预约挂号系统","预约挂号平台","微信挂号平台","网上挂号平台","挂号预约平台","微医预约挂号","挂号网上预约","医疗挂号软件","网上挂号预约","网上预约挂号","网络预约挂号","医疗叫号系统","排队叫号系统","叫号排队系统","人工智能应用","人工智能平台","人工智能产品","内镜治疗系统","内镜清洗系统","预约分诊系统","医联预约平台","预约管理系统","集中预约系统","网上预约医生","网上预约专家","预约转诊接口","医疗信息化改造","医疗信息化建设","数字化放射成像","医院智能化建设","医疗影像云存储","智医助理信息化","信息化管理平台","信息化应用系统","超声微探头系统","人工智能与医疗","智慧云医院服务","智慧云平台建设","影像云平台服务","智能化系统研发","智能化系统项目","智能化系统建设","智能化系统集成","智能化系统工程","智能化升级建设","智能化升级改造","智能化建设项目","智能化建设系统","智能化建设采购","智能化管理系统","智能化工程建设","智能化改造项目","智能化安装项目","影像传输与归纳","超声骨科手术系统","容积成像超声系统","远程会诊咨询系统","药房取药叫号系统","叫号系统在线询价","人工智能导诊系统","人工智能网络安全","医院信息管理系统","智能诊断系统硬件","中医综合诊断系统","耳鼻喉科内镜系统","预约诊疗服务系统","医技集中预约系统","预约管理信息系统","服务预约管理系统","“智医助理”信息化","信息化管理服务平台","超声切割止血刀系统","多普勒超声诊断系统","教学诊断与改进平台","诊断与改进信息化平台","医学影像管理诊断软件","超声光散射乳腺诊断系统","多重PCR分子诊断系统","影像诊断结构化报告系统","便携式彩超多普勒超声系统","人工智能AI辅助诊查系统","全自动快速分子诊断检测系统","结核分枝杆菌复合群快速诊断系统"};
        String[] ccc = {"远程","信息化","云平台","智能"};
        String[] cc1 = {"数字化"};
        String[] cc2 = {"医疗"};
        List<String> blacks = LogUtils.readRule("smf");

        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false){
            log.info("contentid:{} 对应的数据状态不是99, 丢弃" , noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
        if (resultMap != null) {
            String title = resultMap.get("title") != null ? resultMap.get("title").toString() : "";
            String content = resultMap.get("content") != null ? resultMap.get("content").toString() : "";
            content = title + "&" + content;
            content = content.toUpperCase();
            String keyword = "";
            boolean flag = true;
            for (String black : blacks) {
                if(StringUtils.isNotBlank(title) && title.contains(black)){
                    flag = false;
                    break;
                }
            }
            if (flag){
                for (String bb : bbb) {
                    if (content.contains(bb)) {
                        keyword += (bb + "、");
                    }
                }
                for (String cc : ccc) {
                    if (title.contains(cc)) {
                        keyword += (cc + "、");
                    }
                }
                for (String c1 : cc1) {
                    for (String c2 : cc2) {
                        if (title.contains(c1) && title.contains(c2)) {
                            keyword += (c1 + "&" + c2 + "、");
                        }
                    }
                }
            }
            if (StringUtils.isNotBlank(keyword)) {
                keyword = keyword.substring(0, keyword.length() - 1);
                resultMap.put("keyword", keyword);
            }

            String zhaobiaounit = resultMap.get("zhao_biao_unit") != null ? resultMap.get("zhao_biao_unit").toString() : "";
            String zhaobiaoindustry = myRuleUtils.getIndustry(zhaobiaounit);
            String[] zhaobiaosplit = zhaobiaoindustry.split("-");
            String[] hhy = {"医疗","血站","急救中心","疾控中心","卫生院","疗养院","专科医院","中医院","综合医院","医疗服务"};
            for (String hy : hhy) {
                if (zhaobiaosplit[1].contains(hy)){
                    newZhongTaiService.saveIntoMysql(resultMap);
                }
            }
            for (String aa : aaa) {
                if (zhaobiaounit.contains(aa) && (zhaobiaosplit[1].contains("大学") || zhaobiaosplit[1].contains("培训") || zhaobiaosplit[1].contains("学校"))){
                    newZhongTaiService.saveIntoMysql(resultMap);
                }
            }
        }

    }

    //调取中台数据———— //预算金额 OR 中标金额字段值≥1000万
    public void getDataFromZhongTaiAndSave4(NoticeMQ noticeMQ){
        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false){
            log.info("contentid:{} 对应的数据状态不是99, 丢弃" , noticeMQ.getContentid().toString());
            return;
        }
        //预算金额 OR 中标金额字段值≥1000万
        if (checkAmount(noticeMQ.getBudget()) || checkAmount(noticeMQ.getNewAmountUnit())) {
            Map<String, Object> resultMap = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
            if (resultMap != null) {
                newZhongTaiService.saveIntoMysql(resultMap);
            }
        }
    }

    //调取中台数据并 获取标的物
    public void getDataFromZhongTaiAndSave5(NoticeMQ noticeMQ) {

        String[] aaa = {"电脑","平板","音频","PC机","计算机","可穿戴","IOT","物联网"};
        String[] bbb = {"智能音频","智能物联","智能平板","智能路由","智能电视","智能穿戴","智能白板","智慧音频","智慧物联","智慧平板","智慧电视","智慧白板","掌上计算机","掌上电脑","游戏本","一体台式机","一体机电脑","液晶平板电视","液晶电视","台式计算机","台式机","台式电脑","手提电脑","商务本","全面屏电视","苹果计算机","苹果电脑","苹果笔记本","苹果ipad","平板电脑","平板笔记本","品牌电脑","路由器","联想台式机","联想计算机","联想电脑","联想笔记本","可穿戴设备","计算机购置","计算机采购","机房电脑","惠普台式机","惠普平板","惠普计算机","惠普电脑","惠普笔记本","华为计算机","华为电脑","华为笔记本","华硕台式机","华硕平板","华硕计算机","华硕电脑","华硕笔记本","购置电脑","高性能移动计算机","高性能笔记本","高清电视","电子白板","电视一体机","电视设备","电视机","电视购置","电视采购","电脑主机","电脑整机","电脑一体机","电脑设备","电脑购置","电脑采购","电脑笔记本","戴尔台式机","戴尔平板","戴尔计算机","戴尔电脑","戴尔笔记本","打印设备","打印机","彩色电视","采购计算机","采购电视","采购电脑","便携式计算机","便携式电脑","便携计算机","便携电脑","笔记本电脑","办公计算机","办公电脑","macbookpro电脑","MacBook","Lenovo台式机","Lenovo计算机","Lenovo电脑","Lenovo笔记本","HP台式机","HP平板","HP计算机","HP电脑","HP笔记本","DELL台式机","DELL平板","DELL计算机","DELL电脑","DELL笔记本","ASUS台式机","ASUS平板","ASUS计算机","ASUS电脑","ASUS笔记本"};
        String[] blacks = {"平板拖把", "LED平板灯", "平板集装箱", "平板探测器"};

        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
        if (resultMap != null) {
            String contentId = resultMap.get("content_id") != null ? resultMap.get("content_id").toString() : "";
            String content = resultMap.get("content") != null ? resultMap.get("content").toString() : "";
            String title = resultMap.get("title") != null ? resultMap.get("title").toString() : "";
            String task_id = resultMap.get("task_id") != null ? resultMap.get("task_id").toString() : "";
            content = title + "&" + content;
            content = content.toUpperCase();
            String keyword = "";
            String code = "";
            boolean flag = true;
            for (String black : blacks) {
                if(StringUtils.isNotBlank(title) && title.contains(black)){
                    flag = false;
                    break;
                }
            }
            if (flag){
                if (task_id.equals("1")){
                    for (String aa : aaa) {
                        if (title.contains(aa)){
                            keyword += (aa + "、") ;
                        }
                    }
                }
                if (task_id.equals("2")){
                    for (String bb : bbb) {
                        if (content.contains(bb)){
                            code += (bb + "、") ;
                        }
                    }
                }
            }
            if (StringUtils.isNotBlank(keyword)) {
                keyword = keyword.substring(0, keyword.length() - 1);
                resultMap.put("keyword", keyword);
            }
            if (StringUtils.isNotBlank(code)) {
                code = code.substring(0, code.length() - 1);
                resultMap.put("keyword_term", code);
            }
            newZhongTaiService.saveIntoMysql(resultMap);
            try {
                zhongTaiBiaoDiWuService.getAllZhongTaiBiaoDIWu(contentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //调取中台数据 并 匹配行业标签
    public void getDataFromZhongTaiAndSave6(NoticeMQ noticeMQ) {
        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> map = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
        if (map != null) {
            String zhaobiaounit = map.get("zhao_biao_unit") != null ? map.get("zhao_biao_unit").toString() : "";
            String zhaobiaoindustry = myRuleUtils.getIndustry(zhaobiaounit);
            String[] zhaobiaosplit = zhaobiaoindustry.split("-");
            if ("医疗单位".equals(zhaobiaosplit[0])){
                newZhongTaiService.saveIntoMysql(map);
            }
        }
    }

    //调取中台数据 并 获取标的物解析表 并 匹配行业标签
    public void getDataFromZhongTaiAndSave7(NoticeMQ noticeMQ) {
        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
        if (resultMap != null) {
            String contentId = resultMap.get("content_id") != null ? resultMap.get("content_id").toString() : "";
            String zhaobiaounit = resultMap.get("zhao_biao_unit") != null ? resultMap.get("zhao_biao_unit").toString() : "";
            String zhaobiaoindustry = myRuleUtils.getIndustry(zhaobiaounit);
            String[] zhaobiaosplit = zhaobiaoindustry.split("-");
            if (zhaobiaosplit[1].contains("金融") || zhaobiaosplit[0].contains("金融企业")){
                // 匹配行业标签
                newZhongTaiService.saveIntoMysql(resultMap);
                try {
                    //获取标的物清单表
                    zhongTaiBiaoDiWuService.getAllZhongTaiBiaoDIWu(contentId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //调取中台数据 并 匹配关键词
    public void getDataFromZhongTaiAndSave8(NoticeMQ noticeMQ) throws IOException {

        String[] aaa = {"服务机器人","语音机器人","智能机器人","接待机器人","讲解机器人","移动机器人","养老机器人","酒店机器人","政务机器人","迎宾机器人","聊天机器人"};

        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false){
            log.info("contentid:{} 对应的数据状态不是99, 丢弃" , noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ, areaMap);
        if (resultMap != null) {

            String title = resultMap.get("title") != null ? resultMap.get("title").toString() : "";
            String content = resultMap.get("content") != null ? resultMap.get("content").toString() : "";
            content = title + "&" + content;
            content = content.toUpperCase();
            String keyword = "";
            for (String aa : aaa) {
                if (content.contains(aa)) {
                    String key = aa;
                    keyword += (key + "、");
                }
            }
            if (StringUtils.isNotBlank(keyword)) {
                keyword = keyword.substring(0, keyword.length() - 1);
                resultMap.put("keyword", keyword);
            }
            newZhongTaiService.saveIntoMysql(resultMap);
//            zhongTaiBiaoDiWuServiceForOne.getAllZhongTaiBiaoDIWu(contentId);
        }

    }

    /**
     * 判断字符串是否是金额
     * @param str
     * @return
     */
    public static boolean isNumber(String str) {
        if(StringUtils.isBlank(str)){
            return false;
        }
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,5})?$"); // 判断小数点后2位的数字的正则表达式
        java.util.regex.Matcher match = pattern.matcher(str);
        if (match.matches() == false) {
            return false;
        } else {
            return true;
        }
    }

    private boolean checkAmount(String amount) {
        //金额为空 或（金额纯数字的& 金额大于等于1000万）
        if (StringUtils.isBlank(amount) ||
                (MathUtil.match(amount) && new BigDecimal(amount).compareTo(new BigDecimal("10000000")) >= 0)) {
            return true;
        }
        return false;
    }

    //时代华擎
    public void getShiDaiSolrAllField(String date) throws IOException {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] ccp1 = {"电脑","工作站"};
        String[] ccp2 = {"acer笔记本","acer电脑","acer计算机","acer台式机","ace平板","ASUS笔记本","ASUS电脑","ASUS计算机","ASUS平板","ASUS台式机","DELL笔记本","DELL电脑","DELL计算机","DELL平板","DELL台式机","DIY电脑","HP笔记本","HP电脑","HP计算机","HP平板","HP台式机","HUAWEI平板","HUAWEI台式机","Lenovo笔记本","Lenovo电脑","Lenovo计算机","Lenovo平板","Lenovo台式机","MacBook","macbookpro","MateBook","matepadpro","PC电脑","PC机","PC终端","ThinkPad","办公电脑","办公计算机","办公用电脑","办公用计算机","办公用平板","笔记本电脑","便携电脑","便携计算机","便携式笔记本","便携式电脑","便携式计算机","采购电脑","采购计算机","插拔式电脑","戴尔笔记本","戴尔电脑","戴尔计算机","戴尔平板","戴尔台式机","电脑笔记本","电脑采购","电脑购置","电脑设备","电脑显示一体机","电脑一体机","电脑整机","电脑终端","电脑主机","东芝笔记本","东芝电脑","东芝计算机","东芝平板","东芝台式机","二合一平板","方正笔记本","方正电脑","方正计算机","方正平板","方正台式机","服务器","高性能笔记本","高性能工作站","高性能移动计算机","个人电脑","个人计算机","个人使用计算机","个人用计算机","购买电脑","购入电脑","购置电脑","宏基笔记本","宏基电脑","宏基计算机","宏基平板","宏基台式机","华硕笔记本","华硕电脑","华硕计算机","华硕平板","华硕台式机","华为笔记本","华为电脑","华为计算机","华为平板","华为台式机","会议平板","会议系统平板","惠普笔记本","惠普电脑","惠普计算机","惠普平板","惠普台式机","机房电脑","计算机","计算机采购","计算机购置","计算机终端","交互平板","教学平板","教育平板","联想笔记本","联想电脑","联想计算机","联想台式机","迷你电脑","品牌电脑","品牌机","平板笔记本","平板电脑","平板设备","平板式电脑","平板式计算机","平板式微型计算机","平板显示设备","平板一体机","平板终端","苹果ipad","苹果笔记本","苹果电脑","苹果计算机","苹果台式机","轻薄本","商务本","上网本","神州笔记本","神州电脑","神州计算机","神州平板","神州台式机","手提电脑","书写平板","水冷电脑","台式电脑","台式工作站","台式机","台式计算机","台式整机","台式组装机","图形工作站","微型计算机","系统集成","小米笔记本","小米电脑","小米计算机","小米平板","小米台式机","一体机电脑","一体台式机","移动工作站","移动平板","游戏本","阅文平板","掌上电脑","掌上计算机","智慧平板","智能平板","智能音频","终端电脑","终端计算机","组装电脑","组装机"};
        String[] blacks = {"组织破碎仪","租用","租赁钢板","租赁","租车服务项目","租车服务","租车","自然灾害救助物资","自行车棚修建","资料存储管理服务","资料册","资产出让","咨询服务协议书","桌椅项目","桌面型/语音型","桌面文件收纳柜","装修工程","装订机","碎纸机","装订封面","装袋生产线设备","专题片","住宿、餐饮","竹篱笆","轴承架修","轴承","中性笔","中心拆除的部分资产","置物架","治疗机","质谱仪","指挥用车","纸式扫描仪","纸板","植树苗木","整治拆除","镇志编撰","招租中","账册","账本","长尾夹","增殖流放鱼苗","在线质疑功能","运维","运输车采购","阅读器","元器件应用","羽毛球","幼儿园特一粉","有创呼吸机","油墨","油料收储能力","硬卷未轧制","应用软件","印刷文件","印刷服务","印刷费","印刷材料","印泥","饮水机","饮水安全水质检测","音像制作","椅子类采购","仪表材料","医院洗手衣","医用试剂","医用设备","医学装备","医疗设备","一次性餐具","一处房屋","液压站阀采购","液压交换机","液氮存储罐","摇控器","养护机械","养护车","验收结论公示","研磨机","血管造影成像系统","学院护理实验室","学生公寓凳子","旋转拖把","宣传片","宣传拍摄项目","宣传服务","修理","信息检索服务","信息传输服务","新建工程财务","校园巡逻车","小型机械租赁","消防专项规划","消防设施检验服务","消防管路大修","消防改造","消防材料","项目设计施工总承包","项目设计服务","系统软件","系统服务","洗衣粉","洗衣房设备","洗扫车","硒鼓","物业管理服务","物业服务","舞台灯光设备","无极绳绞车","屋面翻修","污水管修复","蚊香液","文印服务","文件夹","文件柜","文件袋","胃肠造影机","尾料校平板","维修","维护","维保","微量注射泵","网站美工","外科显微镜","外电辅修","挖掘装载机","土石方工程","通讯专线租用","碳粉","碳带","檀香","台阶仪","塑杯","送餐服务","水笔","双排座","双层治疗车","数字摄像系统","数字平板血管造影系统","数码单反","数据备份与恢复","束线带","书籍","收纳箱","收费服务","视频制作","事务包","食堂修缮工程","实验室维修改造","实木沙发","石材及钢材","十三层改造配套设备","施工监理","生日蛋糕","升级软件","审计审价","审计服务","设备问题整改","设备升级","烧水壶","砂石料","杀菌灯管","杀毒软件","色带框","三维扫描仪","三级配电箱","撒盐车采购","日杂用品","日常护理","认知功能障碍","热水壶","热合组装机","全过程造价咨询服务","全过程工程咨询","取暖器","清扫车","清洁外包","清查业务协议书","切纸机","签字笔","铅笔","迁建项目","汽车起重机","气质联用仪","普通干电池","平台软件","平板液位计","平板血管造影","平板显示基地","平板卫生纸","平板拖把","平板探测器","平板扣","平板集装箱","平板灯","平板车","平板笔","平板C型臂X射线机","配套软件","配件","抛光机","排危项目施工","暖风机","牛皮纸","内窥镜保修","幕墙工程","木材存储","墨汁","墨水","墨盒","墨粉","模拟推演沙盘","面纸","面粉","面板灯","免疫分析系统","密封件","门面房租赁","美工刀","煤矿专用49处","绿化带修剪","路灯维修","楼体修缮","楼体明亮工程","零星维修工程","零件","零部件","陵园物业管理","临时布花","良种肉牛","脸盆","连续供墨系统","沥青砼","冷藏冷冻冰箱","老旧管道CCTV检测","劳务招标","劳保用品","拉手","垃圾清运","垃圾袋","口腔耗材","空调管道维修","拷贝系统","开平板用剪刃","开荒清洁","开发套件项目","军人服装采购","卷筒纸","卷开平板","捐赠方案","聚光灯","酒精","精密天平","紧急呼叫铃改造","金钟器件","金隅售卡机","金龙客车","秸秆存储场","接口固态硬盘","教学楼改造","教学服务项目","脚手架租赁","胶水","胶带","胶棒","交强险","建筑能效提升","建设项目勘察设计","建设方案编制","剪刀","检测仪器","监理中标候选人","家具购置","家具采购","绩效评价服务","继电器","记事本","记号笔","计算机桌","计算机重点专业教学","计算机直接制版","计算机应用技术","计算机信息系统","计算机项目","计算机系统能力教学","计算机网络系统","计算机体层摄影","计算机软件","计算机集中控制","计算机辅助设备","计算机断层扫描","激光器","激光笔","机动车辆外委维修","活动组织策划","混凝土","绘图板","会议椅子","回形针","呼吸道病原体","后勤服务","耗材","行车配件","海绵胶带","海绵擦","海尔冰箱","海报","果蝇麻醉器","锅炉件","广告牌","广场改造工程","光学镀膜仿","光电设备","管理软件","关于碗","关于其它","关于其他","关于单据","挂锁","购买车辆","供热维修","公寓床具项目","公益除害","公务用车","公路路基路面","工业用地","工业除湿机","工科类建设项目","工程车配件","根雕茶桌","钢丝绳","钢丝球","钢材加工配送","钢材加工配售","改造装修","腹腔镜","复印纸","辅材","风琴包","风管检修","粉刷修缮","粉盒","粉笔","分析仪维修","分析软件","废空调","废旧金属","废旧电脑","房屋租赁","房屋招租","防撞桶","防水招标任务","防护鞋","防尘罩","防尘保护罩","二氧化碳培养箱","儿童图书画","冻库设备采购","动态监测系统","订书机","钓鱼竿","电子计算器","电液平板阀","电梯集体采购","电梯定点采购","电梯采购","电视宣传","电视墙","电视节目制作","电热水器","电气一批","电气备件","电瓶车维修","电脑桌","电脑中频电疗机","电脑制作活动","电脑站级","电脑椅","电脑心肺复苏模拟","电脑调漆","电脑票","电脑连打纸","电脑技术有限","电脑机房桌椅","电脑编程","电脑比色仪","电脑办公网络","电力增容项目","电力线通信设备","电缆","电动汽车充","电动平板搬运车","电厂检修维护","电冰箱","第三方测评服务","地块南侧道路","地点搬迁","道路养护","道路维修","道路大修","倒置显微镜","档案盒","呆滞物料","大头针","大楼物业项目","大疆机甲大师","打印纸","打印机换针","村庄改造项目","村部附属工程","串联质谱仪","储备开闭器","厨具采购","出让地块","抽纸","程序员型计算器","成品油管道工程","车辆维护保养","车辆购置","车辆采购","车船税","超声诊断仪","超声成像","超卡板制作","柴油发电机组","茶叶","茶水柜","策划宣传","采油厂高架计量间","采血车","采购家具用具","采购电线","材料","布艺沙发","不锈钢切边余料","殡葬用品采购","别针","便签条","便签本","编辑服务","笔筒","泵车配件","备件","报废资产","保养","保险箱","保险经纪机构","保险的采购","保密检查技术","保洁服务","宝钢湛江机架","包装纸","包装服务","办公桌","办公椅","办公线材","办公软件","办公家具","办公耗材","办公电话","搬迁家具采购","白板笔","氨用压力表","安装服务","安装锤","安全防护服务","安保系统整改","安保/保洁","PVC弯头","PVC笔记本","PCR仪","PCR系统","LED日光灯","LED平板灯","CT采购项目","A4纸","加粉服务","数码线材","关于橡皮","热轧开平板","废旧电视电脑","废旧资产","电脑角膜验光仪","电脑验光仪","工业园主体招标","电脑室空调采购","标牌"};

        //规则一
        for (String cp1 : ccp1) {
            futureList1.add(executorService1.submit(() -> {
                String key = cp1;
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200701 TO 20200931] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + cp1 + "\"", key, 1);
                log.info(key.trim() + "————" + mqEntities.size());
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
                                list1.add(data);
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


        log.info("全部数据量：" + list1.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");

        if (list != null && list.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(80);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> {
                    try {
                        getShiDaiDataFromZhongTaiAndSave(content);
                    } catch (Exception e) {
                        e.printStackTrace();
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
        }
    }

    private void getShiDaiDataFromZhongTaiAndSave(NoticeMQ noticeMQ) {

        String[] ccp1 = {"电脑","工作站"};
        String[] ccp2 = {"acer笔记本","acer电脑","acer计算机","acer台式机","ace平板","ASUS笔记本","ASUS电脑","ASUS计算机","ASUS平板","ASUS台式机","DELL笔记本","DELL电脑","DELL计算机","DELL平板","DELL台式机","DIY电脑","HP笔记本","HP电脑","HP计算机","HP平板","HP台式机","HUAWEI平板","HUAWEI台式机","Lenovo笔记本","Lenovo电脑","Lenovo计算机","Lenovo平板","Lenovo台式机","MacBook","macbookpro","MateBook","matepadpro","PC电脑","PC机","PC终端","ThinkPad","办公电脑","办公计算机","办公用电脑","办公用计算机","办公用平板","笔记本电脑","便携电脑","便携计算机","便携式笔记本","便携式电脑","便携式计算机","采购电脑","采购计算机","插拔式电脑","戴尔笔记本","戴尔电脑","戴尔计算机","戴尔平板","戴尔台式机","电脑笔记本","电脑采购","电脑购置","电脑设备","电脑显示一体机","电脑一体机","电脑整机","电脑终端","电脑主机","东芝笔记本","东芝电脑","东芝计算机","东芝平板","东芝台式机","二合一平板","方正笔记本","方正电脑","方正计算机","方正平板","方正台式机","服务器","高性能笔记本","高性能工作站","高性能移动计算机","个人电脑","个人计算机","个人使用计算机","个人用计算机","购买电脑","购入电脑","购置电脑","宏基笔记本","宏基电脑","宏基计算机","宏基平板","宏基台式机","华硕笔记本","华硕电脑","华硕计算机","华硕平板","华硕台式机","华为笔记本","华为电脑","华为计算机","华为平板","华为台式机","会议平板","会议系统平板","惠普笔记本","惠普电脑","惠普计算机","惠普平板","惠普台式机","机房电脑","计算机","计算机采购","计算机购置","计算机终端","交互平板","教学平板","教育平板","联想笔记本","联想电脑","联想计算机","联想台式机","迷你电脑","品牌电脑","品牌机","平板笔记本","平板电脑","平板设备","平板式电脑","平板式计算机","平板式微型计算机","平板显示设备","平板一体机","平板终端","苹果ipad","苹果笔记本","苹果电脑","苹果计算机","苹果台式机","轻薄本","商务本","上网本","神州笔记本","神州电脑","神州计算机","神州平板","神州台式机","手提电脑","书写平板","水冷电脑","台式电脑","台式工作站","台式机","台式计算机","台式整机","台式组装机","图形工作站","微型计算机","系统集成","小米笔记本","小米电脑","小米计算机","小米平板","小米台式机","一体机电脑","一体台式机","移动工作站","移动平板","游戏本","阅文平板","掌上电脑","掌上计算机","智慧平板","智能平板","智能音频","终端电脑","终端计算机","组装电脑","组装机"};
        String[] blacks = {"组织破碎仪","租用","租赁钢板","租赁","租车服务项目","租车服务","租车","自然灾害救助物资","自行车棚修建","资料存储管理服务","资料册","资产出让","咨询服务协议书","桌椅项目","桌面型/语音型","桌面文件收纳柜","装修工程","装订机","碎纸机","装订封面","装袋生产线设备","专题片","住宿、餐饮","竹篱笆","轴承架修","轴承","中性笔","中心拆除的部分资产","置物架","治疗机","质谱仪","指挥用车","纸式扫描仪","纸板","植树苗木","整治拆除","镇志编撰","招租中","账册","账本","长尾夹","增殖流放鱼苗","在线质疑功能","运维","运输车采购","阅读器","元器件应用","羽毛球","幼儿园特一粉","有创呼吸机","油墨","油料收储能力","硬卷未轧制","应用软件","印刷文件","印刷服务","印刷费","印刷材料","印泥","饮水机","饮水安全水质检测","音像制作","椅子类采购","仪表材料","医院洗手衣","医用试剂","医用设备","医学装备","医疗设备","一次性餐具","一处房屋","液压站阀采购","液压交换机","液氮存储罐","摇控器","养护机械","养护车","验收结论公示","研磨机","血管造影成像系统","学院护理实验室","学生公寓凳子","旋转拖把","宣传片","宣传拍摄项目","宣传服务","修理","信息检索服务","信息传输服务","新建工程财务","校园巡逻车","小型机械租赁","消防专项规划","消防设施检验服务","消防管路大修","消防改造","消防材料","项目设计施工总承包","项目设计服务","系统软件","系统服务","洗衣粉","洗衣房设备","洗扫车","硒鼓","物业管理服务","物业服务","舞台灯光设备","无极绳绞车","屋面翻修","污水管修复","蚊香液","文印服务","文件夹","文件柜","文件袋","胃肠造影机","尾料校平板","维修","维护","维保","微量注射泵","网站美工","外科显微镜","外电辅修","挖掘装载机","土石方工程","通讯专线租用","碳粉","碳带","檀香","台阶仪","塑杯","送餐服务","水笔","双排座","双层治疗车","数字摄像系统","数字平板血管造影系统","数码单反","数据备份与恢复","束线带","书籍","收纳箱","收费服务","视频制作","事务包","食堂修缮工程","实验室维修改造","实木沙发","石材及钢材","十三层改造配套设备","施工监理","生日蛋糕","升级软件","审计审价","审计服务","设备问题整改","设备升级","烧水壶","砂石料","杀菌灯管","杀毒软件","色带框","三维扫描仪","三级配电箱","撒盐车采购","日杂用品","日常护理","认知功能障碍","热水壶","热合组装机","全过程造价咨询服务","全过程工程咨询","取暖器","清扫车","清洁外包","清查业务协议书","切纸机","签字笔","铅笔","迁建项目","汽车起重机","气质联用仪","普通干电池","平台软件","平板液位计","平板血管造影","平板显示基地","平板卫生纸","平板拖把","平板探测器","平板扣","平板集装箱","平板灯","平板车","平板笔","平板C型臂X射线机","配套软件","配件","抛光机","排危项目施工","暖风机","牛皮纸","内窥镜保修","幕墙工程","木材存储","墨汁","墨水","墨盒","墨粉","模拟推演沙盘","面纸","面粉","面板灯","免疫分析系统","密封件","门面房租赁","美工刀","煤矿专用49处","绿化带修剪","路灯维修","楼体修缮","楼体明亮工程","零星维修工程","零件","零部件","陵园物业管理","临时布花","良种肉牛","脸盆","连续供墨系统","沥青砼","冷藏冷冻冰箱","老旧管道CCTV检测","劳务招标","劳保用品","拉手","垃圾清运","垃圾袋","口腔耗材","空调管道维修","拷贝系统","开平板用剪刃","开荒清洁","开发套件项目","军人服装采购","卷筒纸","卷开平板","捐赠方案","聚光灯","酒精","精密天平","紧急呼叫铃改造","金钟器件","金隅售卡机","金龙客车","秸秆存储场","接口固态硬盘","教学楼改造","教学服务项目","脚手架租赁","胶水","胶带","胶棒","交强险","建筑能效提升","建设项目勘察设计","建设方案编制","剪刀","检测仪器","监理中标候选人","家具购置","家具采购","绩效评价服务","继电器","记事本","记号笔","计算机桌","计算机重点专业教学","计算机直接制版","计算机应用技术","计算机信息系统","计算机项目","计算机系统能力教学","计算机网络系统","计算机体层摄影","计算机软件","计算机集中控制","计算机辅助设备","计算机断层扫描","激光器","激光笔","机动车辆外委维修","活动组织策划","混凝土","绘图板","会议椅子","回形针","呼吸道病原体","后勤服务","耗材","行车配件","海绵胶带","海绵擦","海尔冰箱","海报","果蝇麻醉器","锅炉件","广告牌","广场改造工程","光学镀膜仿","光电设备","管理软件","关于碗","关于其它","关于其他","关于单据","挂锁","购买车辆","供热维修","公寓床具项目","公益除害","公务用车","公路路基路面","工业用地","工业除湿机","工科类建设项目","工程车配件","根雕茶桌","钢丝绳","钢丝球","钢材加工配送","钢材加工配售","改造装修","腹腔镜","复印纸","辅材","风琴包","风管检修","粉刷修缮","粉盒","粉笔","分析仪维修","分析软件","废空调","废旧金属","废旧电脑","房屋租赁","房屋招租","防撞桶","防水招标任务","防护鞋","防尘罩","防尘保护罩","二氧化碳培养箱","儿童图书画","冻库设备采购","动态监测系统","订书机","钓鱼竿","电子计算器","电液平板阀","电梯集体采购","电梯定点采购","电梯采购","电视宣传","电视墙","电视节目制作","电热水器","电气一批","电气备件","电瓶车维修","电脑桌","电脑中频电疗机","电脑制作活动","电脑站级","电脑椅","电脑心肺复苏模拟","电脑调漆","电脑票","电脑连打纸","电脑技术有限","电脑机房桌椅","电脑编程","电脑比色仪","电脑办公网络","电力增容项目","电力线通信设备","电缆","电动汽车充","电动平板搬运车","电厂检修维护","电冰箱","第三方测评服务","地块南侧道路","地点搬迁","道路养护","道路维修","道路大修","倒置显微镜","档案盒","呆滞物料","大头针","大楼物业项目","大疆机甲大师","打印纸","打印机换针","村庄改造项目","村部附属工程","串联质谱仪","储备开闭器","厨具采购","出让地块","抽纸","程序员型计算器","成品油管道工程","车辆维护保养","车辆购置","车辆采购","车船税","超声诊断仪","超声成像","超卡板制作","柴油发电机组","茶叶","茶水柜","策划宣传","采油厂高架计量间","采血车","采购家具用具","采购电线","材料","布艺沙发","不锈钢切边余料","殡葬用品采购","别针","便签条","便签本","编辑服务","笔筒","泵车配件","备件","报废资产","保养","保险箱","保险经纪机构","保险的采购","保密检查技术","保洁服务","宝钢湛江机架","包装纸","包装服务","办公桌","办公椅","办公线材","办公软件","办公家具","办公耗材","办公电话","搬迁家具采购","白板笔","氨用压力表","安装服务","安装锤","安全防护服务","安保系统整改","安保/保洁","PVC弯头","PVC笔记本","PCR仪","PCR系统","LED日光灯","LED平板灯","CT采购项目","A4纸","加粉服务","数码线材","关于橡皮","热轧开平板","废旧电视电脑","废旧资产","电脑角膜验光仪","电脑验光仪","工业园主体招标","电脑室空调采购","标牌"};

        boolean result = newZhongTaiService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false){
            log.info("contentid:{} 对应的数据状态不是99, 丢弃" , noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = newZhongTaiService.handleZhongTaiGetResultMap(noticeMQ);
        if (resultMap != null) {
            String title = resultMap.get("title") != null ? resultMap.get("title").toString() : "";
            String content = resultMap.get("content") != null ? resultMap.get("content").toString() : "";
            String zhaobiaounit = resultMap.get("zhao_biao_unit") != null ? resultMap.get("zhao_biao_unit").toString() : "";
            String industry = myRuleUtils.getIndustry(zhaobiaounit);
            String[] split = industry.split("-");

            title = title.toUpperCase();
            content = content.toUpperCase();

            String titleKeyword = "";
            String contentKeyword = "";
            String biaoDiWuKeyword = "";

            if ("商业公司".equals(split[0]) || "金融企业".equals(split[0])){
                boolean flag = true;
                for (String black : blacks) {
                    if(StringUtils.isNotBlank(title) && title.contains(black)){
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    for (String aa : ccp1) {
                        if (title.contains(aa.toUpperCase())) {
                            String key = aa;
                            titleKeyword += (key + "、");
                        }
                    }
                    if (StringUtils.isNotBlank(titleKeyword)) {
                        titleKeyword = titleKeyword.substring(0, titleKeyword.length() - 1);
                    }
                    if (StringUtils.isNotBlank(titleKeyword)){
                        resultMap.put("keyword_term", titleKeyword);
                        newZhongTaiService.saveIntoMysql(resultMap);
                    }
                    String biaoDIWu = "";
                    try {
                        biaoDIWu = zhongTaiBiaoDiWuServiceForOne.getAllZhongTaiBiaoDIWu(String.valueOf(noticeMQ.getContentid()));
                        for (String aa : ccp2) {
                            if (biaoDIWu.contains(aa.toUpperCase())){
                                biaoDiWuKeyword += aa + ConstantBean.RULE_SEPARATOR_NAME;
                            }
                        }
                    } catch (Exception e){
                        log.info("contentId:{} 对应的数据，处理标的物失败", noticeMQ.getContentid());
                    }

                    if (StringUtils.isNotBlank(biaoDiWuKeyword)){
                        biaoDiWuKeyword = biaoDiWuKeyword.substring(0, biaoDiWuKeyword.length() - 1);
                    }

                    if (StringUtils.isNotBlank(biaoDiWuKeyword)){
                        resultMap.put("code", biaoDiWuKeyword);
                        newZhongTaiService.saveIntoMysql(resultMap);
                    }
//                        zhongTaiBiaoDiWuService.getAllZhongTaiBiaoDIWu(String.valueOf(noticeMQ.getContentid()));
                }
            }
        }
    }
}
















