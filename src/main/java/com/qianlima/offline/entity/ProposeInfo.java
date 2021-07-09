package com.qianlima.offline.entity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 拟在建项目数据表
 */
@Data
@Document(collection = "propose_info")
public class ProposeInfo {

    @Id
    private String _id;
    /**
     * 信息id
     */
    @Field("info_id")
    private String infoId;
    /**
     * 信息标题
     */
    @Field("info_title")
    private String infoTitle;
    /**
     * 信息地区--省
     */
    @Field("area_province")
    private String areaProvince;
    /**
     * 信息地区--市
     */
    @Field("area_city")
    private String areaCity;
    /**
     * 信息地区--区县
     */
    @Field("area_country")
    private String areaCountry;
    /**
     * 信息来源（千里马url）
     */
    @Field("info_qianlima_url")
    private String infoQianlimaUrl;
    /**
     * 项目跟进
     */
    @Field("propose_follows")
    private JSONArray proposeFollows;
    /**
     * 项目概况
     */
    @Field("propose_over_view")
    private JSONObject proposeOverView;
    /**
     * 项目公司联系方式
     */
    @Field("propose_companys")
    private JSONArray proposeCompanys;
    /**
     * 发布时间 格式：yyyy-MM-dd HH:mm:ss
     */
    @Field("info_publish_time")
    private String infoPublishTime;
    /**
     * 更新时间 格式：yyyy-MM-dd HH:mm:ss
     */
    @Field("info_update_time")
    private String infoUpdateTime;
    /**
     * 创建时间(数据的更新时间)
     */
    @Field("create_time")
    private Long createTime;
}
