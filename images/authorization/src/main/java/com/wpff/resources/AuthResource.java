package com.wpff.resources;

import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// password encryption
import org.jasypt.util.password.BasicPasswordEncryptor;

import com.wpff.core.Bearer;
// books
import com.wpff.core.Credentials;
import com.wpff.core.User;
import com.wpff.db.UserDAO;

import io.dropwizard.hibernate.UnitOfWork;
// Swagger
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
// Jedis
import redis.clients.jedis.Jedis;



/**
 * Resource at /authorize that authenticates/authorizes and de-authenticates a user.
 *
 * Successful authentication will create and store a token associated with
 * the user, which is then used later via the TokenFilter to authenticate
 * Resource calls (like GET /user/bob)
 */
@Api("/authorize")
@Path("/authorize")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

  // Static Bearer text
  private static String BEARER = "Bearer";

  /**
   * DAO used to get a User and the associated password
   * that is then used for authorization
   */
  private final UserDAO userDAO;

  /**
   * Create new AuthResource with a user DAO for validating username/password
   *
   * @param userDAO DAO used find a user for authentication
   */
  public AuthResource(UserDAO userDAO) {
    this.userDAO = userDAO;
  }



  /**
   * Creates an authorization token for an incoming user.
   * If there is no matching user in the database an error is thrown.
   *
   * The resulting token is to be put in the HTTP headers. e.g.
   * Authorization : Bearer qwerty-1234-asdf-9876
   * 
   * @param creds A credentials bean with name and password.
   * @param jedis Jedis instance used to store token data. (INJECTED)
   * @return Bearer with authentication token and ID.
   */
  @ApiOperation(
    value="Creates authentication token which is then used for various endpoints.",	// 
    notes="Token is created for the user being authenticated. Token is of form 'Bearer qwerty-1234-asdf-9876'. Where required, it should be put in the HTTP Headers with key 'AUTHORIZATION'."
                )
  @POST
  @UnitOfWork
  @Path("/token")
  public Bearer authenticate(
    @ApiParam(value = "Credentials for creating authentication token", required = true) 
    Credentials creds,
    @Context Jedis jedis) {

    // Get user/password from incoming JSON/bean
    String userToAuthenticate = creds.getName();
    String userPassword = creds.getPassword();

    // See if there is a matching user in the database
    User userInDatabase = findSafely(userToAuthenticate);

    // User exists. Take incoming password and compare against
    // the encrypted one
    BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
    if (passwordEncryptor.checkPassword(userPassword, userInDatabase.getPassword())) {
      // password is OK. Create and return a token
      String token = UUID.randomUUID().toString();
      String fullToken = BEARER + " " + token;

      ///////////
      // Create token in redis that will last 24 hours
           
      // hset user:token name "user name"
      // hset user:token group "user group"
      String redisHashName = "user:" + token;
      jedis.hset(redisHashName, "name", userInDatabase.getName());
      jedis.hset(redisHashName, "group", userInDatabase.getUserGroup());
      jedis.expire(redisHashName, 60 * 60 * 24);
      
      // Create Bearer bean to return to user
      Bearer tokenToReturn = new Bearer();
      tokenToReturn.setToken(fullToken);
      tokenToReturn.setUserId(userInDatabase.getId());
      
      return tokenToReturn;
    }
    else {
      // bad password
      System.out.println("AuthResource.authenticate: invalid password for '" + userToAuthenticate + "'");
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
  }


  /****************************************************************

    Helper methods

  ****************************************************************/

  /**
   * Look for User by incoming name. If returned User is null, throw Not Found (404).
   */
  private User findSafely(String name) {
    return this.userDAO.findByName(name).orElseThrow(() -> new NotFoundException("No user by name '" + name + "'"));
  }

}
