package com.wpff.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Bean representing a Book from the 'book' webservice.
 * 
 * Contains the following:
 * 
 * <pre>
{
  "authorId": 0,
  "firstPublishedYear": 0,
  "title": "string",
  "isbns": [
    "string"
  ],
  "subjects": [
    "string"
  ],
  "description": "string",
  "openlibraryWorkUrl": "string",
  "imageSmall": "string",
  "imageMedium": "string",
  "imageLarge": "string",
  "id": 0,
  "authorName": "string"
}
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookBean {

  private String title;

  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title
   *          the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

}
