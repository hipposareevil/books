package wpff.openlibrary;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import wpff.openlibrary.beans.AuthorDocs;
import wpff.openlibrary.beans.OpenLibraryAuthor;
import wpff.openlibrary.beans.OpenLibraryTitle;
import wpff.openlibrary.beans.TitleDocs;
import wpff.openlibrary.beans.WorksBean;

/**
 * Class to query the OpenLibrary for authors and titles.
 *
 */
public class OpenLibraryHelper {
	
	/**
	 * Base URL at openlibrary for author queries.
	 */
	private static final String authorBaseUrl = "https://openlibrary.org/search/authors?q=";

	/**
	 * Base URL at openlibrary for title queries.
	 */
	private static final String titleBaseUrl = "https://openlibrary.org/search?";
	
	/**
	 * Base URL at openlibrary for 'works' queries
	 */
	private static final String worksBaseUrl = "https://openlibrary.org/";

 
	/**
	 * Query OpenLibrary for list of authors
	 * 
	 * @param authorQuery
	 *            author name
	 * @return List of Author beans
	 * @throws UnsupportedEncodingException
	 */
	public static List<OpenLibraryAuthor> queryForAuthors(String authorQuery) throws UnsupportedEncodingException {
		String queryUrl = authorBaseUrl + authorQuery;
		System.out.println("making query to: " + queryUrl);
				
		RestTemplate restTemplate = new RestTemplate();
/*
		// Set up headers for request
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

		ResponseEntity<String> authorDocx = restTemplate.exchange(
				queryUrl, HttpMethod.GET, entity, String.class);
//		System.out.println("XXXXXXXXXXXX " + authorDocx.getHeaders());
		System.out.println("YYYYYYYYYYYYY " + authorDocx.getBody());
*/		

		//ResponseEntity<Author[]> authors= restTemplate.getForEntity(queryUrl,
		// Author[].class);
		

		AuthorDocs authorDoc = restTemplate.getForObject(queryUrl, AuthorDocs.class);
		/*				
		ResponseEntity<String> response = restTemplate.getForEntity(queryUrl, String.class);
		//System.out.println(response.getBody());
		System.out.println(response.getHeaders());
		System.out.println(response.getStatusCode());
		*/
        		
		//System.out.println("NUM: " + authorDoc.getBody().getNumFound());
				
		List<OpenLibraryAuthor> authors = authorDoc.getDocs();
		return authors;
	}
	
		
	/**
	 * Query OpenLibrary for list of books
	 * 
	 * @param author
	 *            Author name, or partial
	 * @param title
	 *            Title name, or partial
   * @param isbn
	 *            ISBN of book
	 * @return List of Title beans
	 */
	public static List<OpenLibraryTitle> queryForTitles(String author, String title, String isbn) throws IOException {
	  // construct query
	  String queryUrl = titleBaseUrl;
	  
	  // Adding the "&" if there are more than 1 params
	  String and = "";
	  if (title!= null && !title.isEmpty()) {
	    queryUrl += and + "title=" + title;
	    and = "&";
	  }
	  if (author != null && ! author.isEmpty())  {
	    queryUrl += and + "author=" + author;
	    and = "&";
	  }
	  if (isbn!= null && ! isbn.isEmpty()) {
	    queryUrl += and + "isbn=" + isbn;
	    and = "&";
	  }
		
		RestTemplate restTemplate = new RestTemplate();
		
		// Set headers
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
		
		// Make query
		ResponseEntity<String> response = restTemplate.exchange(queryUrl, HttpMethod.GET, entity, String.class);
		
		// Map the String response into a TitleDocs
		ObjectMapper mapper = new ObjectMapper();
		TitleDocs doc = null;
		doc = mapper.readValue(response.getBody(), TitleDocs.class);
			
		return doc.getDocs();
	}
	
	
	 /**
   * Query openlibrary for a description for a title. This will take a 'works'
   * URL/key and get description
   * 
   * @param worksKey
   *          works key
   * @return Description of book, or "" if none exists
   */
	public static String getDescriptionForTitle(String worksKey) {
	  	  // construct query
	  String queryUrl = worksBaseUrl + worksKey;
	  
	  System.out.println("Description going to url: " + queryUrl);
	   RestTemplate restTemplate = new RestTemplate(); 
		
		// Set headers
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
		
		// Make query
		ResponseEntity<String> response = restTemplate.exchange(queryUrl, HttpMethod.GET, entity, String.class);
		
		// Map the String response into a TitleDocs
		ObjectMapper mapper = new ObjectMapper();
    WorksBean bean = null;
    try {
      bean = mapper.readValue(response.getBody(), WorksBean.class);
    } catch (IOException ioe) {
      ioe.printStackTrace();
      return "";
    }
			
		return bean.getTextDescription();
	}
	
}
