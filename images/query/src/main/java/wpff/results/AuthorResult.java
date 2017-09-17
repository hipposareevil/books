package wpff.results;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean representing an Author. 
 * This is filled w/ data from the OpenLibraryAuthor
 *
 */
public class AuthorResult {

	private String name;
	private String key;
	private List<String> subjects = new ArrayList<String>();
	private String birthDate;
	private String authorImageSmall;
	private String authorImageMedium;
	private String authorImageLarge;
	

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

	public List<String> getSubjects() {
		return subjects;
	}

	public void setSubjects(List<String> subjects) {
		if (subjects != null)
		this.subjects.addAll(subjects);
	}

	public String getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}

	
	/**
	 * @return the authorImageSmall
	 */
	public String getAuthorImageSmall() {
		return authorImageSmall;
	}

	/**
	 * @param authorImageSmall the authorImageSmall to set
	 */
	public void setAuthorImageSmall(String authorImageSmall) {
		this.authorImageSmall = authorImageSmall;
	}

	/**
	 * @return the authorImageMedium
	 */
	public String getAuthorImageMedium() {
		return authorImageMedium;
	}

	/**
	 * @param authorImageMedium the authorImageMedium to set
	 */
	public void setAuthorImageMedium(String authorImageMedium) {
		this.authorImageMedium = authorImageMedium;
	}

	/**
	 * @return the authorImageLarge
	 */
	public String getAuthorImageLarge() {
		return authorImageLarge;
	}

	/**
	 * @param authorImageLarge the authorImageLarge to set
	 */
	public void setAuthorImageLarge(String authorImageLarge) {
		this.authorImageLarge = authorImageLarge;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AuthorResult [name=");
		builder.append(name);
		builder.append(", key=");
		builder.append(key);
		builder.append(", subjects=");
		builder.append(subjects);
		builder.append(", birthDate=");
		builder.append(birthDate);
		builder.append("]");
		return builder.toString();
	}


}
