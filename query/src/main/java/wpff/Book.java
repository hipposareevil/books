package wpff;

/**
 * Represents a book, with title and author.
 * TODO: grab isbn and year
 */
public class Book {

  private final String title;
  private final String author;

  public Book(String title, String author) {
    this.title = title;
    this.author = author;
  }


  public String getAuthor() {
    return this.author;
  }
  public String getTitle() {
    return this.title;
  }
}
