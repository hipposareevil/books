package com.wpff.resources;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
 
import com.wpff.core.Author;
import com.wpff.db.AuthorDAO;
import com.wpff.filter.TokenRequired;

import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.params.IntParam;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

// Swagger
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
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

  private final AuthorDAO authorDAO;

  public AuthorResource(AuthorDAO authorDAO) {
    this.authorDAO = authorDAO;
  }

  /**
   * Return a single author, by id.
   *
   * @param authorId ID of author
   * @return Author 
   */
  @ApiOperation(
    value="Get author by ID.",
    notes="Get author information. Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876."
                )
  @GET
  @Path("/{id}")
  @UnitOfWork
  @TokenRequired
  public Author getAuthor(
    @ApiParam(value = "ID of author to retrieve.", required = false)
    @PathParam("id") IntParam authorId
                          ) {
    return findSafely(authorId.get());
  }

  /**
   * Get list authors.
   *
   * @param authorQuery Name of author, or partial name, that is used to
   * match against the database.
   * @return list of matching Author(s). When query is empty, this will be
   * all author
   */
  @ApiOperation(
    value="Get authors via optional 'name' query param.",
    notes="Returns list of authors. When 'name' is specified only matching authors are returned. Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876."    
                )
  @GET
  @UnitOfWork
  @TokenRequired
  public List<Author> getAuthor(
    @ApiParam(value = "Name or partial name of author to retrieve.", required = false)
    @QueryParam("name") String authorQuery
                                ) {
    if (authorQuery != null) {
      return authorDAO.findByName(authorQuery);
    }
    else {
      return authorDAO.findAll();
    }
  }

  /**
   * Create a new author in the DB.
   *
   * @param author Author to add
   * @return newly created Author
   */
  @ApiOperation(
    value="Create author.",
    notes="Create new author in the database. The 'id' field will be ignored. Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876.",
    response = Author.class
                )
  @POST
  @UnitOfWork
  @ApiResponse(code = 409, message = "Duplicate value")
  public Author createAuthor(
    @ApiParam(value = "Author information.", required = false)
    Author author
                             ) {
    try {
      author.setId(0);
      System.out.println("Creating new author: " + author);
      System.out.println("Creating new author: " + author.getName());
      return authorDAO.create(author);
    }
    catch (org.hibernate.exception.ConstraintViolationException e) {
      String errorMessage = e.getMessage();
      // check cause/parent
      if (e.getCause() != null) {
        errorMessage = e.getCause().getMessage();
      }

      throw new WebApplicationException(errorMessage, 409);
    }
  }

  /************************************************************************/
  /** Helper methods **/
  /************************************************************************/
  
  /**
   * Look for author by incoming id. If returned Author is null, throw 404.
   *
   * @param id ID of author to look for
   */
  private Author findSafely(int id) {
    return authorDAO.findById(id).orElseThrow(() -> new NotFoundException("No author by id " + id));
  }
  
}
