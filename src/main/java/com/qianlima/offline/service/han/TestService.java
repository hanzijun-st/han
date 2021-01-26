package com.qianlima.offline.service.han;

/**
 * Created by Administrator on 2021/1/12.
 */
public interface TestService {
    /**
     * 之前的方式获取标的物
     */
    void getBdw();

    /**
     * 新方式获取标的物
     */
    void getNewBdw();

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
