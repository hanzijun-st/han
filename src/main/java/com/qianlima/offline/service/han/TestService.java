package com.qianlima.offline.service.han;

/**
 * Created by Administrator on 2021/1/12.
 */
public interface TestService {
    /**
     * solr 中获取条件（用来查询中台数据）
     */
    void getBdw();

    void getDatasToUpdateKeyword();

    void updateKeyword();

    String downLoad();
}
