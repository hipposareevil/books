package com.wpff.db;

import java.util.List;
import java.util.Optional;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.wpff.common.result.Segment;
import com.wpff.core.Tag;

import io.dropwizard.hibernate.AbstractDAO;


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
    return persist(tag);
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
   * Get total number of tags
   * 
   * @return number of tags
   */
	public long getNumberOfTags() {
	    Criteria criteria = currentSession()
				.createCriteria(Tag.class)
				.setProjection(Projections.rowCount());
	    
	    Number numRows = (Number) criteria.uniqueResult();
	    return numRows.longValue();
	}


  /**
   * Find all tags in the database. 
   * 
   * @param segment
   *          Offset and limit for query
   * @return List of Tags, may be empty
   */
  public List<Tag> findAll(Segment segment) {
      Integer offset = segment.getOffset();
    Integer limit = segment.getLimit();

	  Criteria criteria = currentSession()
     .createCriteria(Tag.class)
     .setFirstResult(offset)
     .setMaxResults(limit);
	    	
	  return criteria.list();
  }


  
}
