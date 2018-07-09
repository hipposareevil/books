package com.wpff.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Bean representing an Author from the 'author' webservice.
 * 
 * Contains the following:
 * 
 * <pre>
   {
    "name": "string",
    "birthDate": "string",
    "olKey": "string",
    "imageSmall": "string",
    "imageMedium": "string",
    "imageLarge": "string",
    "subjects": [
      "string"
    ],
    "id": 0
  }
 * </pre>
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class AuthorBean {
  public String name;

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }
  
  

}
