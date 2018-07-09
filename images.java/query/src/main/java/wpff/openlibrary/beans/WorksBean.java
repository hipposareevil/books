package wpff.openlibrary.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * JSON data from openlibrary from a /works/ID query
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class WorksBean {
  
    public String getTextDescription() {
      if (this.description != null) {
        return this.description.getValue();
      }
      else {
        return "";
      }
    }
  
  public Description description;
    
  /**
   * @return the description
   */
  public Description getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(Description description) {
    this.description = description;
  }

  
  
}
