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
import com.wpff.resources.TagResource;
import com.wpff.core.Tag;
import com.wpff.db.TagDAO;
import com.wpff.filter.TokenRequiredFeature;


/**
 * Application for managing tags
 *
 */
public class TagApplication extends Application<TagConfiguration> {

  public static void main(final String[] args) throws Exception {
    new TagApplication().run(args);
  }

  @Override
  public String getName() {
    return "Tag web-service";
  }


  // Create hibernate bundle
  private final HibernateBundle<TagConfiguration> hibernateBundle =
      new HibernateBundle<TagConfiguration>(Tag.class) {
        @Override
        public DataSourceFactory getDataSourceFactory(TagConfiguration configuration) {
          return configuration.getDataSourceFactory();
        }
      };


  /**
   * Initialize the application
   *
   */
  @Override
  public void initialize(final Bootstrap<TagConfiguration> bootstrap) {
    // Hibernate
    bootstrap.addBundle(hibernateBundle);

    // Swagger
    bootstrap.addBundle(new SwaggerBundle<TagConfiguration>() {
        @Override
        protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(TagConfiguration configuration) {
          return configuration.swaggerBundleConfiguration;
        }
      });


    // Jedis for Redis
    bootstrap.addBundle(new JedisBundle<TagConfiguration>() {
        @Override
        public JedisFactory getJedisFactory(TagConfiguration configuration) {
          return configuration.getJedisFactory();
        }
      });
  }

  @Override
  public void run(final TagConfiguration configuration,
                  final Environment environment) {
    // Set up Jedis. Currently JedisFactory doesn't inject into a filter, just Resources.
    // TODO: look at Guice.
    Jedis jedis = configuration.getJedisFactory().build(environment).getResource();

    // Tag DAO 
    final TagDAO tagDao = new TagDAO(hibernateBundle.getSessionFactory());

    // Register endpoints
    environment.jersey().register(new TagResource(tagDao));

    // Add a container request filter for securing webservice endpoints.
    DynamicFeature tokenRequired =new TokenRequiredFeature(jedis) ;
    environment.jersey().register(tokenRequired);
  }


}
