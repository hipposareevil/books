package com.wpff;

import javax.ws.rs.container.DynamicFeature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Jedis
import com.bendb.dropwizard.redis.JedisBundle;
import com.bendb.dropwizard.redis.JedisFactory;
import com.wpff.core.Book;
// Resources
import com.wpff.db.BookDAO;
import com.wpff.filter.TokenRequiredFeature;
import com.wpff.resources.BookResource;

import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import redis.clients.jedis.Jedis;

/**
 * Application to serve the book web service
 *
 */
public class BookApplication extends Application<BookConfiguration> {

  private static final Logger LOGGER = LoggerFactory.getLogger(BookApplication.class);

  /**
   * Start application
   *
   * @param args Args for application
   * @throws Exception thrown if error in application
   */
  public static void main(final String[] args) throws Exception {
    new BookApplication().run(args);
  }

  // Create hibernate bundle
  private final HibernateBundle<BookConfiguration> hibernateBundle =
      new HibernateBundle<BookConfiguration>(Book.class) {
    @Override
    public DataSourceFactory getDataSourceFactory(BookConfiguration configuration) {
      return configuration.getDataSourceFactory();
    }
  };

  @Override
    public String getName() {
    return "book";
  }

  /**
   * Initialize the application with configurations
   */
  @Override
    public void initialize(final Bootstrap<BookConfiguration> bootstrap) {

    // Hibernate
    bootstrap.addBundle(hibernateBundle);

    // Jedis for Redis
    bootstrap.addBundle(new JedisBundle<BookConfiguration>() {
        @Override
        public JedisFactory getJedisFactory(BookConfiguration configuration) {
          return configuration.getJedisFactory();
        }
      });


    // configuration
    bootstrap.addBundle(new MigrationsBundle<BookConfiguration>() {
        @Override
        public DataSourceFactory getDataSourceFactory(BookConfiguration configuration) {
          return configuration.getDataSourceFactory();
        }
      });    

    // Swagger
    bootstrap.addBundle(new SwaggerBundle<BookConfiguration>() {
        @Override
        protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(BookConfiguration configuration) {
          return configuration.swaggerBundleConfiguration;
        }
      });
  }

  /**
   * Start the jersey endpoint for /book
   */
  @Override
    public void run(final BookConfiguration configuration,
                    final Environment environment) {
    // Set up Jedis. Currently JedisFactory doesn't inject into a filter, just Resources.
    // TODO: look at Guice.
    Jedis jedis = configuration.getJedisFactory().build(environment).getResource();

    // book rest endpoint
    final BookDAO dao = new BookDAO(hibernateBundle.getSessionFactory());
    environment.jersey().register(new BookResource(dao));

    // Add a container request filter for securing webservice endpoints.
    DynamicFeature tokenRequired = new TokenRequiredFeature(jedis) ;
    environment.jersey().register(tokenRequired);

  }

}
