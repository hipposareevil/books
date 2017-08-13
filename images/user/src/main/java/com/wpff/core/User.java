package com.wpff.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.util.Objects;

import org.jasypt.util.password.BasicPasswordEncryptor;

import io.swagger.annotations.ApiModelProperty;

/**
 * Named query to select all users
 */
@Entity
@Table(name = "user")
@NamedQueries(
    {
        @NamedQuery(
          name = "com.wpff.core.User.findAll",
            query = "SELECT u FROM User u"
        )
    }
)
/**
 * User class
 */
public class User {
  /**
   * Users ID.
   */ 
  @Id
  @Column(name = "user_id", unique=true, nullable=false)
  private int id;

  /**
   * Name of User
   */
  @Column(name = "name", unique=true, nullable = false)
  private String name;

  /**
   * Data for the user. May be empty
   */
  @Column(name = "data", nullable=true)
  private String data;

  /**
   * Users password. Encrypted via jasypt
   */ 
  @Column(name = "password", nullable=false)
  private String password;

  public void setId(int id) {
    this.id = id;
  }

  public int getId() {
    return this.id;
  }


  public void setName(String name) {
    this.name = name;
  }


  public String getName() {
    return name;
  }

  public void setData(String data) {
    this.data = data;
  }

  public String getData() {
    return data;
  }


  public String getPassword() {
    return this.password;
  }


  /**
   * Encrypts the incoming password and sets it.
   *
   * @param password plain text password that will be encrypted.
   */
  public void setPassword(String password) {
    BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
    String encryptedPassword = passwordEncryptor.encryptPassword(password);    
    this.password = encryptedPassword;
  }

  /**
   * Sets the encrypted password. Should be used if the already
   * encrypted password should change.
   *
   * @param encryptedPassword Encrypted password to set on User
   */
  public void setEncryptedPassword(String encryptedPassword) {
    this.password = encryptedPassword;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof User)) {
      return false;
    }

    final User that = (User) o;

    return Objects.equals(this.name, that.name) &&
        Objects.equals(this.password, that.password) &&
        Objects.equals(this.id, that.id) &&
        Objects.equals(this.data, that.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name, this.id, this.data, this.password);
  }
}
