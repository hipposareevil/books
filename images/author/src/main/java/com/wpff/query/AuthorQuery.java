package com.wpff.query;

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

  private String imageSmall;

  private String imageMedium;

  private String imageLarge;
  
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
  
}
