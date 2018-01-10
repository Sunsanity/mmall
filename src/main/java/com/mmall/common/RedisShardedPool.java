package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.*;
import redis.clients.util.Hashing;
import redis.clients.util.Sharded;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis分布式连接池
 * Created by hasee on 2018/1/9.
 */
public class RedisShardedPool {

    private static ShardedJedisPool pool;

    private static Integer maxTotal = Integer.valueOf(PropertiesUtil.getProperty("redis.max.total","20"));
    private static Integer maxIdle = Integer.valueOf(PropertiesUtil.getProperty("redis.max.idle","20"));
    private static Integer minTotal = Integer.valueOf(PropertiesUtil.getProperty("redis.min.idle","20"));

    private static Boolean testOnBorrow = Boolean.valueOf(PropertiesUtil.getProperty("redis.test.borrow","true"));
    private static Boolean testOnReturn = Boolean.valueOf(PropertiesUtil.getProperty("redis.test.return","true"));

    private static String redis1Ip = PropertiesUtil.getProperty("redis1.ip");
    private static Integer redis1Port = Integer.valueOf(PropertiesUtil.getProperty("redis1.port"));
    private static String redis2Ip = PropertiesUtil.getProperty("redis2.ip");
    private static Integer redis2Port = Integer.valueOf(PropertiesUtil.getProperty("redis2.port"));

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

        JedisShardInfo jedisShardInfo1 = new JedisShardInfo(redis1Ip,redis1Port,1000*2);
        JedisShardInfo jedisShardInfo2 = new JedisShardInfo(redis2Ip,redis2Port,1000*2);
        List<JedisShardInfo> jedisShardInfoList = new ArrayList<>(2);
        jedisShardInfoList.add(jedisShardInfo1);
        jedisShardInfoList.add(jedisShardInfo2);

        pool = new ShardedJedisPool(config,jedisShardInfoList, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);
    }

    static {
        initPool();
    }

    /**
     * 外界获取jedis方法
     * @return
     */
    public static ShardedJedis getJedis(){
        return pool.getResource();
    }

    public static void returnBrokenResource(ShardedJedis jedis){
        pool.returnBrokenResource(jedis);
    }

    public static void returnResource(ShardedJedis jedis){
        pool.returnResource(jedis);
    }

    /**
     * 测试获取分布式jedis连接
     * @param args
     */
    public static void main(String[] args) {
        ShardedJedis jedis = pool.getResource();

        for(int i =0;i<10;i++){
            jedis.set("key"+i,"value"+i);
        }

        returnResource(jedis);

        //pool.destroy();
        System.out.println("redis测试结束！");
    }
}
