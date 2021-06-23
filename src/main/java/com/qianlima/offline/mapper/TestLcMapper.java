package com.qianlima.offline.mapper;

import com.qianlima.offline.bean.LcDto;
import com.qianlima.offline.bean.LcVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface TestLcMapper {

    List<LcVo> getLcList(LcDto lcDto);

    void uptestLc(Long id);

    Map<String,Object> getMapForOne(LcDto lcDto);

    void saveToDef(LcDto lcDto);
}