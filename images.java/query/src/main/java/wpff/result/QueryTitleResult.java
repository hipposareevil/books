package wpff.result;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean representing a title.
 * This is filled w/ data from the OpenLibraryTitle
 *
 */
public class QueryTitleResult implements Comparable {

	/**
	 * Title of book
	 */
	private String title;

	/**
	 * Cover image - small
	 */
	private String imageSmall;
	/**
	 * Cover image - medium
	 */
	private String imageMedium;
	/**
	 * Cover image - large
	 */
	private String imageLarge;

	/**
	 * List of subjects this book covers
	 */
	private List<String> subjects = new ArrayList<String>();
	
	/**
	 * Key into openlibrary for the author. Can be used for querying for images.
	 * 
	 * 'http://covers.openlibrary.org/a/olid/<authorKey>-M.jpg'
	 */
	private String authorKey;
	
	/**
	 * Name of author
	 */
	private String authorName;
	
	/**
	 * Description
	 */
	private String description;
	
	/**
	 * OpenLibrary key to show the 'works' of the book. Is the top level 'book' for a given title
	 */
	private String openlibraryWorkUrl;
	
	/**
	 * First year of publication
	 */
	private Integer firstPublishedYear;
	
	/**
	 * List of ISBNs
	 */
	private List<String> isbns = new ArrayList<String>();
	
	/**
	 * Set of edition keys for the books in openlibrary
	 */
	private List<String> openLibraryKeys = new ArrayList<String>();
	
	//////////////////////////////////////////////////

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}


	/**
	 * @return the subjects
	 */
	public List<String> getSubjects() {
		return subjects;
	}

	/**
	 * @param subjects the subjects to set
	 */
	public void setSubjects(List<String> subjects) {
		if (subjects != null)
		this.subjects.addAll(subjects);
	}

	/**
	 * @return the authorKey
	 */
	public String getAuthorKey() {
		return authorKey;
	}

	/**
	 * @param authorKey the authorKey to set
	 */
	public void setAuthorKey(String authorKey) {
		this.authorKey = authorKey;
	}

	/**
	 * @return the authorName
	 */
	public String getAuthorName() {
		return authorName;
	}

	/**
	 * @param authorName the authorName to set
	 */
	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	

	/**
	 * @return the firstPublishedYear
	 */
	public Integer getFirstPublishedYear() {
		return firstPublishedYear;
	}

	/**
	 * @param firstPublishedYear the firstPublishedYear to set
	 */
	public void setFirstPublishedYear(Integer firstPublishedYear) {
		this.firstPublishedYear = firstPublishedYear;
	}

	/**
	 * @return the isbns
	 */
	public List<String> getIsbns() {
		return isbns;
	}

	/**
	 * @param isbns the isbns to set
	 */
	public void setIsbns(List<String> isbns) {
		if (isbns != null)
		this.isbns.addAll(isbns);
	}

	/**
	 * @return the openLibraryKeys
	 */
	public List<String> getOpenLibraryKeys() {
		return openLibraryKeys;
	}

	/**
	 * @param openLibraryKeys the openLibraryKeys to set
	 */
	public void setOpenLibraryKeys(List<String> openLibraryKeys) {
		if (openLibraryKeys != null)
		this.openLibraryKeys.addAll(openLibraryKeys);
	}

	  /**
   * @return the openlibraryWorkUrl
   */
  public String getOpenlibraryWorkUrl() {
    return openlibraryWorkUrl;
  }

  /**
   * @param openlibraryWorkUrl the openlibraryWorkUrl to set
   */
  public void setOpenlibraryWorkUrl(String openlibraryWorkUrl) {
    this.openlibraryWorkUrl = openlibraryWorkUrl;
  } 
  
  
  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }
	

	/**
	 * Compare TitleResults. This is solely dependent on the number of ISBNs.
	 * The more ISBNS means higher ranking
	 */
	@Override
	public int compareTo(Object o) {
		if (o == null) {
			throw new NullPointerException("Unable to compare a TitleResult to null.");
		}
		if (this == o)
			return 0;
		if (getClass() != o.getClass())
			throw new RuntimeException("Unable to compare a TitleResult to class: " + o.getClass());
					
		QueryTitleResult other = (QueryTitleResult) o;
				
		int thisSize = this.getIsbns().size();
		int thatSize = other.getIsbns().size();
		
		return Integer.compare(thatSize, thisSize);
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

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("QueryTitleResult [title=");
    builder.append(title);
    builder.append(", imageSmall=");
    builder.append(imageSmall);
    builder.append(", imageMedium=");
    builder.append(imageMedium);
    builder.append(", imageLarge=");
    builder.append(imageLarge);
    builder.append(", subjects=");
    builder.append(subjects);
    builder.append(", authorKey=");
    builder.append(authorKey);
    builder.append(", authorName=");
    builder.append(authorName);
    builder.append(", description=");
    builder.append(description);
    builder.append(", openlibraryWorkUrl=");
    builder.append(openlibraryWorkUrl);
    builder.append(", firstPublishedYear=");
    builder.append(firstPublishedYear);
    builder.append(", isbns=");
    builder.append(isbns);
    builder.append(", openLibraryKeys=");
    builder.append(openLibraryKeys);
    builder.append("]");
    return builder.toString();
  }

	
}
