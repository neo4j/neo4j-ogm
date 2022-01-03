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
package org.neo4j.ogm.persistence.examples.blog;

import static org.assertj.core.api.Assertions.*;

import java.util.HashSet;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.blog.Author;
import org.neo4j.ogm.domain.blog.Comment;
import org.neo4j.ogm.domain.blog.Post;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Vince Bickers
 */
public class BlogTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.blog");
    }

    @Before
    public void init() {
        session = sessionFactory.openSession();
        session.purgeDatabase();
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

        assertThat(p2.getPrevious()).isEqualTo(p1);

        assertThat(p1.getNext()).isEqualTo(p2);
        assertThat(p3.getPrevious()).isEqualTo(p2);

        assertThat(p2.getNext()).isEqualTo(p3);
        assertThat(p4.getPrevious()).isEqualTo(p3);

        assertThat(p3.getNext()).isEqualTo(p4);
        assertThat(p4.getNext()).isNull();

        session.save(p1);

        session.clear();

        Post f3 = session.load(Post.class, p3.getId(), -1);
        Post f2 = f3.getPrevious();
        Post f1 = f2.getPrevious();
        Post f4 = f3.getNext();

        assertThat(f1.getPrevious()).isNull();
        assertThat(f2.getPrevious().getId()).isEqualTo(p1.getId());
        assertThat(f3.getPrevious().getId()).isEqualTo(p2.getId());
        assertThat(f4.getPrevious().getId()).isEqualTo(p3.getId());

        assertThat(f1.getNext().getId()).isEqualTo(p2.getId());
        assertThat(f2.getNext().getId()).isEqualTo(p3.getId());
        assertThat(f3.getNext().getId()).isEqualTo(p4.getId());
        assertThat(f4.getNext()).isNull();
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

        assertThat(author.posts == null || author.posts.size() == 0).isTrue();
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

        assertThat(author.comments == null || author.comments.size() == 0).isTrue();
    }
}
