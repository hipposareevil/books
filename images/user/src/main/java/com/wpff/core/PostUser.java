package com.wpff.core;

/**
 * User bean that is used in the POST method of UserResource.
 * This is used to present a view of the User for creation and is
 * thus missing the 'id' that is present in the normal User bean.
 *
 * Note: the password is not encrypted in this bean
 */
public class PostUser {
  private String name;
  private String data;
  // Not encrypted
  private String password;

  public String toString() {
    return "PostUser[name=" + name + "]";
  }

  public String getData() {
    return data;
  }
  public void setData(String data) {
    this.data = data;
  }


  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String getPassword() {
    return this.password;
  }

  /**
   * Set the password, no encryption performed.
   */
  public void setPassword(String password) {
    this.password = password;
  }

}
