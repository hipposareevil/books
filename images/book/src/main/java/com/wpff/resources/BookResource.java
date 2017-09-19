package com.wpff.resources;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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

import com.wpff.core.Book;
import com.wpff.db.BookDAO;
import com.wpff.filter.TokenRequired;
import com.wpff.query.BookQuery;
import com.wpff.result.BookResult;

import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.params.IntParam;
// Swagger
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;


/**
 * Resource for the /book url. Manages books.
 */
@Api("/book")
@Path("/book")
@Produces(MediaType.APPLICATION_JSON)
public class BookResource {

  private final BookDAO bookDAO;

  public BookResource(BookDAO bookDAO) {
    this.bookDAO = bookDAO;
  }

  /**
   * Get a single book, by id.
   *
   * @param bookId ID of book
   * @param authDummy Dummy authorization string that is solely used for Swagger description.
   * @return Book
   */
  @ApiOperation(
    value="Get book by ID.",
    notes="Get book information. Requires authentication token in header with key AUTHORIZATION. " + 
    "Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876."
                )
  @GET
  @Path("/{id}")
  @UnitOfWork
  @TokenRequired
  public BookResult getBook(
    @ApiParam(value = "ID of book to retrieve.", required = false)
    @PathParam("id") IntParam bookId,
    @ApiParam(value="Bearer authorization", required=true)
    @HeaderParam(value="Authorization") String authDummy
                        ) {
    return this.convertToBean(findSafely(bookId.get()));
  }



  /**
   * Get list of books.
   *
   * @param titleQuery [optional] Name of book, or partial name, that is used to match against the database.
   * @param idQuery [optional] List of book ids.
   * @param authDummy Dummy authorization string that is solely used for Swagger description.
   * @return List of matching Books. When titleQuery is empty, this will be
   * all book titles
   */
  @ApiOperation(value="Get books via optional 'title' query param or optional 'ids' query param. " + 
                "The two query params may be used at the same time.",
                response=BookResult.class, responseContainer="List",
                notes="Returns list of books. When no 'title' or 'ids' specified, all books in database are returned. " +
                "Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876."
                )
  @GET
  @UnitOfWork
  @TokenRequired
  public List<BookResult> getBook(
    @ApiParam(value = "Title or partial title of book to retrieve.", required = false)
    @QueryParam("title") String titleQuery,
    @ApiParam(value = "List of book IDs to retrieve.", required = false)
    @QueryParam("id") List<Integer> idQuery,
    @ApiParam(value="Bearer authorization", required=true)
    @HeaderParam(value="Authorization") String authDummy
                            ) {
    // Start
    
    // Using a Set to deal with any duplicates that might come up from using 
    // a 'title' and a 'id' that would overlap
    Set<Book> bookSet = new TreeSet<Book>();

    // Grab books by title, if it exists
    if (titleQuery != null) {
      System.out.println("Looking at title query: " + titleQuery);
      bookSet.addAll(bookDAO.findByName(titleQuery));
    }

    // The idQuery will be empty if nothing is specified, but will still exist as a List.
    if ( (idQuery != null) && (! idQuery.isEmpty()) ){
      System.out.println("Looking at id query: " + idQuery);
      bookSet.addAll(bookDAO.findById(idQuery));
    }

    // If set of books is empty, grab all books
    if (bookSet.isEmpty()) {
      System.out.println("bookSet is empty. adding all");
      bookSet.addAll(bookDAO.findAll());
    }

    // Convert the set of Books to list of BookResults
    List<BookResult> bookList = bookSet.
        stream().
        sorted().
        map( x -> this.convertToBean(x)).
        collect(Collectors.toList());
    
    return bookList;
  }


  /**
   * Create a new book
   *
   * @param bookBean Book data
   * @param context security context (INJECTED via TokenFilter)
   * @param authDummy Dummy authorization string that is solely used for Swagger description.
   * @return newly created Book
   */
  @ApiOperation(
    value = "Create new book.",
    notes = "Creates new book. Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876.",
    response = Book.class
                )
  @POST
  @UnitOfWork(transactional = false)
  @TokenRequired
  @ApiResponse(code = 409, message = "Duplicate value")
  public BookResult createBook(
    @ApiParam(value = "Book information.", required = true)
    BookQuery bookBean,
    @Context SecurityContext context,
    @ApiParam(value="Bearer authorization", required=true)
    @HeaderParam(value="Authorization") String authDummy
                           ) {
    // START
    try {
       // Verify context's group is admin.
      verifyAdminUser(context);
      
      // Make new Book from bookBean (which is a PostBook)
      Book book = new Book();
      // copy(destination, source)
      BeanUtils.copyProperties(book, bookBean);

      return this.convertToBean(bookDAO.create(book));
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
      throw new WebApplicationException("Error in updating database when creating book  " + bookBean + ".", Response.Status.INTERNAL_SERVER_ERROR);
    }
  }


  /**
   * Deletes a book by ID
   *
   * @param bookId ID of book
   * @param context security context (INJECTED via TokenFilter)
   * @param authDummy Dummy authorization string that is solely used for Swagger description.
   * @return Response denoting if the operation was successful (202) or failed (404)
   */
  @ApiOperation(
    value="Delete book by ID.",
    notes="Delete book from database. Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876."
                )
  @DELETE
  @Path("/{id}")
  @UnitOfWork
  @TokenRequired
  public Response deleteBook(
    @ApiParam(value = "ID of book to retrieve.", required = true)
    @PathParam("id") IntParam bookId,
    @Context SecurityContext context,
    @ApiParam(value="Bearer authorization", required=true)
    @HeaderParam(value="Authorization") String authDummy
                        ) {
    // Start
    try {
      // Verify context's name is admin.
      verifyAdminUser(context);

      // Is OK to remove book
      bookDAO.delete(findSafely(bookId.get()));
      return Response.ok().build();

    }
    catch (org.hibernate.HibernateException he) {
      throw new NotFoundException("No book by id '" + bookId + "'");
    }
  }

  

  /************************************************************************/
  /** Helper methods **/
  /************************************************************************/

  /**
   * Convert a Book from the DB into a BookResult for return to caller
   * 
   * @param dbBook
   *          Book in DB
   * @return Book bean
   */
  private BookResult convertToBean(Book dbBook) {
    BookResult result = new BookResult();

    try {
      System.out.println("Converting: " + dbBook);
      BeanUtils.copyProperties(result, dbBook);
      // dbBook's 'isbn' is a csv. Convert into a list
      List<String> isbns = Arrays.asList(dbBook.getIsbn().split("\\s*,\\s*"));
      BeanUtils.copyProperty(result, "isbns", isbns);
      
            System.out.println("Returning bean: " + result);
    } 
    catch (IllegalAccessException | InvocationTargetException e) {
          e.printStackTrace();
    }
    
    return result; 
  }
  

  /**
   * Look for book by incoming id. If returned Book is null, throw 404.
   *
   * @param id ID of book to look for
   */
  private Book findSafely(int id) {
    return bookDAO.findById(id).orElseThrow(() -> new NotFoundException("No book by id " + id));
  }
  
  
  /**
   * Verifies the incoming user is in group "admin"
   * 
   * Throws exception if user is not admin.
   */
  static void verifyAdminUser(SecurityContext context) throws WebApplicationException {
    if (! context.isUserInRole("admin")) {
      throw new WebApplicationException("Must be logged in as a member of the 'admin' user group.", Response.Status.UNAUTHORIZED);
    }
  }

  
}
