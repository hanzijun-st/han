package com.qianlima.offline.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.util.*;

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
     * json 转 listMap
     * @param json
     * @return
     */
    public static List<Map<String,Object>> jsonToListMap(String json){

        List<Map> list = JSON.parseArray(json, Map.class);
        List<Map<String,Object>> listMap = new ArrayList<>();
        if (list !=null && list.size() >0){
            for (Map map : list) {
                listMap.add(map);
            }
            return listMap;
        }
        return null;
    }

    /**
     * 读取json文件，获取json数据后解析
     * @param jsonFileName
     * @return
     * @throws Exception
     */
    public static JSONObject readJsonFile(String jsonFileName) throws Exception{
        jsonFileName = "/file/"+jsonFileName +".json";
        ClassPathResource resource = new ClassPathResource(jsonFileName);
        File filePath = resource.getFile();

        //读取文件
        String input = FileUtils.readFileToString(filePath, "UTF-8");
        //将读取的数据转换为JSONObject
        JSONObject jsonObject = JSONObject.parseObject(input);

        //返回整个json文件中的对象,对不同结构的数据进行解析
        return jsonObject;
    }

    public static JSONObject createExcel(String src, JSONObject json) {

        return null;
    }
}
