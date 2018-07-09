package com.wpff.query;

import java.util.List;

/**
 * Author bean that is used in the POST method of the AuthorResource.
 * This is used to present a view of the Author for creation and is
 * thus missing the 'id' that is present in the normal Author bean.
 */
public class AuthorQuery {
  private String name;

  private String birthDate;
  
  /**
   * openlibrary.org author key.
   */
  private String olKey;
  
  /**
   * URL to goodreads for author
   */
  private String goodreadsUrl;

  private String imageSmall;

  private String imageMedium;

  private String imageLarge;
  
  private List<String> subjects;
  
  /////////////////////////////////////

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the birthDate
   */
  public String getBirthDate() {
    return birthDate;
  }

  /**
   * @param birthDate the birthDate to set
   */
  public void setBirthDate(String birthDate) {
    this.birthDate = birthDate;
  }

  /**
   * @return the olKey
   */
  public String getOlKey() {
    return olKey;
  }

  /**
   * @param olKey the olKey to set
   */
  public void setOlKey(String olKey) {
    this.olKey = olKey;
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

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("AuthorQuery [name=");
    builder.append(name);
    builder.append(", birthDate=");
    builder.append(birthDate);
    builder.append(", olKey=");
    builder.append(olKey);
    builder.append(", imageSmall=");
    builder.append(imageSmall);
    builder.append(", imageMedium=");
    builder.append(imageMedium);
    builder.append(", imageLarge=");
    builder.append(imageLarge);
    builder.append(", subjects=");
    builder.append(subjects);
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
