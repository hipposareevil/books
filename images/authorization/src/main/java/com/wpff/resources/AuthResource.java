package com.wpff.resources;

// books
import com.wpff.core.Credentials;
import com.wpff.core.User;
import com.wpff.db.UserDAO;
import com.wpff.filter.TokenRequired;


// utils
import org.apache.commons.beanutils.BeanUtils;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Collectors;
import java.util.UUID;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import io.dropwizard.hibernate.UnitOfWork;

// Jedis
import redis.clients.jedis.Jedis;

// password encryption
import org.jasypt.util.password.BasicPasswordEncryptor;

// Swagger
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ResponseHeader;



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
   * @return Response with a new token or an UNAUTHORIZED error
   */
  @ApiOperation(
    value="Creates authentication token which is then used for various endpoints.",
    notes="Token is created for the user being authenticated. Token is of form 'Bearer qwerty-1234-asdf-9876'. Where required, it should be put in the HTTP Headers with key 'AUTHORIZATION'."
                )
  @POST
  @Produces("application/json")
  @Consumes("application/json")
  @UnitOfWork
  @Path("/token")
  public Response authenticate(
    @ApiParam(value = "Credentials for creating authentication token", required = true) 
    Credentials creds,
    @Context Jedis jedis) {

    // Get user/password from incoming JSON/bean
    String userToAuthenticate = creds.getName();
    String userPassword = creds.getPassword();

    System.out.println("user: " + userToAuthenticate);
    System.out.println("pw: " + userPassword);

    // See if there is a matching user in the databse
    User userInDatabase = findSafely(userToAuthenticate);
    System.out.println("user: " + userInDatabase);

    // User exists. Take incoming password and compare against
    // the encrypted one
    BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
    if (passwordEncryptor.checkPassword(userPassword, userInDatabase.getPassword())) {
      // password is OK. Create and return a token
      String token = UUID.randomUUID().toString();
      String fullToken = BEARER + " " + token;

      System.out.println("full token: " + fullToken);

      // Create token in redis that will last 24 hours
      // expire token in 24 hours
      jedis.set(token, userInDatabase.getName());
      jedis.expire(token, 60 * 60 * 24);

      return Response.ok(fullToken).build();
    }
    else {
      // bad password
      return Response.status(Response.Status.UNAUTHORIZED).build();
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
