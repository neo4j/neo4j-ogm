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
