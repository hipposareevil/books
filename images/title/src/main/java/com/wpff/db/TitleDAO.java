package com.wpff.db;

import com.wpff.core.Title;

import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import java.util.List;
import java.util.Optional;


/**
 * Hibernate Data Access Object for an book Title. 
 */
public class TitleDAO extends AbstractDAO<Title> {

  public TitleDAO(SessionFactory factory) {
    super(factory);
  }

  /**
   * Look up an title by id. 
   *
   * @param id Title ID
   * @return Optional Title
   */
  public Optional<Title> findById(Integer id) {
    return Optional.ofNullable(get(id));
  }


  /**
   * Persists a new Title into the backing DB.
   *
   * @param title Title to be created. Comes in via TitleResource.
   * @return Title that was persisted
   */
  public Title create(Title title) {
    return persist(title);
  }

  /**
   * Find all matching Titles in the database. Takes a SQL LIKE syntax,
   * e.g. 'never' would return 'Neverwhere'
   *
   * @param titleName Name of title, or partial name of title.
   * @return List of Titles. May be empty
   */
  public List<Title> findByName(String titleName) {
    return currentSession()
        .createCriteria(Title.class)
        .add(Restrictions.like("title", "%"+ titleName +"%"))
        .list();
  }

  /**
   * Find all titles in the database.
   *
   * @return List of Titles, may be empty
   */
  public List<Title> findAll() {
    return list(namedQuery("com.wpff.core.Title.findAll"));
  }


}
