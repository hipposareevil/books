package com.wpff.core.beans;

import java.util.Arrays;
import java.util.Date;

/**
 * UserBook bean that returned from various methods. It adds the 'userBookId'
 * and 'userId' to the PostUserBook class.
 *
 * This is used instead of normal UserBook as UserBook has no tags.
 *
 */
@SuppressWarnings("rawtypes")
public class FullUserBook extends PostUserBook  implements Comparable {
  /**
   * ID of this entity
   */
	private int userBookId;

	/**
	 * User id
	 */
	private int userId;
	
	/**
	 * Date added
	 */
	private Date dateAdded;
		
	/**
	 * title (stored in Book)
	 */
	private String title;
	
	/**
	 * author (stored in Book)
	 */
	private String authorName;
	
	/**
	 * author ID (stored in Book)
	 */
	private int authorId;
	
	/**
	 * Year of book publication (stored in Book)
	 */
	private int firstPublishedYear;
	
	
  	private String imageSmall;

  	private String imageMedium;

  	private String imageLarge;


	public String toString() {
		StringBuilder string = new StringBuilder();
		string.append("FullUserBook[");
		string.append("userBookId=" + userBookId);
		string.append(", userId=" + userId);
		string.append(", bookId=" + super.bookId);
		string.append(", rating=" + super.rating);
		if (tags != null)
		string.append(", tags=" + Arrays.toString(tags.toArray()));
		else 
		  string.append(", tags=null");
		string.append("]");

		return string.toString();
	}

	public void setUserBookId(int id) {
		this.userBookId = id;
	}

	public int getUserBookId() {
		return this.userBookId;
	}

	public void setUserId(int user_id) {
		this.userId = user_id;
	}

	public int getUserId() {
		return this.userId;
	}

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return the dateAdded
   */
  public Date getDateAdded() {
    return dateAdded;
  }

  /**
   * @param dateAdded the dateAdded to set
   */
  public void setDateAdded(Date dateAdded) {
    this.dateAdded = dateAdded;
  }

  @Override
  public int compareTo(Object o) {
   final FullUserBook that = (FullUserBook) o;
    if (this == that) return 0;

    return (this.userBookId- that.userBookId);
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
  public int getFirstPublishedYear() {
    return firstPublishedYear;
  }

  /**
   * @param firstPublishedYear the firstPublishedYear to set
   */
  public void setFirstPublishedYear(int firstPublishedYear) {
    this.firstPublishedYear = firstPublishedYear;
  }

  /**
   * @return the authorId
   */
  public int getAuthorId() {
    return authorId;
  }

  /**
   * @param authorId the authorId to set
   */
  public void setAuthorId(int authorId) {
    this.authorId = authorId;
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
