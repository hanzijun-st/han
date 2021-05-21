package com.qianlima.offline.bean;

import lombok.Data;

import java.util.List;

/**
 * Created by Administrator on 2021/1/13.
 */
@Data
public class Student {
    private Integer id;
    private String name;
    private Integer age;
    private List<Student> children;

}
