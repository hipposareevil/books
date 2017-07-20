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
 * Resource at /auth that authenticates and de-authenticates a user.
 *
 * Successful authentication will create and store a token associated with
 * the user, which is then used later via the TokenFilter to authenticate
 * Resource calls (like GET /user/bob)
 */
@Api("/auth")
@Path("/auth")
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
   * Return all usernames in the database
   *
   * @param context security context (INJECTED via TokenFilter)
   * @return List of user names as Strings
   */
  @ApiOperation(
    value="Get list of all user names.",
    notes="Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876."
                )
  @GET
  @UnitOfWork
  @Path("/user")
  @TokenRequired
  public List<String> getUsers(@Context SecurityContext context) {
    String userNameFromSecurity = context.getUserPrincipal().getName();
    if (! userNameFromSecurity.equals("admin")) {
      throw new WebApplicationException("Must be logged in as 'admin'", Response.Status.UNAUTHORIZED);
    }

    List<User> users = userDAO.findAll();
    List<String> userNames = users.stream().map((u) -> u.getName()).collect(Collectors.toList());

    return userNames;
  }


  /**
   * Delete a specified user from the database.
   * A deletion is only performed if one of the following is true:
   * - Username from security (token) is the same as the user being deleted
   * - Username from security is 'admin'
   *
   * @param userName Name of user to delete
   * @param context security context (INJECTED via TokenFilter)
   * @return Response denoting if the operation was successful (202) or failed (404)
   */
  @ApiOperation(
    value ="Delete user from database",
    notes="Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876."
                )
  @DELETE
  @Path("/user/{name}")
  @UnitOfWork
  @TokenRequired
  public Response delete(
    @ApiParam(value = "Name of user to delete.", required = true)     
    @PathParam("name") String userName,
    @Context SecurityContext context) {
    try {
      // Verify the userName is equal to the context's name
      // or context's name is admin.
      String userNameFromSecurity = context.getUserPrincipal().getName();
      if (userNameFromSecurity.equals(userName) ||
          userNameFromSecurity.equals("admin")) {
        // Is OK to remove user
        userDAO.delete(findSafely(userName));
      }
      else {
        throw new WebApplicationException("Must be logged in as " + userName + " or 'admin'", Response.Status.UNAUTHORIZED);
      }
    }
    catch (org.hibernate.HibernateException he) {
      throw new NotFoundException("No user by name '" + userName + "'");
    }
    return Response.ok().build();
  }


  /**
   * Update a specified user from the database.
   * An update is only performed if one of the following is true:
   * - Username from security (token) is the same as the user being updated
   * - Username from security is 'admin'
   *
   * @param userName Name of user to update
   * @param user User bean with data that is used to update the User in the database.
   * @param context security context (INJECTED via TokenFilter)
   * @return Response denoting if the operation was successful (202) or failed (404)
   */
  @ApiOperation(
    value ="Update user in the database",
    notes="Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876."
)
  @PUT
  @Path("/user/{name}")
  @UnitOfWork
  @TokenRequired
  public Response update(@PathParam("name") String userName,
                         User user,
                         @Context SecurityContext context) {
    try {
      // Verify the userName is equal to the context's name
      // or context's name is admin.
      String userNameFromSecurity = context.getUserPrincipal().getName();
      if (userNameFromSecurity.equals(userName) ||
          userNameFromSecurity.equals("admin")) {

        System.out.println("UPDATE user: " + userName);
        System.out.println("incoming user: " + user);

        // Verify user exists in database
        User userInDatabase = findSafely(userName);
        if (userInDatabase == null) {
          throw new NotFoundException("No user by name '" + userName + "'");
        }

        // Update properties in database
        try {
        BeanUtils.copyProperty(userInDatabase,
                               "password",
                               user.getPassword());
        BeanUtils.copyProperty(userInDatabase,
                               "data",
                               user.getData());
        }
        catch (Exception bean) {
          throw new WebApplicationException("Error in updating database for user.", Response.Status.INTERNAL_SERVER_ERROR);
        }

        // Is OK to update user
        userDAO.update(userInDatabase);
      }
      else {
        throw new WebApplicationException("Must be logged in as " + userName + " or 'admin'", Response.Status.UNAUTHORIZED);
      }
    }
    catch (org.hibernate.HibernateException he) {
      he.printStackTrace();
      throw new NotFoundException("Error in database" + he.getMessage());
    }
    return Response.ok().build();
  }



  /**
   * Create a user in the database. This requires an authorization token to be
   * present in the headers.
   *
   * @param user User to create in the database
   * @param jedis Jedis instance used to store token data. (INJECTED)
   * @return The newly created user
   */
  @ApiOperation(
    value = "Create new user",
    notes = "Create new user in database. Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876.",
    response = User.class
                )
  @ApiResponse(code = 409, message = "Duplicate user")
  @POST
  @UnitOfWork
  @TokenRequired
  @Path("/user")
  public User createUser(
    @ApiParam(value = "User information.", required = true) 
    User user,
    @Context Jedis jedis) {
    // Check if user already exists
    try {
      User tempUser = findSafely(user.getName());
      if (tempUser != null) {
        throw new WebApplicationException("User '" + user.getName() + "' already exists.", Response.Status.CONFLICT);
      }
    }
    catch(NotFoundException notFound) {
    }

    // No existing user, go ahead and create
    return this.userDAO.create(user);
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
