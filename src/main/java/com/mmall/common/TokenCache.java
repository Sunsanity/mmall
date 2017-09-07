package com.mmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


/**
 * Created by hasee on 2017/5/20.
 */
public class TokenCache {
    //打印错误日志
    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);

    //token前缀
    public static final String TOKEN_PREFIX = "token_";

    private static LoadingCache<String,String> loadingCache = CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(10000).expireAfterAccess(12, TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
                //默认的数据加载实现，当调用get请求值对应的value为空的时候调用这个方法加载数据
                @Override
                public String load(String s) throws Exception {
                    return "null";
                }
            });

    public static void setKey(String key,String value){
        loadingCache.put(key,value);
    }

    public static String getKey(String key){
        String value = null;
        try {
            value = loadingCache.get(key);
            if("null".equals(key)){
                return null;
            }
            return value;
        } catch (ExecutionException e) {
            logger.error("localCache get error!",e);
        }
        return null;
    }
}
