package com.qianlima.offline.service.han;

import com.qianlima.offline.bean.Params;

import java.util.List;
import java.util.Map;

/**
 * 通用接口
 * Created by Administrator on 2021/1/14.
 */
public interface CurrencyService {
    /**
     *  区分 1全部，2.招标 3.中标
     */
    String getProgidStr(String str);

    /**
     * 一个关键词搜索
     * @param params
     */
    void getOnePoc(Params params);

    /**
     * 标的物
     */
    void getBdw();

    /**
     * 批量导入
     */
    void saveList();

    /**
     * 行业标签
     */
    void getBiaoQian(Integer type) throws Exception;

    void getPpei();

    void getPpeiJy();

    /**
     * HttpGet 方法
     * @param contentId
     * @return
     */
    String getHttpGet(String contentId);

    /**
     * 通用方法 ---获取标的物
     */
    void getTongYongBdw(String contentId) throws Exception;

    /**
     * 通用插入中台数据库的操作
     * @param map
     * @param sql 对应的sql
     */
    void saveTyInto(Map<String,Object> map, String sql);

    /**
     * 添加contentId 用来导出标的物
     */
    void saveContentId(String contentid);

    List<Map<String,Object>> getListMap(String sql);

}
