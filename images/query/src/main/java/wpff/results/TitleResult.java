package wpff.results;

import java.util.List;

/**
 * Bean representing a title.
 * This is filled w/ data from the OpenLibraryTitle
 *
 */
public class TitleResult {

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
	private List<String> subjects;
	
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
	private String worksKey;
	
	/**
	 * First year of publication
	 */
	private Integer firstPublishedYear;
	
	/**
	 * List of ISBNs
	 */
	private List<String> isbns;
	
	/**
	 * Set of edition keys for the books in openlibrary
	 */
	private List<String> openLibraryKeys;
	
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
		this.subjects = subjects;
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
	 * @return the worksKey
	 */
	public String getWorksKey() {
		return worksKey;
	}

	/**
	 * @param worksKey the worksKey to set
	 */
	public void setWorksKey(String worksKey) {
		this.worksKey = worksKey;
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
		this.isbns = isbns;
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
		this.openLibraryKeys = openLibraryKeys;
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
	
	

}
