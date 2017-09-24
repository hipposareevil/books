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
	private String coverImageSmall;
	/**
	 * Cover image - medium
	 */
	private String coverImageMedium;
	/**
	 * Cover image - large
	 */
	private String coverImageLarge;

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
	 * @return the coverImageSmall
	 */
	public String getCoverImageSmall() {
		return coverImageSmall;
	}

	/**
	 * @param coverImageSmall the coverImageSmall to set
	 */
	public void setCoverImageSmall(String coverImageSmall) {
		this.coverImageSmall = coverImageSmall;
	}

	/**
	 * @return the coverImageMedium
	 */
	public String getCoverImageMedium() {
		return coverImageMedium;
	}

	/**
	 * @param coverImageMedium the coverImageMedium to set
	 */
	public void setCoverImageMedium(String coverImageMedium) {
		this.coverImageMedium = coverImageMedium;
	}

	/**
	 * @return the coverImageLarge
	 */
	public String getCoverImageLarge() {
		return coverImageLarge;
	}

	/**
	 * @param coverImageLarge the coverImageLarge to set
	 */
	public void setCoverImageLarge(String coverImageLarge) {
		this.coverImageLarge = coverImageLarge;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((authorKey == null) ? 0 : authorKey.hashCode());
		result = prime * result + ((authorName == null) ? 0 : authorName.hashCode());
		result = prime * result + ((coverImageLarge == null) ? 0 : coverImageLarge.hashCode());
		result = prime * result + ((coverImageMedium == null) ? 0 : coverImageMedium.hashCode());
		result = prime * result + ((coverImageSmall == null) ? 0 : coverImageSmall.hashCode());
		result = prime * result + ((firstPublishedYear == null) ? 0 : firstPublishedYear.hashCode());
		result = prime * result + ((isbns == null) ? 0 : isbns.hashCode());
		result = prime * result + ((openLibraryKeys == null) ? 0 : openLibraryKeys.hashCode());
		result = prime * result + ((subjects == null) ? 0 : subjects.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((openlibraryWorkUrl == null) ? 0 : openlibraryWorkUrl.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueryTitleResult other = (QueryTitleResult) obj;
		if (authorKey == null) {
			if (other.authorKey != null)
				return false;
		} else if (!authorKey.equals(other.authorKey))
			return false;
		if (authorName == null) {
			if (other.authorName != null)
				return false;
		} else if (!authorName.equals(other.authorName))
			return false;
		if (coverImageLarge == null) {
			if (other.coverImageLarge != null)
				return false;
		} else if (!coverImageLarge.equals(other.coverImageLarge))
			return false;
		if (coverImageMedium == null) {
			if (other.coverImageMedium != null)
				return false;
		} else if (!coverImageMedium.equals(other.coverImageMedium))
			return false;
		if (coverImageSmall == null) {
			if (other.coverImageSmall != null)
				return false;
		} else if (!coverImageSmall.equals(other.coverImageSmall))
			return false;
		if (firstPublishedYear == null) {
			if (other.firstPublishedYear != null)
				return false;
		} else if (!firstPublishedYear.equals(other.firstPublishedYear))
			return false;
		if (isbns == null) {
			if (other.isbns != null)
				return false;
		} else if (!isbns.equals(other.isbns))
			return false;
		if (openLibraryKeys == null) {
			if (other.openLibraryKeys != null)
				return false;
		} else if (!openLibraryKeys.equals(other.openLibraryKeys))
			return false;
		if (subjects == null) {
			if (other.subjects != null)
				return false;
		} else if (!subjects.equals(other.subjects))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (openlibraryWorkUrl == null) {
			if (other.openlibraryWorkUrl != null)
				return false;
		} else if (!openlibraryWorkUrl.equals(other.openlibraryWorkUrl))
			return false;
		return true;
	}



}
