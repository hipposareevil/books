package wpff.google;

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
	private Map<BookQueryBean.ID_TYPE, String> ids = new HashMap<BookQueryBean.ID_TYPE, String>();

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

	public BookBuilder addId(BookQueryBean.ID_TYPE type, String value) {
		this.ids.put(type, value);
		return this;
	}

	public BookQueryBean build() {
		return new BookQueryBean(this.title, this.author, this.publicationDate, this.ids);
	}

}
