package com.qianlima.offline.bean;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 地区名称表
 */
@Data
@Document(collection = "area_alias")
public class AreaAlias {
    /**
     * 默认自增主键
     */
    @Id
    private String _id;

    @Field("id")
    private String id;

    @Field("areaid")
    private String areaid;

    @Field("name")
    private String name;

    @Field("type")
    private String type;

}
