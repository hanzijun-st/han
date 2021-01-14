package com.qianlima.offline.service.han;

import com.qianlima.offline.bean.Params;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2021/1/12.
 */
public interface AoLinBaSiService {

    void getAoLinBaSiAndSave();

    /**
     * 得到原来url链接
     */
    String getUrlOriginalLink(String num);

    /**
     * 佳电(上海)管理有限公司
     *   time1 起始时间
     *   time2 终止时间
     *   type 区分 1全部，2.招标 3.中标
     *   titleOrAllcontent: 按标题搜索-title，按全文搜索-allcontent
     */
    void getJdgl(String time1,String time2,String type,String titleOrAllcontent);

}
