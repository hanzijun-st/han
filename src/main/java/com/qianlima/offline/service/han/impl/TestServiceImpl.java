package com.qianlima.offline.service.han.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.offline.bean.*;
import com.qianlima.offline.rule02.MyRuleUtils;
import com.qianlima.offline.service.CusDataFieldService;
import com.qianlima.offline.service.NewBiaoDiWuService;
import com.qianlima.offline.service.ZhongTaiBiaoDiWuService;
import com.qianlima.offline.service.han.CurrencyService;
import com.qianlima.offline.service.han.TestService;
import com.qianlima.offline.util.*;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Administrator on 2021/1/12.
 */

@Service
@Slf4j
public class TestServiceImpl implements TestService{

    @Autowired
    private UpdateContentSolr contentSolr;
    @Autowired
    private ZhongTaiBiaoDiWuService bdwService;

    @Autowired
    private CusDataFieldService cusDataFieldService;

    @Autowired
    private NewBiaoDiWuService newBiaoDiWuService;

    @Autowired
    private MyRuleUtils myRuleUtils;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    private static final String INSERT_HAN_ALL = "INSERT INTO han_tab_all (id,json_id,contentid,content_source,sum,sumUnit,serialNumber,name," +
            "brand,model,number,numberUnit,price,priceUnit,totalPrice,totalPriceUnit,configuration_key,configuration_value,appendix_suffix) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    //mysql数据库中插入数据
    public String INSERT_ZT_RESULT_HXR = "INSERT INTO han_data (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time,is_electronic,code,isfile,keyword_term) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    public String INSERT_ZT_RESULT_HXR_COPY = "INSERT INTO han_data_copy (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time,is_electronic,code,isfile,keyword_term) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    @Override
    public void getBdw() {
        try {
            bdwService.getSolrAllField2();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getNewBdw(Integer type) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<Future> futureList = new ArrayList<>();
        //contentid
        List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT id,contentid FROM han_contentid");
        for (Map<String, Object> mapData : mapList) {
            futureList.add(executorService1.submit(() -> {
                newBiaoDiWuService.handleForData(Long.valueOf(mapData.get("contentid").toString()),type);
                log.info("新标的物方法--->:{}",mapData.get("contentid").toString()+"======="+mapData.get("id").toString());
            }));
        }
        for (Future future1 : futureList) {
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
        log.info("---------------===============================新标的物方法运行结束==================================");
    }


    @Override
    public void updateKeyword() {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<Future> futureList = new ArrayList<>();

        //String[] keywords ={"交换机","锐捷"};
        try {
            //如果多个关键词，标的物中追加关键词要合并在一块，多关键词合成一个文件
            List<String> keywords = LogUtils.readRule("keyWords");
            List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT id,contentid,name,brand,model FROM han_biaodiwu");
            if (mapList !=null && mapList.size() >0){
                List<Map<String,Object>> list = new ArrayList<>();

                for (Map<String, Object> map : mapList) {
                    String id = map.get("id").toString();
                    String contentid = map.get("contentid").toString();
                    String name = map.get("name").toString();
                    String brand = map.get("brand").toString();
                    String model = map.get("model").toString();

                    String key = "";
                    for (String keyword : keywords) {
                        if (name.contains(keyword) || brand.contains(keyword) || model.contains(keyword)){
                            key+=keyword+"、";
                        }
                    }
                    if (ZTStringUtil.isNotBlank(key)){
                        Map<String,Object> m = new HashMap<>();
                        m.put(id,key.substring(0,key.length() - 1));
                        list.add(m);
                    }
                }
                if (list !=null && list.size() > 0){
                    for (Map<String, Object> map : list) {
                        for(Map.Entry<String,Object> e :map.entrySet()){
                            if (e.getValue() !=null){
                                futureList.add(executorService1.submit(() -> {
                                    bdJdbcTemplate.update("UPDATE han_biaodiwu SET keyword = ? WHERE id = ?", e.getValue() , e.getKey());
                                }));
                            }
                        }
                    }
                }
                for (Future future1 : futureList) {
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
            e.printStackTrace();
        }

    }

    @Override
    public String downLoad() {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<Future> futureList = new ArrayList<>();
        try {
            ExcelUtil2<MdlInfo> util = new ExcelUtil2<MdlInfo>();
            // 准备数据
            List<MdlInfo> list = new ArrayList<>();
            List<Map<String, Object>> maps = bdJdbcTemplate.queryForList("SELECT id,contentid,data FROM han_tab");
            List<String> bugList = new ArrayList<>();
            if (maps !=null && maps.size() >0){
                for (Map<String, Object> map : maps) {
                    if (map.get("data") != null){
                        String json = map.get("data").toString();
                        if (StringUtils.isNotBlank(json)){
                            try {
                                MdlVo data = JSON.parseObject(json, MdlVo.class);
                                //JSONObject jsonBean = JSONObject.fromObject(json);
                                //MdlVo data = (MdlVo) JSON.parse(json);

                                if (data !=null){
                                    /*mdlInfo.setSum(data.getSum());
                                    mdlInfo.setSumUnit(data.getSumUnit());
                                    mdlInfo.setContentid(map.get("contentid").toString());*/
                                    if (data.getTargetDetails() !=null && data.getTargetDetails().size() >0){
                                        List<MdlTargetDetailsVo> targetDetails = data.getTargetDetails();
                                        if (targetDetails !=null && targetDetails.size() >0){
                                            for (MdlTargetDetailsVo targetDetail : targetDetails) {
                                                MdlInfo mdlInfo = new MdlInfo();
                                                mdlInfo.setSum(data.getSum());
                                                mdlInfo.setSumUnit(data.getSumUnit());
                                                mdlInfo.setContentid(map.get("contentid").toString());
                                                mdlInfo.setName(targetDetail.getName());
                                                mdlInfo.setNumber(targetDetail.getNumber());
                                                mdlInfo.setNumberUnit(targetDetail.getNumberUnit());
                                                mdlInfo.setSerialNumber(targetDetail.getSerialNumber());
                                                mdlInfo.setTotalPrice(targetDetail.getTotalPrice());
                                                mdlInfo.setTotalPriceUnit(targetDetail.getTotalPriceUnit());
                                                mdlInfo.setBrand(targetDetail.getBrand());
                                                mdlInfo.setModel(targetDetail.getModel());
                                                mdlInfo.setPrice(targetDetail.getPrice());
                                                mdlInfo.setPriceUnit(targetDetail.getPriceUnit());

                                                StringBuffer keyStr = new StringBuffer();
                                                if (targetDetail.getConfigurations() !=null && targetDetail.getConfigurations().size() >0){
                                                    List<MdlConfigurationsVo> configurations = targetDetail.getConfigurations();
                                                    for (MdlConfigurationsVo configuration : configurations) {
                                                        keyStr.append(configuration.getKey());
                                                        keyStr.append(":");
                                                        keyStr.append(configuration.getValue());
                                                        keyStr.append(";");
                                                    }
                                                }
                                                mdlInfo.setConfiguration_key(keyStr.toString());
                                                list.add(mdlInfo);
                                            }
                                        }
                                    }

                                }
                                //list.add(mdlInfo);
                            } catch (Exception e) {
                                bugList.add(map.get("contentid").toString());
                                log.error("解析任务参数失败, id :{},错误:{}", map.get("contentid").toString(), e);
                            }
                        }
                    }

                }
            }
            System.out.println(bugList.toString());
            //读取excel，获取数据源
            //Map<String, Object> map = XlsToXls.readXlsOne("E:\\23.xls", 0);

            //String[] columnNames = { "json_id","contentid","content_source","sum","sumUnit","serialNumber","name","brand","model","number","numberUnit","price","priceUnit","totalPrice","totalPriceUnit","configuration_key","configuration_value","appendix_suffix"};
           // util.exportExcel("用户导出", columnNames, list, new FileOutputStream("E:/test.xls"), ExcelUtil2.EXCEL_FILE_2003);
            for (MdlInfo mdlInfo : list) {
                futureList.add(executorService1.submit(() -> {
                    saveIntoMysql(MapUtil.beanToMap(mdlInfo));
                }));
            }
            for (Future future1 : futureList) {
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
            return "-------导出失败";
        }
        return "-------导出成功";
    }

    /**
     * 上海联影医疗
     */
    @Override
    public void getShangHaiLy() {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        try {
            List<String> pbKeys = LogUtils.readRule("pingbici");//屏蔽词
            List<String> blockKeys =LogUtils.readRule("blockKeys");//黑词
            List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20200118 TO 20210118] AND (progid:3 OR progid:5) AND zhaoBiaoUnit:*", "", 1);
            if (!mqEntities.isEmpty()) {
                for (NoticeMQ data : mqEntities) {
                    if (data.getTitle() != null) {
                        futureList1.add(executorService1.submit(() -> {
                            String title = data.getTitle();
                            //删除屏蔽词
                            for (String pbKey : pbKeys) {
                                if(title.contains(pbKey)){
                                    data.setTitle(title.replace(pbKey,""));
                                }
                            }

                            boolean flag = true;

                            if (flag) {
                                //行业标签
                                String zhaobiaoindustry = myRuleUtils.getIndustry(data.getZhaoBiaoUnit());
                                String ylUnit = zhaobiaoindustry.split("-")[0];
                                if (StringUtils.isNotBlank(zhaobiaoindustry)){
                                    if ("政府机构-医疗".equals(zhaobiaoindustry) || "医疗单位".equals(ylUnit) || "商业公司-医疗服务".equals(zhaobiaoindustry)){
                                        list1.add(data);
                                        if (!dataMap.containsKey(data.getContentid().toString())) {
                                            list.add(data);
                                            dataMap.put(data.getContentid().toString(), "0");
                                        }
                                    }
                                }
                            }
                        }));
                    }
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


            log.info("全部数据量：" + list1.size());
            log.info("去重之后的数据量：" + list.size());
            log.info("==========================");

            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(80);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> cusDataFieldService.getAllFieldsWithZiTi(content,true)));
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getChongqi() {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        try {
            String str = "重庆市";
            List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[20190101 TO 20210120] AND (progid:[0 TO 3] OR progid:5) AND allcontent:\"" + str + "\"","", 1);
            log.info( "————" + mqEntities.size());
            if (!mqEntities.isEmpty()) {
                for (NoticeMQ data : mqEntities) {
                    if (data.getTitle() != null) {
                        boolean flag = true;
                        if (flag) {
                            listAll.add(data);
                            if (!dataMap.containsKey(data.getContentid().toString())) {
                                list.add(data);
                                dataMap.put(data.getContentid().toString(), "0");
                            }
                        }
                    }
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


            } catch (Exception e) {
                e.printStackTrace();
            }


            //如果参数为1,则进行存表

            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(32);
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
            System.out.println("--------------------------------本次任务结束---------------------------------------");
    }

    @Override
    public void getZongHengDaPeng3(Integer type, String date) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        try {
            //关键词a
            List<String> aa = LogUtils.readRule("keyWords");
            //关键词b
            //List<String> bb = LogUtils.readRule("keyWordsB");

            //读取配置文件中的黑词
            List<String> blacks = LogUtils.readRule("blockKeys");

            //全文检索关键词a
            for (String str : aa) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + str + "\" ", str, 2);
                    log.info(str.trim() + "————" + mqEntities.size());
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
                                    String zhaobiaoindustry = myRuleUtils.getIndustry(data.getZhaoBiaoUnit());
                                    if (StringUtils.isNotBlank(zhaobiaoindustry)){
                                        if ("政府机构-公安".equals(zhaobiaoindustry) || "政府机构-自然资源".equals(zhaobiaoindustry)
                                                ||"政府机构-能源".equals(zhaobiaoindustry) ||"政府机构-水利水电".equals(zhaobiaoindustry)
                                                ||"政府机构-应急管理".equals(zhaobiaoindustry)  ||"商业公司-电气".equals(zhaobiaoindustry)
                                                ||"商业公司-燃气热力".equals(zhaobiaoindustry)
                                                ||"商业公司-石油化工".equals(zhaobiaoindustry)
                                                ||"商业公司-水利".equals(zhaobiaoindustry)
                                                ||"商业公司-新能源".equals(zhaobiaoindustry)
                                                ||"商业公司-消防安防".equals(zhaobiaoindustry)
                                                ||"商业公司-环保".equals(zhaobiaoindustry)
                                                ||"商业公司-林业".equals(zhaobiaoindustry)
                                                ){
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重之后的数据量：" + list.size());
            log.info("==========================");


            ArrayList<String> arrayList = new ArrayList<>();

            //关键词a
            for (String key :aa){
                arrayList.add(key);
            }
            for (String str : arrayList) {
                int total = 0;
                for (NoticeMQ noticeMQ : list) {
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



            if (type.intValue() == 1){
                if (list != null && list.size() > 0) {
                    ExecutorService executorService = Executors.newFixedThreadPool(80);
                    List<Future> futureList = new ArrayList<>();

                    for (NoticeMQ content : list) {
                        futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
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
            System.out.println("==========================================此程序运行结束========================================");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getHefeiHanglian(Integer type, String date) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        try {
            //关键词a
            List<String> aa = LogUtils.readRule("keyWordsA");
            //关键词b
            List<String> bb = LogUtils.readRule("keyWordsB");

            //读取配置文件中的黑词
            List<String> blacks = LogUtils.readRule("blockKeys");

            //全文检索关键词a AND 关键词b
            for (String str : aa) {
                for (String str2 : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND title:\"" + str + "\"  AND title:\"" + str2 + "\"", str+"&"+str2, 2);
                        log.info(str.trim() + "————" + mqEntities.size());
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

            //关键词a 和 关键词b
            for (String key : aa) {
                for (String str2 : bb) {
                    arrayList.add(key+"&"+str2);
                }
            }


            for (String str : arrayList) {
                int total = 0;
                for (NoticeMQ noticeMQ : list) {
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


            if (type.intValue() == 1){
                if (list != null && list.size() > 0) {
                    ExecutorService executorService = Executors.newFixedThreadPool(80);
                    List<Future> futureList = new ArrayList<>();

                    for (NoticeMQ content : list) {
                        futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
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
            System.out.println("==========================================此程序运行结束========================================");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getHanDanKaiFaQu(Integer type, String date) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        try {
           /* //关键词a
            List<String> aa = LogUtils.readRule("keyWordsA");
            //关键词b
            List<String> bb = LogUtils.readRule("keyWordsB");

            List<String> cc = LogUtils.readRule("keyWordsC");
            List<String> dd = LogUtils.readRule("keyWordsD");
            List<String> ee = LogUtils.readRule("keyWordsE");*/


            String[] aa={"城市","公路","街道","道路","马路","公测","公共厕所","市容","景区"};
            String[] bb={"保洁","收运","保洁","管养","管护","清运","环境管理","卫生管理"};
            String[] cc={"垃圾","环卫"};
            //String[] dd={"处理","清运","焚烧","填埋","整治","运输","回收","服务"};
            String[] dd={"处理","清运","焚烧","填埋","整治","运输","回收"};
            String[] ee={"环卫服务","环卫一体化","垃圾处理","垃圾回收","垃圾治理","农村清运","智慧环卫","市容管理"};

            //全文检索关键词a AND 关键词b
            for (String a : aa) {
                for (String b : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND progid:3 AND catid:[* TO 100] AND (newProvince:\"" + "河北省" + "\" OR newProvince:\"" + "山东省" + "\" OR newProvince:\"" + "山西省" + "\" ) AND allcontent:\"" + a + "\"  AND allcontent:\"" + b + "\"", a+"&"+b, 2);
                        log.info(a.trim() + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    if (flag){
                                        listAll.add(data);
                                        data.setKeyword(a+"&"+b);
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
            //全文检索关键词c AND 关键词d
            for (String c : cc) {
                for (String d : dd) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND progid:3 AND catid:[* TO 100] AND (newProvince:\"" + "河北省" + "\" OR newProvince:\"" + "山东省" + "\" OR newProvince:\"" + "山西省" + "\") AND allcontent:\"" + c + "\"  AND allcontent:\"" + d + "\"", c+"&"+d, 2);
                        log.info(c.trim() + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    if (flag){
                                        listAll.add(data);
                                        data.setKeyword(c+"&"+d);
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

            //关键字ee
            for (String keyWord : ee) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+ date + "] AND progid:3 AND catid:[* TO 100] AND (newProvince:\"" + "河北省" +"\" OR newProvince:\"" + "山东省" +"\" OR newProvince:\"" + "山西省" + "\") AND allcontent:\"" + keyWord + "\"",keyWord, 1);
                    //List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+ date + "] AND progid:3 AND catid:[* TO 100] AND (newProvince:\"" + "河北省" + "\" OR newProvince:\"" + "山东省" + "\" OR newProvince:\"" + "山西省" +") AND allcontent:\"" + keyWord + "\"",keyWord, 2);
                    log.info( "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    listAll.add(data);
                                    data.setKeyword(keyWord);
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重之后的数据量：" + list.size());
            log.info("==========================");


            ArrayList<String> arrayList = new ArrayList<>();

            //关键词a 和 关键词b
            for (String a : aa) {
                for (String b : bb) {
                    arrayList.add(a+"&"+b);
                }
            }
            //关键词c 和 关键词d
            for (String c : cc) {
                for (String d : dd) {
                    arrayList.add(c+"&"+d);
                }
            }
            for (String e : ee) {
                arrayList.add(e);
            }

            for (String str : arrayList) {
                int total = 0;
                for (NoticeMQ noticeMQ : list) {
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


            if (type.intValue() == 1){
                if (list != null && list.size() > 0) {
                    ExecutorService executorService = Executors.newFixedThreadPool(80);
                    List<Future> futureList = new ArrayList<>();

                    for (NoticeMQ content : list) {
                        futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
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
            System.out.println("==========================================此程序运行结束========================================");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getSiChuanYuYiYiLiao(Integer type, String date) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        try {
            //关键词a
            List<String> aa = LogUtils.readRule("keyWordsA");
            //关键词b
            List<String> bb = LogUtils.readRule("keyWordsB");

            List<String> cc = LogUtils.readRule("keyWordsC");
            List<String> dd = LogUtils.readRule("keyWordsD");

            String[] bt1 ={"B超","彩超","TCD"};//标题1
            String[] bt2 ={"CT","DR","DSA","MRI"};//标题2
            String[] bt3 ={"球管","探头","配件"};//标题3

            String[] blacks ={"救护车","电梯","空调"};//黑词

            //全文检索关键词a AND 标题关键词b
            for (String str : aa) {
                for (String str2 : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5)  AND catid:[* TO 100] AND newProvince:\"" + "四川省" +"\" AND allcontent:\"" + str + "\"  AND title:\"" + str2 + "\"", str+"&"+str2, 2);
                        log.info(str.trim() + "————" + mqEntities.size());
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
                                        String zhaobiaoindustry = myRuleUtils.getIndustry(data.getZhaoBiaoUnit());
                                        String[] split = zhaobiaoindustry.split("-");
                                        if (StringUtils.isNotBlank(zhaobiaoindustry)) {
                                            if ("政府机构-医疗".equals(zhaobiaoindustry) || "商业公司-医疗服务".equals(zhaobiaoindustry)
                                                    || "医疗单位".equals(split[0])) {
                                                listAll.add(data);
                                                data.setKeyword(str + "&" + str2);
                                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                                    list.add(data);
                                                    dataMap.put(data.getContentid().toString(), "0");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }));
                }
            }
            //标题搜索-bt1 AND 标题检索关键词b
            for (String str : bt1) {
                for (String str2 : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND newProvince:\"" + "四川省" + "\" AND title:\"" + str + "\"  AND title:\"" + str2 + "\"", str+"&"+str2, 2);
                        log.info(str.trim() + "————" + mqEntities.size());
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
                                        String zhaobiaoindustry = myRuleUtils.getIndustry(data.getZhaoBiaoUnit());
                                        String[] split = zhaobiaoindustry.split("-");
                                        if (StringUtils.isNotBlank(zhaobiaoindustry)) {
                                            if ("政府机构-医疗".equals(zhaobiaoindustry) || "商业公司-医疗服务".equals(zhaobiaoindustry)
                                                    || "医疗单位".equals(split[0])) {
                                                listAll.add(data);
                                                data.setKeyword(str + "&" + str2);
                                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                                    list.add(data);
                                                    dataMap.put(data.getContentid().toString(), "0");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }));
                }
            }
            //全文检索关键词c AND 标题检索关键词d
            for (String str : cc) {
                for (String str2 : dd) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND newProvince:\"" + "四川省" + "\" AND allcontent:\"" + str + "\"  AND title:\"" + str2 + "\"", str+"&"+str2, 2);
                        log.info(str.trim() + "————" + mqEntities.size());
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
                                        String zhaobiaoindustry = myRuleUtils.getIndustry(data.getZhaoBiaoUnit());
                                        String[] split = zhaobiaoindustry.split("-");
                                        if (StringUtils.isNotBlank(zhaobiaoindustry)) {
                                            if ("政府机构-医疗".equals(zhaobiaoindustry) || "商业公司-医疗服务".equals(zhaobiaoindustry)
                                                    || "医疗单位".equals(split[0])) {
                                                listAll.add(data);
                                                data.setKeyword(str + "&" + str2);
                                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                                    list.add(data);
                                                    dataMap.put(data.getContentid().toString(), "0");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }));
                }
            }
            //标题检索-bt2 AND 标题检索关键词d
            for (String str : bt2) {
                for (String str2 : dd) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND newProvince:\"" + "四川省" + "\" AND title:\"" + str + "\"  AND title:\"" + str2 + "\"", str+"&"+str2, 2);
                        log.info(str.trim() + "————" + mqEntities.size());
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
                                        String zhaobiaoindustry = myRuleUtils.getIndustry(data.getZhaoBiaoUnit());
                                        String[] split = zhaobiaoindustry.split("-");
                                        if (StringUtils.isNotBlank(zhaobiaoindustry)) {
                                            if ("政府机构-医疗".equals(zhaobiaoindustry) || "商业公司-医疗服务".equals(zhaobiaoindustry)
                                                    || "医疗单位".equals(split[0])) {
                                                listAll.add(data);
                                                data.setKeyword(str + "&" + str2);
                                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                                    list.add(data);
                                                    dataMap.put(data.getContentid().toString(), "0");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }));
                }
            }

            //标题检索-bt3
            for (String keyWord : bt3) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+ date + "] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND newProvince:\"" + "四川省" +"\" AND title:\"" + keyWord + "\"",keyWord, 1);
                    log.info( "————" + mqEntities.size());
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
                                    String zhaobiaoindustry = myRuleUtils.getIndustry(data.getZhaoBiaoUnit());
                                    String[] split = zhaobiaoindustry.split("-");
                                    if (StringUtils.isNotBlank(zhaobiaoindustry)) {
                                        if ("政府机构-医疗".equals(zhaobiaoindustry) || "商业公司-医疗服务".equals(zhaobiaoindustry)
                                                || "医疗单位".equals(split[0])) {
                                            listAll.add(data);
                                            data.setKeyword(keyWord);
                                            if (!dataMap.containsKey(data.getContentid().toString())) {
                                                list.add(data);
                                                dataMap.put(data.getContentid().toString(), "0");
                                            }
                                        }
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重之后的数据量：" + list.size());
            log.info("==========================");


            ArrayList<String> arrayList = new ArrayList<>();

            //关键词a 和 关键词b
            for (String key : aa) {
                for (String str2 : bb) {
                    arrayList.add(key+"&"+str2);
                }
            }
            //标题1 和 关键词b
            for (String key : bt1) {
                for (String str2 : bb) {
                    arrayList.add(key+"&"+str2);
                }
            }
            //关键词c 和 关键词d
            for (String c : cc) {
                for (String d : dd) {
                    arrayList.add(c+"&"+d);
                }
            }
            //标题2 和 关键词d
            for (String bt : bt2) {
                for (String d : dd) {
                    arrayList.add(bt+"&"+d);
                }
            }
            for (String bt : bt3) {
                arrayList.add(bt);
            }

            for (String str : arrayList) {
                int total = 0;
                for (NoticeMQ noticeMQ : list) {
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


            if (type.intValue() == 1){
                if (list != null && list.size() > 0) {
                    ExecutorService executorService = Executors.newFixedThreadPool(80);
                    List<Future> futureList = new ArrayList<>();

                    for (NoticeMQ content : list) {
                        futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave(content)));
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
            System.out.println("==========================================此程序运行结束========================================");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getJingWanWei(Integer type, String date) throws Exception{
        ExecutorService executorService1 = Executors.newFixedThreadPool(16);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        List<String> keyWords = LogUtils.readRule("keyWords");//中标单位
        try {
            for (String str : keyWords) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:3 AND zhongBiaoUnit:\"" + str + "\"", "", 1);
                    log.info(str+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重之后的数据量：" + list.size());
            log.info("==========================");


        } catch (Exception e) {
            e.printStackTrace();
        }


        //如果参数为1,则进行存表
        if (type.intValue() ==1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(32);
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

    //调用中台数据，进行处理
    private void getZhongTaiDatasAndSave(NoticeMQ noticeMQ) {
        boolean b = cusDataFieldService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }

        //全部自提，不需要正文
        Map<String, Object> resultMap = cusDataFieldService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            //判断中标单位联系方式是不是手机号
            //boolean link_phone = NumberUtil.validateMobilePhone(resultMap.get("link_phone").toString());
            //if (link_phone){
                saveIntoMysql(resultMap,INSERT_ZT_RESULT_HXR);
            //}
        }
    }

    public void saveIntoMysql(Map<String, Object> map ,String table){
        bdJdbcTemplate.update(table,map.get("task_id"), map.get("keyword"), map.get("content_id"), map.get("title"),
                map.get("content"), map.get("province"), map.get("city"), map.get("country"), map.get("url"), map.get("baiLian_budget"),
                map.get("baiLian_amount_unit"), map.get("xmNumber"), map.get("bidding_type"), map.get("progid"), map.get("zhao_biao_unit"),
                map.get("relation_name"), map.get("relation_way"), map.get("agent_unit"), map.get("agent_relation_ame"),
                map.get("agent_relation_way"), map.get("zhong_biao_unit"), map.get("link_man"), map.get("link_phone"),
                map.get("registration_begin_time"), map.get("registration_end_time"), map.get("biding_acquire_time"),
                map.get("biding_end_time"), map.get("tender_begin_time"), map.get("tender_end_time"), map.get("update_time"),
                map.get("type"), map.get("bidder"), map.get("notice_types"), map.get("open_biding_time"), map.get("is_electronic"),
                map.get("code"), map.get("isfile"), map.get("keyword_term"));
    }


    /**
     * 判断标题是否包含   包含当前对象则返回false
     * @param data
     * @param listStr
     * @return
     */
    private boolean getBoolean(NoticeMQ data,List<String> listStr){
        boolean flag = true;
        for (String str : listStr) {
            if(StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(str)){
                flag = false;
                break;
            }
        }
        return flag;
    }

    /**
     * 保存数据入库
     */
    public void saveIntoMysql(Map<String, Object> map){
        bdJdbcTemplate.update(INSERT_HAN_ALL,map.get("json_id"),map.get("contentid"),map.get("content_source"),
                map.get("sum"),map.get("sumUnit"),map.get("serialNumber"),map.get("name"),
                map.get("brand"),map.get("model"),map.get("number"),map.get("numberUnit"),
                map.get("price"),map.get("priceUnit"),map.get("totalPrice"),map.get("totalPriceUnit"),
                map.get("configuration_key"),map.get("configuration_value"),map.get("appendix_suffix"));
    }

    public void getDataFromZhongTaiAndSave(NoticeMQ noticeMQ) {
        boolean result = cusDataFieldService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = cusDataFieldService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            try {
                saveIntoMysql(resultMap,INSERT_ZT_RESULT_HXR);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
