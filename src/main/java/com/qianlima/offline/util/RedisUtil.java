package com.qianlima.offline.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisUtil {

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 加锁，
     * @param key
     * @param expire 时长，单位默认为秒
     * @return
     */
    public boolean lock(String key, long expire) {
        // 锁不存在的话，设置锁并设置锁过期时间，即加锁
        if (redisTemplate.opsForValue().setIfAbsent(key, "1", expire, TimeUnit.SECONDS)) {
            return true;
        }
        return false;
    }

    /**
     * 解锁
     */
    public boolean unlock(String key) {
        try {
            // 锁存在的话，删除锁，即释放锁
            if (redisTemplate.hasKey(key)) {
                redisTemplate.delete(key);
                return true;
            }
        } catch (Exception e) {
            throw new RuntimeException("用户释放锁失败", e);
        }
        return false;
    }


    
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    
    public String getCodeVal(String key) {
        try {
            String value = redisTemplate.opsForValue().get(key).toString();
            return value;
        }catch (Exception e){
            return "";
        }
    }

    public void saveCode(String key, Object val, Long expiration) {
        redisTemplate.opsForValue().set(key, val, expiration, TimeUnit.MINUTES);
    }

    public void save(String key, Object val) {
        redisTemplate.opsForValue().set(key, val);
    }

    public void judge(String keyLogin){
        // 判断key值是否存在
        if ( ! redisTemplate.hasKey(keyLogin)){
            redisTemplate.opsForValue().set(keyLogin ,1, 60, TimeUnit.MINUTES);
        }else {
            redisTemplate.opsForValue().increment(keyLogin,1);
        }
    }

    public Integer getLoginKeyCount(String loginKeyLock) {
        if ( ! redisTemplate.hasKey(loginKeyLock)){
            return 0;
        }
        return (Integer)redisTemplate.opsForValue().get(loginKeyLock);
    }
}