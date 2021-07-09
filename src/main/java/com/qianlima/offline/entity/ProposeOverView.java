package com.qianlima.offline.entity;

import lombok.Data;

/**
 * 项目概况
 */
@Data
public class ProposeOverView {
    /**
     * 项目编号
     */
    private String infoId;
    /**
     * 最新跟进
     */
    private String newFollow;
    /**
     * 进展阶段
     */
    private String followStage;
    /**
     * 项目性质
     */
    private String projectNature;
    /**
     * 业主类别
     */
    private String ownerType;
    /**
     * 项目类别
     */
    private String projectType;
    /**
     * 项目子类别
     */
    private String projectChildType;
    /**
     * 项目投资
     */
    private String investment;
    /**
     * 开工时间
     */
    private String startOnTime;
    /**
     * 竣工时间
     */
    private String endOnTime;
    /**
     * 建筑面积
     */
    private String coveredArea;
    /**
     * 占地面积
     */
    private String floorArea;
    /**
     * 建筑物层数
     */
    private String storey;
    /**
     * 	钢结构
     */
    private String steelwork;
    /**
     * 装修情况
     */
    private String decorationDesc;
    /**
     * 装修标准
     */
    private String decorationNorm;
    /**
     * 外墙预算
     */
    private String wallBudget;
    /**
     * 项目地址
     */
    private String proposeAddress;
    /**
     * 项目概况
     */
    private String proposeContent;
    /**
     * 进展阶段id（只进行逻辑规则判断，不进行任何展示操作）-不对用户提供
     * 1100  前期
     * 1200  设计
     * 1300  施工准备
     * 1400  施工在建
     * 1500  竣工
     * 1101  环评
     * 1102  可研
     * 1103  备案
     * 1104  审批
     * 1105  核准
     * 1106  立项
     * 1201  设计阶段
     * 1301  招标
     * 1401  主体施工
     * 1402  工程分包
     * 1403  室内外装修
     * 1404  景观绿化
     * 1501  竣工
     * 1107  国土
     */
    private Long followStageId;
}
