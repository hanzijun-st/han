import com.qianlima.offline.bean.Student;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

//对象通过某个属性去重
public class TestBeanQuChong {
    public static void main(String[] args) {
        Student s = new Student();
        s.setName("h");
        s.setAge(10);
        Student s2 = new Student();
        s2.setName("h");
        s2.setAge(20);
        Student s3 = new Student();
        s3.setName("h");
        s3.setAge(30);
        List<Student> list = new ArrayList<>();
        list.add(s);
        list.add(s2);
        list.add(s3);

        List<Student> result = list.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<Student>(Comparator.comparing(p -> p.getName()))),
                        ArrayList::new));
        System.out.println(result.toString());
    }
}