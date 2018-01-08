package com.mmall.util;


import com.mmall.common.RedisPool;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

/**
 * Created by hasee on 2018/1/8.
 */
@Slf4j
//jedis基础操作封装类
public class RedisPoolUtil {

    /**
     * 设置有效时间
     * @param key
     * @param exTime
     * @return
     */
    public static Long expire(String key,int exTime){
        Jedis jedis = null;
        Long result = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.expire(key,exTime);
        }catch (Exception e){
            log.error("expire error key:{}",key,e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    /**
     * 带有效期的set
     * @param key
     * @param value
     * @param exTime
     * @return
     */
    public static String setEx(String key,String value,int exTime){
        Jedis jedis = null;
        String result = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.setex(key,exTime,value);
        }catch (Exception e){
            log.error("setEx error key:{} value:{}",key,value,e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    /**
     * 不带有效期的set
     * @param key
     * @param value
     * @return
     */
    public static String set(String key,String value){
        Jedis jedis = null;
        String result = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.set(key,value);
        }catch (Exception e){
            log.error("set error key:{} value:{}",key,value,e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    /**
     * get方法
     * @param key
     * @return
     */
    public static String get(String key){
        Jedis jedis = null;
        String result = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.get(key);
        }catch (Exception e){
            log.error("get error key:{}",key,e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    /**
     * 删除
     * @param key
     * @return
     */
    public static Long del(String key){
        Jedis jedis = null;
        Long result = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.del(key);
        }catch (Exception e){
            log.error("del error key:{}",key,e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    /**
     * redis封装类测试
     * @param args
     */
    public static void main(String[] args) {
        RedisPoolUtil.set("zhangwei","sunjingwen");
        String value = RedisPoolUtil.get("zhangwei");
        RedisPoolUtil.expire("zhangwei",60*20);
        RedisPoolUtil.setEx("liuqiqi","sunjingwen",60*20);
        RedisPoolUtil.del("zhangwei");

        System.out.println("redis封装类测试结束！");
    }
}
