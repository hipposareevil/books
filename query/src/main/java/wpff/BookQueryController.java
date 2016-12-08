package wpff;

import java.util.List;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;

// Swagger
import io.swagger.annotations.*;


@Api( value="/query",
      tags= "Query",
      description="Queries google for books")
@RequestMapping("/query")
@RestController
public class BookQueryController {

  /**
   * google api key, injected via spring
   */
  @Value("${googleapikey:#{null}}")
  private String googleApiKey;


 /**
  * /query/title endpoint. Queries google for books
  *
  * @param author Name (or partial) of author
  * @param title Book title (or partial)
  * @return list of matching books
  */
  @ApiOperation(value = "/title", nickname = "query titles", notes="Query google for book titles")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "author", value = "Author's name", required = false, dataType = "string", paramType = "query"),
      @ApiImplicitParam(name = "title", value = "Book Title", required = false, dataType = "string", paramType = "query")
          })
  @ApiResponses(value = { 
      @ApiResponse(code = 200, message = "Success", response = Book.class)
    }) 
  @RequestMapping(method = RequestMethod.GET, path="/title", produces = "application/json")
  public List<Book> query(@RequestParam(value="author") String author,
                          @RequestParam(value="title") String title) {
    // Query google
    return QueryGoogle.getBooks(googleApiKey, author, title);    
  }


 /**
  * /query/author endpoint. Queries google for author names
  *
  * @param author Name (or partial) of author
  * @return list of matching author
  */
  @ApiOperation(value = "/author", nickname = "query author", notes="Query google for authors")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "author", value = "Author's name", required = false, dataType = "string", paramType = "query")
          })
  @ApiResponses(value = { 
      @ApiResponse(code = 200, message = "Success", response = Author.class)
    }) 
  @RequestMapping(method = RequestMethod.GET, path="/author", produces = "application/json")
  public List<Author> query(@RequestParam(value="author") String author) {
    // Query google
    return QueryGoogle.getAuthor(googleApiKey, author);
  }




}
