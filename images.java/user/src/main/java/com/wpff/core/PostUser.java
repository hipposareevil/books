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
  private String userGroup;
  private String data;
  // Not encrypted
  private String password;

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
	 * @return the group
	 */
	public String getUserGroup() {
		return userGroup;
	}

	/**
	 * @param group
	 *            the group to set
	 */
	public void setUserGroup(String group) {
		this.userGroup = group;
	}

	/**
	 * Set the password, no encryption performed.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PostUser [name=");
		builder.append(name);
		builder.append(", group=");
		builder.append(userGroup);
		builder.append(", data=");
		builder.append(data);
		builder.append(", password=");
		builder.append(password);
		builder.append("]");
		return builder.toString();
	}
}
