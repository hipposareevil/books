package com.wpff.db;

import java.util.List;
import java.util.Optional;

import org.eclipse.jetty.server.Authentication.User;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.wpff.common.result.Segment;
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
   * Find all matching Authors in the database. Takes a SQL LIKE syntax, e.g.
   * 'tolk' would return 'J.R.R. Tolkien'
   * 
   * @param segment
   *          Offset and limit for query
   * @param authorName
   *          Name of author, or partial name of author.
   * @return List of Authors. May be empty
   */
  public List<Author> findByName(String authorName,
      Segment segment) {
    Integer offset = segment.getOffset();
    Integer limit = segment.getLimit();
    
    return currentSession()
        .createCriteria(Author.class)
        .add(Restrictions.like("name", "%"+ authorName +"%"))
        .setFirstResult(offset)
        .setMaxResults(limit)
        .list();
  }

  
    /**
   * Find all authors in the database. 
   *
   * @param segment
   *          Offset and limit for query
   * @return List of Authors, may be empty
   */
  public List<Author> findAll(Segment segment) {
    Integer offset = segment.getOffset();
    Integer limit = segment.getLimit();

	  Criteria criteria = currentSession()
     .createCriteria(Author.class)
     .setFirstResult(offset)
     .setMaxResults(limit);
	    	
	  return criteria.list();
  }
  
  
  /**
   * Get total number of authors
   * 
   * @return number of authors
   */
	public long getNumberOfAuthors() {
	    Criteria criteria = currentSession()
				.createCriteria(Author.class)
				.setProjection(Projections.rowCount());
	    
	    Number numRows = (Number) criteria.uniqueResult();
	    return numRows.longValue();
	}



}
