package org.neo4j.ogm.domain.types;

import java.time.ZonedDateTime;

import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class EntityWithUnmanagedFieldType {

	private Long id;
	// using ZonedDateTime but could be any type unmanaged by OGM
	private ZonedDateTime date;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ZonedDateTime getDate() {
		return date;
	}

	public void setDate(ZonedDateTime date) {
		this.date = date;
	}
}
