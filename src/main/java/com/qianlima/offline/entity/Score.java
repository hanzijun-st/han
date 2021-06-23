package com.qianlima.offline.entity;

import lombok.Data;

@Data
public class Score {

    private String infoId;

    private Integer firstScore;

    private Integer num;

    private Integer secondScore;

    private Object objectMap;

    public Score() {
    }

    public Score(String infoId, Integer num) {
        this.infoId = infoId;
        this.num = num;
    }

    public Score(String infoId, Integer firstScore, Integer secondScore) {
        this.infoId = infoId;
        this.firstScore = firstScore;
        this.secondScore = secondScore;
    }

    public Score(String infoId, Integer firstScore, Integer secondScore, Object objectMap) {
        this.infoId = infoId;
        this.firstScore = firstScore;
        this.secondScore = secondScore;
        this.objectMap = objectMap;
    }
}
