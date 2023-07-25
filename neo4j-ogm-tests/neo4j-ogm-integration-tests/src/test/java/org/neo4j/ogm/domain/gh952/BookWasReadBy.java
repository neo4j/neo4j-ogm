package org.neo4j.ogm.domain.gh952;

import java.time.Instant;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.annotation.typeconversion.DateLong;

@RelationshipEntity("READ_BY")
public class BookWasReadBy {

	public static final String TYPE = "READ_BY";

	@Id
	@GeneratedValue(strategy = UuidGenerationStrategy.class)
	private String uuid;

	@DateLong
	private Instant date;

	@StartNode
	private Book book;

	@EndNode
	private Human human;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Instant getDate() {
		return date;
	}

	public void setDate(Instant date) {
		this.date = date;
	}

	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}

	public Human getHuman() {
		return human;
	}

	public void setHuman(Human human) {
		this.human = human;
	}

}
