package com.wpff.core;


/**
 * Truncated version of User.
 * This will be returned from 'getUsers'.
 */
public class VisableUser {
  private int id;
  private String name;

  public VisableUser(String name, int id) {
    this.name = name;
    this.id = id;
  }

  public int getId() {
    return this.id;
  }

  public String getName() {
    return name;
  }

}

