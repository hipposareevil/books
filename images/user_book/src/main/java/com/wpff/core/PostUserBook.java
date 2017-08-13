package com.wpff.core;


/**
 * User bean that is used in the POST method of UserBook.
 * This is used to present a view of the UserBook for creation and is
 * thus missing the 'user_book_id' that is present in the normal UserBook bean.
 *
 */
public class PostUserBook {

  private int user_id;

  private int bookId;

  private boolean rating;

  private String data;


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


}
