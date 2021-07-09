package com.qianlima.offline.service.han.impl;

import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.service.PocDataFieldService;
import com.qianlima.offline.service.han.CurrencyService;
import com.qianlima.offline.service.han.TestSevenService;
import com.qianlima.offline.util.CollectionUtils;
import com.qianlima.offline.util.OnlineContentSolr;
import com.qianlima.offline.util.TestContentSolr;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.util.stream.Collectors;

@Service
@Slf4j
public class TestSevenServiceImpl implements TestSevenService {

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private OnlineContentSolr onlineContentSolr;

    @Autowired
    private TestContentSolr testContentSolr;

    @Autowired
    private PocDataFieldService pocDataFieldService;

    @Autowired
    @Qualifier("djeJdbcTemplate")
    private JdbcTemplate djeJdbcTemplate;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;


    @Override
    public void getShenZhenHuaDa(Integer type, String date, String sType, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        List<NoticeMQ> listMap = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        try {
            String[] aa = {"测序仪", "基因测序仪", "细菌全基因组测序仪", "高通量基因测序系统", "高通量测序仪", "微生物宏基因二代测序仪", "分子核酸测序系统", "高通量测序平台", "二代基因测序仪", "基因分析仪", "高通量二代测序仪", "高通量基因测序仪", "二代测序仪", "新一代测序仪", "高通量测序系统", "高通量基因测序仪系统", "纳米孔测序系统", "三代测序仪", "产前诊断高通量测序仪", "二代基因测序", "DNA测序仪", "全自动基因分析仪", "第二代基因测序仪", "基因测序仪升级", "测序服务", "核酸提取", "PCR方舱", "PCR检测车", "方舱核酸", "核酸检测车", "实验室信息", "病原分析", "生物管理", "样本库", "病原体分析"};
            String[] bb = {"海关", "科学技术", "医疗", "大学", "中学", "小学", "幼儿园", "培训", "学校", "血站", "急救中心", "疾控中心", "卫生院", "疗养院", "专科医院", "中医院", "综合医院", "医疗服务"};
            //String[] blacks = {"维保", "保洁", "温度计", "系统", "车采购"};

            for (String a : aa) {
                for (String b : bb) {
                    futureList1.add(executorService1.submit(() -> {
                        List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:[0 TO 3]  AND allcontent:\"" + a + "\"   AND zhaoSecondIndustry:\"" + b + "\"", "", 1);
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


            log.info("全部数据量：" + listAll.size());
            log.info("去重数据量：" + listMap.size());

            if ("1".equals(sType) || "2".equals(sType)) {
                currencyService.soutKeywords(listAll, listMap.size(), sType, name, date);
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
    public void getSolrDatas() {
        List<NoticeMQ> list = testContentSolr.companyResultsBaoXian("userIds:\"" + "38" + "\"", "", 1);
        if (!CollectionUtils.isEmpty(list)) {
            ExecutorService executorService = Executors.newFixedThreadPool(16);
            List<Future> futureList = new ArrayList<>();
            for (NoticeMQ content : list) {
                futureList.add(executorService.submit(() -> pocDataFieldService.saveTest(content)));
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

    @Override
    public void getDaJinEDatas(Integer type, String date) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        List<NoticeMQ> listMap = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        try {
            List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND (amountNumber:[100000000 TO *] OR budgetNumber:[100000000 TO *])", "", 1);
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
            //List<Map<String, Object>> mapList = djeJdbcTemplate.queryForList("select info_id, old_winner_amount, old_budget,new_winner_amount,new_budget,user_id from amount_for_handle where  states = 1 group by info_id order by info_id asc");
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
            log.info("去重数据量：" + listMap.size());

        } catch (Exception e) {
            e.getMessage();
        }
        //如果参数为1,则进行存表
        if (type.intValue() == 1) {
            if (listMap != null && listMap.size() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(16);
                List<Future> futureList = new ArrayList<>();
                for (NoticeMQ content : listMap) {
                    futureList.add(executorService.submit(() -> pocDataFieldService.getZiDuanKu_ziTi_dje(content)));
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
    public void getHangZhouBoRi(Integer type, String date, String sType, String name) {
        ExecutorService executorService1 = Executors.newFixedThreadPool(32);//开启线程池
        List<Future> futureList1 = new ArrayList<>();
        List<NoticeMQ> list = new ArrayList<>();//去重后的数据
        List<NoticeMQ> listAll = new ArrayList<>();//得到所以数据
        List<NoticeMQ> listMap = new ArrayList<>();//得到所以数据
        HashMap<String, String> dataMap = new HashMap<>();
        try {
            String[] aa = {"核酸提取仪", "核酸测序仪", "核酸检测仪", "定量PCR仪", "PCR扩增仪", "实时PCR", "数字PCR", "荧光PCR"};
            String[] blacks = {"选址服务", "厨房设备", "办公设备", "办公家具", "办公室家具", "幼儿园家具", "桌椅", "摄像头", "读卡器", "对讲机", "课题经费", "宿舍出租", "房屋出租", "北水环保", "货物运输服务", "安置房项目", "油缸维修", "商务车", "空调", "洗涤服务", "公务车", "投影设备", "汽车", "轿车", "吉利", "复印", "福建三明金明农资", "厂房出租", "玩教具", "幼儿园", "降尘车", "洒水车", "路面养护车", "湿扫车", "租赁", "房租", "路由器", "交换设备", "单反相机", "木业原料", "印刷", "打印", "楼面刷地", "用工外包", "交换机", "公路工程", "护岸工程", "工程监理", "网络设备", "造价控制", "港桥工程", "安防监测", "弱电系统", "广播监控"};

            for (String a : aa) {
                futureList1.add(executorService1.submit(() -> {
                    List<NoticeMQ> mqEntities = onlineContentSolr.companyResultsBaoXian("yyyymmdd:[" + date + "] AND progid:3  AND allcontent:\"" + a + "\"   AND (segmentType:11 OR segmentType:12) ", "", 1);
                    log.info(a + "————" + mqEntities.size());
                    if (!mqEntities.isEmpty()) {
                        for (NoticeMQ data : mqEntities) {
                            if (data.getTitle() != null) {
                                boolean flag = true;
                                for (String black : blacks) {
                                    if (StringUtils.isNotBlank(data.getTitle()) && data.getTitle().contains(black)) {
                                        //flag = false;
                                        data.setBlackWord(black);
                                        break;
                                    }
                                }
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
            log.info("去重数据量：" + listMap.size());

            if ("1".equals(sType) || "2".equals(sType)) {
                currencyService.soutKeywords(listAll, listMap.size(), sType, name, date);
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
}