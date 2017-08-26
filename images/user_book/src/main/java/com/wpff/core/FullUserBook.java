package com.wpff.core;

import java.util.Arrays;

/**
 * UserBook bean that returned from various methods. It adds the 'userBookId'
 * and 'userId' to the PostUserBook class.
 *
 * This is used instead of normal UserBook as UserBook has no tags.
 *
 */
public class FullUserBook extends PostUserBook {

	private int userBookId;

	private int userId;

	public String toString() {
		StringBuilder string = new StringBuilder();
		string.append("FullUserBook[");
		string.append("userBookId=" + userBookId);
		string.append(", userId=" + userId);
		string.append(", bookId=" + super.bookId);
		string.append(", rating=" + super.rating);
		string.append(", tags=" + Arrays.toString(tags.toArray()));
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

}
