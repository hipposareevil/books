package com.wpff.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import javax.ws.rs.core.UriInfo;

// utils
import org.apache.commons.beanutils.BeanUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wpff.common.auth.TokenRequired;
import com.wpff.common.cache.Cache;
import com.wpff.common.result.ResultWrapper;
import com.wpff.common.result.ResultWrapperUtil;
import com.wpff.common.result.Segment;
import com.wpff.core.Book;
import com.wpff.db.BookDAO;
import com.wpff.query.BookQuery;
import com.wpff.result.BookResult;

import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.params.IntParam;
// Swagger
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Resource for the /book url. Manages books.
 */
@Api("/book")
@Path("/book")
@Produces(MediaType.APPLICATION_JSON)
public class BookResource {

  /**
   * Book DAO
   */
  private final BookDAO bookDAO;
  
  /**
   * Redis cache
   */
  private final Cache cache;

  public BookResource(BookDAO bookDAO, Cache cache) {
    this.bookDAO = bookDAO;
    this.cache = cache;
  }

  /**
   * Get a single book, by id.
   *
   * @param bookId
   *          ID of book
   * @param authorizationKey
   *          Dummy authorization string that is solely used for Swagger
   *          description.
   * @return Book
   */
  @ApiOperation(
    value="Get book by ID.",
    notes="Get book information. Requires authentication token in header with key AUTHORIZATION. " + 
    "Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876."
                )
  @GET
  @Path("/{book_id}")
  @UnitOfWork
  @TokenRequired
	@Timed(absolute=true, name="get")
  public BookResult getBook(
    @ApiParam(value = "ID of book to retrieve.", required = false)	
    @PathParam("book_id") 	
    IntParam bookId,	
    
    @ApiParam(value="Bearer authorization", required=true)	
    @HeaderParam(value="Authorization") 	
    String authorizationKey	 
                        ) {
    // The authorization string is passed in so we can get the author name
    // from the 'author' webservice
    return this.convertToBean(authorizationKey, findSafely(bookId.get()));	 
  }	
  /**	
   * Get list of books.	
   *	
   * @param offset	 
   *          Start index of data segment	
   * @param limit
   *          Size of data segment	
   * @param titleQuery	 
   *          [optional] Name of book, or partial name, that is used to match	
   *          against the database.	
   * @param idQuery	 
   *          [optional] List of book ids.	
   * @param authorIdQuery	 
   *          [optional] List of author ids.
   * @param authorizationKey
   *          Dummy authorization string that is solely used for Swagger
   *          description.
   * @return List of matching Books. When the params are empty, all books will be
   *         returned
   */
  @ApiOperation(
      value="Get books via optional 'title' query param or optional 'ids' query param. " + 
            "The three query params may be used at the same time.",
      notes="Returns list of books. When no 'title', 'ids', or 'authorIds' are specified, all books in database are returned. " +
            "Requires authentication token in header with key AUTHORIZATION. Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876."
                )
  @GET
  @UnitOfWork
  @TokenRequired
	@Timed(absolute=true, name="getAll")
  public ResultWrapper<BookResult> getBooks(
    @ApiParam(value = "Title or partial title of book to retrieve.", required = false)
    @QueryParam("title") String titleQuery,
    
    @ApiParam(value = "List of book IDs to retrieve.", required = false)
    @QueryParam("book_id") List<Integer> idQuery,
    
    @ApiParam(value = "List of Author IDs to get books for.", required = false)
    @QueryParam("author_id") List<Integer> authorIdQuery,

    @ApiParam(value = "Where to start the returned data segment from the full result.", required = false) 
    @QueryParam("offset") 
    Integer offset,

    @ApiParam(value = "size of the returned data segment.", required = false) 
   	@QueryParam("limit") 
		Integer limit,
        
    @ApiParam(value="Bearer authorization", required=true)
    @HeaderParam(value="Authorization") String authorizationKey
                            ) {
    // Start
   		
    // Using a Set to deal with any duplicates that might come up from using 
    // a 'title' and a 'id' that would overlap
    Set<Book> bookSet = new TreeSet<Book>();
    
    // When set to true, we won't return all books, just what we expected from the query
    boolean paramsExist = false;

    // Grab books by title, if it exists
    if (titleQuery != null) {
      bookSet.addAll(bookDAO.findByName(titleQuery));
      paramsExist = true;
    }

    // The idQuery will be empty if nothing is specified, but will still exist as a List.
    if ( (idQuery != null) && (! idQuery.isEmpty()) ){
      bookSet.addAll(bookDAO.findById(idQuery));
      paramsExist = true;
    }

    // The authorIdQuery will be empty if nothing is specified, but will still exist as a List.
    if ( (authorIdQuery != null) && (! authorIdQuery.isEmpty()) ){
      bookSet.addAll(bookDAO.findByAuthorId(authorIdQuery));
      paramsExist = true;
    }

    // If set of books is empty, grab all books
    if (bookSet.isEmpty() && (!paramsExist)) {
      ////////
      // Straight lookup
      
       // Create desired segment from offset & limit
      Segment segment = new Segment(offset, limit);
      segment.setTotalLength(bookDAO.getNumberOfBooks());
      
      // Get all for this segment
      bookSet.addAll(bookDAO.findAll(segment));
    
      // Convert the set of Books to list of BookResults
      // The authorization string is passed in so we can get the author name
      // from the 'author' webservice
      List<BookResult> bookList = bookSet.stream()
          .sorted()
          .map(x -> this.convertToBean(authorizationKey, x))
          .collect(Collectors.toList());
      
      return ResultWrapperUtil.createWrapper(bookList, segment);
    } else {
      ////////
      // Query Exists
      
      // Convert the set of Books to list of BookResults
      // The authorization string is passed in so we can get the author name
      // from the 'author' webservice
      List<BookResult> bookList = bookSet.
        stream().
        sorted().
        map( x -> this.convertToBean(authorizationKey, x)).
        collect(Collectors.toList());

      return ResultWrapperUtil.createWrapper(bookList, offset, limit);
    }
  }


