package com.wpff.resources;


import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

// utils
import org.apache.commons.beanutils.BeanUtils;

import com.wpff.core.Author;
import com.wpff.db.AuthorDAO;
import com.wpff.filter.TokenRequired;
import com.wpff.query.AuthorQuery;
import com.wpff.result.AuthorResult;

import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.params.IntParam;
// Swagger
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;


/**
 * Resource for /author url. Manages authors.
 */
@Api( value="/author",
      tags= "author",
      description="Manages authors")
@Path("/author")
@Produces(MediaType.APPLICATION_JSON)
public class AuthorResource {

  private final AuthorDAO authorDAO;

  public AuthorResource(AuthorDAO authorDAO) {
    this.authorDAO = authorDAO;
  }

  /**
   * Return a single author, by id.
   *
   * @param authorId ID of author
   * @param authDummy Dummy authorization string that is solely used for Swagger description.
   * @return Author 
   */
  @ApiOperation(
    value="Get author by ID.",
    notes="Get author information. Requires authentication token in header with key AUTHORIZATION. "
        + "Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876."
                )
  @GET
  @Path("/{id}")
  @UnitOfWork
  @TokenRequired
  public AuthorResult getAuthor(
    @ApiParam(value = "ID of author to retrieve.", required = false)
    @PathParam("id") IntParam authorId,
    @ApiParam(value="Bearer authorization", required=true)
    @HeaderParam(value="Authorization") String authDummy
                          ) {
    return convertToBean(findSafely(authorId.get()));
  }



  /**
   * Get list authors.
   *
   * @param authorQuery Name of author, or partial name, that is used to
   * match against the database.
   * @param authDummy Dummy authorization string that is solely used for Swagger description.
   * 
   * @return list of matching Author(s). When query is empty, this will be
   * all author
   */
  @ApiOperation(
    value="Get authors via optional 'name' query param.",
    notes="Returns list of authors. When 'name' is specified only matching authors are returned."  
        + " Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876."    
                )
  @GET
  @UnitOfWork
  @TokenRequired
  public List<AuthorResult> getAuthor(
    @ApiParam(value = "Name or partial name of author to retrieve.", required = false)
    @QueryParam("name") String authorQuery,
    @ApiParam(value="Bearer authorization", required=true)
    @HeaderParam(value="Authorization") String authDummy
                                ) {
    // Start
    
    List<Author> authors = null;
    if (authorQuery != null) {
      authors = authorDAO.findByName(authorQuery);
    }
    else {
      authors = authorDAO.findAll();
    }
    
    // Convert list of Authors (DB) to AuthorResults (bean)
    List<AuthorResult> results = authors.
        stream().
        sorted().
        map( x -> this.convertToBean(x)).
        collect(Collectors.toList());
    
    return results;
  }



  /**
   * Create a new author in the DB.
   *
   * @param authorBean Author to add
   * @param context security context (INJECTED via TokenFilter)
   * @param authDummy Dummy authorization string that is solely used for Swagger description.
   * @return newly created Author
   */
  @ApiOperation(
    value="Create author.",
    notes="Create new author in the database. The 'id' field will be ignored. Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876."
                )
  @POST
  @UnitOfWork
  @ApiResponse(code = 409, message = "Duplicate value")
  @TokenRequired
  public AuthorResult createAuthor(
    @ApiParam(value = "Author information.", required = false)
    AuthorQuery authorBean,
    @Context SecurityContext context,
    @ApiParam(value="Bearer authorization", required=true)
    @HeaderParam(value="Authorization") String authDummy
      ) {
    // START
    verifyAdminUser(context);
      
    try {
      // Make new Author from authorBean
      Author author = new Author();
      // copy(destination, source)
      BeanUtils.copyProperties(author, authorBean);

      return this.convertToBean(authorDAO.create(author));
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
   * @param authorId ID of author
   * @param context security context (INJECTED via TokenFilter)
   * @param authDummy Dummy authorization string that is solely used for Swagger description.
   * 
   * @return Response denoting if the operation was successful (202) or failed (404)
   */
  @ApiOperation(
    value="Delete author by ID.",
    notes="Delete author from database. Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876."
                )
  @DELETE
  @Path("/{id}")
  @UnitOfWork
  @TokenRequired
  public Response deleteAuthor(
    @ApiParam(value = "ID of author to retrieve.", required = true)
    @PathParam("id") IntParam authorId,
    @Context SecurityContext context,
    @ApiParam(value="Bearer authorization", required=true)
    @HeaderParam(value="Authorization") String authDummy
                        ) {
    try {
      // Start
      verifyAdminUser(context);

      authorDAO.delete(findSafely(authorId.get()));
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
   * @param dbAuthor Author in DB
   * @return Author bean
   */
  private AuthorResult convertToBean(Author dbAuthor) {
    AuthorResult result = new AuthorResult();
    
    try {
      BeanUtils.copyProperties(result,  dbAuthor);
      
       } 
    catch (IllegalAccessException | InvocationTargetException e) {
          e.printStackTrace();
    }
    
    
    return result;
  }
  
  /**
   * Look for author by incoming id. If returned Author is null, throw 404.
   *
   * @param id ID of author to look for
   */
  private Author findSafely(int id) {
    return authorDAO.findById(id).orElseThrow(() -> new NotFoundException("No author by id " + id));
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
