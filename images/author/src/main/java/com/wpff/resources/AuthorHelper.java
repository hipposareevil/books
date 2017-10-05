package com.wpff.resources;

import java.util.List;

import javax.ws.rs.NotFoundException;

import com.wpff.core.Author;
import com.wpff.db.AuthorDAO;

import io.dropwizard.hibernate.UnitOfWork;

public class AuthorHelper {
  
  private final AuthorDAO authorDAO;

  public AuthorHelper(AuthorDAO authorDao) {
    this.authorDAO= authorDao;
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
   * @return list of Authors
   */
  @UnitOfWork
  List<Author> findAll() {
    return authorDAO.findAll();
  }

  /**
   * Get set of Authors by name
   * 
   * @param authorQuery
   *          author name
   * @returnlist of Authors
   */
  @UnitOfWork
  List<Author> findByName(String authorQuery) {
    return authorDAO.findByName(authorQuery);
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
   * Delete an author by ID
   * 
   * @param authorId
   *          ID of author to delete
   */
  @UnitOfWork
  void deleteAuthor(int authorId) {
    Author deleteMe = this.findById(authorId);
    this.authorDAO.delete(deleteMe);
  }
}
