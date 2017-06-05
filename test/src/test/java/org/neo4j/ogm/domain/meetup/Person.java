/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.domain.meetup;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Luanne Misquitta
 */
@NodeEntity(label = "Person")
public class Person {

	@GraphId
	private Long id;

	private String name;

	@Relationship(type = "ORGANISER", direction = Relationship.INCOMING)
	private Set<Meetup> meetupOrganised = new HashSet<>(); //must be a collection

	@Relationship(type = "ATTENDEE", direction = Relationship.INCOMING)
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
