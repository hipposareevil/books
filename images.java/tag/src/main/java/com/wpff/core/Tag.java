package com.wpff.core;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Named query to select all tags
 */
@Entity
@Table(name = "tag")

/**
 * Tag class
 */
public class Tag {

	/**
	 * Tag ID.
	 */
	@Id
	@Column(name = "tag_id", unique = true, nullable = false)
	private int id;

	/**
	 * Name of Tag
	 */
	@Column(name = "name", unique = true, nullable = false)
	private String name;

	/**
	 * Default constructor
	 */
	public Tag() {
	}

	public Tag(String name, int id) {
		this.name = name;
		this.id = id;
	}

	public String toString() {
		return "Tag[id=" + id + ", " +
				"name=" + name + "]";
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setId(int id) {
		this.id = id;
	}



	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Tag)) {
			return false;
		}

		final Tag that = (Tag) o;

		return Objects.equals(this.name, that.name) &&
				Objects.equals(this.id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.name, this.id);
	}
}
