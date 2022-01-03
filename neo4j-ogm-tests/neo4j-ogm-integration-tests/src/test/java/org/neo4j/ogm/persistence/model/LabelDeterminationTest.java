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
package org.neo4j.ogm.persistence.model;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.ogm.config.AutoIndexMode;
import org.neo4j.ogm.domain.generic_hierarchy.AnotherEntity;
import org.neo4j.ogm.domain.generic_hierarchy.ChildA;
import org.neo4j.ogm.domain.generic_hierarchy.ChildB;
import org.neo4j.ogm.domain.generic_hierarchy.ChildC;
import org.neo4j.ogm.domain.generic_hierarchy.Entity;
import org.neo4j.ogm.domain.generic_hierarchy.EntityWithImplicitPlusAdditionalLabels;
import org.neo4j.ogm.domain.gh619.model.RealNode;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;
import org.neo4j.ogm.transaction.Transaction;
import org.neo4j.ogm.utils.EntityUtils;

/**
 * Tests for label determination, especially in the context of class hierarchies with several abstract base classes
 * not containing explicit labels.<br>
 * From the reference documentation at <a href="https://neo4j.com/docs/ogm-manual/current/tutorial/#tutorial:annotations:graphid">2.4.4. Identifiers</a>:
 * <blockquote>"This is an abstract class, so you’ll see that the nodes do not inherit an Entity label, which is exactly what we want."</blockquote>
 *
 * @author Jonathan D'Orleans
 * @author Michael J. Simons
 */
