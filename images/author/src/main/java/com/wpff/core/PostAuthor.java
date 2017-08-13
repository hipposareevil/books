package com.wpff.core;

import java.util.Objects;

// Swagger
import io.swagger.annotations.*;

/**
 * Author bean that is used in the POST method of the AuthorResource.
 * This is used to present a view of the Author for creation and is
 * thus missing the 'id' that is present in the normal Author bean.
 */
public class PostAuthor {
  private String name;

  private String imageUrl;

  public PostAuthor() {
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String url) {
    this.imageUrl = url;
  }


}
