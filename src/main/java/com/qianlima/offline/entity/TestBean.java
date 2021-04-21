package com.qianlima.offline.entity;

import lombok.Data;

@Data
public class TestBean {
    private Long id;
    private Long projectId;
    private Long approvalId;
    private String approvalName;
    private String approvalType;
    private String status;//数据状态
    private String createTime;
    private String updateTime;
}