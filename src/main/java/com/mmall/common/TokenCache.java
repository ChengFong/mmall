package com.mmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by 林成峰 on 2017/8/1.
 */
public class TokenCache {

    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);

    public static final String TOKEN_PREFIX = "token_";

    //LRU算法
    private static LoadingCache<String, String> loadingCache = CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(10000).expireAfterAccess(12, TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
                //默認的數據加載實現，當調用get取值的時候，如果key沒有對應的值，就掉用這個方法進行加載
                @Override
                public String load(String s) throws Exception {
                    return "null";
                }
            });

    public static void setKey(String key, String value){
        loadingCache.put(key, value);
    }

    public static String getKey(String key){
        String value = null;

        try{

            value = loadingCache.get(key);

            if("null".equals(value)){
                return null;
            }

            return value;

        }catch (Exception e){
            logger.error("localCache get error", e);
        }

        return null;
    }

}
