package wpff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

// google query
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.books.Books;
import com.google.api.services.books.Books.Volumes.List;
import com.google.api.services.books.BooksRequestInitializer;
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volumes;

import wpff.Book.ID_TYPE;

/**
 * Utility to query google for book and author information.
 *
 * This uses the Volume construct to grab authors and titles:
 * https://developers.google.com/books/docs/v1/reference/volumes
 */
public class QueryGoogle {

	/**
	 * Query google for authors.
	 *
	 * @param author
	 *            Name of author to query
	 * @return list of Authors returned from google
	 */
	static java.util.List<wpff.Author> getAuthor(String googleApiKey, String author) {

		// List of Authors to be returned.
		java.util.List<Author> authorList = new ArrayList<Author>();

		if ((googleApiKey == null) || (googleApiKey.isEmpty())) {
			System.out.println("No key for google api.");
		} else {
			try {
				JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

				// Google Books entry point
				final Books books = new Books.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, null)
						.setApplicationName("Books!")
						.setGoogleClientRequestInitializer(new BooksRequestInitializer(googleApiKey)).build();

				// Query to google
				String query = "inauthor:" + author;
				// list of volumes
				List volumesList = books.volumes().list(query);

				System.out.println("AUTHOR.query: " + author);

				// Execute the query.
				Volumes volumes = volumesList.execute();
				if (volumes.getTotalItems() == 0 || volumes.getItems() == null) {
					// return empty list
					System.out.println("AUTHOR.query: Got 0 items");
					return authorList;
				}

				// Temporary map to store Author names
				Map<String, String> authorMap = new HashMap<String, String>();

				// Google query returns a set of Volumes instead of just authors.
				// Go through each Volume and extract the authors name.
				// This loop will grab the first authors name and use that
				for (Volume volume : volumes.getItems()) {
					Volume.VolumeInfo volumeInfo = volume.getVolumeInfo();

					// Author
					java.util.List<String> authors = volumeInfo.getAuthors();

					// grab first name.
					String authorText = "";
					if (authors != null && !authors.isEmpty()) {
						authorText = authors.get(0);
					}

					authorMap.put(authorText, "");
				}

				// Go through each author name and add to list
				authorList = authorMap.keySet().stream().map(i -> new Author(i)).collect(Collectors.toList());

			} catch (Exception e) {
				// TODO
				e.printStackTrace();
			}
		}

		return authorList;
	}

	/**
	 * Query google for books.
	 */
	static java.util.List<Book> getBooks(String googleApiKey, String author, String title) {

		// List of Books to be returned.
		java.util.List<Book> bookList = new ArrayList<Book>();

		if ((googleApiKey == null) || (googleApiKey.isEmpty())) {
			System.out.println("No key for google api.");
		} else {
			try {
				JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

				// Google Books entry point
				final Books books = new Books.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, null)
						.setApplicationName("Books!")
						.setGoogleClientRequestInitializer(new BooksRequestInitializer(googleApiKey)).build();

				// Query to google
				String query = "inauthor:" + author + " intitle:" + title;
				// List of volumes (books)
				List volumesList = books.volumes().list(query);

				// Execute the query.
				Volumes volumes = volumesList.execute();
				if (volumes.getTotalItems() == 0 || volumes.getItems() == null) {
					return bookList;
				}

				// Loop through all volumes/books from query
				for (Volume volume : volumes.getItems()) {
					Volume.VolumeInfo volumeInfo = volume.getVolumeInfo();

					BookBuilder bookBuilder = new BookBuilder();

					// Author
					java.util.List<String> authors = volumeInfo.getAuthors();
					if (authors != null && !authors.isEmpty()) {
						bookBuilder.setAuthor(authors.get(0));
					}

					// Title
					bookBuilder.setTitle(volumeInfo.getTitle());

					// Pub date
					bookBuilder.setPublicationDate(volumeInfo.getPublishedDate());

					// ISBN
					java.util.List<Volume.VolumeInfo.IndustryIdentifiers> ids = volumeInfo.getIndustryIdentifiers();
					for (Volume.VolumeInfo.IndustryIdentifiers ii : ids) {
						String volumeInfoType = ii.getType();
						if (volumeInfoType.equalsIgnoreCase("ISBN_13")) {
							bookBuilder.addId(ID_TYPE.ISBN_13, ii.getIdentifier());
						}
						if (volumeInfoType.equalsIgnoreCase("ISBN_10")) {
							bookBuilder.addId(ID_TYPE.ISBN_10, ii.getIdentifier());
						}
					}

					Book newBook = bookBuilder.build();
					bookList.add(newBook);
				}
			} catch (Exception e) {
				// TODO
				e.printStackTrace();
			}
		}

		return bookList;
	}

}
