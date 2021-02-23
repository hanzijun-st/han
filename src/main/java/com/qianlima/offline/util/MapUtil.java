package com.qianlima.offline.util;

import com.google.common.collect.Maps;
import org.springframework.cglib.beans.BeanMap;

import java.util.*;

/**
 * Created by Administrator on 2021/1/15.
 */
public class MapUtil {

    /**
     * 将对象转成 Map
     * @param bean
     * @param <T>
     * @return
     */
    public static <T> Map<String, Object> beanToMap(T bean) {
        Map<String, Object> map = Maps.newHashMap();
        if (bean != null) {
            BeanMap beanMap = BeanMap.create(bean);
            for (Object key : beanMap.keySet()) {
                map.put(key + "", beanMap.get(key));
            }
        }
        return map;
    }
    /**
     * 将map转换为 bean对象
     *
     * @param map
     * @param bean
     * @return
     */
    public static <T> T mapToBean(Map<String, Object> map, T bean) {
        BeanMap beanMap = BeanMap.create(bean);
        beanMap.putAll(map);
        return bean;
    }

    /**
     * 获取单个map的key
     * @param map
     * @param type 1为key  2为value
     * @return
     */
    public static String getMapToKeyOrValue(Map<String,Object> map,Integer type){
        for(String key : map.keySet()) {
            if (type.intValue() == 1){
                return key;
             }
             return map.get(key).toString();
        }
        return null;
    }

    public static List<String> getMapToKeyOrValue(Map<String,Object> map){

        List<String> list = new ArrayList<>();
        Set keys = map.keySet();
        if(keys != null) {
            Iterator iterator = keys.iterator();
            while(iterator.hasNext()) {
                Object key = iterator.next();
                Object value = map.get(key);
                list.add(key.toString());
                list.add(value.toString());
            }
        }
        return list;
    }
}
