package com.wpff.core;

import javax.persistence.*;

import java.util.*;


import org.jasypt.util.password.BasicPasswordEncryptor;

// Used to hide fields from Swagger definition
import io.swagger.annotations.ApiModelProperty;


/**
 * UserBook represents a book for a user. It contains:
 * - user book id (ID of this entity)
 * - userID (ID in user table)
 * - bookID (ID in book table)
 * - rating of book by the user (up or down)
 * - extra data
 *
 * Tags are store in ViewableUserBook which is a subclass of this.
 */
@Entity
@Table(name = "userbook")
public class UserBook {

  /**
   * UserBook ID.
   */ 
  @Id
  @Column(name = "user_book_id", unique=true, nullable=false)
//  @ApiModelProperty(hidden=true)
  private int user_book_id;


  /**
   * ID of user
   */
  @Column(name = "user_id", unique=false, nullable = false)
  private int user_id;


  /**
   * ID of book
   */
  @Column(name = "book_id", unique=false, nullable = false)
  private int bookId;

  /**
   * Rating of book. false=down/true=up
   */
  @Column(name = "rating", unique=false, nullable = false)
  private boolean rating;


  /**
   * Data for the UserBook. May be empty
   */
  @Column(name = "data", nullable=true)
  private String data;


  /**
   * Default constructor
   */
  public UserBook() {
  }

  public UserBook(int id, int user_id, int bookId, boolean rating, String data) {
    this.user_book_id = id;
    this.user_id = user_id;
    this.bookId = bookId;
    this.rating = rating;
    this.data = data;
  }

  public String toString() {
    StringBuilder string = new StringBuilder();
    string.append("UserBook[");
    string.append("id=" + user_book_id);
    string.append(", user_id=" + user_id);
    string.append(", bookId=" + bookId);
    string.append(", rating=" + rating);
    string.append("]");

    return string.toString();
  }

  
  @ApiModelProperty(hidden=true)
  public void setUserBookId(int id) {
    this.user_book_id = id;
  }

  @ApiModelProperty(hidden=true)
  public int getUserBookId() {
    return this.user_book_id;
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
    if (!(o instanceof UserBook)) {
      return false;
    }

    final UserBook that = (UserBook) o;

    return Objects.equals(this.user_book_id, that.user_book_id) &&
        Objects.equals(this.user_id, that.user_id) &&
        Objects.equals(this.bookId, that.bookId) &&
        Objects.equals(this.rating, that.rating) &&
        Objects.equals(this.data, that.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.user_book_id, this.bookId, this.user_id, this.rating, this.data);
  }
}
