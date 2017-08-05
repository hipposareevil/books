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

// Swagger
import io.swagger.annotations.*;


@Entity
@Table(name = "author")
@NamedQueries(
    {
        @NamedQuery(
          name = "com.wpff.core.Author.findAll",
            query = "SELECT a FROM Author a"
        )
    }
)
public class Author {
//  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Column(name = "id", unique=true, nullable = false)
  private int id;

  @Column(name = "name", unique=true, nullable = false)
  private String name;

  @Column(name = "image_url", unique=false, nullable = false)
  private String imageUrl;

  public Author() {
  }

  public Author(String name) {
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String url) {
    this.imageUrl = url;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Author)) {
      return false;
    }

    final Author that = (Author) o;

    return Objects.equals(this.id, that.id) &&
        Objects.equals(this.imageUrl, that.imageUrl) &&
        Objects.equals(this.name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, imageUrl);
  }
}