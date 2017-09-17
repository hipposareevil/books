package wpff.openlibrary;

/**
 * Helper class to create image URLs for openlibrary.org artifacts.
 *
 */
public class ImageUrlCreator {
	
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
	private final static String ROOT_AUTHOR_IMAGE = "http://covers.openlibrary.org/a/olid/";
	
	/**
	 * Root URL of cover images
	 * 'http://covers.openlibrary.org/a/id/<coverImage>-M.jpg'
	 */
	private final static String ROOT_COVER_IMAGE = "http://covers.openlibrary.org/a/id/";

	/**
	 * Create image for an author
	 * 
	 * @param authorKey openlibrary author key
	 * @param size Size of image to create
	 * @return
	 */
	public static String createAuthorImageUrl(String authorKey, ImageSize size) {
		if (authorKey == null) {
			return null;
		}
		
		String imageUrl = ROOT_AUTHOR_IMAGE + authorKey + "-";

		switch (size) {
		case SMALL:
			imageUrl += "S.jpg";
			break;
		case MEDIUM:
			imageUrl += "M.jpg";
			break;
		case LARGE:
			imageUrl += "L.jpg";
			break;
		}

		return imageUrl;
	}

	/**
	 * Create image for an book cover
	 * 
	 * @param coverId
	 *            ID of cover image
	 * @param size
	 *            Size of image to create
	 * @return
	 */
	public static String createCoverImageUrl(String coverId, ImageSize size) {
		if (coverId == null) {
			return null;
		}
		
		String imageUrl = ROOT_COVER_IMAGE + coverId + "-";

		switch (size) {
		case SMALL:
			imageUrl += "S.jpg";
			break;
		case MEDIUM:
			imageUrl += "M.jpg";
			break;
		case LARGE:
			imageUrl += "L.jpg";
			break;
		}

		return imageUrl;
	}
}
