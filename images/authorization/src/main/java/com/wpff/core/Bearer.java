package com.wpff.core;

/**
 * Bean to represent authenticated information
 */
public class Bearer {
	
	private String token;
	private int userId;	

	
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

}
