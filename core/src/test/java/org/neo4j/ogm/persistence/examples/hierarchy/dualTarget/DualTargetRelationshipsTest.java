/*
 * Copyright (c)  [2011-2015] "Neo Technology" / "Graph Aware Ltd."
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with separate copyright notices and license terms. Your use of the source code for these subcomponents is subject to the terms and conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 *
 */

package org.neo4j.ogm.persistence.examples.hierarchy.dualTarget;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.domain.hierarchy.dualTarget.Event;
import org.neo4j.ogm.domain.hierarchy.dualTarget.Member;
import org.neo4j.ogm.domain.hierarchy.dualTarget.Tag;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @see Issue 161
 * @author Luanne Misquitta
 */
public class DualTargetRelationshipsTest  extends MultiDriverTestClass{
	private Session session;

	@Before
	public void init() throws IOException {
		session = new SessionFactory(baseConfiguration, "org.neo4j.ogm.domain.hierarchy.dualTarget").openSession();
	}

	@After
	public void teardown() {
		session.purgeDatabase();
	}

	@Test
	public void shouldBeAbleToRemoveRelationship() {
		Event event = new Event("Event 1");

		Member member = new Member("Member 1");

		Tag tag = new Tag("Tag 1");

		member.getEvents().add(event);
		member.getTags().add(tag);
		event.setCreator(member);
		event.getTags().add(tag);
		tag.setCreator(member);
		tag.getEvents().add(event);

		session.save(event);

		event.getTags().remove(tag);
		tag.getEvents().remove(event);
		session.save(event);
		session.save(tag);

		session.clear();

		Event e = session.load(Event.class, event.getId(),2); //Depth 2 required for it to fail
		assertEquals(0, e.getTags().size());
		assertEquals(member.getId(), e.getCreator().getId());
		assertEquals(1, e.getCreator().getEvents().size());
		//session.clear(); //fails if the session is not cleared

		Member m = session.load(Member.class, member.getId(), 2);
		assertEquals(1, m.getTags().size());
		assertEquals(1, m.getEvents().size());

	}

	@Test
	public void shouldBeAbleToCreateSharedTags() {
		Member member = new Member("Member1");

		Event event1 = new Event("Event1");
		event1.setCreator(member);
		member.getEvents().add(event1);

		Event event2 = new Event("Event2");
		event2.setCreator(member);
		member.getEvents().add(event2);

		Tag tag1 = new Tag("Tag1");
		tag1.setCreator(member);
		member.getTags().add(tag1);
		tag1.getEvents().add(event1);
		event1.getTags().add(tag1);
		member.getTags().add(tag1);

		Tag tag2 = new Tag("Tag2");
		tag2.setCreator(member);
		member.getTags().add(tag2);
		tag2.getEvents().add(event1);
		tag2.getEvents().add(event2);
		event1.getTags().add(tag2);
		event2.getTags().add(tag2);
		member.getTags().add(tag2);

		assertEquals(2, event1.getTags().size());
		assertEquals(1, event2.getTags().size());

		session.save(member);

		Member m = session.loadAll(Member.class, new Filter("name","Member1"), 2).iterator().next();
		assertEquals(2, m.getTags().size());
		assertEquals(2, m.getEvents().size());

		Event e = session.load(Event.class, event1.getId(), 2);
		assertEquals(2, e.getTags().size());

	}


}
