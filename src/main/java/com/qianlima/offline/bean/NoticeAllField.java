package com.qianlima.offline.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * solr 中所有字段
 */
@Data
public class NoticeAllField implements Serializable {

    private Long contentid;
    private String progid;//信息类型
    private String segmentType;//细分类型
    private String catid;//100以下:招标；拟在建项目 （101：项目信息；201：VIP项目；） 审批项目（301：工程数据； 601：施工许可证；）
    private String url;
    private String updatetime;
    private String areaid;//地区id

    /**
     * 省市县
     */
    private String newProvince;//新地区 省
    private String newCity ;//新地区 市
    private String newCountry;//县

    /**
     * 自提招标
     */
    private String zhaoBiaoUnit;//自提招标单位
    private String zhaoRelationName;//自提招标单位联系人
    private String zhaoRelationWay ;//自提招标单位联系方式
    private String zhaoFirstIndustry ;//自提招标单位一级行业 KA内部行业划分
    private String zhaoSecondIndustry;//自提招标单位二级行业 KA内部行业划分
    private String zhaoTYCIndustry;//自提招标单位二级行业 天眼查
    private String budget;//自提招标预算
    private String budgetNumber;//自提招标预算 只有纯数字的才会进这里

    /**
     * 自提代理机构
     */
    private String agentUnit ;//自提代理机构
    private String agentRelationName;//自提代理机构联系人
    private String agentRelationWay;//自提代理机构联系方式

    /**
     *  百炼
     */
    private String blZhaoBiaoUnit;//百炼招标单位（存储多个，用英文逗号分隔）
    private String blAmountUnit;//百炼中标金额(只取第一个)
    private String blAgentUnit;//百炼代理机构
    private String blBudget;//百炼招标预算(只取第一个)
    private String blZhongBiaoUnit;//百炼中标单位（存储多个，用英文逗号分隔）

    /**
     * 混合
     */
    private String newZhaoBiaoUnit;//混合招标单位（自提为主）
    private String newAmountUnit;//混合中标金额（自提为主）
    private String newAgentUnit;//混合代理机构（自提为主）
    private String newBudget;//混合招标预算
    private String newZhongBiaoUnit;//混合中标单位（自提为主）

    /**
     *  自提中标
     */
    private String zhongBiaoUnit;//自提中标单位
    private String zhongRelationName;//自提中标单位联系人
    private String zhongRelationWay;//自提中标单位联系方式
    private String zhongFirstIndustry;//自提中标单位一级行业 KA内部行业划分
    private String zhongSecondIndustry;//自提中标单位二级行业 KA内部行业划分
    private String zhongTYCIndustry;//自提中标单位二级行业  天眼查
    private String amountUnit;//自提中标金额
    private String amountNumber;//自提中标金额 只有纯数字的才会进这里


    private String xmNumber;//项目编号
    private String projName;//项目名称
    private String isElectronic;//是否电子招标 0 否1 是
    private String biddingType;//招标方式
    private String allcontent;//全文搜索
    private String title;//标题搜索




}
