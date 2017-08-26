package com.wpff.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * UserBook bean that is used in the POST method of UserBook. This is used to
 * present a view of the UserBook for creation and is thus missing the
 * 'user_book_id' that is present in the normal UserBook bean. It also has the
 * list of tags, which is missing from UserBook
 */
public class PostUserBook {

	protected int bookId;

	protected boolean rating;

	protected String data;

	protected List<String> tags = new ArrayList<String>();

	public String toString() {
		StringBuilder string = new StringBuilder();
		string.append("PostUserBook[");
		string.append("bookId=" + bookId);
		string.append(", rating=" + rating);
		string.append(", tags=" + Arrays.toString(tags.toArray()));
		string.append("]");

		return string.toString();
	}

	public void setBookId(int bookId) {
		this.bookId = bookId;
	}

	public int getBookId() {
		return this.bookId;
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

	public void setTags(List<String> tags) {
		this.tags = new ArrayList<String>(tags);
	}

	public List<String> getTags() {
		return new ArrayList<String>(this.tags);
	}
}
