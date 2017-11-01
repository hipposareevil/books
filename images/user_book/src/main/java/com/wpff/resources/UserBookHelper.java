package com.wpff.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

// utils
import org.apache.commons.beanutils.BeanUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wpff.common.cache.Cache;
import com.wpff.common.result.Segment;
import com.wpff.core.DatabaseUserBook;
import com.wpff.core.Tag;
import com.wpff.core.TagMapping;
import com.wpff.core.User;
import com.wpff.core.beans.FullUserBook;
import com.wpff.core.beans.PostUserBook;
import com.wpff.db.TagDAO;
import com.wpff.db.TagMappingDAO;
import com.wpff.db.UserBookDAO;
import com.wpff.db.UserDAO;

import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.params.IntParam;


/**
 * Helper for userbooks to deal with unitofwork issues
 */
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
	 * Cache
	 */
	private final Cache cache;

	/**
	 * DAO for tagmap
	 */
	private final TagMappingDAO tagMappingDAO;

	public UserBookHelper(UserBookDAO userBookDAO, UserDAO userDAO, TagDAO tagDAO, TagMappingDAO tagMapDAO,
	    Cache cache) {
		this.tagDAO = tagDAO;
		this.userBookDAO = userBookDAO;
		this.userDAO = userDAO;
		this.tagMappingDAO = tagMapDAO;
		this.cache = cache;
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
	DatabaseUserBook createUserBook(PostUserBook userBookBean, IntParam userId) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		// Create transient UserBook
		DatabaseUserBook userBookToCreate = new DatabaseUserBook();
		
		// Copy over bean values - copy(destination, source)
		BeanUtils.copyProperties(userBookToCreate, userBookBean);
		userBookToCreate.setDateAdded(new Date());

		// Set the user_id from the URL to the 'userBook'
		userBookToCreate.setUserId(userId.get());

		// Create user book in DB
		return this.userBookDAO.create(userBookToCreate);
	}
	
	
  /**
   * Update a UserBook in the database
   * 
   * @param userBookBean
   *          Bean with new data
   * @param userBookId
   *          ID of user book
   * @return
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @throws InvocationTargetException
   */
  @UnitOfWork
  DatabaseUserBook updateUserBook(PostUserBook userBookBean, int userBookId) throws IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {
    // Grab existing user book
    DatabaseUserBook userBookToUpdate = this.userBookDAO.findById(userBookId).orElseThrow(
        () -> new NotFoundException("No UserBook by id '" + userBookId + "'"));

    // Copy non null values over
    BeanUtils.copyProperty(userBookToUpdate, "data", userBookBean.getData());
    if (userBookBean.getRating() != null) {
      BeanUtils.copyProperty(userBookToUpdate, "rating", userBookBean.getRating());
    }

    // Update book in DB
    this.userBookDAO.update(userBookToUpdate);

    return userBookToUpdate;
  }
  
  /**
   * Get total number of userbooks
   * @param userId User to get number of books for
   * @return Number of userbooks for user
   */
  	@UnitOfWork
  long getTotalNumberUserBooks(Integer userId) {
    return userBookDAO.getNumberOfUserBooks(userId);
  }

	/**
   * Get list of UserBooks for the requested User id
   * 
   * @param authString
   *          Authentication header which is necessary for a REST call to 'book'
   *          web service
   * @param userId
   *          ID of user to get books for
   * @param desiredSegment
   *          offset and limit for the query
   * @return List of UserBooks
   */
	@UnitOfWork
	List<FullUserBook> getUserBooksForUser(
	    String authString, 
	    Integer userId,
	    Segment desiredSegment)
	    throws IllegalAccessException, InvocationTargetException {
		List<FullUserBook> userBooks = new ArrayList<FullUserBook>();

		// Get list of books in db
		List<DatabaseUserBook> booksInDatabase = userBookDAO.findBooksByUserId(userId, desiredSegment);

		// convert each book into a FullUserBook
		for (DatabaseUserBook dbBook : booksInDatabase) {
		  userBooks.add(convert(dbBook, authString));
		}
		
		return userBooks;
	}
	
	
	/**
   * Get all UserBooks for the incoming UserId and tagId
   * 
   * @param authString
   *          Authentication header which is necessary for a REST call to 'book'
   *          web service
   * @param userId
   *          ID of user to get books for
   * @param tagId
   *          ID of tag to get user book for
   * @return List of user books
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
	@UnitOfWork
	List<FullUserBook> getUserBooksByUserAndTag(
	    String authString,
	    Integer userId,
	    int tagId) 
	 throws IllegalAccessException, InvocationTargetException {
	  // Get TagMappings for this user and tag ID
	  List<TagMapping> mappings = this.tagMappingDAO.findTagMappingsByTagId(userId, tagId);
	  
	  List<FullUserBook> userBooks = new ArrayList<FullUserBook>();
	  for (TagMapping mapping : mappings) {
	    int userBookId = mapping.getUserBookId();
	    FullUserBook current = getUserBookByUserBookId(authString, userBookId);
	    
	    userBooks.add(current);
	  }

	  return userBooks;
	}

	@UnitOfWork
	List<FullUserBook> getUserBooksByTitle(
      String authString,
	    Integer userId,
	    String titleQuery) 
	 throws IllegalAccessException, InvocationTargetException {
	  List<FullUserBook> userBooks = new ArrayList<FullUserBook>();

	  // List of book IDs
	  List<Integer> ids = this.getBookIdsForTitleQuery(authString, titleQuery);
	  for (Integer userBookId : ids) {
	        FullUserBook current = getUserBookByUserBookId(authString, userBookId);
	    
	    userBooks.add(current);
	  }
	  
	  return userBooks;
	}
	
	/**
	 * Get UserBook from database. This will contain the UserBooks tags
	 *
	 * @param userBookId
	 *            ID of user book to retrieve
   * @param authString
   *          Authentication header which is necessary for a REST call to 'book'
   *          web service
	 * @return GetUserBook containing all UserBook info and tags
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	@UnitOfWork
	FullUserBook getUserBookByUserBookId(String authString, int userBookId) 
	    throws IllegalAccessException, InvocationTargetException {
	  // Get db book
		DatabaseUserBook bookInDb = this.userBookDAO.findById(userBookId).orElseThrow(
				() -> new NotFoundException("No UserBook by id '" + userBookId + "'"));

		return convert(bookInDb, authString);
	}


	/**
	 * Delete a user book. It is assumed the caller of this function has already
	 * verified the user IDs match up to the owner of this user book.
	 * 
	 * @param userBookId
	 *            ID of user_book to delete
	 */
	@UnitOfWork
	void deleteUserBookById(int userBookId) {
		// Get book in db
		DatabaseUserBook bookInDb = this.userBookDAO.findById(userBookId).orElseThrow(
				() -> new NotFoundException("No UserBook by id '" + userBookId + "'"));

		this.userBookDAO.delete(bookInDb);

		// Delete tag mappings from tagmapping table
		this.tagMappingDAO.deleteTagMappingByUserBookId(userBookId);
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
		return this.tagMappingDAO.addTagMapingEntry(tagMapping);
	}

  /**
   * Delete all tags for a specific user book.
   * 
   * @param userBookId
   *          ID of user book
   */
  @UnitOfWork
  void deleteTagMappingsForUserBook(int userBookId) {
    this.tagMappingDAO.deleteTagMappingByUserBookId(userBookId);
	}

	/**
	 * Get list of TagMappings
	 *
	 * @return list of TagMappings
	 */
	@UnitOfWork
	List<TagMapping> getTagMap() {
		return this.tagMappingDAO.findAll();
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
	 * Verify the userId in the path matches the user from the security context. 
	 * Or if the context user is in group 'admin'.
	 *
	 * @param context
	 *            SecurityContext to grab username from
	 * @param userId
	 *            ID of user from the Path
	 */
	@UnitOfWork
	void verifyUserIdHasAccess(SecurityContext context, int userId) throws WebApplicationException {
		// Get the username corresponding to the incoming userId and verify that is the
		// same as the authenticated caller.
		String userNameFromSecurity = context.getUserPrincipal().getName();
		User userFromId = userDAO.findById(userId).orElseThrow(() -> new NotFoundException("No user with ID '" + userId + "' found."));

		String userNameFromId = userFromId.getName();
		
		// Check names.
		// If:
		// user in security is in the 'admin' group
		// or 
		// userNameFromSecurity == name from id
		// we can proceed

		if ( (context.isUserInRole("admin")) || (userNameFromSecurity.equals(userNameFromId))) {
			// Is ok
			System.out.println("User logged in as " + userNameFromId);
		} else {
			throw new WebApplicationException(
					"Must be logged in as user with id '" + userFromId.getName() + "' or as as a member of the 'admin' user group to access this resource.",
					Response.Status.UNAUTHORIZED);
		}
	}
	
	
	////////////////////////////////////////////////////////////////
	// 
	// Helpers
		
	
	/**
   * Convert a DB book to a FullUserBook bean
   * 
   * @param dbBook
   *          Book to convert
   * @param authString
   *          Authentication header which is necessary for a REST call to 'book'
   *          web service
   * @return
   */
  private FullUserBook convert(DatabaseUserBook dbBook, String authString) throws IllegalAccessException,
      InvocationTargetException {
    FullUserBook bookToReturn = new FullUserBook();

    // Copy over bean values - copy(destination, source)
    BeanUtils.copyProperties(bookToReturn, dbBook);

    // Add tags from tagmapping table
    addTagsToUserBook(bookToReturn);

    // Get title
    String title = getTitle(authString, dbBook.getBookId());
    bookToReturn.setTitle(title);
    return bookToReturn;
  }

	/**
	 * Add tags from database to userbook
	 *
	 * @param userBook UserBook to add tags into 
	 */
	private void addTagsToUserBook(FullUserBook userBook) {
	  // Get tag mappings for user book
		List<TagMapping> tagMappings = this.tagMappingDAO.findTagMappings(userBook.getUserBookId());

		// Get tag IDs for the user book
		List<Integer> tagIds = tagMappings.stream().map(TagMapping::getTagId).collect(Collectors.toList());

		// Get all tags in database and convert into a map keyed by tagID
		Map<String, Tag> allTags = this.tagDAO.findAll();
		Map<Integer, Tag> tagsIndexById = allTags
		    .values()
		    .stream()
		    .collect(Collectors.toMap(Tag::getId, p -> p));

		// Correlate tag ids from tagMappings into tag names
		List<String> tagNames = tagIds.stream().map(e -> tagsIndexById.get(e).getName()).collect(Collectors.toList());

		userBook.setTags(tagNames);
	}
	

	/**
   * Retrieve the book title from the 'book' webservice for the incoming book id.
   * 
   * @param authString
   *          Authentication header which is necessary for a REST call to 'book'
   *          web service
   * @param bookId
   *          ID of book to get title for
   * @return
   */
  private String getTitle(String authString, int bookId) {
    String title = null;
    try {   
      // Try the cache
      title = this.cache.get("book.title", bookId);
      if (title != null) {
        return title;
      }
      
      /////////////////////
      // Get from WS now
      
      // Going to the 'book' web service directly
      String url = "http://book:8080/book/" + bookId;
      
      HttpClient client = HttpClientBuilder.create().build();
      HttpGet request = new HttpGet(url);

      // add request header
      request.addHeader("User-Agent", "BookAgent");
      request.addHeader("content-type", "application/json");
      request.addHeader("Authorization", authString);
      
      // Execute request
      HttpResponse response = client.execute(request);

      // Get code
      int responseCode = response.getStatusLine().getStatusCode();
      
      // Convert body of result
      BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
      StringBuffer result = new StringBuffer();
      String line = "";
      while ((line = rd.readLine()) != null) {
        result.append(line);
      }
      
      // Check result
      if (responseCode == 200) {
        // Convert into bean
        ObjectMapper mapper = new ObjectMapper();
        BookBean bookBean = null;
        try {
          bookBean = mapper.readValue(result.toString(), BookBean.class);
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
        
        title = bookBean.getTitle();
      }
      else {
        System.out.println("Unable to get book's title for id: " + bookId);
        System.out.println("Error code: " + responseCode);
        System.out.println("Error content: " + result);
      }

      // Set cache
      this.cache.set("book.title", bookId, title);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return title;    
  }

  
  /**
   * Retrieve BookIds that match the incoming title query.
   * 
   * This makes call to /book?title=TITLE_QUERY 
   * 
   * @param authString
   *          Authentication header which is necessary for a REST call to 'book'
   *          web service
   * @param titleQuery
   *          Title query to make 
   * @return
   */
  private List<Integer> getBookIdsForTitleQuery(String authString, String titleQuery) {
    List<Integer> bookIds = new ArrayList<Integer>();
        
    try {   
      /////////////////////
      // Get from WS now
      
      // Going to the 'book' web service directly
      String url = "http://book:8080/book?title=" + titleQuery;
      
      HttpClient client = HttpClientBuilder.create().build();
      HttpGet request = new HttpGet(url);

      // add request header
      request.addHeader("User-Agent", "BookAgent");
      request.addHeader("content-type", "application/json");
      request.addHeader("Authorization", authString);
      
      // Execute request
      HttpResponse response = client.execute(request);

      // Get code
      int responseCode = response.getStatusLine().getStatusCode();
      
      // Convert body of result
      BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
      StringBuffer result = new StringBuffer();
      String line = "";
      while ((line = rd.readLine()) != null) {
        result.append(line);
      }
      
      // Check result
      if (responseCode == 200) {
        // Convert into bean
        ObjectMapper mapper = new ObjectMapper();
        BookBeanList bookBeanList = null;
        try {
          bookBeanList = mapper.readValue(result.toString(), BookBeanList.class);
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
        
        for (BookBean bean : bookBeanList.getData()) {
          // Add book id
          bookIds.add(bean.getId());
        }        
      }
      else {
        System.out.println("Error code: " + responseCode);
        System.out.println("Error content: " + result);
      }

    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    return bookIds;
  }
}
