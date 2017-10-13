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

import com.wpff.common.drop.filter.TokenRequired;
import com.wpff.common.result.ResultWrapper;
import com.wpff.common.result.ResultWrapperUtil;
import com.wpff.core.DatabaseUserBook;
import com.wpff.core.FullUserBook;
import com.wpff.core.PostUserBook;
import com.wpff.core.Tag;
import com.wpff.core.TagMapping;

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
	public Response deleteUserBook(@Context SecurityContext context, 
	    @ApiParam(value = "ID of user.", required = false) 
     	@PathParam("user_id") 
	    IntParam userId,

			@ApiParam(value = "ID of userBook.", required = false) @PathParam("user_book_id") IntParam userBookId,

			@ApiParam(value = "Bearer authorization", required = true) @HeaderParam(value = "Authorization") String authDummy) {
		// Start

		// Verify the username matches the userid or is 'admin'
		ubHelper.verifyUserIdHasAccess(context, userId.get());

		System.out.println("UBR: getUserBook for user " + userId.get() + ", userbookid: " + userBookId.get());

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
	 * @param authDummy
	 *            Dummy authorization string that is solely used for Swagger
	 *            description.
	 * @return GetUserBook
	 * 
	 */
	@ApiOperation(value = "Get single UserBook.",
			notes = "Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876.")
	@GET
	@Path("/{user_id}/{user_book_id}")
	@TokenRequired
	public FullUserBook getUserBook(
	    @Context SecurityContext context, 
	    @ApiParam(value = "ID of user.", 	required = false) 
	    @PathParam("user_id") 
	    IntParam userId,

			@ApiParam(value = "ID of userBook.", required = false) @PathParam("user_book_id") IntParam userBookId,

			@ApiParam(value = "Bearer authorization", required = true) @HeaderParam(value = "Authorization") String authDummy) {
		try {
			// Start

			// Verify the username matches the userid or is 'admin'
			ubHelper.verifyUserIdHasAccess(context, userId.get());

			System.out.println("UBR: getUserBook for user " + userId.get() + ", userbookid: " + userBookId.get());

			return ubHelper.getUserBookById(userBookId.get());
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
	 *            security context (INJECTED via TokenFilter)
	 * @param userId
	 *            ID of user
	 * @param tagQuery
	 *            [optional] List of tags of books.
   * @param start
   *          Start index of data segment
   * @param segmentSize
   *          Size of data segment
	 * @param authDummy
	 *            Dummy authorization string that is solely used for Swagger
	 *            description.
	 * @return List of GetUserBook
	 * 
	 */
	@ApiOperation(value = "Get userBooks for user.",
			notes = "Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876.")
	@GET
	@Path("/{user_id}")
	@TokenRequired
	public ResultWrapper<FullUserBook> getUserBooks(
	    @Context SecurityContext context, 
	    @ApiParam(value = "ID of user.", required = false) 
	    @PathParam("user_id") 
	    IntParam userId,

			@ApiParam(value = "List of tags of books to retrieve. Only user books that have these tags will be returned",
					required = false)
      @QueryParam("tag") List<String> 
	    tagQuery,
	    
      @ApiParam(value = "Where to start the returned data segment from the full result.", required = false) 
      @QueryParam("start") 
      Integer start,

      @ApiParam(value = "size of the returned data segment.", required = false) 
			@QueryParam("segmentSize") 
			Integer segmentSize,
	    

			@ApiParam(value = "Bearer authorization", required = true) 
	    @HeaderParam(value = "Authorization") 
	    String authDummy) {
		// Start
		try {
			// Verify the username matches the userid or is 'admin'
			ubHelper.verifyUserIdHasAccess(context, userId.get());

			// Ok to get books
			List<FullUserBook> userBooks = ubHelper.getUserBooksForUser(userId.get());

      // If tagQuery is non null, filter out tags
      if (! tagQuery.isEmpty()) {
        // Filter out books by tag. If the books tags match any in the query, keep them
        userBooks = userBooks.stream().filter(t -> !Collections.disjoint(t.getTags(), tagQuery)).collect(
            Collectors.toList());
      }

      ResultWrapper<FullUserBook> result = ResultWrapperUtil.createWrapper(userBooks, start, segmentSize);
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
	 *            security context (INJECTED via TokenFilter)
	 * @param userBookBean
	 *            UserBook to create in the database
	 * @param userId
	 *            ID of user
	 * @param authDummy
	 *            Dummy authorization string that is solely used for Swagger
	 *            description.
	 * @return The newly created userbook
	 */
	@ApiOperation(value = "Create new userbook",
			notes = "Create new userbook in database. Requires authentication token in header with key AUTHORIZATION. "
			    + "Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876.")
	@ApiResponses(value = { 
	    @ApiResponse(code = 409, message = "Userbook already exists in database."), 
	    @ApiResponse(code = 200, response = FullUserBook.class, message = "Userbook successfully added.") })
	@POST
	@TokenRequired
	@Path("/{user_id}")
	public FullUserBook createUserBook(@Context SecurityContext context, @ApiParam(value = "ID of user.",
			required = false) @PathParam("user_id") IntParam userId,

			@ApiParam(value = "User Book information.", required = true) PostUserBook userBookBean,
			
			@ApiParam(value = "Bearer authorization", required = true) @HeaderParam(
					value = "Authorization") String authDummy) {
		// Start
		try {
			// Verify the username is 'admin or matches the userid's username.
			this.ubHelper.verifyUserIdHasAccess(context, userId.get());

			//////////////////
			// Save to database

			// This creates a new UserBook in the database without any tags
			DatabaseUserBook newUserBook = ubHelper.createUserBook(userBookBean, userId);
			System.out.println("Created userbook in DB: " + newUserBook);

			////////////
			// Set up tags in tagmap

			// Get tags from the incoming bean.
			List<String> incomingTags = userBookBean.getTags();

			// 1- get IDs for all matching tags in the database
			Map<String, Tag> tagsInDbMap = ubHelper.getAllTags();

			// Iterate over tagsInDbMap values and for each entry,
			// if it is contained in 'incomingTags', add it to the list.
			List<Tag> matchingTags = tagsInDbMap.values().stream().filter(t -> incomingTags.contains(t.getName())).collect(
					Collectors.toList());

			for (Tag x : matchingTags) {
				System.out.println("got matching tag: " + x);
			}

			List<Tag> allTags = new ArrayList<Tag>(matchingTags);

			// 2- Create Tags in DB for tags that don't exist
			Tag newTag = null;
			for (String desiredTag : incomingTags) {
				if (!tagsInDbMap.containsKey(desiredTag)) {
					System.out.println("We are missing tag " + desiredTag + " in database");

					newTag = ubHelper.createTag(desiredTag);
					allTags.add(newTag);
					System.out.println("NEW TAG: " + newTag);
				}
			}

			System.out.println("");
			// 3- for each desired tag add an entry into the tagmap
			for (Tag tag : allTags) {
				System.out.println("Looking at tag: " + tag);

				// Make tag mapping [userBookId, tagId]
				TagMapping mapping = new TagMapping(newUserBook.getUserBookId(), tag.getId());

				ubHelper.createTagMapping(mapping);
				System.out.println("Created new tag mapping: " + mapping);
			}


			//////////////////
			// Marshall back from database

			// Copy values into new 'UserBook' class
			FullUserBook userBookToReturn = this.ubHelper.getUserBookById(newUserBook.getUserBookId());
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
   * @param authDummy
   *          Dummy authorization string that is solely used for Swagger
   *          description.
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
	public FullUserBook updateUserBook(
	    @Context SecurityContext context, 
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
	    String authDummy) {
		// Start
		try {
			// Verify the username is 'admin or matches the userid's username.
			this.ubHelper.verifyUserIdHasAccess(context, userId.get());

			//////////////////
			// Get existing userbook from database
      DatabaseUserBook existingUserBook = ubHelper.updateUserBook(userBookBean, userBookId.get());

      // Update with any non null values from userBookBean
      System.out.println("PUT: rating: " + userBookBean.getRating());
      System.out.println("PUT: tags: " + userBookBean.getTags());
      System.out.println("PUT: data: " + userBookBean.getData());

      ////////////////
			// Set up tags in tagmap
      
      // Clear out tags for existing userbook
      ubHelper.deleteTagMappingsForUserBook(userBookId.get());
      
			// Get tags from the incoming bean.
			List<String> incomingTags = userBookBean.getTags();

			// 1- get IDs for all matching tags in the database
			Map<String, Tag> tagsInDbMap = ubHelper.getAllTags();

			// Iterate over tagsInDbMap values and for each entry,
			// if it is contained in 'incomingTags', add it to the list matchingTags.
			List<Tag> matchingTags = tagsInDbMap.values().stream().filter(t -> incomingTags.contains(t.getName())).collect(
					Collectors.toList());

			for (Tag x : matchingTags) {
				System.out.println("got matching tag: " + x);
			}

			List<Tag> allTags = new ArrayList<Tag>(matchingTags);

			// 2- Create Tags in DB for tags that don't exist
			Tag newTag = null;
			for (String desiredTag : incomingTags) {
				if (!tagsInDbMap.containsKey(desiredTag)) {
					System.out.println("We are missing tag " + desiredTag + " in database");

					newTag = ubHelper.createTag(desiredTag);
					allTags.add(newTag);
					System.out.println("NEW TAG: " + newTag);
				}
			}

			System.out.println("");
			// 3- for each desired tag add an entry into the tagmap
			for (Tag tag : allTags) {
				System.out.println("Looking at tag: " + tag);

				// Make tag mapping [userBookId, tagId]
				TagMapping mapping = new TagMapping(userBookId.get(), tag.getId());

				ubHelper.createTagMapping(mapping);
				System.out.println("Created new tag mapping: " + mapping);
			}


			//////////////////
			// Marshall back from database

			// Copy values into new 'UserBook' class
			FullUserBook userBookToReturn = this.ubHelper.getUserBookById(userBookId.get());
			return userBookToReturn;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException error) {
			throw new WebApplicationException(
					"Error in updating database when creating user_book",
					error,
					Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
}
