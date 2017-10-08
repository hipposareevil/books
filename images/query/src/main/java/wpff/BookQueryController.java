package wpff;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// wpff common
import com.wpff.common.result.ResultWrapper;
import com.wpff.common.result.ResultWrapperUtil;

// Swagger
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import wpff.openlibrary.OpenLibraryHelper;
import wpff.openlibrary.OpenLibraryUrlConverter;
import wpff.openlibrary.OpenLibraryUrlConverter.ImageSize;
import wpff.openlibrary.beans.OpenLibraryAuthor;
import wpff.openlibrary.beans.OpenLibraryTitle;
import wpff.result.QueryAuthorResult;
import wpff.result.QueryTitleResult;


@Api( value="/query",
      tags= "Query",
      description="Queries openlibrary.org for books")
@RequestMapping("/query")
@RestController
public class BookQueryController<T> {
	
	
	////////////////////////////////////////////////////////
	//
	// Open Library calls
	// 

  /**
   * /query/author endpoint. Will make query to OpenLibrary.org.
   * 
   * @param authorQuery
   *          Name (or partial name) of author
   * @return List of matching Authors
   * @throws UnsupportedEncodingException
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   */
	@ApiOperation(value = "/author", nickname = "query author",
			notes = "Query openlibrary.org for authors. Returns list of authors.")
	@ApiImplicitParams(
	    {   
	   @ApiImplicitParam(
	       name = "author", 
	       value = "Author's name", 
	       required = false,
			   dataType = "string", 
			   paramType = "query"),
	    @ApiImplicitParam(
	       name = "start", 
	       value = "Where to start the returned data segment from the full result",
	       required = false,
			   dataType = "int", 
			   paramType = "query"),
	     @ApiImplicitParam(
	       name = "segmentSize", 
	       value = "Size of the returned data segment.",
	       required = false,
			   dataType = "int",
			   paramType = "query")
	  }
	)
	@RequestMapping(method = RequestMethod.GET, path = "/author", produces = "application/json")	
	public ResultWrapper<QueryAuthorResult> queryForAuthor(
	    @RequestParam(value = "author") String authorQuery,
  	    @RequestParam(value = "start", required=false) Integer start,
  	    @RequestParam(value = "segmentSize", required=false) Integer segmentSize
	    )
			throws UnsupportedEncodingException, IllegalAccessException, InvocationTargetException 
	{
		// Begin
		List<OpenLibraryAuthor> authors = OpenLibraryHelper.queryForAuthors(authorQuery);
		if (authors.size() == 0) {
			// No authors, try with an asterix
			authors = OpenLibraryHelper.queryForAuthors(authorQuery);
		}
		
		// Convert to AuthorResult. This is done as the OpenLibraryAuthor has strange
		// field names due to the JSON returned from openlibrary.org
    List<QueryAuthorResult> authorList= authors.
        stream().
        sorted().
        map( x -> this.convertToResult(x)).
        collect(Collectors.toList());
    
    // create wrapper
    ResultWrapper<QueryAuthorResult> results = ResultWrapperUtil.createWrapper(authorList, start, segmentSize);
        
    return results;
	}
	


