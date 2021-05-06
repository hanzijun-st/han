import com.qianlima.offline.bean.Student;
import com.qianlima.offline.util.CollectionUtils;
import com.qianlima.offline.util.JsonUtil;
import com.qianlima.offline.util.MapUtil;
import lombok.Data;

import java.util.*;

/**
 * Created by Administrator on 2021/1/12.
 */
public class TestClass {
    public static void main(String[] args) {
        /*Map<String, Object> map = XlsToXls.readXlsOne("E:\\excelFile\\2.xls", 0);

        System.out.println(map.toString());*/

       /* String json = "{'name':'hahaha','age':'10'}";
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
        System.out.println(map1);*/

        List<Student> list = new ArrayList<>();
        Student student = new Student();
        student.setAge(10);
        student.setName("韩");
        Student student2 = new Student();
        student2.setAge(20);
        student2.setName("家");
        Student student3 = new Student();
        student3.setAge(25);
        student3.setName("李");
        list.add(student);
        list.add(student2);
        list.add(student3);
        System.out.println("原始数组："+list);
        Collections.sort(list, Comparator.comparing(Student::getName).reversed());
        System.out.println("倒叙："+list);

        Collections.sort(list,Comparator.comparing(Student::getName));
        System.out.println("正序："+list);
    }

}
