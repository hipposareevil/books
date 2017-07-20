package com.wpff;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.db.DataSourceFactory;

import io.federecio.dropwizard.swagger.*;

import javax.ws.rs.container.DynamicFeature;

// Jedis
import com.bendb.dropwizard.redis.JedisBundle;
import com.bendb.dropwizard.redis.JedisFactory;
import redis.clients.jedis.Jedis;


// Resources
import com.wpff.db.TitleDAO;
import com.wpff.core.Title;
import com.wpff.resources.TitleResource;
import com.wpff.filter.TokenRequiredFeature;

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

    // Jedis for Redis
    bootstrap.addBundle(new JedisBundle<TitleConfiguration>() {
        @Override
        public JedisFactory getJedisFactory(TitleConfiguration configuration) {
          return configuration.getJedisFactory();
        }
      });


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
    // Set up Jedis. Currently JedisFactory doesn't inject into a filter, just Resources.
    // TODO: look at Guice.
    Jedis jedis = configuration.getJedisFactory().build(environment).getResource();

    // title rest endpoint
    final TitleDAO dao = new TitleDAO(hibernateBundle.getSessionFactory());
    environment.jersey().register(new TitleResource(dao));

    // Add a container request filter for securing webservice endpoints.
    DynamicFeature tokenRequired =new TokenRequiredFeature(jedis) ;
    environment.jersey().register(tokenRequired);

  }

}
