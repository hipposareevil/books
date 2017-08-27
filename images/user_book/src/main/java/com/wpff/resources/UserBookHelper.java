package com.wpff.resources;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

// utils
import org.apache.commons.beanutils.BeanUtils;

import com.wpff.core.DatabaseUserBook;
import com.wpff.core.FullUserBook;
import com.wpff.core.PostUserBook;
import com.wpff.core.Tag;
import com.wpff.core.TagMapping;
import com.wpff.core.User;
import com.wpff.db.TagDAO;
import com.wpff.db.TagMappingDAO;
import com.wpff.db.UserBookDAO;
import com.wpff.db.UserDAO;

import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.params.IntParam;

public class UserBookHelper {

	/**
	 * DAO used to get a UserBook.
	 */
	private final UserBookDAO userBookDAO;

	/**
	 * DAO for tags
	 */
	private final TagDAO tagDAO;

	/**
	 * DAO for users
	 */
	private final UserDAO userDAO;

	/**
	 * DAO for tagmap
	 */
	private final TagMappingDAO tagMapDAO;

	public UserBookHelper(UserBookDAO userBookDAO, UserDAO userDAO, TagDAO tagDAO, TagMappingDAO tagMapDAO) {
		this.tagDAO = tagDAO;
		this.userBookDAO = userBookDAO;
		this.userDAO = userDAO;
		this.tagMapDAO = tagMapDAO;
	}

	/**
	 * Create a UserBook in the database. Tags are not created at this time.
	 *
	 * @param userBookBean
	 *            Incoming bean with UserBook information
	 * @param userId
	 *            ID of user
	 *
	 * @return Newly created UserBook from database.
	 */
	@UnitOfWork
	DatabaseUserBook createUserBook(PostUserBook userBookBean, IntParam userId)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// Create transient UserBook
		DatabaseUserBook userBookToCreate = new DatabaseUserBook();

		// Copy over bean values - copy(destination, source)
		BeanUtils.copyProperties(userBookToCreate, userBookBean);

		// Set the user_id from the URL to the 'userBook'
		userBookToCreate.setUserId(userId.get());

