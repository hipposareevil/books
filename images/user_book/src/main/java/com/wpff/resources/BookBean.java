package com.wpff.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Bean representing a Book from the 'book' webservice.
 * 
 * Contains the following:
 * 
 * <pre>
{
  "authorId": 0,
  "firstPublishedYear": 0,
  "title": "string",
  "isbns": [
    "string"
  ],
  "subjects": [
    "string"
  ],
  "description": "string",
  "openlibraryWorkUrl": "string",
  "imageSmall": "string",
  "imageMedium": "string",
  "imageLarge": "string",
  "id": 0,
  "authorName": "string"
}
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookBean {
  
  /**
   * Title of Book
   */
  private String title;
  
  /**
   * book_id
   */
  private int id;
  
  /**
   * Year of book
   */
  private int firstPublishedYear;
  
  /**
   * Author of book
   */
  private String authorName;
  
  /**
   * Author id
   */
  	private int authorId;

  	private String imageSmall;

  	private String imageMedium;

  	private String imageLarge;

  	

  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title
   *          the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return the id
   */
  public int getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(int id) {
    this.id = id;
  }



  /**
   * @return the author
   */
  public String getAuthorName() {
    return authorName;
  }

  /**
   * @param author the author to set
   */
  public void setAuthorName(String author) {
    this.authorName = author;
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

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("BookBean [title=");
    builder.append(title);
    builder.append(", id=");
    builder.append(id);
    builder.append(", firstPublishedYear=");
    builder.append(firstPublishedYear);
    builder.append(", authorName=");
    builder.append(authorName);
    builder.append(", authorId=");
    builder.append(authorId);
    builder.append(", imageSmall=");
    builder.append(imageSmall);
    builder.append(", imageMedium=");
    builder.append(imageMedium);
    builder.append(", imageLarge=");
    builder.append(imageLarge);
    builder.append("]");
    return builder.toString();
  }

}
