import com.qianlima.offline.bean.Student;

import java.util.*;
import java.util.stream.Collectors;

public class TestLambda {
    public static void main(String[] args) {
        List<Student> list = new ArrayList<>();
        Student student = new Student();
        student.setAge(12);
        student.setName("H");
        list.add(student);
        Student student2 = new Student();
        student2.setAge(22);
        student.setName("L");
        list.add(student2);
        //long count = list.stream().distinct().parallel().count();
        System.out.println(list);
        Long count = list.stream().filter(item -> item.getAge().intValue() == 12).collect(Collectors.toList()).stream().mapToInt(Student::getAge).count();
        Integer sum = list.stream().filter(item -> item.getAge().intValue() == 12).collect(Collectors.toList()).stream().mapToInt(Student::getAge).sum();

        Map<Integer, Long> size = list.stream().collect(Collectors.groupingBy(Student::getAge, Collectors.counting()));
        System.out.println("size:"+size);
        System.out.println(sum);
        System.out.println(count);

        //Map<Integer, String> c = list.stream().filter(item -> item.getId() !=null && item.getId() == 1).collect(Collectors.toMap(Student::getAge, Student::getName));
        List<Integer> collect = list.stream().map(Student::getAge).collect(Collectors.toList());//将对象某个属性变成集合
        System.out.println(collect);

        long count2 = list.stream().mapToInt(Student::getAge).sum();
        System.out.println("统计2："+count2);

        long count1 = list.stream().count();
        System.out.println("集合数量："+count1);

        List<Student> resultList=list.stream().collect(
                Collectors.collectingAndThen(
                    Collectors.toCollection(
                            () -> new TreeSet<Student>(
                                    Comparator.comparing(
                                            p -> p.getAge()
                                    )
                            )
                    ),ArrayList::new
                )
        );


        //计算学生的平均年龄
        Double avgAge = list.stream().collect(Collectors.collectingAndThen(Collectors.averagingDouble(Student::getAge), Double::doubleValue));
        System.out.println("学生的平均年龄："+avgAge);

        list.stream().mapToInt(Student::getAge).max();

    }
}