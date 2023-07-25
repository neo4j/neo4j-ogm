package org.neo4j.ogm.domain.gh952;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity(Book.LABEL)
public class Book {

	public static final String LABEL = "Book";

	@Id
	@GeneratedValue(strategy = UuidGenerationStrategy.class)
	private String uuid;

	private String title;

	private List<Human> readBy = List.of();

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<Human> getReadBy() {
		return Collections.unmodifiableList(readBy);
	}

	public void setReadBy(List<Human> readBy) {
		this.readBy = new ArrayList<>(readBy);
	}

}
