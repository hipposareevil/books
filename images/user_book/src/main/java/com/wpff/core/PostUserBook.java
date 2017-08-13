package com.wpff.core;

import java.util.*;

/**
 * User bean that is used in the POST method of UserBook.
 * This is used to present a view of the UserBook for creation and is
 * thus missing the 'user_book_id' that is present in the normal UserBook bean.
 *
 */
public class PostUserBook {

  private int bookId;

  private boolean rating;

  private String data;

  private List<String> tags = new ArrayList<String>();


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
