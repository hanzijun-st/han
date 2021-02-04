package com.qianlima.offline.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.ss.formula.functions.T;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2021/1/15.
 */
public class JsonUtil {

    /**
     * 将对象转换成json字符串
     * @param obj
     * @return
     */
    public static String objToJsonStr(Object obj) {

        String str = JSON.toJSONString(obj);
        return str;
    }

    /**
     * 将json字符串转换成 Bean对象
     * @param json
     * @param bean
     * @return
     */
    public static <T> T jsonToBean(String json,Class<T> bean){
        T b  = JSON.parseObject(json, bean);
        return b;
    }

    /**
     * json 转 list
     * @param json
     * @return
     */
    public static List<Map<String,Object>> jsonToListMap(String json){
        JSONObject jsonStr = JSONObject.parseObject(json);
        List<Map<String, Object>> list = (List<Map<String,Object>>) jsonStr;
        if (list !=null && list.size() >0){
            return list;
        }
        return null;
    }
}
