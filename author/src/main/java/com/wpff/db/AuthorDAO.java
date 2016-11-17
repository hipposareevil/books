package com.wpff.db;

import com.wpff.core.Author;

import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import java.util.List;
import java.util.Optional;


/**
 * Hibernate Data Access Object for an Author. 
 */
public class AuthorDAO extends AbstractDAO<Author> {

  public AuthorDAO(SessionFactory factory) {
    super(factory);
  }

  public Optional<Author> findById(Integer id) {
    return Optional.ofNullable(get(id));
  }

  public Author create(Author author) {
    return persist(author);
  }

  public List<Author> findByName(String authorName) {
    return currentSession()
        .createCriteria(Author.class)
        .add(Restrictions.like("name", "%"+ authorName +"%"))
        .list();
  }


  public List<Author> findAll() {
    return list(namedQuery("com.wpff.core.Author.findAll"));
  }


}
