package org.neo4j.ogm.domain.gh952;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity(Human.LABEL)
public class Human {

	public static final String LABEL = "Human";

	@Id
	@GeneratedValue(strategy = UuidGenerationStrategy.class)
	private String uuid;

	private String name;

	@Relationship(type = HumanIsParentOf.TYPE, direction = Relationship.OUTGOING)
	private List<HumanIsParentOf> children;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<HumanIsParentOf> getChildren() {
		return Collections.unmodifiableList(children);
	}

	public void setChildren(List<HumanIsParentOf> children) {
		this.children = new ArrayList<>(children);
	}

}
