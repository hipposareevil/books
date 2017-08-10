package com.wpff.core;


/**
 * Book bean that is used in the POST method of the BookResource.
 * This is used to present a view of the Book for creation and is
 * thus missing the 'id' that is present in the normal Book bean.
 */
public class PostBook {
  private String title;
  private int year;
  private int author_id;

  public PostBook() {
  }

  public int getAuthorId() {
    return author_id;
  }

  public void setAuthorId(int id) {
    this.author_id = id;
  }


  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
  
  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

}
