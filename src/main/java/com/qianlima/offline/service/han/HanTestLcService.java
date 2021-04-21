package com.qianlima.offline.service.han;

import com.qianlima.offline.bean.LcDto;
import com.qianlima.offline.bean.LcVo;

import java.util.List;

public interface HanTestLcService {
    List<LcVo> testLc(LcDto lcDto);

    Boolean uptestLc(Long id);
}