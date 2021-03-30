package com.qianlima.offline.bean;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 全国区划表到地区表的映射关系表
 */
@Data
@Document(collection = "newarea_to_oldarea")
public class NewareaToOldarea {
    /**
     * 默认自增主键
     */
    @Id
    private String _id;

    @Field("new_id")
    private String newId;

    @Field("new_level")
    private String newLevel;

    @Field("new_name")
    private String newName;

    @Field("new_pid")
    private String newPid;

    @Field("old_id")
    private String oldId;

    @Field("old_level")
    private String oldLevel;

    @Field("old_name")
    private String oldName;

    @Field("old_pid")
    private String oldPid;









}
