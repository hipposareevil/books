package com.wpff.resources;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
import com.wpff.core.Author;
import com.wpff.query.AuthorQuery;
import com.wpff.result.AuthorResult;

import io.dropwizard.jersey.params.IntParam;
// Swagger
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;


/**
 * Resource for /author url. Manages authors.
 */
@Api( value="/author",
      tags= "author",
      description="Manages authors")
@Path("/author")
@Produces(MediaType.APPLICATION_JSON)
public class AuthorResource {

  private final AuthorHelper authorHelper;

  public AuthorResource(AuthorHelper authorHelper) {
    this.authorHelper = authorHelper;
  }

  /**
   * Return a single author, by id.
   *
   * @param authorId
   *          ID of author
   * @param authDummy
   *          Dummy authorization string that is solely used for Swagger
   *          description.
   * @return Author
   */
  @ApiOperation(
    value="Get author by ID.",
    notes="Get author information. Requires authentication token in header with key AUTHORIZATION. "
        + "Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876."
                )
  @GET
  @Path("/{author_id}")
  @TokenRequired
  public AuthorResult getAuthor(
    @ApiParam(value = "ID of author to retrieve.", required = false)
    @PathParam("author_id") 
    IntParam authorId,
    @ApiParam(value="Bearer authorization", required=true)
    @HeaderParam(value="Authorization")
    String authDummy
                          ) {
    Author authorInDb = this.authorHelper.findById(authorId.get());
    return convertToBean(authorInDb);
  }

  /**
   * Get list authors.
   *
   * @param offset
   *          Start index of data segment
   * @param limit
   *          Size of data segment
   * @param authorNameQuery
   *          Name of author, or partial name, that is used to match against the
   *          database.
   * @param authDummy
   *          Dummy authorization string that is solely used for Swagger
   *          description.
   * 
   * @return list of matching Author(s). When query is empty, this will be all
   *         author
   */
  @ApiOperation(
    value="Get authors via optional 'name' query param.",
    notes="Returns list of authors. When 'name' is specified only matching authors are returned."  
        + " Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876."    
                )
  @GET
  @TokenRequired
  public ResultWrapper<AuthorResult> getAuthor(
      @ApiParam(value = "Name or partial name of author to retrieve.", required = false)
      @QueryParam("name") String authorNameQuery,
    
      @ApiParam(value = "Where to start the returned data segment from the full result.", required = false) 
      @QueryParam("offset") 
      Integer offset,

      @ApiParam(value = "size of the returned data segment.", required = false) 
			@QueryParam("limit") 
			Integer limit,

      @ApiParam(value="Bearer authorization", required=true)
      @HeaderParam(value="Authorization") String authDummy
                                ) {
    // Start
    
    List<Author> authors = null;
    if (authorNameQuery != null) {
      authors = this.authorHelper.findByName(authorNameQuery);
    }
    else {
      authors = this.authorHelper.findAll();
    }
    
    // Convert list of Authors (DB) to AuthorResults (bean)
    List<AuthorResult> authorList = authors.
        stream().
        sorted().
        map( x -> this.convertToBean(x)).
        collect(Collectors.toList());
    
    System.out.println("author.get: start: " + offset);
        System.out.println("author.get: length: " + limit);
        
    ResultWrapper<AuthorResult> result = ResultWrapperUtil.createWrapper(authorList, offset, limit);
    
    return result;
  }


