package com.wpff;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import org.hibernate.validator.constraints.NotEmpty;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Map;

// Jedis
import com.bendb.dropwizard.redis.JedisFactory;



/**
 * Configuration for the 'book' application.
 * The values will be injected via the book.cfg.yml file.
 */ 
public class BookConfiguration extends Configuration {

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
