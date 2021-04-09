import com.alibaba.fastjson.JSONObject;
import com.qianlima.offline.util.JsonUtil;

public class TestJson {
    public static void main(String[] args) {
        try {
            JSONObject jsonObject = JsonUtil.readJsonFile("test");
            String name = jsonObject.getString("name");
            String age = jsonObject.getString("age");
            System.out.println("姓名："+name+",年龄："+age);

        } catch (Exception e) {

        }
    }
}