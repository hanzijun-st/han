import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class TestJsonObject {
    public static void main(String[] args){

        JSONArray array = JSONArray.parseArray("[{'name':'hehe','age':22}]");
        System.out.println(array.size());

        for (int i =0; i< array.size();i++) {
            JSONObject jsonObject = array.getJSONObject(i);
            System.out.println(jsonObject.get("name"));
        }
    }
}