public class LabelDeterminationTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void setupSessionFactory() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.generic_hierarchy");
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
    }

    @Test
    public void indexesShouldBeCreatedForLoadableClassesInHierarchy() {
        assumeTrue(isVersionOrGreater("3.3"));
        final IndexDescription[] expectedIndexes = new IndexDescription[] {
            new IndexDescription("DefaultUser", "id"),
            new IndexDescription("Admin", "id"),
            new IndexDescription("ChildA", "uuid"),
            new IndexDescription("ChildB", "uuid"),
            new IndexDescription("ChildC", "uuid"),
            new IndexDescription("LabeledEntity", "uuid"),
            new IndexDescription("EntityWithImplicitPlusAdditionalLabels", "id")
        };
        sessionFactory
            .runAutoIndexManager(getBaseConfigurationBuilder().autoIndex(AutoIndexMode.UPDATE.name()).build());

        Session session = sessionFactory.openSession();

        try (Transaction tx = session.beginTransaction()) {
            Result indexResult = session.query("CALL db.indexes()", emptyMap());

            List<IndexDescription> indexDescriptions = new ArrayList<>();
            for (Map<String, Object> queryResult : indexResult.queryResults()) {

                // Skip 4.3 node label / rel types
                if(queryResult.containsKey("type") && queryResult.get("type").equals("LOOKUP")) {
                    continue;
                }

                // ensure compatibility with 3.x and 4.x
                String label = "unknown";
                if (queryResult.get("label") != null) { // 3.4(-)
                    label = (String) queryResult.get("label");
                } else if (queryResult.get("tokenNames") != null) { // 3.5
                    label = ((String[]) queryResult.get("tokenNames"))[0];
                } else if (queryResult.get("labelsOrTypes") != null) { // 4.0+
                    label = ((String[]) queryResult.get("labelsOrTypes"))[0];
                }

                indexDescriptions.add(new IndexDescription(label, (String[]) queryResult.get("properties")));
            }

            assertThat(indexDescriptions).containsExactlyInAnyOrder(expectedIndexes);

            tx.commit();
        }
    }

    @Test // GH-488
    public void shouldUpdateLabelWhenLoadingEntityInSameSession() {
        ChildA a = new ChildA();
        a.addLabel("A0");
        session.save(a);
        session.clear();

        ChildA dbA = session.load(ChildA.class, a.getUuid());
        assertThat(dbA.getLabels().size()).isEqualTo(1);
        assertThat(dbA.getLabels()).contains("A0");
        dbA.removeLabel("A0");
        dbA.addLabel("A1");
        session.save(dbA);
        session.clear();

        dbA = session.load(ChildA.class, a.getUuid());
        assertThat(dbA.getLabels().size()).isEqualTo(1);
        assertThat(dbA.getLabels()).contains("A1");
    }

    @Test // GH-488
    public void shouldUpdateLabelWhenLoadingEntityInNewSession() {
        ChildA a = new ChildA();
        a.addLabel("A0");
        session.save(a);

        Session newSession = sessionFactory.openSession();
        ChildA dbA = newSession.load(ChildA.class, a.getUuid());
        assertThat(dbA.getLabels().size()).isEqualTo(1);
        assertThat(dbA.getLabels()).contains("A0");
        dbA.removeLabel("A0");
        dbA.addLabel("A1");
        newSession.save(dbA);
        newSession.clear();

        dbA = newSession.load(ChildA.class, a.getUuid());
        assertThat(dbA.getLabels().size()).isEqualTo(1);
        assertThat(dbA.getLabels()).contains("A1");
    }

    @Test // GH-539
    public void labelsShouldBeDeleted() {
        Session throwAwaySession = sessionFactory.openSession();
        throwAwaySession
            .query("CREATE (a:EntityWithImplicitPlusAdditionalLabels:Label1:Label2 {id: 'myId'}) RETURN a", emptyMap());
        throwAwaySession.clear();

        throwAwaySession = sessionFactory.openSession();
        EntityWithImplicitPlusAdditionalLabels entity = session
            .load(EntityWithImplicitPlusAdditionalLabels.class, "myId");
        assertThat(entity.getLabels()).containsExactlyInAnyOrder("Label1", "Label2");
        entity.getLabels().remove("Label1");
        throwAwaySession.save(entity);
        throwAwaySession.clear();

        throwAwaySession = sessionFactory.openSession();
        entity = session.load(EntityWithImplicitPlusAdditionalLabels.class, "myId");
        assertThat(entity.getLabels()).containsExactlyInAnyOrder("Label2");
    }

    @Test // GH-619
    public void metaDataFromParentPackageShouldWork() {

        MetaData metaData = new MetaData("org.neo4j.ogm.domain.gh619");
        Collection<String> labels = EntityUtils.labels(new RealNode(), metaData);

        assertThat(labels).hasSize(1).containsExactly("real");
    }

    @Test // GH-619
    public void metaDataWithExplicitPackagesShouldWork() {

        MetaData metaData = new MetaData("org.neo4j.ogm.domain.gh619.base",
            "org.neo4j.ogm.domain.gh619.model");
        Collection<String> labels = EntityUtils.labels(new RealNode(), metaData);

        assertThat(labels).hasSize(1).containsExactly("real");
    }

    @Test // GH-619
    public void metaDataWithImplicitParentPackageShouldWork() {

        MetaData metaData = new MetaData("org.neo4j.ogm.domain.gh619.model");
        Collection<String> labels = EntityUtils.labels(new RealNode(), metaData);

        assertThat(labels).hasSize(1).containsExactly("real");
    }

    static class IndexDescription {
        final String[] labels;

        final String[] propertyKeys;

        IndexDescription(String label, String... propertyKeys) {
            this.labels = new String[] { label };
            this.propertyKeys = propertyKeys;
        }

        IndexDescription(IndexDefinition indexDefinition) {
            this.labels = StreamSupport.stream(indexDefinition.getLabels().spliterator(), false).toArray(String[]::new);
            this.propertyKeys = StreamSupport.stream(indexDefinition.getPropertyKeys().spliterator(), false)
                .toArray(String[]::new);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            IndexDescription that = (IndexDescription) o;
            return Arrays.equals(labels, that.labels) && Arrays.equals(propertyKeys, that.propertyKeys);
        }

        @Override
        public int hashCode() {
            int result = Arrays.hashCode(labels);
            result = 31 * result + Arrays.hashCode(propertyKeys);
            return result;
        }

        @Override
        public String toString() {
            return "IndexDescription{" +
                "labels=" + Arrays.toString(labels) +
                ", propertyKeys=" + Arrays.toString(propertyKeys) +
                '}';
        }
    }
}
