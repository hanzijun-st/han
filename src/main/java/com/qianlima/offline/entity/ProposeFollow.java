package com.qianlima.offline.entity;

import lombok.Data;

/**
 * 项目跟进
 */
@Data
public class ProposeFollow {
    /**
     * 跟进时间
     */
    private String followTime;
    /**
     * 版本
     */
    private String followVersion;
    /**
     * 进展阶段
     */
    private String followStage;
    /**
     * 进展备注
     */
    private String followDesc;
}
