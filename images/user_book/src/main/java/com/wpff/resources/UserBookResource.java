package com.wpff.resources;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

import com.wpff.common.drop.filter.TokenRequired;
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

import com.codahale.metrics.annotation.Timed;

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
	 * @param ubHelper DAO helper
	 */
	public UserBookResource(UserBookHelper ubHelper) {
	
		this.ubHelper = ubHelper;
	}

	/**
	 * Delete a single UserBook by its id
	 * 
	 * @param context
	 *            security context (INJECTED via TokenFilter)
	 * @param userId
	 *            ID of user
	 * @param userBookId
	 *            ID of user_book
	 * @param authDummy
	 *            Dummy authorization string that is solely used for Swagger
	 *            description
	 * @return Response denoting if the operation was successful (202) or failed
	 *         (404)
	 */
	@ApiOperation(value = "Delete a UserBook.",
			notes = "Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876.")
	@DELETE
	@Path("/{user_id}/{user_book_id}")
	@TokenRequired
	@Timed(absolute=true, name="delete")
	public Response deleteUserBook(
    @Context SecurityContext context, 
	  
    @ApiParam(value = "ID of user.", required = false) 
    @PathParam("user_id") 
    IntParam userId,
    
    @ApiParam(value = "ID of userBook.", required = false) 
    @PathParam("user_book_id") 
    IntParam userBookId,
    
    @ApiParam(value = "Bearer authorization", required = true) 
    @HeaderParam(value = "Authorization") 
    String authDummy) 
	{
		// Start

		// Verify the username matches the userid or is 'admin'
		ubHelper.verifyUserIdHasAccess(context, userId.get());

		ubHelper.deleteUserBookById(userBookId.get());

		return Response.ok().build();
	}

	/**
	 * Return single userBook from database
	 *
	 * @param context
	 *            security context (INJECTED via TokenFilter)
	 * @param userId
	 *            ID of user
	 * @param userBookId
	 *            ID of user_book
	 * @param authString
	 *           Authorization string
	 * @return GetUserBook
	 * 
	 */
	@ApiOperation(value = "Get single UserBook.",
			notes = "Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876.")
	@GET
	@Path("/{user_id}/{user_book_id}")
	@TokenRequired
	@Timed(absolute=true, name="get")
	public FullUserBook getUserBook(
	    @Context SecurityContext context, 
	    
	    @ApiParam(value = "ID of user.", 	required = false) 
	    @PathParam("user_id") 
	    IntParam userId,

			@ApiParam(value = "ID of userBook.", required = false)
	    @PathParam("user_book_id") 
	    IntParam userBookId,

			@ApiParam(value = "Bearer authorization", required = true) 
	    @HeaderParam(value = "Authorization") 
	    String authString) {
		try {
			// Start

			// Verify the username matches the userid or is 'admin'
			ubHelper.verifyUserIdHasAccess(context, userId.get());

			return ubHelper.getUserBookById(authString, userBookId.get());
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
	@Timed(absolute=true, name="getAll")
	public ResultWrapper<FullUserBook> getUserBooks(
	    @Context SecurityContext context, 
	    @ApiParam(value = "ID of user.", required = false) 
	    @PathParam("user_id") 
	    IntParam userId,

			@ApiParam(value = "List of tags of books to retrieve. Only user books that have these tags will be returned",
					required = false)
      @QueryParam("tag") 
	    List<String> tagQuery,
	    
			@ApiParam(value = "ID of book to find in list of user books. Only the user book that contains this book id will be returned",
					required = false)
      @QueryParam("book_id") 
	    Integer bookId,
	    
			@ApiParam(value = "Title of books within the user's books.Only the books with that matching title will be returned",
					required = false)
      @QueryParam("book_title") 
	    String bookTitle,
	    
      @ApiParam(value = "Where to start the returned data segment from the full result.", required = false) 
      @QueryParam("offset") 
      Integer offset,

      @ApiParam(value = "size of the returned data segment.", required = false) 
			@QueryParam("limit") 
			Integer limit,

			@ApiParam(value = "Bearer authorization", required = true) 
	    @HeaderParam(value = "Authorization") 
	    String authString) {
		// Start
		try {
			// Verify the username matches the userid or is 'admin'
			ubHelper.verifyUserIdHasAccess(context, userId.get());

			// Create desired segment from offset & limit
			Segment segment = new Segment(offset, limit);
			List<FullUserBook> userBooks = null;

      userBooks = ubHelper.getUserBooksForUser(authString, userId.get(), segment);
      segment.setTotalLength(ubHelper.getTotalNumberUserBooks(userId.get()));

      ////////////////////
      // Filter out by tag, id, title
      
      // If tagQuery is non null, filter out tags
      if (! tagQuery.isEmpty()) {
        // Filter out books by tag. If the books tags match any in the query, keep them
        userBooks = userBooks.
            stream().
            filter(t -> !Collections.disjoint(t.getTags(), tagQuery)).
            collect(Collectors.toList());
      }
      
      if ( bookId != null ) {
        // Only return user-book if it matches the book_id
         userBooks = userBooks.
             stream().
             filter(t -> t.getBookId() == bookId.intValue()).
             collect(Collectors.toList());
      }
      
      if ( bookTitle != null && !bookTitle.isEmpty() ) {
          userBooks = userBooks.
             stream().
             filter(t -> t.getTitle().toLowerCase().contains(bookTitle.toLowerCase())).
             collect(Collectors.toList());
      }
      
      // Return values
      ResultWrapper<FullUserBook> result = ResultWrapperUtil.createWrapper(userBooks, segment);
      
      return result;

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
	 *           Authorization string
   * @return The newly created userbook
   */
	@ApiOperation(value = "Create new userbook",
			notes = "Create new userbook in database. Requires authentication token in header with key AUTHORIZATION. "
			    + "Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876.")
	@ApiResponses(value = { 
	    @ApiResponse(code = 409, message = "Userbook already exists in database."), 
	    @ApiResponse(code = 200, 
	       message = "Userbook created.")
	    })
	@POST
	@TokenRequired
	@Path("/{user_id}")
	@Timed(absolute=true, name="create")
	public FullUserBook createUserBook(
	    @Context SecurityContext context, 
	    @Context UriInfo uriInfo,
	    
	    @ApiParam(value = "ID of user.",
			required = false) @PathParam("user_id") 
	    IntParam userId,

			@ApiParam(value = "User Book information.", required = true) PostUserBook userBookBean,
			
			@ApiParam(value = "Bearer authorization", required = true) 
	    @HeaderParam(value = "Authorization") 
	    String authString) {
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
          // Make tag mapping [userBookId, tagId]
          TagMapping mapping = new TagMapping(newUserBook.getUserBookId(), tag.getId());

          ubHelper.createTagMapping(mapping);
        }
			}

			//////////////////
			// Marshall back from database

			// Copy values into new 'UserBook' class
			FullUserBook userBookToReturn = this.ubHelper.getUserBookById(authString, newUserBook.getUserBookId());
			
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
	 *           Authorization string
   * @param userBookId
   *          ID of user book to get. Taken from path param
   * 
   * @return The updated userbook
   */
  @ApiOperation(value = "Update a userbook",
      notes = "Updates a userbook in database. Requires authentication token in header with key AUTHORIZATION. "
			    + "Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876.")
	@ApiResponses(value = { 
	    	    @ApiResponse(code = 200, response = FullUserBook.class, message = "Userbook successfully updated.") })
	@PUT
	@TokenRequired
	@Path("/{user_id}/{user_book_id}")
	@Timed(absolute=true, name="update")
	public FullUserBook updateUserBook(
	    @Context 
	    SecurityContext context,
	    
	    @ApiParam(value = "ID of user.",required = false) 
	    @PathParam("user_id") 
	    IntParam userId,

	    @ApiParam(value = "ID of user_book.",required = false) 
	    @PathParam("user_book_id") 
	    IntParam userBookId,

			@ApiParam(value = "User Book information.", required = true) 
	    PostUserBook userBookBean,
			
			@ApiParam(value = "Bearer authorization", required = true) 
	    @HeaderParam(value = "Authorization") 
	    String authString) {
		// Start
		try {
			// Verify the username is 'admin or matches the userid's username.
			this.ubHelper.verifyUserIdHasAccess(context, userId.get());

			//////////////////
			// Get existing userbook from database
      ubHelper.updateUserBook(userBookBean, userBookId.get());
      
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
        List<Tag> matchingTags = tagsInDbMap.
			    values().
			    stream().
			    filter(t -> incomingTags.contains(t.getName())).collect(
					Collectors.toList());

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
          TagMapping mapping = new TagMapping(userBookId.get(), tag.getId());

          ubHelper.createTagMapping(mapping);
        }
      }

			//////////////////
			// Marshall back from database

			// Copy values into new 'UserBook' class
			FullUserBook userBookToReturn = this.ubHelper.getUserBookById(authString, userBookId.get());
			return userBookToReturn;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException error) {
			throw new WebApplicationException(
					"Error in updating database when creating user_book",
					error,
					Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
}
