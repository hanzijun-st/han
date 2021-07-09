package com.qianlima.offline.entity;

import lombok.Data;

/**
 * 项目公司信息
 */
@Data
public class ProposeCompany {
    /**
     * 公司类型
     */
    private String companyType;
    /**
     * 公司名称
     */
    private String companyName;
    /**
     * 联系人
     */
    private String linkman;
    /**
     * 职务
     */
    private String duty;
    /**
     * 手机
     */
    private String phone;
    /**
     * 固定电话
     */
    private String mobile;
    /**
     * 传真
     */
    private String fax;
    /**
     * 公司地址
     */
    private String companyAddress;
}
