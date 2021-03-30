package com.qianlima.offline.bean;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 信息定制业务--用户定制数据表
 */
@Data
@Document(collection = "authorize_cusdata_info")
public class AuthorizeCusdataInfo {

    @Id
    private String _id;
    /**
     * 客户id，与官网等程序保持一直
     */
    @Field("user_id")
    private String userId;
    /**
     * 信息id
     */
    @Field("info_id")
    private String infoId;
    /**
     * 存储的classbean类型
     * 对应的目录为common模块下面的 com.qianlima.data.classBean
     */
    @Field("class_bean")
    private String classBean;
    /**
     * 存储的json格式信息
     */
    @Field("json_bean")
    private JSONObject jsonBean;
    /**
     * 数据的创建时间
     */
    @Field("create_time")
    private String createTime;
}
