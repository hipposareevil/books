package com.wpff.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.jasypt.util.password.BasicPasswordEncryptor;

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
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  /**
   * Name of User
   */
  @Column(name = "name", unique=true, nullable = false)
  private String name;

  /**
   * User's group
   */
  @Column(name = "user_group", nullable = true)
  private String user_group;


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
  
  
  	/**
	 * @return the group
	 */
	public String getUserGroup() {
		return user_group;
	}

	/**
	 * @param group
	 *            the group to set
	 */
	public void setUserGroup(String group) {
		this.user_group = group;
	}


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


  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("User [id=");
    builder.append(id);
    builder.append(", name='");
    builder.append(name);
    builder.append("', group='");
    builder.append(user_group);
    builder.append("', data='");
    builder.append(data);
    builder.append("', password='");
    builder.append(password);
    builder.append("']");
    return builder.toString();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((data == null) ? 0 : data.hashCode());
    result = prime * result + ((user_group == null) ? 0 : user_group.hashCode());
    result = prime * result + id;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((password == null) ? 0 : password.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    User other = (User) obj;
    if (data == null) {
      if (other.data != null)
        return false;
    } else if (!data.equals(other.data))
      return false;
    
    if (user_group == null) {
      if (other.user_group != null)
        return false;
    } else if (!user_group.equals(other.user_group))
      return false;
    
    if (id != other.id)
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (password == null) {
      if (other.password != null)
        return false;
    } else if (!password.equals(other.password))
      return false;
    return true;
  }

  

}
