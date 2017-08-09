package com.wpff.core;


import java.io.Serializable;

/**
 * Utility bean to group a user name and password for authentication
 */
public class Credentials implements Serializable {

  /**
   * User to authenticate
   */
  private String name;

  /**
   * Password for user

   * Password should be encrypted
   */
  private String password;

  /**
   * Create empty bean
   */
  public Credentials() {
  }

  public String getName(){
    return this.name;
  }

  public String getPassword(){
    return this.password;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPassword(String password) {
    this.password = password;
  }

}
