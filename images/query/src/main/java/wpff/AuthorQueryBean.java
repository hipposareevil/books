package wpff;

/**
 * Represents an author
 */
public class AuthorQueryBean {

  private final String name;

  public AuthorQueryBean(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }
}
