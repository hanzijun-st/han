package com.qianlima.offline.service.han.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.qianlima.offline.bean.*;
import com.qianlima.offline.service.ZhongTaiBiaoDiWuService;
import com.qianlima.offline.service.han.TestService;
import com.qianlima.offline.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
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
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    private static final String INSERT_HAN_ALL = "INSERT INTO han_tab_all (json_id,contentid,content_source,sum,sumUnit,serialNumber,name," +
            "brand,model,number,numberUnit,price,priceUnit,totalPrice,totalPriceUnit,configuration_key,configuration_value,appendix_suffix) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    @Override
    public void getBdw() {
        try {
            bdwService.getSolrAllField2("hBdw");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getDatasToUpdateKeyword() {

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
