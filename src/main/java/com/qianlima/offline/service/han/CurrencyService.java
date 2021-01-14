package com.qianlima.offline.service.han;

import com.qianlima.offline.bean.Params;

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
}
