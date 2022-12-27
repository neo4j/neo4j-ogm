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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.neo4j.ogm.domain.mappings.Article;
import org.neo4j.ogm.domain.mappings.Person;
import org.neo4j.ogm.domain.mappings.RichRelation;
import org.neo4j.ogm.domain.mappings.Tag;
import org.neo4j.ogm.domain.versioned_rel.Service;
import org.neo4j.ogm.domain.versioned_rel.Template;
import org.neo4j.ogm.domain.versioned_rel.UsedBy;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Nils Dröge
 * @author Michael J. Simons
 */
public class RichRelationTest extends TestContainersTestBase {


    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeAll
    public static void prepareSessionFactory() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.mappings", "org.neo4j.ogm.domain.versioned_rel");
    }

    @BeforeEach
    public void init() throws IOException {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    /**
     * Outgoing relationships from one start node targeting a different endnode should not
     * influence each others version properties through the process of removing obsolete
     * relationships from the mapping context.
     */
    @Test
    @RepeatedTest(20)
    void versionedRelationshipsTargetingDifferentEndNodes() {
        final Session localSession = sessionFactory.openSession();

        Service serviceA = new Service();
        serviceA.setName("A service");

        Service serviceB = new Service();
        serviceB.setName("B service");

        Template template = new Template();
        template.setName("A template");

        UsedBy userAUsingA = new UsedBy();
        userAUsingA.setUser("A user");
        userAUsingA.setService(serviceA);
        userAUsingA.setTemplate(template);

        UsedBy userBUsingA = new UsedBy();
        userBUsingA.setUser("B user");
        userBUsingA.setService(serviceA);
        userBUsingA.setTemplate(template);

        UsedBy userBUsingB = new UsedBy();
        userBUsingB.setUser("B user");
        userBUsingB.setService(serviceB);
        userBUsingB.setTemplate(template);

        template.setUsedBy(new HashSet<>(Arrays.asList(userAUsingA, userBUsingA, userBUsingB)));
        serviceA.setUsedBy(userAUsingA);

        localSession.save(template);

        Template loaded = null;
        loaded = new Template();
        loaded.setName("new name");
        loaded.setId(template.getId());
        loaded.setUuid(template.getUuid());
        loaded.setOptlock(template.getOptlock());
        loaded.set_identifier(template.get_identifier());
        loaded.setRef(template.getRef());
        localSession.save(loaded);
    }

    // DATAGRAPH-715
    @Test
    void shouldCreateARichRelation() {
        Person person = new Person();
        session.save(person);

        Article article1 = new Article();
        session.save(article1);
        Article article2 = new Article();
        session.save(article2);

        RichRelation relation1 = new RichRelation();
        person.addRelation(article1, relation1);
        session.save(person, 1);
        session.clear();

        RichRelation relation2 = new RichRelation();
        person.addRelation(article2, relation2);
        session.save(person, 1);
    }

    // GH-46
    @Test
    void shouldUpdateEndNodeEntityWithoutException() {
        Person person = new Person();
        session.save(person);

        Article article1 = new Article();
        Tag tag1 = new Tag("tag1");
        article1.tags = Collections.singletonList(tag1);
        session.save(article1);
        RichRelation relation1 = new RichRelation();
        person.addRelation(article1, relation1);
        session.save(person, 1);

        Article updateArticle = session.load(Article.class, article1.getNodeId(), 1);
        assertThat(((RichRelation) updateArticle.relations.toArray()[0]).article).isSameAs(updateArticle);
        updateArticle.tags = Collections.singletonList(new Tag("tag2"));
        session.save(updateArticle, 1);

        updateArticle = session.load(Article.class, article1.getNodeId(), 1);
        assertThat(((RichRelation) updateArticle.relations.toArray()[0]).article).isSameAs(updateArticle);
        session.save(updateArticle, 1);
    }

    // DATAGRAPH-730
    @Test
    void shouldSaveRelationshipEntityWhenNoReferencesToRelationshipEntityOnEitherStartOrEndNode() {

        RichRelation relation = new RichRelation();
        Person person = new Person();
        Article article = new Article();

        relation.person = person;
        relation.article = article;

        session.save(relation);

        assertThat(person.getNodeId()).isNotNull();
        assertThat(article.getNodeId()).isNotNull();

        session.clear();

        Person savedPerson = session.load(Person.class, person.getNodeId());
        Article savedArticle = session.load(Article.class, article.getNodeId());

        assertThat(savedPerson).isNotNull();
        assertThat(savedArticle).isNotNull();
    }

    // DATAGRAPH-730
    @Test
    void shouldSaveRelationshipEntityWhenReferenceToRelationshipEntityOnStartNodeOnly() {

        RichRelation relation = new RichRelation();

        Person person = new Person();
        Article article = new Article();

        relation.person = person;
        relation.article = article;

        person.relations.add(relation);

        session.save(relation);

        assertThat(person.getNodeId()).isNotNull();
        assertThat(article.getNodeId()).isNotNull();

        session.clear();

        Person savedPerson = session.load(Person.class, person.getNodeId());
        Article savedArticle = session.load(Article.class, article.getNodeId());

        assertThat(savedPerson).isNotNull();
        assertThat(savedArticle).isNotNull();
    }

    // DATAGRAPH-730
    @Test
    void shouldSaveRelationshipEntityWhenReferenceToRelationshipEntityOnEndNodeOnly() {

        RichRelation relation = new RichRelation();

        Person person = new Person();
        Article article = new Article();

        relation.person = person;
        relation.article = article;

        article.relations.add(relation);

        session.save(relation);

        assertThat(person.getNodeId()).isNotNull();
        assertThat(article.getNodeId()).isNotNull();

        session.clear();

        Person savedPerson = session.load(Person.class, person.getNodeId());
        Article savedArticle = session.load(Article.class, article.getNodeId());

        assertThat(savedPerson).isNotNull();
        assertThat(savedArticle).isNotNull();
    }
}
