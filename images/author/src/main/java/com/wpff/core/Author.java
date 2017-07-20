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
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "name", unique=true, nullable = false)
  private String name;

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
        Objects.equals(this.name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name);
  }
}
