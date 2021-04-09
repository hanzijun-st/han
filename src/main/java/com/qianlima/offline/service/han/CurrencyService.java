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
     *  判断 0:0、1:全部、2:招标[0 TO 2]、3:3、4:[0 TO 3]、5:中标[3 OR progid:5]、6:[0 OR progid:3]
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
    void getBdw(Integer type);

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

    /**
     * 标的物获取的第三版本
     * @param type
     */
    void getNewBdw3(Integer type);

    /**
     * 存库方法1
     */
    void saveData1(List<Map> maps);

    void getPiPeiHangYeBiaoQian();

    /**
     *  本地
     * @param name
     * @param list
     */
    void readFileByNameBd(String name,List<String> list);

    /**
     * 测试服务器
     * @param name
     * @param list
     */
    void readFileByName(String name,List<String> list);
}
