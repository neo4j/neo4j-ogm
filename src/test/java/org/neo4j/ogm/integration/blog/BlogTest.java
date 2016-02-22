/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.integration.blog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.neo4j.ogm.domain.blog.Author;
import org.neo4j.ogm.domain.blog.Comment;
import org.neo4j.ogm.domain.blog.Post;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.Neo4jIntegrationTestRule;

/**
 * @author Vince Bickers
 */
public class BlogTest {

    @ClassRule
    public static Neo4jIntegrationTestRule neo4jRule = new Neo4jIntegrationTestRule();

    private Session session;

    @Before
    public void init() throws IOException {
        session = new SessionFactory("org.neo4j.ogm.domain.blog").openSession(neo4jRule.url());
    }

    @Test
    public void shouldTraverseListOfBlogPosts() {

        Post p1 = new Post("first");
        Post p2 = new Post("second");
        Post p3 = new Post("third");
        Post p4 = new Post("fourth");

        p1.setNext(p2);
        p2.setNext(p3);
        p3.setNext(p4);

        assertEquals(p1, p2.getPrevious());

        assertEquals(p2, p1.getNext());
        assertEquals(p2, p3.getPrevious());

        assertEquals(p3, p2.getNext());
        assertEquals(p3, p4.getPrevious());

        assertEquals(p4, p3.getNext());
        assertNull(p4.getNext());


        session.save(p1);

        session.clear();

        Post f3 = session.load(Post.class, p3.getId(), -1);
        Post f2 = f3.getPrevious();
        Post f1 = f2.getPrevious();
        Post f4 = f3.getNext();

        assertNull(f1.getPrevious());
        assertEquals(p1.getId(), f2.getPrevious().getId());
        assertEquals(p2.getId(), f3.getPrevious().getId());
        assertEquals(p3.getId(), f4.getPrevious().getId());

        assertEquals(p2.getId(), f1.getNext().getId());
        assertEquals(p3.getId(), f2.getNext().getId());
        assertEquals(p4.getId(), f3.getNext().getId());
        assertNull(f4.getNext());

    }

    /**
     * @see Issue #99
     */
    @Test
    public void shouldDeleteAuthoredRelationship() {
        Author author = new Author();
        Post post = new Post();

        author.posts = new HashSet<>();
        author.posts.add(post);
        session.save(author);
        session.clear();

        author = session.load(Author.class, author.id);
        author.posts.clear();
        session.save(author);
        session.clear();

        author = session.load(Author.class, author.id);

        assertTrue(author.posts == null || author.posts.size() == 0);
    }

    /**
     * @see Issue #99
     */
    @Test
    public void shouldDeleteCommentsRelationship() {
        Author author = new Author();
        Post post = new Post();
        Comment comment = new Comment(post, author, "Try to delete me!");

        author.posts = new HashSet<>();
        author.posts.add(post);
        author.comments.add(comment);
        session.save(author);
        session.clear();

        author = session.load(Author.class, author.id);
        author.comments.clear();
        session.save(author);
        session.clear();

        author = session.load(Author.class, author.id);

        assertTrue(author.comments == null || author.comments.size() == 0);
    }
}
