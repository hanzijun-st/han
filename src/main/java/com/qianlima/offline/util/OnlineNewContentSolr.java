package com.qianlima.offline.util;

import com.qianlima.offline.bean.NoticeAllField;
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
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  调用solr 直接返回map结构
 */
@Component
@Slf4j
public class OnlineNewContentSolr {

    @Autowired
    //@Qualifier("onlineSolr")
    private SolrClient solrClient;

    public List<NoticeAllField> companyResultsBaoXian(String tiaojian, String key, Integer taskId) {
        String cursormark = "";
        List<NoticeAllField> resultList = new ArrayList<>();
        while (true) {
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setQuery(tiaojian);
            solrQuery.setRows(5000);
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

                            NoticeAllField noticeAllField = MapUtil.mapToBean(doc, NoticeAllField.class);
                            if (noticeAllField !=null){
                                resultList.add(noticeAllField);
                            }
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
            log.info("=====关键词:" + key + " solr执行到了：" + resultList.size());
        }
        return resultList;
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
}
