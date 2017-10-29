package com.wpff.db;

import java.util.List;
import java.util.Optional;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.wpff.common.result.Segment;
import com.wpff.core.User;

import io.dropwizard.hibernate.AbstractDAO;


/**
 * Data access object for a User
 */
public class UserDAO extends AbstractDAO<User> {

  public UserDAO(SessionFactory factory) {
    super(factory);
  }

  /**
   * Look user up by name
   *
   * @param userName Name 
   * @return List of Users. May be empty
   */
  public List<User> findByName(String userName) {
    return currentSession()
        .createCriteria(User.class)
        .add(Restrictions.like("name", userName, MatchMode.EXACT))
        .list();
  }

  /**
   * Look up an User by id. 
   *
   * @param id User ID
   * @return Optional User
   */
  public Optional<User> findById(Integer id ) {
    return Optional.ofNullable(get(id));
  }


  /**
   * Persists a new User into the backing DB.
   *
   * @param user User to be created. Comes in via UserResource.
   * @return User that was just persisted
   */
  public User create(User user) {
    return persist(user);
  }

  /**
   * Update an existing user
   *
   * @param user to update
   */
  public void update(User user) {
    currentSession().saveOrUpdate(user);
  }

  /**
   * Delete user from database.
   *
   * @param user to delete
   */
  public void delete(User user) {
    currentSession().delete(user);
  }

  /**
   * Find all users in the database. 
   *
   * @param segment
   *          Offset and limit for query
   * @return List of Users, may be empty
   */
  public List<User> findAll(Segment segment) {
    Integer offset = segment.getOffset();
    Integer limit = segment.getLimit();

	  Criteria criteria = currentSession()
     .createCriteria(User.class)
     .setFirstResult(offset)
     .setMaxResults(limit);
	    	
	  return criteria.list();
  }
  
  
  /**
   * Get total number of users
   * 
   * @return number of users
   */
	public long getNumberOfUsers() {
	    Criteria criteria = currentSession()
				.createCriteria(User.class)
				.setProjection(Projections.rowCount());
	    
	    Number numRows = (Number) criteria.uniqueResult();
	    return numRows.longValue();
	}

  
}
