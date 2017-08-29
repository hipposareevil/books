package com.wpff.db;

import java.util.List;

import org.hibernate.SessionFactory;

import com.wpff.core.TagMapping;

import io.dropwizard.hibernate.AbstractDAO;

/**
 * Data access object for the TagMap table
 */
public class TagMappingDAO extends AbstractDAO<TagMapping> {

	public TagMappingDAO(SessionFactory factory) {
		super(factory);
	}

	/**
	 * Find all tag mappings in the database. Uses the named query in
	 * com.wpff.core.TagMap
	 *
	 * @return List of TagMaps, may be empty
	 */
	@SuppressWarnings("unchecked")
	public List<TagMapping> findAll() {
		List<TagMapping> tagMap = list(namedQuery("com.wpff.core.TagMapping.findAll"));
		return tagMap;
	}

	/**
	 * Find tag mappings for the incoming user book
	 */
	@SuppressWarnings("unchecked")
	public List<TagMapping> findTagMappings(int userBookId) {
		List<TagMapping> tagMap = list(
				namedQuery("com.wpff.core.TagMapping.findByUserBookId").setParameter("user_book_id", userBookId));
		return tagMap;
	}

	/**
	 * Add a new tag mapping to the database.
	 * 
	 * @param tagMapping
	 *            Tagmapping to add
	 * @return The added TagMapping
	 */
	public TagMapping addTagMapingEntry(TagMapping tagMapping) {
		return persist(tagMapping);
	}
	
	/**
	 * Delete all tag mappings for the incoming user book
	 * @param userBookId ID of user book to remove from tag mappings
	 */
	public void deleteTagMappingByUserBookId(int userBookId) {
		namedQuery("com.wpff.core.TagMapping.deleteUserBook")
			.setParameter("user_book_id", userBookId)
			.executeUpdate();
	}

}
