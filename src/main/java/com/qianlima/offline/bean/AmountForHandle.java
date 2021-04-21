package com.qianlima.offline.bean;

import lombok.Data;

@Data
public class AmountForHandle {
    private Long id;
    private Long infoId;
    private String userId;
    private String infoPublishTime;
    private String oldWinnerAmount;
    private String newWinnerAmount;
    private String oldBudget;
    private String newBudget;
    private Integer states;
    private Long updateTime;
    private Long handleTime;
}
