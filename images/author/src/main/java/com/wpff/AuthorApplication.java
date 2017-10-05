package com.wpff;

import javax.ws.rs.container.DynamicFeature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Jedis
import com.bendb.dropwizard.redis.JedisBundle;
import com.bendb.dropwizard.redis.JedisFactory;
import com.wpff.core.Author;
// Resources
import com.wpff.db.AuthorDAO;
import com.wpff.filter.TokenRequiredFeature;
import com.wpff.resources.AuthorHelper;
import com.wpff.resources.AuthorResource;

import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.UnitOfWorkAwareProxyFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import redis.clients.jedis.Jedis;

/**
 * Application for managing authors.
 *
 */
public class AuthorApplication extends Application<AuthorConfiguration> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthorApplication.class);

  /**
   * Start application
   *
   * @param args
   *          Args for application
   * @throws Exception
   *           if error in application
   */
  public static void main(final String[] args) throws Exception {
    new AuthorApplication().run(args);
  }

  // Create hibernate bundle
  private final HibernateBundle<AuthorConfiguration> hibernateBundle = new HibernateBundle<AuthorConfiguration>(
      Author.class) {
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

    // configuration for migration of databse
    bootstrap.addBundle(new MigrationsBundle<AuthorConfiguration>() {
      @Override
      public DataSourceFactory getDataSourceFactory(AuthorConfiguration configuration) {
        return configuration.getDataSourceFactory();
      }
    });

    // Jedis for Redis
    bootstrap.addBundle(new JedisBundle<AuthorConfiguration>() {
      @Override
      public JedisFactory getJedisFactory(AuthorConfiguration configuration) {
        return configuration.getJedisFactory();
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
  public void run(final AuthorConfiguration configuration, final Environment environment) {
    // Set up Jedis. Currently JedisFactory doesn't inject into a filter, just
    // Resources.
    // TODO: look at Guice.
    Jedis jedis = configuration.getJedisFactory().build(environment).getResource();

    // author rest endpoint
    final AuthorDAO authorDao = new AuthorDAO(hibernateBundle.getSessionFactory());

    // Helper for UnitOfWork
    AuthorHelper authorHelper = new UnitOfWorkAwareProxyFactory(hibernateBundle).create(
        AuthorHelper.class, 
        new Class[] { AuthorDAO.class }, 
        new Object[] { authorDao } );

    environment.jersey().register(new AuthorResource(authorHelper));

    // Add a container request filter for securing webservice endpoints.
    DynamicFeature tokenRequired = new TokenRequiredFeature(jedis);
    environment.jersey().register(tokenRequired);
  }

}
