package com.qianlima.offline.bean;

import lombok.Data;

@Data
public class LcVo {
    private Long id;
    private Long approvalId;
    private String approvalName;
    private Integer approvalType;
    private Long projectId ;
}