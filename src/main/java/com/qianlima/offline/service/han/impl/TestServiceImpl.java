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
import org.springframework.aop.framework.AopContext;
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

import static com.ibm.icu.impl.ValidIdentifiers.Datatype.unit;

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

    private static final String INSERT_HAN_ALL = "INSERT INTO han_tab_all (id,json_id,contentid,content_source,sum,sumUnit,serialNumber,name," +
            "brand,model,number,numberUnit,price,priceUnit,totalPrice,totalPriceUnit,configuration_key,configuration_value,appendix_suffix) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String BJG = "INSERT INTO han_beijiangong (province,city,regLocation,unit) VALUES (?,?,?,?)";

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
        Map<String, Object> resultMap = cusDataFieldService.getAllFieldsWithHunHe(noticeMQ, false);
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
        Map<String, Object> resultMap = cusDataFieldService.getAllFieldsWithHunHe(noticeMQ, false);
        if (resultMap != null) {
           try {
                saveIntoMysql(resultMap,INSERT_ZT_RESULT_HXR);
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
    private String handleInfoId(Map<String, Object> map) {
        String keyword = map.get("keyword").toString();
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
                    log.info(str.trim()+"&"+str2 + "————" + mqEntities.size());
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
                    log.info(str.trim()+"&"+str2 + "————" + mqEntities.size());
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

}
