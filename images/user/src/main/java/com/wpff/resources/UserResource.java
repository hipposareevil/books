package com.wpff.resources;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

// utils
import org.apache.commons.beanutils.BeanUtils;

import com.wpff.core.PostUser;
import com.wpff.core.User;
import com.wpff.core.VisableUser;
import com.wpff.db.UserDAO;
import com.wpff.filter.TokenRequired;

import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.params.IntParam;
// Swagger
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
// Jedis
import redis.clients.jedis.Jedis;



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
   * Return a single user, by id.
   *
   * @param context security context (INJECTED via TokenFilter)
   * @param userId ID of user
   * @param authDummy Dummy authorization string that is solely used for Swagger description.
   * @return User 
   */
  @ApiOperation(
    value="Get user by ID.",
    notes="Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876. Caller must be 'admin' user."
                )
  @GET
  @Path("/{id}")
  @UnitOfWork
  @TokenRequired
  public User getUser(
    @Context SecurityContext context,
    @ApiParam(value = "ID of user to retrieve.", required = false)
    @PathParam("id") IntParam userId,
    @ApiParam(value="Bearer authorization", required=true)
    @HeaderParam(value="Authorization") String authDummy
                          ) {
    // Start
    verifyAdminUser(context);

    return findSafely(userId.get());
  }



  /**
   * Return all users in the database
   *
   * @param context security context (INJECTED via TokenFilter)
   * @param authDummy Dummy authorization string that is solely used for Swagger description.
   * @return List of VisableUsers
   */
  @ApiOperation(
    value="Get list of all users.",
    notes="Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876. Caller must be 'admin' user."
                )
  @GET
  @UnitOfWork
  @TokenRequired
  public List<VisableUser> getUsers(
    @Context SecurityContext context,
    @ApiParam(value="Bearer authorization", required=true)
    @HeaderParam(value="Authorization") String authDummy
                               ) {
    // Start
    verifyAdminUser(context);

    // Get list of all Users.
    List<User> users = userDAO.findAll();

    // Convert each User into a VisableUser bean that just contains 'id' and 'name'
    List<VisableUser> userList = users.stream().map(e -> new VisableUser(e.getName(), e.getUserGroup(), e.getId())).collect(Collectors.toList());
    
    return userList;
  }




  /**
   * Create a user in the database. This requires an authorization token to be
   * present in the headers.
   *
   * @param userBean User to create in the database
   * @param context security context (INJECTED via TokenFilter)
   * @param jedis Jedis instance used to store token data. (INJECTED)
   * @param authDummy Dummy authorization string that is solely used for Swagger description.
   * @return The newly created user
   */
  @ApiOperation(
    value = "Create new user",
    notes = "Create new user in database. Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876. Caller must be 'admin' user. "
                )
  @ApiResponse(code = 409, message = "Duplicate user")
  @POST
  @UnitOfWork
  @TokenRequired
  public User createUser(
    @ApiParam(value = "User information.", required = true) 
    PostUser userBean,
    @Context Jedis jedis,
        @Context SecurityContext context,
    @ApiParam(value="Bearer authorization", required=true)
    @HeaderParam(value="Authorization") String authDummy
                         ) {
    // START
    verifyAdminUser(context);
  
    // Check if user already exists
    // userDAO.findByName looks for exact name
    List<User> existing = userDAO.findByName(userBean.getName());
    if (! existing.isEmpty() ) {
      throw new WebApplicationException("User '" + userBean.getName() + "' already exists.", Response.Status.CONFLICT);
    }
    // No existing user, go ahead and create

    try {
      // Make a new User from incoming userBean (which is a PostBook)
      User user = new User();
      // copy(destination, source)
      BeanUtils.copyProperties(user, userBean);
      
      User newUser = this.userDAO.create(user);
      return newUser;
    }
    catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException bean) {
      throw new WebApplicationException("Error in updating database when creating user   " + userBean + ".", Response.Status.INTERNAL_SERVER_ERROR);
    }
  }  




  /**
   * Update a specified user from the database.
   * An update is only performed if one of the following is true:
   * - Username from security is 'admin'
   *
   * @param userId ID of user update delete
   * @param userBean User bean with data that is used to update the User in the database.
   * @param context security context (INJECTED via TokenFilter)
   * @param authDummy Dummy authorization string that is solely used for Swagger description.
   * @return Response denoting if the operation was successful (202) or failed (404)
   */
  @ApiOperation(value ="Update user in the database",
                notes="Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876. Caller must be 'admin' user. ")
  @PUT
  @Path("/{id}")
  @UnitOfWork
  @TokenRequired
  public Response update(
    @ApiParam(value = "ID of user to update.", required = true)     
    @PathParam("id") Integer userId,
    User userBean,
    @Context SecurityContext context,
    @ApiParam(value="Bearer authorization", required=true)
    @HeaderParam(value="Authorization") String authDummy
                         ) {
    try {
      // Start
      verifyAdminUser(context);

      // Verify user exists in database
      User userInDatabase = findSafely(userId);
      if (userInDatabase == null) {
        throw new NotFoundException("No user by id '" + userId + "'");
      }

      // Update properties in user
      try {
        // Set the encrypted password as 'userBean' has its password already encrypted
        // via the 'setPassword' method on the User class.
        // So this sets the already encrypted version, otherwise it will be
        // encrypted again and we lose the original password.
        if (userBean.getPassword() != null)
          BeanUtils.copyProperty(userInDatabase,
                                 "encryptedPassword",
                                 userBean.getPassword());
        if (userBean.getName() != null)
          BeanUtils.copyProperty(userInDatabase,
                                 "name",
                                 userBean.getName());
        
        if (userBean.getUserGroup() != null)
          BeanUtils.copyProperty(userInDatabase,
                                 "userGroup",
                                 userBean.getUserGroup());
                                 
        if (userBean.getData() != null)
          BeanUtils.copyProperty(userInDatabase,
                                 "data",
                                 userBean.getData());
      }
      catch (Exception bean) {
        throw new WebApplicationException("Error in updating database for user " + userId + ".", Response.Status.INTERNAL_SERVER_ERROR);
      }

      // Update database
      userDAO.update(userInDatabase);
    }
    catch (org.hibernate.HibernateException he) {
      he.printStackTrace();
      throw new NotFoundException("Error in database" + he.getMessage());
    }

    return Response.ok().build();
  }


  /**
   * Delete a specified user from the database.
   * A deletion is only performed if one of the following is true:
   * - Username from security is 'admin'
   *
   * @param userId ID of user to delete
   * @param context security context (INJECTED via TokenFilter)
   * @param authDummy Dummy authorization string that is solely used for Swagger description.
   * @return Response denoting if the operation was successful (202) or failed (404)
   */
  @ApiOperation(
    value ="Delete user from database",
    notes="Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876. Caller must be 'admin' user. "
                )
  @DELETE
  @Path("/{id}")
  @UnitOfWork
  @TokenRequired
  public Response delete(
    @ApiParam(value = "ID of user to delete.", required = true)     
    @PathParam("id") Integer userId,
    @Context SecurityContext context,
    @ApiParam(value="Bearer authorization", required=true)
    @HeaderParam(value="Authorization") String authDummy
                         ) {
    try {
      // Start
      verifyAdminUser(context);

      // Is OK to remove user
      userDAO.delete(findSafely(userId));
      return Response.ok().build();
    }
    catch (org.hibernate.HibernateException he) {
      throw new NotFoundException("No user by id '" + userId + "'");
    }
  }




  /****************************************************************

    Helper methods

  ****************************************************************/

  /**
   * Look for User by incoming id. If returned User is null, throw Not Found (404).
   */
  private User findSafely(int id) {
    System.out.println("find safely by id: " + id + " :");
    return this.userDAO.findById(id).orElseThrow(() -> new NotFoundException("No user by id '" + id + "'"));
  }

  /**
   * Verifies the incoming user is in group "admin"
   * 
   * Throws exception if user is not admin.
   */
  static void verifyAdminUser(SecurityContext context) throws WebApplicationException {
    if (! context.isUserInRole("admin")) {
      throw new WebApplicationException("Must be logged in as a member of the 'admin' user group.", Response.Status.UNAUTHORIZED);
    }
  }


}
