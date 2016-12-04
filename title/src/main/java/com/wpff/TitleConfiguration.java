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


/**
 * Configuration for book titles
 */ 
public class TitleConfiguration extends Configuration {

  @JsonProperty("swagger")
  public SwaggerBundleConfiguration swaggerBundleConfiguration;

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
