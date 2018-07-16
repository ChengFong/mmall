package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by 林成峰 on 2018/4/6.
 */
public class RedisPool {

    //jedis連接池
    private static JedisPool pool;

    //最大連接數
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total", "20"));

    //在jedispool中最大的idle狀態(空閒的)的jedis實例的個數
    private static Integer maxIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle", "10"));

    //在jedispool中最小的idle狀態(空閒的)的jedis實例的個數
    private static Integer minIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle", "2"));

    //在borrow一個jedis實例的時候，是否要進行驗證操作。
    //如果賦值true，則得到的jedis實例肯定是可以用的。
    private static Boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow", "true"));

    //在return一個jedis實例的時候，是否要進行驗證操作。
    //如果賦值true，則放回jedispool的jedis實例肯定是可以用的。
    private static Boolean testOnReturn = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.return", "true"));

    private static String redisIp = PropertiesUtil.getProperty("redis.ip");
    private static Integer redisPool = Integer.parseInt(PropertiesUtil.getProperty("redis.port"));

    static {

        initPool();
    }

    private static void initPool(){

        JedisPoolConfig config = new JedisPoolConfig();

        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);

        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);

        //連接耗盡的時候，是否阻塞。
        //false會拋出異常，true阻塞直到超時，默認為true
        config.setBlockWhenExhausted(true);

        pool = new JedisPool(config, redisIp, redisPool, 1000*2);
    }

    public static Jedis getJedis(){

        return pool.getResource();
    }

    public static void returnBrokenResource(Jedis jedis){

        pool.returnBrokenResource(jedis);
    }

    public static void returnResource(Jedis jedis){

        pool.returnResource(jedis);
    }

    public static void main(String[] args) {

        Jedis jedis = getJedis();

        jedis.set("geelykey", "geelyvalue");

        returnResource(jedis);

        //臨時調用，銷毀連接池中的所有連接
        pool.destroy();

        System.out.println("program is end");
    }

}
