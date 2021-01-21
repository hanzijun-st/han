package com.qianlima.offline.service.han;

/**
 * Created by Administrator on 2021/1/12.
 */
public interface TestService {
    /**
     * solr 中获取条件（用来查询中台数据）
     */
    void getBdw();

    void updateKeyword();

    String downLoad();

    /**
     * 上海联影医疗
     */
    void getShangHaiLy();

    /**
     * 重庆地区
     */
    void getChongqi();
}
