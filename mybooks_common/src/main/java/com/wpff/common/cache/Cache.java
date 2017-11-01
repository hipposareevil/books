package com.wpff.common.cache;

/**
 * Interface to cache data. 
 *
 */
public interface Cache {

  /**
   * Set a cache value
   * 
   * @param namespace
   *          Namespace for value
   * @param key
   *          Key in the namespace
   * @param value
   *          Value to set
   */
  public void set(String namespace, int key, String value);

  /**
   * Get a cache value
   * 
   * @param namespace
   *          Namespace
   * @param key
   *          Key in namespace
   * @return The value or null if none exists
   */
  public String get(String namespace, int key);

  /**
   * Clear the cache for a namespace
   * 
   * @param namespace
   *          Namespace to clear
   */
  public void clear(String namespace);

  /**
   * Clear the cache for a namespace
   * 
   * @param namespace
   *          Namespace to clear
   * @param key
   *          Key in namespace
   */
  public void clear(String namespace, int key);

}
