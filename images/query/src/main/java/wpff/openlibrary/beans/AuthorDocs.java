package wpff.openlibrary.beans;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Wrapper for Authors when parsing data from openlibrary:
 *  'https://openlibrary.org/search/authors?q=AUTHORNAME*'
 *  The returned json starts with:
 *  <pre>
 *  {
    "docs": [
        {
         "name" : "author"....
 *  </pre>
 * So this class gets us to the array of authors.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class AuthorDocs {
	List<OpenLibraryAuthor> doc;
	
	int numFound;

	public int getNumFound() {
		return numFound;
	}

	public void setNumFound(int numFound) {
		this.numFound = numFound;
	}

	public List<OpenLibraryAuthor> getDocs() {
		return doc;
	}

	public void setDocs(List<OpenLibraryAuthor> authors) {
			this.doc = authors;
	}
	

}
