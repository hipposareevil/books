package com.wpff.core;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

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
	 * User ID.
	 */
	@Column(name = "user_id", unique = true, nullable = false)
	@ApiModelProperty(hidden = true)
	private int id;


	/**
	 * Name of User
	 */
	@Id
	@Column(name = "name", unique = true, nullable = false)
	private String name;

	/**
	 * Data for the user. May be empty
	 */
	@Column(name = "data", nullable = true)
	private String data;

	/**
	 * Users password. Encrypted via jasypt
	 */
	@Column(name = "password", nullable = false)
	private String password;

	
	
  /**
   * User's group
   */
  @Column(name = "user_group", nullable = true)
  private String user_group;

 
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

	public String toString() {
		return "User[name=" + name + "]";
	}

	public String getData() {
		return data;
	}

	@ApiModelProperty(hidden = true)
	public int getId() {
		return this.id;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return this.password;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ApiModelProperty(hidden = true)
	public void setId(int id) {
		this.id = id;
	}

	public void setData(String data) {
		this.data = data;
	}

	/**
	 * Encrypts the incoming password and sets it.
	 *
	 * @param password
	 *            plain text password that will be encrypted.
	 */
	public void setPassword(String password) {
		BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
		String encryptedPassword = passwordEncryptor.encryptPassword(password);
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

		return Objects.equals(this.name, that.name) && Objects.equals(this.password, that.password) && Objects
				.equals(this.id, that.id) && Objects.equals(this.data, that.data);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.name, this.id, this.data, this.password);
	}
}
