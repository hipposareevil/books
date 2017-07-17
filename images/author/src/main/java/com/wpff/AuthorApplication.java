package com.wpff;

import com.wpff.db.AuthorDAO;
import com.wpff.core.Author;
import com.wpff.resources.AuthorResource;


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

public class AuthorApplication extends Application<AuthorConfiguration> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthorApplication.class);

  /**
   * Start application
   *
   * @param args Args for application
   * @throws Exception if error in application
   */
  public static void main(final String[] args) throws Exception {
    new AuthorApplication().run(args);
  }

  // Create hibernate bundle
  private final HibernateBundle<AuthorConfiguration> hibernateBundle =
      new HibernateBundle<AuthorConfiguration>(Author.class) {
    @Override
    public DataSourceFactory getDataSourceFactory(AuthorConfiguration configuration) {
      return configuration.getDataSourceFactory();
    }
  };

  @Override
    public String getName() {
    return "author";
  }

  /**
   * Initialize the application with configurations
   */
  @Override
    public void initialize(final Bootstrap<AuthorConfiguration> bootstrap) {

    // Hibernate
    bootstrap.addBundle(hibernateBundle);

    // configuration
    bootstrap.addBundle(new MigrationsBundle<AuthorConfiguration>() {
        @Override
        public DataSourceFactory getDataSourceFactory(AuthorConfiguration configuration) {
          return configuration.getDataSourceFactory();
        }
      });    

    // Swagger
    bootstrap.addBundle(new SwaggerBundle<AuthorConfiguration>() {
        @Override
        protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(AuthorConfiguration configuration) {
          return configuration.swaggerBundleConfiguration;
        }
      });
  }

  /**
   * Start the jersey endpoint for /author
   */
  @Override
    public void run(final AuthorConfiguration configuration,
                    final Environment environment) {
    // author rest endpoint
    final AuthorDAO dao = new AuthorDAO(hibernateBundle.getSessionFactory());
    environment.jersey().register(new AuthorResource(dao));
  }

}
