package com.qianlima.offline.service.han.impl;

import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.service.CusDataFieldService;
import com.qianlima.offline.service.han.CusDataNewService;
import com.qianlima.offline.service.han.HanPocService;
import com.qianlima.offline.util.LogUtils;
import com.qianlima.offline.util.OnlineContentSolr;
import com.qianlima.offline.util.StrUtil;
import com.qianlima.offline.util.UpdateContentSolr;
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

/**
 * 新接口
 */
@Service
@Slf4j
public class HanPocServiceImpl implements HanPocService {
    @Autowired
    private OnlineContentSolr onlineContentSolr;

    @Autowired
    private UpdateContentSolr updateSolr;//solr查询接口

    @Autowired
    private CusDataFieldService cusDataFieldService;

    @Autowired
    private CusDataNewService cusDataNewService;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;


    //mysql数据库中插入数据
    public static final String INSERT_ZT_RESULT_HAN = "INSERT INTO han_data (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time,is_electronic,code,isfile,keyword_term) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String UPDATA_SQL_01 = "INSERT INTO han_biaodiwu (contentid, serialNumber, name, brand, model, number, numberUnit, price, priceUnit, totalPrice, totalPriceUnit) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    @Override
    public void getNew(Integer type,String date) {
        //ExecutorService executorService1 = Executors.newFixedThreadPool(32);//开启线程池
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
       // List<Future> futureList1 = new ArrayList<>();

        try {
            //关键字
            List<String> keyWords = LogUtils.readRule("keyWords");
            for (String keyWord : keyWords) {
                List<NoticeMQ> mqEntities = updateSolr.companyResultsBaoXian("yyyymmdd:["+ date + "] AND (progid:[0 TO 3] OR progid:5) AND allcontent:\"" + keyWord + "\"",keyWord, 1);
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
            }
            log.info("全部数据量：" + listAll.size());
            log.info("去重之后的数据量：" + list.size());
            log.info("==========================");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果参数为1,则进行存表
        if (type.intValue() == 1){
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
            log.info("数据库保存成功：-->{}",StrUtil.getSaveDataStr());
        }

        log.info("当前接口执行完毕：-->{}",StrUtil.getPutStr());
    }

    @Override
    public void getZheJiangNiuRuoSi(String date,Integer type) {

        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();

        try {
            //关键字a
            List<String> keyWords = LogUtils.readRule("keyWords");
            for (String keyWord : keyWords) {
                List<NoticeMQ> mqEntities = updateSolr.companyResultsBaoXian("yyyymmdd:["+ date + "] AND (progid:3 OR progid:5) AND allcontent:\"" + keyWord + "\"",keyWord, 1);
                log.info(keyWord + "————" + mqEntities.size());
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
            }

            //关键字b
            List<String> keyWordsB = LogUtils.readRule("keyWordsB");
            for (String keyWord : keyWordsB) {
                List<NoticeMQ> mqEntities = updateSolr.companyResultsBaoXian("yyyymmdd:["+ date + "] AND (progid:3 OR progid:5) AND title:\"" + keyWord + "\"",keyWord, 1);
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
            }
            log.info("全部数据量：" + listAll.size());
            log.info("去重之后的数据量：" + list.size());
            log.info("==========================");


            ArrayList<String> arrayList = new ArrayList<>();
            for (String key : keyWords) {
                arrayList.add(key);
            }
            for (String key : keyWordsB) {
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
        /*if (list !=null && list.size() >0){
            for (NoticeMQ noticeMQ : list) {
                try {
                    getAllZhongTaiBiaoDIWu(noticeMQ.getContentid().toString());
                } catch (Exception e) {

                }
            }
        }*/
        //如果参数为1,则进行存表
        if (type.intValue() == 1){
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
            log.info("数据库保存成功：-->{}",StrUtil.getSaveDataStr());
        }

        log.info("当前接口执行完毕：-->{}",StrUtil.getPutStr());
    }

    @Override
    public Map<String, Object> getSolr(String tiaojian,String date) {
        return updateSolr.getSolr(tiaojian, date);
    }

    @Override
    public void getTongFangWeiShi(Integer type, String date) {
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();

        String[] keyWords ={"集装箱车辆检查系统","车辆检查系统","集装箱/车辆检查系统","铁路货物/车辆检查系统","集装箱车辆快速检查系统","集装箱/车辆快速检查系统","集装箱快速检查系统","铁路货物检查系统","铁路货物车辆检查系统","集装货物/车辆检查系统","集装货物检查系统","集装货物车辆检查系统","航空集装箱货物检查系统","小型车辆检查系统","背散射货物车辆检查系统","背散射货物检查系统","背散射货物/车辆检查系统","车底检查机器人","X光安检机","行李物品检查系统","行李/物品检查系统","物品检查系统","行李检查系统","X射线检查系统","灭菌通道","行李交运系统","开包检查系统","搬运机器人","安全检查系统","安全检查仪","安检成像仪","测温金属门","探测门","测温通道","赫兹成像仪","违禁品探测仪","快速探测仪","液体安全检查系统","物质识别仪","背散射检查仪","液体安检仪","液体检测仪","物质监测设备","伽马射线成像系统","物质识别系统","核素识别仪","辐射监测仪","辐射监测系统","辐射探测仪","气溶胶采样器","气碘采样器","环境自动监测系统","移动监测实验室","核素分析仪","人脸摄像头","隔离单元","智慧识别系统","食品安全检测仪","拉曼光谱仪","监测报警系统","安全防灾体系","周界安防系统","监测智能终端","海关监控指挥中心","现代化通关系统","物流监管解决方案","窗口解决方案","现代化解决方案","辐照加工系统","检疫辐照系统","辐照检疫处理系统","辐照检疫加工系统","辐照安全系统","科研应用系统"};
        try {
            //关键字a
            for (String keyWord : keyWords) {
                List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:["+ date + "] AND (progid:[0 TO 3] OR progid:5) AND allcontent:\"" + keyWord + "\"",keyWord, null);
                log.info(keyWord + "————" + mqEntities.size());
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
            }


            log.info("全部数据量：" + listAll.size());
            log.info("去重之后的数据量：" + list.size());
            log.info("==========================");


            ArrayList<String> arrayList = new ArrayList<>();
            for (String key : keyWords) {
                arrayList.add(key);
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

        } catch (Exception e) {
            e.printStackTrace();
        }
        //如果参数为1,则进行存表
        if (type.intValue() == 1){
            if (list != null && list.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(32);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : list) {
                    futureList.add(executorService.submit(() -> getZhongTaiDatasAndSaveByTf(content,keyWords)));
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
        log.info("当前接口执行完毕：-->{}",StrUtil.getPutStr());
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
            saveIntoMysql(resultMap,INSERT_ZT_RESULT_HAN);
            log.info("数据存库content_id--->{}",resultMap.get("content_id").toString());
        }
    }

    //存储数据库
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
     * 调用中台数据，进行处理-----同方
     */
    private void getZhongTaiDatasAndSaveByTf(NoticeMQ noticeMQ,String[] keyWords) {

        boolean b = cusDataNewService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b) {
            log.info("contentid:{} 对应的数据状态不是99, 丢弃", noticeMQ.getContentid().toString());
            return;
        }
        //全部自提，不需要正文
        Map<String, Object> resultMap = cusDataNewService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            String content = cusDataNewService.getContent(noticeMQ);//获取正文字段
            String title = resultMap.get("title").toString();//标题

            content = content+"&" +title;
            // 进行匹配关键词操作
            if (keyWords != null && keyWords.length > 0){
                for (String aa : keyWords) {
                    if (content.toUpperCase().contains(aa.toUpperCase())){
                        resultMap.put("keyword", aa);
                        resultMap.put("task_id",1);
                        break;
                    }

                }
               /* if (StringUtils.isNotBlank(keyword)) {
                    keyword = keyword.substring(0, keyword.length() - 1);
                    resultMap.put("keyword", keyword);
                }*/
            }
            saveIntoMysql(resultMap);
        }
    }

    // 数据入库操作-规则三
    public static final String INSERT_ZT_RESULT_TONGFANG = "INSERT INTO han_new_data_tf (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time,is_electronic,code,isfile,keyword_term,keywords, infoTypeSegment) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public void saveIntoMysql(Map<String, Object> map) {
        bdJdbcTemplate.update(INSERT_ZT_RESULT_TONGFANG, map.get("task_id"), map.get("keyword"), map.get("content_id"), map.get("title"),
                map.get("content"), map.get("province"), map.get("city"), map.get("country"), map.get("url"), map.get("baiLian_budget"),
                map.get("baiLian_amount_unit"), map.get("xmNumber"), map.get("bidding_type"), map.get("progid"), map.get("zhao_biao_unit"),
                map.get("relation_name"), map.get("relation_way"), map.get("agent_unit"), map.get("agent_relation_ame"),
                map.get("agent_relation_way"), map.get("zhong_biao_unit"), map.get("link_man"), map.get("link_phone"),
                map.get("registration_begin_time"), map.get("registration_end_time"), map.get("biding_acquire_time"),
                map.get("biding_end_time"), map.get("tender_begin_time"), map.get("tender_end_time"), map.get("update_time"),
                map.get("type"), map.get("bidder"), map.get("notice_types"), map.get("open_biding_time"), map.get("is_electronic"),
                map.get("code"), map.get("isfile"), map.get("keyword_term"), map.get("keywords"), map.get("infoTypeSegment"));
    }
}