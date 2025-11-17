/*
 * Copyright (c) 2002-2025 "Neo4j,"
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
package org.neo4j.ogm.session;

import static org.assertj.core.api.Assertions.assertThatNoException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.ogm.session.gh1355.Archive;
import org.neo4j.ogm.session.gh1355.Book;
import org.neo4j.ogm.session.gh1355.Document;
import org.neo4j.ogm.session.gh1355.Product;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Michael J. Simons
 */
public class SessionCacheAndClearingTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    @BeforeAll
    public static void oneTimeSetUp() {

        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.session.gh1355");
        sessionFactory.openSession().purgeDatabase();
    }

    @Test
    void inheritanceWithConcreteTypeShouldWork() {
        Book book = new Book();
        book.setName(" ABook");
        var session = sessionFactory.openSession();
        session.save(book);
        Set<Archive> docsToUpdate = new HashSet<>();
        Set<Product> prodsToUpdate = new HashSet<>();
        for (var i = 0; i < 8000; i++) {
            Document doc = new Document();
            doc.setType("firstType");
            session.save(doc);
        }
        session.query(Document.class, "MATCH (d:Document) return d", Collections.emptyMap()).forEach(docsToUpdate::add);
        book.setArchives(docsToUpdate);
        session.save(book);
        session.query("MATCH (n: Document) WITH n LIMIT 300 DETACH DELETE n", Collections.emptyMap());

        for (var i = 0; i < 8000; i++) {
            Product product = new Product();
            product.setCategory("someCat");
            product.setProductCode("myCode");
            session.save(product);
        }
        session.query(Product.class, "MATCH (d:Product) return d", Collections.emptyMap()).forEach(prodsToUpdate::add);
        book.setProducts(prodsToUpdate);
        session.save(book);
        session.query("MATCH (n: Product) WITH n LIMIT 200 DETACH DELETE n", Collections.emptyMap());
        session.query("MATCH (n: Book) WITH n LIMIT 50 DETACH DELETE n", Collections.emptyMap());

        Book newBook = new Book();
        newBook.setName(" ANewBook");
        newBook.setArchives(docsToUpdate);
        session.save(newBook);
        newBook.setProducts(prodsToUpdate);
        session.save(newBook);

        var newSession = sessionFactory.openSession();
        assertThatNoException().isThrownBy(() -> newSession.loadAll(Book.class));
        assertThatNoException().isThrownBy(() -> newSession.loadAll(Product.class));
        assertThatNoException().isThrownBy(() -> newSession.loadAll(Document.class));
    }

}
