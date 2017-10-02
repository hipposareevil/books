package com.wpff.core;

/**
 * Bean to represent authenticated information
 */
public class Bearer {
	
	private String token;
	private int userId;	
	private String groupName;

	
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
  /**
   * @return the groupName
   */
  public String getGroupName() {
    return groupName;
  }
  /**
   * @param groupName the groupName to set
   */
  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

}
