package com.wpff.resources;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
 
import com.wpff.core.Title;
import com.wpff.db.TitleDAO;
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
 * Resource for the /title url. Manages book titles.
 */
@Api( value="/title",
      tags="title")
@Path("/title")
@Produces(MediaType.APPLICATION_JSON)
public class TitleResource {

  private final TitleDAO titleDAO;

  public TitleResource(TitleDAO titleDAO) {
    this.titleDAO = titleDAO;
  }

  /**
   * Get a single book title, by id.
   *
   * @param titleId ID of title
   * @return Title
   */
  @ApiOperation(
    value="Get title by ID.",
    notes="Get title information. Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876."
                )
  @GET
  @Path("/{id}")
  @UnitOfWork
  @TokenRequired
  public Title getTitle(
    @ApiParam(value = "ID of title to retrieve.", required = false)
    @PathParam("id") IntParam titleId
                        ) {
    return findSafely(titleId.get());
  }


  /**
   * Get list of book titles.
   *
   * @param titleQuery Name of title, or partial name, that is used to
   * match against the database.
   * @return list of matching Titles. When titleQuery is empty, this will be
   * all book titles
   */
  @ApiOperation(value="Get titles via optional 'title' query param.",
                response=Title.class,
                notes="Returns list of books. Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876."
                )
  @GET
  @UnitOfWork
  @TokenRequired
  public List<Title> getTitle(
    @ApiParam(value = "Title or partial title of book to retrieve.", required = false)
    @QueryParam("title") String titleQuery
                              ) {
    if (titleQuery != null) {
      return titleDAO.findByName(titleQuery);
    }
    else {
      return titleDAO.findAll();
    }
  }


  /**
   * Create a new (book) title
   *
   * @param title Book Title
   * @return newly created Title
   */
  @ApiOperation(
    value = "Create new book.",
    notes = "Creates new book. Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876.",
    response = Title.class
                )
  @POST
  @UnitOfWork(transactional = false)
  @ApiResponse(code = 409, message = "Duplicate value")
  public Title createTitle(
    @ApiParam(value = "Book title information.", required = true)
    Title title
                           ) {
    try {
    return titleDAO.create(title);
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
   * Look for title by incoming id. If returned Title is null, throw 404.
   *
   * @param id ID of book to look for
   */
  private Title findSafely(int id) {
    return titleDAO.findById(id).orElseThrow(() -> new NotFoundException("No title by id " + id));
  }
  
}
