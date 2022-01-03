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
package org.neo4j.ogm.persistence.relationships;

import static org.assertj.core.api.Assertions.*;

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
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Nils Dröge
 * @author Luanne Misquitta
 */
public class DualTargetEntityRelationshipTest extends TestContainersTestBase {

    private Session session;

    @Before
    public void init() throws IOException {
        session = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.mappings").openSession();
        session.purgeDatabase();
    }

    @Test // DATAGRAPH-636
    public void mappingShouldConsiderClasses() {

        Category category = new Category("cat1");

        Tag tag1 = new Tag("tag1");
        Set<Tag> tags = new HashSet<>();
        tags.add(tag1);

        Event event = new Event("title");
        event.setCategory(category);
        event.setTags(tags);

        session.save(event);

        assertThat(event.getNodeId()).isNotNull();
        assertThat(event.getCategory().getNodeId()).isNotNull();
        assertThat(event.getTags().iterator().next().getNodeId()).isNotNull();

        session.clear();
        event = session.load(Event.class, event.getNodeId(), 1);

        assertThat(event).isNotNull();
        assertThat(event.getCategory()).isEqualTo(category);
        assertThat(event.getTags().iterator().next()).isEqualTo(tag1);
    }

    @Test // DATAGRAPH-690
    public void shouldKeepAllRelations() {

        Category category = new Category("cat1");

        Tag tag1 = new Tag("tag1");
        Set<Tag> tags = new HashSet<>();
        tags.add(tag1);

        Event event = new Event("title");
        event.setCategory(category);
        event.setTags(tags);

        session.save(event);

        assertThat(event.getNodeId()).isNotNull();
        assertThat(category.getNodeId()).isNotNull();
        assertThat(tag1.getNodeId()).isNotNull();

        session.clear();

        Collection<Tag> tagsFound = session.loadAll(Tag.class, new Filter("name", ComparisonOperator.EQUALS, "tag1"));
        assertThat(tagsFound).hasSize(1);
        event.setTags(new HashSet<>(tagsFound));

        Collection<Category> categoriesFound = session
            .loadAll(Category.class, new Filter("name", ComparisonOperator.EQUALS, "cat1"));
        assertThat(categoriesFound).hasSize(1);
        event.setCategory(categoriesFound.iterator().next());

        assertThat(event.getTags().iterator().next()).isEqualTo(tag1);
        assertThat(event.getCategory()).isEqualTo(category);
        session.save(event);

        session.clear();
        Event eventFound = session.load(Event.class, event.getNodeId(), 1);

        assertThat(eventFound.getNodeId()).isNotNull();
        assertThat(eventFound.getCategory()).isEqualTo(category);
        assertThat(eventFound.getTags().iterator().next()).isEqualTo(tag1);
    }
}
