package com.wpff.resources;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.ws.rs.NotFoundException;

import org.apache.commons.beanutils.BeanUtils;

import com.wpff.common.cache.Cache;
import com.wpff.common.result.Segment;
import com.wpff.core.Author;
import com.wpff.db.AuthorDAO;
import com.wpff.query.AuthorQuery;

import io.dropwizard.hibernate.UnitOfWork;

/**
 * Helper for managing DAO
 *
 */
public class AuthorHelper {
  
  /**
   * Author DAO
   */
  private final AuthorDAO authorDAO;

   /**
   * Redis cache
   */
  private final Cache cache;
  
  /**
   * Create new helper
   * 
   * @param authorDao
   *          DAO used by helper
   * @param cache
   *          Redis cache
   */
  public AuthorHelper(AuthorDAO authorDao, Cache cache) {
    this.authorDAO= authorDao;
    this.cache = cache;
    
    this.cache.clear("author.name");
  }
  
  /**
   * Helper to convert a list into a csv of those values
   * 
   * @param values
   * @return the list of values as a CSV string
   */
  static String convertListToCsv(List<String> values) {
    String csvString = "";
    if (values != null) {
      for (String s : values) {
        csvString += s + ",";
      }
      // trim last comma
      csvString = csvString.substring(0, csvString.length());      
    }
    return csvString;
  }
 

  /**
   * Look for author by incoming id. If returned Author is null, throw 404.
   * 
   * @param id
   *          ID of author to look for
   * @return Author in database
   */
  @UnitOfWork
  Author findById(int id) {
    return authorDAO.findById(id).orElseThrow(() -> new NotFoundException("No author by id " + id));
  }
  
   /**
   * Get all authors
   * 
   * @param desiredSegment
   *          offset and limit for the query
   * @return list of Authors
   */
  @UnitOfWork
  List<Author> findAll(Segment desiredSegment) {
    return authorDAO.findAll(desiredSegment);
  }
  
  /**
   * Get total number of authors
   * @return Number of authors
   */
  	@UnitOfWork
  long getTotalNumberAuthors() {
  	  return authorDAO.getNumberOfAuthors();
  }

  /**
   * Get set of Authors by name
   * 
   * @param authorQuery
   *          author name
   * @param desiredSegment
   *          offset and limit for the query  
   * @returnlist of Authors
   */
  @UnitOfWork
  List<Author> findByName(String authorQuery, Segment desiredSegment) {
    return authorDAO.findByName(authorQuery, desiredSegment);
  }

  /**
   * Create an author in the database
   * 
   * @param authorToCreate
   *          what to create
   * @return author in db
   */
  @UnitOfWork
  Author createAuthor(Author authorToCreate) {
    Author author = this.authorDAO.create(authorToCreate);
    System.out.println("Created author: " + author.getId());
    return author;
  }
  
  /**
   * Update an Author in the database
   * 
   * @param authorBean
   *          Author to update in database
   * @param authorId
   *          Author's ID
   * @return Updated author from database
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   */
  @UnitOfWork
  Author updateAuthor(AuthorQuery authorBean, int authorId) 
      throws IllegalAccessException, InvocationTargetException {
    Author authorToUpdate  = authorDAO.findById(authorId).orElseThrow(() -> new NotFoundException("No author by id " + authorId));
    
    // Copy over non null values
    copyProperty(authorToUpdate, "name", authorBean.getName());
    copyProperty(authorToUpdate, "birthDate", authorBean.getBirthDate());
    copyProperty(authorToUpdate, "olKey", authorBean.getOlKey());
    copyProperty(authorToUpdate, "goodreads_url", authorBean.getGoodreadsUrl());
    copyProperty(authorToUpdate, "imageSmall", authorBean.getImageSmall());
    copyProperty(authorToUpdate, "imageLarge", authorBean.getImageLarge());
    copyProperty(authorToUpdate, "imageMedium", authorBean.getImageMedium());
    
    // Make subjects in DB a CSV string
    authorToUpdate.setSubjectsAsCsv(convertListToCsv(authorBean.getSubjects()));
    
    this.authorDAO.update(authorToUpdate);
    
    // Clear the cache
    this.cache.clear("author.name", authorId);
    return authorToUpdate;
  }

  
  /**
   * Delete an author by ID
   * 
   * @param authorId
   *          ID of author to delete
   */
  @UnitOfWork
  void deleteAuthor(int authorId) {
    Author deleteMe = this.findById(authorId);
            
    // Clear the cache
    this.cache.clear("author.name", authorId);
    
    this.authorDAO.delete(deleteMe);
  }
  
  
  /**
   * Copy non null property
   * 
   * @param destination
   * @param field
   * @param value
   * @throws InvocationTargetException 
   * @throws IllegalAccessException 
   */
  private static void copyProperty(Object destination, String field, Object value) 
      throws IllegalAccessException, InvocationTargetException {
    if (value != null) {
      BeanUtils.copyProperty(destination, field, value);
    }
  }
}
