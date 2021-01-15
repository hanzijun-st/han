package com.qianlima.offline.util;

import com.alibaba.fastjson.JSON;
import org.apache.poi.ss.formula.functions.T;

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
}
