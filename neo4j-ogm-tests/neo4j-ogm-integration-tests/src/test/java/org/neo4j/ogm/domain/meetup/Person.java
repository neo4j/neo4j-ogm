/*
 * Copyright (c) 2002-2022 "Neo4j,"
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
package org.neo4j.ogm.domain.meetup;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Luanne Misquitta
 */
@NodeEntity(label = "Person")
public class Person {

    @Id @GeneratedValue
    private Long id;

    private String name;

    @Relationship(type = "ORGANISER", direction = Relationship.Direction.INCOMING)
    private Set<Meetup> meetupOrganised = new HashSet<>(); //must be a collection

    @Relationship(type = "ATTENDEE", direction = Relationship.Direction.INCOMING)
    private Set<Meetup> meetupsAttended = new HashSet<>();

    public Person() {
    }

    public Person(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Meetup> getMeetupOrganised() {
        return meetupOrganised;
    }

    public void setMeetupOrganised(Set<Meetup> meetupOrganised) {
        this.meetupOrganised = meetupOrganised;
    }

    public Set<Meetup> getMeetupsAttended() {
        return meetupsAttended;
    }

    public void setMeetupsAttended(Set<Meetup> meetupsAttended) {
        this.meetupsAttended = meetupsAttended;
    }
}
