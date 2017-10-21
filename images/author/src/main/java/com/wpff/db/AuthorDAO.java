package com.wpff.db;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import com.wpff.core.Author;

import io.dropwizard.hibernate.AbstractDAO;


/**
 * Hibernate Data Access Object for an Author. 
 */
public class AuthorDAO extends AbstractDAO<Author> {

  public AuthorDAO(SessionFactory factory) {
    super(factory);
  }

  /**
   * Look up an author by id. 
   *
   * @param id Author ID
   * @return Optional Author
   */
  public Optional<Author> findById(Integer id) {
    return Optional.ofNullable(get(id));
  }


  /**
   * Persists a new Author into the backing DB.
   *
   * @param author Author to be created. Comes in via AuthorResource.
   * @return Author that was persisted
   */
  public Author create(Author author) {
    return persist(author);
  }


  /**
   * Delete Author from database.
   *
   * @param author to delete
   */
  public void delete(Author author) {
    currentSession().delete(author);
  }

  
  	/**
   * Update an existing author
   *
   * @param author
   *          Author to update
   */
	public void update(Author author) {
		currentSession().saveOrUpdate(author);
	}


  /**
   * Find all matching Authors in the database. Takes a SQL LIKE syntax,
   * e.g. 'tolk' would return 'J.R.R. Tolkien'
   *
   * @param authorName Name of author, or partial name of author.
   * @return List of Authors. May be empty
   */
  public List<Author> findByName(String authorName) {
    return currentSession()
        .createCriteria(Author.class)
        .add(Restrictions.like("name", "%"+ authorName +"%"))
        .list();
  }

  /**
   * Find all authors in the database.
   *
   * @return List of Authors, may be empty
   */
  public List<Author> findAll() {
    return list(namedQuery("com.wpff.core.Author.findAll"));
  }


}
