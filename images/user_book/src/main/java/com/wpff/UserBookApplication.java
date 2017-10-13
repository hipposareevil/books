package com.wpff;

import java.util.HashMap;
import java.util.logging.Level;

import javax.persistence.PersistenceException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

// Exception mapping
import org.apache.commons.lang3.exception.ExceptionUtils;

// Jedis
import com.bendb.dropwizard.redis.JedisBundle;
import com.bendb.dropwizard.redis.JedisFactory;
import com.wpff.common.drop.filter.TokenRequiredFeature;
import com.wpff.core.DatabaseUserBook;
import com.wpff.core.Tag;
import com.wpff.core.TagMapping;
import com.wpff.core.User;
import com.wpff.db.TagDAO;
import com.wpff.db.TagMappingDAO;
import com.wpff.db.UserBookDAO;
import com.wpff.db.UserDAO;
import com.wpff.resources.UserBookHelper;
// Resources
import com.wpff.resources.UserBookResource;

import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.UnitOfWorkAwareProxyFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
// swagger
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import redis.clients.jedis.JedisPool;

/**
 * Application for managing tags
 *
 */
public class UserBookApplication extends Application<UserBookConfiguration> {

	public static void main(final String[] args) throws Exception {
		new UserBookApplication().run(args);
	}

	@Override
	public String getName() {
		return "UserBook web-service";
	}

	// Create hibernate bundle
	private final HibernateBundle<UserBookConfiguration> hibernateBundle =
			// https://stackoverflow.com/questions/29614205/org-hibernate-annotationexception-use-of-onetomany-or-manytomany-targeting-an
			new HibernateBundle<UserBookConfiguration>(DatabaseUserBook.class, Tag.class, User.class, TagMapping.class) {
				@Override
				public DataSourceFactory getDataSourceFactory(UserBookConfiguration configuration) {
					return configuration.getDataSourceFactory();
				}
			};

	/**
	 * Initialize the application
	 *
	 */
	@Override
	public void initialize(final Bootstrap<UserBookConfiguration> bootstrap) {
		// Hibernate
		bootstrap.addBundle(hibernateBundle);

		// Swagger
		bootstrap.addBundle(new SwaggerBundle<UserBookConfiguration>() {
			@Override
			protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(UserBookConfiguration configuration) {
				return configuration.swaggerBundleConfiguration;
			}
		});

		// Jedis for Redis
		bootstrap.addBundle(new JedisBundle<UserBookConfiguration>() {
			@Override
			public JedisFactory getJedisFactory(UserBookConfiguration configuration) {
				return configuration.getJedisFactory();
			}
		});
	}

	@Override
	public void run(final UserBookConfiguration configuration, final Environment environment) {
    // Set up Jedis. Currently JedisFactory doesn't inject into a filter, just Resources.
    JedisPool jedisPool = configuration.getJedisFactory().build(environment);

		java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.SEVERE);

		// UserBook DAO
		final UserBookDAO userBookDao = new UserBookDAO(hibernateBundle.getSessionFactory());
		final UserDAO userDao = new UserDAO(hibernateBundle.getSessionFactory());
		final TagDAO tagDao = new TagDAO(hibernateBundle.getSessionFactory());
		final TagMappingDAO tagMapDao = new TagMappingDAO(hibernateBundle.getSessionFactory());

		// Helper for UnitOfWork
		UserBookHelper ubHelper = new UnitOfWorkAwareProxyFactory(hibernateBundle)
		    .create(UserBookHelper.class,
		        new Class[] { UserBookDAO.class, UserDAO.class, TagDAO.class, TagMappingDAO.class },
				new   Object[] { userBookDao, userDao, tagDao, tagMapDao });

		// Register endpoints
		environment.jersey().register(new UserBookResource(ubHelper));

		// mapper for customized PersistenceException
		environment.jersey().register(new PersistenceExceptionMapper());

		// mapper for customized WebApplicationExceptions
		environment.jersey().register(new WebAppExceptionMapper());

		// Add a container request filter for securing webservice endpoints.
		DynamicFeature tokenRequired = new TokenRequiredFeature(jedisPool);
		environment.jersey().register(tokenRequired);
	}
}

/**
 * Mapper to extract web application errors into readable errors
 *
 */
class WebAppExceptionMapper implements ExceptionMapper<WebApplicationException> {
	@Override
	public Response toResponse(final WebApplicationException e) {
		final String message = e.getMessage();
		final int errorCode = e.getResponse().getStatus();

		HashMap entity = new HashMap<String, String>() {
			{
				put("error", message);
			}
		};
		if (e.getCause() != null) {
			entity.put("reason", e.getCause().getMessage());
		}

		return Response.status(errorCode)
				.type(MediaType.APPLICATION_JSON_TYPE)
				.entity(entity)
				.build();
	}
}

/**
 * Mapper to convert peristence exceptions into something more readable.
 *
 */
class PersistenceExceptionMapper implements ExceptionMapper<PersistenceException> {
	@Override
	public Response toResponse(final PersistenceException e) {
		final String rootMessage = ExceptionUtils.getRootCauseMessage(e);
		System.out.println("Got exception from database: " + rootMessage);

		if (rootMessage.contains("Duplicate entry")) {
			return Response
					.status(409)
					.type(MediaType.APPLICATION_JSON_TYPE)
					.entity(new HashMap<String, String>() {
						{
							put("error", "Entity already exists, please update your query and try again.");
						}
					})
					.build();
		}
		e.printStackTrace();

		// Create a JSON response with the provided hashmap
		return Response.status(500)
				.type(MediaType.APPLICATION_JSON_TYPE)
				.entity(new HashMap<String, String>() {
					{
						put("error", rootMessage);
					}
				}).build();
	}
}