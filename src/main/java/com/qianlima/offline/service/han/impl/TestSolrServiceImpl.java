package com.qianlima.offline.service.han.impl;

import com.qianlima.offline.service.han.TestSolrService;
import com.qianlima.offline.util.TestSolr;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TestSolrServiceImpl implements TestSolrService {


    @Autowired
    private TestSolr testSolr;

    @Override
    public void testSolr() {
        try {
            testSolr.getAuthorizeContent();
        } catch (Exception e) {
            log.info("异常:{}",e);
            e.getMessage();
        }
    }
}