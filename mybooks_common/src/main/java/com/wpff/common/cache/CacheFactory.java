package com.wpff.common.cache;

import redis.clients.jedis.JedisPool;

/**
 * Factory to create a Cache.  Requires JedisPool
 *
 */
public class CacheFactory {
  
  private static Cache cache = null;
  
  /**
   * Create a Cache
   * @param jedisPool Jedis
   * @return new Cache
   */
  public static synchronized Cache createCache(JedisPool jedisPool) {
    if (cache == null) {
      cache = new CacheImpl(jedisPool);
    }
    
    return cache;
  }

}
