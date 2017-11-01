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
public class FullUserBook extends PostUserBook  implements Comparable {
	private int userBookId;

	private int userId;
	
	private String title;
	
	private Date dateAdded;

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

}
