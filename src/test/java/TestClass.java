import com.qianlima.offline.bean.Student;
import com.qianlima.offline.util.JsonUtil;
import com.qianlima.offline.util.MapUtil;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2021/1/12.
 */
public class TestClass {
    public static void main(String[] args) {
        /*Map<String, Object> map = XlsToXls.readXlsOne("E:\\excelFile\\2.xls", 0);

        System.out.println(map.toString());*/

        String json = "{'name':'hahaha','age':'10'}";
        Student abc = JsonUtil.jsonToBean(json, Student.class);
        System.out.println(abc.getName() + "---" + abc.getAge());

        String s = JsonUtil.objToJsonStr(abc);
        System.out.println(s);

        Map<String, Object> map = new HashMap<>();
        map.put("name", "哈哈哈");
        map.put("age", "20");

        Student abcClass = MapUtil.mapToBean(map, Student.class);
        //System.out.println(abcClass.getName()+"==="+abcClass.getAge());


        Map<String, Object> map1 = MapUtil.beanToMapNew(abcClass);
        System.out.println(map1);
    }

}
