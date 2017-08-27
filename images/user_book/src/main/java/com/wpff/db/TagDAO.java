package com.wpff.db;

import com.wpff.core.Tag;

import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.MatchMode;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Data access object for a Tag
 */
public class TagDAO extends AbstractDAO<Tag> {

  public TagDAO(SessionFactory factory) {
    super(factory);
  }

  /**
   * Look tag up by name
   *
   * @param tagName Name 
   * @return List of Tags. May be empty
   */
  public List<Tag> findByName(String tagName) {
    return currentSession()
        .createCriteria(Tag.class)
        .add(Restrictions.like("name", tagName, MatchMode.EXACT))
        .list();
  }

  /**
   * Look up an Tag by id. 
   *
   * @param id Tag ID
   * @return Optional Tag
   */
  public Optional<Tag> findById(Integer id) {
    System.out.println("tagdao.findbyid:" + id);
    return Optional.ofNullable(get(id));
  }


  /**
   * Persists a new Tag into the backing DB.
   *
   * @param tag Tag to be created. Comes in via TagResource.
   * @return Tag that was just persisted
   */
  public Tag create(Tag tag) {
    Tag newtag = persist(tag);
    System.out.println("TagDAO: Create new tag: " + newtag);
    return newtag;
  }

  /**
   * Update an existing tag
   *
   * @param tag to update
   */
  public void update(Tag tag) {
    currentSession().saveOrUpdate(tag);
  }

  /**
   * Delete tag from database.
   *
   * @param tag to delete
   */
  public void delete(Tag tag) {
    currentSession().delete(tag);
  }

  /**
   * Find all tags in the database. Uses the named query in com.wpff.core.Tag
   *
   * @return List of Tags, may be empty
   */
  public Map<String, Tag> findAll() {
    List<Tag> tags = list(namedQuery("com.wpff.core.Tag.findAll"));

    Map<String, Tag> tagsMap = tags.stream().collect(
      Collectors.toMap(Tag::getName, p -> p));
    return tagsMap;
  }


  
}
