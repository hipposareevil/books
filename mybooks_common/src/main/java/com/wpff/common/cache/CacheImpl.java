package com.wpff.common.cache;

import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Cache implementation
 */
public class CacheImpl implements Cache {
  
  private JedisPool jedisPool;

  /**
   * Create new Cache
   * @param jedisPool Jedis
   */
  public CacheImpl(JedisPool jedisPool) {
   this.jedisPool = jedisPool;
  }

  /**
   * Set value in jedis
   */
  @Override
  public void set(String namespace, int key, String value) {
    Jedis jedis = null;
    try {
      jedis = this.jedisPool.getResource();

      jedis.zadd(namespace, key, value);
    } finally {
      if (jedis != null) {
        this.jedisPool.returnResource(jedis);
      }
    }
  }

  /**
   * Get value from jedis
   */
  @Override
  public String get(String namespace, int key) {
    Jedis jedis = null;
    try {
      jedis = this.jedisPool.getResource();

      String value = null;
      // Get value from zrange
      Set<String> v = jedis.zrangeByScore(namespace, key, key);
      if (!v.isEmpty()) {
        value = v.iterator().next();
      }

      return value;
    } finally {
      if (jedis != null) {
        this.jedisPool.returnResource(jedis);
      }
    }
  }

  /**
   * Clear the cache
   */
  @Override
  public void clear(String namespace) {
 Jedis jedis = null;
    try {
      jedis = this.jedisPool.getResource();

      jedis.del(namespace);
    } finally {
      if (jedis != null) {
        this.jedisPool.returnResource(jedis);
      }
    }    
  }
  
   /**
   * Clear the cache
   */
  @Override
  public void clear(String namespace, int key) {
 Jedis jedis = null;
    try {
      jedis = this.jedisPool.getResource();

      jedis.del(namespace);
    } finally {
      if (jedis != null) {
        this.jedisPool.returnResource(jedis);
      }
    }    
  }

}
