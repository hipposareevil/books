package wpff.openlibrary.beans;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Title bean from openlibrary.org
 * 
 * Will be retrieved through:
 * 'https://openlibrary.org/search?title=X&author=Y'
 * 
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class OpenLibraryTitle implements Comparable {
  
	// Title
	private String title_suggest;

	// Cover image
	private String cover_i;

	// author key
	private List<String> author_key = new ArrayList<String>();

	// author name
	private List<String> author_name = new ArrayList<String>();

	// subject
	private List<String> subject;

	// works key (main page for the book)
	private String key;

	// years of publication
	private List<Integer> publish_year;

	// first year of publication
	private Integer first_publish_year;

	// isbns
	private List<String> isbn;

	// openlibrary ids
	private List<String> edition_key;

	////////////////////////////////////////////////////
	
	public String getTitle_suggest() {
		return title_suggest;
	}

	public void setTitle_suggest(String title_suggest) {
		this.title_suggest = title_suggest;
	}

	public String getCover_i() {
		return cover_i;
	}

	public void setCover_i(String cover_i) {
		this.cover_i = cover_i;
	}

	public List<String> getAuthor_key() {
		return author_key;
	}

	public void setAuthor_key(List<String> author_key) {
		this.author_key.addAll(author_key);
	}

	public List<String> getAuthor_name() {
		return author_name;
	}

	public void setAuthor_name(List<String> author_name) {
		this.author_name.addAll(author_name);
	}

	public Integer getFirst_publish_year() {
		return first_publish_year;
	}

	public void setFirst_publish_year(Integer first_publish_year) {
		this.first_publish_year = first_publish_year;
	}

	public List<String> getEdition_key() {
		return edition_key;
	}

	public void setEdition_key(List<String> edition_key) {
		this.edition_key = edition_key;
	}

	public List<String> getSubject() {
		return subject;
	}

	public void setSubject(List<String> subject) {
			// Remove "Protected DAISY"	
		List<String> subjects = subject.stream()
				.filter(t -> !(t.contains("Protected DAISY") || 
						(t.contains("In library")) || 
						(t.contains("Accessible book"))))
				.collect(Collectors.toList());

		this.subject = subjects;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public List<Integer> getPublish_year() {
		return publish_year;
	}

	public void setPublish_year(List<Integer> publish_year) {
		this.publish_year = publish_year;
	}

	public List<String> getIsbn() {
		return isbn;
	}

	public void setIsbn(List<String> isbn) {
		this.isbn = isbn;
	}


    
  @Override
  public int compareTo(Object o) {
    OpenLibraryTitle other = (OpenLibraryTitle) o;
    return new CompareToBuilder().append(this.key, other.key)
        .append(this.edition_key, other.edition_key)
        .append(this.title_suggest, other.title_suggest)
        .append(this.cover_i, other.cover_i)
        .append(this.isbn, other.isbn)
        .append(this.publish_year, other.publish_year)
        .toComparison();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("OpenLibraryTitle [title_suggest=");
    builder.append(title_suggest);
    builder.append(", cover_i=");
    builder.append(cover_i);
    builder.append(", author_key=");
    builder.append(author_key);
    builder.append(", author_name=");
    builder.append(author_name);
    builder.append(", subject=");
    builder.append(subject);
    builder.append(", key=");
    builder.append(key);
    builder.append(", publish_year=");
    builder.append(publish_year);
    builder.append(", first_publish_year=");
    builder.append(first_publish_year);
    builder.append(", isbn=");
    builder.append(isbn);
    builder.append(", edition_key=");
    builder.append(edition_key);
    builder.append("]");
    return builder.toString();
  }

}
