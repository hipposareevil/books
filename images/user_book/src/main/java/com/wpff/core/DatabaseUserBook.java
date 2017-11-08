package com.wpff.core;

import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * DatabaseUserBook represents a book for a user in the database. It contains:
 *
 * Tags are not stored in this class/table.
 */
@Entity
@Table(name = "userbook")
public class DatabaseUserBook {

	/**
	 * UserBook ID.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_book_id", unique = true, nullable = false)
	private int userBookId;

	/**
	 * ID of user
	 */
	@Column(name = "user_id", unique = false, nullable = false)
	private int user_id;

	/**
	 * ID of book
	 */
	@Column(name = "book_id", unique = false, nullable = false)
	private int book_id;

	/**
	 * Rating of book. false=down/true=up
	 */
	@Column(name = "rating", unique = false, nullable = false)
	private boolean rating;

	/**
	 * Date user book was added
	 */
	@Column(name = "date_added", nullable = true)
	private Date date_added;

	/**
	 * Default constructor
	 */
	public DatabaseUserBook() {
	}

	public DatabaseUserBook(int id, int user_id, int bookId, boolean rating) {
		this.userBookId = id;
		this.user_id = user_id;
		this.book_id = bookId;
		this.rating = rating;
	}

	public String toString() {
		StringBuilder string = new StringBuilder();
		string.append("UserBook[");
		string.append("id=" + userBookId);
		string.append(", user_id=" + user_id);
		string.append(", bookId=" + book_id);
		string.append(", rating=" + rating);
		string.append("]");

		return string.toString();
	}

	public void setUserBookId(int id) {
		this.userBookId = id;
	}

	public int getUserBookId() {
		return this.userBookId;
	}

	public void setBookId(int bookId) {
		this.book_id = bookId;
	}

	public int getBookId() {
		return this.book_id;
	}

	public void setUserId(int user_id) {
		this.user_id = user_id;
	}

	public int getUserId() {
		return this.user_id;
	}

	

	public void setRating(boolean rating) {
		this.rating = rating;
	}

	public boolean getRating() {
		return rating;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof DatabaseUserBook)) {
			return false;
		}

		final DatabaseUserBook that = (DatabaseUserBook) o;

		return Objects.equals(this.userBookId, that.userBookId) && Objects.equals(this.user_id, that.user_id)
				&& Objects.equals(this.book_id, that.book_id) && Objects.equals(this.rating, that.rating);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.userBookId, this.book_id, this.user_id, this.rating);
	}

  /**
   * @return the user_id
   */
  public int getUser_id() {
    return user_id;
  }

  /**
   * @param user_id the user_id to set
   */
  public void setUser_id(int user_id) {
    this.user_id = user_id;
  }

  /**
   * @return the dateAdded
   */
  public Date getDateAdded() {
    return date_added;
  }

  /**
   * @param dateAdded the dateAdded to set
   */
  public void setDateAdded(Date dateAdded) {
    this.date_added = dateAdded;
  }
}
