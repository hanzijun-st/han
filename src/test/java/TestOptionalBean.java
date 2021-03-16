package test.java;

import com.qianlima.offline.util.OptionalBean;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TestOptionalBean {
    public static void main(String[] args) {
        Person p = new Person("哈哈哈",29,"沙河");
        Person p1 = new Person("嘎嘎嘎",29,"西二旗");

        List<Person> list1 = new ArrayList<>();
        List<Person> list2 = new ArrayList<>();
        list1.add(p);
        list2.add(p1);
        List<Person> l = list1.stream().map(
                                pp -> list2.stream().filter(
                                        ppp -> pp.getAge().equals(ppp.getAge())).findFirst().map(girl -> {
                                            return pp;
                                         }).orElse(null))
                         .collect(Collectors.toList());
        //String s = OptionalBean.ofNullable(p).getBean(Person::getName).get();//得到ame值
        System.out.println(l.toString());
    }
}

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
}