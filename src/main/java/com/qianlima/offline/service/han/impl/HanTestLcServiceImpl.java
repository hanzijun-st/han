package com.qianlima.offline.service.han.impl;

import com.qianlima.offline.bean.LcDto;
import com.qianlima.offline.bean.LcVo;
import com.qianlima.offline.mapper.TestLcMapper;
import com.qianlima.offline.service.han.HanTestLcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HanTestLcServiceImpl implements HanTestLcService {
    @Autowired
    private TestLcMapper testLcMapper;

    @Override
    public List testLc(LcDto lcDto) {
        List<LcVo> lcList = testLcMapper.getLcList(lcDto);
        return lcList;
    }

    @Override
    public Boolean uptestLc(Long id) {
        testLcMapper.uptestLc(id);
        return true;
    }
}