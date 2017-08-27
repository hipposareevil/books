package com.wpff.resources;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.wpff.core.FullUserBook;
import com.wpff.core.PostUserBook;
import com.wpff.core.Tag;
import com.wpff.core.TagMapping;
import com.wpff.core.DatabaseUserBook;
import com.wpff.db.TagDAO;
import com.wpff.db.UserBookDAO;
import com.wpff.db.UserDAO;
import com.wpff.filter.TokenRequired;

import io.dropwizard.jersey.params.IntParam;
// Swagger
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
// Jedis
import redis.clients.jedis.Jedis;

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
	 * @param userDAO
	 *            DAO used for dealing with user
	 * @param userBookDAO
	 *            used for dealing with user_books
	 * @param tagDAO
	 *            DAO used find a tag for authentication
	 */
	public UserBookResource(UserDAO userDAO, TagDAO tagDAO, UserBookDAO userBookDAO, UserBookHelper ubHelper) {
		this.tagDAO = tagDAO;
		this.userBookDAO = userBookDAO;
		this.userDAO = userDAO;
		this.ubHelper = ubHelper;
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
	@ApiOperation(value = "Get single userBook.", notes = "Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876.")
	@GET
	@Path("/{user_id}/{user_book_id}")
	@TokenRequired
	public FullUserBook getUserBook(@Context SecurityContext context,
			@ApiParam(value = "ID of user.", required = false) @PathParam("user_id") IntParam userId,

			@ApiParam(value = "ID of userBook.", required = false) @PathParam("user_book_id") IntParam userBookId,

			@ApiParam(value = "Bearer authorization", required = true) @HeaderParam(value = "Authorization") String authDummy) {
		try {
			// Start

			// Verify the username matches the userid or is 'admin'
			ubHelper.verifyUserIdMatches(context, userId.get());

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
	 * @param authDummy
	 *            Dummy authorization string that is solely used for Swagger
	 *            description.
	 * @return List of GetUserBook
	 * 
	 */
	@ApiOperation(value = "Get userBooks for user.", notes = "Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876.")
	@GET
	@Path("/{user_id}")
	@TokenRequired
	public List<FullUserBook> getUserBook(@Context SecurityContext context,
			@ApiParam(value = "ID of user.", required = false) @PathParam("user_id") IntParam userId,

			@ApiParam(value = "Bearer authorization", required = true) @HeaderParam(value = "Authorization") String authDummy) {
		// Start
		try {
			// Verify the username matches the userid or is 'admin'
			ubHelper.verifyUserIdMatches(context, userId.get());

			// Ok to get books
			List<FullUserBook> userBooks = ubHelper.getUserBooksForUser(userId.get());
			return userBooks;

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
	 * @param jedis
	 *            Jedis instance used to store token data. (INJECTED)
	 * @param authDummy
	 *            Dummy authorization string that is solely used for Swagger
	 *            description.
	 * @return The newly created user
	 */
	@ApiOperation(value = "Create new userbook", notes = "Create new userbook in database. Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876.")
	@ApiResponses(value = { @ApiResponse(code = 409, message = "Userbook already exists in database."),
			@ApiResponse(code = 200, response = FullUserBook.class, message = "Userbook successfully added.") })
	@POST
	@TokenRequired
	@Path("/{user_id}")
	public FullUserBook createUserBook(@Context SecurityContext context,
			@ApiParam(value = "ID of user.", required = false) @PathParam("user_id") IntParam userId,

			@ApiParam(value = "User Book information.", required = true) PostUserBook userBookBean,

			@Context Jedis jedis,
			@ApiParam(value = "Bearer authorization", required = true) @HeaderParam(value = "Authorization") String authDummy) {
		// Start
		try {
			// Verify the username is 'admin or matches the userid's username.
			this.ubHelper.verifyUserIdMatches(context, userId.get());

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
			List<Tag> matchingTags = tagsInDbMap.values().stream().filter(t -> incomingTags.contains(t.getName()))
					.collect(Collectors.toList());

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

			System.out.println("");

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
}
