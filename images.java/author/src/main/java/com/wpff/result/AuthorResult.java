package com.wpff.result;

import com.wpff.query.AuthorQuery;

/**
 * Bean representing an author that is returned to the caller. This is used
 * instead of the core.Author class so we can control the structure of the
 * format more closely.
 *
 */
public class AuthorResult extends AuthorQuery {
  private int id;

  /**
   * @return the id
   */
  public int getId() {
    return id;
  }

  /**
   * @param id
   *          the id to set
   */
  public void setId(int id) {
    this.id = id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("AuthorResult [id=");
    builder.append(id);
    builder.append(", ");
    builder.append(super.toString());
    builder.append("]");
    return builder.toString();
  }

  
}
