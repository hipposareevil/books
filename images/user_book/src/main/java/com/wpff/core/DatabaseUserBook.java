package com.wpff.core;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * DatabaseUserBook represents a book for a user in the database. It contains:
 * <ul>
 * <li>user book id (ID of this entity)
 * <li>- userID (ID in user table)
 * <li>- bookID (ID in book table)
 * <li>rating of book by the user (up or down)
 * <li>extra data
 *
 * </ul>
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
	private int bookId;

	/**
	 * Rating of book. false=down/true=up
	 */
	@Column(name = "rating", unique = false, nullable = false)
	private boolean rating;

	/**
	 * Data for the UserBook. May be empty
	 */
	@Column(name = "data", nullable = true)
	private String data;

	/**
	 * Default constructor
	 */
	public DatabaseUserBook() {
	}

	public DatabaseUserBook(int id, int user_id, int bookId, boolean rating, String data) {
		this.userBookId = id;
		this.user_id = user_id;
		this.bookId = bookId;
		this.rating = rating;
		this.data = data;
	}

	public String toString() {
		StringBuilder string = new StringBuilder();
		string.append("UserBook[");
		string.append("id=" + userBookId);
		string.append(", user_id=" + user_id);
		string.append(", bookId=" + bookId);
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
		this.bookId = bookId;
	}

	public int getBookId() {
		return this.bookId;
	}

	public void setUserId(int user_id) {
		this.user_id = user_id;
	}

	public int getUserId() {
		return this.user_id;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getData() {
		return data;
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
				&& Objects.equals(this.bookId, that.bookId) && Objects.equals(this.rating, that.rating)
				&& Objects.equals(this.data, that.data);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.userBookId, this.bookId, this.user_id, this.rating, this.data);
	}
}
