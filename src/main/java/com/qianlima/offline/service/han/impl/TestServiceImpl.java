package com.qianlima.offline.service.han.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.offline.bean.*;
import com.qianlima.offline.rule02.MyRuleUtils;
import com.qianlima.offline.service.CusDataFieldService;
import com.qianlima.offline.service.NewBiaoDiWuService;
import com.qianlima.offline.service.ZhongTaiBiaoDiWuService;
import com.qianlima.offline.service.han.TestService;
import com.qianlima.offline.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
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
    private OnlineContentSolr onlineContentSolr;

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

    @Autowired
    @Qualifier("djeJdbcTemplate")
    private JdbcTemplate djeJdbcTemplate;

    private static final String INSERT_HAN_ALL = "INSERT INTO han_tab_all (id,json_id,contentid,content_source,sum,sumUnit,serialNumber,name," +
            "brand,model,number,numberUnit,price,priceUnit,totalPrice,totalPriceUnit,configuration_key,configuration_value,appendix_suffix) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String BJG = "INSERT INTO han_beijiangong (province,city,regLocation,unit) VALUES (?,?,?,?)";

    //mysql数据库中插入数据
    public String INSERT_ZT_RESULT_HXR = "INSERT INTO han_data (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time,is_electronic,code,isfile,keyword_term) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    //贝登---第二次数据
    public String INSERT_ZT_BEI_DENG2 = "INSERT INTO han_data_bd (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time,is_electronic,code,isfile,keyword_term,heici) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    //新加一级行业标签和二级行业标签字段
    public String INSERT_ZT_JIAOFU = "INSERT INTO han_data_wshh_jf (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time," +
            "is_electronic,code,isfile,keyword_term,zhao_first_industry,zhao_second_industry) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    @Override
    public void getBdw(Integer type) {
        try {
            bdwService.getSolrAllField2(type);
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
            ExportExcelUtil<MdlInfo> util = new ExportExcelUtil<MdlInfo>();
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
        String st = myRuleUtils.getIndustry("四川省巴中市南江县人民医院");
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
                if (str.equals("神经外科")){
                    System.out.println("str："+str);
                }
                for (String str2 : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5)  AND catid:[* TO 100] AND newProvince:\"" + "四川省" +"\" AND allcontent:\"" + str + "\"  AND title:\"" + str2 + "\"", str+"&"+str2, 2);
                        log.info(str.trim() + "————" + mqEntities.size());
                        if (!mqEntities.isEmpty()) {
                            for (NoticeMQ data : mqEntities) {
                                if (data.getContentid().longValue() == 208444381){
                                    System.out.println("关键词神经外科"+208444381);
                                }
                                if (data.getTitle() != null) {
                                    boolean flag = true;
                                    for (String black : blacks) {
                                        if(StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)){
                                            flag = false;
                                            break;
                                        }
                                    }
                                    if (flag){
                                        if ("四川省巴中市南江县人民医院".equals(data.getZhaoBiaoUnit())){
                                            System.out.println(data.getZhaoBiaoUnit());
                                        }
                                        if (StringUtils.isNotBlank(data.getZhaoBiaoUnit())){
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
                        }
                    }));
                }
            }
            //标题搜索-bt1 AND 标题检索关键词b
            for (String str : bt1) {
                for (String str2 : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5)  AND catid:[* TO 100] AND newProvince:\"" + "四川省" + "\" AND title:\"" + str + "\"  AND title:\"" + str2 + "\"", str+"&"+str2, 2);
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
                                        if (StringUtils.isNotBlank(data.getZhaoBiaoUnit())){
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
                                        if (StringUtils.isNotBlank(data.getZhaoBiaoUnit())){
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
                                        if (StringUtils.isNotBlank(data.getZhaoBiaoUnit())){
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
                                    if (StringUtils.isNotBlank(data.getZhaoBiaoUnit())){
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
                               /* if (flag) {
                                    listAll.add(data);
                                    data.setKeyword(keyWord);
                                    if (!dataMap.containsKey(data.getContentid().toString())) {
                                        list.add(data);
                                        dataMap.put(data.getContentid().toString(), "0");
                                    }
                                }*/
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

        //List<String> keyWords = LogUtils.readRule("keyWords");//中标单位
        String[] keyWords ={"国产化"};
        try {
            for (String str : keyWords) {
                futureList1.add(executorService1.submit(() -> {
                    //List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:3 AND newZhongBiaoUnit:\"" + str + "\"", "", 1);
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:3 AND allcontent:\"" + str + "\"", "", 1);
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


            ArrayList<String> arrayList = new ArrayList<>();


            //关键词c
            for (String key : keyWords){
                arrayList.add(key);
            }

            for (String str : arrayList) {
                int total = 0;
                for (NoticeMQ noticeMQ : list) {
                    String keyword = noticeMQ.getKeyword();
                    if (StrUtil.isNotEmpty(keyword)){
                        if (keyword.equals(str)) {
                            total++;
                        }
                    }
                }
                if (total == 0) {
                    continue;
                }
                System.out.println(str + ": " + total);
            }
            System.out.println("全部数据量：" + listAll.size());
            System.out.println("去重之后的数据量：" + list.size());

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
            String contentId = resultMap.get("content_id").toString();
            //进行大金额替换操作
            List<Map<String, Object>> maps = djeJdbcTemplate.queryForList("select info_id, winner_amount, budget from amount_code where info_id = ?", contentId);
            if (maps != null && maps.size() > 0){
                // 由于大金额处理的特殊性，只能用null进行判断
                String winnerAmount = maps.get(0).get("winner_amount") != null ? maps.get(0).get("winner_amount").toString() : null;
                if (winnerAmount != null){
                    resultMap.put("baiLian_amount_unit", winnerAmount);
                }
                String budget = maps.get(0).get("budget") != null ? maps.get(0).get("budget").toString() : null;
                if (budget != null){
                    resultMap.put("baiLian_budget", budget);
                }
            }
            //判断中标单位联系方式是不是手机号
            //boolean link_phone = NumberUtil.validateMobilePhone(resultMap.get("link_phone").toString());
            //if (link_phone){
                saveIntoMysql(resultMap,INSERT_ZT_RESULT_HXR);
            //}
        }
    }

    //专用，别的接口从新改
    public void saveIntoMysql(Map<String, Object> map ,String table){
        bdJdbcTemplate.update(table,map.get("task_id"), map.get("keyword"), map.get("content_id"), map.get("title"),
                map.get("content"), map.get("province"), map.get("city"), map.get("country"), map.get("url"), map.get("baiLian_budget"),
                map.get("new_amount_unit"), map.get("xmNumber"), map.get("bidding_type"), map.get("progid"), map.get("zhao_biao_unit"),
                map.get("relation_name"), map.get("relation_way"), map.get("agent_unit"), map.get("agent_relation_ame"),
                map.get("agent_relation_way"), map.get("new_zhong_biao_unit"), map.get("link_man"), map.get("link_phone"),
                map.get("registration_begin_time"), map.get("registration_end_time"), map.get("biding_acquire_time"),
                map.get("biding_end_time"), map.get("tender_begin_time"), map.get("tender_end_time"), map.get("update_time"),
                map.get("type"), map.get("bidder"), map.get("notice_types"), map.get("open_biding_time"), map.get("is_electronic"),
                map.get("code"), map.get("isfile"), map.get("keyword_term"));
    }

    public void saveIntoMysqlBd(Map<String, Object> map ,String table){
        bdJdbcTemplate.update(table,map.get("task_id"), map.get("keyword"), map.get("content_id"), map.get("title"),
                map.get("content"), map.get("province"), map.get("city"), map.get("country"), map.get("url"), map.get("baiLian_budget"),
                map.get("baiLian_amount_unit"), map.get("xmNumber"), map.get("bidding_type"), map.get("progid"), map.get("zhao_biao_unit"),
                map.get("relation_name"), map.get("relation_way"), map.get("agent_unit"), map.get("agent_relation_ame"),
                map.get("agent_relation_way"), map.get("zhong_biao_unit"), map.get("link_man"), map.get("link_phone"),
                map.get("registration_begin_time"), map.get("registration_end_time"), map.get("biding_acquire_time"),
                map.get("biding_end_time"), map.get("tender_begin_time"), map.get("tender_end_time"), map.get("update_time"),
                map.get("type"), map.get("bidder"), map.get("notice_types"), map.get("open_biding_time"), map.get("is_electronic"),
                map.get("code"), map.get("isfile"), map.get("keyword_term"),map.get("heici"));
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


    //贝登
    public void getDataFromZhongTaiAndSaveBd(NoticeMQ noticeMQ) {
        boolean result = cusDataFieldService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = cusDataFieldService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
           try {
               String contentId = resultMap.get("content_id").toString();
               //进行大金额替换操作
               List<Map<String, Object>> maps = djeJdbcTemplate.queryForList("select info_id, winner_amount, budget from amount_code where info_id = ?", contentId);
               if (maps != null && maps.size() > 0){
                   // 由于大金额处理的特殊性，只能用null进行判断
                   String winnerAmount = maps.get(0).get("winner_amount") != null ? maps.get(0).get("winner_amount").toString() : null;
                   if (winnerAmount != null){
                       resultMap.put("baiLian_amount_unit", winnerAmount);
                   }
                   String budget = maps.get(0).get("budget") != null ? maps.get(0).get("budget").toString() : null;
                   if (budget != null){
                       resultMap.put("baiLian_budget", budget);
                   }
               }
               saveIntoMysqlBd(resultMap,INSERT_ZT_BEI_DENG2);
                log.info("数据库存储--->{}",noticeMQ.getContentid());
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    public void getDataFromZhongTaiAndSave(NoticeMQ noticeMQ) {
        boolean result = cusDataFieldService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = cusDataFieldService.getAllFieldsWithHunHe(noticeMQ, false);
        if (resultMap != null) {
           try {
               String contentId = resultMap.get("content_id").toString();
               //进行大金额替换操作
               List<Map<String, Object>> maps = djeJdbcTemplate.queryForList("select info_id, winner_amount, budget from amount_code where info_id = ?", contentId);
               if (maps != null && maps.size() > 0){
                   // 由于大金额处理的特殊性，只能用null进行判断
                   String winnerAmount = maps.get(0).get("winner_amount") != null ? maps.get(0).get("winner_amount").toString() : null;
                   if (winnerAmount != null){
                       resultMap.put("baiLian_amount_unit", winnerAmount);
                   }
                   String budget = maps.get(0).get("budget") != null ? maps.get(0).get("budget").toString() : null;
                   if (budget != null){
                       resultMap.put("baiLian_budget", budget);
                   }
               }
               saveIntoMysql(resultMap,INSERT_ZT_JIAOFU);
               log.info("数据库存储--->{}",noticeMQ.getContentid());
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    public void getDataFromZhongTaiAndSaveZhongBiaoJinE(NoticeMQ noticeMQ) {
        boolean result = cusDataFieldService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = cusDataFieldService.getAllFieldsWithHunHe(noticeMQ, false);
        if (resultMap != null) {
           try {
               if (checkAmount(noticeMQ.getNewAmountUnit())) {
                   boolean b = handleInfoId3(resultMap);
                   if (b){
                       saveIntoMysql(resultMap,INSERT_ZT_RESULT_HXR);
                   }
               }
               log.info("数据库存储--->{}",noticeMQ.getContentid());
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 中标单位取前两个
     */
    public void getDataFromZhongTaiAndSave6(NoticeMQ noticeMQ) {
        boolean result = cusDataFieldService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = cusDataFieldService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            String unit = handleInfoId(resultMap);
            if (StringUtils.isNotBlank(unit)){
                resultMap.put("zhong_biao_unit",unit);
                cusDataFieldService.saveIntoMysql(resultMap);
            }
        }

    }
    //中标单位取 前两个
    private String handleInfoId(Map<String, Object> map) {
//        String infoId = map.get("infoId").toString();
        String keyword = map.get("keyword") != null ? map.get("keyword").toString() : "";
        String zhongUnit = map.get("zhong_biao_unit") != null ? map.get("zhong_biao_unit").toString() : "";
        String newUnit = "";
        String[] split = zhongUnit.split("、");
        if (split.length == 1){
            newUnit = zhongUnit;
        } else {
            for (int i = 0; i < split.length; i++) {
                String unit = split[i];
                if (i == 0){
                    newUnit += unit + ConstantBean.RULE_SEPARATOR;
                } else {
                    if (newUnit.contains(keyword)){
                        newUnit += unit + ConstantBean.RULE_SEPARATOR;
                        break;
                    } else {
                        if (unit.contains(keyword)){
                            newUnit += unit + ConstantBean.RULE_SEPARATOR;
                            break;
                        }
                    }
                }
            }
            //
            if (StringUtils.isNotBlank(newUnit)){
                newUnit = newUnit.substring(0, newUnit.length() - 1);
            }
        }
        return newUnit;
    }


    //混合中标单位输出个数≤3家
    private boolean handleInfoId3(Map<String, Object> map) {
        //String keyword = map.get("keyword") != null ? map.get("keyword").toString() : "";
        String newZhongBiaoUnit = map.get("new_zhong_biao_unit") != null ? map.get("new_zhong_biao_unit").toString() : "";//混合中标单位
        String[] split = newZhongBiaoUnit.split(",");
        if (split.length <= 3){
            return true;
        }
        return false;
    }

    public void getDaoJinSolrAllField() {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> list1 = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] aa1 = {"拍片机","医用X线机","高频X线机","X线摄影系统","医用诊断X光","高频X光机","摄影X光机","医用x光机","数字x光机","摄影X射线机","高频X射线摄影机","X射线拍片机","X线拍片机","X射线机","体检透视机","医学拍片","医用拍片","医疗拍片","拍片仪","拍片设备","床旁机","床边X线机","床旁X光机","移动X光机","移动DR","床边机","骨科小C","C型臂","C形臂","C臂","小C臂","介入C臂","影增C臂","移动式C形臂","骨科小型C","小型C臂","移动式C形臂X射线机","胃肠机","多功能X线机","遥控X线机","胃肠系统","遥控X光机","胃肠诊断","遥控医用诊断X射线机","X光透视拍片机","多功能数字化胃肠X线机","多功能数字化胃肠造影X光机","数字化X射线遥控透视摄影系统","数字化遥控胃肠X光机","数字胃肠","数字化透视摄影系统","数字多功能X光","平板多功能X线透视","动态平板透视摄影系统","动态平板","透视摄影X射线机","数字化透视摄影X射线机","胃肠X射线机","医用诊断X射线透视摄影系统","X射线胃肠诊断床","数字化透视X射线机","医用诊断X射线透视摄影系统","胃肠X射线机","医学透视摄影","医用透视摄影","医疗透视摄影","胃肠机","透视摄影仪","透视摄影机","透视摄影设备","x光透视机","透视X射线机","数字X","数字化X","平板X","平板摄影","平板摄片","直接X","X线数字","数字化X射线成像系统","平板DR","医用诊断X射线机","数字X线摄影","计算机X线摄影","动态DR","U臂DR","数字化医用X射线影像系统","悬吊DR","医学X光","医学X线","医学X射线","医学DR","医用X光","医用X线","医用X射线","医用DR","医疗X光","医疗X线","医疗X射线","医疗DR","X光设备","X线机","X线设备","X射线机","X射线设备","DR仪","DR机","DR设备","血管机组","血管机","血管造影","大C","大型血管介入治疗","外周血管造影机","大型血管介入治疗系统","大型心血管介入治疗系统","平板血管机","平板血管造影机","大型平板心血管介入治疗系统","直接转换型平板血管机","直接转换式平板血管造影机","数字减影","血管造影X射线机","数字X线血管机","血管造影设备","减影仪","减影机","减影设备","剪影血管造影仪","正电子发射型计算机断层显像","正电子发射断层成像设备","骨密度","X线双能量","骨密度仪","骨密度检测仪","双能量X线","双能X线","X射线骨密度仪","双能X射线骨密度仪","骨密度机","骨密度设备"};
        String[] aa2 = {"DR","DSA","PET","PET-CT","PET/CT","PETCT"};
        String[] bbb = {"X光机","X射线"};
        String[] blacks = {"口腔X射线","乳腺X射线","周口X射线","牙科x光机","车载X射线机","携带式X射线机","微型X射线机","牙科X射线机","乳腺X射线机","口腔X射线机","口腔全景X射线机","口腔颌面全景X射线机","口腔数字化体层摄影X射线机","口腔颌面锥形束计算机体层摄影设备","肢体数字化体层摄影X射线机","肢体锥形束计算机体层摄影设备","X射线放射治疗机","X射线放射治疗系统","体检机","口腔CBCT","牙科影像板","牙科X射线机","CBCT","泌尿X射线机","牙科CBCT","医用小型X光机","X射线摄影床","X射线摄影床","遥测监护系统","心电遥测系统","远程监护系统","中央监护系统","中央监护仪","数字化X射线影像处理软件","X射线平板探测器","X射线CCD探测器","X射线动态平板探测器","数字平板探测器成像系统","乳腺数字化体层摄影X射线机","透视摄影X射线机","数字化透视摄影X射线机","医用诊断X射线透视摄影系统","乳腺X射线摄影系统","X射线影像计算机辅助诊断软件","X射线发生装置","X射线血管造影影像处理软件","血管内超声诊断系统","血管内超声诊断仪","体外冲击波心血管治疗系统","X射线计算机断层成像系统","X射线计算机体层摄影设备","X射线摄影用影像板成像装置","影像板扫描仪","X射线立体定向放射外科治疗系统","X射线放射治疗机","X射线放射治疗系统","超声骨密度仪","放射性核素骨密度仪","口腔","牙科","齿科","乳腺","改造工程","配套设备","改造项目","用户改造","机房改造","家具","场地改造","药物采购","药物单一","网关升级","服务器","保修","维保","保养","维修","修理","维养","口牙","耗材","配件","备件","身体检查","体检项目","职工体检","新生体检","体检服务","干部体检","体检采购","入学体检","防护工程","防护项目","印刷服务","维护","检测服务","计量检测","检测项目","环评服务","验收服务","健康管理服务","预控评服务","移机","后勤保障服务","技术服务项目","护工服务","安保服务","物业服务","保安服务","保洁服务","检定服务","防护预评价","防护服务","防护评价","标识","委托项目","年度检测","锅炉","查体服务","健康检查服务","螺旋风管","舾装件","DR胶片机","硒鼓","设计图","柱塞泵","双屏机","复印机","图强","电动执行器","设备搬迁","打印机","劳务派遣","配电箱","高压注射器","体膜","球管","零件","部件","螺旋CT","定位CT","层CT","排CT","滑轨CT","门诊CT","台CT","院CT","用CT","方舱CT","移动CT"};


        for (String a1 : aa1) {
            futureList1.add(executorService1.submit(() -> {
                String key = a1 ;
                List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[20150101 TO 20200131] AND progid:3 AND allcontent:\"" + a1 + "\" ", key, 1);
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
                                data.setKeyword(key);
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

        for (String a2 : aa2) {
            futureList1.add(executorService1.submit(() -> {
                String key = a2 ;
                List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[20150101 TO 20200131] AND progid:3 AND title:\"" + a2 + "\" ", key, 1);
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
                                data.setKeyword(key);
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

        for (String bb : bbb) {
            futureList1.add(executorService1.submit(() -> {
                String key = bb ;
                //                                                                        yyyymmdd:[20150101 TO 20200131] AND progid:3 AND ( zhaoFirstIndustry:"医疗单位" OR (zhaoFirstIndustry:"政府机构" AND zhaoSecondIndustry:"医疗") OR ( zhaoFirstIndustry:"商业公司" AND zhaoSecondIndustry:"医疗服务" ) OR zhaoBiaoUnit:"监狱" )
                List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[20150101 TO 20200131] AND progid:3 AND ( zhaoFirstIndustry:" + "医疗单位" + " OR (zhaoFirstIndustry:" + "政府机构" + " AND zhaoSecondIndustry:" + "医疗" + " ) OR ( zhaoFirstIndustry:" + "商业公司" + " AND zhaoSecondIndustry:" + "医疗服务" + ") OR zhaoBiaoUnit:" + "监狱" + " ) AND allcontent:\"" + bb + "\" ", key, 2);
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
                                data.setKeyword(key);
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
                futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSave6(content)));
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
 * 北京宇信科技集团股份有限公司
     * @param type
     * @param date
     */
    @Override
    public void getYuxin(Integer type, String date) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        try {

            String[] aa={"中电文思海辉","长亮科技","上海安硕信息"};
            //全文检索关键词a AND 关键词b
            for (String keyWord : aa) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+ date + "] AND progid:3 AND catid:[* TO 100]  AND newZhongBiaoUnit:\"" + keyWord + "\"",keyWord, 1);
                    log.info(keyWord+"————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                if (flag) {
                                    /*String zhaobiaoindustry = myRuleUtils.getIndustry(data.getZhaoBiaoUnit());
                                    if (StringUtils.isNotBlank(zhaobiaoindustry)) {
                                        if ("金融企业-银行".equals(zhaobiaoindustry) || "金融企业-合作社".equals(zhaobiaoindustry)) {
                                            listAll.add(data);
                                            data.setKeyword(keyWord);
                                            if (!dataMap.containsKey(data.getContentid().toString())) {
                                                list.add(data);
                                                dataMap.put(data.getContentid().toString(), "0");
                                            }
                                        }
                                    }*/
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
           /* String[] bb={"银行","合作社"};
            for (String str : aa) {
                for (String str2 : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND progid:3 AND catid:[* TO 100] AND newZhongBiaoUnit:\"" + str + "\"  AND title:\"" + str2 + "\"", str+"&"+str2, 2);
                        log.info(str.trim() + "————" + mqEntities.size());
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

            //关键词aa
            for (String a : aa) {
                arrayList.add(a);
            }

            //关键词a 和 关键词b
           /* for (String key : aa) {
                for (String str2 : bb) {
                    arrayList.add(key+"&"+str2);
                }
            }*/
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
    public void getYuxin2(Integer type, String date) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        try {

            String[] aa={"信贷","数据","个贷","微贷","数字","智能","智慧","网贷","云端","云盘","云政"};

            //自提招标单位检索“行业标签”中标黄部分  AND  标题检索关键词aa
            for (String keyWord : aa) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+ date + "] AND progid:3 AND catid:[* TO 100] AND zhaoFirstIndustry:\""+"金融企业"+"\"  AND title:\"" + keyWord + "\"",keyWord, 1);
                    log.info(keyWord+"————" + mqEntities.size());
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

            List<String> bb = LogUtils.readRule("keyWords");
            for (String keyWord : bb) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+ date + "] AND progid:3 AND catid:[* TO 100] AND zhaoFirstIndustry:\"" + "金融企业" + "\"  AND allcontent:\"" + keyWord + "\"",keyWord, 1);
                    log.info(keyWord+"————" + mqEntities.size());
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

            //关键词aa
            for (String a : aa) {
                arrayList.add(a);
            }
            //关键词bb
            for (String b : bb) {
                arrayList.add(b);
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
    public void getYuxin3(Integer type, String date) throws  Exception{
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> list2 = new ArrayList<>();//去重后的数据-联系方式
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        HashMap<String, String> dataMap2 = new HashMap<>();//联系方式
        List<Future> futureList1 = new ArrayList<>();

        List<NoticeMQ> list3 = new ArrayList<>();//去手机号数据统计(不包括关键词)
        List<NoticeMQ> list4 = new ArrayList<>();//去手机号数据统计

        List<String> keyWords = LogUtils.readRule("keyWords");
        try {
            //自提招标单位检索“行业标签”中标黄部分  AND  标题检索关键词aa
            //futureList1.add(executorService1.submit(() -> {
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+ date + "] AND progid:3 AND catid:[* TO 100] AND zhongRelationWay:*","", 1);
                if (!mqEntities.isEmpty()) {
                    System.out.println("solr所有数据量："+mqEntities.size());
                    for (NoticeMQ data : mqEntities) {
                        if (NumberUtil.validateMobilePhone(data.getZhongRelationWay())){
                            list3.add(data);
                            if (keyWords.contains(data.getZhongBiaoUnit())){
                                listAll.add(data);
                                //data.setKeyword(keyWord);
                                if (!dataMap.containsKey(data.getContentid().toString())) {
                                    list.add(data);
                                    dataMap.put(data.getContentid().toString(), "0");
                                }
                                if (!dataMap2.containsKey(data.getZhongRelationWay())){
                                    list2.add(data);
                                    dataMap2.put(data.getZhongRelationWay().toString(), "0");
                                }
                            }
                        }
                    }
                }
           // }));

            /*for (Future future1 : futureList1) {
                try {
                    future1.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    executorService1.shutdown();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService1.shutdown();*/


            log.info("全部数据量：" + listAll.size());
            log.info("去重之后的数据量：" + list.size());
            log.info("去重之后的数据量-联系方式：" + list2.size());
            log.info("去手机号数据统计(不包括关键词)：" + list3.size());
            log.info("==========================");


            /*for (String str : arrayList) {
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
            }*/
            System.out.println("全部数据量：" + listAll.size());
            System.out.println("去重之后的数据量：" + list.size());
            System.out.println("去重之后的数据量-联系方式：" + list2.size());
            System.out.println("去手机号数据统计(不包括关键词)：" + list3.size());


            if (type.intValue() == 1){
                if (list != null && list2.size() > 0) {
                    ExecutorService executorService = Executors.newFixedThreadPool(80);
                    List<Future> futureList = new ArrayList<>();

                    for (NoticeMQ content : list2) {
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
    public void getYuxin1_4(Integer type, String date) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        try {

            String[] aa={"文思海辉"};

            //自提招标单位检索“行业标签”中标黄部分  AND  标题检索关键词aa
            for (String keyWord : aa) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+ date + "] AND progid:3 AND catid:[* TO 100] AND newZhongBiaoUnit:\"" + keyWord + "\"",keyWord, 1);
                    log.info(keyWord+"————" + mqEntities.size());
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

            //关键词aa
            for (String a : aa) {
                arrayList.add(a);
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
    public void getError(Integer type, String date) {
        List<String> idsFile = null;
        List<Map<String,Object>> listMap = new ArrayList<>();
        try {
            idsFile = LogUtils.readRule("idsFile");
            for (String s : idsFile) {
                boolean b = cusDataFieldService.checkStatus(s);//范围 例如:全国
                if (!b) {
                    log.info("contentid:{} 对应的数据状态不是99, 丢弃", s);
                    return;
                }

                NoticeMQ noticeMQ = new NoticeMQ();
                noticeMQ.setContentid(Long.valueOf(s));
                //全部自提，不需要正文
                try {
                    Map<String, Object> resultMap = cusDataFieldService.getAllFieldsWithZiTi(noticeMQ, false);
                    String str = resultMap.get("zhao_biao_unit").toString();
                    if (StringUtils.isNotBlank(str)){
                        String zhaobiaoindustry = myRuleUtils.getIndustry(str);
                        String[] split = zhaobiaoindustry.split("-");
                        if (StringUtils.isNotBlank(zhaobiaoindustry)) {
                            if ("政府机构-医疗".equals(zhaobiaoindustry) || "商业公司-医疗服务".equals(zhaobiaoindustry)
                                    || "医疗单位".equals(split[0])) {
                                Map<String,Object> map = new HashMap<>();
                                map.put(resultMap.get("content_id").toString(),resultMap.get("zhao_biao_unit"));
                                listMap.add(map);
                            }
                        }
                    }
                    if (resultMap != null) {
                        //saveIntoMysql(resultMap,INSERT_ZT_RESULT_HXR);

                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println(listMap);
            log.info("--------该接口运行结束--------");
        } catch (IOException e) {

        }

    }

    @Override
    public Map getBeiJianGong(String units) {
        List<String> errorList = new ArrayList<>();
        try {
            List<String> keyWords = LogUtils.readRule("keyWords");
            for (String unit : keyWords) {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(60000)
                        .setSocketTimeout(60000).setConnectTimeout(60000).build();
                HttpPost post = new HttpPost("http://118.190.158.164:8088/api/area");
                //创建参数列表
                List<NameValuePair> list = new ArrayList<NameValuePair>();
                list.add(new BasicNameValuePair("unit", unit));
                post.setEntity(new UrlEncodedFormEntity(list, "UTF-8"));
                //url格式编码
                post.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
                //设置超时时间为60秒
                post.setConfig(requestConfig);
                //执行请求
                CloseableHttpResponse httpResponse = httpClient.execute(post);
                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    String entity = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
                    JSONObject jsonObject = JSON.parseObject(entity);
                    //成功
                    if (jsonObject.getInteger("code") == 0) {
                        String data = jsonObject.getString("data");
                        Map map = (Map) JSONObject.parse(data);
                        bdJdbcTemplate.update(BJG,map.get("province"), map.get("city"), map.get("regLocation"),unit);

                        log.info("工商信息存库,单位是--->{}",unit);
                    /*if (StringUtils.isNotEmpty(data)) {
                        return data;
                    }*/
                    } else {
                        errorList.add(unit);//记录错误的单位
                        log.error("工商信息获取错误");
                        throw new RuntimeException("调用工商信息服务报错");
                    }
                    Map maps = (Map)JSON.parse(entity);
                    //return maps;
                }
            }
            System.out.println(errorList);
        }catch (Exception e) {
            log.error("工商信息判断出错:{}", e);
            throw new RuntimeException("工商信息判断出错");
        }

        return null;
    }





    private static final String SELECT_SQL_01 = "SELECT id,zhao_biao_unit FROM loiloi_data where zhao_biao_unit is not null and zhao_biao_unit !='' AND keyword is null and code is null";
    private static final String UPDATE_SQL_01 = "UPDATE loiloi_data SET keyword = ?,code = ? WHERE id = ? ";

    //KA自用行业___根据contentid匹配行业标签
    public void getKaHangYeSolrAllField() {

        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<Future> futureList1 = new ArrayList<>();
        List<Map<String, Object>> maps = bdJdbcTemplate.queryForList(SELECT_SQL_01);
        for (Map<String, Object> map : maps) {
            futureList1.add(executorService1.submit(() -> {
                try {
                     searchingHyAllData(map);
                } catch (IOException e) {
                    e.printStackTrace();
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

    }

    /**
     * 文思海辉
     * @param type
     * @param date
     */
    @Override
    public void getWenSiHaiHuib(Integer type, String date) throws Exception{
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> listAll = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] a = {"RAP","信创","智慧","天眼","信息技术应用创新","IOT","区块链","物联网","互联网+","IT信息产业转型","数字中国","信创产业链","新基建","新型基础建设","信创项目","国家信创园","信创产业园","信创发展","信创通用技术","信创产品","信创体系","SCM","天网","车联网","车路网","街数字","楼数字","物联卡","EAM","数字化","技术服务"};
        String[] dw = {"电力","电厂","电场","制造","汽车","教育","通信","能源","大学","学校","本科","小学","幼儿园","高校","高职","党校","军校","艺校","专升本","理科","文科","医科","学院","师范","院校","理工","体校","医校","技校","中专","职高","职中","中学","附小","托儿所","培训中心","职教中心","教育中心","自然考试","成人教育","远程教育","电网","发电","国电","供电","煤电","核电","水电","电能","风电","电站","热电","电建","华电集团","大唐集团","电务公司","粤电","能耗","能效","节能","风能","地热能","潮汐能","太阳能","电池","核能","中核","加工","装备","重工","轻工","纺织","钢铁","型材","板材","柴油机","不锈钢","电器","材料","家具","机械","空调","印刷","纸业","工业","云南云铝","铜业","锡业","精密铸造","轮胎","电梯","橡胶","润滑油","制品","铝业","海尔集团","电动车","轿车","网约车","商用车","特种车","越野车","工业车","出租车","机动车","旅游车","二手车","共享车","重汽","手机","信号","基站","通讯","信息技术","信息科技","信息安全","信息网络","信息产业","有线网络","有限网络","无线电","辅导","课程","培训","家教","燃气管理","供热","矿产资源","钻井","地矿","石油管理","矿山","矿产","煤矿","石油化工","供力","油田","电业局"};
        List<String> b = LogUtils.readRule("keyWords");
        String[] blacks = {"物业管理","保洁服务","安保服务","保安服务","物业服务","硬件","配件","耗材","家具","办公用品","组件","混凝土","钢管","配电柜","复印机","打印机","粉盒","硒鼓","辅材","标牌","标示牌","宣传视频","宣传物资","慰问品","印刷品","印刷服务","电梯","空调","综合布线","电线","电缆","管件","后勤服务","后勤管理","公务用车","用车服务","公务车","车辆采购","商务车","物业保洁","印刷采购"};


        for (String str : a) {
            for (String str2 : dw) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5)  AND catid:[* TO 100] AND title:\"" + str + "\"  AND allcontent:\"" + str2 + "\"", str+"&"+str2, 2);
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
        for (String str : b) {
            for (String str2 : dw) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + str + "\"  AND allcontent:\"" + str2 + "\"", str+"&"+str2, 2);
                    log.info(str.trim()+"" + "————" + mqEntities.size());
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

        //关键词a 和 定位词
        for (String key : a) {
            for (String str2 : dw) {
                arrayList.add(key+"&"+str2);
            }
        }
        //关键词a 和 定位词
        for (String key : b) {
            for (String str2 : dw) {
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

        if (type.intValue() ==1){
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
    }

    @Override
    public void getWenSiHaiHuib2_1(Integer type, String date) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> listAll = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] a = {"RAP","信创","天眼","SCM","EAM"};
        String[] b = {"安全监控","供应链运营管理系统","供应链信息系统","供应链金融平台","供应链金融服务平台","供应链系统解决方案","供应链信息平台","供应链大数据应用项目","供应链数字金融项目","供应链协同服务平台","供应链综合服务平台","供应链业务系统","供应链综合管理平台","供应链综合管理系统","供应链协同服务系统","供应链综合服务系统","供应链金融系统","供应链服务平台","供应链服务系统","供应链业务平台","固定资产管理信息系统","固定资产管理系统","固定资产管理信息平台","固定资产管理平台","资产经营管理系统","实物资产系统","资产管理系统","资产管理云平台","资产管理平台","资产托管系统","资产管理软件","资产交易管理信息平台","资产交易平台","资产管理信息化系统","资产数字管理平台","资产精细化管理综合平台","资产监督管理平台","资产监督平台","资产监督管理信息化系统","资产一体化管理平台","资产综合信息管理平台","资产管理信息系统","资产数字化运营管理平台","资产数字化管理平台","资产数字化管理系统","资产数字化运营管理系统","资产一体化管理系统","资产数字管理系统","资产动态管理云平台","资产动态管理系统","资产动态管理平台","资产管理信息化平台","资产盘点系统","资产盘点平台","资产盘点软件","资产盘点管理系统","资产盘点管理平台","资产盘点管理软件","资产管理工具软件","接口管理平台","接口文档管理平台","接口文档管理工具","智慧园区","智慧城市","安保监控","安防监控"};

        String[] bq ={"教育","能源","电力","新能源","制造","汽车","通信","教育服务"};
       /* for (String str : a) {

                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = fbsContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:0 OR progid:3)  AND catid:[* TO 100] AND title:\"" + str + "\"" , str, 2);
                    log.info(str.trim()+ "————" + mqEntities.size());
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
        for (String str : b) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:0 OR progid:3) AND catid:[* TO 100] AND allcontent:\"" + str +  "\"", str, 2);
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

        }*/

        for (String str : a) {
            for (String str2 : bq) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:0 OR progid:3)  AND catid:[* TO 100] AND title:\"" + str + "\"  AND zhaoSecondIndustry:\"" + str2 + "\"", str+"&"+str2, 2);
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
        for (String str : b) {
            for (String str2 : bq) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:0 OR progid:3) AND catid:[* TO 100] AND allcontent:\"" + str + "\"  AND zhaoSecondIndustry:\"" + str2 + "\"", str+"&"+str2, 2);
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

        //关键词a 和 定位词
        for (String key : a) {
            for (String str2 : bq) {
                arrayList.add(key+"&"+str2);
            }
            //arrayList.add(key);
        }
        //关键词a 和 定位词
        for (String key : b) {
            for (String str2 : bq) {
                arrayList.add(key+"&"+str2);
            }
            //arrayList.add(key);
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

        if (type.intValue() ==1){
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
    }

    @Override
    public void getWenSiHaiHuib2_2(Integer type, String date) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> listAll = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] a = {"RAP","信创","天眼","SCM","EAM"};
        String[] b = {"安全监控","供应链运营管理系统","供应链信息系统","供应链金融平台","供应链金融服务平台","供应链系统解决方案","供应链信息平台","供应链大数据应用项目","供应链数字金融项目","供应链协同服务平台","供应链综合服务平台","供应链业务系统","供应链综合管理平台","供应链综合管理系统","供应链协同服务系统","供应链综合服务系统","供应链金融系统","供应链服务平台","供应链服务系统","供应链业务平台","固定资产管理信息系统","固定资产管理系统","固定资产管理信息平台","固定资产管理平台","资产经营管理系统","实物资产系统","资产管理系统","资产管理云平台","资产管理平台","资产托管系统","资产管理软件","资产交易管理信息平台","资产交易平台","资产管理信息化系统","资产数字管理平台","资产精细化管理综合平台","资产监督管理平台","资产监督平台","资产监督管理信息化系统","资产一体化管理平台","资产综合信息管理平台","资产管理信息系统","资产数字化运营管理平台","资产数字化管理平台","资产数字化管理系统","资产数字化运营管理系统","资产一体化管理系统","资产数字管理系统","资产动态管理云平台","资产动态管理系统","资产动态管理平台","资产管理信息化平台","资产盘点系统","资产盘点平台","资产盘点软件","资产盘点管理系统","资产盘点管理平台","资产盘点管理软件","资产管理工具软件","接口管理平台","接口文档管理平台","接口文档管理工具","智慧园区","智慧城市","安保监控","安防监控"};

        String[] bq ={"教育","能源","电力","新能源","制造","汽车","通信","教育服务"};


        /*for (String str : a) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:0 OR progid:3) AND catid:[* TO 100] AND title:\"" + str + "\"", str, 2);
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
        for (String str : b) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:0 OR progid:3) AND catid:[* TO 100] AND title:\"" + str + "\"", str, 2);
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

        }*/

        for (String str : a) {
            for (String str2 : bq) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:0 OR progid:3) AND catid:[* TO 100] AND title:\"" + str + "\"  AND zhaoSecondIndustry:\"" + str2 + "\"", str+"&"+str2, 2);
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
        for (String str : b) {
            for (String str2 : bq) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:0 OR progid:3) AND catid:[* TO 100] AND title:\"" + str + "\"  AND zhaoSecondIndustry:\"" + str2 + "\"", str+"&"+str2, 2);
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

        //关键词a 和 定位词
        for (String key : a) {
            for (String str2 : bq) {
                arrayList.add(key+"&"+str2);
            }
            //arrayList.add(key);
        }
        //关键词a 和 定位词
        for (String key : b) {
            for (String str2 : bq) {
                arrayList.add(key+"&"+str2);
            }
            //arrayList.add(key);
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

        if (type.intValue() ==1){
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
    }

    @Override
    public void getAolinbasi2(Integer type, String date) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> listAll = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] a = {"内窥镜","胃镜","肠镜","腹腔镜","胃肠镜","宫腔镜","支气管镜","鼻咽喉镜","胆道镜","耳鼻喉镜","宫腔镜","电切镜","耳鼻喉镜","腹腔镜","腔镜","十二指肠镜","超声镜","小肠镜","胸腔镜","宫腔电切","宫腔电切镜","内镜","电子镜","气管镜","输尿管镜","电子胃镜","电子腹腔镜","电子结肠镜","电切镜","纤维支气管镜","膀胱镜","呼吸镜","窥镜","电子膀胱镜","输尿管软镜","电子内窥镜","电子支气管镜","腔镜","电子肠镜","电子胃肠镜","超声刀","能量平台","小探头","测漏器","电刀","光学视管","气腹机","肾盂镜","探头驱动器","纤维镜","胸腔镜","硬性镜","维护保养装置"};
        String[] b = {"鼻咽喉","摄像系统","超声","摄像平台","支气管","输尿管","胃肠","宫腔","腹腔","呼吸","膀胱","消化","胆道","清洗消毒","整体手术室","影像装置","图像处理","摄像头","监视器","保养装置","光源","台车","主机","显示器","适配器"};


        for (String str : a) {
            futureList1.add(executorService1.submit(() -> {
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + str + "\"", str, 2);
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
        for (String str : b) {
            futureList1.add(executorService1.submit(() -> {
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND title:\"" + str + "\"  AND zhaoFirstIndustry:\"" + "医疗单位" + "\"", str, 2);
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

        //关键词a 和 定位词
        for (String key : a) {
            arrayList.add(key);
        }
        //关键词a 和 定位词
        for (String key : b) {
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

        if (type.intValue() ==1){
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
    }

    @Override
    public void getAolinbasi2_qw(Integer type, String date) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> listAll = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        //String[] a = {"内窥镜","胃镜","肠镜","腹腔镜","胃肠镜","宫腔镜","支气管镜","鼻咽喉镜","胆道镜","耳鼻喉镜","宫腔镜","电切镜","耳鼻喉镜","腹腔镜","腔镜","十二指肠镜","超声镜","小肠镜","胸腔镜","宫腔电切","宫腔电切镜","内镜","电子镜","气管镜","输尿管镜","电子胃镜","电子腹腔镜","电子结肠镜","电切镜","纤维支气管镜","膀胱镜","呼吸镜","窥镜","电子膀胱镜","输尿管软镜","电子内窥镜","电子支气管镜","腔镜","电子肠镜","电子胃肠镜","超声刀","能量平台","小探头","测漏器","电刀","光学视管","气腹机","肾盂镜","探头驱动器","纤维镜","胸腔镜","硬性镜","维护保养装置"};
        String[] b = {"鼻咽喉","摄像系统","超声","摄像平台","支气管","输尿管","胃肠","宫腔","腹腔","呼吸","膀胱","消化","胆道","清洗消毒","整体手术室","影像装置","图像处理","摄像头","监视器","保养装置","光源","台车","主机","显示器","适配器"};

        for (String str : b) {
            futureList1.add(executorService1.submit(() -> {
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + str + "\"", str, 2);
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

        //关键词a 和 定位词
        /*for (String key : a) {
            arrayList.add(key);
        }*/
        //关键词a 和 定位词
        for (String key : b) {
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

        if (type.intValue() ==1){
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
    }

    @Override
    public void getAolinbasi2_3(Integer type, String date) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> listAll = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] a = {"内窥镜","胃镜","肠镜","腹腔镜","胃肠镜","宫腔镜","支气管镜","鼻咽喉镜","胆道镜","耳鼻喉镜","宫腔镜","电切镜","耳鼻喉镜","腹腔镜","腔镜","十二指肠镜","超声镜","小肠镜","胸腔镜","宫腔电切","宫腔电切镜","内镜","电子镜","气管镜","输尿管镜","电子胃镜","电子腹腔镜","电子结肠镜","电切镜","纤维支气管镜","膀胱镜","呼吸镜","窥镜","电子膀胱镜","输尿管软镜","电子内窥镜","电子支气管镜","腔镜","电子肠镜","电子胃肠镜","超声刀","能量平台","小探头","测漏器","电刀","光学视管","气腹机","肾盂镜","探头驱动器","纤维镜","胸腔镜","硬性镜","维护保养装置"};
        String[] b = {"鼻咽喉","摄像系统","超声","摄像平台","支气管","输尿管","胃肠","宫腔","腹腔","呼吸","膀胱","消化","胆道","清洗消毒","整体手术室","影像装置","图像处理","摄像头","监视器","保养装置","光源","台车","主机","显示器","适配器"};
        String[] bq = {"医疗单位"};

        /*for (String str : a) {
            futureList1.add(executorService1.submit(() -> {
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\"" + str + "\"", str, 2);
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
        }*/


        for (String str : b) {
            futureList1.add(executorService1.submit(() -> {
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND title:\"" + str + "\"  AND zhaoFirstIndustry:\"" + "医疗单位" + "\"", str, 2);
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
        /*for (String key : a) {
            arrayList.add(key);
        }*/
        //关键词b
        for (String key : b) {
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

        if (type.intValue() ==1){
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
    }

    /**
     * 贝登
     * @param type
     * @param date
     */
    @Override
    public void getBeiDeng(Integer type, String date) throws Exception{
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> listAll = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] b = {"医院","诊所","门诊","保健院","健康委员会","医学院","体检中心","健康局","医院部","药房","卫生院","医疗保障局","合作医疗","医药服务管理司","兽医实验室","医药","精神病院","防治院","血液中心","眼科中心","治疗中心","保健中心","保健所","血管病研究所","防治所","外科中心","康复中心","透析中心","正畸中心","荣军院","防治中心","保健站","列腺病研究所","职业病院","防治站","产院","急救中心","卫生局","卫生厅","防治办公室","卫生保健中心","医疗中心","卫生中心","门诊部","卫生服务站","医检所","制剂室","药交所","眼科","医保","医疗保障","卫健委","戒毒所","敬老院","疗养院","眼病防治所","矫治所","结核病防治所","休养所","血站","福利院","医疗机构","病防治办公室","计划生育","生育委员","计生委","大健康","同仁堂","江中集团","医学","健康科技","养生堂","保健品","诊断","康宁","制药","药业","药集团","医疗集团","精神卫生","药店","军医","医用","医疗","诊疗","残联","医护","卫生所","卫生院 ","卫生院校","医科大学","妇幼","健康中心","运动康复","中医馆","预防控制","医务室"};
        //全文+辅助
        String[] qwAndFz = {"pcr","功能分析仪","氧含量测定仪","钾分析仪","二氧化碳电极","钙电极","钾电极","选择性电极","锂电极","参比电极","氯电极","钠电极","葡萄糖电极盒","葡萄糖电极","乳酸电极","氧电极","电极膜","钙分析仪","氯分析仪","钠分析仪","电解质生化分析仪","金标测试仪","浊度分析仪","光法分析仪","恒温杂交仪","扫描图像分析系统","标本测定装置","样本分析设备","成分分析仪器","电泳仪","流式点阵仪器","色谱柱","质谱系统","层析柱","检测阅读系统","信号扩大仪","电泳装置","电泳槽","缓冲液槽","采样设备","采样器具","采样储藏管","标本采集保存管","采样器","取样器","样本采集器具","切片机","整体切片机","组织脱水机","染色机","包埋机","制片机","涂片机","组织处理机","轮转式切片机","平推式切片机","振动式切片机","冷冻切片机","包埋机热台","包埋机冷台","自动涂片机","滴染染色机","裂解仪","样本处理仪器","样本裂解仪","离心机","培养设备","孵育设备","恒温箱","孵育器","恒温培养箱","培养箱","振荡孵育器","检验辅助设备","洗板机","计数板","自动加样系统","低温储存设备","样本处理系统","样品前处理系统","样品检查自动化系统","样品处理系统","样品后处理系统","分杯处理系统","样本孵育系统","超净装置","自动进样系统","真空冷冻干燥箱","纯水机","电刀笔","电极","电极板","电缆线","低温冰箱","超低温冰箱","电化学仪器","电导率仪","实验天平","分光光度计","采样系统","测氧仪","蛋白电泳","电镜","电泳涂装设备","干式恒温器","观察扫描仪","观片灯","光度计","恒温水槽","恒温水浴箱","冷冻箱","生物容器","水机"};
        //全文
        List<String> qw = LogUtils.readRule("keyWords");

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

        if (type.intValue() ==1){
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
    }

    @Override
    public void getWensihaihui_Jiaofu(Integer type, String date) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> listAll = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] a = {"RAP","信创","SCM","EAM","安全监控","供应链运营管理系统","供应链信息系统","供应链金融平台","供应链金融服务平台","供应链系统解决方案","供应链信息平台","供应链大数据应用项目","供应链数字金融项目","供应链协同服务平台","供应链综合服务平台","供应链业务系统","供应链综合管理平台","供应链综合管理系统","供应链协同服务系统","供应链综合服务系统","供应链金融系统","供应链服务平台","供应链服务系统","供应链业务平台","固定资产管理信息系统","固定资产管理系统","固定资产管理信息平台","固定资产管理平台","资产经营管理系统","实物资产系统","资产管理系统","资产管理云平台","资产管理平台","资产托管系统","资产管理软件","资产交易管理信息平台","资产交易平台","资产管理信息化系统","资产数字管理平台","资产精细化管理综合平台","资产监督管理平台","资产监督平台","资产监督管理信息化系统","资产一体化管理平台","资产综合信息管理平台","资产管理信息系统","资产数字化运营管理平台","资产数字化管理平台","资产数字化管理系统","资产数字化运营管理系统","资产一体化管理系统","资产数字管理系统","资产动态管理云平台","资产动态管理系统","资产动态管理平台","资产管理信息化平台","资产盘点系统","资产盘点平台","资产盘点软件","资产盘点管理系统","资产盘点管理平台","资产盘点管理软件","资产管理工具软件","接口管理平台","接口文档管理平台","接口文档管理工具","智慧园区","智慧城市","安保监控","安防监控"};


        for (String str : a) {
            futureList1.add(executorService1.submit(() -> {
                List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND (progid:0 OR progid:3) AND catid:[* TO 100] AND title:\"" + str + "\"", str, 2);
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

        for (String str : a) {
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
    }

    @Override
    public void getBeiDeng2(Integer type, String date) throws Exception{
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> listAll = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        String[] b = {"医院","诊所","门诊","保健院","健康委员会","医学院","体检中心","健康局","医院部","药房","卫生院","医疗保障局","合作医疗","医药服务管理司","兽医实验室","医药","精神病院","防治院","血液中心","眼科中心","治疗中心","保健中心","保健所","血管病研究所","防治所","外科中心","康复中心","透析中心","正畸中心","荣军院","防治中心","保健站","列腺病研究所","职业病院","防治站","产院","急救中心","卫生局","卫生厅","防治办公室","卫生保健中心","医疗中心","卫生中心","门诊部","卫生服务站","医检所","制剂室","药交所","眼科","医保","医疗保障","卫健委","戒毒所","敬老院","疗养院","眼病防治所","矫治所","结核病防治所","休养所","血站","福利院","医疗机构","病防治办公室","计划生育","生育委员","计生委","大健康","同仁堂","江中集团","医学","健康科技","养生堂","保健品","诊断","康宁","制药","药业","药集团","医疗集团","精神卫生","药店","军医","医用","医疗","诊疗","残联","医护","卫生所","卫生院 ","卫生院校","医科大学","妇幼","健康中心","运动康复","中医馆","预防控制","医务室"};
        //全文+辅助
        String[] qwAndFz = {"功能分析仪","氧含量测定仪","钾分析仪","二氧化碳电极","钙电极","钾电极","选择性电极","锂电极","参比电极","氯电极","钠电极","葡萄糖电极盒","葡萄糖电极","乳酸电极","氧电极","电极膜","钙分析仪","氯分析仪","钠分析仪","电解质生化分析仪","金标测试仪","浊度分析仪","光法分析仪","恒温杂交仪","扫描图像分析系统","标本测定装置","样本分析设备","成分分析仪器","电泳仪","流式点阵仪器","色谱柱","质谱系统","层析柱","检测阅读系统","信号扩大仪","电泳装置","电泳槽","缓冲液槽","采样储藏管","标本采集保存管","采样器","取样器","样本采集器具","切片机","整体切片机","组织脱水机","染色机","包埋机","制片机","涂片机","组织处理机","轮转式切片机","平推式切片机","振动式切片机","冷冻切片机","包埋机热台","包埋机冷台","自动涂片机","滴染染色机","裂解仪","样本处理仪器","样本裂解仪","离心机","培养设备","孵育设备","恒温箱","孵育器","恒温培养箱","培养箱","振荡孵育器","检验辅助设备","洗板机","计数板","自动加样系统","低温储存设备","样本处理系统","样品前处理系统","样品检查自动化系统","样品处理系统","样品后处理系统","分杯处理系统","样本孵育系统","超净装置","自动进样系统","真空冷冻干燥箱","电刀笔","电极","低温冰箱","超低温冰箱","电化学仪器","电导率仪","实验天平","分光光度计","采样系统","测氧仪","蛋白电泳","电镜","电泳涂装设备","干式恒温器","观察扫描仪","光度计","光化学反应仪","恒温水槽","恒温水浴箱","冷冻箱","色谱仪","脱水机","冷水机","自动洗涤脱水机"};
        //全文
        List<String> qw = LogUtils.readRule("keyWords");

        //黑词
       String [] blacks={"废标","流标","终止","违规","招标异常","无效公告","暂停公告","失败公告","终止公告","路灯采购","奶粉采购","采购家具","空调采购","多联机空调","锅炉房设备采购","电视机采购","采购电视机","环卫工具采购","印刷采购","加装电梯","被服采购","家具采购","石材采购","停车设备采购","电梯采购","垃圾压缩成套设备","窗帘采购","混凝土招标","数控机床附件","监理","工程监理","施工监理","广告宣传","临建食堂购餐桌椅","食堂食材采购","食堂食品","员工工装","热机组采购","竹地板材料","有限公司轮胎","保险采购","苗木采购","鱼苗采购","多联机配件","污水处理设备","白色OPPOR9手机","货物类采购","水分配系统","采购日常百货","石材招标","玻璃隔断","玻璃栏杆","医院勘察采购","防褥疮床垫","清洁能源示范","铅桶采购","笔记本电脑采购","车辆维修","人才招聘","保温材料更换","物业管理服务","空调管路系统","安保服务","空调维保","污水处理系统","改造安装防护门",
                "热水系统改造","排风系统升级改造","空调系统改造","采购安装风管机空调","消防维保","绿化带拆除","电梯维保服务","中央空调清洗","消防维修","地下车库加建","食堂对外承包","保护测评","保洁服务","监理单位","监理企业","招租","设施改造","房租","出租","选择招标代理机构","水杯维修","食堂外包服务","后厨管理承包","食堂承包经营","食堂等物业服务","后勤保洁服务","食堂项目承包","肉类配送","保安服务","车转让","变压器扩容","网络招聘服务","保险联网结算系统","宣传片投放","广告服务","工程垃圾设备","景观节点整治","塌方除理","房屋拍卖","汽车采购","工程造价咨询","整体板房询价","租赁服务","垃圾清运","外墙保温","康复大楼工程监理","宣传策划","车辆租赁","办公系统开发","水体治理","审计业务","养老购买","坑塘整治","后勤保洁管理服务","设备维修维护保养",
                "子女保险","保险辅助","保险服务","医疗责任保险","意外保险","伤害保险","运输保险","大病医疗保险","中邮保险","保险统保","保险运营"};
        //List<String> blacks = LogUtils.readRule("blockKeys");

        for (String str : qw) {
            futureList1.add(executorService1.submit(() -> {
                List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND progid:3 AND allcontent:\"" + str + "\"", str, 2);
                log.info(str.trim() + "————" + mqEntities.size());
                if (!mqEntities.isEmpty()) {
                    for (NoticeMQ data : mqEntities) {
                        if (data.getTitle() != null) {
                            boolean flag = true;
                            if (flag){
                                String heici ="";
                                for (String black : blacks) {
                                    if (data.getTitle().contains(black)){
                                        heici +=black+"、";
                                    }
                                }
                                if (StrUtil.isNotEmpty(heici)){
                                    data.setHeici(heici.substring(0, heici.length() - 1));
                                }
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
                                    String heici ="";
                                    for (String black : blacks) {
                                        if (data.getTitle().contains(black)){
                                            heici +=black+"、";
                                        }
                                    }
                                    if (StrUtil.isNotEmpty(heici)){
                                        data.setHeici(heici.substring(0, heici.length() - 1));
                                    }
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
                                    String heici ="";
                                    for (String black : blacks) {
                                        if (data.getTitle().contains(black)){
                                            heici +=black+"、";
                                        }
                                    }
                                    if (StrUtil.isNotEmpty(heici)){
                                        data.setHeici(heici.substring(0, heici.length() - 1));
                                    }
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
            /*for (String k : b){
                arrayList.add(key+"&"+k);
            }*/
            arrayList.add(key);
        }

        //去重前的统计量
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
                    futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSaveBd(content)));
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
    public void getYuNanMaoShao(Integer type, String date) throws Exception{
        ExecutorService executorService1 = Executors.newFixedThreadPool(80);
        List<NoticeMQ> list = new ArrayList<>();
        List<NoticeMQ> listAll = new ArrayList<>();
        HashMap<String, String> dataMap = new HashMap<>();
        List<Future> futureList1 = new ArrayList<>();

        List<String> keyWords = LogUtils.readRule("keyWordsA");

        for (String str : keyWords) {
            futureList1.add(executorService1.submit(() -> {
                List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+date+"] AND progid:3 AND newZhongBiaoUnit:\"" + str + "\"", str, 2);
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
        for (String str :keyWords){
            arrayList.add(str);
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
                    futureList.add(executorService.submit(() -> getDataFromZhongTaiAndSaveZhongBiaoJinE(content)));
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


    private void searchingHyAllData(Map<String, Object> map) throws IOException {

        String id = map.get("id") != null ? map.get("id").toString() : "";
        String zhaobiaounit = map.get("zhao_biao_unit") != null ? map.get("zhao_biao_unit").toString() : "";

        HttpClient client = new DefaultHttpClient();
        HttpResponse response = null;
        String url = "http://cusdata.qianlima.com/api/ka/industry?unit="+zhaobiaounit+"";
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type", "application/json");

        response = client.execute(post);
        String ret = null;
        ret = EntityUtils.toString(response.getEntity(), "UTF-8");

        System.out.println(ret);
        JSONObject parseObject= JSON.parseObject(ret);
        JSONObject data = parseObject.getJSONObject("data");
        String firstLevel = data.getString("firstLevel");
        String secondLevel = data.getString("secondLevel");
        bdJdbcTemplate.update(UPDATE_SQL_01,firstLevel,secondLevel, id);
        log.info("contentId:{} =========== 数据处理成功！！！ ",id);

    }



    private boolean checkAmount(String amount) {
        //金额为空 或（金额纯数字的& 金额大于等于10万）
        if (StringUtils.isBlank(amount) ||
                (MathUtil.match(amount) && new BigDecimal(amount).compareTo(new BigDecimal("100000")) >= 0)) {
            return true;
        }
        return false;
    }
}
