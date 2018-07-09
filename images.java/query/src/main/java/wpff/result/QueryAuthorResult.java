package wpff.result;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean representing an Author. 
 * This is filled w/ data from the OpenLibraryAuthor
 *
 */
public class QueryAuthorResult {

	private String name;
	private String olKey;
	private List<String> subjects = new ArrayList<String>();
	private String birthDate;
	private String imageSmall;
	private String imageMedium;
	private String imageLarge;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOlKey() {
		return olKey;
	}

	public void setOlKey(String key) {
		this.olKey = key;
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

	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AuthorResult [name=");
		builder.append(name);
		builder.append(", key=");
		builder.append(olKey);
		builder.append(", subjects=");
		builder.append(subjects);
		builder.append(", birthDate=");
		builder.append(birthDate);
		builder.append("]");
		return builder.toString();
	}

  /**
   * @return the imageSmall
   */
  public String getImageSmall() {
    return imageSmall;
  }

  /**
   * @param imageSmall the imageSmall to set
   */
  public void setImageSmall(String imageSmall) {
    this.imageSmall = imageSmall;
  }

  /**
   * @return the imageMedium
   */
  public String getImageMedium() {
    return imageMedium;
  }

  /**
   * @param imageMedium the imageMedium to set
   */
  public void setImageMedium(String imageMedium) {
    this.imageMedium = imageMedium;
  }

  /**
   * @return the imageLarge
   */
  public String getImageLarge() {
    return imageLarge;
  }

  /**
   * @param imageLarge the imageLarge to set
   */
  public void setImageLarge(String imageLarge) {
    this.imageLarge = imageLarge;
  }


}
