package com.qianlima.offline.service.han;

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
}