  /**
   * Create a new author in the DB.
   *
   * @param authorBean
   *          Author to add
   * @param context
   *          security context (INJECTED via TokenFilter)
   * @param uriInfo
   *          Information about this URI
   * @param authDummy
   *          Dummy authorization string that is solely used for Swagger
   *          description.
   * @return newly created Author
   */
  @ApiOperation(
    value="Create author.",
    notes="Create new author in the database. The 'id' field will be ignored. Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876."
                )
  @POST
  @ApiResponses( value = {
      @ApiResponse(code = 409, message = "Author already exists."),
      @ApiResponse(code = 200, 
                   message = "Author created. URI of Author is in the header 'location'.",
                   responseHeaders = @ResponseHeader(name = "location", description="URI of newly created Author")
                  )
           })
  @TokenRequired
  public Response createAuthor(
    @ApiParam(value = "Author information.", required = false)
    AuthorQuery authorBean,
    
    @Context SecurityContext context,
    
    @Context UriInfo uriInfo,
    
    @ApiParam(value="Bearer authorization", required=true)
    @HeaderParam(value="Authorization") String authDummy
      ) {
    if (authorBean == null) {
      throw new WebApplicationException("No data payload received for creating Author.", Response.Status.BAD_REQUEST);
    }
    
    // START
    verifyAdminUser(context);
      
    try {
      // Make new Author from authorBean
      Author authorInDatabase = new Author();
      
      // copy(destination, source)
      BeanUtils.copyProperties(authorInDatabase, authorBean);
      
      // Make subjects in DB a CSV string
      authorInDatabase.setSubjectsAsCsv(AuthorHelper.convertListToCsv(authorBean.getSubjects()));

      // Create the author in the database, 
      // then convert it to a normal bean and return that
      Author created = this.authorHelper.createAuthor(authorInDatabase);
      
      UriBuilder builder = uriInfo.getAbsolutePathBuilder();
      builder.path(Integer.toString(created.getId()));
      return Response.created(builder.build()).build();  
    }
    catch (org.hibernate.exception.ConstraintViolationException e) {
      String errorMessage = e.getMessage();
      // check cause/parent
      if (e.getCause() != null) {
        errorMessage = e.getCause().getMessage();
      }

      throw new WebApplicationException(errorMessage, 409);
    }
    catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException bean) {
      throw new WebApplicationException("Error in updating database when creating author  " + authorBean + ".", Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

   /**
   * Update an author in the DB.
   * 
   * @param authorBean
   *          Author to update
   * @param context
   *          security context (INJECTED via TokenFilter)
   * @param authorId  ID of author, from the path
   * @param authDummy
   *          Dummy authorization string that is solely used for Swagger
   *          description.
   * @return udpatedAuthor
   */
  @ApiOperation(
    value="Update an existing author.",
    notes="Update the author in the database. The 'id' field will be ignored. Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876."
                )
  @PUT
  @ApiResponse(code = 409, message = "Duplicate value")
  @TokenRequired
  @Path("/{author_id}")
  public AuthorResult updateAuthor(
    @ApiParam(value = "Author information.", required = false)
    AuthorQuery authorBean,
    
    @Context SecurityContext context,
    
    @ApiParam(value = "ID of author.", required = false) 
    @PathParam("author_id") 
    IntParam authorId,

    @ApiParam(value="Bearer authorization", required=true)
    @HeaderParam(value="Authorization") String authDummy
      ) {
    if (authorBean == null) {
      throw new WebApplicationException("No data payload received for updating Author.", Response.Status.BAD_REQUEST);
    }
    
    // START
    verifyAdminUser(context);
      
    try {
      Author updated = this.authorHelper.updateAuthor(authorBean, authorId.get());

      return this.convertToBean(updated);
    }
    catch (org.hibernate.exception.ConstraintViolationException e) {
      String errorMessage = e.getMessage();
      // check cause/parent
      if (e.getCause() != null) {
        errorMessage = e.getCause().getMessage();
      }

      throw new WebApplicationException(errorMessage, 409);
    }
    catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException bean) {
      throw new WebApplicationException("Error in updating database when creating author  " + authorBean + ".", Response.Status.INTERNAL_SERVER_ERROR);
    }
  }
  


  /**
   * Deletes a author by ID
   *
   * @param authorId
   *          ID of author
   * @param context
   *          security context (INJECTED via TokenFilter)
   * @param authDummy
   *          Dummy authorization string that is solely used for Swagger
   *          description.
   * 
   * @return Response denoting if the operation was successful (202) or failed
   *         (404)
   */
  @ApiOperation(
    value="Delete author by ID.",
    notes="Delete author from database. Requires authentication token in header with key AUTHORIZATION. "
        + "Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876. "
        + "User must be in the 'admin' group."
                )
  @DELETE
  @Path("/{author_id}")
  @TokenRequired
  public Response deleteAuthor(
    @ApiParam(value = "ID of author to retrieve.", required = true)
    @PathParam("author_id") IntParam authorId,
    @Context SecurityContext context,
    @ApiParam(value="Bearer authorization", required=true)
    @HeaderParam(value="Authorization") String authDummy
                        ) {
    try {
      // Start
      verifyAdminUser(context);

      this.authorHelper.deleteAuthor(authorId.get());
    }
    catch (org.hibernate.HibernateException he) {
      throw new NotFoundException("No author by id '" + authorId + "'");
    }
    return Response.ok().build();
  }




  /************************************************************************/
  /** Helper methods **/
  /************************************************************************/
  

  
  /**
   * Convert an Author from the DB into a AuthorResult for return to the caller
   * 
   * @param dbAuthor
   *          Author in DB
   * @return Author bean
   */
  private AuthorResult convertToBean(Author dbAuthor) {
    AuthorResult result = new AuthorResult();

    try {
      BeanUtils.copyProperties(result, dbAuthor);

      // dbAuthor's 'subjects' is a csv. Convert to a list
      List<String> subjects = Arrays.asList(dbAuthor.getSubjectsAsCsv().split("\\s*,\\s*"));
      BeanUtils.copyProperty(result, "subjects", subjects);
    } catch (IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }

    return result;
  }
 

  /**
   * Verifies the incoming user is 'admin'.
   * Throws exception if user is not admin.
   */
  static void verifyAdminUser(SecurityContext context) throws WebApplicationException {
    if (! context.isUserInRole("admin")) {
       throw new WebApplicationException("Must be logged in as a member of the 'admin' user group.", Response.Status.UNAUTHORIZED);
    }
  }


}
