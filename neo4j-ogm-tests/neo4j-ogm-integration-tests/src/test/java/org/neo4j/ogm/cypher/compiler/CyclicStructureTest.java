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
package org.neo4j.ogm.cypher.compiler;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.context.EntityGraphMapper;
import org.neo4j.ogm.context.EntityMapper;
import org.neo4j.ogm.context.MappingContext;
import org.neo4j.ogm.domain.gh609.CyclicNodeType;
import org.neo4j.ogm.domain.gh609.RefField;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.request.RowStatementFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;
import org.neo4j.ogm.transaction.Transaction;

/**
 * This tests ensures that the combined behaviour of the {@link CypherContext} and the {@link CompileContext} doesn't
 * lead to an exponential growth of intermediate objects.
 *
 * @author Michael J. Simons
 * @author Andreas Berger
 */
public class CyclicStructureTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    @BeforeClass
    public static void initSesssionFactory() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.gh609");
    }

    /**
     * This issue was introduced by #407
     *
     * @see org.neo4j.ogm.persistence.examples.social.SocialIntegrationTest#shouldSaveObjectsToCorrectDepth
     * @see org.neo4j.ogm.persistence.examples.social.SocialIntegrationTest#shouldSaveAllDirectedRelationships
     */
    @Test // GH-609
    public void testCyclicStructure() throws Exception {
        long numberOfNodes = 10;
        long numberOfRefFields = 100;

        List<CyclicNodeType> nodes = new ArrayList<>();

        for (long i = 0; i < numberOfNodes; ++i) {
            nodes.add(new CyclicNodeType());
        }

        List<RefField> refFields = new ArrayList<>();
        for (long i = 0; i < numberOfRefFields; i++) {
            RefField field = new RefField().setNodeTypes(nodes);
            refFields.add(field);
        }
        for (CyclicNodeType nodeType : nodes) {
            nodeType.setSubordinateNodeTypes(nodes);
            nodeType.setRefFields(refFields);
        }

        Session session = sessionFactory.openSession();

        // Make sure the number of builders fits the number of expected relationships
        MultiStatementCypherCompiler compiler = (MultiStatementCypherCompiler) mapAndCompile((Neo4jSession) session,
            nodes.get(0), -1);
        List<RelationshipBuilder> newRelationshipBuilders = extractRelationshipBuilder(compiler);
        assertThat(newRelationshipBuilders).size()
            .isEqualTo(numberOfNodes * numberOfNodes + (numberOfNodes * numberOfRefFields * 2));

        // Make sure the session saves all relationships accordingly
        Transaction transaction = session.beginTransaction();
        session.save(nodes.get(0), -1);
        session.clear();
        transaction.commit();

        session = sessionFactory.openSession();
        Result result = session.query(""
                + " MATCH (c1:CyclicNodeType) - [s:SUBORDINATE] - (c2:CyclicNodeType),"
                + "       (c1) - [f:HAS_FIELD] -> (r:RefField)"
                + " RETURN count(distinct c1) as numberOfNodes, count(distinct s) as countRelCyc, "
                + "        count(distinct r)  as numberOfRefFields, count(distinct f) as countRelHasField",
            Collections.emptyMap());

        assertThat(result).hasSize(1);
        result.queryResults().forEach(record -> {
            assertThat(record.get("numberOfNodes")).isEqualTo(numberOfNodes);
            assertThat(record.get("numberOfRefFields")).isEqualTo(numberOfRefFields);

            assertThat(record.get("countRelCyc")).isEqualTo(numberOfNodes * numberOfNodes);
            assertThat(record.get("countRelHasField")).isEqualTo(numberOfNodes * numberOfRefFields);
        });
    }

    private static List<RelationshipBuilder> extractRelationshipBuilder(MultiStatementCypherCompiler compiler)
        throws Exception {
        Field newRelationshipBuildersField = MultiStatementCypherCompiler.class
            .getDeclaredField("newRelationshipBuilders");
        newRelationshipBuildersField.setAccessible(true);
        return (List<RelationshipBuilder>) newRelationshipBuildersField
            .get(compiler);
    }

    private static Compiler mapAndCompile(Neo4jSession session, Object object, int depth) {

        final MetaData metaData = session.metaData();
        final MappingContext mappingContext = new MappingContext(metaData);

        EntityMapper mapper = new EntityGraphMapper(metaData, mappingContext);
        CompileContext context = mapper.map(object, depth);
        Compiler compiler = context.getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());
        return compiler;
    }
}
