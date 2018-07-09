package wpff.openlibrary;

import java.util.List;

import wpff.openlibrary.beans.OpenLibraryTitle;

/**
 * Helper class to create image URLs for openlibrary.org artifacts.
 *
 */
public class OpenLibraryUrlConverter {
	
	/**
	 * Enumeration of image size options
	 */
	public static enum ImageSize {
		SMALL, MEDIUM, LARGE
	}
	
	/**
	 * Root URL of author images
	 * 'http://covers.openlibrary.org/a/olid/<authorKey>-[S|M|L].jpg'
	 */
	private final static String ROOT_AUTHOR_IMAGE = "https://covers.openlibrary.org/a/olid/";
	
	/**
	 * Root URL of cover images
	 * 'http://covers.openlibrary.org/b/<ID TYPE>/<coverImage>-M.jpg'
	 */
	private final static String ROOT_COVER_IMAGE = "https://covers.openlibrary.org/b/";
	
	/**
	 * Root URL of openlibrary itself
	 */
	private final static String ROOT_URL = "https://openlibrary.org";
	
	/**
	 * Create full URL for a 'work' of a book, the toplevel page for books in openlibrary.org
	 * @param workKey Work key
	 * @return Full URL of work
	 */
	public static String createWorkUrl(String workKey) {
	  return ROOT_URL + workKey;
	}

	/**
	 * Create image URL for an author
	 * 
	 * @param authorKey openlibrary author key
	 * @param size Size of image to create
	 * @return Full URL to author image
	 */
	public static String createAuthorImageUrl(String authorKey, ImageSize size) {
		if (authorKey == null) {
			return null;
		}
		
		String imageUrl = ROOT_AUTHOR_IMAGE + authorKey;
		return appendSize(imageUrl, size);
	}
	
	
	/**
	 * Create image URL for an book cover. This will query the incoming olTitle for its cover_id,
	 * and if that is null, it will go through the list of ISBNs and find one that has a
	 * valid URL.
	 * 
	 * @param olTitle
	 *            OpenLibraryTitle object
	 * @param size
	 *            Size of image to create
	 * @return Full URL of an cover image
	 */
	public static String createCoverImageUrl(OpenLibraryTitle olTitle, ImageSize size) {
		if (olTitle == null) {
			return null;
		}
		
		// Check cover id
		String coverId = olTitle.getCover_i();
		if (coverId != null) {
			String imageUrl = ROOT_COVER_IMAGE + "id/" + coverId;
			return appendSize(imageUrl, size);
		}
		
		// no cover id, so use isbn
		List<String> isbns = olTitle.getIsbn();
		if (isbns != null && isbns.size() > 0) {
			String imageUrl = ROOT_COVER_IMAGE + "isbn/" + isbns.get(0);
			return appendSize(imageUrl, size);
		}
		
		return null;
	}
	
	
	/**
	 * Append the S.jpg, M.jpg or L.jpg to url
	 * @param imageUrl Url to modify
	 * @param size Size to modify with
	 * @return
	 */
	private static String appendSize(String imageUrl, ImageSize size) {
		
		switch (size) {
		case SMALL:
			imageUrl += "-S.jpg";
			break;
		case MEDIUM:
			imageUrl += "-M.jpg";
			break;
		case LARGE:
			imageUrl += "-L.jpg";
			break;
		}

		return imageUrl;
	}
}
