package com.qianlima.offline.service.han.impl;

import com.qianlima.offline.entity.TestUser;
import com.qianlima.offline.mapper.TestUserMapper;
import com.qianlima.offline.service.han.TestMyBatisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TestMyBatisServiceImpl implements TestMyBatisService {
    //@Autowired
    //private TestUserMapper testUserMapper;

    @Override
    public void testMyBatis() {
        //TestUser testUser = testUserMapper.selectByPrimaryKey(1);
        log.info("name:{}");
    }
}