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
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

// utils
import org.apache.commons.beanutils.BeanUtils;

import com.wpff.common.drop.filter.TokenRequired;
import com.wpff.common.result.ResultWrapper;
import com.wpff.common.result.ResultWrapperUtil;
import com.wpff.core.PostTag;
import com.wpff.core.Tag;
import com.wpff.db.TagDAO;

import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.params.IntParam;
// Swagger
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
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
   * @param offset
   *          Start index of data segment
   * @param limit
   *          Size of data segment
   * @param context
   *          security context (INJECTED via TokenFilter)
   * @param authDummy
   *          Dummy authorization string that is solely used for Swagger
   *          description.
   * @return List of tags
   */
	@ApiOperation(
	    value = "Get list of all tags.", 
	    notes = "Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876."
	    )
	@GET
	@UnitOfWork
	@TokenRequired
	public ResultWrapper<Tag> getTags(
	    @ApiParam(value = "Where to start the returned data segment from the full result.", required = false) 
			@QueryParam("offset") 
			Integer offset,

	    @ApiParam(value = "size of the returned data segment.", required = false) 
			@QueryParam("limit") 
			Integer limit,

			@Context 
			SecurityContext context,
			
			@ApiParam(value = "Bearer authorization", required = true) 
			@HeaderParam(value = "Authorization") 
	    String authDummy) {
	  // Start
		List<Tag> tags = tagDAO.findAll();
	
		ResultWrapper<Tag> result = ResultWrapperUtil.createWrapper(tags, offset, limit);
		return result;
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
	@ApiOperation(
	    value = "Get single tag.", 
	    notes = "Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876."
	    )
	@GET
	@Path("/{tag_id}")
	@UnitOfWork
	@TokenRequired
	public Tag getTag(
			@Context SecurityContext context,
			@ApiParam(value = "ID of tag to retrieve.", required = false) 
			@PathParam("tag_id") 
			IntParam tagId,
			
			@ApiParam(value = "Bearer authorization", required = true) 
			@HeaderParam(value = "Authorization") 
			String authDummy) {
	  // Start
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
	@ApiOperation(
	    value = "Delete tag from database", 
	    notes = "Create new user in database. Requires authentication token in header with key AUTHORIZATION. "
	        + "Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876. Caller must be 'admin' user. "
	    )
	@DELETE
	@Path("/{tag_id}")
	@UnitOfWork
	@TokenRequired
	public Response delete(
			@ApiParam(value = "Name of tag to delete.", required = true) 
			@PathParam("tag_id") 
			Integer tagId,
			
			@Context SecurityContext context,
			@ApiParam(value = "Bearer authorization", required = true) 
			@HeaderParam(value = "Authorization") 
			String authDummy) {
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
	@ApiOperation(
	    value = "Update tag in the database", 
	    notes = "Requires authentication token in header with key AUTHORIZATION. "
	        + "Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876. Caller must be 'admin' user. "
	    )
	@PUT
	@Path("/{tag_id}")
	@UnitOfWork
	@TokenRequired
	public Response update(
			@PathParam("tag_id") 
			Integer tagId,
			PostTag tagBean,
			@Context SecurityContext context,
			@ApiParam(value = "Bearer authorization", required = true) 
			@HeaderParam(value = "Authorization") 
			String authDummy) {
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
   * @param uriInfo
   *          Information about this URI	
	 * @param authDummy
	 *            Dummy authorization string that is solely used for Swagger
	 *            description.
	 * @return The newly created tag
	 */
	@ApiOperation(
	    value = "Create new tag", 
	    notes = "Create new tag in database. Requires authentication token in header with key AUTHORIZATION. "
	        + "Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876."
	        )
	@ApiResponses( value = {
      @ApiResponse(code = 409, message = "Tag already exists."),
      @ApiResponse(code = 200, 
                   message = "Tag created. URI of Tag is in the header 'location'.",
                   responseHeaders = @ResponseHeader(name = "location", description="URI of newly created Tag")
                  )
           })
	@POST
	@UnitOfWork
	@TokenRequired
	public Response createTag(
			@ApiParam(value = "Tag information.", required = true) 
			PostTag tagBean,
			@Context Jedis jedis,
      @Context UriInfo uriInfo,
    
			@ApiParam(value = "Bearer authorization", required = true) 
			@HeaderParam(value = "Authorization") 
			String authDummy) {
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
		Tag newTag = this.tagDAO.create(transientTag);

    UriBuilder builder = uriInfo.getAbsolutePathBuilder();
    builder.path(Integer.toString(newTag.getId()));
    return Response.created(builder.build()).build();
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
