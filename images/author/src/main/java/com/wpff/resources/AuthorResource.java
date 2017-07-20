package com.wpff.resources;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
 
import com.wpff.core.Author;
import com.wpff.db.AuthorDAO;
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
  @ApiOperation("Get author by ID")
  @GET
  @Path("/{id}")
  @UnitOfWork
  public Author getAuthor(@PathParam("id") IntParam authorId) {
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
  @ApiOperation("Get authors with optional 'name' query param.")
  @GET
  @UnitOfWork
  public List<Author> getAuthor(
    @ApiParam(value = "Name or partial name of author to retrieve.", required = false)@QueryParam("name") String authorQuery) {
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
    value = "Create Author",
    notes = "Create new Author",
    response = Author.class
                )
  @POST
  @UnitOfWork(transactional = false)
  @ApiResponse(code = 409, message = "Duplicate value")
  public Author createAuthor(Author author) {
    try {
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

  
  /**
   * Look for author by incoming id. If returned Author is null, throw 404.
   *
   * @param id ID of author to look for
   */
  private Author findSafely(int id) {
    return authorDAO.findById(id).orElseThrow(() -> new NotFoundException("No author by id " + id));
  }
  
}
