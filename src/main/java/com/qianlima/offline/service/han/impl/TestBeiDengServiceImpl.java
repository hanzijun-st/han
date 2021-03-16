package com.qianlima.offline.service.han.impl;

import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.service.han.CurrencyService;
import com.qianlima.offline.service.han.CusDataNewService;
import com.qianlima.offline.service.han.TestBeiDengService;
import com.qianlima.offline.util.LogUtils;
import com.qianlima.offline.util.OnlineContentSolr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Slf4j
public class TestBeiDengServiceImpl implements TestBeiDengService {

    @Autowired
    private OnlineContentSolr onlineContentSolr;

    @Autowired
    private CurrencyService currencyService;//为了获取 progid

    @Autowired
    private CusDataNewService cusDataNewService;//调用中台接口

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Override
    public void getBeiDeng4(Integer type, String date, String progidStr) throws Exception{
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> listAll = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        //关键词 全文
        List<String> qw = LogUtils.readRule("bdA");

        //关键词 全文+辅助
        //List<String> qwAndFz = LogUtils.readRule("bdAndFz");

        //b词
        //String[] b ={"医院","诊所","门诊","保健院","健康委员会","医学院","体检中心","健康局","医院部","药房","卫生院","医疗保障局","合作医疗","医药服务管理司","兽医实验室","医药","精神病院","防治院","血液中心","眼科中心","治疗中心","保健中心","保健所","血管病研究所","防治所","外科中心","康复中心","透析中心","正畸中心","荣军院","防治中心","保健站","列腺病研究所","职业病院","防治站","产院","急救中心","卫生局","卫生厅","防治办公室","卫生保健中心","医疗中心","卫生中心","门诊部","卫生服务站","医检所","制剂室","药交所","眼科","医保","医疗保障","卫健委","戒毒所","敬老院","疗养院","眼病防治所","矫治所","结核病防治所","休养所","血站","福利院","医疗机构","病防治办公室","计划生育","生育委员","计生委","大健康","同仁堂","江中集团","医学","健康科技","养生堂","保健品","诊断","康宁","制药","药业","药集团","医疗集团","精神卫生","药店","军医","医用","医疗","诊疗","残联","医护","卫生所","卫生院 ","卫生院校","医科大学","妇幼","健康中心","运动康复","中医馆","预防控制","医务室"};

        //黑词
        //String[] hc={"车辆维修","人才招聘","保温材料更换","物业管理服务","空调管路系统","安保服务","空调维保","污水处理系统","改造安装防护门","热水系统改造","排风系统升级改造","空调系统改造","采购安装风管机空调","消防维保","绿化带拆除","电梯维保服务","中央空调清洗","消防维修","地下车库加建","食堂对外承包","保护测评","保洁服务","监理单位","监理企业","招租","设施改造","房租","出租","选择招标代理机构","水杯维修","食堂外包服务","后厨管理承包","食堂承包经营","食堂等物业服务","后勤保洁服务","食堂项目承包","肉类配送","保安服务","车转让","变压器扩容","网络招聘服务","保险联网结算系统","宣传片投放","广告服务","工程垃圾设备","景观节点整治","塌方除理","房屋拍卖","汽车采购","工程造价咨询","整体板房询价","租赁服务","垃圾清运","外墙保温","康复大楼工程监理","宣传策划","车辆租赁","办公系统开发","水体治理","审计业务","养老购买","坑塘整治","后勤保洁管理服务","设备维修维护保养","冷水机组主机设备及末端设备采购","路灯采购","奶粉采购","采购家具","空调采购","多联机空调","锅炉房设备采购","电视机采购","采购电视机","环卫工具采购","印刷采购","加装电梯","被服采购","家具采购","石材采购","停车设备采购","电梯采购","垃圾压缩成套设备","窗帘采购","混凝土招标","数控机床附件","监理","工程监理","施工监理","广告宣传","临建食堂购餐桌椅","食堂食材采购","食堂食品","员工工装","热机组采购","竹地板材料","有限公司轮胎","保险采购","苗木采购","鱼苗采购","多联机配件","污水处理设备","白色OPPOR9手机","货物类采购","水分配系统","采购日常百货","石材招标","玻璃隔断","玻璃栏杆","医院勘察采购","防褥疮床垫","清洁能源示范","铅桶采购","笔记本电脑采购","空调设备招标","废标","流标","终止","违规","招标异常","无效公告","暂停公告","失败公告","终止公告"};

        for (String str : qw) {
            futureList1.add(executorService1.submit(() -> {
                List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND progid:3 AND allcontent:\"" + str + "\"", str, 2);
                log.info(str.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag){
                                listAll.add(data);
                                data.setKeyword(str);
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


       /* for (String str : qwAndFz) {
            for (String str2 : b) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND progid:3 AND allcontent:\"" + str + "\"  AND zhaoBiaoUnit:\"" + str2 + "\"", str+"&"+str2, 2);
                    log.info(str.trim()+"&"+str2 + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag){
                                    listAll.add(data);
                                    data.setKeyword(str+"&"+str2);
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
        }
        for (String str : qwAndFz) {
            for (String str2 : b) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND progid:3 AND allcontent:\"" + str + "\"  AND title:\"" + str2 + "\"", str+"&"+str2, 2);
                    log.info(str.trim()+"&"+str2 + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag){
                                    listAll.add(data);
                                    data.setKeyword(str+"&"+str2);
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
        }*/

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


        log.info("全部数据量：" + listAll.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");

        ArrayList<String> arrayList = new ArrayList<>();

        //关键词全文
        for (String a :qw){
            arrayList.add(a);
        }

       /* for (String key : qwAndFz) {
            for (String k : b){
                arrayList.add(key+"&"+k);
            }
        }*/

        for (String str : arrayList) {
            int total = 0;
            for (NoticeMQ noticeMQ : listAll) {
                String keyword = noticeMQ.getKeyword();
                if (keyword.equals(str)) {
                    total++;
                }
            }
            if (total == 0) {
                continue;
            }
            System.out.println(str + ": " + total);
        }
        System.out.println("全部数据量：" + listAll.size());
        System.out.println("去重之后的数据量：" + list.size());

        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(80);
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
    }

    @Override
    public void getBeideng4_2(Integer type, String date, String progidStr) throws Exception{
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> listAll = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        //关键词 全文
        //List<String> qw = LogUtils.readRule("bdA");

        //关键词 全文+辅助
        List<String> qwAndFz = LogUtils.readRule("bdAndFz");

        //b词
        String[] b ={"医院","诊所","门诊","保健院","健康委员会","医学院","体检中心","健康局","医院部","药房","卫生院","医疗保障局","合作医疗","医药服务管理司","兽医实验室","医药","精神病院","防治院","血液中心","眼科中心","治疗中心","保健中心","保健所","血管病研究所","防治所","外科中心","康复中心","透析中心","正畸中心","荣军院","防治中心","保健站","列腺病研究所","职业病院","防治站","产院","急救中心","卫生局","卫生厅","防治办公室","卫生保健中心","医疗中心","卫生中心","门诊部","卫生服务站","医检所","制剂室","药交所","眼科","医保","医疗保障","卫健委","戒毒所","敬老院","疗养院","眼病防治所","矫治所","结核病防治所","休养所","血站","福利院","医疗机构","病防治办公室","计划生育","生育委员","计生委","大健康","同仁堂","江中集团","医学","健康科技","养生堂","保健品","诊断","康宁","制药","药业","药集团","医疗集团","精神卫生","药店","军医","医用","医疗","诊疗","残联","医护","卫生所","卫生院 ","卫生院校","医科大学","妇幼","健康中心","运动康复","中医馆","预防控制","医务室"};

        //黑词
        //String[] hc={"车辆维修","人才招聘","保温材料更换","物业管理服务","空调管路系统","安保服务","空调维保","污水处理系统","改造安装防护门","热水系统改造","排风系统升级改造","空调系统改造","采购安装风管机空调","消防维保","绿化带拆除","电梯维保服务","中央空调清洗","消防维修","地下车库加建","食堂对外承包","保护测评","保洁服务","监理单位","监理企业","招租","设施改造","房租","出租","选择招标代理机构","水杯维修","食堂外包服务","后厨管理承包","食堂承包经营","食堂等物业服务","后勤保洁服务","食堂项目承包","肉类配送","保安服务","车转让","变压器扩容","网络招聘服务","保险联网结算系统","宣传片投放","广告服务","工程垃圾设备","景观节点整治","塌方除理","房屋拍卖","汽车采购","工程造价咨询","整体板房询价","租赁服务","垃圾清运","外墙保温","康复大楼工程监理","宣传策划","车辆租赁","办公系统开发","水体治理","审计业务","养老购买","坑塘整治","后勤保洁管理服务","设备维修维护保养","冷水机组主机设备及末端设备采购","路灯采购","奶粉采购","采购家具","空调采购","多联机空调","锅炉房设备采购","电视机采购","采购电视机","环卫工具采购","印刷采购","加装电梯","被服采购","家具采购","石材采购","停车设备采购","电梯采购","垃圾压缩成套设备","窗帘采购","混凝土招标","数控机床附件","监理","工程监理","施工监理","广告宣传","临建食堂购餐桌椅","食堂食材采购","食堂食品","员工工装","热机组采购","竹地板材料","有限公司轮胎","保险采购","苗木采购","鱼苗采购","多联机配件","污水处理设备","白色OPPOR9手机","货物类采购","水分配系统","采购日常百货","石材招标","玻璃隔断","玻璃栏杆","医院勘察采购","防褥疮床垫","清洁能源示范","铅桶采购","笔记本电脑采购","空调设备招标","废标","流标","终止","违规","招标异常","无效公告","暂停公告","失败公告","终止公告"};

        for (String str : qwAndFz) {
            for (String str2 : b) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND progid:3 AND allcontent:\"" + str + "\"  AND zhaoBiaoUnit:\"" + str2 + "\"", str+"&"+str2, 2);
                    log.info(str.trim()+"&"+str2 + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag){
                                    listAll.add(data);
                                    data.setKeyword(str+"&"+str2);
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
        }
        for (String str : qwAndFz) {
            for (String str2 : b) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND progid:3 AND allcontent:\"" + str + "\"  AND title:\"" + str2 + "\"", str+"&"+str2, 2);
                    log.info(str.trim()+"&"+str2 + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag){
                                    listAll.add(data);
                                    data.setKeyword(str+"&"+str2);
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


        log.info("全部数据量：" + listAll.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");

        ArrayList<String> arrayList = new ArrayList<>();

        //关键词全文
        for (String key : qwAndFz) {
            for (String k : b){
                arrayList.add(key+"&"+k);
            }
        }

        for (String str : arrayList) {
            int total = 0;
            for (NoticeMQ noticeMQ : listAll) {
                String keyword = noticeMQ.getKeyword();
                if (keyword.equals(str)) {
                    total++;
                }
            }
            if (total == 0) {
                continue;
            }
            System.out.println(str + ": " + total);
        }
        System.out.println("全部数据量：" + listAll.size());
        System.out.println("去重之后的数据量：" + list.size());

        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(30);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSave2(content)));
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
    }

    @Override
    public void getBeideng3(Integer type, String date, String progidStr) throws Exception{
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> listAll = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        //关键词 全文
        List<String> qw = LogUtils.readRule("bdA");

        //关键词 全文+辅助
        List<String> qwAndFz = LogUtils.readRule("bdAndFz");

        //b词
        String[] b ={"医院","诊所","门诊","保健院","健康委员会","医学院","体检中心","健康局","医院部","药房","卫生院","医疗保障局","合作医疗","医药服务管理司","兽医实验室","医药","精神病院","防治院","血液中心","眼科中心","治疗中心","保健中心","保健所","血管病研究所","防治所","外科中心","康复中心","透析中心","正畸中心","荣军院","防治中心","保健站","列腺病研究所","职业病院","防治站","产院","急救中心","卫生局","卫生厅","防治办公室","卫生保健中心","医疗中心","卫生中心","门诊部","卫生服务站","医检所","制剂室","药交所","眼科","医保","医疗保障","卫健委","戒毒所","敬老院","疗养院","眼病防治所","矫治所","结核病防治所","休养所","血站","福利院","医疗机构","病防治办公室","计划生育","生育委员","计生委","大健康","同仁堂","江中集团","医学","健康科技","养生堂","保健品","诊断","康宁","制药","药业","药集团","医疗集团","精神卫生","药店","军医","医用","医疗","诊疗","残联","医护","卫生所","卫生院 ","卫生院校","医科大学","妇幼","健康中心","运动康复","中医馆","预防控制","医务室"};

        //黑词
        //String[] hc={"车辆维修","人才招聘","保温材料更换","物业管理服务","空调管路系统","安保服务","空调维保","污水处理系统","改造安装防护门","热水系统改造","排风系统升级改造","空调系统改造","采购安装风管机空调","消防维保","绿化带拆除","电梯维保服务","中央空调清洗","消防维修","地下车库加建","食堂对外承包","保护测评","保洁服务","监理单位","监理企业","招租","设施改造","房租","出租","选择招标代理机构","水杯维修","食堂外包服务","后厨管理承包","食堂承包经营","食堂等物业服务","后勤保洁服务","食堂项目承包","肉类配送","保安服务","车转让","变压器扩容","网络招聘服务","保险联网结算系统","宣传片投放","广告服务","工程垃圾设备","景观节点整治","塌方除理","房屋拍卖","汽车采购","工程造价咨询","整体板房询价","租赁服务","垃圾清运","外墙保温","康复大楼工程监理","宣传策划","车辆租赁","办公系统开发","水体治理","审计业务","养老购买","坑塘整治","后勤保洁管理服务","设备维修维护保养","冷水机组主机设备及末端设备采购","路灯采购","奶粉采购","采购家具","空调采购","多联机空调","锅炉房设备采购","电视机采购","采购电视机","环卫工具采购","印刷采购","加装电梯","被服采购","家具采购","石材采购","停车设备采购","电梯采购","垃圾压缩成套设备","窗帘采购","混凝土招标","数控机床附件","监理","工程监理","施工监理","广告宣传","临建食堂购餐桌椅","食堂食材采购","食堂食品","员工工装","热机组采购","竹地板材料","有限公司轮胎","保险采购","苗木采购","鱼苗采购","多联机配件","污水处理设备","白色OPPOR9手机","货物类采购","水分配系统","采购日常百货","石材招标","玻璃隔断","玻璃栏杆","医院勘察采购","防褥疮床垫","清洁能源示范","铅桶采购","笔记本电脑采购","空调设备招标","废标","流标","终止","违规","招标异常","无效公告","暂停公告","失败公告","终止公告"};


        for (String str : qw) {
            futureList1.add(executorService1.submit(() -> {
                List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND progid:3 AND allcontent:\"" + str + "\"", str, 2);
                log.info(str.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag){
                                listAll.add(data);
                                data.setKeyword(str);
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


        for (String str : qwAndFz) {
            for (String str2 : b) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND progid:3 AND allcontent:\"" + str + "\"  AND zhaoBiaoUnit:\"" + str2 + "\"", str+"&"+str2, 2);
                    log.info(str.trim()+"&"+str2 + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag){
                                    listAll.add(data);
                                    data.setKeyword(str+"&"+str2);
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
        }
        for (String str : qwAndFz) {
            for (String str2 : b) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND progid:3 AND allcontent:\"" + str + "\"  AND title:\"" + str2 + "\"", str+"&"+str2, 2);
                    log.info(str.trim()+"&"+str2 + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag){
                                    listAll.add(data);
                                    data.setKeyword(str+"&"+str2);
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


        log.info("全部数据量：" + listAll.size());
        log.info("去重之后的数据量：" + list.size());
        log.info("==========================");

        ArrayList<String> arrayList = new ArrayList<>();

        //关键词全文
        for (String a :qw){
            arrayList.add(a);
        }

        for (String key : qwAndFz) {
            for (String k : b){
                arrayList.add(key+"&"+k);
            }
        }

        for (String str : arrayList) {
            int total = 0;
            for (NoticeMQ noticeMQ : listAll) {
                String keyword = noticeMQ.getKeyword();
                if (keyword.equals(str)) {
                    total++;
                }
            }
            if (total == 0) {
                continue;
            }
            System.out.println(str + ": " + total);
        }
        System.out.println("全部数据量：" + listAll.size());
        System.out.println("去重之后的数据量：" + list.size());

        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(80);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSave3(content)));
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
    }

    /**
     * 调用中台数据，进行处理
     */
    private void getZhongTaiDatasAndSave(NoticeMQ noticeMQ) {

        boolean b = cusDataNewService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        //全部自提，不需要正文
        Map<String, Object> resultMap = cusDataNewService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            saveIntoMysql(resultMap);
        }
    }
    /**
     * 调用中台数据，进行处理-规则二
     */
    private void getZhongTaiDatasAndSave2(NoticeMQ noticeMQ) {

        boolean b = cusDataNewService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        //全部自提，不需要正文
        Map<String, Object> resultMap = cusDataNewService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            saveIntoMysql2(resultMap);
        }
    }
    /**
     * 调用中台数据，进行处理-规则三
     */
    private void getZhongTaiDatasAndSave3(NoticeMQ noticeMQ) {

        boolean b = cusDataNewService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        //全部自提，不需要正文
        Map<String, Object> resultMap = cusDataNewService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            saveIntoMysql3(resultMap);
        }
    }

    // 数据入库操作
    public static final String INSERT_ZT_RESULT_YILIAO = "INSERT INTO han_new_data_bd1 (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time,is_electronic,code,isfile,keyword_term,keywords, infoTypeSegment) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    // 数据入库操作-规则二
    public static final String INSERT_ZT_RESULT_YILIAO2 = "INSERT INTO han_new_data_bd2 (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time,is_electronic,code,isfile,keyword_term,keywords, infoTypeSegment) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    // 数据入库操作-规则三
    public static final String INSERT_ZT_RESULT_YILIAO3 = "INSERT INTO han_new_data_bd3 (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time,is_electronic,code,isfile,keyword_term,keywords, infoTypeSegment) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public void saveIntoMysql(Map<String, Object> map){
        bdJdbcTemplate.update(INSERT_ZT_RESULT_YILIAO,map.get("task_id"), map.get("keyword"), map.get("content_id"), map.get("title"),
                map.get("content"), map.get("province"), map.get("city"), map.get("country"), map.get("url"), map.get("baiLian_budget"),
                map.get("baiLian_amount_unit"), map.get("xmNumber"), map.get("bidding_type"), map.get("progid"), map.get("zhao_biao_unit"),
                map.get("relation_name"), map.get("relation_way"), map.get("agent_unit"), map.get("agent_relation_ame"),
                map.get("agent_relation_way"), map.get("zhong_biao_unit"), map.get("link_man"), map.get("link_phone"),
                map.get("registration_begin_time"), map.get("registration_end_time"), map.get("biding_acquire_time"),
                map.get("biding_end_time"), map.get("tender_begin_time"), map.get("tender_end_time"), map.get("update_time"),
                map.get("type"), map.get("bidder"), map.get("notice_types"), map.get("open_biding_time"), map.get("is_electronic"),
                map.get("code"), map.get("isfile"), map.get("keyword_term"),map.get("keywords"),map.get("infoTypeSegment"));
    }
    public void saveIntoMysql2(Map<String, Object> map){
        bdJdbcTemplate.update(INSERT_ZT_RESULT_YILIAO2,map.get("task_id"), map.get("keyword"), map.get("content_id"), map.get("title"),
                map.get("content"), map.get("province"), map.get("city"), map.get("country"), map.get("url"), map.get("baiLian_budget"),
                map.get("baiLian_amount_unit"), map.get("xmNumber"), map.get("bidding_type"), map.get("progid"), map.get("zhao_biao_unit"),
                map.get("relation_name"), map.get("relation_way"), map.get("agent_unit"), map.get("agent_relation_ame"),
                map.get("agent_relation_way"), map.get("zhong_biao_unit"), map.get("link_man"), map.get("link_phone"),
                map.get("registration_begin_time"), map.get("registration_end_time"), map.get("biding_acquire_time"),
                map.get("biding_end_time"), map.get("tender_begin_time"), map.get("tender_end_time"), map.get("update_time"),
                map.get("type"), map.get("bidder"), map.get("notice_types"), map.get("open_biding_time"), map.get("is_electronic"),
                map.get("code"), map.get("isfile"), map.get("keyword_term"),map.get("keywords"),map.get("infoTypeSegment"));
    }
    public void saveIntoMysql3(Map<String, Object> map){
        bdJdbcTemplate.update(INSERT_ZT_RESULT_YILIAO3,map.get("task_id"), map.get("keyword"), map.get("content_id"), map.get("title"),
                map.get("content"), map.get("province"), map.get("city"), map.get("country"), map.get("url"), map.get("baiLian_budget"),
                map.get("baiLian_amount_unit"), map.get("xmNumber"), map.get("bidding_type"), map.get("progid"), map.get("zhao_biao_unit"),
                map.get("relation_name"), map.get("relation_way"), map.get("agent_unit"), map.get("agent_relation_ame"),
                map.get("agent_relation_way"), map.get("zhong_biao_unit"), map.get("link_man"), map.get("link_phone"),
                map.get("registration_begin_time"), map.get("registration_end_time"), map.get("biding_acquire_time"),
                map.get("biding_end_time"), map.get("tender_begin_time"), map.get("tender_end_time"), map.get("update_time"),
                map.get("type"), map.get("bidder"), map.get("notice_types"), map.get("open_biding_time"), map.get("is_electronic"),
                map.get("code"), map.get("isfile"), map.get("keyword_term"),map.get("keywords"),map.get("infoTypeSegment"));
    }
}