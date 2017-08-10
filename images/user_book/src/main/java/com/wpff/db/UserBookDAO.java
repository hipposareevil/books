package com.wpff.db;

import com.wpff.core.UserBook;

import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.MatchMode;

import java.util.List;
import java.util.Optional;


/**
 * Data access object for a UserBook
 */
public class UserBookDAO extends AbstractDAO<UserBook> {

  public UserBookDAO(SessionFactory factory) {
    super(factory);
  }

  /**
   * Look up an UserBook by id. 
   *
   * @param id UserBook ID
   * @return Optional UserBook
   */
  public Optional<UserBook> findById(Integer id) {
    System.out.println("userBookDao.findbyid:" + id);
    return Optional.ofNullable(get(id));
  }

  /**
   * Find all UserBooks for a given user
   *
   * @param userId User ID
   * @return list of UserBooks for incoming user
   */
  public List<UserBook> findBooksByUserId(Integer userId) {
    return currentSession()
        .createCriteria(UserBook.class)
        .add(Restrictions.eq("user_id", userId))
        .list();
  }

  /**
   * Persists a new UserBook into the backing DB.
   *
   * @param userBook UserBook to be created. Comes in via UserBookResource.
   * @return UserBook that was just persisted
   */
  public UserBook create(UserBook userBook) {
    return persist(userBook);
  }

  /**
   * Update an existing userBook
   *
   * @param userBook to update
   */
  public void update(UserBook userBook) {
    currentSession().saveOrUpdate(userBook);
  }

  /**
   * Delete userBook from database.
   *
   * @param userBook to delete
   */
  public void delete(UserBook userBook) {
    currentSession().delete(userBook);
  }

  
}
