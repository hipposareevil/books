package com.wpff.query;

import java.util.List;

/**
 * Book bean that is used in the POST method of the BookResource.
 * This is used to present a view of the Book for creation and is
 * thus missing the 'id' that is present in the full BookResult bean.
 */
public class BookQuery {
  private Integer authorId;

  private Integer firstPublishedYear;

  private String title;

  /**
   * CSV string of isbns
   */
  private List<String> isbns;
  
  /**
   * CSV string of subjects
   */
  private List<String> subjects;
  
  /**
   * Description of book
   */
  private String description;

  /**
   * openlibrary.org 'works' identifier
   */
  private String openlibraryWorkUrl;
  
  /**
   * goodreads URL
   */
  private String goodreadsUrl;

  private String imageSmall;

  private String imageMedium;

  private String imageLarge;

  /**
   * @return the authorId
   */
  public Integer getAuthorId() {
    return authorId;
  }

  /**
   * @param authorId the authorId to set
   */
  public void setAuthorId(Integer authorId) {
    this.authorId = authorId;
  }

  /**
   * @return the firstPublishedYear
   */
  public Integer getFirstPublishedYear() {
    return firstPublishedYear;
  }

  /**
   * @param firstPublishedYear the firstPublishedYear to set
   */
  public void setFirstPublishedYear(Integer firstPublishedYear) {
    this.firstPublishedYear = firstPublishedYear;
  }

  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return the isbns
   */
  public List<String> getIsbns() {
    return isbns;
  }

  /**
   * @param isbns the isbns to set
   */
  public void setIsbns(List<String> isbns) {
    this.isbns = isbns;
  }

  /**
   * @return the subjects
   */
  public List<String> getSubjects() {
    return subjects;
  }

  /**
   * @param subjects the subjects to set
   */
  public void setSubjects(List<String> subjects) {
    this.subjects = subjects;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the openlibraryWorkUrl
   */
  public String getOpenlibraryWorkUrl() {
    return openlibraryWorkUrl;
  }

  /**
   * @param openlibraryWorkUrl the openlibraryWorkUrl to set
   */
  public void setOpenlibraryWorkUrl(String openlibraryWorkUrl) {
    this.openlibraryWorkUrl = openlibraryWorkUrl;
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
    builder.append("BookQuery [authorId=");
    builder.append(authorId);
    builder.append(", firstPublishedYear=");
    builder.append(firstPublishedYear);
    builder.append(", title=");
    builder.append(title);
    builder.append(", isbns=");
    builder.append(isbns);
    builder.append(", subjects=");
    builder.append(subjects);
    builder.append(", description=");
    builder.append(description);
    builder.append(", openlibraryWorkUrl=");
    builder.append(openlibraryWorkUrl);
    builder.append(", imageSmall=");
    builder.append(imageSmall);
    builder.append(", imageMedium=");
    builder.append(imageMedium);
    builder.append(", imageLarge=");
    builder.append(imageLarge);
    builder.append("]");
    return builder.toString();
  }

  /**
   * @return the goodreadsUrl
   */
  public String getGoodreadsUrl() {
    return goodreadsUrl;
  }

  /**
   * @param goodreadsUrl the goodreadsUrl to set
   */
  public void setGoodreadsUrl(String goodreadsUrl) {
    this.goodreadsUrl = goodreadsUrl;
  }  




}
