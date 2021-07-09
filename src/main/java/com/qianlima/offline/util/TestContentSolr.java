package com.qianlima.offline.util;

import com.qianlima.offline.bean.NoticeMQ;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CursorMarkParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 线上solr
 */
@Component
@Slf4j
public class TestContentSolr {

    @Autowired
    //@Qualifier("testsolr")
    private SolrClient solrClient;

    public List<NoticeMQ> companyResultsBaoXian(String tiaojian, String key, Integer taskId) {
        String cursormark = "";
        List<NoticeMQ> resultMap = new ArrayList<>();

        while (true) {
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setQuery(tiaojian);
            solrQuery.setRows(5000);
            //solrQuery.setFields("fl","id","zhaoBiaoUnit","title","newZhongBiaoUnit","newAmountUnit");
            if (StringUtils.isEmpty(cursormark)) {
                solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, CursorMarkParams.CURSOR_MARK_START);
            } else {
                solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, cursormark);
            }
            solrQuery.setSort("id", SolrQuery.ORDER.desc);
            try {
                QueryResponse response = solrClient.query(solrQuery, SolrRequest.METHOD.POST);
                SolrDocumentList results = response.getResults();
                if (results != null && results.size() > 0) {
                    int count = 0;
                    for (SolrDocument doc : results) {
                        if (doc.containsKey("id") && null != doc.get("id")) {
                            NoticeMQ toMQEntity = new NoticeMQ();
                            toMQEntity.setContentid(Long.valueOf(doc.get("contentid").toString()));
                            toMQEntity.setTitle(doc.get("title") != null ? doc.get("title").toString() : null);//标题
                            toMQEntity.setUpdatetime(doc.get("updatetime") != null ? doc.get("updatetime").toString() : null);
                            toMQEntity.setTags(doc.get("tagIds") != null ? doc.get("tagIds").toString() : null);
                            toMQEntity.setProgid(doc.get("progid") != null ? doc.get("progid").toString() : null);
                            toMQEntity.setNewProvince(doc.get("newProvince") != null ? doc.get("newProvince").toString() : null);
                            toMQEntity.setNewCity(doc.get("newCity") != null ? doc.get("newCity").toString() : null);
                            toMQEntity.setNewCountry(doc.get("newCountry") != null ? doc.get("newCountry").toString() : null);
                            toMQEntity.setUserIds(doc.get("userIds") != null ? doc.get("userIds").toString() : null);
                            resultMap.add(toMQEntity);
                        }
                    }
                } else {
                    break;
                }
                if (results.size() == results.getNumFound() || cursormark.equals(response.getNextCursorMark())) {
                    break;
                } else {
                    cursormark = response.getNextCursorMark();
                }
            } catch (SolrServerException | IOException e) {
                log.error("跑数据异常,{}", e);
            }
            log.info("=====关键词:" + key + " solr执行到了：" + resultMap.size());
        }
        return resultMap;
    }

    /**
     * 行业标签-专业solr
     * @param tiaojian
     * @param key
     * @param taskId
     * @return
     */
    public List<NoticeMQ> hybqSolr(String tiaojian, String key, Integer taskId) {
        String cursormark = "";
        List<NoticeMQ> resultMap = new ArrayList<>();

        while (true) {
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setQuery(tiaojian);
            solrQuery.setRows(5000);
            //solrQuery.setFields("fl","id","zhaoBiaoUnit","title","newZhongBiaoUnit","newAmountUnit");
            if (StringUtils.isEmpty(cursormark)) {
                solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, CursorMarkParams.CURSOR_MARK_START);
            } else {
                solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, cursormark);
            }
            solrQuery.setSort("id", SolrQuery.ORDER.desc);
            try {
                QueryResponse response = solrClient.query(solrQuery, SolrRequest.METHOD.POST);
                SolrDocumentList results = response.getResults();
                if (results != null && results.size() > 0) {
                    int count = 0;
                    for (SolrDocument doc : results) {
                        if (doc.containsKey("id") && null != doc.get("id")) {
                            NoticeMQ toMQEntity = new NoticeMQ();
                            toMQEntity.setContentid(Long.valueOf(doc.get("id").toString()));
                            toMQEntity.setZhaoFirstIndustry(doc.get("zhaoFirstIndustry") != null ? doc.get("zhaoFirstIndustry").toString() : null);//招标- 一级行业标签
                            toMQEntity.setZhaoSecondIndustry(doc.get("zhaoSecondIndustry") != null ? doc.get("zhaoSecondIndustry").toString() : null);//招标- 二级行业标签
                            toMQEntity.setZhongFirstIndustry(doc.get("zhongFirstIndustry") != null ? doc.get("zhongFirstIndustry").toString() : null);//中标- 一级行业标签
                            toMQEntity.setZhongSecondIndustry(doc.get("zhongSecondIndustry") != null ? doc.get("zhongSecondIndustry").toString() : null);//中标- 二级行业标签
                            resultMap.add(toMQEntity);
                        }
                    }
                } else {
                    break;
                }
                if (results.size() == results.getNumFound() || cursormark.equals(response.getNextCursorMark())) {
                    break;
                } else {
                    cursormark = response.getNextCursorMark();
                }
            } catch (SolrServerException | IOException e) {
                log.error("跑数据异常,{}", e);
            }
            log.info("=====关键词:" + key + " solr执行到了：" + resultMap.size());
        }
        return resultMap;
    }

    /**
     * 查询solr中的数据  通过输入的年份
     * @param tiaojian  查询条件
     * @param time 输入的时间
     * @return
     */
    public Map<String,Object> getSolr(String tiaojian,String time){
        Map<String,Object> map = new HashMap<>();

        tiaojian = "yyyymm:"+time+" AND "+tiaojian;
        String cursormark = "";

        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(tiaojian);
        solrQuery.setRows(5000);
        solrQuery.setFields("fl","id","zhaoBiaoUnit","title");
        if (StringUtils.isEmpty(cursormark)) {
            solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, CursorMarkParams.CURSOR_MARK_START);
        } else {
            solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, cursormark);
        }
        solrQuery.setSort("id", SolrQuery.ORDER.desc);
        try {
            QueryResponse response = solrClient.query(solrQuery, SolrRequest.METHOD.POST);
            SolrDocumentList results = response.getResults();
            if (results != null && results.size() > 0) {
                map.put(time,results.getNumFound());
            } else {
                map.put(time,0);
            }
        } catch (SolrServerException | IOException e) {
            log.error("跑数据异常,{}", e);
        }
        return map;
    }


    public Long companyResultsCount(String tiaojian) throws Exception {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(tiaojian);
        QueryResponse response = solrClient.query(solrQuery, SolrRequest.METHOD.POST);
        SolrDocumentList results = response.getResults();
        return results.getNumFound();
    }
}
