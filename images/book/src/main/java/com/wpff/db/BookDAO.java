package com.wpff.db;

import com.wpff.core.Book;

import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import java.util.List;
import java.util.Optional;


/**
 * Hibernate Data Access Object for an book Book. 
 */
public class BookDAO extends AbstractDAO<Book> {

  public BookDAO(SessionFactory factory) {
    super(factory);
  }

  /**
   * Look up an book by id. 
   *
   * @param id Book ID
   * @return Optional Book
   */
  public Optional<Book> findById(Integer id) {
    return Optional.ofNullable(get(id));
  }


  /**
   * Persists a new Book into the backing DB.
   *
   * @param book Book to be created. Comes in via BookResource.
   * @return Book that was persisted
   */
  public Book create(Book book) {
    return persist(book);
  }

  /**
   * Delete book from database.
   *
   * @param book to delete
   */
  public void delete(Book book) {
    currentSession().delete(book);
  }


  /**
   * Find all matching Books in the database. Takes a SQL LIKE syntax,
   * e.g. 'never' would return 'Neverwhere'
   *
   * @param titleName Title of book, or partial name of title.
   * @return List of Books. May be empty
   */
  public List<Book> findByName(String titleName) {
    return currentSession()
        .createCriteria(Book.class)
        .add(Restrictions.like("title", "%"+ titleName +"%"))
        .list();
  }

  /**
   * Find all books in the database.
   *
   * @return List of Books, may be empty
   */
  public List<Book> findAll() {
    return list(namedQuery("com.wpff.core.Book.findAll"));
  }


}
