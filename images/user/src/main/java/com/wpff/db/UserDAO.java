package com.wpff.db;

import com.wpff.core.User;

import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.MatchMode;

import java.util.List;
import java.util.Optional;


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
  public Optional<User> findById(Integer id) {
    System.out.println("userdao.findbyid:" + id);
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
   * Find all users in the database. Uses the named query in com.wpff.core.User
   *
   * @return List of Users, may be empty
   */
  public List<User> findAll() {
    return list(namedQuery("com.wpff.core.User.findAll"));
  }


  
}
