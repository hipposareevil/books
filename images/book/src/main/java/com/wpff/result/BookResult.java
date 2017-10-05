package com.wpff.result;
import com.wpff.query.BookQuery;

/**
 * Bean representing a book that is returned to the caller.
 * This is used instead of the core.Book class so we can control the structure
 * of the format more closely.
 *
 */
public class BookResult extends BookQuery {

  private int id;
  
  private String authorName;
  
  /**
   * @return the id
   */
  public int getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(int id) {
    this.id = id;
  }


  /**
   * @return the authorName
   */
  public String getAuthorName() {
    return authorName;
  }

  /**
   * @param authorName the authorName to set
   */
  public void setAuthorName(String authorName) {
    this.authorName = authorName;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("BookResult [id=");
    builder.append(id);
    builder.append(", authorName=");
    builder.append(authorName);
    builder.append(", parent: " );
    builder.append(super.toString());
    builder.append("]");
    return builder.toString();
  }


}
