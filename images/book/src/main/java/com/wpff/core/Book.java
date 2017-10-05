package com.wpff.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/***
 * Represents a book. This is marshalled to/from the database.
 */
@Entity
@Table(name = "book")
@NamedQueries(
    {
        @NamedQuery(
          name = "com.wpff.core.Book.findAll",
            query = "SELECT b FROM Book b"
        )
    }
)
public class Book implements Comparable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "book_id", nullable=false)
  private int id;

  @Column(name = "author_id", nullable=false)
  private int authorId;

  @Column(name = "year", nullable=false)
  private int year;

  @Column(name = "title", unique=true, nullable = false)
  private String title;
  
  @Column(name = "description", unique=true, nullable = false)
  private String description;  

  // csv list of isbns
  @Column(name = "isbn", unique=false, nullable = true)
  private String isbn;
  
  // csv list of subjects
  @Column(name = "subjects", unique=false, nullable = true)
  private String subject;  

  // openlibrary.org 'works' location
  @Column(name = "ol_works", unique=false, nullable = true)
  private String olWorks;

  // small image URL
  @Column(name = "image_small", unique=false, nullable = true)
  private String imageSmall;

  // medium image URL
  @Column(name = "image_medium", unique=false, nullable = true)
  private String imageMedium;

  // large image URL
  @Column(name = "image_large", unique=false, nullable = true)
  private String imageLarge;
  
  //////////////////////////////////////////////////



  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + authorId;
    result = prime * result + id;
    result = prime * result + ((imageLarge == null) ? 0 : imageLarge.hashCode());
    result = prime * result + ((imageMedium == null) ? 0 : imageMedium.hashCode());
    result = prime * result + ((imageSmall == null) ? 0 : imageSmall.hashCode());
    result = prime * result + ((isbn == null) ? 0 : isbn.hashCode());
    result = prime * result + ((olWorks == null) ? 0 : olWorks.hashCode());
    result = prime * result + ((title == null) ? 0 : title.hashCode());
    result = prime * result + year;
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Book other = (Book) obj;
    if (authorId != other.authorId)
      return false;
    if (id != other.id)
      return false;
    if (imageLarge == null) {
      if (other.imageLarge != null)
        return false;
    } else if (!imageLarge.equals(other.imageLarge))
      return false;
    if (imageMedium == null) {
      if (other.imageMedium != null)
        return false;
    } else if (!imageMedium.equals(other.imageMedium))
      return false;
    if (imageSmall == null) {
      if (other.imageSmall != null)
        return false;
    } else if (!imageSmall.equals(other.imageSmall))
      return false;
    if (isbn == null) {
      if (other.isbn != null)
        return false;
    } else if (!isbn.equals(other.isbn))
      return false;
    if (olWorks == null) {
      if (other.olWorks != null)
        return false;
    } else if (!olWorks.equals(other.olWorks))
      return false;
    if (title == null) {
      if (other.title != null)
        return false;
    } else if (!title.equals(other.title))
      return false;
    if (year != other.year)
      return false;
    return true;
  }

  @Override
  public int compareTo(Object o) {
   final Book that = (Book) o;
    if (this == that) return 0;

    return (this.id - that.id);
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
   * @return the isbn
   */
  public String getIsbn() {
    return isbn;
  }

  /**
   * @param isbn the isbn to set
   */
  public void setIsbn(String isbn) {
    this.isbn = isbn;
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
   * @return the subjects
   */
  public String getSubject() {
    return subject;
  }

  /**
   * @param subject  the subjects to set
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Book [id=");
    builder.append(id);
    builder.append(", authorId=");
    builder.append(authorId);
    builder.append(", year=");
    builder.append(year);
    builder.append(", title=");
    builder.append(title);
    builder.append(", description=");
    builder.append(description);
    builder.append(", isbn=");
    builder.append(isbn);
    builder.append(", subject=");
    builder.append(subject);
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
