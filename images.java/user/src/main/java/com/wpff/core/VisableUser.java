package com.wpff.core;


/**
 * Truncated version of User.
 * This will be returned from 'getUsers'.
 */
public class VisableUser {
  private int id;
  private String name;
  private String userGroup;

  public VisableUser(String name, String userGroup, int id) {
    this.name = name;
    this.userGroup = userGroup;
    this.id = id;
  }
  
  public String getUserGroup() {
	  return this.userGroup;
  }

  public int getId() {
    return this.id;
  }

  public String getName() {
    return name;
  }

}

