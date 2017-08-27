package com.wpff.core;

/**
 * Tag class used in the Post methods
 */
public class PostTag {

	private String name;

	private String data;

	public String toString() {
		return "PostTag[name=" + name + "]";
	}

	public String getData() {
		return data;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setData(String data) {
		this.data = data;
	}

}