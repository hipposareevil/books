package com.wpff.db;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

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
	 * @param id
	 *            UserBook ID
	 * @return Optional UserBook
	 */
	public Optional<DatabaseUserBook> findById(Integer id) {
		return Optional.ofNullable(get(id));
	}

	/**
	 * Find all UserBooks for a given user
	 *
	 * @param userId
	 *            User ID
	 * @return list of UserBooks for incoming user
	 */
	@SuppressWarnings("deprecation")
	public List<DatabaseUserBook> findBooksByUserId(Integer userId) {
		return currentSession()
				.createCriteria(DatabaseUserBook.class)
				.add(Restrictions.eq("user_id", userId))
				.list();
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
