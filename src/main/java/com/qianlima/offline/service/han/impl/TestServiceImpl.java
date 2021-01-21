package com.qianlima.offline.service.han.impl;

import com.alibaba.fastjson.JSON;
import com.qianlima.offline.bean.*;
import com.qianlima.offline.rule02.MyRuleUtils;
import com.qianlima.offline.service.CusDataFieldService;
import com.qianlima.offline.service.ZhongTaiBiaoDiWuService;
import com.qianlima.offline.service.han.CurrencyService;
import com.qianlima.offline.service.han.TestService;
import com.qianlima.offline.util.*;
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
    private ContentSolr contentSolr;
    @Autowired
    private ZhongTaiBiaoDiWuService bdwService;

    @Autowired
    private CusDataFieldService cusDataFieldService;

    @Autowired
    private CurrencyService currencyService;

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
    @Override
    public void getBdw() {
        try {
            bdwService.getSolrAllField2("hBdw");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void updateKeyword() {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);
        List<Future> futureList = new ArrayList<>();

        //String[] keywords ={"交换机","锐捷"};
        try {
            List<String> keywords = LogUtils.readRule("keyWords");
            List<Map<String, Object>> mapList = bdJdbcTemplate.queryForList("SELECT id,contentid,name,brand,model FROM h_biaodiwu");
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
                                    bdJdbcTemplate.update("UPDATE h_biaodiwu SET keyword = ? WHERE id = ?", e.getValue() , e.getKey());
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
            String province = resultMap.get("province").toString();//省

            if (province.contains("重庆市")) {
                saveIntoMysql(resultMap,INSERT_ZT_RESULT_HXR);
            }
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

}
