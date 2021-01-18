package com.qianlima.offline.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * 返回腾信行业
 */
@Data
public class TencentIndustryRes implements Serializable {

    //一级分类
    private String firstLevel;

    //二级分类
    private String secondLevel;

    //三级行业
    private String thirdLevel;

    public TencentIndustryRes() {
    }

    public TencentIndustryRes(String firstLevel, String secondLevel, String thirdLevel) {
        this.firstLevel = firstLevel;
        this.secondLevel = secondLevel;
        this.thirdLevel = thirdLevel;
    }
}