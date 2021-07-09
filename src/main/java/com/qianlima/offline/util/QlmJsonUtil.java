package com.qianlima.offline.util;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * 主要为了解决使用JSONObject.toJSONString()转化为jsonbean时，字段丢失的问题。
 * 这里实现一个通用工具类
 */
public class QlmJsonUtil {

    public static JSONObject toJsonBean(Object object) {
        JSONObject jsonBean = JSONObject.parseObject(toJSONString(object));
        return jsonBean;
    }

    public static String toJSONString(Object object) {
        String str = JSONObject.toJSONString(object
                , SerializerFeature.WriteNullStringAsEmpty
                , SerializerFeature.WriteNullListAsEmpty
                , SerializerFeature.WriteMapNullValue
        );
        return str;
    }

}
