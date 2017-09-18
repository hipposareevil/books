package com.wpff.resources;

import java.util.List;

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

import com.wpff.core.PostTag;
import com.wpff.core.Tag;
import com.wpff.db.TagDAO;
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
 * Resource at /tag that manages tags.
 */
@Api("/tag")
@Path("/tag")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TagResource {

	// Static Bearer text
	private static String BEARER = "Bearer";

	/**
	 * DAO used to get a Tag.
	 */
	private final TagDAO tagDAO;

	/**
	 * Create new TagResource with a tag DAO for validating tagname/password
	 *
	 * @param tagDAO
	 *            DAO used find a tag for authentication
	 */
	public TagResource(TagDAO tagDAO) {
		this.tagDAO = tagDAO;
	}

	/**
	 * Return all tags in the database
	 *
	 * @param context
	 *            security context (INJECTED via TokenFilter)
	 * @param authDummy
	 *            Dummy authorization string that is solely used for Swagger
	 *            description.
	 * @return List of tags
	 */
	@ApiOperation(value = "Get list of all tags.", notes = "Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876.")
	@GET
	@UnitOfWork
	@TokenRequired
	public List<Tag> getTags(
			@Context SecurityContext context,
			@ApiParam(value = "Bearer authorization", required = true) @HeaderParam(value = "Authorization") String authDummy) {
		List<Tag> tags = tagDAO.findAll();

		return tags;
	}

	/**
	 * Return single tag from database
	 *
	 * @param context
	 *            security context (INJECTED via TokenFilter)
	 * @param tagId
	 *            ID of tag
	 * @param authDummy
	 *            Dummy authorization string that is solely used for Swagger
	 *            description.
	 * @return Tag
	 */
	@ApiOperation(value = "Get single tag.", notes = "Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876.")
	@GET
	@Path("/{id}")
	@UnitOfWork
	@TokenRequired
	public Tag getTag(
			@Context SecurityContext context,
			@ApiParam(value = "ID of tag to retrieve.", required = false) @PathParam("id") IntParam tagId,
			@ApiParam(value = "Bearer authorization", required = true) @HeaderParam(value = "Authorization") String authDummy) {
		return findSafely(tagId.get());
	}

	/**
	 * Delete a specified tag from the database. A deletion is only performed if one
	 * of the following is true: - Tagname from security is 'admin'
	 *
	 * @param tagId
	 *            ID of tag to delete
	 * @param context
	 *            security context (INJECTED via TokenFilter)
	 * @param authDummy
	 *            Dummy authorization string that is solely used for Swagger
	 *            description.
	 * @return Response denoting if the operation was successful (202) or failed
	 *         (404)
	 */
	@ApiOperation(value = "Delete tag from database", notes = "Create new user in database. Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876. Caller must be 'admin' user. ")
	@DELETE
	@Path("/{id}")
	@UnitOfWork
	@TokenRequired
	public Response delete(
			@ApiParam(value = "Name of tag to delete.", required = true) @PathParam("id") Integer tagId,
			@Context SecurityContext context,
			@ApiParam(value = "Bearer authorization", required = true) @HeaderParam(value = "Authorization") String authDummy) {
		// Start
		verifyAdminUser(context);

		try {
			tagDAO.delete(findSafely(tagId));
			return Response.ok().build();
		} catch (org.hibernate.HibernateException he) {
			throw new NotFoundException("No tag by id '" + tagId + "'");
		}
	}

	/**
	 * Update a specified tag from the database. An update is only performed if one
	 * of the following is true: - User from security is 'admin'
	 *
	 * @param tagId
	 *            Name of tag to update
	 * @param tagBean
	 *            Tag bean with data that is used to update the Tag in the database.
	 * @param context
	 *            security context (INJECTED via TokenFilter)
	 * @param authDummy
	 *            Dummy authorization string that is solely used for Swagger
	 *            description.
	 * @return Response denoting if the operation was successful (202) or failed
	 *         (404)
	 */
	@ApiOperation(value = "Update tag in the database", notes = "Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876. Caller must be 'admin' user. ")
	@PUT
	@Path("/{id}")
	@UnitOfWork
	@TokenRequired
	public Response update(
			@PathParam("id") Integer tagId,
			PostTag tagBean,
			@Context SecurityContext context,
			@ApiParam(value = "Bearer authorization", required = true) @HeaderParam(value = "Authorization") String authDummy) {
		try {
			// Start
			verifyAdminUser(context);

			// Verify user exists in database
			Tag tagInDatabase = findSafely(tagId);
			if (tagInDatabase == null) {
				throw new NotFoundException("No tag by id '" + tagId + "'");
			}

			// Update properties in tag. Copying name & data manually instead of using
			// 'copyProperties' so the id doesn't get overwritten accidently.
			try {
				BeanUtils.copyProperty(tagInDatabase,
						"name",
						tagBean.getName());
				BeanUtils.copyProperty(tagInDatabase,
						"data",
						tagBean.getData());
			} catch (Exception bean) {
				throw new WebApplicationException("Error in updating database for tag " + tagId + ".",
						Response.Status.INTERNAL_SERVER_ERROR);
			}

			// Update database
			tagDAO.update(tagInDatabase);
		} catch (org.hibernate.HibernateException he) {
			he.printStackTrace();
			throw new NotFoundException("Error in database" + he.getMessage());
		}

		return Response.ok().build();
	}

	/**
	 * Create a tag in the database. This requires an authorization token to be
	 * present in the headers.
	 *
	 * @param tagBean
	 *            Tag to create in the database
	 * @param jedis
	 *            Jedis instance used to store token data. (INJECTED)
	 * @param authDummy
	 *            Dummy authorization string that is solely used for Swagger
	 *            description.
	 * @return The newly created tag
	 */
	@ApiOperation(value = "Create new tag", notes = "Create new tag in database. Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876.", response = Tag.class)
	@ApiResponse(code = 409, message = "Duplicate tag")
	@POST
	@UnitOfWork
	@TokenRequired
	public Tag createTag(
			@ApiParam(value = "Tag information.", required = true) PostTag tagBean,
			@Context Jedis jedis,
			@ApiParam(value = "Bearer authorization", required = true) @HeaderParam(value = "Authorization") String authDummy) {
		// Check if tag already exists

		// tagDAO.findByName looks for exact match
		List<Tag> existing = tagDAO.findByName(tagBean.getName());
		if (!existing.isEmpty()) {
			throw new WebApplicationException("Tag '" + tagBean.getName() + "' already exists.",
					Response.Status.CONFLICT);
		}

		// Create transient bean for this tag
		Tag transientTag = new Tag();

		// Update properties in tag
		try {
			BeanUtils.copyProperties(transientTag, tagBean);
		} catch (Exception bean) {
			throw new WebApplicationException("Error in creating tag " + tagBean.getName() + " in database",
					Response.Status.INTERNAL_SERVER_ERROR);
		}

		// No existing tag, go ahead and create
		return this.tagDAO.create(transientTag);
	}

	/****************************************************************
	 * 
	 * Helper methods
	 * 
	 ****************************************************************/

	/**
	 * Look for Tag by incoming id. If returned Tag is null, throw Not Found (404).
	 */
	private Tag findSafely(int id) {
		System.out.println("find safely by id: " + id + " :");
		return this.tagDAO.findById(id).orElseThrow(() -> new NotFoundException("No tag by id '" + id + "'"));
	}

	/**
   * Verifies the incoming user is 'admin'. Throws exception if user is not admin.
   */
  static void verifyAdminUser(SecurityContext context) throws WebApplicationException {
    if (!context.isUserInRole("admin")) {
      throw new WebApplicationException("Must be logged in as a member of the 'admin' user group.", Response.Status.UNAUTHORIZED);
    }
  }

}
