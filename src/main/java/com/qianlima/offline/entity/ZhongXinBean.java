package com.qianlima.offline.entity;

import lombok.Data;

@Data
public class ZhongXinBean {

    String name;//原企业名称（用来匹配数据）
    String actualCapital;//实收注册资金
    String regStatus;//企业状态
    String regCapital;//注册资金
    String regInstitute;//登记机关
    String companyName;//企业名称
    String businessScope;//经营范围
    String industry;//行业
    String regLocation;//注册地址
    String regNumber;//注册号
    String phoneNumber;//联系方式
    String creditCode;//统一社会信用代码
    String approvedTime;//核准时间
    String fromTime;//经营开始时间
    String companyOrgType;//企业类型
    String orgNumber;//组织机构代码
    String toTime;//经营结束时间
    String legalPersonName;//法人
    String province;//省
    String city;//市
    String country;//县
}