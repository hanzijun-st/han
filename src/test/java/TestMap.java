package test.java;

import com.qianlima.offline.util.JsonUtil;
import com.qianlima.offline.util.MapUtils;

import java.util.HashMap;
import java.util.Map;

public class TestMap {
    public static void main(String[] args) {
        Map<String,String> map = new HashMap<>();
        map.put("1","韩子君");
        map.put("2","哈哈哈");

        String s = MapUtils.toJson(map);
        System.out.println(s);


    }
}