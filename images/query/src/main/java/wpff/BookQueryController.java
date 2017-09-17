package wpff;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Swagger
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import wpff.google.AuthorQueryBean;
import wpff.google.BookQueryBean;
import wpff.google.QueryGoogle;
import wpff.openlibrary.ImageUrlCreator;
import wpff.openlibrary.ImageUrlCreator.ImageSize;
import wpff.openlibrary.OpenLibraryAuthor;
import wpff.openlibrary.OpenLibraryTitle;
import wpff.openlibrary.QueryOpenLibrary;
import wpff.results.AuthorResult;
import wpff.results.TitleResult;


@Api( value="/query",
      tags= "Query",
      description="Queries openlibrary.org for books")
@RequestMapping("/query")
@RestController
public class BookQueryController {

	//////////////////////////////////////////////////////////////
	//
	// Google queries

	/**
	 * google api key, injected via spring
	 */
	@Value("${googleapikey:#{null}}")
	private String googleApiKey;

	/**
	 * /query/g.title endpoint. Queries google for books
	 *
	 * @param author
	 *            Name (or partial) of author
	 * @param title
	 *            Book title (or partial)
	 * @return list of matching Books
	 */
	@ApiOperation(value = "/g.title", nickname = "query titles",
			notes = "Query google for book titles. A title will have a set of ids that corresond to ISBN 10 or ISBN 13 values. ")
	@ApiImplicitParams({ @ApiImplicitParam(name = "author", value = "Author's name", required = false,
			dataType = "string", paramType = "query"), @ApiImplicitParam(name = "title", value = "Book Title",
					required = false, dataType = "string", paramType = "query") })
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success", response = BookQueryBean.class) }
	)
	@RequestMapping(method = RequestMethod.GET, path = "/g.title", produces = "application/json")
	public List<BookQueryBean> query(@RequestParam(value = "author") String author, @RequestParam(
			value = "title") String title) {
		// Query google
		return QueryGoogle.getBooks(googleApiKey, author, title);
	}

	/**
	 * /query/g.author endpoint. Queries google for author names
	 *
	 * @param author
	 *            Name (or partial) of author
	 * @return list of matching author
	 */
	@ApiOperation(value = "/g.author", nickname = "query author",
			notes = "Query google for authors. Returns list of Author names.")
	@ApiImplicitParams({ @ApiImplicitParam(name = "author", value = "Author's name", required = false,
			dataType = "string", paramType = "query") })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = AuthorQueryBean.class) })
	@RequestMapping(method = RequestMethod.GET, path = "/g.author", produces = "application/json")
	public List<AuthorQueryBean> query(@RequestParam(value = "author") String author) {
		// Query google
		return QueryGoogle.getAuthor(googleApiKey, author);
	}
	
	////////////////////////////////////////////////////////
	//
	// Open Library calls
	// 

	/**
	 * /query/author endpoint.  Will make query to OpenLibrary.org.
	 * @param authorQuery
	 * Name (or partial name) of author
	 * @return List of matching Authors
	 * @throws UnsupportedEncodingException
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	@ApiOperation(value = "/author", nickname = "query author",
			notes = "Query openlibrary.org for authors. Returns list of authors.")
	@ApiImplicitParams({ @ApiImplicitParam(name = "author", value = "Author's name", required = false,
			dataType = "string", paramType = "query") })
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success", response = AuthorResult.class, responseContainer="List") 
			})
	@RequestMapping(method = RequestMethod.GET, path = "/author", produces = "application/json")
	public List<AuthorResult> queryForAuthor(@RequestParam(value = "author") String authorQuery)
			throws UnsupportedEncodingException, IllegalAccessException, InvocationTargetException {
		// Begin
		List<OpenLibraryAuthor> authors = QueryOpenLibrary.queryForAuthors(authorQuery);
		if (authors.size() == 0) {
			// No authors, try with an asterix
			authors = QueryOpenLibrary.queryForAuthors(authorQuery);
		}
		
		System.out.println("");
		System.out.println("We got " + authors.size() + " authors back.");
		for (OpenLibraryAuthor a : authors) {
			System.out.println(" >> " + a);
		}
		

		// Convert to AuthorResult. This is done as the OpenLibraryAuthor has strange
		// field names due to the JSON returned from openlibrary.org
		List<AuthorResult> results = new ArrayList<AuthorResult>();
		for (OpenLibraryAuthor current : authors) {
			AuthorResult newResult = convertToResult(current);
			results.add(newResult);
		}
		return results;
	}


	/**
	 * /query/title endpoint. Queries openlibrary for books
	 *
	 * @param author
	 *            Name (or partial) of author
	 * @param title
	 *            Book title (or partial)
	 * @return list of matching Books
	 * @throws IOException 
	 */
	@ApiOperation(value = "/title", nickname = "query titles",
			notes = "Query openlibrary for book titles. Results are sorted by the number of ISBNs per book. So the first titles in the resulting list will be the ones with more associated ISBNS")
	@ApiImplicitParams({ @ApiImplicitParam(name = "author", value = "Author's name", required = false,
			dataType = "string", paramType = "query"), @ApiImplicitParam(name = "title", value = "Book Title",
					required = false, dataType = "string", paramType = "query") })
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success", response = TitleResult.class, responseContainer="List") 
                  })
	@RequestMapping(method = RequestMethod.GET, path = "/title", produces = "application/json")
	public List<TitleResult> queryForTitles(@RequestParam(value = "author") String author, 
											@RequestParam(value = "title") String title) throws IOException {
		// Begin
		List<OpenLibraryTitle> titles = QueryOpenLibrary.queryForTitles(author, title);

		// Convert
		List<TitleResult> results = new ArrayList<TitleResult>();
		
		for (OpenLibraryTitle current : titles) {
			TitleResult newResult = convertToResult(current);
			results.add(newResult);
		}		
		
		Collections.sort(results);
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
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	private AuthorResult convertToResult(OpenLibraryAuthor author) throws IllegalAccessException,
			InvocationTargetException {
		AuthorResult newResult = new AuthorResult();

		BeanUtils.copyProperties(newResult, author);
		BeanUtils.copyProperty(newResult, "birthDate", author.getBirth_date());
		newResult.setSubjects(author.getTop_subjects());

		// Set images for the author
		newResult.setAuthorImageSmall(ImageUrlCreator.createAuthorImageUrl(author.getKey(), ImageSize.SMALL));
		newResult.setAuthorImageMedium(
				ImageUrlCreator.createAuthorImageUrl(author.getKey(), ImageSize.MEDIUM));
		newResult.setAuthorImageLarge(ImageUrlCreator.createAuthorImageUrl(author.getKey(), ImageSize.LARGE));

		return newResult;
	}

	/**
	 * Convert an OpenLibrary object to normal bean
	 * 
	 * @param openLibraryTitle
	 *            Title to convert
	 * @return
	 */
	private TitleResult convertToResult(OpenLibraryTitle openLibraryTitle) {
		TitleResult newResult = new TitleResult();

		newResult.setTitle(openLibraryTitle.getTitle_suggest());
		
		// Set images for book
		newResult.setCoverImageSmall(ImageUrlCreator.createCoverImageUrl(openLibraryTitle, ImageSize.SMALL));
		newResult.setCoverImageMedium(ImageUrlCreator.createCoverImageUrl(openLibraryTitle, ImageSize.MEDIUM));
		newResult.setCoverImageLarge(ImageUrlCreator.createCoverImageUrl(openLibraryTitle, ImageSize.LARGE));
				
		newResult.setSubjects(openLibraryTitle.getSubject());
		if (!openLibraryTitle.getAuthor_key().isEmpty())
			newResult.setAuthorKey(openLibraryTitle.getAuthor_key().get(0));
		if (!openLibraryTitle.getAuthor_name().isEmpty())
			newResult.setAuthorName(openLibraryTitle.getAuthor_name().get(0));
		newResult.setWorksKey(openLibraryTitle.getKey());
		newResult.setFirstPublishedYear(openLibraryTitle.getFirst_publish_year());
		newResult.setIsbns(openLibraryTitle.getIsbn());
		newResult.setOpenLibraryKeys(openLibraryTitle.getEdition_key());

		return newResult;
	}


	
}