  /**
   * Create a new book
   *
   * @param bookBean
   *          Book data
   * @param context
   *          security context (INJECTED via TokenFilter)
   * @param uriInfo
   *          Information about this URI
   * @param authorizationKey
   *          Dummy authorization string that is solely used for Swagger
   *          description.
   * @return newly created Book
   */
  @ApiOperation(
    value = "Create new book.",
    notes = "Creates new book. Requires authentication token in header with key AUTHORIZATION. "
        + "Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876."
                )
  @POST
  @UnitOfWork(transactional = false)
  @TokenRequired
  @ApiResponses( value = {
      @ApiResponse(code = 409, message = "Book already exists."),
      @ApiResponse(code = 200, 
                   message = "Book created.")
           })  
	@Timed(absolute=true, name="create")
  public BookResult createBook(
    @ApiParam(value = "Book information.", required = true)
    BookQuery bookBean,
    
    @Context SecurityContext context,
    @Context UriInfo uriInfo,
    
    @ApiParam(value="Bearer authorization", required=true)
    @HeaderParam(value="Authorization") String authorizationKey
                           ) {
    // START
    try {
       // Verify context's group is admin.
      verifyAdminUser(context);
      
      // See if book already exists, by title & author id
      boolean doesBookExist = checkIfBookExists(bookBean);
      if (doesBookExist) {
         throw new WebApplicationException("Book '" + bookBean.getTitle() + "' already exists.", Response.Status.CONFLICT);
      }
      
      // Make new Book from bookBean (which is a PostBook)
      Book bookInDatabase = new Book();
      // copy(destination, source)
      BeanUtils.copyProperties(bookInDatabase, bookBean);
      
      // the bookBean's subjects is a list, convert it into a CSV string
      BeanUtils.copyProperty(bookInDatabase, "subject", convertListToCsv(bookBean.getSubjects()));
      
      // the bookBean's isbns is a list, convert it into a CSV string
      BeanUtils.copyProperty(bookInDatabase, "isbn", convertListToCsv(bookBean.getIsbns()));

      // year is different too
      BeanUtils.copyProperty(bookInDatabase, "year", bookBean.getFirstPublishedYear()); 
      
      // open library url is different too
      BeanUtils.copyProperty(bookInDatabase, "olWorks", bookBean.getOpenlibraryWorkUrl());
      
      // The authorization string is passed in so we can get the author name 
      // from the 'author' webservice      
      Book created = bookDAO.create(bookInDatabase);
      return this.convertToBean(authorizationKey, created);
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
   * Updates a book in the database
   *
   * @param bookBean
   *          Book data
   * @param bookId
   *          ID of book to be updated
   * @param context
   *          security context (INJECTED via TokenFilter)
   * @param authorizationKey
   *          Dummy authorization string that is solely used for Swagger
   *          description.
   * @return updated Book
   */
  @ApiOperation(
    value = "Updates a book in the database.",
    notes = "Updats existing book. Requires authentication token in header with key AUTHORIZATION. "
        + "Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876.",
    response = Book.class
                )
  @PUT
  @UnitOfWork
  @TokenRequired
  @Path("/{book_id}")
	@Timed(absolute=true, name="update")
  public BookResult updateBook(
    @ApiParam(value = "Book information.", required = true)
    BookQuery bookBean,
    
    @ApiParam(value = "ID of book.", required = false) 
    @PathParam("book_id") 
    IntParam bookId,
    
    @Context SecurityContext context,
    
    @ApiParam(value="Bearer authorization", required=true)
    @HeaderParam(value="Authorization") String authorizationKey
                           ) {
    // START
    try {
       // Verify context's group is admin.
      verifyAdminUser(context);
      
      // Get existing book
      Book bookToUpdate = findSafely(bookId.get());        
      
      // Only copy over non null values. 
      // Note we don't override everything
      copyProperty(bookToUpdate, "year", bookBean.getFirstPublishedYear());
      copyProperty(bookToUpdate, "title", bookBean.getTitle());
      copyProperty(bookToUpdate, "description", bookBean.getDescription());
      copyProperty(bookToUpdate, "olWorks", bookBean.getOpenlibraryWorkUrl());
      copyProperty(bookToUpdate, "imageSmall", bookBean.getImageSmall());
      copyProperty(bookToUpdate, "imageMedium", bookBean.getImageMedium());
      copyProperty(bookToUpdate, "imageLarge", bookBean.getImageLarge());

      if (bookBean.getSubjects() != null) {
        // the bookBean's subjects is a list, convert it into a CSV string
        copyProperty(bookToUpdate, "subject", convertListToCsv(bookBean.getSubjects()));
      }

      if (bookBean.getIsbns() != null) {
        // the bookBean's isbns is a list, convert it into a CSV string
        copyProperty(bookToUpdate, "isbn", convertListToCsv(bookBean.getIsbns()));
      }

      // Update
      this.bookDAO.update(bookToUpdate);
      
      // Clear cache
      this.cache.clear("book.info", bookId.get());      
 
      // The authorization string is passed in so we can get the author name 
      // from the 'author' webservice      
      return this.convertToBean(authorizationKey, bookToUpdate);
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
   * @param authorizationKey Dummy authorization string that is solely used for Swagger description.
   * @return Response denoting if the operation was successful (202) or failed (404)
   */
  @ApiOperation(
    value="Delete book by ID.",
    notes="Delete book from database. Requires authentication token in header with key AUTHORIZATION."
        + " Example: AUTHORIZATION: Bearer qwerty-1234-asdf-9876."
                )
  @DELETE
  @Path("/{book_id}")
  @UnitOfWork
  @com.wpff.common.auth.TokenRequired
	@Timed(absolute=true, name="delete")
  public Response deleteBook(
    @ApiParam(value = "ID of book to retrieve.", required = true)
    @PathParam("book_id") 
    IntParam bookId,
    @Context SecurityContext context,
    @ApiParam(value="Bearer authorization", required=true)
    @HeaderParam(value="Authorization") 
    String authorizationKey
                        ) {
    // Start
    try {
      // Verify context's name is admin.
      verifyAdminUser(context);

      // Is OK to remove book
      bookDAO.delete(findSafely(bookId.get()));
      
      // Clear cache
      this.cache.clear("book.title", bookId.get());
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
   * Check if the incoming book via POST already exists. This will check against
   * existing books by the authorId and title
   * 
   * @param bookBean
   *          Bean to create new book
   * @return True if book already exists, false otherwise.
   */
  private boolean checkIfBookExists(BookQuery bookBean) {
    // Existing books by name
    Set<Book> bookSet = new TreeSet<Book>();
    bookSet.addAll(bookDAO.findByName(bookBean.getTitle()));
    
    for (Book current : bookSet) {
      if (current.getAuthorId() == bookBean.getAuthorId()) {
        return true;
      }
    }
            
    return false;
  }

  /**
   * Helper to convert a list into a csv of those values
   * 
   * @param values
   * @return
   */
  private static String convertListToCsv(List<String> values) {
    if (values != null) {
      if (values.size() > 20) {
        values = values.subList(0, 20);
      }

      String csvString = "";
      for (String s : values) {
        csvString += s + ",";
      }
      // trim last comma
      csvString = csvString.substring(0, csvString.length());
      return csvString;
    } else {
      return "";
    }
  }
  
  /**
   * Convert a Book from the DB into a BookResult for return to caller
   * 
   * @param authString
   *          Authentication header which is necessary for a REST call to 'author'
   *          web service
   * @param dbBook
   *          Book in DB
   * @return Book bean
   */
  private BookResult convertToBean(String authString, Book dbBook) {
    BookResult result = new BookResult();

    try {
      System.out.println("Converting: " + dbBook);
      BeanUtils.copyProperties(result, dbBook);
      
      // open library url
      BeanUtils.copyProperty(result, "openlibraryWorkUrl", dbBook.getOlWorks());

      // published year
      BeanUtils.copyProperty(result, "firstPublishedYear", dbBook.getYear());

      // NOTE:
      // the dbBook has 'getSubject' and 'getIsbn', both singular,
      // while the result bean has 'getSubjects' and 'getIsbns', both plural.
      // If they had the same name, the above copyProperties would die as 
      // it's copying a List to a String and vica versa.
      
      // dbBook's 'subjects' is a csv, convert into a list
      if (dbBook.getSubject() != null) {
        List<String> subjects = Arrays.asList(dbBook.getSubject().split("\\s*,\\s*"));
        BeanUtils.copyProperty(result, "subjects", subjects);
      }
      
      // dbBook's 'isbn' is a csv. Convert into a list
      List<String> isbns = Arrays.asList(dbBook.getIsbn().split("\\s*,\\s*"));
      BeanUtils.copyProperty(result, "isbns", isbns);

      // Get author name now via the 'author' webservice
      String authorName = getAuthorName(authString, dbBook.getAuthorId());
      BeanUtils.copyProperty(result, "authorName", authorName);      
    } 
    catch (IllegalAccessException | InvocationTargetException e) {
          e.printStackTrace();
    }
    
    return result; 
  }

  /**
   * Retrieve the author name from the 'author' webservice for the incoming
   * authorId
   * 
   * @param authString
   *          Authentication header which is necessary for a REST call to 'author'
   *          web service
   * @param authorId
   *          ID of author to get name for
   * @return
   */
  private String getAuthorName(String authString, int authorId) {
    try {
      String authorName = null;
      
      // Try the cache
      authorName = this.cache.get("author.name", authorId);
      if (authorName != null) {
        return authorName;
      }
      
      // Going to the 'author' web service directly
      String url = "http://author:8080/author/" + authorId;
      
      HttpClient client = HttpClientBuilder.create().build();
      HttpGet request = new HttpGet(url);

      // add request header
      request.addHeader("User-Agent", "BookAgent");
      request.addHeader("content-type", "application/json");
      request.addHeader("Authorization", authString);
      
      // Execute request
      HttpResponse response = client.execute(request);

      // Get code
      int responseCode = response.getStatusLine().getStatusCode();
      
      // Convert body of result
      BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
      StringBuffer result = new StringBuffer();
      String line = "";
      while ((line = rd.readLine()) != null) {
        result.append(line);
      }
      
       // Check result
      if (responseCode == 200) {
        // Convert into bean
        ObjectMapper mapper = new ObjectMapper();
        AuthorBean authorBean = null;
        try {
          authorBean = mapper.readValue(result.toString(), AuthorBean.class);
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
        
        authorName = authorBean.getName();
      }
      else {
        System.out.println("Unable to get author name for id: " + authorId);
        System.out.println("Error code: " + responseCode);
        System.out.println("Error content: " + result);
      }

      // Set cache
      this.cache.set("author.name", authorId, authorName);
      
      return authorName;
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
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
  
    /**
   * Copy non null property
   * 
   * @param destination
   * @param field
   * @param value
   * @throws InvocationTargetException 
   * @throws IllegalAccessException 
   */
  private static void copyProperty(Object destination, String field, Object value) 
      throws IllegalAccessException, InvocationTargetException {
    if (value != null) {
      BeanUtils.copyProperty(destination, field, value);

    }
  }
  
}
