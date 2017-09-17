package wpff.openlibrary;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Wrapper for Titles when parsing data from openlibrary:
 *  'https://openlibrary.org/search?title=X&author=Y'
 *  The returned json starts with:
 *  <pre>
 *  {
    "docs": [
        {
           "title_suggest": ".....
 *  </pre>
 * So this class gets us to the array of Titles
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class TitleDocs {
	List<OpenLibraryTitle> doc;
	
	int numFound;

	public int getNumFound() {
		return numFound;
	}

	public void setNumFound(int numFound) {
		this.numFound = numFound;
	}

	
	public List<OpenLibraryTitle> getDocs() {
		return doc;
	}

	public void setDocs(List<OpenLibraryTitle> titles) {
		this.doc = titles;
	}
}
