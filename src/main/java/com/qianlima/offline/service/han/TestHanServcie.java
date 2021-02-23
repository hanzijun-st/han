package com.qianlima.offline.service.han;

public interface TestHanServcie {
    void test(String key, Integer value,Long time);

    String getRedis(String key);

    void saveRedis(String key, Integer value);
}