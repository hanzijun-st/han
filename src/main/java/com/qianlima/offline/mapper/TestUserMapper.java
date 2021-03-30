package com.qianlima.offline.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TestUserMapper {
    List getTestList();
}