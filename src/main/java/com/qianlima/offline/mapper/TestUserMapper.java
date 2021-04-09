package com.qianlima.offline.mapper;

import com.qianlima.offline.entity.HanTestMy;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface TestUserMapper {
    List getTestList();

    void saveDatas(List<HanTestMy> list);

    void saveList(List<Map<String,Object>> maps);
}