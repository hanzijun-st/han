package com.qianlima.offline.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.extract.TargetService;
import com.qianlima.offline.rule02.BiaoDiWuRule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class YiLiaoBiaoDiWuService {

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;


    // 标的物匹配到的关键词
    //private String[] keywords = {};
    private String[] keywords = {"钙电极","钾电极","锂电极","氯电极","钠电极","氧电极","电极膜","酶免仪","酶标仪","比浊仪","浊度计","尿糖计","电泳仪","色谱柱","质谱仪","层析柱","电泳槽","采血笔","采血管","隐血珠","采血针","切片机","染色机","包埋机","制片机","涂片机","裂解仪","离心机","恒温箱","孵育器","洗板机","计数板","血沉管","血流变仪","血库系统","钾分析仪","参比电极","乳酸电极","钙分析仪","氯分析仪","钠分析仪","电泳仪器","质谱系统","电泳装置","缓冲液槽","采样设备","采样器具","采样拭子","培养设备","孵育设备","超净装置","血型分析仪","半自动血栓","血凝分析仪","血糖分析仪","血液粘度计","血栓弹力仪","凝血分析仪","功能分析仪","血沉分析仪","流式细胞仪","生化分析仪","肌酐分析仪","血糖血酮仪","血脂分析仪","乳酸分析仪","尿酸分析仪","血气分析仪","血气采血器","血氧分析仪","选择性电极","葡萄糖电极","pH分析仪","尿素电极盒","乳酸电极盒","免疫印迹仪","金标测试仪","蛋白印迹仪","蛋白分析仪","抗体检测仪","浊度分析仪","蛋白检测仪","酶免分析仪","酶标分析仪","荧光阅读仪","光法分析仪","免疫分析仪","精子分析仪","基因测序仪","检测分析仪","基因扩增仪","恒温杂交仪","电子比浊仪","药敏分析仪","细菌培养仪","医用显微镜","尿液分析仪","成分分析仪","粪便分析仪","体液分析仪","白带分析仪","流式点阵仪","色谱分析仪","蛋白层析柱","信号扩大仪","电泳凝胶板","动脉血气针","末梢采血针","末梢采血器","真空采血管","血液采集卡","微生物拭子","病毒采样盒","采样储藏管","病毒采样管",
            "激光采血仪","足跟采血器","激光采血机","整体切片机","组织脱水机","组织处理机","推片染色机","冷冻切片机","细胞制片机","包埋机热台","包埋机冷台","自动涂片机","特殊染色机","滴染染色机","抗原修复仪","细胞过滤器","裂解洗脱仪","样本裂解仪","医用离心机","超速离心机","核酸提取仪","核酸纯化仪","恒温培养箱","生化培养箱","医用培养箱","恒温保存箱","厌氧培养箱","振荡孵育器","恒温箱系统","酶标洗板机","微孔洗板机","细胞计数板","医用冷藏箱","医用冷冻箱","细胞分选仪","血球记数板","医用低温箱","生物安全柜","洁净工作台","血型分析仪器","凝血分析仪器","血型卡离心机","全自动涂片机","自动血库系统","血小板聚集仪","红细胞变形仪","血液分析系统","血细胞分析仪","血细胞计数器","白细胞计数仪","二聚体分析仪","ACT监测仪","血栓弹力图仪","血小板分析仪","血小板凝集仪","血流变分析仪","生化分析设备","生化分析仪器","胆红素分析仪","血酮体测试仪","葡萄糖分析仪","电解质分析仪","胆固醇分析仪","氧含量测定仪","电化学测氧仪","血气分析系统","血气分析仪器","血气检测电极","血气分析设备","二氧化碳电极","葡萄糖电极盒","免疫分析设备","荧光判读系统","免疫分析系统","临床检验系统","PCR扩增仪","基因测序系统","PCR分析仪","基因测序仪器","核酸扩增仪器","微生物比浊仪","菌培养监测仪","微生物鉴定仪","细菌测定系统","细菌分析系统","图像扫描仪器","图像分析仪器","尿液分析设备","样本分析设备","尿液分析仪器","成分分析仪器","尿液分析系统","粪便分析仪器","精子分析仪器","体液分析仪器","便潜血分析仪","尿液分析试纸","流式点阵仪器","质谱检测系统","冰点渗透压计","检测阅读系统","毛细管电泳仪","琼脂糖电泳仪","非真空采血管","血样采集容器","隐血采样胶囊","样本采样拭子","集菌培养容器","血液化验设备","血液化验器具","红白血球吸管","动静脉采血针","轮转式切片机","平推式切片机","振动式切片机","抗原热修复仪","玻片处理系统","样本处理仪器","样本分离设备","微孔板离心机","CO2培养箱","厌氧培养装置","厌氧培养系统","血小板振荡器","检验辅助设备","自动加样系统","低温储存设备","样本处理系统","去血片洗板机","血细胞计数板","尿沉渣计数板","样品处理系统","分杯处理系统","自动进样系统","脏器冷藏装置","医用低温设备","医用冷藏设备","医用冷冻设备","血液学分析设备","血细胞分析仪器","血小板分析仪器","血流变分析仪器","红细胞沉降仪器","血红蛋白测定仪","流式细胞分析仪","干式血球计数仪","即时凝血分析仪","凝血速率监测仪","自动凝血计时器","凝血功能分析仪","血液流变分析仪","动态血沉分析仪","淋巴细胞计数仪","血红蛋白分析仪","血糖血压测试仪","血糖两用检测仪","血糖乳酸分析仪","血气酸碱分析仪","电解质分析仪器","电解质分析设备","血液血气分析仪","血气生化分析仪","红细胞压积电极","代谢物测量系统","酶联免疫分析仪","荧光免疫分析仪","免疫层析分析仪",
            "免疫分析一体机","化学发光测定仪","早孕试纸阅读仪","散射比浊分析仪","化学比浊测定仪","生化免疫分析仪","排卵试纸阅读仪","生物学分析设备","生物芯片阅读仪","PCR分析系统","恒温核酸扩增仪","核酸分子杂交仪","微生物分析设备","呼气试验测试仪","结核杆菌分析仪","微生物比浊仪器","微生物鉴定仪器","倒置生物显微镜","正置生物显微镜","数码生物显微镜","光学生物显微镜","荧光生物显微镜","病理切片扫描仪","显微镜扫描系统","显微影像分析仪","放射免疫测定仪","放射免疫分析仪","放射免疫计数器","液体闪烁计数器","分泌物分析仪器","粪便常规分析仪","精子质量分析仪","精子采集分析仪","自动尿液分析仪","生殖道分析仪器","渗透压测定仪器","微量元素分析仪","血液铅镉分析仪","生物芯片反应仪","基因芯片阅读仪","血样采集连接头","真空静脉采血管","微量无菌采血管","标本采集保存管","病变细胞采集器","静脉血样采血管","末梢血采集容器","胃隐血采集器具","微量血液搅拌器","微量血液振荡器","细胞离心涂片机","免疫组化染色机","核酸提取纯化仪","细胞洗涤离心机","血型专用离心机","超净恒温培养箱","二氧化碳培养箱","医用血液冷藏箱","医用血浆速冻机","医用冷藏冷冻箱","样品前处理系统","样品后处理系统",
            "低温生物降温仪","血液制品冷藏箱","冷冻干燥血浆机","真空冷冻干燥箱","流式细胞分析仪器","全自动血型分析仪","血液体液分析系统","T淋巴细胞计数仪","血细胞形态分析仪","细胞形态学分析仪","纤溶多功能分析仪","半自动凝血分析仪","全自动凝血分析仪","血小板功能分析仪","红细胞沉降压积仪","血糖参数分析仪器","血糖与血脂监测仪","多项电解质分析仪","胆固醇两用检测仪","胆固醇乳酸分析仪","血氧饱和度测试仪","CO2红外分析仪","电解质血气分析仪","电解质检测电极块","电解质生化分析仪","生化分析仪用电极","生化免疫分析仪器","心脏标志物检测仪","荧光显微检测系统","核酸检测分析系统","实时定量PCR仪","基因扩增热循环仪","核酸扩增分析仪器","核酸分子杂交仪器","微生物培养监测仪","微生物质谱鉴定仪","微生物药敏分析仪","细菌内毒素检测仪","碳13呼气质谱仪","碳13红外光谱仪","碳13呼气分析仪","真菌葡聚糖检测仪","扫描图像分析系统","超倍生物显微系统","LED生物显微镜","核素标本测定装置","放射免疫γ计数器","放射性层析扫描仪","阴道分泌物检测仪","体液形态学分析仪","微量元素分析仪器","液相色谱分析仪器","生物芯片分析仪器","血液五元素分析仪","三重四极杆质谱仪","冰点渗透压测定仪","胶体渗透压测定仪","微阵列芯片检测仪","微阵列芯片扫描仪","动静脉血样采集针","真空动静脉采血针","真空动静脉采血器","静脉血样采集容器","医用超低温冷冻箱","医用液氮储存系统","粪便分析前处理仪","医用生物防护设备","一次性使用采样器","一次性使用取样器","血细胞形态分析仪器","全自动血细胞分析仪","半自动血细胞分析仪","血液流变参数测试仪","血细胞形态学分析仪","凝血酶原时间检测仪","活化凝血时间分析仪","活化凝血时间监测仪","血液流变动态分析仪","动态血沉压积测试仪","红细胞沉降率测定仪","氧自由基生化分析仪","尿微量白蛋白分析仪","甘油三酯乳酸分析仪","经皮血氧分压监测仪","半自动电解质分析仪","电解质分析仪用电极","化学发光免疫分析仪","免疫散射浊度分析仪","金标免疫层析分析仪","免疫层析试条检测仪","医用PCR分析系统","sanger测序仪","恒温核酸扩增分析仪","基因测序文库制备仪","幽门螺旋杆菌检测仪","幽门螺旋杆菌测定仪","微生物培养监测仪器","微生物质谱鉴定仪器","真菌葡聚糖检测仪器","放射性层析扫描装置","体液形态学分析仪器","精子自动检测分析仪","生殖道分泌物分析仪","医用原子吸收光谱仪","循环肿瘤细胞分析仪","压电蛋白芯片分析仪","琼脂糖凝胶电泳装置","样品检查自动化系统","样本处理及孵育系统","粪便标本采集保存管","全自动凝血纤溶分析仪","全自动凝血因子分析仪","新生儿总胆红素测定仪","血红蛋白干化学分析仪","间接免疫荧光分析仪器","电化学发光免疫分析仪","金标斑点法定量读数仪","血液微生物培养监测仪","微生物药敏培养监测仪","微生物鉴定药敏分析仪","幽门螺旋杆菌分析仪器","玻片扫描分析影像系统","细胞医学图像分析系统","医学显微图像分析系统","循环肿瘤细胞分析仪器","真空静脉血样采集容器","动静脉采血针及连接件","微生物样本前处理系统","全自动血栓止血分析系统","全自动配血及血型分析仪","血型分析用凝胶卡判读仪","时间分辨免疫荧光分析仪","微生物药敏培养监测仪器","微生物鉴定药敏分析仪器","染色体显微图像扫描系统","放射性核素标本测定装置","流式点阵发光免疫分析仪","生物免疫层析芯片检测仪","医用开放式血液冷藏周转箱","自动扫描显微镜和图像分析系统","低温冰箱","超低温冰箱","免疫荧光分析仪","氦质谱检漏仪","冷冻箱","医用冰箱","脱水机","酶联免疫检测仪"};


    private String SQL = "insert into han_biaodiwu_new(infoId, sum, sum_unit, keyword, serial_number, name, brand, model, " +
            "number, number_unit, price, price_unit, total_price, total_price_unit, configuration, type) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


    public void getSolrAllField() throws IOException {

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        List<Future> futureList = new ArrayList<>();

//        List<String> ids = LogUtils.readRule("smf");

        List<Map<String, Object>> maps = bdJdbcTemplate.queryForList("SELECT content_id FROM loiloi_data where id <= 10000");
        for (Map<String, Object> resultMap : maps) {

            String contentId = resultMap.get("content_id") != null ? resultMap.get("content_id").toString() : "";

            futureList.add(executorService.submit(() -> {
                try {
                   // handleForYiLiao(Long.valueOf(contentId),type);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));

        }

//        for (String id : ids) {
//            futureList.add(executorService.submit(() -> {
//                try {
//                    handleForYiLiao(Long.valueOf(id));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }));
//        }

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

//            1、迈瑞接口地址：http://47.104.4.12:5001/to_json_v3/﻿
//            2、[模型识别侧重“ICT行业”]：http://47.104.4.12:2022/inspect﻿
//            3、[模型识别侧重“医疗行业”]：http://47.104.4.12:2023/inspect﻿
//            4、[模型识别没有侧重点]：http://47.104.4.12:2024/inspect

    AtomicInteger atomicInteger = new AtomicInteger(0);

//    public static void main(String[] args) {
//        String result = TargetService.extract(Long.valueOf(211666351),"http://47.104.4.12:2022/inspect");
//        System.out.println(result);
//
//    }

    public void handleForYiLiao(Long contentId,Integer type){
        JSONObject jsonObject = null;
        try{
            String url = "";
            for (BiaoDiWuRule value : BiaoDiWuRule.values()) {
                if (value.getValue().intValue() == type){
                    url = value.getName();
                }
            }
            String result = TargetService.extract(contentId,url);
            int total = atomicInteger.addAndGet(1);
            log.info("游标获取用户数据，本次获取了size：{} 条", total);
            if (StringUtils.isNotBlank(result)){
                jsonObject = JSONObject.parseObject(result);
                if (jsonObject != null && jsonObject.get("data") != null){
                    JSONObject dataObject = (JSONObject) jsonObject.get("data");
                    if (dataObject.containsKey("content_target")){
                        JSONObject resultObject = dataObject.getJSONObject("content_target");
                        saveBiaoDiWuToMysql(resultObject, contentId, "正文");
                    }
                    if (dataObject.get("attachment_targets") != null){
                        JSONArray jsonArray = dataObject.getJSONArray("attachment_targets");
                        if (jsonArray != null ){
                            for (int i = 0; i < jsonArray.size(); i++) {
                                JSONObject resultObject = jsonArray.getJSONObject(i);
                                Set<String> docNames = resultObject.keySet();
                                if (docNames != null && docNames.size() > 0){
                                    for (String docName : docNames) {
                                        JSONObject object = resultObject.getJSONObject(docName);
                                        saveBiaoDiWuToMysql(object, contentId, "附件");
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            log.error("调用标的物接口异常:{}", e);
        }
//        String code = jsonObject.get("code") != null ? jsonObject.get("code").toString() : "";
//        String message = jsonObject.get("message") != null ? jsonObject.get("message").toString() : "";
//
//        if ("400".equals(code) || "500".equals(code) || "501".equals(code) || "502".equals(code)) {
//            bdJdbcTemplate.update("update loiloi_data set code = ?,keyword = ? where content_id = ?",code,message,contentId.toString());
//            log.error("contentId:{} 调用标的物解析接口异常, 对应的状态码 code ：{} ", contentId, code);
//        }
    }

    private void saveBiaoDiWuToMysql(JSONObject resultObject, Long contentId, String type) {
        if (resultObject != null && resultObject.containsKey("target_details")){
            String sum = resultObject.getString("sum");
            String sum_unit = resultObject.getString("sum_unit");
            JSONArray targetDetails = resultObject.getJSONArray("target_details");
            if (targetDetails != null && targetDetails.size() > 0){
                for (int i = 0; i < targetDetails.size(); i++) {
                    String serial_number = "";
                    String name = "";
                    String brand = "";
                    String model = "";
                    String number = "";
                    String number_unit = "";
                    String price = "";
                    String price_unit = "";
                    String total_price = "";
                    String total_price_unit = "";
                    String configuration = "";
                    String keyword = "";
                    JSONObject finalObject = targetDetails.getJSONObject(i);
                    if (finalObject != null){
                        serial_number = finalObject.getString("serial_number");
                        name = finalObject.getString("name");
                        brand = finalObject.getString("brand");
                        model = finalObject.getString("model");
                        number = finalObject.getString("number");
                        number_unit = finalObject.getString("number_unit");
                        price = finalObject.getString("price");
                        price_unit = finalObject.getString("price_unit");
                        total_price = finalObject.getString("total_price");
                        total_price_unit = finalObject.getString("total_price_unit");
                        JSONArray configurations = finalObject.getJSONArray("configurations");
                        if (configurations != null && configurations.size() > 0){
                            for (int j = 0; j < configurations.size(); j++) {
                                JSONObject jsonObject1 = configurations.getJSONObject(j);
                                String key = jsonObject1.getString("key");
                                String value = jsonObject1.getString("value");
                                configuration += key + "：" + value + "：";
                            }
                        }
                        if (StringUtils.isNotBlank(configuration)){
                            configuration = configuration.substring(0, configuration.length() - 1);
                        }
                        // 进行匹配关键词操作
                        if (keywords != null && keywords.length > 0){
                            String allField = name + "&" + brand + "&" + model + "&" + configuration;
                            for (String key : keywords) {
                                if (allField.toUpperCase().contains(key.toUpperCase())){
                                    keyword += key + "，";
                                }
                            }
                            if (StringUtils.isNotBlank(keyword)){
                                keyword = keyword.substring(0, keyword.length() - 1);
                            }
                        }
                    }
                    // 进行数据库保存操作
                    bdJdbcTemplate.update(SQL, contentId, sum, sum_unit, keyword, serial_number, name, brand, model, number, number_unit, price, price_unit, total_price, total_price_unit, configuration, type);
                    log.info("contentId:{} ==== 获取标的物解析表成功!!!!", contentId);
                }
            }
        }
    }



}
