package com.wpff.core;

/**
 * Tag class used in the Post methods. Different than the Tag class for the
 * swagger definitions.
 */
public class PostTag {

	private String name;

	public String toString() {
		return "PostTag[name=" + name + "]";
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}