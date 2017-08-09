package com.wpff.resources;

// books
import com.wpff.core.Credentials;
import com.wpff.core.Tag;
import com.wpff.core.User;
import com.wpff.core.UserBook;
import com.wpff.db.TagDAO;
import com.wpff.db.UserDAO;
import com.wpff.db.UserBookDAO;
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
import io.dropwizard.jersey.params.IntParam;

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
 * Resource at /user_book that manages user's books
 */
@Api("/user_book")
@Path("/user_book")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserBookResource {

  // Static Bearer text
  private static String BEARER = "Bearer";

  /**
   * DAO used to get a UserBook. 
   */
  private final UserBookDAO userBookDAO;

  /**
   * DAO for tags
   */
  private final TagDAO tagDAO;

  /**
   * DAO for users
   */
  private final UserDAO userDAO;

  /**
   * Create new UserBookResource with a tag DAO for validating tagname/password
   *
   * @param userDAO DAO used for dealing with user
   * @param userBookDAO used for dealing with user_books 
   * @param tagDAO DAO used find a tag for authentication
   */
  public UserBookResource(UserDAO userDAO,
                          TagDAO tagDAO,
                          UserBookDAO userBookDAO) {
    this.tagDAO = tagDAO;
    this.userBookDAO = userBookDAO;
    this.userDAO = userDAO;
  }

  /**
   * Return single userBook from database
   *
   * @param context security context (INJECTED via TokenFilter)
   * @param userId ID of user
   * @param userBookId ID of user_book
   * @param authDummy Dummy authorization string that is solely used for Swagger description.
   * @return UserBook
   */
  @ApiOperation(
    value="Get single userBook.",
    notes="Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876."
                )
  @GET
  @Path("/{user_id}/{user_book_id}")
  @UnitOfWork
  @TokenRequired
  public UserBook getUserBook (
    @Context SecurityContext context,
    @ApiParam(value = "ID of user.", required = false)
    @PathParam("user_id") IntParam userId,

    @ApiParam(value = "ID of userBook.", required = false)
    @PathParam("user_book_id") IntParam userBookId,

    @ApiParam(value="Bearer authorization", required=true)
    @HeaderParam(value="Authorization") String authDummy
                               ) {
    // Start

    // Get the username corresponding to the incoming userId and verify that is the same as the authenticated caller.
    String userNameFromSecurity = context.getUserPrincipal().getName();
    User userFromId = userDAO.findById(userId.get()).orElseThrow(() -> new NotFoundException("No user with ID '" + userId.get() + "' found."));

    // Check names, if:
    // userNameFromSecurity == admin or
    // userNameFromSecurity == name from id
    // we can proceed
    if ( (userFromId.getName().equals("admin")) ||
         (userFromId.getName().equals(userNameFromSecurity)) ) {
      UserBook userBook = findSafely(userBookId.get());
      return userBook;
    }
    else {
      throw new WebApplicationException("Must be logged in as user with id '" + userId + "' to view this.",
                                        Response.Status.UNAUTHORIZED);
    }
  }

  /**
   * Return all userBooks for a given user
   *
   * @param context security context (INJECTED via TokenFilter)
   * @param userId ID of user
   * @param authDummy Dummy authorization string that is solely used for Swagger description.
   * @return UserBook
   */
  @ApiOperation(
    value="Get userBooks for user.",
    notes="Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876."
                )
  @GET
  @Path("/{user_id}")
  @UnitOfWork
  @TokenRequired
  public List<UserBook> getUserBook (
    @Context SecurityContext context,
    @ApiParam(value = "ID of user.", required = false)
    @PathParam("user_id") IntParam userId,

    @ApiParam(value="Bearer authorization", required=true)
    @HeaderParam(value="Authorization") String authDummy
                               ) {
    // Start

    // Get the username corresponding to the incoming userId and verify that is the same as the authenticated caller.
    String userNameFromSecurity = context.getUserPrincipal().getName();
    User userFromId = userDAO.findById(userId.get()).orElseThrow(() -> new NotFoundException("No user with ID '" + userId.get() + "' found."));

    // Check names, if:
    // userNameFromSecurity == admin or
    // userNameFromSecurity == name from id
    // we can proceed
    if ( (userFromId.getName().equals("admin")) ||
         (userFromId.getName().equals(userNameFromSecurity)) ) {

      List<UserBook> userBooks = userBookDAO.findBooksByUserId(userId.get());
      return userBooks;
    }
    else {
      throw new WebApplicationException("Must be logged in as user with id '" + userId + "' to view this.",
                                        Response.Status.UNAUTHORIZED);
    }
  }



  /**
   * Create a userbook in the database. This requires an authorization token to be
   * present in the headers.
   *
   * @param userBookBean UserBook to create in the database
   * @param userId ID of user
   * @param jedis Jedis instance used to store token data. (INJECTED)
   * @param authDummy Dummy authorization string that is solely used for Swagger description.
   * @return The newly created user
   */
  @ApiOperation(
    value = "Create new userbook",
    notes = "Create new userbook in database. Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876.",
    response = UserBook.class
                )
  @ApiResponse(code = 409, message = "Duplicate userbook")
  @POST
  @UnitOfWork
  @TokenRequired
  @Path("/{user_id}")
  public UserBook createUserBook(
    @ApiParam(value = "ID of user.", required = false)
    @PathParam("user_id") IntParam userId,

    @ApiParam(value = "User Book information.", required = true) 
    UserBook userBookBean,

    @Context Jedis jedis,
    @ApiParam(value="Bearer authorization", required=true)
    @HeaderParam(value="Authorization") String authDummy
                         ) {
    // Verify the userID matches the username from context

/*
    // userDAO.findByName looks for exact
    List<User> existing = userDAO.findByName(userBean.getName());
    if (! existing.isEmpty() ) {
      throw new WebApplicationException("User '" + userBean.getName() + "' already exists.", Response.Status.CONFLICT);
    }
*/

    // No existing user, go ahead and create
    return this.userBookDAO.create(userBookBean);
  }  





  /****************************************************************

    Helper methods

  ****************************************************************/


  /**
   * Look for UserBook by incoming id. If returned UserBook is null, throw Not Found (404).
   */
  private UserBook findSafely(int id) {
    return this.userBookDAO.findById(id).orElseThrow(() -> new NotFoundException("No tag by id '" + id + "'"));
  }


}
