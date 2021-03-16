package com.qianlima.offline.service.han.impl;

import com.qianlima.offline.service.han.TestHanServcie;
import com.qianlima.offline.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TestHanServcieImpl implements TestHanServcie {
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void test(String key, Integer value,Long time) {
        redisUtil.saveCode(key,value,time);
    }

    @Override
    public String getRedis(String key) {
        String value = redisUtil.getCodeVal(key);
        return value;
    }

    @Override
    public void saveRedis(String key, Integer value) {
        redisUtil.save(key,value);
    }
}