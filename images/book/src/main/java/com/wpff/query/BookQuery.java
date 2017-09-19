package com.wpff.query;

import java.util.List;

/**
 * Book bean that is used in the POST method of the BookResource.
 * This is used to present a view of the Book for creation and is
 * thus missing the 'id' that is present in the full BookResult bean.
 */
public class BookQuery {
  private int authorId;

  private int year;

  private String title;

  private List<String> isbns;

  /**
   * openlibrary.org 'works' identifier
   */
  private String olWorks;

  private String imageSmall;

  private String imageMedium;

  private String imageLarge;
  

  
   ////////////////////////////////////////////

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
   * @return the year
   */
  public int getYear() {
    return year;
  }

  /**
   * @param year the year to set
   */
  public void setYear(int year) {
    this.year = year;
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
   * @return the olWorks
   */
  public String getOlWorks() {
    return olWorks;
  }

  /**
   * @param olWorks the olWorks to set
   */
  public void setOlWorks(String olWorks) {
    this.olWorks = olWorks;
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

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("BookQuery [authorId=");
    builder.append(authorId);
    builder.append(", year=");
    builder.append(year);
    builder.append(", title=");
    builder.append(title);
    builder.append(", isbns=");
    builder.append(isbns);
    builder.append(", olWorks=");
    builder.append(olWorks);
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
