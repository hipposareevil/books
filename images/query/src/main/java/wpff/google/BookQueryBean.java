package wpff.google;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a book, with title and author.
 */
public class BookQueryBean {

	/**
	 * Type of Identification for this book,
	 */
	public static enum ID_TYPE {
		ISBN_10, ISBN_13
	};

	private final String title;
	private final String author;
	private final String publicationDate;
	private final Map<ID_TYPE, String> ids = new HashMap<ID_TYPE, String>();

	BookQueryBean(String title, String author, String publicationDate, Map<ID_TYPE, String> ids) {
		this.title = title;
		this.author = author;
		this.publicationDate = publicationDate;
		this.ids.putAll(ids);
	}

	public String getAuthor() {
		return this.author;
	}

	public String getTitle() {
		return this.title;
	}

	public String getPublicationDate() {
		return this.publicationDate;
	}

	public Map<ID_TYPE, String> getIds() {
		return this.ids;
	}
}