	/**
	 * /query/title endpoint. Queries openlibrary for books
	 *
	 * @param author
	 *            Name (or partial) of author
	 * @param title
	 *            Book title (or partial)
	 * @param isbn
	 *            ISBN of book
	 * @return list of matching Books
	 * @throws IOException 
	 */
	@ApiOperation(value = "/book", nickname = "query book titles",
                notes = "Query openlibrary for book titles. Results are sorted by the number of ISBNs per book."
                + " The first titles in the resulting list will be the ones with more associated ISBNS")
	@ApiImplicitParams(
	    {
    @ApiImplicitParam(name = "author", value = "Author's name", required = false,
                        dataType = "string", paramType = "query"),
    @ApiImplicitParam(name = "title", value = "Book Title", required = false,
                        dataType = "string", paramType = "query"),
    @ApiImplicitParam(name = "isbn", value = "Book ISBN", required = false,
                        dataType = "string", paramType = "query"),
   @ApiImplicitParam(
	       name = "start", 
	       value = "Where to start the returned data segment from the full result",
	       required = false,
			   dataType = "int", 
			   paramType = "query"),
	     @ApiImplicitParam(
	       name = "segmentSize", 
	       value = "Size of the returned data segment.",
	       required = false,
			   dataType = "int",
			   paramType = "query")      
          })
	@RequestMapping(method = RequestMethod.GET, path = "/book", produces = "application/json")
	public ResultWrapper<QueryTitleResult> queryForTitles(
    @RequestParam(value = "author", required=false) String author, 
    @RequestParam(value = "title", required=false) String title,
    @RequestParam(value = "isbn", required=false) String isbn,
    @RequestParam(value = "start", required=false) Integer start, 
  	  @RequestParam(value = "segmentSize", required=false) Integer segmentSize 
    ) throws IOException {
	  System.out.println("Got query for titles: " + author + ":" + title + ":" + isbn);
		// Begin
		List<OpenLibraryTitle> titles = OpenLibraryHelper.queryForTitles(author, title, isbn);

    // Convert
    List<QueryTitleResult> bookList = titles.
        stream().
        sorted().
        map( x -> this.convertToResult(x)).
        collect(Collectors.toList());
    
    // Sort the list of titles by the # of isbns
    Collections.sort(bookList);
    
  // create wrapper
    ResultWrapper<QueryTitleResult> results = ResultWrapperUtil.createWrapper(bookList, start, segmentSize);
        
    return results;
	}
	
	
	///////////////////////////////////////////////////////////
	//
	// Private methods
	//
	
	


	
	/**
	 * Convert an OpenLibrary object to normal bean
	 * 
	 * @param author
	 *            Author to convert
	 * @return converted bean
	 */
	private QueryAuthorResult convertToResult(OpenLibraryAuthor author) {
		QueryAuthorResult newResult = new QueryAuthorResult();
		
		try {
      BeanUtils.copyProperties(newResult, author);
      BeanUtils.copyProperty(newResult, "birthDate", author.getBirth_date());
    } catch (IllegalAccessException | InvocationTargetException e) {
      // Unable to copy properties
      e.printStackTrace();
    }

		// openlibrary key
		newResult.setOlKey(author.getKey());
		// subjects
		newResult.setSubjects(author.getTop_subjects());

		// Set images for the author
		newResult.setImageSmall(OpenLibraryUrlConverter.createAuthorImageUrl(author.getKey(), ImageSize.SMALL));
		newResult.setImageMedium(
				OpenLibraryUrlConverter.createAuthorImageUrl(author.getKey(), ImageSize.MEDIUM));
		newResult.setImageLarge(OpenLibraryUrlConverter.createAuthorImageUrl(author.getKey(), ImageSize.LARGE));

		return newResult;
	}

	/**
	 * Convert an OpenLibrary object to normal bean
	 * 
	 * @param openLibraryTitle
	 *            Title to convert
	 * @return
	 */
	private QueryTitleResult convertToResult(OpenLibraryTitle openLibraryTitle)  {
		QueryTitleResult newResult = new QueryTitleResult();

		System.out.println("converting: " + openLibraryTitle);
		
		
		newResult.setTitle(openLibraryTitle.getTitle_suggest());
		
		// Set images for book
		newResult.setImageSmall(OpenLibraryUrlConverter.createCoverImageUrl(openLibraryTitle, ImageSize.SMALL));
		newResult.setImageMedium(OpenLibraryUrlConverter.createCoverImageUrl(openLibraryTitle, ImageSize.MEDIUM));
		newResult.setImageLarge(OpenLibraryUrlConverter.createCoverImageUrl(openLibraryTitle, ImageSize.LARGE));
		
		// set work url
	  newResult.setOpenlibraryWorkUrl(OpenLibraryUrlConverter.createWorkUrl(openLibraryTitle.getKey()));
	  
	  // Get Description if possible
	  String description = OpenLibraryHelper.getDescriptionForTitle(openLibraryTitle.getKey());
	  newResult.setDescription(description);	  
		
		// the rest
		newResult.setSubjects(openLibraryTitle.getSubject());
		if (!openLibraryTitle.getAuthor_key().isEmpty())
			newResult.setAuthorKey(openLibraryTitle.getAuthor_key().get(0));
		if (!openLibraryTitle.getAuthor_name().isEmpty())
			newResult.setAuthorName(openLibraryTitle.getAuthor_name().get(0));
		newResult.setFirstPublishedYear(openLibraryTitle.getFirst_publish_year());
		newResult.setIsbns(openLibraryTitle.getIsbn());
		newResult.setOpenLibraryKeys(openLibraryTitle.getEdition_key());

		System.out.println("converted to: " + newResult);
		
		return newResult;
	}


	
}
