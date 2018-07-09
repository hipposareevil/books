package com.wpff.core;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Named query to select all tags
 */
@Entity
@Table(name = "tagmapping")
@NamedQueries({ 
	    @NamedQuery(name = "com.wpff.core.TagMapping.findAll", 
	    		       query = "SELECT u FROM TagMapping u"),
		
	    @NamedQuery(name = "com.wpff.core.TagMapping.findByUserBookId",
				   query = "SELECT u FROM TagMapping u "
				       + "WHERE u.user_book_id = :user_book_id"), 

	    @NamedQuery(name = "com.wpff.core.TagMapping.deleteUserBook",
				    query = "DELETE FROM TagMapping u "
				        + "WHERE u.user_book_id = :user_book_id"),
		
	    @NamedQuery(name = "com.wpff.core.TagMapping.getTagMappingsForUserAndTagIds",
				    query = "SELECT u FROM TagMapping u "
				        + "WHERE u.user_id = :user_id AND "
				        + "u.tag_id = :tag_id")
	    })
/**
 * Class to map a UserBook to a set of Tags.  Uses the 'tagmapping' table.
 */
public class TagMapping implements java.io.Serializable {

	private static final long serialVersionUID = -9079509977863749820L;

	/**
	 * Tag ID.
	 */
	@Column(name = "tag_id", nullable = false)
	@Id
	private int tag_id;

	/**
	 * User book ID.
	 */
	@Column(name = "user_book_id", nullable = false)
	@Id
	private int user_book_id;
	
	/**
	 * User ID
	 */
	@Column(name = "user_id", nullable = false)
	@Id
	private int user_id;

	/**
	 * Default constructor
	 */
	public TagMapping() {
	}

	public TagMapping(int user_id, int user_book_id, int tag_id) {
	  this.user_id = user_id;
		this.user_book_id = user_book_id;
		this.tag_id = tag_id;
	}

	public String toString() {
		StringBuilder string = new StringBuilder();
		string.append("TagMapping[");
		string.append("userBookId='" + user_book_id + "',");
		string.append("userId='" + user_id+ "',");		
		string.append("tagId='" + tag_id + "' ");
		string.append("]");
		return string.toString();
	}

	public int getTagId() {
		return this.tag_id;
	}

	public void setTagId(int id) {
		this.tag_id = id;
	}

	public int getUserBookId() {
		return this.user_book_id;
	}

  public void setUserBookId(int id) {
    this.user_book_id = id;
  }

  public int getUserId() {
    return this.user_id;
  }

  public void setUserId(int id) {
    this.user_id = id;
  }

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof TagMapping)) {
			return false;
		}

		final TagMapping that = (TagMapping) o;

		return Objects.equals(this.user_id, that.user_id)
		    && Objects.equals(this.user_book_id, that.user_book_id)
		    && Objects.equals(this.tag_id, that.tag_id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.user_book_id, this.tag_id, this.user_id);
	}

}
