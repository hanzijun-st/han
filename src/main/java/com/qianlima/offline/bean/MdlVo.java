package com.qianlima.offline.bean;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 美敦力
 * Created by Administrator on 2021/1/15.
 */
@Data
public class MdlVo {

    private String sum;
    private String sumUnit;
    List<MdlTargetDetailsVo> targetDetails = new ArrayList<>();
}
