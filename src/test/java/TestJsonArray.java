import com.alibaba.fastjson.JSONArray;
import com.qianlima.offline.util.JsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestJsonArray {
    public static void main(String[] args) {
        List<Map> list = new ArrayList<>();
        Map<String,Object> map = new HashMap<>();
        Map<String,Object> map2 = new HashMap<>();
        map.put("k1","h");
        list.add(map);
        map2.put("k2","h2");
        list.add(map2);

        String jsonStr = "[{\"id\":\"01\",\"open\":false,\"pId\":\"0\",\"name\":\"A部门\"},{\"id\":\"011\",\"open\":false,\"pId\":\"01\",\"name\":\"A部门\"}]";
        List<Map<String, Object>> mapList = JsonUtil.jsonToListMap(jsonStr);
        System.out.println(mapList.toString());

    }
}