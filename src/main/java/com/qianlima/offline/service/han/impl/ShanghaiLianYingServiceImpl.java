package com.qianlima.offline.service.han.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianlima.extract.target.TargetExtractService;
import com.qianlima.offline.bean.ConstantBean;
import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.rule02.MyRuleUtils;
import com.qianlima.offline.service.CusDataFieldService;
import com.qianlima.offline.service.han.CurrencyService;
import com.qianlima.offline.service.han.ShanghaiLianYingService;
import com.qianlima.offline.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 上海联影
 */

@Service
@Slf4j
public class ShanghaiLianYingServiceImpl implements ShanghaiLianYingService{

    @Autowired
    private ContentSolr contentSolr;//solr 查询

    @Autowired
    private CusDataFieldService cusDataFieldService;//处理中台数据的方法

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private MyRuleUtils myRuleUtils;//行业标签

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;//常用

    @Autowired
    @Qualifier("gwJdbcTemplate")
    private JdbcTemplate gwJdbcTemplate;//官网

    private static final String UPDATA_BDW_SQL = "INSERT INTO han_biaodiwu (contentid, serialNumber, name, brand, model, number, numberUnit, price, priceUnit, totalPrice, totalPriceUnit) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    //mysql数据库中插入数据
    public String INSERT_ZT_RESULT_HXR = "INSERT INTO han_data (task_id,keyword,content_id,title,content, province, city, country, url, baiLian_budget, baiLian_amount_unit," +
            "xmNumber, bidding_type, progid, zhao_biao_unit, relation_name, relation_way, agent_unit, agent_relation_ame, agent_relation_way, zhong_biao_unit, link_man, link_phone," +
            " registration_begin_time, registration_end_time, biding_acquire_time, biding_end_time, tender_begin_time, tender_end_time,update_time,type,bidder,notice_types,open_biding_time,is_electronic,code,isfile,keyword_term) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    @Override
    public void getShanghaiLianYing() throws Exception{
        List<NoticeMQ> list = new ArrayList<>();//总数据
        List<NoticeMQ> qcList = new ArrayList<>();//去重后数据
        HashMap<String, String> dataMap = new HashMap<>();
        //全文关键词
        List<String> allKeyWords = LogUtils.readRule("keyWords");
        //标题关键词
        String[] titleKeyWords = { "MR ","CT ","XR ","DR" };

        //对全文关键词进行数据处理
        for (String keyWord : allKeyWords) {
            String aa = keyWord;
            //从solr中获取数据
            List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian( "yyyymmdd:[20200101 TO 20201231] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND allcontent:\""+aa+"\"", keyWord, 101);
            log.info("keyword:{}查询出了size：{}条数据", keyWord, mqEntities.size());
            if (!mqEntities.isEmpty()) {
                for (NoticeMQ data : mqEntities) {
                    list.add(data);
                    if (!dataMap.containsKey(data.getContentid().toString())) {
                        list.add(data);
                        data.setKeyword(keyWord);
                        dataMap.put(data.getContentid().toString(), "0");
                    }
                }
            }
        }
        //对标题关键词进行数据处理
        for (String titleKeyWord : titleKeyWords) {
            String aa = titleKeyWord;
            //从solr中获取数据
            List<NoticeMQ> mqEntities = contentSolr.companyResultsBaoXian( "yyyymmdd:[20200101 TO 20201231] AND (progid:[0 TO 3] OR progid:5) AND catid:[* TO 100] AND title:\""+aa+"\"", titleKeyWord, 101);
            log.info("keyword:{}查询出了size：{}条数据", titleKeyWord, mqEntities.size());
            if (!mqEntities.isEmpty()) {
                for (NoticeMQ data : mqEntities) {
                    list.add(data);
                    if (!dataMap.containsKey(data.getContentid().toString())) {
                        list.add(data);
                        data.setKeyword(titleKeyWord);
                        dataMap.put(data.getContentid().toString(), "0");
                    }
                }
            }
        }
        log.info("全部数据量：" + list.size());
        log.info("去重之后的数据量：" + qcList.size());
        log.info("=================================================================================================");

        //对去重后的数据进行操作
        if (qcList !=null && qcList.size() >0){
            ExecutorService executorService = Executors.newFixedThreadPool(32);//创建线程池
            List<Future> futureList = new ArrayList<>();//存放线程
            for (NoticeMQ noticeMQ : qcList) {
                futureList.add(executorService.submit(() ->{
                    //调用中台数据
                }));
            }
            //线程池的释放
            for (Future future : futureList) {
                try {
                    future.get();//等待计算完成，然后检索其结果
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
        }
        log.info("================================当前接口执行完毕==================================");
    }

    //调用中台数据，进行处理
    private void getZhongTaiDatasAndSave(NoticeMQ noticeMQ){
        String zhaoBiaoUnit = noticeMQ.getZhaoBiaoUnit();//招标单位
        if (StringUtils.isEmpty(zhaoBiaoUnit)){
            return;
        }
        boolean b = cusDataFieldService.checkStatus(noticeMQ.getContentid().toString());//范围 例如:全国
        if (!b){
            log.info("contentid:{} 对应的数据状态不是99, 丢弃" , noticeMQ.getContentid().toString());
            return;
        }

        //全部自提，不需要正文
        Map<String, Object> resultMap = cusDataFieldService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap !=null){
            resultMap.put("keyword","");//先将keyword 清空
            String title = resultMap.get("title") != null ? resultMap.get("title").toString() : "";//标题
            String zbUnit = resultMap.get("zhao_biao_unit") != null ? resultMap.get("zhao_biao_unit").toString() : "";//中标单位
            String industry = myRuleUtils.getIndustry(zbUnit);//得到行业标签
            String firstIndustry = industry.split("-")[0];//一级行业分类
            //校验行业信息
            if ("政府机构-医疗".equals(industry) || "医疗单位".equals(firstIndustry)|| "商业公司-医疗服务".equals(industry)) {
                resultMap.put("code",industry);//将code 设定行业分类
                resultMap.put("keyword_term",noticeMQ.getKeywordTerm());
            }
            //获取正文字段
            List<Map<String, Object>> contentList = gwJdbcTemplate.queryForList(ConstantBean.SELECT_ITEM_CONTENT_BY_CONTENTID, noticeMQ.getContentid().toString());
            if (contentList == null || contentList.size() <=0){
                return;
            }
            String content = contentList.get(0).get("content").toString();//正文字段
            String tempContent = content;//设定不变正文字段，下文使用
            content = MathUtil.delHTMLTag(content);//去除正文中的html
            content = title +"&" + content;// 标题 + 正文
            content = content.toUpperCase();//将最后正文的字符转成大写
            title = title.toUpperCase();//将标题字符转成大写

            //追加关键字
            String keyWord ="";//追加正文、标题
            String keyWords="";//存储医院等级
            
            try {
                //全文关键词
                List<String> allKeyWords = LogUtils.readRule("keyWords");
                //标题关键词
                String[] titleKeyWords = { "MR ","CT ","XR ","DR" };

                //全文检索
                for (String kw : allKeyWords) {
                    if (content.contains(kw)){
                        keyWord += kw+"、";
                    }
                }
                //标题检索
                for (String titleKeyWord : titleKeyWords) {
                    if (title.contains(titleKeyWord)){
                        keyWord += titleKeyWord+"、";
                    }
                }
                //去掉最后一个字符
                keyWord = org.apache.commons.lang3.StringUtils.isNotBlank(keyWord) ? keyWord.substring(0, keyWord.length() -1) : "";
                //如果得到的为空
                if (StringUtils.isEmpty(keyWord)){
                    keyWord = noticeMQ.getKeyword();
                }
                
                //读取医院信息
                HashMap<String, String> simpleAreaMap = KeyUtils.getSimpleAreaMap();
                Set<Map.Entry<String, String>> entries = simpleAreaMap.entrySet();//将map的key和value 进行映射成 集合
                for (Map.Entry<String, String> entry : entries) {
                    String key = entry.getKey();
                    if (zbUnit.contains(key)){
                        //如果中标单位中包含医院名称
                        keyWords = entry.getValue();
                    }
                }

                resultMap.put("keyword",keyWord);//
                resultMap.put("keywords",keyWords);//医院等级

                //标的物进行匹配
                String progid = resultMap.get("progid").toString();
                Integer taskId = 2;
                if ("0".equals(progid) || "1".equals(progid) || "2".equals(progid)){
                    taskId =1;
                }
                resultMap.put("task_id",taskId);

                //处理标的物的数据
                getBiaoDiWuForAll(tempContent, String.valueOf(noticeMQ.getContentid()), taskId);

                //对结果数据进行存储
                currencyService.saveTyInto(resultMap,INSERT_ZT_RESULT_HXR);
            } catch (IOException e) {

            }
        }
    }
    public void getBiaoDiWuForAll(String content, String contentId, Integer taskId){
        if (StringUtils.isEmpty(content) && StringUtils.isEmpty(contentId)){
            return;
        }
        try {
            //标的物解析表---对象（进行解析）
            String target = TargetExtractService.getTargetResult("http://47.104.4.12:5001/to_json_v3/", content);
            if (org.apache.commons.lang3.StringUtils.isNotBlank(target)){
                JSONObject targetObject = JSONObject.parseObject(target);
                if (targetObject.containsKey("targetDetails")){
                    JSONArray targetDetails = (JSONArray) targetObject.get("targetDetails");
                    for (Object targetDetail : targetDetails) {
                        String detail = targetDetail.toString();
                        Map detailMap = JSON.parseObject(detail, Map.class);
                        String serialNumber = ""; //标的物序号
                        String name = ""; //名称
                        String brand = ""; //品牌
                        String model = ""; //型号
                        String number = ""; //数量
                        String numberUnit = ""; //数量单位
                        String price = ""; //单价
                        String priceUnit = "";  //单价单位
                        String totalPrice = ""; //总价
                        String totalPriceUnit = ""; //总价单位
                        String keyword = "";
                        if (detailMap.containsKey("serialNumber")){
                            serialNumber = (String) detailMap.get("serialNumber");
                        }
                        if (detailMap.containsKey("name")){
                            name = (String) detailMap.get("name");
                        }
                        if (detailMap.containsKey("brand")){
                            brand = (String) detailMap.get("brand");
                        }
                        if (detailMap.containsKey("model")){
                            model = (String) detailMap.get("model");
                        }
                        if (detailMap.containsKey("number")){
                            number = (String) detailMap.get("number");
                        }
                        if (detailMap.containsKey("numberUnit")){
                            numberUnit = (String) detailMap.get("numberUnit");
                        }
                        if (detailMap.containsKey("price")){
                            price = (String) detailMap.get("price");
                        }
                        if (detailMap.containsKey("priceUnit")){
                            priceUnit = (String) detailMap.get("priceUnit");
                        }

                        if (detailMap.containsKey("totalPrice")){
                            totalPrice = (String) detailMap.get("totalPrice");
                        }
                        if (detailMap.containsKey("totalPriceUnit")){
                            totalPriceUnit = (String) detailMap.get("totalPriceUnit");
                        }

                        String result = name + "&" + brand + "&" + model;
                        result = result.toUpperCase();
                        List<String> allKeyWords = LogUtils.readRule("keyWords");
                        for (String cc : allKeyWords) {
                            if (result.contains(cc.toUpperCase())){
                                keyword += cc + ConstantBean.RULE_SEPARATOR_NAME;
                            }
                        }
                        keyword = StringUtils.isEmpty(keyword) ? keyword.substring(0, keyword.length() -1) : "";

                        //标的物操作数据库接口
                        bdJdbcTemplate.update(UPDATA_BDW_SQL, contentId, keyword, taskId, serialNumber, name, brand, model, number, numberUnit, price, priceUnit, totalPrice, totalPriceUnit);
                    }
                }
            }
        } catch (Exception e){
            log.info("infoId:{} 获取标的物信息异常", contentId);
        }
    }




}
