package com.qianlima.offline.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

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


    /**
     * @param redisTemplate redis操作模板工具
     * @param key           缓存主键
     * @param timeout       过期时间
     * @param timeUnit      过期时间单位
     * @return
     * @description 方法描述
     * @author hanzijun
     * @date 2021/06/01 14:39:00
     */
    public static void expire(RedisTemplate<Object, Object> redisTemplate, String key, long timeout, TimeUnit timeUnit) {
        boolean hasKey = redisTemplate.hasKey(key);
        // 如果key存在
        if (hasKey) {
            // 获取过期时间是永久则设置
            Long timeOut = redisTemplate.getExpire(key);
            if (timeOut.intValue() == -1) {
                redisTemplate.expire(key, timeout, timeUnit);
            }
        }
    }

    public static Object getValue(RedisTemplate<Object, Object>  redisTemplate, String key, Function<String, Object> mappingFunction){
        BoundValueOperations boundValueOperations = redisTemplate.boundValueOps(key);
        Object value = boundValueOperations.get();
        if(value == null){
            synchronized (key.intern()){
                value =  boundValueOperations.get();
                if(value == null){
                    value = mappingFunction.apply(key);
                    if(value != null){
                        boundValueOperations.set(value);
                    }
                }
            }
        }
        return value;
    }

    public static Object getHashValue(RedisTemplate<Object, Object> redisTemplate, String key, String hashKey, BiFunction<String, String, Object> mappingFunction){
        BoundHashOperations boundHashOperations = redisTemplate.boundHashOps(key);
        Object value = boundHashOperations.get(hashKey);
        if(value == null){
            synchronized (key.concat(hashKey).intern()){
                value =  boundHashOperations.get(hashKey);
                if(value == null){
                    value = mappingFunction.apply(key, hashKey);
                    if(value != null){
                        boundHashOperations.put(hashKey, value);
                    }
                }
            }
        }
        return value;
    }

    public static Map<?, ?> getHashValue(RedisTemplate<Object, Object> redisTemplate, String key, Function<String, Map<?,?>> mappingFunction){
        Map<?,?> entries = redisTemplate.opsForHash().entries(key);
        if(entries == null){
            synchronized (key.intern()){
                if(!redisTemplate.hasKey(key)){
                    entries = mappingFunction.apply(key);
                    if(entries != null){
                        redisTemplate.opsForHash().putAll(key, entries);
                    }
                }
            }
        }
        return entries;
    }

    public static String getHashValue(StringRedisTemplate redisTemplate, String key, String hashKey, BiFunction<String, String, String> mappingFunction){
        BoundHashOperations<String, String, String> boundHashOperations = redisTemplate.boundHashOps(key);
        String value = boundHashOperations.get(hashKey);
        if(value == null){
            synchronized (key.concat(hashKey).intern()){
                value =  boundHashOperations.get(hashKey);
                if(value == null){
                    value = mappingFunction.apply(key, hashKey);
                    if(value != null){
                        boundHashOperations.put(hashKey, value);
                    }
                }
            }
        }
        return value;
    }
}