package com.wpff.common.drop.filter;


import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import com.wpff.common.auth.TokenFilter;

import redis.clients.jedis.Jedis;


/**
 * A DynamicFeature that triggers the TokenFilter filter to be applied to certain Resource methods.
 * In this case, any Resource method that has the 'TokenRequired' annotation.
 * 
 * This is hooked into the system inside UserApplication.
 */
@Provider
public class TokenRequiredFeature implements DynamicFeature {

 /**
   * Jedis (redis) entrypoint
   */
  private Jedis jedis;


  /**
   * Create new dynamic feature
   *
   * @param jedis Jedis instance used by the TokenFilter 
   */
  public TokenRequiredFeature(Jedis jedis) {
    this.jedis = jedis;
  }

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        if (resourceInfo.getResourceMethod().getAnnotation(TokenRequired.class) != null) {
          TokenFilter newFilter = new TokenFilter(this.jedis);
          context.register(newFilter);
        }
    }
}
