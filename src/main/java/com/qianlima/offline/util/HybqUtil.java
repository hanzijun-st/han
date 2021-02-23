package com.qianlima.offline.util;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 行业标签-工具
 */
public class HybqUtil {


    /**
     * 将字符串进行拆分，放入map返回
     * @param str
     * @return
     */
    public static Map getHashMap(String str) {
        Map<String,Object> map = new HashMap<>();
        String[] split = str.split("-");
        map.put(split[0],split[1]);
        return map;
    }


}
