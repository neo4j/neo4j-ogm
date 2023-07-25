package org.neo4j.ogm.domain.gh952;

import java.time.Instant;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.annotation.typeconversion.DateLong;

@RelationshipEntity(HumanIsParentOf.TYPE)
public class HumanIsParentOf {

	public static final String TYPE = "PARENT_OF";

	@Id
	@GeneratedValue(strategy = UuidGenerationStrategy.class)
	private String uuid;

	@StartNode
	private Human parent;

	@EndNode
	private Human child;

	@DateLong
	private Instant lastMeeting;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Human getParent() {
		return parent;
	}

	public void setParent(Human parent) {
		this.parent = parent;
	}

	public Human getChild() {
		return child;
	}

	public void setChild(Human child) {
		this.child = child;
	}

	public Instant getLastMeeting() {
		return lastMeeting;
	}

	public void setLastMeeting(Instant lastMeeting) {
		this.lastMeeting = lastMeeting;
	}

}
