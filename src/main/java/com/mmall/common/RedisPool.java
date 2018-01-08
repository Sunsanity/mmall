package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by hasee on 2018/1/8.
 */
public class RedisPool {

    private static JedisPool pool;

    private static Integer maxTotal = Integer.valueOf(PropertiesUtil.getProperty("redis.max.total","20"));
    private static Integer maxIdle = Integer.valueOf(PropertiesUtil.getProperty("redis.max.idle","20"));
    private static Integer minTotal = Integer.valueOf(PropertiesUtil.getProperty("redis.min.idle","20"));

    private static Boolean testOnBorrow = Boolean.valueOf(PropertiesUtil.getProperty("redis.test.borrow","true"));
    private static Boolean testOnReturn = Boolean.valueOf(PropertiesUtil.getProperty("redis.test.return","true"));

    private static String redisIp = PropertiesUtil.getProperty("redis1.ip");
    private static Integer redisPort = Integer.valueOf(PropertiesUtil.getProperty("redis1.port"));

    /**
     * 初始化jedispool
     */
    private static void initPool(){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minTotal);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);
        config.setBlockWhenExhausted(true);

        pool = new JedisPool(config,redisIp,redisPort,1000*2);
    }

    static {
        initPool();
    }

    /**
     * 外界获取jedis方法
     * @return
     */
    public static Jedis getJedis(){
        return pool.getResource();
    }

    public static void returnBrokenResource(Jedis jedis){
        pool.returnBrokenResource(jedis);
    }

    public static void returnResource(Jedis jedis){
        pool.returnResource(jedis);
    }

    /**
     * 测试获取jedis
     * @param args
     */
    public static void main(String[] args) {
        Jedis jedis = RedisPool.getJedis();
        jedis.set("sunjingwen","zhangwei");
        returnResource(jedis);

        pool.destroy();
        System.out.println("redis测试结束！");
    }

}
