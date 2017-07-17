package wpff;

import java.util.HashMap;
import java.util.Map;

/**
 * Builder of Book objects.
 *
 * @See wpff.Book
 */
public class BookBuilder {

	private String title;
	private String author;
	private String publicationDate;
	private Map<Book.ID_TYPE, String> ids = new HashMap<Book.ID_TYPE, String>();

	public BookBuilder setAuthor(String author) {
		this.author = author;
		return this;
	}

	public BookBuilder setTitle(String title) {
		this.title = title;
		return this;
	}

	public BookBuilder setPublicationDate(String date) {
		this.publicationDate = date;
		return this;
	}

	public BookBuilder addId(Book.ID_TYPE type, String value) {
		this.ids.put(type, value);
		return this;
	}

	public Book build() {
		return new Book(this.title, this.author, this.publicationDate, this.ids);
	}

}
