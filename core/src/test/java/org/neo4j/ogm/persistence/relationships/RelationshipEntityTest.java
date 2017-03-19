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

package org.neo4j.ogm.persistence.relationships;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.exception.MappingException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class RelationshipEntityTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;
    private Session session;

    private U u;
    private M m;
    private R r1;


    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory("org.neo4j.ogm.persistence.relationships");
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

        assertEquals(2, m.rset.size());
    }

    @Test
    public void shouldUpdateExistingR() {

        session.save(u);

        r1.stars = 3;

        session.save(u);
        session.clear();

        m = session.load(M.class, m.id);

        assertEquals(1, m.rset.size());
        assertEquals(3, m.rset.iterator().next().stars.intValue());
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
        assertNotNull(m);
        assertEquals(0, m.rset.size());

        assertNotNull(u);
        assertEquals(0, u.rset.size());
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

        assertEquals(1, m.rset.size());
        assertEquals(0, m.rset.iterator().next().stars.intValue());
    }

    @Test
    public void shouldDirectlyAddR() {

        session.save(r1);
        session.clear();

        r1 = session.load(R.class, r1.id);

        assertEquals(1, r1.m.rset.size());
        assertEquals(1, r1.u.rset.size());
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
        assertEquals(1, r1.m.rset.size());
        assertEquals(1, r1.u.rset.size());
        assertEquals(Integer.valueOf(5), r1.stars);

        u = session.load(U.class, u.id);
        assertEquals(1, u.rset.size());
        m = session.load(M.class, m.id);
        assertEquals(1, m.rset.size());
        assertEquals(Integer.valueOf(5), m.rset.iterator().next().stars);
    }

    @Test
    public void shouldDirectlyDeleteR() {

        session.save(r1);

        assertNull(session.getTransaction());

        r1 = session.load(R.class, r1.id);
        u = session.load(U.class, u.id);
        m = session.load(M.class, m.id);

        assertNotNull(m);
        assertNotNull(u);
        assertNotNull(r1);
        assertNull(session.getTransaction());

        u.rset.clear();
        m.rset.clear();

        session.delete(r1);
        assertNull(session.getTransaction());

        session.clear();

        M qryM = session.queryForObject(M.class, "MATCH (n:M) return n", Utils.map());
        M findM = session.queryForObject(M.class, "MATCH (n:M) WHERE ID(n) = { id } RETURN n", Utils.map("id", m.id));
        M findM2 = session.queryForObject(M.class, "MATCH (n:M) WHERE ID(n) = { id } WITH n MATCH p=(n)-[*0..1]-(m) RETURN nodes(p)", Utils.map("id", m.id));

        assertNotNull(qryM);
        assertNotNull(findM);
        assertNotNull(findM2);

        session.clear();

        assertNull(session.load(R.class, r1.id));

        m = session.load(M.class, m.id);
        assertNotNull(m);
        assertEquals(0, m.rset.size());

        u = session.load(U.class, u.id);
        assertNotNull(u);
        assertEquals(0, u.rset.size());
    }

    /**
     * @see DATAGRAPH-706
     */
    @Test
    public void shouldReplaceOneEndOfR() {
        session.save(u);

        M m2 = new M("Lost");
        r1.m = m2;

        session.save(r1);
        assertEquals(m2, u.rset.iterator().next().m);

        session.clear();

        u = session.load(U.class, u.id);
        assertEquals(1, u.rset.size()); //we've lost all R's from u
        assertEquals(m2.title, u.rset.iterator().next().m.title);
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

        Assert.assertNotNull(link.id);
        Arc reloaded = session.queryForObject(Arc.class, "MATCH (f:Vertex)-[a:Arc]->(t:Vertex) return f, a, t", Utils.map());

        Assert.assertNotNull(reloaded);
        Assert.assertNotNull(reloaded.from);
        Assert.assertNotNull(reloaded.to);

        Assert.assertEquals(link.id, reloaded.id);
        Assert.assertEquals(from.id, reloaded.from.id);
        Assert.assertEquals(to.id, reloaded.to.id);
    }

    @NodeEntity(label = "U")
    public static class U {

        Long id;
        String name;
        @Relationship(type = "EDGE", direction = Relationship.OUTGOING)
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
        @Relationship(type = "EDGE", direction = Relationship.INCOMING)
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
