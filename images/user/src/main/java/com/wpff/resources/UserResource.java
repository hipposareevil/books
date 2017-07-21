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
import javax.ws.rs.HeaderParam;
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
 * Resource at /user that manages users.
 *
 */
@Api("/user")
@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

  // Static Bearer text
  private static String BEARER = "Bearer";

  /**
   * DAO used to get a User and the associated password
   * that is then used for authorization
   */
  private final UserDAO userDAO;

  /**
   * Create new UserResource with a user DAO for validating username/password
   *
   * @param userDAO DAO used find a user for authentication
   */
  public UserResource(UserDAO userDAO) {
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
  @TokenRequired
  public List<String> getUsers(
    @Context SecurityContext context,
    @ApiParam(value="Bearer authorization", required=true)
    @HeaderParam(value="Authorization") String authDummy
                               ) {
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
  @Path("/{name}")
  @UnitOfWork
  @TokenRequired
  public Response delete(
    @ApiParam(value = "Name of user to delete.", required = true)     
    @PathParam("name") String userName,
    @Context SecurityContext context,
    @ApiParam(value="Bearer authorization", required=true)
    @HeaderParam(value="Authorization") String authDummy
                         ) {
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
  @ApiOperation(value ="Update user in the database",
                notes="Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876.")
  @PUT
  @Path("/{name}")
  @UnitOfWork
  @TokenRequired
  public Response update(
    @PathParam("name") String userName,
    User user,
    @Context SecurityContext context,
    @ApiParam(value="Bearer authorization", required=true)
    @HeaderParam(value="Authorization") String authDummy
                         ) {
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
  public User createUser(
    @ApiParam(value = "User information.", required = true) 
    User user,
    @Context Jedis jedis,
    @ApiParam(value="Bearer authorization", required=true)
    @HeaderParam(value="Authorization") String authDummy
                         ) {
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
