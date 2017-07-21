package com.wpff;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

// swagger
import io.federecio.dropwizard.swagger.*;

// hibernate
import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.UnitOfWorkAwareProxyFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;


// Jedis
import com.bendb.dropwizard.redis.JedisBundle;
import com.bendb.dropwizard.redis.JedisFactory;
import redis.clients.jedis.Jedis;

import javax.ws.rs.container.DynamicFeature;

// Resources
import com.wpff.resources.UserResource;
import com.wpff.core.User;
import com.wpff.db.UserDAO;
import com.wpff.filter.TokenRequiredFeature;


/**
 * Application for managing users
 *
 */
public class UserApplication extends Application<UserConfiguration> {

  public static void main(final String[] args) throws Exception {
    new UserApplication().run(args);
  }

  @Override
  public String getName() {
    return "User web-service";
  }


  // Create hibernate bundle
  private final HibernateBundle<UserConfiguration> hibernateBundle =
      new HibernateBundle<UserConfiguration>(User.class) {
        @Override
        public DataSourceFactory getDataSourceFactory(UserConfiguration configuration) {
          return configuration.getDataSourceFactory();
        }
      };


  /**
   * Initialize the application
   *
   */
  @Override
  public void initialize(final Bootstrap<UserConfiguration> bootstrap) {
    // Hibernate
    bootstrap.addBundle(hibernateBundle);

    // Swagger
    bootstrap.addBundle(new SwaggerBundle<UserConfiguration>() {
        @Override
        protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(UserConfiguration configuration) {
          return configuration.swaggerBundleConfiguration;
        }
      });


    // Jedis for Redis
    bootstrap.addBundle(new JedisBundle<UserConfiguration>() {
        @Override
        public JedisFactory getJedisFactory(UserConfiguration configuration) {
          return configuration.getJedisFactory();
        }
      });
  }

  @Override
  public void run(final UserConfiguration configuration,
                  final Environment environment) {
    // Set up Jedis. Currently JedisFactory doesn't inject into a filter, just Resources.
    // TODO: look at Guice.
    Jedis jedis = configuration.getJedisFactory().build(environment).getResource();

    // User DAO 
    final UserDAO userDao = new UserDAO(hibernateBundle.getSessionFactory());

    // Register endpoints
    environment.jersey().register(new UserResource(userDao));

    // Add a container request filter for securing webservice endpoints.
    DynamicFeature tokenRequired =new TokenRequiredFeature(jedis) ;
    environment.jersey().register(tokenRequired);
  }


}
