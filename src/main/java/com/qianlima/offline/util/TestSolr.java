package com.qianlima.offline.util;

import com.alibaba.fastjson.JSONObject;
import com.qianlima.offline.bean.NoticeMQ;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CursorMarkParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Slf4j
public class TestSolr {
    @Autowired
    //@Qualifier("testsolr")
    private SolrClient solrClient;


    public void getAuthorizeContent() throws Exception{
        List<SolrInputDocument> wordList = new ArrayList<>();
        SolrInputDocument solrDocument = new SolrInputDocument();
        solrDocument.addField("userId", 1);
        solrDocument.addField("infoId", 2222);
        wordList.add(solrDocument);
        this.commitIndex(wordList);
    }
    private void commitIndex(List<SolrInputDocument> docs) throws IOException, SolrServerException {
        long start = System.currentTimeMillis();
        if (docs.size() > 0) {
            solrClient.add(docs);
            solrClient.commit(true, false);
        }
        long endTime = System.currentTimeMillis();
        log.debug("提交索引耗时：{}毫秒！数量{}", (endTime - start), docs.size());
        docs.clear();
    }

    public void insertToCore() throws SolrServerException, IOException{
        HttpSolrClient client = new HttpSolrClient("http://localhost:8099/solr/"+"solr");//solr部署的地址+core
        SolrInputDocument input = new SolrInputDocument();

        //各个属性值
        String uuid = UUID.randomUUID().toString().replace("-", "");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
        String time = format.format(new Date());
        input.addField("id", "3");
        input.addField("title", "战狼4");
        input.addField("subject", "动作、军事");
        input.addField("url", uuid);
        input.addField("content_type", "战狼3就是好看");
        client.add(input);
        client.commit();
        client.close();
    }
}
