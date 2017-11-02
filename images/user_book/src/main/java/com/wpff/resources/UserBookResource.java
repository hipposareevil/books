package com.wpff.resources;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import com.codahale.metrics.annotation.Timed;
import com.wpff.common.auth.TokenRequired;
import com.wpff.common.result.ResultWrapper;
import com.wpff.common.result.ResultWrapperUtil;
import com.wpff.common.result.Segment;
import com.wpff.core.DatabaseUserBook;
import com.wpff.core.Tag;
import com.wpff.core.TagMapping;
import com.wpff.core.beans.FullUserBook;
import com.wpff.core.beans.PostUserBook;

import io.dropwizard.jersey.params.IntParam;
// Swagger
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Resource at /user_book that manages user's books
 */
@Api("/user_book")
@Path("/user_book")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserBookResource {

  /**
   * Helper for DB queries
   */
  UserBookHelper ubHelper;

  /**
   * Create new UserBookResource with a tag DAO for validating tagname/password
   *
   * @param ubHelper
   *          DAO helper
   */
  public UserBookResource(UserBookHelper ubHelper) {

    this.ubHelper = ubHelper;
  }

  /**
   * Delete a single UserBook by its id
   * 
   * @param context
   *          security context (INJECTED via TokenFilter)
   * @param userId
   *          ID of user
   * @param userBookId
   *          ID of user_book
   * @param authDummy
   *          Dummy authorization string that is solely used for Swagger
   *          description
   * @return Response denoting if the operation was successful (202) or failed
   *         (404)
   */
  @ApiOperation(value = "Delete a UserBook.",
      notes = "Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876.")
  @DELETE
  @Path("/{user_id}/{user_book_id}")
  @TokenRequired
  @Timed(absolute = true, name = "delete")
  public Response deleteUserBook(@Context SecurityContext context,

      @ApiParam(value = "ID of user.", required = false) @PathParam("user_id") IntParam userId,

      @ApiParam(value = "ID of userBook.", required = false) @PathParam("user_book_id") IntParam userBookId,

      @ApiParam(value = "Bearer authorization", required = true) @HeaderParam(
          value = "Authorization") String authDummy) {
    // Start

    // Verify the username matches the userid or is 'admin'
    ubHelper.verifyUserIdHasAccess(context, userId.get());

    ubHelper.deleteUserBookById(userId.get(), userBookId.get());

    return Response.ok().build();
  }

  /**
   * Return single userBook from database
   *
   * @param context
   *          security context (INJECTED via TokenFilter)
   * @param userId
   *          ID of user
   * @param userBookId
   *          ID of user_book
   * @param authString
   *          Authorization string
   * @return GetUserBook
   * 
   */
  @ApiOperation(value = "Get single UserBook.",
      notes = "Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876.")
  @GET
  @Path("/{user_id}/{user_book_id}")
  @com.wpff.common.auth.TokenRequired
  @Timed(absolute = true, name = "get")
  public FullUserBook getUserBook(@Context SecurityContext context,

      @ApiParam(value = "ID of user.", required = false) @PathParam("user_id") IntParam userId,

      @ApiParam(value = "ID of userBook.", required = false) @PathParam("user_book_id") IntParam userBookId,

      @ApiParam(value = "Bearer authorization", required = true) @HeaderParam(
          value = "Authorization") String authString) {
    try {
      // Start

      // Verify the username matches the userid or is 'admin'
      ubHelper.verifyUserIdHasAccess(context, userId.get());

      return ubHelper.getUserBookByUserBookId(authString, userId.get(), userBookId.get());
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException error) {
      throw new WebApplicationException(
          "Error in getting UserBook for user: " + userId.get() + ", user book id: " + userBookId.get(),
          error,
          Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Return all userBooks for a given user
   *
   * @param context
   *          security context (INJECTED via TokenFilter)
   * @param userId
   *          ID of user
   * @param tagQuery
   *          [optional] List of tags of books.
   * @param bookId
   *          [optional] ID of book to find
   * @param bookTitle
   *          [optional] Title of books for the user
   * @param offset
   *          [optional] Start index of data segment
   * @param limit
   *          [optional] Size of data segment
   * @param authString
   *          Authorization string
   * @return List of GetUserBook
   * 
   */
  @ApiOperation(value = "Get list of UserBooks for user.",
      notes = "Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876.")
  @GET
  @Path("/{user_id}")
  @TokenRequired
  @Timed(absolute = true, name = "getAll")
  public ResultWrapper<FullUserBook> getUserBooks(@Context SecurityContext context, @ApiParam(
      value = "ID of user.", required = false) @PathParam("user_id") IntParam userId,

      @ApiParam(
          value = "List of tags of books to retrieve. Only user books that have these tags will be returned",
          required = false) @QueryParam("tag") List<String> tagQuery,

      @ApiParam(
          value = "ID of book to find in list of user books. Only the user book that contains this book id will be returned",
          required = false) @QueryParam("book_id") Integer bookId,

      @ApiParam(
          value = "Title of books within the user's books. Only the books with that matching title will be returned",
          required = false) @QueryParam("book_title") String bookTitle,

      @ApiParam(value = "Where to start the returned data segment from the full result.",
          required = false) @QueryParam("offset") Integer offset,

      @ApiParam(value = "size of the returned data segment.",
          required = false) @QueryParam("limit") Integer limit,

      @ApiParam(value = "Bearer authorization", required = true) @HeaderParam(
          value = "Authorization") String authString) {
    // Start
    try {
      // Verify the username matches the userid or is 'admin'
      ubHelper.verifyUserIdHasAccess(context, userId.get());

      // If there are any queries, we get all books and then filter down
      // With no queries, we just get books according to offset & limit
      if ((!tagQuery.isEmpty()) || (bookId != null) || (bookTitle != null)) {
        /////////////////////////
        // A query is being used
                
        // The set of books to return to user
        // This is an intersection of the 3 following sets
        Set<FullUserBook> bookSet = new TreeSet<FullUserBook>();
        
        Set<FullUserBook> tagBookSet = new TreeSet<FullUserBook>();
        Set<FullUserBook> titleBookSet = new TreeSet<FullUserBook>();
        Set<FullUserBook> idBookSet = new TreeSet<FullUserBook>();

        // Query by TAG
        if (!tagQuery.isEmpty()) {
          // Get tagIDs for all matching tags in the database
          Map<String, Tag> tagsInDbMap = ubHelper.getAllTags();

          // Iterate over tagsInDbMap values and for each tag that
          // has the matching name, add it to 'matchingTags'
          List<Tag> matchingTags = tagsInDbMap.values()
              .stream()
              .filter(t -> tagQuery.contains(t.getName()))
              .collect(Collectors.toList());
          
          // For each matching tag, get a list of user_books
          for (Tag t : matchingTags) {
            int tagId = t.getId();
            List<FullUserBook> booksForTag = ubHelper.getUserBooksByUserAndTag(authString, userId.get(), tagId);
            tagBookSet.addAll(booksForTag);
          }
          
          bookSet = tagBookSet;          
        }
        
        // Query by TITLE
        if (bookTitle != null) {
           List<FullUserBook> booksForTitle= ubHelper.getUserBooksByTitle(authString, userId.get(), bookTitle);
           titleBookSet.addAll(booksForTitle);
           
           bookSet = titleBookSet;
           if (!tagQuery.isEmpty()) {
             bookSet.retainAll(tagBookSet);
           }
        }
        
        // Query by BookId
        if (bookId != null) {
          FullUserBook book = ubHelper.getUserBookByUserBookId(authString, userId.get(), bookId);
          if (book != null) {
            idBookSet.add(book);
          }
          
          bookSet = idBookSet;
          if (!tagQuery.isEmpty()) {
            bookSet.retainAll(tagBookSet);
          }
          if (bookTitle != null) {
            bookSet.retainAll(titleBookSet);
          }
        }

        // Wrap the results with the desired offset and limit
        List<FullUserBook> bookList = new ArrayList<FullUserBook>(bookSet);
        ResultWrapper<FullUserBook> result = ResultWrapperUtil.createWrapper(bookList, offset, limit);
        return result;
      } else {
        /////////////////////////
        // There are no queries, so it is a straight call
        Segment segment = new Segment(offset, limit);

        System.out.println("Making query for " + limit + " user books for user " + userId.get());
        List<FullUserBook> userBooks = ubHelper.getUserBooksForUser(authString, userId.get(), segment);
        
        // Set overall size
        segment.setTotalLength(ubHelper.getTotalNumberUserBooks(userId.get()));
      
        System.out.println("result: " + userBooks);
        
        ResultWrapper<FullUserBook> result = ResultWrapperUtil.createWrapper(userBooks, segment);
        return result;
      }   
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException error) {
      throw new WebApplicationException(
          "Error in getting UserBooks for user: " + userId.get(),
          error,
          Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  
  /**
   * Create a userbook in the database. This requires an authorization token to be
   * present in the headers.
   *
   * @param context
   *          security context (INJECTED via TokenFilter)
   * @param uriInfo
   *          Information about this URI
   * @param userBookBean
   *          UserBook to create in the database
   * @param userId
   *          ID of user
   * @param authString
   *          Authorization string
   * @return The newly created userbook
   */
  @ApiOperation(value = "Create new userbook",
      notes = "Create new userbook in database. Requires authentication token in header with key AUTHORIZATION. "
          + "Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876.")
  @ApiResponses(value = { @ApiResponse(code = 409, message = "Userbook already exists in database."),
      @ApiResponse(code = 200, message = "Userbook created.") })
  @POST
  @TokenRequired
  @Path("/{user_id}")
  @Timed(absolute = true, name = "create")
  public FullUserBook createUserBook(@Context SecurityContext context, @Context UriInfo uriInfo,

      @ApiParam(value = "ID of user.", required = false) @PathParam("user_id") IntParam userId,

      @ApiParam(value = "User Book information.", required = true) PostUserBook userBookBean,

      @ApiParam(value = "Bearer authorization", required = true) @HeaderParam(
          value = "Authorization") String authString) {
    // Start
    try {
      // Verify the username is 'admin or matches the userid's username.
      this.ubHelper.verifyUserIdHasAccess(context, userId.get());

      //////////////////
      // Save to database

      // This creates a new UserBook in the database without any tags
      if (userBookBean.getRating() == null) {
        userBookBean.setRating(true);
      }
      DatabaseUserBook newUserBook = ubHelper.createUserBook(userBookBean, userId);
      System.out.println("Created userbook in DB: " + newUserBook);

      ////////////
      // Set up tags in tagmap

      // Get tags from the incoming bean.
      List<String> incomingTags = userBookBean.getTags();
      if (incomingTags != null) {
        // 1- get IDs for all matching tags in the database
        Map<String, Tag> tagsInDbMap = ubHelper.getAllTags();

        // Iterate over tagsInDbMap values and for each entry,
        // if it is contained in 'incomingTags', add it to the list.
        List<Tag> matchingTags = tagsInDbMap.values()
            .stream()
            .filter(t -> incomingTags.contains(t.getName()))
            .collect(Collectors.toList());

        List<Tag> allTags = new ArrayList<Tag>(matchingTags);

        // 2- Create Tags in DB for tags that don't exist
        Tag newTag = null;
        for (String desiredTag : incomingTags) {
          if (!tagsInDbMap.containsKey(desiredTag)) {
            newTag = ubHelper.createTag(desiredTag);
            allTags.add(newTag);
          }
        }

        // 3- for each desired tag add an entry into the tagmap
        for (Tag tag : allTags) {
          // Make tag mapping [userId, userBookId, tagId]          
          TagMapping mapping = new TagMapping(
              userId.get(),
              newUserBook.getUserBookId(), 
              tag.getId());
          
          System.out.println("Create tagmapping: " + mapping);

          ubHelper.createTagMapping(mapping);
        }
      }

      //////////////////
      // Marshall back from database

      // Copy values into new 'UserBook' class
      FullUserBook userBookToReturn = this.ubHelper.getUserBookByUserBookId(authString, userId.get(), newUserBook.getUserBookId());

      return userBookToReturn;
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException error) {
      throw new WebApplicationException(
          "Error in updating database when creating user_book",
          error,
          Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Update a UserBook in the database
   * 
   * @param context
   *          security context (INJECTED via TokenFilter)
   * @param userBookBean
   *          UserBook to update in the database
   * @param userId
   *          ID of user
   * @param authString
   *          Authorization string
   * @param userBookId
   *          ID of user book to get. Taken from path param
   * 
   * @return The updated userbook
   */
  @ApiOperation(value = "Update a userbook",
      notes = "Updates a userbook in database. Requires authentication token in header with key AUTHORIZATION. "
          + "Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876.")
  @ApiResponses(value = { @ApiResponse(code = 200, response = FullUserBook.class,
      message = "Userbook successfully updated.") })
  @PUT
  @TokenRequired
  @Path("/{user_id}/{user_book_id}")
  @Timed(absolute = true, name = "update")
  public FullUserBook updateUserBook(@Context SecurityContext context,

      @ApiParam(value = "ID of user.", required = false) @PathParam("user_id") IntParam userId,

      @ApiParam(value = "ID of user_book.", required = false) @PathParam("user_book_id") IntParam userBookId,

      @ApiParam(value = "User Book information.", required = true) PostUserBook userBookBean,

      @ApiParam(value = "Bearer authorization", required = true) @HeaderParam(
          value = "Authorization") String authString) {
    // Start
    try {
      // Verify the username is 'admin or matches the userid's username.
      this.ubHelper.verifyUserIdHasAccess(context, userId.get());

      //////////////////
      // Get existing userbook from database
      ubHelper.updateUserBook(userBookBean, userId.get(), userBookId.get());

      List<String> tags = userBookBean.getTags();

      if (tags != null) {
        ////////////////
        // Set up tags in tagmap

        // Clear out tags for existing userbook
        ubHelper.deleteTagMappingsForUserBook(userBookId.get());

        // Get tags from the incoming bean
        List<String> incomingTags = userBookBean.getTags();

        // 1- get IDs for all matching tags in the database
        Map<String, Tag> tagsInDbMap = ubHelper.getAllTags();

        // Iterate over tagsInDbMap values and for each entry,
        // if it is contained in 'incomingTags', add it to the list matchingTags.
        List<Tag> matchingTags = tagsInDbMap.values()
            .stream()
            .filter(t -> incomingTags.contains(t.getName()))
            .collect(Collectors.toList());

        List<Tag> allTags = new ArrayList<Tag>(matchingTags);

        // 2- Create Tags in DB for tags that don't exist
        Tag newTag = null;
        for (String desiredTag : incomingTags) {
          if (!tagsInDbMap.containsKey(desiredTag)) {
            newTag = ubHelper.createTag(desiredTag);
            allTags.add(newTag);
          }
        }

        // 3- for each desired tag add an entry into the tagmap
        for (Tag tag : allTags) {
          // Make tag mapping [userBookId, tagId]
          TagMapping mapping = new TagMapping(
              userId.get(),
              userBookId.get(), 
              tag.getId());

          ubHelper.createTagMapping(mapping);
        }
      }

      //////////////////
      // Marshall back from database

      // Copy values into new 'UserBook' class
      FullUserBook userBookToReturn = this.ubHelper.getUserBookByUserBookId(authString, userId.get(), userBookId.get());
      return userBookToReturn;
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException error) {
      throw new WebApplicationException(
          "Error in updating database when creating user_book",
          error,
          Response.Status.INTERNAL_SERVER_ERROR);
    }
  }
}
