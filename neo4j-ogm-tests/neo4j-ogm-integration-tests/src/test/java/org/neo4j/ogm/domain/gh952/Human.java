/*
 * Copyright (c) 2002-2023 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

	@Relationship(type = HumanIsParentOf.TYPE, direction = Relationship.Direction.OUTGOING)
	private List<HumanIsParentOf> children = Collections.emptyList();

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
