package com.qianlima.offline.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

public class ObjectUtil {

    /**
     *  将obj对象转List<String>
     * @param obj
     * @return
     */
    public static List<String> objToList(Object obj){
        JSONArray jsonArray = JSONObject.parseArray(obj.toString());
        List<String> list = JSONObject.parseArray(jsonArray.toJSONString(), String.class);
        return list;
    }
}