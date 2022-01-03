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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.forum.Member;
import org.neo4j.ogm.domain.forum.activity.Activity;
import org.neo4j.ogm.domain.forum.activity.Comment;
import org.neo4j.ogm.domain.forum.activity.Post;
import org.neo4j.ogm.domain.gh806.ConcreteElement;
import org.neo4j.ogm.domain.gh806.Container;
import org.neo4j.ogm.domain.gh806.Element;
import org.neo4j.ogm.domain.gh806.EvenMoreConcreteElement;
import org.neo4j.ogm.domain.gh806.IElementImpl1;
import org.neo4j.ogm.domain.gh806.IElementImpl1A;
import org.neo4j.ogm.domain.gh806.VeryConcreteElementA;
import org.neo4j.ogm.domain.hierarchy.relations.BaseEntity;
import org.neo4j.ogm.domain.hierarchy.relations.Type1;
import org.neo4j.ogm.domain.hierarchy.relations.Type2;
import org.neo4j.ogm.domain.hierarchy.relations.Type3;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;
import org.neo4j.ogm.transaction.Transaction;

/**
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class HierarchyRelsTest extends TestContainersTestBase {

    private Session session;

    @Before
    public void init() throws IOException {
        session = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.hierarchy.relations",
            "org.neo4j.ogm.domain.forum", "org.neo4j.ogm.domain.gh806")
            .openSession();
        session.purgeDatabase();
    }

    @After
    public void cleanup() {
        //r  session.purgeDatabase();
        session.clear();
    }

    @Test // GH-806
    public void relationshipsToSubclassesShouldBeClearedAsWell() {

        inheritanceImpl(s -> new Element(s), Container::setElement);
        inheritanceImpl(s -> new ConcreteElement(s), Container::setElement);
        inheritanceImpl(s -> new VeryConcreteElementA(s), Container::setElement);
        inheritanceImpl(s -> new EvenMoreConcreteElement(s), Container::setElement);

        inheritanceImpl(s -> new IElementImpl1(s), Container::setElement2);
        inheritanceImpl(s -> new IElementImpl1A(s), Container::setElement2);
    }

    <T> void inheritanceImpl(Function<String, T> portProvider, BiConsumer<Container, Set<T>> elementConsumer) {

        session.query("MATCH (n) DETACH DELETE n", Collections.emptyMap());
        session.clear();

        // Setup initial relationships in one tx
        Container card = new Container("container");
        T port1 = portProvider.apply("e1");
        T port2 = portProvider.apply("e2");
        elementConsumer.accept(card, new HashSet<>(Arrays.asList(port1, port2)));

        card.setElementsOfAnotherRelationship(Collections.singleton(new ConcreteElement("oe")));

        session.save(card);
        session.clear();

        // Verify state
        String verificationQuery = "match (c:Container) <- [:RELATES_TO|RELATES_TO_TOO|RELATES_TO_AS_WELL] - (p) "
            + "where any (label in labels(p) where label in $expectedLabels) return c.name as c, p.name as p";
        Set<String> expectedLabels = new HashSet<>(Arrays.asList(ConcreteElement.class.getSimpleName(), port1.getClass().getSimpleName()));
        Map<String, Object> parameters = Collections.singletonMap("expectedLabels", expectedLabels);

        Result r;
        r = session.query(verificationQuery, parameters);
        assertThat(r.queryResults()).hasSize(3);
        assertThat(r.queryResults()).extracting(m -> m.get("p")).containsExactlyInAnyOrder("e1", "e2", "oe");

        // Reload in cleared session for fresh tx
        card = session.load(Container.class, card.getId());
        T port3 = portProvider.apply("e3");

        // Replace associations
        elementConsumer.accept(card, new HashSet<>(Arrays.asList(port3)));
        session.save(card);
        session.clear();

        r = session.query(verificationQuery, parameters);
        assertThat(r.queryResults()).hasSize(2);
        assertThat(r.queryResults()).extracting(m -> m.get("p")).containsExactlyInAnyOrder("e3", "oe");
    }

    @Test // GH-152
    public void saveMultipleRelationshipsToBase() {
        Type1 node1 = new Type1();
        node1.name = "type1";
        Type2 node2 = new Type2();
        node2.name = "type2";
        node1.addIncoming(node2);
        node2.addIncoming(node1);

        Transaction transaction = session.beginTransaction();
        session.save(node1);
        transaction.commit();
        transaction.close();

        session.clear();
        transaction = session.beginTransaction();
        BaseEntity entity = session.load(BaseEntity.class, node1.getGraphId());
        transaction.close();
        assertThat(entity.getIncoming()).hasSize(1);
        assertThat(entity.getOutgoing()).hasSize(1);
        assertThat(node2.getGraphId()).isEqualTo(entity.getIncoming().get(0).getGraphId());
        assertThat(node2.getGraphId()).isEqualTo(entity.getOutgoing().get(0).getGraphId());
    }

    @Test
    public void saveMultipleRelationsOfType() {
        Type3 node1 = new Type3();
        Type3 node2 = new Type3();

        node1.getType3In().add(node2);
        node1.getType3Out().add(node2);
        node2.getType3In().add(node1);
        node2.getType3Out().add(node1);

        session.save(node1);

        session.clear();
        Type3 type3_1 = session.load(Type3.class, node1.getGraphId());
        assertThat(type3_1.getType3In()).hasSize(1);
        assertThat(type3_1.getType3Out()).hasSize(1);

        session.clear();
        Type3 type3_2 = session.load(Type3.class, node2.getGraphId());
        assertThat(type3_2.getType3In()).hasSize(1);
        assertThat(type3_2.getType3Out()).hasSize(1);
    }

    @Test // GH-404
    public void shouldLoadRelationByAbstractParent() {

        Post post = new Post();
        post.setPost("sample post");

        Activity comment = new Comment();
        Date now = new Date();
        comment.setDate(now);

        Member member = new Member();
        member.setUserName("sample member");
        member.setActivityList(Arrays.asList(post, comment));

        session.save(member);
        session.clear();

        Member reloaded = session.load(Member.class, member.getId());
        assertThat(reloaded).isNotNull();
        assertThat(reloaded.getUserName()).isEqualTo("sample member");
        assertThat(reloaded.getActivityList()).hasSize(2);

        Post p = (Post) StreamSupport.stream(reloaded.getActivityList().spliterator(), false)
            .filter(elt -> elt.getClass().equals(Post.class)).findFirst().get();
        assertThat(p.getPost()).isEqualTo("sample post");

        Comment c = (Comment) StreamSupport.stream(reloaded.getActivityList().spliterator(), false)
            .filter(elt -> elt.getClass().equals(Comment.class)).findFirst().get();
        assertThat(c.getDate()).isEqualTo(now);
    }
}