		// Create user book in DB
		return this.userBookDAO.create(userBookToCreate);
	}

	/**
	 * Get list of UserBooks for the requested User id
	 * 
	 * @param userId
	 *            ID of user to get books for
	 * @return List of UserBooks
	 */
	@UnitOfWork
	List<FullUserBook> getUserBooksForUser(int userId) throws IllegalAccessException, InvocationTargetException {
		List<FullUserBook> userBooks = new ArrayList<FullUserBook>();

		// Get list of books in db
		List<DatabaseUserBook> booksInDatabase = userBookDAO.findBooksByUserId(userId);

		// convert each book into a FullUserBook
		for (DatabaseUserBook dbBook : booksInDatabase) {
			FullUserBook bookToReturn = new FullUserBook();

			// Copy over bean values - copy(destination, source)
			BeanUtils.copyProperties(bookToReturn, dbBook);

			// Add tags from tagmapping table
			addTagsToUserBook(bookToReturn);

			userBooks.add(bookToReturn);
		}

		return userBooks;
	}

	/**
	 * Get UserBook from database. This will contain the UserBooks tags
	 *
	 * @param userBookId
	 *            ID of user book to retrieve
	 * @return GetUserBook containing all UserBook info and tags
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	@UnitOfWork
	FullUserBook getUserBookById(int userBookId) throws IllegalAccessException, InvocationTargetException {
		DatabaseUserBook bookInDb = this.userBookDAO.findById(userBookId)
				.orElseThrow(() -> new NotFoundException("No UserBook by id '" + userBookId + "'"));

		System.out.println("got userbook from DB: " + bookInDb);

		FullUserBook bookToReturn = new FullUserBook();
		// Copy over bean values - copy(destination, source)
		BeanUtils.copyProperties(bookToReturn, bookInDb);

		System.out.println("Copied DB stuff into bookToReturn:" + bookToReturn);

		// Add tags from tagmapping table
		addTagsToUserBook(bookToReturn);

		return bookToReturn;
	}

	/**
	 * Add tags from database to userbook
	 *
	 */
	private void addTagsToUserBook(FullUserBook userBook) {
		System.out.println("Adding tags to userbook:" + userBook);

		// Get tag mappings for user book
		List<TagMapping> tagMappings = this.tagMapDAO.findTagMappings(userBook.getUserBookId());

		// Get tag IDs for the user book
		List<Integer> tagIds = tagMappings.stream().map(TagMapping::getTagId).collect(Collectors.toList());

		System.out.println("TAGIDS: " + Arrays.toString(tagIds.toArray()));

		// Get all tags in database and convert into a map keyed by tagID
		Map<String, Tag> allTags = this.tagDAO.findAll();
		Map<Integer, Tag> tagsIndexById = allTags.values().stream().collect(Collectors.toMap(Tag::getId, p -> p));

		// Correlate tag ids from tagMappings into tag names
		List<String> tagNames = tagIds.stream().map(e -> tagsIndexById.get(e).getName()).collect(Collectors.toList());
		System.out.println("TAG names: " + Arrays.toString(tagNames.toArray()));

		for (String x : tagNames) {
			System.out.println("Assiging tag " + x + " to userbook: " + userBook);
		}

		userBook.setTags(tagNames);
	}

	/**
	 * Get map of Tags from database
	 *
	 * @return Map of tags indexed by tag name
	 */
	@UnitOfWork
	Map<String, Tag> getAllTags() {
		Map<String, Tag> tagsInDbMap = this.tagDAO.findAll();
		return tagsInDbMap;
	}

	/**
	 * Create a single TagMapping in the database.
	 *
	 * @param tagMapping
	 *            New tag map
	 * @return created TagMap
	 */
	@UnitOfWork
	TagMapping createTagMapping(TagMapping tagMapping) {
		return this.tagMapDAO.addTagMapingEntry(tagMapping);
	}

	/**
	 * Get list of TagMappings
	 *
	 * @return list of TagMappings
	 */
	@UnitOfWork
	List<TagMapping> getTagMap() {
		return this.tagMapDAO.findAll();
	}

	/**
	 * Create a new Tag in the database.
	 * 
	 * @param tagName
	 *            Name of tag to create
	 * @return Newly created Tag from database
	 */
	@UnitOfWork
	Tag createTag(String tagName) {
		Tag t = new Tag();
		t.setName(tagName);
		Tag newtag = this.tagDAO.create(t);
		return newtag;
	}

	/**
	 * Verify the userId in the path matches the user from the security context. Or
	 * if the context user is 'admin'.
	 *
	 * @param context
	 *            SecurityContext to grab username from
	 * @param userId
	 *            ID of user from the Path
	 */
	@UnitOfWork
	void verifyUserIdMatches(SecurityContext context, int userId) throws WebApplicationException {
		// Get the username corresponding to the incoming userId and verify that is the
		// same as the authenticated caller.
		String userNameFromSecurity = context.getUserPrincipal().getName();
		User userFromId = userDAO.findById(userId)
				.orElseThrow(() -> new NotFoundException("No user with ID '" + userId + "' found."));

		String userNameFromId = userFromId.getName();
		System.out.println("userNameFromSecurity>" + userNameFromSecurity + "<");
		System.out.println("userFromId:" + userNameFromId + ":");

		// Check names.
		// If:
		// userNameFromSecurity == admin or
		// userNameFromSecurity == name from id
		// we can proceed

		if ((userNameFromSecurity.equals("admin")) || (userNameFromSecurity.equals(userNameFromId))) {
			// Is ok
			System.out.println("User logged in as " + userNameFromId);
		} else {
			throw new WebApplicationException("Must be logged in as user with id '" + userFromId.getName()
					+ "' or as 'admin' to access this resource.", Response.Status.UNAUTHORIZED);
		}
	}

}
