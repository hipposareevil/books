package com.wpff.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


/***
 * Represents an author. This is marshalled to/from the database.
 */
@Entity
@Table(name = "author")
public class Author implements Comparable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "author_id", unique=true, nullable = false)
  private int id;

  @Column(name = "name", unique=true, nullable = false)
  private String name;

  @Column(name = "birth_date", unique=true, nullable = false)
  private String birthDate;

  // small image URL
  @Column(name = "image_small", unique=false, nullable = true)
  private String imageSmall;

  // medium image URL
  @Column(name = "image_medium", unique=false, nullable = true)
  private String imageMedium;

  // large image URL
  @Column(name = "image_large", unique=false, nullable = true)
  private String imageLarge;

  // openlibrary.org 'author key' location
  @Column(name = "ol_key", unique=false, nullable = true)
  private String olKey;

  // goodreads URL for author
  @Column(name = "goodreads_url", unique=false, nullable = true)
  private String goodreadsUrl;

  // openlibrary.org 'author key' location
  @Column(name = "subjects", unique=false, nullable = true)
  private String subjectsAsCsv;

  //////////////////////////////////
  
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

  @Override
  public int compareTo(Object o) {
    Author other = (Author) o;
    return Integer.compare(this.id, other.id);
  }

  /**
   * @return the subjects
   */
  public String getSubjectsAsCsv() {
    return subjectsAsCsv;
  }

  /**
   * @param subjects the subjects to set
   */
  public void setSubjectsAsCsv(String subjects) {
    this.subjectsAsCsv = subjects;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Author [id=");
    builder.append(id);
    builder.append(", name=");
    builder.append(name);
    builder.append(", birthDate=");
    builder.append(birthDate);
    builder.append(", imageSmall=");
    builder.append(imageSmall);
    builder.append(", imageMedium=");
    builder.append(imageMedium);
    builder.append(", imageLarge=");
    builder.append(imageLarge);
    builder.append(", olKey=");
    builder.append(olKey);
    builder.append(", subjectsAsCsv=");
    builder.append(subjectsAsCsv);
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
