package com.wpff;

import com.wpff.db.TitleDAO;
import com.wpff.core.Title;
import com.wpff.resources.TitleResource;


import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.db.DataSourceFactory;

import io.federecio.dropwizard.swagger.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application to serve the title web service
 *
 */
public class TitleApplication extends Application<TitleConfiguration> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TitleApplication.class);

  /**
   * Start application
   *
   * @param args Args for application
   * @throws Exception thrown if error in application
   */
  public static void main(final String[] args) throws Exception {
    new TitleApplication().run(args);
  }

  // Create hibernate bundle
  private final HibernateBundle<TitleConfiguration> hibernateBundle =
      new HibernateBundle<TitleConfiguration>(Title.class) {
    @Override
    public DataSourceFactory getDataSourceFactory(TitleConfiguration configuration) {
      return configuration.getDataSourceFactory();
    }
  };

  @Override
    public String getName() {
    return "title";
  }

  /**
   * Initialize the application with configurations
   */
  @Override
    public void initialize(final Bootstrap<TitleConfiguration> bootstrap) {

    // Hibernate
    bootstrap.addBundle(hibernateBundle);

    // configuration
    bootstrap.addBundle(new MigrationsBundle<TitleConfiguration>() {
        @Override
        public DataSourceFactory getDataSourceFactory(TitleConfiguration configuration) {
          return configuration.getDataSourceFactory();
        }
      });    

    // Swagger
    bootstrap.addBundle(new SwaggerBundle<TitleConfiguration>() {
        @Override
        protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(TitleConfiguration configuration) {
          return configuration.swaggerBundleConfiguration;
        }
      });
  }

  /**
   * Start the jersey endpoint for /title
   */
  @Override
    public void run(final TitleConfiguration configuration,
                    final Environment environment) {
    // title rest endpoint
    final TitleDAO dao = new TitleDAO(hibernateBundle.getSessionFactory());
    environment.jersey().register(new TitleResource(dao));
  }

}
