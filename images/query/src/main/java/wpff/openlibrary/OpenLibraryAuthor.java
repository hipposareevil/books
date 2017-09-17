package wpff.openlibrary;
	
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Author bean that stores data retrieved from openlibrary.org.
 * 
 * Will be retrieved through:
 * 'https://openlibrary.org/search/authors?q=AUTHORNAME'
 * 
 * @see AuthorDocs
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenLibraryAuthor {


	private String name;
	private String key;
	private List<String> top_subjects;
	private String birth_date;
	
	
	public List<String> getTop_subjects() {
		return top_subjects;
	}

	/**
	 * Set the subjects for this author.
	 * Remove 'protected daisy' and 'accessible book'
	 * 
	 * @param top_subjects
	 */
	public void setTop_subjects(List<String> top_subjects) {
		// Remove "Protected DAISY" and others
		List<String> subjects = top_subjects.stream()
				.filter(t -> !(t.contains("Protected DAISY") || 
						(t.contains("Accessible book"))))
				.collect(Collectors.toList());

		this.top_subjects = subjects;
	}

	public String getBirth_date() {
		return birth_date;
	}

	public void setBirth_date(String birth_date) {
		this.birth_date = birth_date;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OpenLibraryAuthor [name=");
		builder.append(name);
		builder.append(", key=");
		builder.append(key);
		builder.append(", top_subjects=");
		builder.append(top_subjects);
		builder.append(", birth_date=");
		builder.append(birth_date);
		builder.append("]");
		return builder.toString();
	}
	

	

}
