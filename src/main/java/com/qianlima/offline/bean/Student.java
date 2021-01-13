package com.qianlima.offline.bean;

import lombok.Data;

/**
 * Created by Administrator on 2021/1/13.
 */
@Data
public class Student {
    private Integer id;
    private String name;
    private Integer age;

    public Student(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

}
