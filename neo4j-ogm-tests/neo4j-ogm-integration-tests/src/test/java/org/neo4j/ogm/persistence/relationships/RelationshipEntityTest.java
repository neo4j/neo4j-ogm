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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.exception.core.MappingException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class RelationshipEntityTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private Session session;

    private U u;
    private M m;
    private R r1;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.persistence.relationships");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();

        u = new U("Luanne");
        m = new M("Taken");
        r1 = new R(u, m, "great!", 4);

        u.rset.add(r1);
        m.rset.add(r1);

        session.purgeDatabase();
    }

    @Test
    public void shouldAddR() {

        session.save(u);

        R r2 = new R(u, m, "even better next time!", 5);
        u.rset.add(r2);
        m.rset.add(r2);

        session.save(u);
        session.clear();

        m = session.load(M.class, m.id);

        assertThat(m.rset).hasSize(2);
    }

    @Test
    public void shouldUpdateExistingR() {

        session.save(u);

        r1.stars = 3;

        session.save(u);
        session.clear();

        m = session.load(M.class, m.id);

        assertThat(m.rset).hasSize(1);
        assertThat(m.rset.iterator().next().stars.intValue()).isEqualTo(3);
    }

    @Test
    public void shouldDeleteR() {

        session.save(u);

        u.rset.clear();
        m.rset.clear();

        session.save(u);

        session.clear();

        m = session.load(M.class, m.id);
        u = session.load(U.class, u.id);
        assertThat(m).isNotNull();
        assertThat(m.rset).isEmpty();

        assertThat(u).isNotNull();
        assertThat(u.rset).isEmpty();
    }

    @Test
    public void shouldReplaceExistingR() {

        session.save(u);

        R r3 = new R(u, m, "Only Gravity sucks more than this film", 0);

        u.rset.clear();
        u.rset.add(r3);

        m.rset.clear();
        m.rset.add(r3);

        session.save(u);
        session.clear();

        m = session.load(M.class, m.id);

        assertThat(m.rset).hasSize(1);
        assertThat(m.rset.iterator().next().stars.intValue()).isEqualTo(0);
    }

    @Test
    public void shouldDirectlyAddR() {

        session.save(r1);
        session.clear();

        r1 = session.load(R.class, r1.id);

        assertThat(r1.m.rset).hasSize(1);
        assertThat(r1.u.rset).hasSize(1);
    }

    /**
     * @see DATAGRAPH-582
     */
    @Test
    public void shouldDirectlyUpdateR() {

        session.save(r1);
        r1 = session.load(R.class, r1.id);
        r1.stars = 5;
        session.save(r1);
        session.clear();
        assertThat(r1.m.rset).hasSize(1);
        assertThat(r1.u.rset).hasSize(1);
        assertThat(r1.stars).isEqualTo(Integer.valueOf(5));

        u = session.load(U.class, u.id);
        assertThat(u.rset).hasSize(1);
        m = session.load(M.class, m.id);
        assertThat(m.rset).hasSize(1);
        assertThat(m.rset.iterator().next().stars).isEqualTo(Integer.valueOf(5));
    }

    @Test
    public void shouldDirectlyDeleteR() {

        session.save(r1);

        assertThat(session.getTransaction()).isNull();

        r1 = session.load(R.class, r1.id);
        u = session.load(U.class, u.id);
        m = session.load(M.class, m.id);

        assertThat(m).isNotNull();
        assertThat(u).isNotNull();
        assertThat(r1).isNotNull();
        assertThat(session.getTransaction()).isNull();

        u.rset.clear();
        m.rset.clear();

        session.delete(r1);
        assertThat(session.getTransaction()).isNull();

        session.clear();

        M qryM = session.queryForObject(M.class, "MATCH (n:M) return n", Collections.emptyMap());
        M findM = session.queryForObject(M.class, "MATCH (n:M) WHERE ID(n) = $id RETURN n", Collections.singletonMap("id", m.id));
        M findM2 = session
            .queryForObject(M.class, "MATCH (n:M) WHERE ID(n) = $id WITH n MATCH p=(n)-[*0..1]-(m) RETURN nodes(p)",
                Collections.singletonMap("id", m.id));

        assertThat(qryM).isNotNull();
        assertThat(findM).isNotNull();
        assertThat(findM2).isNotNull();

        session.clear();

        assertThat(session.load(R.class, r1.id)).isNull();

        m = session.load(M.class, m.id);
        assertThat(m).isNotNull();
        assertThat(m.rset).isEmpty();

        u = session.load(U.class, u.id);
        assertThat(u).isNotNull();
        assertThat(u.rset).isEmpty();
    }

    @Test // DATAGRAPH-706
    public void shouldReplaceOneEndOfR() {
        session.save(u);

        M m2 = new M("Lost");
        r1.m = m2;

        session.save(r1);
        assertThat(u.rset.iterator().next().m).isEqualTo(m2);

        session.clear();

        u = session.load(U.class, u.id);
        assertThat(u.rset).hasSize(1); //we've lost all R's from u
        assertThat(u.rset.iterator().next().m.title).isEqualTo(m2.title);
    }

    /**
     * @see DATAGRAPH-732
     */
    @Test(expected = MappingException.class)
    public void shouldThrowExceptionWhenTheStartNodeIsNull() {
        R invalidR = new R(null, m, "exception", 0);
        m.rset.add(invalidR);
        session.save(m);
    }

    /**
     * @see DATAGRAPH-732
     */
    @Test(expected = MappingException.class)
    public void shouldThrowExceptionWhenTheEndNodeIsNull() {
        R invalidR = new R(u, null, "exception", 0);
        u.rset.add(invalidR);
        session.save(u);
    }

    /**
     * @see DATAGRAPH-732
     */
    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionWhenRIsSavedWithMissingEndNodes() {
        R invalidR = new R(null, null, "exception", 0);
        session.save(invalidR);
    }

    /**
     * @see DATAGRAPH-944
     */
    @Test
    public void shouldReloadCompleteRelationshipWhenStartAndEndNodesDontDeclareTheRelationshipExplicitly() {

        Vertex from = new Vertex();
        Vertex to = new Vertex();
        from.name = "from";
        to.name = "to";

        Arc link = new Arc();
        link.from = from;
        link.to = to;
        link.created = System.currentTimeMillis();

        session.save(link);

        session.clear();

        assertThat(link.id).isNotNull();
        Arc reloaded = session
            .queryForObject(Arc.class, "MATCH (f:Vertex)-[a:Arc]->(t:Vertex) return f, a, t", Collections.emptyMap());

        assertThat(reloaded).isNotNull();
        assertThat(reloaded.from).isNotNull();
        assertThat(reloaded.to).isNotNull();

        assertThat(reloaded.id).isEqualTo(link.id);
        assertThat(reloaded.from.id).isEqualTo(from.id);
        assertThat(reloaded.to.id).isEqualTo(to.id);
    }

    @NodeEntity(label = "U")
    public static class U {

        Long id;
        String name;
        @Relationship(type = "EDGE", direction = Relationship.Direction.OUTGOING)
        Set<R> rset = new HashSet<>();

        public U() {
        }

        public U(String name) {
            this.name = name;
        }
    }

    @RelationshipEntity(type = "EDGE")
    public static class R {

        Long id;
        String comments;
        Integer stars;

        @StartNode
        U u;

        @EndNode
        M m;

        public R() {
        }

        public R(U u, M m, String comments, Integer stars) {
            this.u = u;
            this.m = m;
            this.comments = comments;
            this.stars = stars;
        }
    }

    @NodeEntity(label = "M")
    public static class M {

        Long id;
        String title;
        @Relationship(type = "EDGE", direction = Relationship.Direction.INCOMING)
        Set<R> rset = new HashSet<>();

        public M() {
        }

        public M(String title) {
            this.title = title;
        }
    }

    @NodeEntity(label = "Vertex")
    public static class Vertex {

        Long id;
        String name;
    }

    @RelationshipEntity(type = "Arc")
    public static class Arc {

        Long id;
        Long created;

        @StartNode
        Vertex from;

        @EndNode
        Vertex to;
    }
}
