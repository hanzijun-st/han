package com.qianlima.offline.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

/**
 * Created by Administrator on 2021/1/14.
 */
@Data
@ApiModel
public class Params {
    @ApiModelProperty("起始时间")
    private String time1;

    @ApiModelProperty("终止时间")
    private String time2;

    @ApiModelProperty("类型区分 1全部[0、1、2、3、5]，2.招标[0 1 2] 3.中标[3 5] 4.其他[0 1 2 3]")
    private String type;

    @ApiModelProperty("搜索范围 按标题搜索-title，按全文搜索-allcontent")
    private String title;

    @ApiModelProperty("是否存表  默认不存")
    private Integer isSave=0;

    @ApiModelProperty("是否有黑词 默认没有")
    private Integer isHave =0;

    @ApiModelProperty("是否导出 contentId,用来标的物  默认不导出")
    private Integer isSaveContentId =0;

}
