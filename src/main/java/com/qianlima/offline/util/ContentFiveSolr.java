package com.qianlima.offline.util;

import com.mongodb.client.model.Facet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ContentFiveSolr {

    @Autowired
    private SolrClient solrClient;

    @Autowired
    @Qualifier("bdJdbcTemplate")
    private JdbcTemplate bdJdbcTemplate;


    /* public List<Map<String,Object>> companyResultsBaoXian(String tiaojian,String unit) {

         SolrQuery solrQuery = new SolrQuery();
         solrQuery.setQuery(tiaojian);
         solrQuery.setRows(0);
         solrQuery.setFacet(true).addFacetField(unit);
         solrQuery.setFacetMinCount(1);
         solrQuery.setFacetLimit();
         try {
             QueryResponse response = solrClient.query(solrQuery, SolrRequest.METHOD.POST);
             List<FacetField> facetFields = response.getFacetFields();
             List<Map<String,Object>> list = new ArrayList<>();
             for (FacetField facetField : facetFields) {
                 List<FacetField.Count> values = facetField.getValues();
                 if (values != null && values.size() > 0) {
                     for (FacetField.Count value : values) {
                         String name = value.getName();
                         if (StringUtils.isNotEmpty(name)) {
                             long count = value.getCount();
                             Map<String,Object> map = new HashMap<>();
                             map.put("key",name);
                             map.put("value",count);
                             list.add(map);
                         }
                     }
                 }
             }
             return list;
         }catch (Exception e){
             log.error("数据错误",e.getMessage());
         }
         return null;
     }*/
    public Map<String, Long> companyResultsBaoXian(String condition, String field) throws Exception {
        // 设置开始页
        Integer rows = 0;
        Integer limit = 100;
        Map<String, Long> resultMap = new HashMap<>();
        while (true) {
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setQuery(condition);
            solrQuery.setRows(rows);
            solrQuery.setFacet(true).addFacetField(field);
            solrQuery.setFacetLimit(limit);
            solrQuery.setParam("facet.offset", rows.toString());
            QueryResponse response = solrClient.query(solrQuery, SolrRequest.METHOD.POST);
            List<FacetField> facetFields = response.getFacetFields();
            if (facetFields != null && facetFields.size() > 0) {
                for (FacetField facetField : facetFields) {
                    List<FacetField.Count> values = facetField.getValues();
                    if (values != null && values.size() > 0) {
                        for (FacetField.Count value : values) {
                            //resultMap.put(value.getName(), value.getCount());
                            String name = value.getName();
                            String num = String.valueOf(value.getCount());
                            bdJdbcTemplate.update("INSERT INTO han_tongji_zb (name,num,type) VALUES (?,?,?)", name, num, "2");
                            log.info("中标入库 name:{}", name);
                        }
                    } else {
                        break;
                    }
                }
                rows += limit;
            } else {
                break;
            }
        }
        return resultMap;
    }

    public Map<String, Long> companyResultsZhaoBiao(String condition, String field) throws Exception {
        // 设置开始页
        Integer rows = 0;
        Integer limit = 100;
        Map<String, Long> resultMap = new HashMap<>();
        while (true) {
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setQuery(condition);
            solrQuery.setRows(rows);
            solrQuery.setFacet(true).addFacetField(field);
            solrQuery.setFacetLimit(limit);
            solrQuery.setParam("facet.offset", rows.toString());
            QueryResponse response = solrClient.query(solrQuery, SolrRequest.METHOD.POST);
            List<FacetField> facetFields = response.getFacetFields();
            if (facetFields != null && facetFields.size() > 0) {
                for (FacetField facetField : facetFields) {
                    List<FacetField.Count> values = facetField.getValues();
                    if (values != null && values.size() > 0) {
                        for (FacetField.Count value : values) {
                            //resultMap.put(value.getName(), value.getCount());
                            String name = value.getName();
                            String num = String.valueOf(value.getCount());
                            bdJdbcTemplate.update("INSERT INTO han_tongji (name,num,type) VALUES (?,?,?)", name, num, "1");
                            log.info("招标入库 name:{}", name);
                        }
                    } else {
                        break;
                    }
                }
                rows += limit;
            } else {
                break;
            }
        }
        return resultMap;
    }


}
