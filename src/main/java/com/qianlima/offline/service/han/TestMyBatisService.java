package com.qianlima.offline.service.han;

import com.qianlima.offline.bean.LcDto;
import com.qianlima.offline.entity.TestUser;

import java.util.List;
import java.util.Map;

public interface TestMyBatisService {
   /* List testMyBatis();

    void saveDatas();*/
   List testLc(LcDto lcDto);

   Map<String,Object> getMapForOne(LcDto lcDto);
}