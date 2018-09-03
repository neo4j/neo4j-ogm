/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

package org.neo4j.ogm.persistence.model;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.ogm.config.AutoIndexMode;
import org.neo4j.ogm.domain.generic_hierarchy.AnotherEntity;
import org.neo4j.ogm.domain.generic_hierarchy.ChildA;
import org.neo4j.ogm.domain.generic_hierarchy.ChildB;
import org.neo4j.ogm.domain.generic_hierarchy.ChildC;
import org.neo4j.ogm.domain.generic_hierarchy.Entity;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * Tests for label determination, especially in the context of class hierarchies with several abstract base classes
 * not containing explicit labels.<br>
 * From the reference documentation at <a href="https://neo4j.com/docs/ogm-manual/current/tutorial/#tutorial:annotations:graphid">2.4.4. Identifiers</a>:
 * <blockquote>"This is an abstract class, so you’ll see that the nodes do not inherit an Entity label, which is exactly what we want."</blockquote>
 *
 * @author Jonathan D'Orleans
 * @author Michael J. Simons
 */
public class LabelDeterminationTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void setupSessionFactory() {
        sessionFactory = new SessionFactory(getBaseConfiguration().build(), "org.neo4j.ogm.domain.generic_hierarchy");
    }

    @Before
    public void setUp() {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test
    public void shouldNotSaveEntityLabelAndMustRetrieveChildA() {
        ChildA a = new ChildA();
        a.setValue("ChildA");
        session.save(a);
        session.clear();

        assertThat(session.loadAll(Entity.class)).isEmpty();
        assertThat(session.load(Entity.class, a.getUuid())).isNull();

        ChildA dbA = session.load(ChildA.class, a.getUuid());
        assertThat(dbA).isNotNull();
        assertThat(dbA.getValue()).isEqualTo("ChildA");
    }

    @Test
    public void shouldNotSaveEntityLabelAndMustRetrieveChildAChildren() {
        ChildA a = new ChildA();
        ChildB b1 = new ChildB();
        ChildB b2 = new ChildB();
        ChildC c1 = new ChildC();
        a.add(b1);
        a.add(b2);
        a.add(c1);
        session.save(a);
        session.clear();

        // Asserts that classes without a label (abstract, without @NodeEntity) are not loaded.
        // Those two return an empty, immutable list and log a warning.
        assertThat(session.loadAll(Entity.class)).isEmpty();
        assertThat(session.loadAll(AnotherEntity.class)).isEmpty();

        // Asking for a node without label but with id can be ok. Nothing is found in these cases because
        // we never wrote a node without a label (see name "shouldNotSaveEntityLabelAndMustRetrieveChildAChildren").
        a.getChildren().forEach(c -> assertThat(session.load(AnotherEntity.class, c.getUuid())).isNull());

        // Concrete classes that have their simple class name as label must be loaded.
        Set<AnotherEntity> children = session.load(ChildA.class, a.getUuid()).getChildren();
        assertThat(children).contains(b1, b2, c1);

        //        FIXME - #414 - @PostLoad is not called in child overrided method (see ChildB)
        //        children.stream().filter(c -> c instanceof ChildB).forEach(b -> assertThat(((ChildB) b).getValue()).isNotNull());
    }

    @Test
    public void indexesShouldBeCreatedForLoadableClassesInHierarchy() {
        final IndexDescription[] expectedIndexes = new IndexDescription[] {
            new IndexDescription("User", "id"),
            new IndexDescription("Admin", "id"),
            new IndexDescription("ChildA", "uuid"),
            new IndexDescription("ChildB", "uuid"),
            new IndexDescription("ChildC", "uuid"),
            new IndexDescription("LabeledEntity", "uuid")
        };

        sessionFactory.runAutoIndexManager(getBaseConfiguration().autoIndex(AutoIndexMode.UPDATE.name()).build());
        GraphDatabaseService service = getGraphDatabaseService();

        try (Transaction tx = service.beginTx()) {
            IndexDescription[] indexes = StreamSupport.stream(service.schema().getIndexes().spliterator(), false)
                .map(IndexDescription::new).toArray(IndexDescription[]::new);

            assertThat(indexes).containsExactlyInAnyOrder(expectedIndexes);

            tx.success();
        }
    }

    static class IndexDescription {
        final String label;

        final String[] propertyKeys;

        public IndexDescription(String label, String... propertyKeys) {
            this.label = label;
            this.propertyKeys = propertyKeys;
        }

        public IndexDescription(IndexDefinition indexDefinition) {
            this.label = indexDefinition.getLabel().name();
            this.propertyKeys = StreamSupport.stream(indexDefinition.getPropertyKeys().spliterator(), false)
                .toArray(String[]::new);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof IndexDescription))
                return false;
            IndexDescription that = (IndexDescription) o;
            return Objects.equals(label, that.label) &&
                Arrays.equals(propertyKeys, that.propertyKeys);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(label);
            result = 31 * result + Arrays.hashCode(propertyKeys);
            return result;
        }

        @Override
        public String toString() {
            return "IndexDescription{" +
                "label='" + label + '\'' +
                ", propertyKeys=" + Arrays.toString(propertyKeys) +
                '}';
        }
    }
}
