package com.qianlima.offline.service.han;

import com.alibaba.fastjson.JSONArray;

public interface TestTencentService {

    void saveTencent();

    void jsonTo();

    void toIds() throws Exception;
    //JSONArray getDataType(String title, String content, Long contentid, String infoTypeUrl);
}