package com.qianlima.offline.bean;

import lombok.Data;

@Data
public class TencentIndustryCompany {

    private Integer id;
    /**
     * 一级行业（腾讯）
     */
    private String firstIndustry;
    /**
     * 二级行业（腾讯）
     */
    private String secondIndustry;
    /**
     * 三级行业（腾讯）
     */
    private String thirdIndustry;
    /**
     * 企业名录
     */
    private String company;
}
