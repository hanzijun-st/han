import com.qianlima.offline.bean.Student;

import java.util.*;
import java.util.stream.Collectors;

public class TestString3 {
    public static void main(String[] args) {
       /* String str ="abc,";
        String[] split = str.split(",");
        for (String s : split) {
            System.out.println(s);
        }

        List<String> list = new ArrayList<>();
        list.add("abc");
        boolean b = equalList(list, Arrays.asList(split));
        System.out.println(b);*/

       //get();
        delSame();
    }
    public static boolean equalList(List list1, List list2) {
        return (list1.size() == list2.size()) && list1.containsAll(list2);
    }


    private static void get(){
        Student student = new Student();
        student.setName("zhangsan");
        student.setAge(20);

        Student student2 = new Student();
        student2.setName("zhangsan2");
        student2.setAge(30);

        Student student3 = new Student();
        student3.setName("zhangsan");
        student3.setAge(10);

        List<Student> list = new ArrayList<>();
        list.add(student);
        list.add(student2);
        list.add(student3);
        System.out.println("原始数据："+list);
        //判断姓名是否有重复,练习使用java8的stream方法
        //方法1. distinct, 直接比较大小，只知道是否有重复
        List<String> collect1 = list.stream().map(Student::getName).distinct().collect(Collectors.toList());
        System.out.println(collect1.size()!=list.size()?"方法1-姓名有重复":"无重复");
        //方法2.用户姓名计数
        Map<Object, Long> collect2 = list.stream().collect(
                Collectors.groupingBy(Student::getName , Collectors.counting()  )   );
        System.out.println("姓名重复计数情况："+collect2);
        //筛出有重复的姓名
        List<Object> collect3 = collect2.keySet().stream().
                filter(key -> collect2.get(key) > 1).collect(Collectors.toList());
        //可以知道有哪些姓名有重复
        System.out.println("方法2-重复的姓名 ： "+collect3);
        //方法3，对重复的姓名保留计数
        List<Map<String, Long>> collect4 = collect2.keySet().stream().
                filter(key -> collect2.get(key) > 1).map(key -> {
            Map<String, Long> map = new HashMap<>();
            map.put((String) key, collect2.get(key));
            return map;
        }).collect(Collectors.toList());
        System.out.println("方法3-重复的姓名及计数："+collect4);
    }

    private static void delSame(){
        List<Integer> numbersList = new ArrayList<>(Arrays.asList(1, 1, 2, 3, 3, 3, 4, 5, 6, 6, 6, 7, 8));
        List list = numbersList.stream().distinct().collect(Collectors.toList());
        System.out.println(list);


        Student student = new Student();
        student.setName("zhangsan2");
        student.setAge(30);

        Student student2 = new Student();
        student2.setName("zhangsan2");
        student2.setAge(20);

        Student student3 = new Student();
        student3.setName("zhangsan");
        student3.setAge(10);

        List<Student> lists = new ArrayList<>();
        lists.add(student);
        lists.add(student2);
        lists.add(student3);

        List<Student> students = lists.stream().filter(s -> s.getAge() >10).collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<Student>(Comparator.comparing(p -> p.getName()))), ArrayList::new));
        System.out.println(students);
    }
}