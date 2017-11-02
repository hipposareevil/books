package com.wpff.db;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.wpff.common.result.Segment;
import com.wpff.core.DatabaseUserBook;

import io.dropwizard.hibernate.AbstractDAO;

/**
 * Data access object for a UserBook
 */
public class UserBookDAO extends AbstractDAO<DatabaseUserBook> {

	public UserBookDAO(SessionFactory factory) {
		super(factory);
	}

	/**
   * Look up an UserBook by id.
   *
   * @param userId
   *          ID of User
   * @param userBookId
   *          UserBook ID
   * @return Optional UserBook
   */
	public DatabaseUserBook findByUserBookId(
	    int userId,
	    Integer userBookId) {
	  // Get matching book
	  DatabaseUserBook result = get(userBookId);
    int booksUserId = result.getUser_id();
    if (booksUserId != userId) {
      // This book isn't owned by the incoming user
      result = null;
    }

    return result;
	}

	/**
   * Find user books for incoming user
   * 
   * @param userId
   *          ID of user
   * @param segment
   *          Segment describing start and offset
   * @return List of user book
   */
	@SuppressWarnings({ "deprecation", "unchecked" })
	public List<DatabaseUserBook> findBooksByUserId(
	    Integer userId,
	    Segment segment) {
	    Integer offset = segment.getOffset();
	    Integer limit = segment.getLimit();

	    	Criteria criteria = currentSession()
        .createCriteria(DatabaseUserBook.class)
        .add(Restrictions.eq("user_id", userId))
        .setFirstResult(offset)
        .setMaxResults(limit);

	    	return criteria.list();
	}

  /**
   * Get total number of user books
   * 
   * @param userId
   *          ID for user books
   * @return number of books
   */
	public long getNumberOfUserBooks(Integer userId) {
	    Criteria criteria = currentSession()
				.createCriteria(DatabaseUserBook.class)
				.add(Restrictions.eq("user_id", userId))
				.setProjection(Projections.rowCount());
	    
	    Number numRows = (Number) criteria.uniqueResult();
	    return numRows.longValue();
	}

	
	/**
	 * Persists a new UserBook into the backing DB.
	 *
	 * @param userBook
	 *            UserBook to be created. Comes in via UserBookResource.
	 * @return UserBook that was just persisted
	 */
	public DatabaseUserBook create(DatabaseUserBook userBook) {
		return persist(userBook);
	}

	/**
	 * Update an existing userBook
	 *
	 * @param userBook
	 *            to update
	 */
	public void update(DatabaseUserBook userBook) {
		currentSession().saveOrUpdate(userBook);
	}

	/**
	 * Delete userBook from database.
	 *
	 * @param userBook
	 *            to delete
	 */
	public void delete(DatabaseUserBook userBook) {
		currentSession().delete(userBook);
	}

}
