package com.qianlima.offline.service.han.impl;

import com.qianlima.offline.mapper.TestUserMapper;
import com.qianlima.offline.service.han.TestMyBatisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class TestMyBatisServiceImpl implements TestMyBatisService {
    @Resource
    private TestUserMapper testUserMapper;

    @Override
    public List testMyBatis() {
        List testList = testUserMapper.getTestList();
        return testList;
    }
}