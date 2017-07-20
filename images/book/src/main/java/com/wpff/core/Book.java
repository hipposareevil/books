package com.wpff.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.util.Objects;

/***
 * Represents a book.
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
public class Book {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "title", unique=true, nullable = false)
  private String title;

  @Column(name = "author_id", nullable=false)
  private int author_id;

  @Column(name = "year", nullable=false)
  private int year;

  public Book() {
  }

  public Book(String title, int year, int author_id) {
    this.title = title;
    this.year = year;
    this.author_id = author_id;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
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

  
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Book)) {
      return false;
    }

    final Book that = (Book) o;

    return Objects.equals(this.id, that.id) &&
        Objects.equals(this.title, that.title);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title);
  }
}
