package com.qianlima.offline.service.aquchong;

import com.qianlima.offline.bean.NoticeMQ;
import com.qianlima.offline.util.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Slf4j
public class AliDataService {

    @Autowired
    private CusDataFieldsService cusDataFieldsService;

    public void getSolrAllField() throws Exception{
        // 获取contentId列表信息
        List<String> contentIdList = LogUtils.readRule("contentIds");
        ExecutorService executorService = Executors.newFixedThreadPool(6);
        List<Future> futureList = new ArrayList<>();
        for (String contentId : contentIdList) {
            futureList.add(executorService.submit(() ->  getDataFromZhongTaiAndSave(contentId)));
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




    private void getDataFromZhongTaiAndSave(String infoId) {
        NoticeMQ noticeMQ = new NoticeMQ();
        noticeMQ.setContentid(Long.valueOf(infoId));
        boolean result = cusDataFieldsService.checkStatus(noticeMQ.getContentid().toString());
        if (result == false){
            log.info("contentid:{} 对应的数据状态不是99, 丢弃" , noticeMQ.getContentid().toString());
            return;
        }
        Map<String, Object> resultMap = cusDataFieldsService.getAllFieldsWithZiTi(noticeMQ, false);
        if (resultMap != null) {
            // 获取正文字段
            resultMap.put("code", noticeMQ.getKey());
            resultMap.put("keyword_term", resultMap.get("extract_proj_name"));
            cusDataFieldsService.saveIntoMysql(resultMap);
        }
    }

}
