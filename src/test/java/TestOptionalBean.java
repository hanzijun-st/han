import com.qianlima.offline.bean.Student;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TestOptionalBean {
    public static void main(String[] args) {
        Student p = new Student(1,"哈哈哈",29);
        Student p1 = new Student(2,"嘎嘎嘎",29);

        List<Student> list1 = new ArrayList<>();
        List<Student> list2 = new ArrayList<>();
        list1.add(p);
        list2.add(p1);
        List<Student> l = list1.stream().map(
                                pp -> list2.stream().filter(
                                        ppp -> pp.getAge().equals(ppp.getAge())).findFirst().map(girl -> {
                                            return pp;
                                         }).orElse(null))
                         .collect(Collectors.toList());
        //String s = OptionalBean.ofNullable(p).getBean(Person::getName).get();//得到ame值
        System.out.println(l.toString());
    }
}

/*
@Data
class Person {
    private String name;
    private Integer age;
    private String address;

    public Person(String name,Integer age,String address){
        this.name = name;
        this.age = age;
        this.address = address;
    }
}*/
