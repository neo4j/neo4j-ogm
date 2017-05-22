/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

package org.neo4j.ogm.persistence.relationships;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.domain.mappings.Category;
import org.neo4j.ogm.domain.mappings.Event;
import org.neo4j.ogm.domain.mappings.Tag;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Nils Dr\u00F6ge
 * @author Luanne Misquitta
 */
public class DualTargetEntityRelationshipTest extends MultiDriverTestClass {

    private Session session;

    @Before
    public void init() throws IOException {
        session = new SessionFactory(driver, "org.neo4j.ogm.domain.mappings").openSession();
		session.purgeDatabase();
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void mappingShouldConsiderClasses() {

        Category category = new Category("cat1");

        Tag tag1 = new Tag("tag1");
        Set<Tag> tags = new HashSet<>();
        tags.add(tag1);

        Event event = new Event("title");
        event.setCategory(category);
        event.setTags(tags);

        session.save(event);

        assertNotNull(event.getNodeId());
        assertNotNull(event.getCategory().getNodeId());
        assertNotNull(event.getTags().iterator().next().getNodeId());

        session.clear();
        event = session.load(Event.class, event.getNodeId(), 1);

        assertNotNull(event);
        assertEquals(category, event.getCategory());
        assertEquals(tag1, event.getTags().iterator().next());
    }

    /**
     * @see DATAGRAPH-690
     */
    @Test
    public void shouldKeepAllRelations() {

        Category category = new Category("cat1");

        Tag tag1 = new Tag("tag1");
        Set<Tag> tags = new HashSet<>();
        tags.add(tag1);

        Event event = new Event("title");
        event.setCategory(category);
        event.setTags(tags);

        session.save(event);

        assertNotNull(event.getNodeId());
        assertNotNull(category.getNodeId());
        assertNotNull(tag1.getNodeId());

        session.clear();

        Collection<Tag> tagsFound = session.loadAll(Tag.class, new Filter("name", ComparisonOperator.EQUALS, "tag1"));
        assertEquals(1, tagsFound.size());
        event.setTags(new HashSet<>(tagsFound));

        Collection<Category> categoriesFound = session.loadAll(Category.class, new Filter("name", ComparisonOperator.EQUALS, "cat1"));
        assertEquals(1, categoriesFound.size());
        event.setCategory(categoriesFound.iterator().next());

        assertEquals(tag1, event.getTags().iterator().next());
        assertEquals(category, event.getCategory());
        session.save(event);

        session.clear();
        Event eventFound = session.load(Event.class, event.getNodeId(), 1);

        assertNotNull(eventFound.getNodeId());
        assertEquals(category, eventFound.getCategory());
        assertEquals(tag1, eventFound.getTags().iterator().next());
    }
}
