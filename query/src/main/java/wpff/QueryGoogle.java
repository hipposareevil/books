package wpff;

import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.util.stream.Collectors;


// google query
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.books.Books;
import com.google.api.services.books.BooksRequestInitializer;
import com.google.api.services.books.Books.Volumes.List;
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volumes;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.NumberFormat;


/**
 * Utility to query google for book info
 */
public class QueryGoogle {


  /**
   * Query for author
   */
  static java.util.List<Author> getAuthor(String googleApiKey, String author) {

    java.util.List<Author> authorList = new ArrayList<Author>();

    if ( (googleApiKey == null) ||
         (googleApiKey.isEmpty()) ) {
      System.out.println("No key for google api.");
    }
    else {
      try {
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        final Books books = new Books.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, null)
            .setApplicationName("Books!")
            .setGoogleClientRequestInitializer(new BooksRequestInitializer(googleApiKey))
            .build();

        String query = "inauthor:" + author;
        List volumesList = books.volumes().list(query);


        // Execute the query.
        Volumes volumes = volumesList.execute();
        if (volumes.getTotalItems() == 0 || volumes.getItems() == null) {
          return authorList;
        }

        Map<String, String> authorMap = new HashMap<String, String>();

        for (Volume volume : volumes.getItems()) {
          Volume.VolumeInfo volumeInfo = volume.getVolumeInfo();

          // Author
          java.util.List<String> authors = volumeInfo.getAuthors();
          String authorText = "";
          if (authors != null && !authors.isEmpty()) {
            authorText = authors.get(0);
          }

          authorMap.put(authorText, "");
        }

        // Go through each author name and add to list
        authorList = authorMap.keySet().stream().
            map(i -> new Author(i)).
            collect(Collectors.toList());

      }
      catch (Exception e) {
        // TODO
        e.printStackTrace();
      }
    }

    return authorList;
  }
  

  /**
   * Query for books
   */
  static java.util.List<Book> getBooks(String googleApiKey, String author, String title) {

    java.util.List<Book> list = new ArrayList<Book>();

    if ( (googleApiKey == null) ||
         (googleApiKey.isEmpty()) ) {
      System.out.println("No key for google api.");
    }
    else {
      try {
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        final Books books = new Books.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, null)
            .setApplicationName("Books!")
            .setGoogleClientRequestInitializer(new BooksRequestInitializer(googleApiKey))
            .build();

        String query = "inauthor:" + author + " intitle:" + title;

        List volumesList = books.volumes().list(query);

        // Execute the query.
        Volumes volumes = volumesList.execute();
        if (volumes.getTotalItems() == 0 || volumes.getItems() == null) {
          return list;
        }

        for (Volume volume : volumes.getItems()) {
          Volume.VolumeInfo volumeInfo = volume.getVolumeInfo();

          // Author
          java.util.List<String> authors = volumeInfo.getAuthors();
          String authorText = "";
          if (authors != null && !authors.isEmpty()) {
            authorText = authors.get(0);
          }

          // Create new book from title & author
          Book newBook = new Book(volumeInfo.getTitle(), authorText);

          list.add(newBook);
        }
      }
      catch (Exception e) {
        // TODO
        e.printStackTrace();
      }
    }

    return list;
  }

}

