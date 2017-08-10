package com.wpff;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.*;

import javax.validation.constraints.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

// Swagger
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

// Jedis
import com.bendb.dropwizard.redis.JedisFactory;

/**
 * Configuration for the 'user book' application.
 * The values will be injected via the user_book.cfg.yml file.
 */
public class UserBookConfiguration extends Configuration {

  /////////////////////////
  // SWAGGER
  @JsonProperty("swagger")
  public SwaggerBundleConfiguration swaggerBundleConfiguration;


  /////////////////////////
  //  REDIS
  @NotNull
  @JsonProperty
  private JedisFactory redis;
  
  public JedisFactory getJedisFactory() {
    return redis;
  }
  
  public void setJedisFactory(JedisFactory jedisFactory) {
    this.redis = jedisFactory;
  }

  /////////////////////////
  // DATABASE
  @Valid
  @NotNull
  private DataSourceFactory database = new DataSourceFactory();

  @JsonProperty("database")
  public DataSourceFactory getDataSourceFactory() {
    return database;
  }

  @JsonProperty("database")
  public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
    this.database = dataSourceFactory;
  }



}
